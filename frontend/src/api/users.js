import api from './index'

export default {
  getAll(keyword = '') {
    return api.get('/users', { params: { keyword } })
  },
  getById(id) {
    return api.get(`/users/${id}`)
  },
  create(data) {
    return api.post('/users', data)
  },
  update(id, data) {
    return api.put(`/users/${id}`, data)
  },
  delete(id) {
    return api.delete(`/users/${id}`)
  },
  getRoles() {
    return api.get('/users/roles')
  }
}
