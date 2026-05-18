import api from './index'

export default {
  getSerialInfo(serialCode) {
    return api.get(`/scan/${serialCode}`)
  },
  execute(data) {
    return api.post('/scan/execute', data)
  }
}
