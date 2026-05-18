import Vue from 'vue'
import VueRouter from 'vue-router'
import store from '../store'

Vue.use(VueRouter)

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { guest: true }
  },
  {
    path: '/',
    component: () => import('../layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: '/dashboard'
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue')
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('../views/Users.vue'),
        meta: { roles: ['SUPERVISOR'] }
      },
      {
        path: 'products',
        name: 'Products',
        component: () => import('../views/Products.vue'),
        meta: { roles: ['SUPERVISOR'] }
      },
      {
        path: 'process-steps',
        name: 'ProcessSteps',
        component: () => import('../views/ProcessSteps.vue'),
        meta: { roles: ['SUPERVISOR'] }
      },
      {
        path: 'production-orders',
        name: 'ProductionOrders',
        component: () => import('../views/ProductionOrders.vue'),
        meta: { roles: ['SUPERVISOR', 'OPERATOR', 'QC'] }
      },
      {
        path: 'scan',
        name: 'ScanProduction',
        component: () => import('../views/ScanProduction.vue'),
        meta: { roles: ['OPERATOR', 'SUPERVISOR'] }
      },
      {
        path: 'quality-check',
        name: 'QualityCheck',
        component: () => import('../views/QualityCheck.vue'),
        meta: { roles: ['QC', 'SUPERVISOR'] }
      },
      {
        path: 'traceability',
        name: 'Traceability',
        component: () => import('../views/Traceability.vue')
      },
      {
        path: 'defects',
        name: 'Defects',
        component: () => import('../views/Defects.vue'),
        meta: { roles: ['QC', 'SUPERVISOR'] }
      },
      {
        path: 'reports',
        name: 'Reports',
        component: () => import('../views/Reports.vue'),
        meta: { roles: ['SUPERVISOR'] }
      }
    ]
  },
  {
    path: '*',
    redirect: '/'
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

// Navigation Guards
router.beforeEach((to, from, next) => {
  const isAuthenticated = store.getters['auth/isAuthenticated']
  const userRole = store.getters['auth/userRole']

  // Check requiresAuth from matched routes (inherits from parent)
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
  if (requiresAuth && !isAuthenticated) {
    return next('/login')
  }

  if (to.meta.guest && isAuthenticated) {
    return next('/dashboard')
  }

  // Check roles from matched routes
  const requiredRoles = to.matched.reduce((roles, record) => {
    return record.meta.roles ? record.meta.roles : roles
  }, null)
  if (requiredRoles && !requiredRoles.includes(userRole)) {
    return next('/dashboard')
  }

  next()
})

export default router
