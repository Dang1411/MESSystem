<template>
  <div>
    <div class="d-flex justify-content-between align-items-center mb-4">
      <p class="text-muted mb-0">Quản lý các công đoạn trong quy trình sản xuất</p>
      <button class="btn btn-primary" @click="openCreate">➕ Thêm công đoạn</button>
    </div>

    <div class="card card-mes">
      <div class="card-body p-0">
        <table class="table table-mes mb-0">
          <thead>
            <tr>
              <th>Mã công đoạn</th>
              <th>Tên công đoạn</th>
              <th>Mô tả</th>
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
            <tr v-else-if="!steps.length">
              <td colspan="5" class="text-center py-4 text-muted">Chưa có công đoạn nào</td>
            </tr>
            <tr v-for="s in steps" :key="s.id">
              <td><code>{{ s.stepCode }}</code></td>
              <td><strong>{{ s.stepName }}</strong></td>
              <td class="text-muted small">{{ s.description || '—' }}</td>
              <td>
                <span class="badge-status" :class="s.isActive ? 'badge-active' : 'badge-inactive'">
                  {{ s.isActive ? 'Hoạt động' : 'Tắt' }}
                </span>
              </td>
              <td>
                <button class="btn btn-sm btn-outline-primary mr-1" @click="openEdit(s)">✏ Sửa</button>
                <button class="btn btn-sm btn-outline-danger" @click="confirmDelete(s)">🗑 Xóa</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modal -->
    <div class="modal fade" id="stepModal" tabindex="-1">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">{{ editMode ? 'Sửa công đoạn' : 'Thêm công đoạn' }}</h5>
            <button type="button" class="close" data-dismiss="modal"><span>&times;</span></button>
          </div>
          <form @submit.prevent="saveStep">
            <div class="modal-body">
              <div class="alert alert-danger" v-if="formError">{{ formError }}</div>
              <div class="form-group">
                <label>Mã công đoạn <span class="text-danger">*</span></label>
                <input v-model="form.stepCode" class="form-control" required :disabled="editMode">
              </div>
              <div class="form-group">
                <label>Tên công đoạn <span class="text-danger">*</span></label>
                <input v-model="form.stepName" class="form-control" required>
              </div>
              <div class="form-group">
                <label>Mô tả</label>
                <textarea v-model="form.description" class="form-control" rows="3"></textarea>
              </div>
              <div class="form-group">
                <div class="custom-control custom-switch">
                  <input type="checkbox" class="custom-control-input" id="stepActive" v-model="form.isActive">
                  <label class="custom-control-label" for="stepActive">Hoạt động</label>
                </div>
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
  </div>
</template>

<script>
import processStepApi from '../api/processSteps'
/* global $ */

export default {
  name: 'ProcessSteps',
  data() {
    return {
      steps: [], loading: false, saving: false,
      editMode: false, editId: null, formError: '',
      form: { stepCode: '', stepName: '', description: '', isActive: true }
    }
  },
  methods: {
    async loadSteps() {
      this.loading = true
      try {
        const res = await processStepApi.getAll()
        this.steps = res.data
      } finally { this.loading = false }
    },
    openCreate() {
      this.editMode = false; this.editId = null; this.formError = ''
      this.form = { stepCode: '', stepName: '', description: '', isActive: true }
      $('#stepModal').modal('show')
    },
    openEdit(s) {
      this.editMode = true; this.editId = s.id; this.formError = ''
      this.form = { stepCode: s.stepCode, stepName: s.stepName, description: s.description || '', isActive: s.isActive }
      $('#stepModal').modal('show')
    },
    async saveStep() {
      this.saving = true; this.formError = ''
      try {
        if (this.editMode) await processStepApi.update(this.editId, this.form)
        else await processStepApi.create(this.form)
        $('#stepModal').modal('hide')
        this.loadSteps()
      } catch (e) {
        this.formError = e.response?.data?.message || 'Lỗi khi lưu'
      } finally { this.saving = false }
    },
    async confirmDelete(s) {
      if (!confirm(`Xóa công đoạn "${s.stepName}"?`)) return
      try {
        await processStepApi.delete(s.id)
        this.loadSteps()
      } catch (e) {
        alert(e.response?.data?.message || 'Không thể xóa công đoạn')
      }
    }
  },
  mounted() { this.loadSteps() }
}
</script>
