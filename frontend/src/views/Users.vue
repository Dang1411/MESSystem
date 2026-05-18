<template>
  <div>
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div class="d-flex gap-2">
        <input v-model="keyword" @input="loadUsers" type="text" class="form-control" placeholder="Tìm theo tên, username..." style="width:280px">
      </div>
      <button class="btn btn-primary" @click="openCreate">➕ Thêm người dùng</button>
    </div>

    <div class="card card-mes">
      <div class="card-body p-0">
        <table class="table table-mes mb-0">
          <thead>
            <tr>
              <th>Mã NV</th>
              <th>Họ tên</th>
              <th>Username</th>
              <th>Vai trò</th>
              <th>Trạng thái</th>
              <th>Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="6" class="text-center py-4">
                <div class="spinner-border spinner-border-sm text-primary mr-2"></div> Đang tải...
              </td>
            </tr>
            <tr v-else-if="!users.length">
              <td colspan="6" class="text-center py-4 text-muted">Không có dữ liệu</td>
            </tr>
            <tr v-for="u in users" :key="u.id">
              <td><code>{{ u.employeeCode }}</code></td>
              <td>{{ u.fullName }}</td>
              <td>{{ u.username }}</td>
              <td>
                <span class="badge" :class="roleBadge(u.roleName)">{{ u.roleName }}</span>
              </td>
              <td>
                <span class="badge-status" :class="u.isActive ? 'badge-active' : 'badge-inactive'">
                  {{ u.isActive ? 'Hoạt động' : 'Tắt' }}
                </span>
              </td>
              <td>
                <button class="btn btn-sm btn-outline-primary mr-1" @click="openEdit(u)">✏ Sửa</button>
                <button class="btn btn-sm btn-outline-danger" @click="confirmDelete(u)">🗑 Xóa</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modal Create/Edit -->
    <div class="modal fade" id="userModal" tabindex="-1">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">{{ editMode ? 'Sửa người dùng' : 'Thêm người dùng' }}</h5>
            <button type="button" class="close" data-dismiss="modal"><span>&times;</span></button>
          </div>
          <form @submit.prevent="saveUser">
            <div class="modal-body">
              <div class="alert alert-danger" v-if="formError">{{ formError }}</div>
              <div class="form-group">
                <label>Mã nhân viên <span class="text-danger">*</span></label>
                <input v-model="form.employeeCode" class="form-control" required :disabled="editMode">
              </div>
              <div class="form-group">
                <label>Họ tên <span class="text-danger">*</span></label>
                <input v-model="form.fullName" class="form-control" required>
              </div>
              <div class="form-group">
                <label>Username <span class="text-danger">*</span></label>
                <input v-model="form.username" class="form-control" required :disabled="editMode">
              </div>
              <div class="form-group" v-if="!editMode">
                <label>Mật khẩu <span class="text-danger">*</span></label>
                <input v-model="form.password" type="password" class="form-control" required>
              </div>
              <div class="form-group" v-if="editMode">
                <label>Mật khẩu mới (để trống nếu không đổi)</label>
                <input v-model="form.password" type="password" class="form-control">
              </div>
              <div class="form-group">
                <label>Vai trò <span class="text-danger">*</span></label>
                <select v-model="form.roleId" class="form-control" required>
                  <option value="">-- Chọn vai trò --</option>
                  <option v-for="r in roles" :key="r.id" :value="r.id">{{ r.name }}</option>
                </select>
              </div>
              <div class="form-group">
                <div class="custom-control custom-switch">
                  <input type="checkbox" class="custom-control-input" id="activeSwitch" v-model="form.isActive">
                  <label class="custom-control-label" for="activeSwitch">Hoạt động</label>
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
import userApi from '../api/users'
/* global $ */

export default {
  name: 'Users',
  data() {
    return {
      users: [], roles: [],
      loading: false, saving: false,
      keyword: '', editMode: false,
      form: { employeeCode: '', fullName: '', username: '', password: '', roleId: '', isActive: true },
      editId: null,
      formError: ''
    }
  },
  methods: {
    async loadUsers() {
      this.loading = true
      try {
        const res = await userApi.getAll(this.keyword)
        this.users = res.data
      } finally { this.loading = false }
    },
    async loadRoles() {
      const res = await userApi.getRoles()
      this.roles = res.data
    },
    roleBadge(role) {
      const map = { SUPERVISOR: 'badge-warning', OPERATOR: 'badge-info', QC: 'badge-success' }
      return map[role] || 'badge-secondary'
    },
    openCreate() {
      this.editMode = false
      this.editId = null
      this.formError = ''
      this.form = { employeeCode: '', fullName: '', username: '', password: '', roleId: '', isActive: true }
      $('#userModal').modal('show')
    },
    openEdit(u) {
      this.editMode = true
      this.editId = u.id
      this.formError = ''
      const role = this.roles.find(r => r.name === u.roleName)
      this.form = {
        employeeCode: u.employeeCode,
        fullName: u.fullName,
        username: u.username,
        password: '',
        roleId: role ? role.id : '',
        isActive: u.isActive
      }
      $('#userModal').modal('show')
    },
    async saveUser() {
      this.saving = true
      this.formError = ''
      try {
        const payload = { ...this.form }
        if (this.editMode && !payload.password) delete payload.password
        if (this.editMode) {
          await userApi.update(this.editId, payload)
        } else {
          await userApi.create(payload)
        }
        $('#userModal').modal('hide')
        this.loadUsers()
      } catch (e) {
        this.formError = e.response?.data?.message || 'Lỗi khi lưu người dùng'
      } finally { this.saving = false }
    },
    async confirmDelete(u) {
      if (!confirm(`Xóa người dùng "${u.fullName}"?`)) return
      try {
        await userApi.delete(u.id)
        this.loadUsers()
      } catch (e) {
        alert(e.response?.data?.message || 'Không thể xóa người dùng')
      }
    }
  },
  mounted() {
    this.loadUsers()
    this.loadRoles()
  }
}
</script>
