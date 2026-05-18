// import Vue from 'vue'
// import App from './App.vue'
// import router from './router'
// import store from './store'


// // Bootstrap
// import 'bootstrap/dist/css/bootstrap.min.css'
// import 'bootstrap/dist/js/bootstrap.bundle.min.js'

// Vue.config.productionTip = false

// new Vue({
//   router,
//   store,
//   render: h => h(App)
// }).$mount('#app')
import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'

// jQuery
import $ from 'jquery'
window.$ = $
window.jQuery = $

// Bootstrap
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js'

Vue.config.productionTip = false

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')