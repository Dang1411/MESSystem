import api from './index'

export default {
  getAll() {
    return api.get('/process-steps')
  },
  getById(id) {
    return api.get(`/process-steps/${id}`)
  },
  create(data) {
    return api.post('/process-steps', data)
  },
  update(id, data) {
    return api.put(`/process-steps/${id}`, data)
  },
  delete(id) {
    return api.delete(`/process-steps/${id}`)
  }
}
