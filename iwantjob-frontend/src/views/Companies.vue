<template>
  <div>
    <!-- ===== 企业列表 ===== -->
    <template v-if="!currentCompany">
      <el-card shadow="never" style="margin-bottom:16px">
        <div class="list-head">
          <span class="title">🏢 知名企业主页</span>
          <el-select v-model="industry" placeholder="全部行业" clearable style="width:160px" @change="loadList">
            <el-option v-for="i in industries" :key="i" :value="i" :label="i" />
          </el-select>
        </div>
      </el-card>

      <div v-loading="loading">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="c in list" :key="c.id" style="margin-bottom:16px">
            <el-card shadow="hover" class="c-card" @click="openCompany(c.id)">
              <div class="c-head">
                <span class="c-logo">{{ c.logo || '🏢' }}</span>
                <div class="c-name-wrap">
                  <div class="c-name">{{ c.name }}</div>
                  <div class="c-sub">{{ c.industry || '互联网' }} · {{ c.scale || '-' }}</div>
                </div>
                <el-tag v-if="c.jobCount" type="success" size="small">{{ c.jobCount }} 个在招</el-tag>
              </div>
              <div class="c-intro">{{ c.intro || '暂无介绍' }}</div>
              <div class="c-foot">
                <span>📍 {{ c.headquarters || '-' }}</span>
                <el-tag v-if="c.claimedBy" type="warning" size="small" effect="plain">已认领</el-tag>
              </div>
            </el-card>
          </el-col>
        </el-row>
        <el-empty v-if="!loading && !list.length" description="暂无企业数据" />
      </div>
    </template>

    <!-- ===== 企业详情 ===== -->
    <template v-else>
      <el-page-header @back="back" content="" style="margin-bottom:16px">
        <template #content>
          <span class="d-title">{{ currentCompany.logo }} {{ currentCompany.name }}</span>
          <el-button v-if="canEdit" type="primary" size="small" style="margin-left:12px" @click="openEdit">编辑主页</el-button>
        </template>
      </el-page-header>

      <el-row :gutter="16">
        <el-col :md="10" :sm="24">
          <el-card shadow="never">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="行业">{{ currentCompany.industry || '-' }}</el-descriptions-item>
              <el-descriptions-item label="规模">{{ currentCompany.scale || '-' }}</el-descriptions-item>
              <el-descriptions-item label="总部">{{ currentCompany.headquarters || '-' }}</el-descriptions-item>
              <el-descriptions-item label="官网">
                <el-link v-if="currentCompany.website" type="primary" :href="currentCompany.website" target="_blank">{{ currentCompany.website }}</el-link>
                <span v-else>-</span>
              </el-descriptions-item>
            </el-descriptions>
            <h4 style="margin:16px 0 8px">📌 企业介绍</h4>
            <p class="pre">{{ currentCompany.intro || '暂无' }}</p>
            <h4 style="margin:16px 0 8px">🌱 企业文化</h4>
            <p class="pre">{{ currentCompany.culture || '暂无' }}</p>
            <h4 style="margin:16px 0 8px">🎁 福利待遇</h4>
            <p class="pre">{{ currentCompany.welfare || '暂无' }}</p>
          </el-card>
        </el-col>

        <el-col :md="14" :sm="24">
          <el-card shadow="never">
            <template #header>该企业在招职位（{{ jobTotal }}）</template>
            <el-table :data="jobs" v-loading="jobLoading" stripe size="small" @row-click="goJobs">
              <el-table-column prop="title" label="职位" min-width="180" />
              <el-table-column label="批次" width="80">
                <template #default="{ row }">
                  <el-tag :type="batchType(row.recruitmentBatch)" size="small">{{ batchLabel(row.recruitmentBatch) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="salaryRange" label="薪资" width="120" />
              <el-table-column prop="location" label="城市" width="90" />
            </el-table>
            <el-pagination
              v-if="jobTotal > 5"
              style="margin-top:12px; justify-content:flex-end"
              layout="total, prev, pager, next" small
              :total="jobTotal" :page-size="jobSize" :current-page="jobPage"
              @current-change="loadCompanyJobs"
            />
          </el-card>
        </el-col>
      </el-row>
    </template>

    <!-- HR 编辑弹窗 -->
    <el-dialog v-model="editDialog" title="编辑企业主页" width="560px">
      <el-alert type="info" :closable="false" style="margin-bottom:12px"
        title="认领规则：需发布过该企业的职位方可编辑；管理员可直接编辑" />
      <el-form :model="editForm" label-width="90px">
        <el-form-item label="LOGO"><el-input v-model="editForm.logo" style="width:100px" placeholder="emoji" /></el-form-item>
        <el-form-item label="行业"><el-input v-model="editForm.industry" /></el-form-item>
        <el-form-item label="规模"><el-input v-model="editForm.scale" /></el-form-item>
        <el-form-item label="总部"><el-input v-model="editForm.headquarters" /></el-form-item>
        <el-form-item label="官网"><el-input v-model="editForm.website" /></el-form-item>
        <el-form-item label="企业介绍"><el-input v-model="editForm.intro" type="textarea" :rows="4" /></el-form-item>
        <el-form-item label="企业文化"><el-input v-model="editForm.culture" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="福利待遇"><el-input v-model="editForm.welfare" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveCompany">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { companyApi, jobApi } from '../api'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const canEdit = computed(() => [2, 9].includes(auth.user?.role))

const list = ref([])
const industries = ref([])
const industry = ref('')
const loading = ref(false)

const currentCompany = ref(null)
const jobs = ref([])
const jobTotal = ref(0)
const jobPage = ref(1)
const jobSize = ref(5)
const jobLoading = ref(false)

const editDialog = ref(false)
const saving = ref(false)
const editForm = reactive({ id: null, logo: '', industry: '', scale: '', headquarters: '', website: '', intro: '', culture: '', welfare: '' })

const batchMap = { 0: '日常', 1: '春招', 2: '秋招', 3: '实习批' }
const batchLabel = b => batchMap[b] || '日常'
const batchType = b => ({ 1: 'success', 2: 'warning', 3: 'primary' }[b] || 'info')

async function loadList() {
  loading.value = true
  try {
    const res = await companyApi.list(industry.value ? { industry: industry.value } : {})
    list.value = res.data || []
    const set = new Set(list.value.map(c => c.industry).filter(Boolean))
    industries.value = [...set]
  } finally { loading.value = false }
}

async function openCompany(id) {
  router.replace({ query: { companyId: id } })
  await loadCompany(id)
}

async function loadCompany(id) {
  const res = await companyApi.detail(id)
  currentCompany.value = res.data
  jobPage.value = 1
  loadCompanyJobs()
}

async function loadCompanyJobs(p = jobPage.value) {
  jobPage.value = p
  jobLoading.value = true
  try {
    const res = await jobApi.search({ companyId: currentCompany.value.id, page: p, size: jobSize.value })
    jobs.value = res.data?.records || []
    jobTotal.value = res.data?.total || 0
  } finally { jobLoading.value = false }
}

function back() {
  currentCompany.value = null
  router.replace({ query: {} })
  loadList()
}

function goJobs() { router.push('/jobs') }

function openEdit() {
  const c = currentCompany.value
  Object.assign(editForm, {
    id: c.id, logo: c.logo || '', industry: c.industry || '', scale: c.scale || '',
    headquarters: c.headquarters || '', website: c.website || '',
    intro: c.intro || '', culture: c.culture || '', welfare: c.welfare || ''
  })
  editDialog.value = true
}

async function saveCompany() {
  saving.value = true
  try {
    const { id, ...data } = editForm
    await companyApi.update(id, data)
    ElMessage.success('企业主页已更新')
    editDialog.value = false
    await loadCompany(id)
  } finally { saving.value = false }
}

onMounted(() => {
  const cid = route.query.companyId
  if (cid) loadCompany(Number(cid))
  else loadList()
})
</script>

<style scoped>
.list-head { display: flex; justify-content: space-between; align-items: center; }
.list-head .title { font-weight: 600; font-size: 16px; }
.c-card { cursor: pointer; }
.c-head { display: flex; gap: 10px; align-items: center; }
.c-logo { font-size: 34px; }
.c-name-wrap { flex: 1; overflow: hidden; }
.c-name { font-weight: 600; font-size: 15px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.c-sub { font-size: 12px; color: var(--foreground-muted); margin-top: 2px; }
.c-intro { font-size: 13px; color: var(--foreground-muted); line-height: 1.6; height: 63px; margin-top: 10px; overflow: hidden; display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; }
.c-foot { display: flex; justify-content: space-between; align-items: center; margin-top: 10px; font-size: 12px; color: var(--foreground-muted); }
.d-title { font-size: 18px; font-weight: 600; }
.pre { white-space: pre-wrap; color: var(--foreground-muted); font-size: 13px; line-height: 1.7; }
</style>