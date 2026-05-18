import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

const TOKEN_KEY = 'mes_token'
const USER_KEY  = 'mes_user'

export default new Vuex.Store({
  modules: {
    auth: {
      namespaced: true,
      state: {
        token: localStorage.getItem(TOKEN_KEY) || null,
        user:  JSON.parse(localStorage.getItem(USER_KEY) || 'null')
      },
      getters: {
        isAuthenticated: state => !!state.token,
        token:     state => state.token,
        user:      state => state.user,
        userRole:  state => state.user ? state.user.role : null,
        fullName:  state => state.user ? state.user.fullName : '',
        username:  state => state.user ? state.user.username : ''
      },
      mutations: {
        SET_AUTH(state, { token, user }) {
          state.token = token
          state.user  = user
          localStorage.setItem(TOKEN_KEY, token)
          localStorage.setItem(USER_KEY, JSON.stringify(user))
        },
        CLEAR_AUTH(state) {
          state.token = null
          state.user  = null
          localStorage.removeItem(TOKEN_KEY)
          localStorage.removeItem(USER_KEY)
        }
      },
      actions: {
        login({ commit }, payload) {
          commit('SET_AUTH', payload)
        },
        logout({ commit }) {
          commit('CLEAR_AUTH')
        }
      }
    }
  }
})
