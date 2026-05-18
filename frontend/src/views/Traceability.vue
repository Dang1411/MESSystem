<template>
  <div>
    <!-- Search Bar -->
    <div class="card card-mes mb-4">
      <div class="card-body">
        <div class="input-group input-group-lg">
          <input
            v-model="searchCode"
            @keyup.enter="trace"
            type="text"
            class="form-control"
            placeholder="Nhập mã serial để truy xuất nguồn gốc..."
          />
          <div class="input-group-append">
            <button class="btn btn-primary btn-lg" @click="trace" :disabled="loading">
              {{ loading ? '⏳ Đang tra cứu...' : '🔗 Truy xuất' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="alert alert-danger" v-if="error">{{ error }}</div>

    <!-- Trace Result -->
    <div v-if="result">
      <!-- Header info -->
      <div class="card card-mes mb-4">
        <div class="card-header text-white d-flex align-items-center justify-content-between"
             :style="{ background: statusColor(result.status) }">
          <div>
            <h4 class="mb-0">{{ result.serialCode }}</h4>
            <small>{{ result.productName }} | Lệnh: {{ result.orderCode }}</small>
          </div>
          <span class="badge badge-light" style="font-size:1.1rem; padding:10px 18px">{{ result.status }}</span>
        </div>
        <div class="card-body">
          <div class="row">
            <div class="col-md-3"><strong>Mã sản phẩm:</strong> {{ result.productCode }}</div>
            <div class="col-md-3"><strong>Lệnh SX:</strong> {{ result.orderCode }}</div>
            <div class="col-md-3"><strong>Trạng thái:</strong> {{ result.status }}</div>
            <div class="col-md-3"><strong>Công đoạn hiện tại:</strong> {{ result.currentStepName || 'Hoàn thành' }}</div>
          </div>
        </div>
      </div>

      <!-- Timeline -->
      <div class="card card-mes mb-4" v-if="result.steps && result.steps.length">
        <div class="card-header bg-white">
          <strong>📋 Lịch sử thực hiện công đoạn</strong>
        </div>
        <div class="card-body">
          <div class="timeline">
            <div v-for="(h, i) in result.steps" :key="i" class="timeline-item" :class="'result-' + h.result.toLowerCase()">
              <div class="timeline-dot" :style="{ background: resultColor(h.result) }">
                {{ resultIcon(h.result) }}
              </div>
              <div class="timeline-content">
                <div class="d-flex align-items-center gap-2">
                  <strong>{{ h.stepName }}</strong>
                  <span class="badge ml-2" :style="{ background: resultColor(h.result), color: '#fff' }">{{ h.result }}</span>
                </div>
                <div class="text-muted small mt-1">
                  👤 {{ h.operatorName }} &nbsp;|&nbsp;
                  🕐 {{ formatDateTime(h.startTime) }}
                  <span v-if="h.endTime"> → {{ formatDateTime(h.endTime) }}</span>
                </div>
                <div class="mt-1" v-if="h.notes"><em>{{ h.notes }}</em></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Defect Logs -->
      <div class="card card-mes" v-if="result.defects && result.defects.length">
        <div class="card-header bg-white">
          <strong>⚠ Nhật ký lỗi</strong>
        </div>
        <div class="card-body p-0">
          <table class="table table-sm mb-0">
            <thead class="thead-light">
              <tr>
                <th>Công đoạn</th>
                <th>Loại lỗi</th>
                <th>Hành động</th>
                <th>Người báo cáo</th>
                <th>Thời gian</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(d, i) in result.defects" :key="i">
                <td>{{ d.stepName }}</td>
                <td><span class="badge badge-danger">{{ d.defectName }}</span></td>
                <td>{{ d.actionTaken }}</td>
                <td>🔍 {{ d.reportedByName }}</td>
                <td class="text-muted small">{{ formatDateTime(d.createdAt) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import traceApi from '../api/traceability'

export default {
  name: 'Traceability',
  data() {
    return {
      searchCode: '',
      loading: false,
      error: '',
      result: null
    }
  },
  methods: {
    async trace() {
      if (!this.searchCode.trim()) return
      this.loading = true; this.error = ''; this.result = null
      try {
        const res = await traceApi.trace(this.searchCode.trim())
        this.result = res.data
      } catch (e) {
        this.error = e.response?.data?.message || 'Không tìm thấy thông tin serial'
      } finally { this.loading = false }
    },
    statusColor(status) {
      const map = {
        WAITING: '#6c757d', IN_PROGRESS: '#1565C0', OK: '#2E7D32',
        NG: '#C62828', REWORK: '#fd7e14', SCRAP: '#6f42c1',
        HOLD: '#F9A825', FINISHED: '#0288D1'
      }
      return map[status] || '#6c757d'
    },
    resultColor(r) {
      const m = { OK: '#2E7D32', NG: '#C62828', REWORK: '#fd7e14', SCRAP: '#6f42c1', HOLD: '#F9A825' }
      return m[r] || '#6c757d'
    },
    resultIcon(r) {
      const m = { OK: '✅', NG: '❌', REWORK: '🔄', SCRAP: '🗑', HOLD: '⏸' }
      return m[r] || '•'
    },
    formatDateTime(dt) {
      if (!dt) return '—'
      return new Date(dt).toLocaleString('vi-VN')
    }
  },
  mounted() {
    // If navigated with query param
    if (this.$route.query.serial) {
      this.searchCode = this.$route.query.serial
      this.trace()
    }
  }
}
</script>

<style scoped>
.timeline { position: relative; padding-left: 40px; }
.timeline::before {
  content: '';
  position: absolute; left: 16px; top: 0; bottom: 0;
  width: 2px; background: #e0e0e0;
}
.timeline-item {
  position: relative;
  margin-bottom: 24px;
  padding: 12px 16px;
  border-radius: 8px;
  background: #f8f9fa;
  border: 1px solid #e0e0e0;
}
.timeline-item.result-ok    { border-left: 4px solid #2E7D32; }
.timeline-item.result-ng    { border-left: 4px solid #C62828; }
.timeline-item.result-rework{ border-left: 4px solid #fd7e14; }
.timeline-item.result-scrap { border-left: 4px solid #6f42c1; }
.timeline-item.result-hold  { border-left: 4px solid #F9A825; }

.timeline-dot {
  position: absolute;
  left: -32px; top: 12px;
  width: 28px; height: 28px;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 0.8rem;
  border: 2px solid #fff;
  box-shadow: 0 2px 4px rgba(0,0,0,0.2);
}
</style>
