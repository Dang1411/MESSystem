<template>
  <div>
    <div class="d-flex justify-content-between align-items-center mb-4">
      <input v-model="keyword" @input="loadProducts" type="text" class="form-control" placeholder="Tìm theo mã, tên sản phẩm..." style="width:280px">
      <button class="btn btn-primary" @click="openCreate">➕ Thêm sản phẩm</button>
    </div>

    <div class="card card-mes">
      <div class="card-body p-0">
        <table class="table table-mes mb-0">
          <thead>
            <tr>
              <th>Mã sản phẩm</th>
              <th>Tên sản phẩm</th>
              <th>Loại linh kiện</th>
              <th>Trạng thái</th>
              <th>Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="5" class="text-center py-4">
                <div class="spinner-border spinner-border-sm text-primary mr-2"></div>Đang tải...
              </td>
            </tr>
            <tr v-else-if="!products.length">
              <td colspan="5" class="text-center py-4 text-muted">Chưa có sản phẩm nào</td>
            </tr>
            <tr v-for="p in products" :key="p.id">
              <td><code>{{ p.productCode }}</code></td>
              <td>{{ p.productName }}</td>
              <td>{{ p.componentType }}</td>
              <td>
                <span class="badge-status" :class="p.status === 'ACTIVE' ? 'badge-active' : 'badge-inactive'">
                  {{ p.status === 'ACTIVE' ? 'Hoạt động' : 'Ngừng' }}
                </span>
              </td>
              <td>
                <button class="btn btn-sm btn-outline-info mr-1" @click="openRoute(p)" title="Cấu hình quy trình">⚙ Quy trình</button>
                <button class="btn btn-sm btn-outline-primary mr-1" @click="openEdit(p)">✏ Sửa</button>
                <button class="btn btn-sm btn-outline-danger" @click="confirmDelete(p)">🗑 Xóa</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Create/Edit Modal -->
    <div class="modal fade" id="productModal" tabindex="-1">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">{{ editMode ? 'Sửa sản phẩm' : 'Thêm sản phẩm' }}</h5>
            <button type="button" class="close" data-dismiss="modal"><span>&times;</span></button>
          </div>
          <form @submit.prevent="saveProduct">
            <div class="modal-body">
              <div class="alert alert-danger" v-if="formError">{{ formError }}</div>
              <div class="form-group">
                <label>Mã sản phẩm <span class="text-danger">*</span></label>
                <input v-model="form.productCode" class="form-control" required :disabled="editMode">
              </div>
              <div class="form-group">
                <label>Tên sản phẩm <span class="text-danger">*</span></label>
                <input v-model="form.productName" class="form-control" required>
              </div>
              <div class="form-group">
                <label>Loại linh kiện</label>
                <input v-model="form.componentType" class="form-control" placeholder="PCB, Sensor, Module...">
              </div>
              <div class="form-group">
                <label>Mô tả</label>
                <textarea v-model="form.description" class="form-control" rows="3"></textarea>
              </div>
              <div class="form-group">
                <label>Trạng thái</label>
                <select v-model="form.status" class="form-control">
                  <option value="ACTIVE">Hoạt động</option>
                  <option value="INACTIVE">Ngừng</option>
                </select>
              </div>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" data-dismiss="modal">Hủy</button>
              <button type="submit" class="btn btn-primary" :disabled="saving">
                {{ saving ? 'Đang lưu...' : 'Lưu' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- Route Config Modal -->
    <div class="modal fade" id="routeModal" tabindex="-1">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">Cấu hình Quy trình: <strong>{{ selectedProduct ? selectedProduct.productName : '' }}</strong></h5>
            <button type="button" class="close" data-dismiss="modal"><span>&times;</span></button>
          </div>
          <div class="modal-body">
            <div class="alert alert-info small">
              Kéo thả hoặc nhấn ➕ để thêm công đoạn. Số thứ tự xác định thứ tự sản xuất.
            </div>
            <table class="table table-sm" v-if="routes.length">
              <thead class="thead-light">
                <tr><th>Thứ tự</th><th>Mã công đoạn</th><th>Tên công đoạn</th><th>Bắt buộc</th><th></th></tr>
              </thead>
              <tbody>
                <tr v-for="r in routes" :key="r.id">
                  <td><span class="badge badge-secondary">{{ r.stepOrder }}</span></td>
                  <td><code>{{ r.stepCode }}</code></td>
                  <td>{{ r.stepName }}</td>
                  <td>
                    <span class="badge" :class="r.isMandatory ? 'badge-danger' : 'badge-secondary'">
                      {{ r.isMandatory ? 'Bắt buộc' : 'Tùy chọn' }}
                    </span>
                  </td>
                  <td>
                    <button class="btn btn-sm btn-outline-danger" @click="removeRoute(r.id)">🗑</button>
                  </td>
                </tr>
              </tbody>
            </table>
            <p v-else class="text-muted text-center py-3">Chưa có công đoạn nào. Thêm công đoạn từ danh sách bên dưới.</p>

            <div class="add-step-area mt-3">
              <h6>Thêm công đoạn:</h6>
              <div class="row">
                <div class="col-md-6">
                  <select v-model="newStepId" class="form-control">
                    <option value="">-- Chọn công đoạn --</option>
                    <option v-for="s in availableSteps" :key="s.id" :value="s.id">
                      [{{ s.stepCode }}] {{ s.stepName }}
                    </option>
                  </select>
                </div>
                <div class="col-md-3">
                  <input v-model.number="newStepOrder" type="number" min="1" class="form-control" placeholder="Thứ tự">
                </div>
                <div class="col-md-3">
                  <button class="btn btn-success btn-block" @click="addRoute" :disabled="!newStepId">➕ Thêm</button>
                </div>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-dismiss="modal">Đóng</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import productApi from '../api/products'
import processStepApi from '../api/processSteps'
/* global $ */

export default {
  name: 'Products',
  data() {
    return {
      products: [], allSteps: [], routes: [],
      loading: false, saving: false,
      keyword: '', editMode: false,
      form: { productCode: '', productName: '', componentType: '', description: '', status: 'ACTIVE' },
      editId: null, formError: '',
      selectedProduct: null,
      newStepId: '', newStepOrder: 1
    }
  },
  computed: {
    availableSteps() {
      const usedIds = this.routes.map(r => r.stepId)
      return this.allSteps.filter(s => s.isActive && !usedIds.includes(s.id))
    }
  },
  methods: {
    async loadProducts() {
      this.loading = true
      try {
        const res = await productApi.getAll(this.keyword)
        this.products = res.data
      } finally { this.loading = false }
    },
    async loadSteps() {
      const res = await processStepApi.getAll()
      this.allSteps = res.data
    },
    openCreate() {
      this.editMode = false; this.editId = null; this.formError = ''
      this.form = { productCode: '', productName: '', componentType: '', description: '', status: 'ACTIVE' }
      $('#productModal').modal('show')
    },
    openEdit(p) {
      this.editMode = true; this.editId = p.id; this.formError = ''
      this.form = { productCode: p.productCode, productName: p.productName, componentType: p.componentType || '', description: p.description || '', status: p.status }
      $('#productModal').modal('show')
    },
    async saveProduct() {
      this.saving = true; this.formError = ''
      try {
        if (this.editMode) await productApi.update(this.editId, this.form)
        else await productApi.create(this.form)
        $('#productModal').modal('hide')
        this.loadProducts()
      } catch (e) {
        this.formError = e.response?.data?.message || 'Lỗi khi lưu sản phẩm'
      } finally { this.saving = false }
    },
    async confirmDelete(p) {
      if (!confirm(`Xóa sản phẩm "${p.productName}"?`)) return
      try {
        await productApi.delete(p.id)
        this.loadProducts()
      } catch (e) {
        alert(e.response?.data?.message || 'Không thể xóa sản phẩm')
      }
    },
    async openRoute(p) {
      this.selectedProduct = p
      this.newStepId = ''; this.newStepOrder = 1
      const res = await productApi.getRoutes(p.id)
      this.routes = res.data
      const nextOrder = this.routes.length > 0 ? Math.max(...this.routes.map(r => r.stepOrder)) + 1 : 1
      this.newStepOrder = nextOrder
      $('#routeModal').modal('show')
    },
    async addRoute() {
      if (!this.newStepId) return
      try {
        await productApi.saveRoutes(this.selectedProduct.id, [{
          processStepId: this.newStepId,
          stepOrder: this.newStepOrder,
          isMandatory: true
        }])
        const res = await productApi.getRoutes(this.selectedProduct.id)
        this.routes = res.data
        this.newStepId = ''
        this.newStepOrder = Math.max(...this.routes.map(r => r.stepOrder)) + 1
      } catch (e) {
        alert(e.response?.data?.message || 'Lỗi khi thêm công đoạn')
      }
    },
    async removeRoute(routeId) {
      if (!confirm('Xóa công đoạn khỏi quy trình?')) return
      try {
        await productApi.deleteRouteStep(this.selectedProduct.id, routeId)
        const res = await productApi.getRoutes(this.selectedProduct.id)
        this.routes = res.data
      } catch (e) {
        alert(e.response?.data?.message || 'Lỗi khi xóa công đoạn')
      }
    }
  },
  mounted() {
    this.loadProducts()
    this.loadSteps()
  }
}
</script>
