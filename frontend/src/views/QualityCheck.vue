<template>
  <div>
    <div class="row mb-4">
      <div class="col-md-4">
        <div class="card card-mes" style="border-top: 4px solid #C62828;">
          <div class="card-body text-center">
            <h3 class="text-danger">{{ ngCount }}</h3>
            <p class="mb-0">Serial NG cần xử lý</p>
          </div>
        </div>
      </div>
      <div class="col-md-4">
        <div class="card card-mes" style="border-top: 4px solid #F9A825;">
          <div class="card-body text-center">
            <h3 style="color:#F9A825">{{ holdCount }}</h3>
            <p class="mb-0">Serial HOLD đang chờ</p>
          </div>
        </div>
      </div>
      <div class="col-md-4">
        <div class="card card-mes" style="border-top: 4px solid #6f42c1;">
          <div class="card-body text-center">
            <h3 class="text-purple">{{ reworkCount }}</h3>
            <p class="mb-0">Serial cần REWORK</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Filters -->
    <div class="card card-mes mb-4">
      <div class="card-body">
        <div class="row">
          <div class="col-md-3">
            <select v-model="filterStatus" @change="loadSerials" class="form-control">
              <option value="">Tất cả trạng thái</option>
              <option value="NG">NG - Không đạt</option>
              <option value="HOLD">HOLD - Giữ lại</option>
              <option value="REWORK">REWORK - Làm lại</option>
            </select>
          </div>
          <div class="col-md-3">
            <input v-model="filterKeyword" @input="filterData" type="text" class="form-control" placeholder="Tìm theo serial code...">
          </div>
        </div>
      </div>
    </div>

    <!-- Serials Table -->
    <div class="card card-mes">
      <div class="card-header bg-white">
        <strong>Danh sách Serial cần xử lý</strong>
      </div>
      <div class="card-body p-0">
        <table class="table table-mes mb-0">
          <thead>
            <tr>
              <th>Serial Code</th>
              <th>Sản phẩm</th>
              <th>Công đoạn</th>
              <th>Trạng thái</th>
              <th>Lỗi gần nhất</th>
              <th>Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="6" class="text-center py-4">
                <div class="spinner-border spinner-border-sm text-primary mr-2"></div>Đang tải...
              </td>
            </tr>
            <tr v-else-if="!filteredSerials.length">
              <td colspan="6" class="text-center py-4 text-muted">
                ✅ Không có serial nào cần xử lý
              </td>
            </tr>
            <tr v-for="s in filteredSerials" :key="s.id">
              <td><code>{{ s.serialCode }}</code></td>
              <td>{{ s.productName }}<br><small class="text-muted">{{ s.orderCode }}</small></td>
              <td>{{ s.currentStepName || '—' }}</td>
              <td>
                <span class="badge-status" :class="statusBadge(s.status)">{{ s.status }}</span>
              </td>
              <td class="small text-muted">{{ s.lastDefect || '—' }}</td>
              <td>
                <button class="btn btn-sm btn-outline-info" @click="viewTrace(s.serialCode)">
                  🔗 Truy xuất
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script>
import orderApi from '../api/productionOrders'

export default {
  name: 'QualityCheck',
  data() {
    return {
      serials: [], loading: false,
      filterStatus: 'NG', filterKeyword: ''
    }
  },
  computed: {
    filteredSerials() {
      return this.serials.filter(s => {
        if (this.filterKeyword && !s.serialCode.toLowerCase().includes(this.filterKeyword.toLowerCase())) return false
        return true
      })
    },
    ngCount()     { return this.serials.filter(s => s.status === 'NG').length },
    holdCount()   { return this.serials.filter(s => s.status === 'HOLD').length },
    reworkCount() { return this.serials.filter(s => s.status === 'REWORK').length }
  },
  methods: {
    async loadSerials() {
      this.loading = true
      try {
        // Load from all orders and flatten serials with pending status
        const statusesToCheck = this.filterStatus ? [this.filterStatus] : ['NG', 'HOLD', 'REWORK']
        const ordersRes = await orderApi.getAll({ status: 'IN_PROGRESS' })
        const orders = ordersRes.data
        let allSerials = []
        for (const o of orders) {
          try {
            const sRes = await orderApi.getSerials(o.id)
            const filtered = sRes.data.filter(s => statusesToCheck.includes(s.status))
            filtered.forEach(s => { s.orderCode = o.orderCode; s.productName = o.productName })
            allSerials = allSerials.concat(filtered)
          } catch (e) { /* skip */ }
        }
        this.serials = allSerials
      } finally { this.loading = false }
    },
    filterData() { /* reactive via computed */ },
    statusBadge(s) {
      const m = { NG: 'badge-ng', HOLD: 'badge-hold', REWORK: 'badge-rework', SCRAP: 'badge-scrap' }
      return m[s] || ''
    },
    viewTrace(serialCode) {
      this.$router.push({ name: 'Traceability', query: { serial: serialCode } })
    }
  },
  mounted() { this.loadSerials() }
}
</script>

<style scoped>
.text-purple { color: #6f42c1; }
</style>
