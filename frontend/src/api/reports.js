import api from './index'

export default {
  getProductionReport(params = {}) {
    return api.get('/reports/production', { params })
  },
  getQualityReport(params = {}) {
    return api.get('/reports/quality', { params })
  },
  getSerialReport(params = {}) {
    return api.get('/reports/serials', { params })
  }
}
