<template>
  <div class="login-page">
    <div class="login-left">
      <div class="login-brand">
        <div class="brand-icon">⚙</div>
        <h1>MES System</h1>
        <p>Hệ thống Quản trị Sản xuất<br>Linh kiện Điện tử</p>
      </div>
      <div class="brand-features">
        <div class="feature-item"><span>✅</span> Quản lý Lệnh Sản xuất</div>
        <div class="feature-item"><span>✅</span> Truy xuất Nguồn gốc</div>
        <div class="feature-item"><span>✅</span> Kiểm soát Chất lượng</div>
        <div class="feature-item"><span>✅</span> Báo cáo Thống kê</div>
      </div>
    </div>

    <div class="login-right">
      <div class="login-card">
        <div class="login-header">
          <h2>Đăng nhập</h2>
          <p class="text-muted">Nhập thông tin tài khoản để tiếp tục</p>
        </div>

        <form @submit.prevent="handleLogin">
          <div class="alert alert-danger" v-if="error">{{ error }}</div>

          <div class="form-group">
            <label>Tên đăng nhập</label>
            <div class="input-group">
              <div class="input-group-prepend">
                <span class="input-group-text">👤</span>
              </div>
              <input
                v-model="form.username"
                type="text"
                class="form-control"
                placeholder="Nhập tên đăng nhập"
                required
                autofocus
              />
            </div>
          </div>

          <div class="form-group">
            <label>Mật khẩu</label>
            <div class="input-group">
              <div class="input-group-prepend">
                <span class="input-group-text">🔒</span>
              </div>
              <input
                v-model="form.password"
                :type="showPassword ? 'text' : 'password'"
                class="form-control"
                placeholder="Nhập mật khẩu"
                required
              />
              <div class="input-group-append">
                <button type="button" class="btn btn-outline-secondary" @click="showPassword = !showPassword">
                  {{ showPassword ? '🙈' : '👁' }}
                </button>
              </div>
            </div>
          </div>

          <button type="submit" class="btn btn-primary btn-block btn-login" :disabled="loading">
            <span v-if="loading">⏳ Đang đăng nhập...</span>
            <span v-else>🚀 Đăng nhập</span>
          </button>
        </form>

        <div class="demo-accounts mt-4">
          <p class="text-muted small mb-2">Tài khoản demo:</p>
          <div class="demo-list">
            <div class="demo-item" v-for="acc in demoAccounts" :key="acc.username" @click="fillDemo(acc)">
              <span class="demo-role" :class="acc.badgeClass">{{ acc.role }}</span>
              <span class="demo-cred">{{ acc.username }} / {{ acc.password }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import authApi from '../api/auth'

export default {
  name: 'Login',
  data() {
    return {
      form: { username: '', password: '' },
      loading: false,
      error: '',
      showPassword: false,
      demoAccounts: [
        { username: 'supervisor', password: '123456', role: 'SUPERVISOR', badgeClass: 'badge badge-warning' },
        { username: 'operator',   password: '123456', role: 'OPERATOR',   badgeClass: 'badge badge-info' },
        { username: 'qc',         password: '123456', role: 'QC',         badgeClass: 'badge badge-success' }
      ]
    }
  },
  methods: {
    async handleLogin() {
      this.loading = true
      this.error = ''
      try {
        const res = await authApi.login(this.form)
        const data = res.data
        await this.$store.dispatch('auth/login', {
          token: data.token,
          user: {
            username: data.username,
            fullName: data.fullName,
            role: data.role
          }
        })
        this.$router.push('/dashboard')
      } catch (err) {
        this.error = err.response?.data?.message || 'Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin.'
      } finally {
        this.loading = false
      }
    },
    fillDemo(acc) {
      this.form.username = acc.username
      this.form.password = acc.password
    }
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.login-left {
  flex: 1;
  background: linear-gradient(135deg, #1A237E 0%, #1565C0 50%, #0288D1 100%);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 40px;
  color: #fff;
}

.login-brand { text-align: center; margin-bottom: 40px; }
.brand-icon { font-size: 4rem; margin-bottom: 16px; animation: spin 10s linear infinite; }
@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
.login-brand h1 { font-size: 2.5rem; font-weight: 800; letter-spacing: 2px; margin-bottom: 8px; }
.login-brand p { font-size: 1rem; opacity: 0.85; }

.brand-features { display: flex; flex-direction: column; gap: 12px; }
.feature-item { display: flex; align-items: center; gap: 10px; font-size: 0.95rem; opacity: 0.9; }

.login-right {
  width: 460px;
  background: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 32px;
}

.login-card {
  width: 100%;
  background: #fff;
  border-radius: 12px;
  padding: 36px 32px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.1);
}

.login-header { text-align: center; margin-bottom: 28px; }
.login-header h2 { color: #1A237E; font-weight: 700; margin-bottom: 6px; }

.btn-login {
  background: linear-gradient(135deg, #1565C0, #0288D1);
  border: none;
  padding: 12px;
  font-size: 1rem;
  font-weight: 600;
  border-radius: 8px;
  transition: opacity 0.2s;
}
.btn-login:hover { opacity: 0.9; }

.demo-accounts { border-top: 1px solid #eee; padding-top: 16px; }
.demo-list { display: flex; flex-direction: column; gap: 6px; }
.demo-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 10px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.15s;
  border: 1px solid #eee;
}
.demo-item:hover { background: #e8f4fd; }
.demo-cred { font-size: 0.82rem; color: #555; font-family: monospace; }
</style>
