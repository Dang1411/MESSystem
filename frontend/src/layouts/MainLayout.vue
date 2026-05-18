<template>
  <div class="main-layout">
    <!-- Sidebar -->
    <nav class="sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="sidebar-header">
        <div class="logo-area">
          <span class="logo-icon">⚙</span>
          <span class="logo-text" v-if="!sidebarCollapsed">MES System</span>
        </div>
        <button class="btn-toggle" @click="toggleSidebar">
          <span>{{ sidebarCollapsed ? '▶' : '◀' }}</span>
        </button>
      </div>

      <ul class="sidebar-menu">
        <li v-for="item in menuItems" :key="item.path">
          <router-link :to="item.path" class="menu-item" active-class="active">
            <span class="menu-icon">{{ item.icon }}</span>
            <span class="menu-text" v-if="!sidebarCollapsed">{{ item.label }}</span>
          </router-link>
        </li>
      </ul>

      <div class="sidebar-footer">
        <div class="user-info" v-if="!sidebarCollapsed">
          <div class="user-avatar">{{ userInitials }}</div>
          <div class="user-details">
            <div class="user-name">{{ fullName || username }}</div>
            <div class="user-role badge" :class="roleBadgeClass">{{ userRole || 'USER' }}</div>
          </div>
        </div>
        <div class="user-avatar-only" v-else>{{ userInitials }}</div>
      </div>
    </nav>

    <!-- Main content -->
    <div class="main-content" :class="{ expanded: sidebarCollapsed }">
      <!-- Top navbar -->
      <header class="top-navbar">
        <div class="navbar-left">
          <h5 class="page-title mb-0">{{ currentPageTitle }}</h5>
        </div>
        <div class="navbar-right">
          <span class="text-muted mr-3">{{ currentTime }}</span>
          <div class="dropdown">
            <button class="btn btn-outline-secondary btn-sm dropdown-toggle" data-toggle="dropdown">
              {{ fullName }}
            </button>
            <div class="dropdown-menu dropdown-menu-right">
              <span class="dropdown-item-text text-muted small">{{ username }}</span>
              <div class="dropdown-divider"></div>
              <a class="dropdown-item text-danger" href="#" @click.prevent="logout">
                🚪 Đăng xuất
              </a>
            </div>
          </div>
        </div>
      </header>

      <!-- Page content -->
      <main class="page-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'MainLayout',
  data() {
    return {
      sidebarCollapsed: false,
      currentTime: ''
    }
  },
  computed: {
    ...mapGetters('auth', ['fullName', 'username', 'userRole']),
    userInitials() {
      if (!this.fullName) return 'U'
      return this.fullName.split(' ').map(w => w[0]).slice(-2).join('').toUpperCase()
    },
    roleBadgeClass() {
      const map = { SUPERVISOR: 'badge-warning', OPERATOR: 'badge-info', QC: 'badge-success' }
      return map[this.userRole] || 'badge-secondary'
    },
    menuItems() {
      const role = this.userRole
      const all = [
        { path: '/dashboard',         icon: '📊', label: 'Dashboard',         roles: ['SUPERVISOR', 'OPERATOR', 'QC'] },
        { path: '/scan',              icon: '📡', label: 'Quét & Sản xuất',   roles: ['SUPERVISOR', 'OPERATOR'] },
        { path: '/quality-check',     icon: '🔍', label: 'Kiểm tra chất lượng', roles: ['SUPERVISOR', 'QC'] },
        { path: '/traceability',      icon: '🔗', label: 'Truy xuất nguồn gốc', roles: ['SUPERVISOR', 'OPERATOR', 'QC'] },
        { path: '/production-orders', icon: '📋', label: 'Lệnh sản xuất',     roles: ['SUPERVISOR', 'OPERATOR', 'QC'] },
        { path: '/products',          icon: '📦', label: 'Sản phẩm',          roles: ['SUPERVISOR'] },
        { path: '/process-steps',     icon: '⚙', label: 'Công đoạn',         roles: ['SUPERVISOR'] },
        { path: '/defects',           icon: '🐛', label: 'Quản lý lỗi',       roles: ['SUPERVISOR', 'QC'] },
        { path: '/reports',           icon: '📈', label: 'Báo cáo',           roles: ['SUPERVISOR'] },
        { path: '/users',             icon: '👥', label: 'Người dùng',        roles: ['SUPERVISOR'] }
      ]
      // If role is null/unknown, show common items available to all roles
      if (!role) return all.filter(item => item.roles.includes('OPERATOR'))
      return all.filter(item => item.roles.includes(role))
    },
    currentPageTitle() {
      const route = this.$route.name
      const titles = {
        Dashboard: 'Dashboard Tổng quan',
        ScanProduction: 'Quét & Thực hiện Sản xuất',
        QualityCheck: 'Kiểm tra Chất lượng',
        Traceability: 'Truy xuất Nguồn gốc',
        ProductionOrders: 'Quản lý Lệnh Sản xuất',
        Products: 'Quản lý Sản phẩm',
        ProcessSteps: 'Quản lý Công đoạn',
        Defects: 'Quản lý Lỗi',
        Reports: 'Báo cáo & Thống kê',
        Users: 'Quản lý Người dùng'
      }
      return titles[route] || 'MES System'
    }
  },
  methods: {
    toggleSidebar() {
      this.sidebarCollapsed = !this.sidebarCollapsed
    },
    logout() {
      this.$store.dispatch('auth/logout')
      this.$router.push('/login')
    },
    updateTime() {
      const now = new Date()
      this.currentTime = now.toLocaleString('vi-VN')
    }
  },
  mounted() {
    this.updateTime()
    this.timer = setInterval(this.updateTime, 1000)
  },
  beforeDestroy() {
    clearInterval(this.timer)
  }
}
</script>

<style scoped>
.main-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

/* Sidebar */
.sidebar {
  width: 240px;
  background: linear-gradient(180deg, #1A237E 0%, #283593 50%, #1565C0 100%);
  color: #fff;
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  position: fixed;
  top: 0; left: 0; bottom: 0;
  z-index: 100;
  overflow-x: hidden;
}
.sidebar.collapsed { width: 60px; }

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 12px;
  border-bottom: 1px solid rgba(255,255,255,0.1);
  min-height: 64px;
}
.logo-area { display: flex; align-items: center; gap: 10px; }
.logo-icon { font-size: 1.5rem; }
.logo-text { font-size: 1.1rem; font-weight: 700; letter-spacing: 1px; white-space: nowrap; }
.btn-toggle {
  background: rgba(255,255,255,0.1);
  border: none; color: #fff;
  border-radius: 4px; padding: 4px 8px;
  cursor: pointer; font-size: 0.7rem;
}
.btn-toggle:hover { background: rgba(255,255,255,0.2); }

.sidebar-menu {
  list-style: none;
  padding: 8px 0;
  margin: 0;
  flex: 1;
  overflow-y: auto;
}
.menu-item {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  color: rgba(255,255,255,0.8);
  text-decoration: none;
  gap: 12px;
  border-left: 3px solid transparent;
  transition: all 0.2s;
  white-space: nowrap;
}
.menu-item:hover {
  background: rgba(255,255,255,0.1);
  color: #fff;
  border-left-color: #64B5F6;
}
.menu-item.active {
  background: rgba(255,255,255,0.15);
  color: #fff;
  border-left-color: #fff;
  font-weight: 600;
}
.menu-icon { font-size: 1.1rem; min-width: 20px; text-align: center; }
.menu-text { font-size: 0.88rem; }

.sidebar-footer {
  padding: 12px;
  border-top: 1px solid rgba(255,255,255,0.1);
}
.user-info { display: flex; align-items: center; gap: 10px; }
.user-avatar {
  width: 36px; height: 36px;
  background: rgba(255,255,255,0.2);
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-weight: 700; font-size: 0.85rem;
  flex-shrink: 0;
}
.user-name { font-size: 0.82rem; font-weight: 600; color: #fff; }
.user-role { font-size: 0.68rem; padding: 2px 6px; margin-top: 2px; }
.user-avatar-only {
  width: 36px; height: 36px;
  background: rgba(255,255,255,0.2);
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-weight: 700; font-size: 0.85rem; color: #fff;
  margin: 0 auto;
}

/* Main content */
.main-content {
  margin-left: 240px;
  display: flex;
  flex-direction: column;
  flex: 1;
  overflow: hidden;
  transition: margin-left 0.3s ease;
}
.main-content.expanded { margin-left: 60px; }

/* Top navbar */
.top-navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 56px;
  background: #fff;
  border-bottom: 1px solid #e0e0e0;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
  flex-shrink: 0;
}
.page-title { color: #1A237E; font-weight: 700; font-size: 1rem; }
.navbar-right { display: flex; align-items: center; gap: 12px; }

/* Page content */
.page-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background: #f0f2f5;
}
</style>
