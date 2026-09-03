<template>
  <div>
    <!-- 管理员审核区 -->
    <el-card v-if="isAdmin" shadow="never" style="margin-bottom:16px">
      <template #header>
        <div class="card-header-flex">
          <span>🛡️ 薪资审核（管理员）</span>
          <el-button type="primary" size="small" :loading="generating" @click="generateWp">
            重新生成白皮书
          </el-button>
        </div>
      </template>
      <el-table :data="pendingList" v-loading="pendingLoading" stripe>
        <el-table-column prop="city" label="城市" width="80" />
        <el-table-column prop="position" label="岗位" min-width="110" />
        <el-table-column label="薪资(元/月)" width="130">
          <template #default="{ row }">{{ row.salaryMin }}~{{ row.salaryMax }}</template>
        </el-table-column>
        <el-table-column prop="companyScale" label="公司规模" width="90" />
        <el-table-column prop="educationLevel" label="学历" width="70">
          <template #default="{ row }">{{ ['专科', '本科', '硕士', '博士', '其他'][row.educationLevel] || '-' }}</template>
        </el-table-column>
        <el-table-column prop="offerMonth" label="offer月" width="90" />
        <el-table-column label="异常检测" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.anomalyFlag" type="danger" size="small">3σ 异常</el-tag>
            <el-tag v-else type="success" size="small" effect="plain">正常</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="success" @click="review(row, 'APPROVE')">通过</el-button>
            <el-button size="small" type="danger" plain @click="review(row, 'REJECT')">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!pendingLoading && !pendingList.length" description="暂无待审核数据" :image-size="60" />
    </el-card>

    <el-row :gutter="16">
      <!-- 左：贡献表单 -->
      <el-col :span="10">
        <el-card shadow="never">
          <template #header>
            <div class="card-header-flex">
              <span>✍️ 匿名贡献薪资数据</span>
              <el-tooltip content="不收集姓名/身份证/公司全称，审核通过可获得积分与精准匹配优先权">
                <el-icon color="var(--foreground-muted)"><InfoFilled /></el-icon>
              </el-tooltip>
            </div>
          </template>

          <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
            <el-row :gutter="10">
              <el-col :span="12">
                <el-form-item label="城市" prop="city">
                  <el-input v-model="form.city" placeholder="北京" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="岗位" prop="position">
                  <el-input v-model="form.position" placeholder="Java开发工程师" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="10">
              <el-col :span="12">
                <el-form-item label="薪资下限" prop="salaryMin">
                  <el-input-number v-model="form.salaryMin" :min="0" :step="500" style="width:100%" placeholder="8000" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="薪资上限" prop="salaryMax">
                  <el-input-number v-model="form.salaryMax" :min="0" :step="500" style="width:100%" placeholder="12000" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="10">
              <el-col :span="12">
                <el-form-item label="公司规模">
                  <el-select v-model="form.companyScale" placeholder="选填" clearable style="width:100%">
                    <el-option v-for="s in ['0-50人', '50-200人', '200-500人', '500-1000人', '1000+']" :key="s" :label="s" :value="s" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="行业">
                  <el-input v-model="form.industry" placeholder="互联网（选填）" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="10">
              <el-col :span="12">
                <el-form-item label="职位类型" prop="jobType">
                  <el-select v-model="form.jobType" style="width:100%">
                    <el-option :value="0" label="实习" />
                    <el-option :value="1" label="校招" />
                    <el-option :value="2" label="社招" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="学历" prop="educationLevel">
                  <el-select v-model="form.educationLevel" style="width:100%">
                    <el-option v-for="(l, i) in ['专科', '本科', '硕士', '博士', '其他']" :key="i" :value="i" :label="l" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="10">
              <el-col :span="12">
                <el-form-item label="双一流">
                  <el-switch v-model="form.isDoubleFirstClass" :active-value="1" :inactive-value="0" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="offer月份" prop="offerMonth">
                  <el-date-picker v-model="form.offerMonth" type="month" value-format="YYYY-MM" placeholder="2026-07" style="width:100%" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-button type="primary" style="width:100%" :loading="submitting" @click="submit">
              提交贡献（审核通过 +20 积分）
            </el-button>
          </el-form>
        </el-card>
      </el-col>

      <!-- 右：白皮书 + 我的贡献 -->
      <el-col :span="14">
        <el-card shadow="never" style="margin-bottom:16px">
          <template #header>
            <div class="card-header-flex">
              <span>📈 最新白皮书</span>
              <el-tag v-if="wpReport" size="small" effect="plain">{{ wpReport.totalSamples }} 份样本</el-tag>
            </div>
          </template>
          <template v-if="whitepaper">
            <h3>{{ whitepaper.title }}</h3>
            <p class="wp-meta">版本 {{ whitepaper.version }} · 生成于 {{ (whitepaper.generatedAt || '').slice(0, 10) }}</p>
            <el-alert
              v-if="whitepaper.advancedUnlocked"
              type="success" :closable="false"
              title="已解锁完整版（P25/P50/P75/P99 分位统计）"
              style="margin-top:10px"
            />
            <el-alert
              v-else
              type="info" :closable="false"
              title="当前为简版 · 贡献一条薪资数据即可解锁高级章节"
              style="margin-top:10px"
            />

            <!-- 白皮书正文：整体分位统计 -->
            <div v-if="wpReport && wpReport.overall && wpReport.overall.sampleCount > 0" class="wp-body">
              <div class="wp-stats">
                <div class="wp-stat">
                  <div class="ws-val">{{ fmtK(wpReport.overall.p25) }}</div>
                  <div class="ws-label">P25</div>
                </div>
                <div class="wp-stat hl">
                  <div class="ws-val">{{ fmtK(wpReport.overall.p50) }}</div>
                  <div class="ws-label">P50 中位数</div>
                </div>
                <div class="wp-stat">
                  <div class="ws-val">{{ fmtK(wpReport.overall.p75) }}</div>
                  <div class="ws-label">P75</div>
                </div>
                <div class="wp-stat">
                  <div class="ws-val">{{ fmtK(wpReport.overall.p99) }}</div>
                  <div class="ws-label">P99</div>
                </div>
              </div>

              <!-- 分组明细表 -->
              <div v-if="wpReport.groups && wpReport.groups.length" class="wp-groups">
                <div class="wp-sec-title">分岗位/城市明细（已解锁）</div>
                <el-table :data="wpReport.groups" size="small" stripe>
                  <el-table-column prop="position" label="岗位" min-width="100" />
                  <el-table-column prop="city" label="城市" width="70" />
                  <el-table-column prop="industry" label="行业" width="80" />
                  <el-table-column label="P50" width="80">
                    <template #default="{ row }">{{ fmtK(row.p50) }}</template>
                  </el-table-column>
                  <el-table-column label="区间" min-width="110">
                    <template #default="{ row }">{{ fmtK(row.p25) }} ~ {{ fmtK(row.p99) }}</template>
                  </el-table-column>
                  <el-table-column prop="sampleCount" label="样本" width="60" />
                </el-table>
              </div>
              <div v-else class="wp-empty-groups">当前样本量较少，暂无分组统计；贡献数据越多，分组越丰富</div>
            </div>
            <div v-else class="wp-empty-groups">
              {{ whitepaper.advancedUnlocked ? '白皮书生成中数据不足，待新贡献审核通过后重新生成' : '解锁完整版后可查看分位统计' }}
            </div>
          </template>
          <el-empty v-else description="白皮书尚未生成，等待管理员发布" :image-size="70" style="padding:20px 0" />
        </el-card>

        <el-card shadow="never">
          <template #header>🗂️ 我的贡献记录</template>
          <el-table :data="contributions" stripe>
            <el-table-column prop="position" label="岗位" min-width="120" />
            <el-table-column prop="city" label="城市" width="80" />
            <el-table-column label="薪资(元/月)" width="140">
              <template #default="{ row }">{{ row.salaryMin }}~{{ row.salaryMax }}</template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="['warning', 'success', 'danger'][row.verified]" size="small">
                  {{ ['待审核', '已通过', '已驳回'][row.verified] }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="offerMonth" label="offer月" width="90" />
          </el-table>
          <el-empty v-if="!contributions.length && !loadingC" description="暂无贡献，提交第一条数据吧" :image-size="70" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { InfoFilled } from '@element-plus/icons-vue'
import { salaryApi, adminApi } from '../api'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const isAdmin = computed(() => auth.user?.role === 9)
const formRef = ref()
const submitting = ref(false)
const loadingC = ref(false)
const whitepaper = ref(null)
const contributions = ref([])
const pendingList = ref([])
const pendingLoading = ref(false)
const generating = ref(false)

const form = reactive({
  city: '', position: '', salaryMin: null, salaryMax: null,
  companyScale: '', industry: '', jobType: 1, educationLevel: 1,
  isDoubleFirstClass: 0, offerMonth: ''
})

const rules = {
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
  position: [{ required: true, message: '请输入岗位', trigger: 'blur' }],
  salaryMin: [{ required: true, message: '必填', trigger: 'blur' }],
  salaryMax: [{ required: true, message: '必填', trigger: 'blur' }],
  jobType: [{ required: true, message: '必选', trigger: 'change' }],
  educationLevel: [{ required: true, message: '必选', trigger: 'change' }],
  offerMonth: [{ required: true, message: '必选', trigger: 'change' }]
}

// 解析白皮书 reportJson
const wpReport = computed(() => {
  if (!whitepaper.value?.reportJson) return null
  try {
    return JSON.parse(whitepaper.value.reportJson)
  } catch (e) {
    return null
  }
})

// 金额格式化：23000 → 2.3w
function fmtK(v) {
  if (v == null) return '-'
  return (v / 10000).toFixed(1).replace(/\.0$/, '') + 'w'
}

async function submit() {
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }
  submitting.value = true
  try {
    await salaryApi.contribute(form)
    ElMessage.success('贡献成功，等待审核')
    form.city = ''; form.position = ''; form.salaryMin = null; form.salaryMax = null
    form.offerMonth = ''
    loadContributions()
    if (isAdmin.value) loadPending()
    auth.fetchUserInfo() // 刷新积分显示
  } catch (e) {
    // 错误已由拦截器弹出
  } finally {
    submitting.value = false
  }
}

async function loadContributions() {
  loadingC.value = true
  try {
    const res = await salaryApi.myContributions({ page: 1, size: 10 })
    contributions.value = res.data?.records || []
  } catch (e) { /* 静默 */ } finally {
    loadingC.value = false
  }
}

async function loadWhitepaper() {
  try {
    const res = await salaryApi.latestWhitepaper()
    whitepaper.value = res.data
  } catch (e) { /* 静默 */ }
}

// ===== 管理员：待审列表 / 审核 / 生成 =====
async function loadPending() {
  pendingLoading.value = true
  try {
    const res = await adminApi.salaryPending({ page: 1, size: 20 })
    pendingList.value = res.data?.records || []
  } catch (e) { /* 静默 */ } finally {
    pendingLoading.value = false
  }
}

async function review(row, action) {
  const label = action === 'APPROVE' ? '通过' : '驳回'
  try {
    await ElMessageBox.confirm(
      `确定${label}「${row.city}·${row.position} ${row.salaryMin}~${row.salaryMax}」这条贡献？${action === 'APPROVE' ? '（贡献者将获 30 积分）' : ''}`,
      '审核确认',
      { type: action === 'APPROVE' ? 'success' : 'warning' }
    )
  } catch (e) {
    return
  }
  try {
    await adminApi.salaryReview(row.id, { action })
    ElMessage.success(`已${label}`)
    loadPending()
    loadWhitepaper()
  } catch (e) {
    // 错误已由拦截器弹出
  }
}

async function generateWp() {
  generating.value = true
  try {
    await adminApi.generateWhitepaper()
    ElMessage.success('白皮书已重新生成')
    await loadWhitepaper()
  } catch (e) {
    // 错误已由拦截器弹出
  } finally {
    generating.value = false
  }
}

onMounted(() => {
  loadWhitepaper()
  loadContributions()
  if (isAdmin.value) loadPending()
})
</script>

<style scoped>
.wp-meta { font-size: 12px; color: var(--foreground-muted); margin: 6px 0 0; }
.wp-body { margin-top: 14px; }
.wp-stats { display: flex; gap: 12px; }
.wp-stat {
  flex: 1; text-align: center; padding: 14px 8px;
  border-radius: 12px; background: var(--card);
  border: 1px solid var(--hairline);
}
.wp-stat.hl { background: linear-gradient(135deg, rgba(99, 102, 241, 0.25), rgba(168, 85, 247, 0.18)); border-color: rgba(139, 92, 246, 0.5); }
.ws-val { font-size: 22px; font-weight: 800; color: var(--foreground); }
.wp-stat.hl .ws-val { font-size: 26px; }
.ws-label { font-size: 12px; color: var(--foreground-muted); margin-top: 4px; }
.wp-groups { margin-top: 16px; }
.wp-sec-title { font-size: 13px; font-weight: 600; margin-bottom: 8px; color: var(--foreground); }
.wp-empty-groups { font-size: 13px; color: var(--foreground-subtle); text-align: center; padding: 18px 0; }
.card-header-flex { display: flex; justify-content: space-between; align-items: center; }
</style>
