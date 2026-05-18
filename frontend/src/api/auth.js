import api from './index'

export default {
  login(credentials) {
    return api.post('/auth/login', credentials)
  },
  getCurrentUser() {
    return api.get('/auth/me')
  }
}
