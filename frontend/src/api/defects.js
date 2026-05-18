import api from './index'

export default {
  getAll() {
    return api.get('/defects')
  },
  getById(id) {
    return api.get(`/defects/${id}`)
  },
  create(data) {
    return api.post('/defects', data)
  },
  update(id, data) {
    return api.put(`/defects/${id}`, data)
  },
  delete(id) {
    return api.delete(`/defects/${id}`)
  },
  getLogs(params = {}) {
    return api.get('/defects/logs', { params })
  },
  createLog(data) {
    return api.post('/defects/logs', data)
  },
  getLogsBySerial(serialCode) {
    return api.get(`/defects/logs/serial/${serialCode}`)
  }
}
