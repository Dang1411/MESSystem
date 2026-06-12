<template>
  <div>
    <!-- Filters -->
    <div class="card card-mes mb-4">
      <div class="card-body">
        <div class="row align-items-end">
          <div class="col-md-4">
            <label class="small font-weight-bold">Tìm kiếm</label>
            <input v-model="filters.keyword" @input="loadOrders" type="text" class="form-control" placeholder="Mã lệnh, sản phẩm...">
          </div>
          <div class="col-md-3">
            <label class="small font-weight-bold">Trạng thái</label>
            <select v-model="filters.status" @change="loadOrders" class="form-control">
              <option value="">Tất cả</option>
              <option value="CREATED">Mới tạo</option>
              <option value="IN_PROGRESS">Đang thực hiện</option>
              <option value="COMPLETED">Hoàn thành</option>
              <option value="CANCELLED">Đã hủy</option>
            </select>
          </div>
          <div class="col-md-3">
            <button class="btn btn-outline-secondary" @click="resetFilters">🔄 Đặt lại</button>
          </div>
          <div class="col-md-2 text-right" v-if="isSupervisor">
            <button class="btn btn-primary" @click="openCreate">➕ Tạo lệnh</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Orders Table -->
    <div class="card card-mes">
      <div class="card-body p-0">
        <table class="table table-mes mb-0">
          <thead>
            <tr>
              <th>Mã lệnh</th>
              <th>Sản phẩm</th>
              <th>Số lượng</th>
              <th>Tiến độ</th>
              <th>Ngày bắt đầu</th>
              <th>Trạng thái</th>
              <th>Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="7" class="text-center py-4">
                <div class="spinner-border spinner-border-sm text-primary mr-2"></div>Đang tải...
              </td>
            </tr>
            <tr v-else-if="!orders.length">
              <td colspan="7" class="text-center py-4 text-muted">Không có lệnh sản xuất nào</td>
            </tr>
            <tr v-for="o in orders" :key="o.id">
              <td><strong><code>{{ o.orderCode }}</code></strong></td>
              <td>{{ o.productName }}<br><small class="text-muted">{{ o.productCode }}</small></td>
              <td>{{ o.completedQuantity || 0 }} / {{ o.plannedQuantity }}</td>
              <td style="min-width:120px">
                <div class="progress" style="height:8px">
                  <div class="progress-bar bg-success" :style="{ width: progressRate(o) + '%' }"></div>
                </div>
                <small class="text-muted">{{ progressRate(o) }}%</small>
              </td>
              <td>{{ formatDate(o.startDate) }}</td>
              <td>
                <span class="badge-status" :class="statusBadge(o.status)">{{ statusLabel(o.status) }}</span>
              </td>
              <td>
                <button class="btn btn-sm btn-outline-info mr-1" @click="viewSerials(o)" title="Xem serials">📋</button>
                <button v-if="isSupervisor && o.status !== 'CANCELLED' && o.status !== 'COMPLETED'"
                  class="btn btn-sm btn-outline-warning" @click="openUpdateStatus(o)" title="Cập nhật trạng thái">🔄</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Create Modal -->
    <div class="modal fade" id="orderModal" tabindex="-1">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">Tạo Lệnh Sản xuất</h5>
            <button type="button" class="close" data-dismiss="modal"><span>&times;</span></button>
          </div>
          <form @submit.prevent="createOrder">
            <div class="modal-body">
              <div class="alert alert-danger" v-if="formError">{{ formError }}</div>

              <!-- <div class="form-group">
                <label>Mã lệnh <span class="text-danger">*</span></label>
                <input v-model="form.orderCode" class="form-control" required placeholder="VD: PO003">
              </div> -->
              
              <div class="form-group">
                <label>Sản phẩm <span class="text-danger">*</span></label>
                <select v-model="form.productId" class="form-control" required>
                  <option value="">-- Chọn sản phẩm --</option>
                  <option v-for="p in products" :key="p.id" :value="p.id">
                    [{{ p.productCode }}] {{ p.productName }}
                  </option>
                </select>
              </div>
              <div class="form-row">
                <div class="form-group col-md-6">
                  <label>Số lượng <span class="text-danger">*</span></label>
                  <input v-model.number="form.plannedQuantity" type="number" min="1" class="form-control" required>
                </div>
              </div>
              <div class="form-row">
                <div class="form-group col-md-6">
                  <label>Ngày bắt đầu</label>
                  <input v-model="form.startDate" type="date" class="form-control">
                </div>
                <div class="form-group col-md-6">
                  <label>Ngày kết thúc</label>
                  <input v-model="form.endDate" type="date" class="form-control">
                </div>
              </div>
              <div class="form-group">
                <label>Ghi chú</label>
                <textarea v-model="form.notes" class="form-control" rows="2"></textarea>
              </div>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" data-dismiss="modal">Hủy</button>
              <button type="submit" class="btn btn-primary" :disabled="saving">
                {{ saving ? 'Đang tạo...' : 'Tạo lệnh' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- Status Update Modal -->
    <div class="modal fade" id="statusModal" tabindex="-1">
      <div class="modal-dialog modal-sm">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">Cập nhật trạng thái</h5>
            <button type="button" class="close" data-dismiss="modal"><span>&times;</span></button>
          </div>
          <div class="modal-body" v-if="selectedOrder">
            <p>Lệnh: <strong>{{ selectedOrder.orderCode }}</strong></p>
            <div class="form-group">
              <select v-model="newStatus" class="form-control">
                <option value="IN_PROGRESS">Đang thực hiện</option>
                <option value="COMPLETED">Hoàn thành</option>
                <option value="CANCELLED">Hủy</option>
              </select>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-dismiss="modal">Hủy</button>
            <button class="btn btn-warning" @click="updateStatus">Cập nhật</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Serials Modal -->
    <div class="modal fade" id="serialsModal" tabindex="-1">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              Danh sách Serial — {{ selectedOrder ? selectedOrder.orderCode : '' }}
            </h5>
            <button type="button" class="close" data-dismiss="modal"><span>&times;</span></button>
          </div>
          <div class="modal-body p-0">
            <table class="table table-sm mb-0">
              <thead class="thead-dark">
                <tr><th>#</th><th>Serial Code</th><th>Công đoạn hiện tại</th><th>Trạng thái</th></tr>
              </thead>
              <tbody>
                <tr v-for="(s, i) in serials" :key="s.id">
                  <td>{{ i + 1 }}</td>
                  <td><code>{{ s.serialCode }}</code></td>
                  <td>{{ s.currentStepName || '—' }}</td>
                  <td>
                    <span class="badge-status" :class="serialBadge(s.status)">{{ s.status }}</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import orderApi from '../api/productionOrders'
import productApi from '../api/products'
import { mapGetters } from 'vuex'
/* global $ */

export default {
  name: 'ProductionOrders',
  data() {
    return {
      orders: [], products: [], serials: [],
      loading: false, saving: false,
      filters: { keyword: '', status: '' },
      form: { orderCode: '', productId: '', plannedQuantity: 1, startDate: '', endDate: '', notes: '' },
      formError: '',
      selectedOrder: null, newStatus: 'IN_PROGRESS'
    }
  },
  computed: {
    ...mapGetters('auth', ['userRole']),
    isSupervisor() { return this.userRole === 'SUPERVISOR' }
  },
  methods: {
    async loadOrders() {
      this.loading = true
      try {
        const res = await orderApi.getAll(this.filters)
        this.orders = res.data
      } finally { this.loading = false }
    },
    async loadProducts() {
      const res = await productApi.getAll()
      this.products = res.data.filter(p => p.status === 'ACTIVE')
    },
    resetFilters() {
      this.filters = { keyword: '', status: '' }
      this.loadOrders()
    },
    progressRate(o) {
      if (!o.plannedQuantity) return 0
      return Math.round(((o.completedQuantity || 0) / o.plannedQuantity) * 100)
    },
    formatDate(d) {
      if (!d) return '—'
      return new Date(d).toLocaleDateString('vi-VN')
    },
    statusBadge(s) {
      const m = { CREATED: 'badge-created', IN_PROGRESS: 'badge-inprogress', COMPLETED: 'badge-completed', CANCELLED: 'badge-cancelled' }
      return m[s] || 'badge-secondary'
    },
    statusLabel(s) {
      const m = { CREATED: 'Mới tạo', IN_PROGRESS: 'Đang thực hiện', COMPLETED: 'Hoàn thành', CANCELLED: 'Đã hủy' }
      return m[s] || s
    },
    serialBadge(s) {
      const m = {
        WAITING: 'badge-waiting', IN_PROGRESS: 'badge-inprogress', OK: 'badge-ok',
        NG: 'badge-ng', REWORK: 'badge-rework', SCRAP: 'badge-scrap',
        HOLD: 'badge-hold', FINISHED: 'badge-finished'
      }
      return m[s] || ''
    },
    openCreate() {
      this.formError = ''
      this.form = { orderCode: '', productId: '', plannedQuantity: 1, startDate: '', endDate: '', notes: '' }
      $('#orderModal').modal('show')
    },
    async createOrder() {
      this.saving = true; this.formError = ''
      try {
        await orderApi.create(this.form)
        $('#orderModal').modal('hide')
        this.loadOrders()
      } catch (e) {
        this.formError = e.response?.data?.message || 'Lỗi khi tạo lệnh'
      } finally { this.saving = false }
    },
    openUpdateStatus(o) {
      this.selectedOrder = o
      this.newStatus = o.status
      $('#statusModal').modal('show')
    },
    async updateStatus() {
      try {
        await orderApi.updateStatus(this.selectedOrder.id, this.newStatus)
        $('#statusModal').modal('hide')
        this.loadOrders()
      } catch (e) {
        alert(e.response?.data?.message || 'Lỗi khi cập nhật trạng thái')
      }
    },
    async viewSerials(o) {
      this.selectedOrder = o
      const res = await orderApi.getSerials(o.id)
      this.serials = res.data
      $('#serialsModal').modal('show')
    }
  },
  mounted() {
    this.loadOrders()
    this.loadProducts()
  }
}
</script>
