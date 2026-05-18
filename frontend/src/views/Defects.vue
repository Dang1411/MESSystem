<template>
  <div>
    <!-- Tabs: Defects / Logs -->
    <ul class="nav nav-tabs mb-4">
      <li class="nav-item">
        <a class="nav-link" :class="{ active: tab === 'defects' }" href="#" @click.prevent="tab = 'defects'">
          🐛 Danh mục lỗi
        </a>
      </li>
      <li class="nav-item">
        <a class="nav-link" :class="{ active: tab === 'logs' }" href="#" @click.prevent="tab = 'logs'">
          📋 Nhật ký lỗi
        </a>
      </li>
    </ul>

    <!-- Defects Tab -->
    <div v-if="tab === 'defects'">
      <div class="d-flex justify-content-between align-items-center mb-3">
        <p class="text-muted mb-0">Quản lý các loại lỗi trong sản xuất</p>
        <button class="btn btn-primary" @click="openCreate">➕ Thêm loại lỗi</button>
      </div>
      <div class="card card-mes">
        <div class="card-body p-0">
          <table class="table table-mes mb-0">
            <thead>
              <tr><th>Mã lỗi</th><th>Tên lỗi</th><th>Mô tả</th><th>Trạng thái</th><th>Thao tác</th></tr>
            </thead>
            <tbody>
              <tr v-if="loading"><td colspan="5" class="text-center py-4"><div class="spinner-border spinner-border-sm text-primary mr-2"></div>Đang tải...</td></tr>
              <tr v-else-if="!defects.length"><td colspan="5" class="text-center py-4 text-muted">Chưa có loại lỗi nào</td></tr>
              <tr v-for="d in defects" :key="d.id">
                <td><code>{{ d.defectCode }}</code></td>
                <td><strong>{{ d.defectName }}</strong></td>
                <td class="text-muted small">{{ d.description || '—' }}</td>
                <td>
                  <span class="badge-status" :class="d.isActive ? 'badge-active' : 'badge-inactive'">
                    {{ d.isActive ? 'Hoạt động' : 'Tắt' }}
                  </span>
                </td>
                <td>
                  <button class="btn btn-sm btn-outline-primary mr-1" @click="openEdit(d)">✏ Sửa</button>
                  <button class="btn btn-sm btn-outline-danger" @click="confirmDelete(d)">🗑 Xóa</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Logs Tab -->
    <div v-if="tab === 'logs'">
      <div class="card card-mes mb-4">
        <div class="card-header bg-white"><strong>📋 Nhật ký lỗi sản xuất</strong></div>
        <div class="card-body p-0">
          <table class="table table-mes mb-0">
            <thead>
              <tr><th>Serial Code</th><th>Loại lỗi</th><th>Công đoạn</th><th>Hành động</th><th>Người báo cáo</th><th>Thời gian</th></tr>
            </thead>
            <tbody>
              <tr v-if="logsLoading"><td colspan="6" class="text-center py-4"><div class="spinner-border spinner-border-sm text-primary mr-2"></div>Đang tải...</td></tr>
              <tr v-else-if="!logs.length"><td colspan="6" class="text-center py-4 text-muted">Chưa có nhật ký lỗi</td></tr>
              <tr v-for="l in logs" :key="l.id">
                <td><code>{{ l.serialCode }}</code></td>
                <td><span class="badge badge-danger">{{ l.defectName }}</span></td>
                <td>{{ l.stepName }}</td>
                <td class="small">{{ l.actionTaken }}</td>
                <td>{{ l.reportedByName }}</td>
                <td class="text-muted small">{{ formatDateTime(l.createdAt) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Defect Modal -->
    <div class="modal fade" id="defectModal" tabindex="-1">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">{{ editMode ? 'Sửa loại lỗi' : 'Thêm loại lỗi' }}</h5>
            <button type="button" class="close" data-dismiss="modal"><span>&times;</span></button>
          </div>
          <form @submit.prevent="saveDefect">
            <div class="modal-body">
              <div class="alert alert-danger" v-if="formError">{{ formError }}</div>
              <div class="form-group">
                <label>Mã lỗi <span class="text-danger">*</span></label>
                <input v-model="form.defectCode" class="form-control" required :disabled="editMode">
              </div>
              <div class="form-group">
                <label>Tên lỗi <span class="text-danger">*</span></label>
                <input v-model="form.defectName" class="form-control" required>
              </div>
              <div class="form-group">
                <label>Mô tả</label>
                <textarea v-model="form.description" class="form-control" rows="3"></textarea>
              </div>
              <div class="form-group">
                <div class="custom-control custom-switch">
                  <input type="checkbox" class="custom-control-input" id="defectActive" v-model="form.isActive">
                  <label class="custom-control-label" for="defectActive">Hoạt động</label>
                </div>
              </div>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" data-dismiss="modal">Hủy</button>
              <button type="submit" class="btn btn-primary" :disabled="saving">{{ saving ? 'Đang lưu...' : 'Lưu' }}</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import defectApi from '../api/defects'
/* global $ */

export default {
  name: 'Defects',
  data() {
    return {
      tab: 'defects',
      defects: [], logs: [],
      loading: false, logsLoading: false, saving: false,
      editMode: false, editId: null, formError: '',
      form: { defectCode: '', defectName: '', description: '', isActive: true }
    }
  },
  watch: {
    tab(val) {
      if (val === 'logs') this.loadLogs()
    }
  },
  methods: {
    async loadDefects() {
      this.loading = true
      try {
        const res = await defectApi.getAll()
        this.defects = res.data
      } finally { this.loading = false }
    },
    async loadLogs() {
      this.logsLoading = true
      try {
        const res = await defectApi.getLogs()
        this.logs = res.data
      } finally { this.logsLoading = false }
    },
    openCreate() {
      this.editMode = false; this.editId = null; this.formError = ''
      this.form = { defectCode: '', defectName: '', description: '', isActive: true }
      $('#defectModal').modal('show')
    },
    openEdit(d) {
      this.editMode = true; this.editId = d.id; this.formError = ''
      this.form = { defectCode: d.defectCode, defectName: d.defectName, description: d.description || '', isActive: d.isActive }
      $('#defectModal').modal('show')
    },
    async saveDefect() {
      this.saving = true; this.formError = ''
      try {
        if (this.editMode) await defectApi.update(this.editId, this.form)
        else await defectApi.create(this.form)
        $('#defectModal').modal('hide')
        this.loadDefects()
      } catch (e) {
        this.formError = e.response?.data?.message || 'Lỗi khi lưu'
      } finally { this.saving = false }
    },
    async confirmDelete(d) {
      if (!confirm(`Xóa loại lỗi "${d.defectName}"?`)) return
      try {
        await defectApi.delete(d.id)
        this.loadDefects()
      } catch (e) {
        alert(e.response?.data?.message || 'Không thể xóa')
      }
    },
    formatDateTime(dt) {
      if (!dt) return '—'
      return new Date(dt).toLocaleString('vi-VN')
    }
  },
  mounted() { this.loadDefects() }
}
</script>
