<template>
  <div>
    <el-row :gutter="16">
      <!-- 左：贡献表单 -->
      <el-col :span="10">
        <el-card shadow="never">
          <template #header>
            <div class="card-header-flex">
              <span>✍️ 匿名贡献薪资数据</span>
              <el-tooltip content="不收集姓名/身份证/公司全称，审核通过可获得积分与精准匹配优先权">
                <el-icon color="#909399"><InfoFilled /></el-icon>
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

      <!-- 右：我的贡献 + 白皮书 -->
      <el-col :span="14">
        <el-card shadow="never" style="margin-bottom:16px">
          <template #header>📈 最新白皮书</template>
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { InfoFilled } from '@element-plus/icons-vue'
import { salaryApi } from '../api'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const formRef = ref()
const submitting = ref(false)
const loadingC = ref(false)
const whitepaper = ref(null)
const contributions = ref([])

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

async function submit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    await salaryApi.contribute(form)
    ElMessage.success('贡献成功，等待审核')
    form.city = ''; form.position = ''; form.salaryMin = null; form.salaryMax = null
    form.offerMonth = ''
    loadContributions()
    auth.fetchUserInfo() // 刷新积分显示
  } finally {
    submitting.value = false
  }
}

async function loadContributions() {
  loadingC.value = true
  try {
    const res = await salaryApi.myContributions({ page: 1, size: 10 })
    contributions.value = res.data?.records || []
  } finally {
    loadingC.value = false
  }
}

onMounted(() => {
  salaryApi.latestWhitepaper().then(res => (whitepaper.value = res.data)).catch(() => {})
  loadContributions()
})
</script>

<style scoped>
.wp-meta { font-size: 12px; color: #909399; margin: 6px 0 0; }
</style>
