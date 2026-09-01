<template>
  <div>
    <!-- 招聘批次 Tab -->
    <el-card shadow="never" style="margin-bottom:16px">
      <el-radio-group v-model="query.batch" @change="load(1)">
        <el-radio-button :value="null">全部批次</el-radio-button>
        <el-radio-button :value="1">🌱 春招</el-radio-button>
        <el-radio-button :value="2">🍂 秋招</el-radio-button>
        <el-radio-button :value="3">🧪 实习批</el-radio-button>
        <el-radio-button :value="0">💼 日常</el-radio-button>
      </el-radio-group>

      <el-form inline @submit.prevent style="margin-top:12px">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="搜索职位/描述/要求" clearable style="width:220px" @keyup.enter="load(1)" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="query.type" placeholder="全部" clearable style="width:120px">
            <el-option :value="0" label="实习" />
            <el-option :value="1" label="校招" />
            <el-option :value="2" label="社招" />
          </el-select>
        </el-form-item>
        <el-form-item label="城市">
          <el-input v-model="query.city" placeholder="如 北京" clearable style="width:140px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="load(1)">搜索</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 职位列表 -->
    <el-card shadow="never">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column label="职位" min-width="260">
          <template #default="{ row }">
            <div class="job-title" @click="openDetail(row)">
              {{ row.title }}
              <el-tag v-if="row.recruitmentBatch" :type="batchType(row.recruitmentBatch)" size="small" style="margin-left:6px">
                {{ batchLabel(row.recruitmentBatch) }}
              </el-tag>
            </div>
            <div class="job-company">
              <span class="company-link" v-if="row.companyId" @click.stop="goCompany(row.companyId)">{{ row.companyName }}</span>
              <span v-else>{{ row.companyName }}</span>
              · {{ row.location }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="90">
          <template #default="{ row }">
            <el-tag :type="['info', 'primary', 'success'][row.jobType] || 'info'" size="small">
              {{ ['实习', '校招', '社招'][row.jobType] || '其他' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="salaryRange" label="薪资" width="140" />
        <el-table-column prop="viewCount" label="浏览" width="80" />
        <el-table-column label="发布时间" width="110">
          <template #default="{ row }">{{ (row.createdAt || '').slice(0, 10) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button v-if="canFav" size="small" circle
              :type="favSet.has(row.id) ? 'warning' : 'default'"
              :icon="favSet.has(row.id) ? StarFilled : Star"
              @click="toggleFav(row)" />
            <el-button size="small" type="primary" @click="openDetail(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        style="margin-top:16px; justify-content:flex-end"
        layout="total, prev, pager, next"
        :total="total" :page-size="query.size" :current-page="query.page"
        @current-change="load"
      />
    </el-card>

    <!-- 详情/投递抽屉 -->
    <el-drawer v-model="drawer" :title="current?.title || '职位详情'" size="480px">
      <template v-if="current">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="公司">
            <span class="company-link" v-if="current.companyId" @click="goCompany(current.companyId)">{{ current.companyName }}</span>
            <span v-else>{{ current.companyName }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="招聘批次">{{ batchLabel(current.recruitmentBatch) }}</el-descriptions-item>
          <el-descriptions-item label="城市">{{ current.location }}</el-descriptions-item>
          <el-descriptions-item label="薪资">{{ current.salaryRange || '面议' }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ ['实习', '校招', '社招'][current.jobType] }}</el-descriptions-item>
          <el-descriptions-item label="联系邮箱">{{ current.contactEmail || '-' }}</el-descriptions-item>
        </el-descriptions>

        <h4 style="margin:16px 0 8px">职位描述</h4>
        <p class="pre">{{ current.description || '暂无' }}</p>
        <h4 style="margin:16px 0 8px">任职要求</h4>
        <p class="pre">{{ current.requirements || '暂无' }}</p>

        <el-divider />
        <el-form>
          <el-form-item label="求职信（可选）">
            <el-input v-model="coverLetter" type="textarea" :rows="3" placeholder="向 HR 简短介绍自己" />
          </el-form-item>
          <el-button type="primary" style="width:100%" :loading="applying" @click="handleApply">
            一键投递
          </el-button>
        </el-form>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Star, StarFilled } from '@element-plus/icons-vue'
import { jobApi } from '../api'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const canFav = computed(() => [0, 1].includes(auth.user?.role))

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ keyword: '', type: null, city: '', batch: null, page: 1, size: 10 })
const favSet = ref(new Set())

const drawer = ref(false)
const current = ref(null)
const coverLetter = ref('')
const applying = ref(false)

const batchMap = { 0: '日常', 1: '春招', 2: '秋招', 3: '实习批' }
const batchLabel = b => batchMap[b] || '日常'
const batchType = b => ({ 1: 'success', 2: 'warning', 3: 'primary' }[b] || 'info')

async function load(page = query.page) {
  query.page = page
  loading.value = true
  try {
    const res = await jobApi.search(query)
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

async function loadFavs() {
  if (!canFav.value) return
  try {
    const res = await jobApi.favoriteIds()
    favSet.value = new Set(res.data || [])
  } catch (e) { /* HR 无权限时静默 */ }
}

async function toggleFav(row) {
  const res = await jobApi.toggleFavorite(row.id)
  if (res.data.favored) { favSet.value.add(row.id); ElMessage.success('已收藏，可在「我的收藏」查看') }
  else { favSet.value.delete(row.id); ElMessage.info('已取消收藏') }
}

function goCompany(companyId) {
  router.push({ path: '/companies', query: { companyId } })
}

async function openDetail(row) {
  current.value = row
  drawer.value = true
}

async function handleApply() {
  applying.value = true
  try {
    await jobApi.apply(current.value.id, { coverLetter: coverLetter.value || null })
    ElMessage.success('投递成功！可在「我的投递」查看进度')
    drawer.value = false
    coverLetter.value = ''
  } finally {
    applying.value = false
  }
}

onMounted(() => {
  load(1)
  loadFavs()
})
</script>

<style scoped>
.job-title { color: #409eff; cursor: pointer; font-weight: 500; }
.job-title:hover { text-decoration: underline; }
.job-company { font-size: 12px; color: #909399; margin-top: 2px; }
.company-link { color: #67c23a; cursor: pointer; }
.company-link:hover { text-decoration: underline; }
.pre { white-space: pre-wrap; color: #606266; font-size: 13px; line-height: 1.7; }
</style>