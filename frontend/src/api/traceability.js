import api from './index'

export default {
  trace(serialCode) {
    return api.get(`/traceability/${serialCode}`)
  }
}
