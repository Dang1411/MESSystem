<template>
  <div>
    <div class="row justify-content-center">
      <div class="col-md-8">

        <!-- Scan Input -->
        <div class="card card-mes mb-4 scan-card">
          <div class="card-header text-white" style="background: #1565C0;">
            <h5 class="mb-0">📡 Quét Serial / Nhập mã</h5>
          </div>
          <div class="card-body">
            <div class="input-group input-group-lg">
              <input
                ref="scanInput"
                v-model="scanCode"
                @keyup.enter="lookupSerial"
                type="text"
                class="form-control form-control-lg"
                placeholder="Quét QR hoặc nhập mã serial..."
                autofocus
              />
              <div class="input-group-append">
                <button class="btn btn-primary btn-lg" @click="lookupSerial" :disabled="lookingUp">
                  {{ lookingUp ? '⏳' : '🔍 Tra cứu' }}
                </button>
              </div>
            </div>
            <div class="text-muted small mt-2">
              💡 Nhấn Enter hoặc nhấn nút Tra cứu sau khi quét/nhập mã serial
            </div>
          </div>
        </div>

        <!-- Error -->
        <div class="alert alert-danger" v-if="lookupError">
          ❌ {{ lookupError }}
          <button class="close" @click="lookupError = ''"><span>&times;</span></button>
        </div>

        <!-- Serial Info -->
        <div v-if="serialInfo" class="card card-mes mb-4">
          <div class="card-header d-flex align-items-center justify-content-between"
               :style="{ background: statusColor(serialInfo.status), color: '#fff' }">
            <div>
              <h5 class="mb-0">{{ serialInfo.serialCode }}</h5>
              <small>{{ serialInfo.productName }}</small>
            </div>
            <span class="badge badge-light" style="font-size:1rem; padding:8px 16px">
              {{ serialInfo.status }}
            </span>
          </div>
          <div class="card-body">
            <div class="row">
              <div class="col-md-6">
                <p class="mb-1"><strong>Lệnh sản xuất:</strong> {{ serialInfo.orderCode }}</p>
                <p class="mb-1"><strong>Sản phẩm:</strong> {{ serialInfo.productName }} ({{ serialInfo.productCode }})</p>
              </div>
              <div class="col-md-6">
                <p class="mb-1"><strong>Công đoạn hiện tại:</strong>
                  <span v-if="serialInfo.currentStepName" class="badge badge-info">{{ serialInfo.currentStepName }}</span>
                  <span v-else class="text-muted">—</span>
                </p>
                <p class="mb-1"><strong>Công đoạn tiếp theo:</strong>
                  <span v-if="serialInfo.nextStepName" class="badge badge-warning">{{ serialInfo.nextStepName }}</span>
                  <span v-else class="text-muted">—</span>
                </p>
              </div>
            </div>

            <!-- Process route progress -->
            <div class="mt-3" v-if="serialInfo.history && serialInfo.history.length">
              <strong class="small">Tiến trình công đoạn đã thực hiện:</strong>
              <div class="route-progress mt-2">
                <div v-for="(h, i) in serialInfo.history" :key="i"
                     class="route-step" :class="'result-' + h.result.toLowerCase()">
                  <div class="step-num">{{ i + 1 }}</div>
                  <div class="step-info">
                    <div class="step-name">{{ h.stepName }}</div>
                    <div class="step-result">{{ h.result }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Execution panel - only if not terminal status -->
          <div class="card-footer bg-light" v-if="canExecute">
            <h6 class="mb-3">⚡ Thực hiện công đoạn: <strong>{{serialInfo.currentStepName }}</strong></h6>
            <div class="alert alert-danger" v-if="execError">{{ execError }}</div>

            <div class="form-row">
              <div class="form-group col-md-4">
                <label class="small font-weight-bold">Kết quả <span class="text-danger">*</span></label>
                <select v-model="execForm.result" class="form-control">
                  <option value="">-- Chọn kết quả --</option>
                  <option value="OK">✅ OK - Đạt</option>
                  <option value="NG">❌ NG - Không đạt</option>
                  <option value="REWORK">🔄 REWORK - Làm lại</option>
                  <option value="SCRAP">🗑 SCRAP - Phế phẩm</option>
                  <option value="HOLD">⏸ HOLD - Giữ lại</option>
                </select>
              </div>
              <div class="form-group col-md-4" v-if="execForm.result === 'NG' || execForm.result === 'SCRAP'">
                <label class="small font-weight-bold">Loại lỗi</label>
                <select v-model="execForm.defectId" class="form-control">
                  <option value="">-- Chọn lỗi --</option>
                  <option v-for="d in defects" :key="d.id" :value="d.id">{{ d.defectName }}</option>
                </select>
              </div>
              
                            <div
                class="form-group col-md-4"
                v-if="execForm.result === 'REWORK'"
              >
                <label class="small font-weight-bold">
                  Công đoạn làm lại
                </label>

                <select
                  v-model="execForm.reworkStepId"
                  class="form-control"
                >
                  <option value="">
                    -- Chọn công đoạn --
                  </option>

                  <option
                    v-for="step in processSteps"
                    :key="step.id"
                    :value="step.id"
                  >
                    {{ step.stepOrder }} - {{ step.stepName }}
                  </option>

                </select>
              </div>
          
              <div class="form-group col-md-4">
                <label class="small font-weight-bold">Ghi chú</label>
                <input v-model="execForm.notes" type="text" class="form-control" placeholder="Ghi chú thêm...">
              </div>
            </div>

            <div class="d-flex gap-2 mt-2">
              <button class="btn btn-success" @click="executeStep" :disabled="!execForm.result || executing">
                {{ executing ? '⏳ Đang xử lý...' : '✅ Xác nhận thực hiện' }}
              </button>
              <button class="btn btn-outline-secondary ml-2" @click="resetScan">🔄 Quét serial khác</button>
            </div>
          </div>

          <div class="card-footer text-muted" v-else>
            <em>Serial ở trạng thái {{ serialInfo.status }} — không thể thực hiện thêm.</em>
            <button class="btn btn-sm btn-outline-primary ml-3" @click="resetScan">Quét serial khác</button>
          </div>
        </div>

        <!-- Success result -->
        <div class="alert alert-success alert-result" v-if="execResult">
          <h5>{{ execResult.message }}</h5>
          <p class="mb-0">Serial mới: <strong>{{ execResult.serialCode }}</strong> — Trạng thái: <strong>{{ execResult.status }}</strong></p>
          <button class="btn btn-success mt-2" @click="resetScan">📡 Quét serial tiếp theo</button>
        </div>

      </div>
    </div>
  </div>
</template>

<script>
import scanApi from '../api/scan'
import defectApi from '../api/defects'

export default {
  name: 'ScanProduction',
  data() {
    return {
      scanCode: '',
      lookingUp: false, executing: false,
      serialInfo: null,
      defects: [],
      //
      processSteps: [],
      //
      lookupError: '', execError: '',
      execForm: { result: '', defectId: '',reworkStepId: '', notes: '' },
      execResult: null
    }
  },
  computed: {
    canExecute() {
      if (!this.serialInfo) return false
      const terminal = ['SCRAP', 'FINISHED', 'CANCELLED']
      return !terminal.includes(this.serialInfo.status)
    }
  },
  methods: {
    async lookupSerial() {
      if (!this.scanCode.trim()) return
      this.lookingUp = true
      this.lookupError = ''
      this.serialInfo = null
      this.execResult = null
      this.execForm = { result: '', defectId: '', notes: '' }
      try {
        const res = await scanApi.getSerialInfo(this.scanCode.trim())
        this.serialInfo = res.data
        //
        this.processSteps = res.data.processSteps || []
      } catch (e) {
        this.lookupError = e.response?.data?.message || 'Không tìm thấy serial'
      } finally { this.lookingUp = false }
    },
    // async executeStep() {
    //   if (!this.execForm.result) return
    //   this.executing = true; this.execError = ''
    //   try {
    //     const payload = {
    //       serialCode: this.serialInfo.serialCode,
    //       //stepId: this.serialInfo.nextStepId || this.serialInfo.currentStepId,
    //       stepId: this.serialInfo.currentStepId,
    //       result: this.execForm.result,
    //       notes: this.execForm.notes || null,
    //       defectId: this.execForm.defectId || null
    //     }
    //     const res = await scanApi.execute(payload)
    //     this.execResult = res.data
    //     this.serialInfo = null
    //     this.scanCode = ''
    //   } catch (e) {
    //     this.execError = e.response?.data?.message || 'Lỗi khi thực hiện công đoạn'
    //   } finally { this.executing = false }
    // },
    async executeStep() {
      if (!this.execForm.result) return
      // Bắt buộc chọn công đoạn khi REWORK
      if (
        this.execForm.result === 'REWORK' &&
        !this.execForm.reworkStepId
      ) {
        this.execError = 'Vui lòng chọn công đoạn làm lại'
        return
      }
      this.executing = true
      this.execError = ''
      try {
        const payload = {
          serialCode: this.serialInfo.serialCode,

          stepId: this.serialInfo.currentStepId,

          result: this.execForm.result,

          notes: this.execForm.notes || null,

          defectId: this.execForm.defectId || null,

          reworkStepId: this.execForm.reworkStepId || null
        }

        const res = await scanApi.execute(payload)

        this.execResult = res.data

        this.serialInfo = null
        this.scanCode = ''

      } catch (e) {

        this.execError =
          e.response?.data?.message ||
          'Lỗi khi thực hiện công đoạn'

      } finally {

        this.executing = false

      }
    },
    resetScan() {
      this.scanCode = ''
      this.serialInfo = null
      this.execResult = null
      this.lookupError = ''
      this.execError = ''
      this.execForm = { result: '', defectId: '', notes: '' }
      this.$nextTick(() => { if (this.$refs.scanInput) this.$refs.scanInput.focus() })
    },
    statusColor(status) {
      const map = {
        WAITING: '#6c757d', IN_PROGRESS: '#1565C0', OK: '#2E7D32',
        NG: '#C62828', REWORK: '#fd7e14', SCRAP: '#6f42c1',
        HOLD: '#F9A825', FINISHED: '#0288D1'
      }
      return map[status] || '#6c757d'
    },
    async loadDefects() {
      const res = await defectApi.getAll()
      this.defects = res.data.filter(d => d.isActive)
    }
  },
  mounted() {
    this.loadDefects()
    this.$nextTick(() => { if (this.$refs.scanInput) this.$refs.scanInput.focus() })
  }
}
</script>

<style scoped>
.scan-card { border-top: 4px solid #1565C0; }

.route-progress { display: flex; flex-wrap: wrap; gap: 8px; }
.route-step {
  display: flex; align-items: center; gap: 8px;
  padding: 6px 12px; border-radius: 8px;
  border: 2px solid #ddd; background: #f8f9fa;
  font-size: 0.82rem;
}
.result-ok     { border-color: #2E7D32; background: #e8f5e9; }
.result-ng     { border-color: #C62828; background: #ffebee; }
.result-rework { border-color: #fd7e14; background: #fff3e0; }
.result-scrap  { border-color: #6f42c1; background: #f3e5f5; }
.result-hold   { border-color: #F9A825; background: #fffde7; }

.step-num {
  width: 24px; height: 24px;
  background: #1565C0; color: #fff;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-weight: 700; font-size: 0.75rem;
  flex-shrink: 0;
}
.step-name { font-weight: 600; }
.step-result { font-size: 0.7rem; color: #666; text-transform: uppercase; }

.alert-result {
  border-radius: 10px; border: none;
  padding: 20px 24px;
  box-shadow: 0 4px 16px rgba(0,128,0,0.15);
}
</style>
