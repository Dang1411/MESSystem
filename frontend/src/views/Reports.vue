<template>
  <div>
    <!-- Tabs -->
    <ul class="nav nav-tabs mb-4">
      <li class="nav-item" v-for="tab in tabs" :key="tab.key">
        <a class="nav-link" :class="{ active: activeTab === tab.key }" href="#" @click.prevent="activeTab = tab.key">
          {{ tab.icon }} {{ tab.label }}
        </a>
      </li>
    </ul>

    <!-- Production Report -->
    <div v-if="activeTab === 'production'">
      <div class="card card-mes mb-4">
        <div class="card-header bg-white d-flex justify-content-between align-items-center">
          <strong>📊 Báo cáo Sản xuất</strong>
          <button class="btn btn-sm btn-outline-primary" @click="loadProductionReport">🔄 Tải lại</button>
        </div>
        <div class="card-body p-0">
          <table class="table table-mes mb-0" v-if="productionReport.length">
            <thead>
              <tr>
                <th>Lệnh SX</th>
                <th>Sản phẩm</th>
                <th>Kế hoạch</th>
                <th>Đã hoàn thành</th>
                <th>Tỷ lệ</th>
                <th>Trạng thái</th>
                <th>Ngày tạo</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="r in productionReport" :key="r.orderId">
                <td><code>{{ r.orderCode }}</code></td>
                <td>{{ r.productName }}</td>
                <td class="text-center">{{ r.plannedQuantity }}</td>
                <td class="text-center text-success font-weight-bold">{{ r.completedQuantity }}</td>
                <td>
                  <div class="progress" style="height:8px; width:80px">
                    <div class="progress-bar bg-success" :style="{ width: r.completionRate + '%' }"></div>
                  </div>
                  <small>{{ r.completionRate }}%</small>
                </td>
                <td>
                  <span class="badge-status" :class="statusBadge(r.status)">{{ statusLabel(r.status) }}</span>
                </td>
                <td class="text-muted small">{{ formatDate(r.createdAt) }}</td>
              </tr>
            </tbody>
          </table>
          <div class="p-4 text-muted text-center" v-else-if="!loadingProd">Chưa có dữ liệu</div>
          <div class="p-4 text-center" v-else>
            <div class="spinner-border spinner-border-sm text-primary"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- Quality Report -->
    <div v-if="activeTab === 'quality'">
      <div class="card card-mes mb-4">
        <div class="card-header bg-white d-flex justify-content-between align-items-center">
          <strong>🔍 Báo cáo Chất lượng</strong>
          <button class="btn btn-sm btn-outline-primary" @click="loadQualityReport">🔄 Tải lại</button>
        </div>
        <div class="card-body p-0">
          <table class="table table-mes mb-0" v-if="qualityReport.length">
            <thead>
              <tr>
                <th>Công đoạn</th>
                <th>Tổng thực hiện</th>
                <th>OK</th>
                <th>NG</th>
                <th>Rework</th>
                <th>Scrap</th>
                <th>Tỷ lệ NG</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="r in qualityReport" :key="r.stepId">
                <td><strong>{{ r.stepName }}</strong><br><small class="text-muted">{{ r.stepCode }}</small></td>
                <td class="text-center">{{ r.total }}</td>
                <td class="text-center text-success font-weight-bold">{{ r.okCount }}</td>
                <td class="text-center text-danger font-weight-bold">{{ r.ngCount }}</td>
                <td class="text-center" style="color:#fd7e14">{{ r.reworkCount }}</td>
                <td class="text-center text-purple">{{ r.scrapCount }}</td>
                <td>
                  <div class="d-flex align-items-center gap-2">
                    <div class="progress flex-grow-1" style="height:8px">
                      <div class="progress-bar bg-danger" :style="{ width: r.ngRate + '%' }"></div>
                    </div>
                    <small class="text-danger font-weight-bold">{{ r.ngRate }}%</small>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
          <div class="p-4 text-muted text-center" v-else-if="!loadingQual">Chưa có dữ liệu</div>
          <div class="p-4 text-center" v-else>
            <div class="spinner-border spinner-border-sm text-primary"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- Serial Report -->
    <div v-if="activeTab === 'serials'">
      <div class="card card-mes mb-4">
        <div class="card-header bg-white d-flex justify-content-between align-items-center">
          <strong>📋 Báo cáo Serial theo Lệnh</strong>
          <button class="btn btn-sm btn-outline-primary" @click="loadSerialReport">🔄 Tải lại</button>
        </div>
        <div class="card-body p-0">
          <table class="table table-mes mb-0" v-if="serialReport.length">
            <thead>
              <tr>
                <th>Lệnh SX</th>
                <th>Sản phẩm</th>
                <th>WAITING</th>
                <th>IN_PROGRESS</th>
                <th>FINISHED</th>
                <th>NG</th>
                <th>HOLD</th>
                <th>SCRAP</th>
                <th>Tổng</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="r in serialReport" :key="r.orderId">
                <td><code>{{ r.orderCode }}</code></td>
                <td>{{ r.productName }}</td>
                <td class="text-center text-muted">{{ r.waitingCount }}</td>
                <td class="text-center text-primary">{{ r.inProgressCount }}</td>
                <td class="text-center text-success font-weight-bold">{{ r.finishedCount }}</td>
                <td class="text-center text-danger font-weight-bold">{{ r.ngCount }}</td>
                <td class="text-center" style="color:#F9A825">{{ r.holdCount }}</td>
                <td class="text-center text-purple">{{ r.scrapCount }}</td>
                <td class="text-center font-weight-bold">{{ r.totalCount }}</td>
              </tr>
            </tbody>
          </table>
          <div class="p-4 text-muted text-center" v-else-if="!loadingSer">Chưa có dữ liệu</div>
          <div class="p-4 text-center" v-else>
            <div class="spinner-border spinner-border-sm text-primary"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import reportApi from '../api/reports'

export default {
  name: 'Reports',
  data() {
    return {
      activeTab: 'production',
      tabs: [
        { key: 'production', label: 'Báo cáo Sản xuất', icon: '📊' },
        { key: 'quality',    label: 'Báo cáo Chất lượng', icon: '🔍' },
        { key: 'serials',   label: 'Báo cáo Serial',    icon: '📋' }
      ],
      productionReport: [], qualityReport: [], serialReport: [],
      loadingProd: false, loadingQual: false, loadingSer: false
    }
  },
  methods: {
    async loadProductionReport() {
      this.loadingProd = true
      try {
        const res = await reportApi.getProductionReport()
        this.productionReport = res.data
      } finally { this.loadingProd = false }
    },
    async loadQualityReport() {
      this.loadingQual = true
      try {
        const res = await reportApi.getQualityReport()
        this.qualityReport = res.data
      } finally { this.loadingQual = false }
    },
    async loadSerialReport() {
      this.loadingSer = true
      try {
        const res = await reportApi.getSerialReport()
        this.serialReport = res.data
      } finally { this.loadingSer = false }
    },
    statusBadge(s) {
      const m = { CREATED: 'badge-created', IN_PROGRESS: 'badge-inprogress', COMPLETED: 'badge-completed', CANCELLED: 'badge-cancelled' }
      return m[s] || ''
    },
    statusLabel(s) {
      const m = { CREATED: 'Mới tạo', IN_PROGRESS: 'Đang thực hiện', COMPLETED: 'Hoàn thành', CANCELLED: 'Đã hủy' }
      return m[s] || s
    },
    formatDate(d) {
      if (!d) return '—'
      return new Date(d).toLocaleDateString('vi-VN')
    }
  },
  mounted() {
    this.loadProductionReport()
    this.loadQualityReport()
    this.loadSerialReport()
  }
}
</script>

<style scoped>
.text-purple { color: #6f42c1; }
</style>
