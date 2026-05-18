import api from './index'

export default {
  getStats() {
    return api.get('/dashboard/stats')
  }
}
