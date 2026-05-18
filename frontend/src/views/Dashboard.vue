<template>
  <div>
    <!-- Stat Cards -->
    <div class="row mb-4">
      <div class="col-md-3 mb-3" v-for="card in statCards" :key="card.label">
        <div class="card card-mes stat-card" :style="{ borderTop: '4px solid ' + card.color }">
          <div class="card-body d-flex align-items-center">
            <div class="stat-icon" :style="{ background: card.color + '22', color: card.color }">
              {{ card.icon }}
            </div>
            <div class="ml-3">
              <div class="stat-value">{{ card.value }}</div>
              <div class="stat-label">{{ card.label }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Charts row -->
    <div class="row mb-4">
      <div class="col-md-8 mb-3">
        <div class="card card-mes">
          <div class="card-header bg-white">
            <strong>📈 Sản lượng 7 ngày gần nhất</strong>
          </div>
          <div class="card-body">
            <canvas ref="productionChart" height="100"></canvas>
          </div>
        </div>
      </div>
      <div class="col-md-4 mb-3">
        <div class="card card-mes">
          <div class="card-header bg-white">
            <strong>🍩 Tỷ lệ chất lượng Serial</strong>
          </div>
          <div class="card-body d-flex flex-column align-items-center">
            <canvas ref="qualityChart" width="200" height="200"></canvas>
            <div class="quality-legend mt-3" v-if="stats">
              <div v-for="(item, i) in qualityLegend" :key="i" class="legend-item">
                <span class="legend-dot" :style="{ background: item.color }"></span>
                {{ item.label }}: {{ item.value }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Defect steps + Order summary -->
    <div class="row">
      <div class="col-md-6 mb-3">
        <div class="card card-mes">
          <div class="card-header bg-white">
            <strong>⚠ Top 5 Công đoạn có Lỗi nhiều nhất</strong>
          </div>
          <div class="card-body p-0">
            <table class="table table-sm mb-0" v-if="stats && stats.topDefectSteps && stats.topDefectSteps.length">
              <thead class="thead-light">
                <tr><th>Công đoạn</th><th class="text-right">Số lỗi NG</th></tr>
              </thead>
              <tbody>
                <tr v-for="(step, i) in stats.topDefectSteps" :key="i">
                  <td>{{ step.stepName }}</td>
                  <td class="text-right">
                    <span class="badge badge-danger">{{ step.ngCount }}</span>
                  </td>
                </tr>
              </tbody>
            </table>
            <div class="p-3 text-muted text-center" v-else>Chưa có dữ liệu</div>
          </div>
        </div>
      </div>

      <div class="col-md-6 mb-3">
        <div class="card card-mes">
          <div class="card-header bg-white">
            <strong>📋 Tình trạng Lệnh sản xuất</strong>
          </div>
          <div class="card-body">
            <div class="order-stat-row" v-if="stats">
              <div class="order-stat-item" v-for="item in orderStats" :key="item.label">
                <div class="order-stat-num" :style="{ color: item.color }">{{ item.value }}</div>
                <div class="order-stat-label">{{ item.label }}</div>
              </div>
            </div>
            <div class="mt-3" v-if="stats">
              <div class="d-flex justify-content-between small mb-1">
                <span>Tỷ lệ hoàn thành tổng thể</span>
                <strong>{{ completionRate }}%</strong>
              </div>
              <div class="progress" style="height: 10px;">
                <div class="progress-bar bg-success" :style="{ width: completionRate + '%' }"></div>
              </div>
              <div class="d-flex justify-content-between small mt-2 mb-1">
                <span>Tỷ lệ lỗi NG</span>
                <strong class="text-danger">{{ ngRate }}%</strong>
              </div>
              <div class="progress" style="height: 10px;">
                <div class="progress-bar bg-danger" :style="{ width: ngRate + '%' }"></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Loading overlay -->
    <div class="loading-overlay" v-if="loading">
      <div class="spinner-border text-primary" role="status"></div>
      <p>Đang tải dữ liệu...</p>
    </div>
  </div>
</template>

<script>
import Chart from 'chart.js'
import dashboardApi from '../api/dashboard'

export default {
  name: 'Dashboard',
  data() {
    return {
      stats: null,
      loading: true,
      prodChart: null,
      qualChart: null
    }
  },
  computed: {
    statCards() {
      if (!this.stats) return []
      return [
        { label: 'Lệnh đang thực hiện', value: this.stats.ordersInProgress || 0, icon: '🏭', color: '#1565C0' },
        { label: 'Serial đã hoàn thành', value: this.stats.totalFinished || 0,   icon: '✅', color: '#2E7D32' },
        { label: 'Serial bị lỗi NG',     value: this.stats.totalNg || 0,         icon: '❌', color: '#C62828' },
        { label: 'Serial đang giữ HOLD', value: this.stats.totalHold || 0,       icon: '⏸', color: '#F9A825' }
      ]
    },
    orderStats() {
      if (!this.stats) return []
      return [
        { label: 'Mới tạo',        value: this.stats.ordersCreated || 0,    color: '#6c757d' },
        { label: 'Đang thực hiện', value: this.stats.ordersInProgress || 0, color: '#1565C0' },
        { label: 'Hoàn thành',     value: this.stats.ordersCompleted || 0,  color: '#2E7D32' },
        { label: 'Đã hủy',         value: this.stats.ordersCancelled || 0,  color: '#C62828' }
      ]
    },
    completionRate() {
      if (!this.stats) return 0
      return this.stats.completionRate ? parseFloat(this.stats.completionRate).toFixed(1) : 0
    },
    ngRate() {
      if (!this.stats) return 0
      return this.stats.ngRate ? parseFloat(this.stats.ngRate).toFixed(1) : 0
    },
    qualityLegend() {
      if (!this.stats || !this.stats.qualityChart) return []
      const labelMap = { FINISHED: 'Hoàn thành', NG: 'Lỗi NG', REWORK: 'Làm lại', SCRAP: 'Loại bỏ', HOLD: 'Tạm giữ', WAITING: 'Chờ xử lý', IN_PROGRESS: 'Đang làm' }
      const colors = ['#2E7D32','#C62828','#fd7e14','#6f42c1','#F9A825','#0dcaf0','#1565C0']
      return Object.entries(this.stats.qualityChart).map(([key, val], i) => ({
        label: labelMap[key] || key,
        value: val,
        color: colors[i % colors.length]
      }))
    }
  },
  methods: {
    async loadStats() {
      this.loading = true
      try {
        const res = await dashboardApi.getStats()
        this.stats = res.data
        this.$nextTick(() => {
          this.renderProductionChart()
          this.renderQualityChart()
        })
      } catch (e) {
        console.error('Lỗi tải dashboard:', e)
      } finally {
        this.loading = false
      }
    },
    renderProductionChart() {
      if (this.prodChart) { this.prodChart.destroy() }
      const ctx = this.$refs.productionChart
      if (!ctx || !this.stats || !this.stats.productionChart) return
      const data = this.stats.productionChart
      this.prodChart = new Chart(ctx, {
        type: 'bar',
        data: {
          labels: data.map(d => d.date),
          datasets: [{
            label: 'Serial hoàn thành',
            data: data.map(d => d.count),
            backgroundColor: 'rgba(21, 101, 192, 0.7)',
            borderColor: '#1565C0',
            borderWidth: 1,
            borderRadius: 4
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: true,
          scales: {
            yAxes: [{ ticks: { beginAtZero: true, stepSize: 1 } }]
          },
          legend: { display: false }
        }
      })
    },
    renderQualityChart() {
      if (this.qualChart) { this.qualChart.destroy() }
      const ctx = this.$refs.qualityChart
      if (!ctx || !this.stats || !this.stats.qualityChart) return
      const labelMap = { FINISHED: 'Hoàn thành', NG: 'Lỗi NG', REWORK: 'Làm lại', SCRAP: 'Loại bỏ', HOLD: 'Tạm giữ', WAITING: 'Chờ xử lý', IN_PROGRESS: 'Đang làm' }
      const colors = ['#2E7D32','#C62828','#fd7e14','#6f42c1','#F9A825','#0dcaf0','#1565C0']
      const entries = Object.entries(this.stats.qualityChart)
      this.qualChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
          labels: entries.map(([k]) => labelMap[k] || k),
          datasets: [{
            data: entries.map(([, v]) => v),
            backgroundColor: colors.slice(0, entries.length)
          }]
        },
        options: {
          responsive: false,
          legend: { display: false },
          cutoutPercentage: 65
        }
      })
    }
  },
  mounted() {
    this.loadStats()
  }
}
</script>

<style scoped>
.stat-card { cursor: default; }
.stat-icon {
  width: 48px; height: 48px;
  border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  font-size: 1.5rem;
  flex-shrink: 0;
}
.stat-value { font-size: 1.6rem; font-weight: 700; color: #1a3a5c; }
.stat-label { font-size: 0.78rem; color: #666; }

.quality-legend { width: 100%; }
.legend-item { display: flex; align-items: center; gap: 8px; font-size: 0.82rem; margin-bottom: 4px; }
.legend-dot { width: 12px; height: 12px; border-radius: 2px; flex-shrink: 0; }

.order-stat-row { display: flex; justify-content: space-around; text-align: center; }
.order-stat-num { font-size: 1.8rem; font-weight: 700; }
.order-stat-label { font-size: 0.72rem; color: #666; }

.loading-overlay {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(255,255,255,0.7);
  display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  z-index: 999;
}
</style>
