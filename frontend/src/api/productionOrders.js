import api from './index'

export default {
  getAll(params = {}) {
    const cleaned = Object.fromEntries(Object.entries(params).filter(([, v]) => v !== '' && v != null))
    return api.get('/production-orders', { params: cleaned })
  },
  getById(id) {
    return api.get(`/production-orders/${id}`)
  },
  create(data) {
    return api.post('/production-orders', data)
  },
  updateStatus(id, status) {
    return api.patch(`/production-orders/${id}/status`, { status })
  },
  getSerials(id) {
    return api.get(`/production-orders/${id}/serials`)
  }
}
