import api from './index'

export default {
  getAll(keyword = '') {
    return api.get('/products', { params: { keyword } })
  },
  getById(id) {
    return api.get(`/products/${id}`)
  },
  create(data) {
    return api.post('/products', data)
  },
  update(id, data) {
    return api.put(`/products/${id}`, data)
  },
  delete(id) {
    return api.delete(`/products/${id}`)
  },
  getRoutes(id) {
    return api.get(`/products/${id}/routes`)
  },
  // saveRoutes(id, routes) {
  //   return api.post(`/products/${id}/routes`, routes)
  // },
    // SỬA CHỖ NÀY
  saveRoutes(id, routes) {
    return api.post(`/products/${id}/routes`, routes[0])
  },
  deleteRouteStep(id, routeId) {
    return api.delete(`/products/${id}/routes/${routeId}`)
  }
}
