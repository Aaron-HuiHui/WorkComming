<template>
  <el-card shadow="never">
    <template #header>
      <div class="card-header-flex">
        <span>我的收藏</span>
        <el-button :icon="Refresh" circle size="small" @click="load" />
      </div>
    </template>

    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column label="职位" min-width="240">
        <template #default="{ row }">
          <div class="job-title" @click="$router.push('/jobs')">{{ row.title }}</div>
          <div class="job-company">{{ row.companyName }} · {{ row.location }}</div>
        </template>
      </el-table-column>
      <el-table-column label="批次" width="90">
        <template #default="{ row }">
          <el-tag :type="batchType(row.recruitmentBatch)" size="small">{{ batchLabel(row.recruitmentBatch) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="类型" width="80">
        <template #default="{ row }">
          <el-tag :type="['info', 'primary', 'success'][row.jobType] || 'info'" size="small">
            {{ ['实习', '校招', '社招'][row.jobType] || '其他' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="salaryRange" label="薪资" width="130" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="danger" text :icon="StarFilled" @click="unfav(row)">取消收藏</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && !list.length" description="暂无收藏职位，去职位广场看看吧" />

    <el-pagination
      style="margin-top:16px; justify-content:flex-end"
      layout="total, prev, pager, next"
      :total="total" :page-size="size" :current-page="page"
      @current-change="p => { page = p; load() }"
    />
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, StarFilled } from '@element-plus/icons-vue'
import { jobApi } from '../api'

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)

const batchMap = { 0: '日常', 1: '春招', 2: '秋招', 3: '实习批' }
const batchLabel = b => batchMap[b] || '日常'
const batchType = b => ({ 1: 'success', 2: 'warning', 3: 'primary' }[b] || 'info')

async function load() {
  loading.value = true
  try {
    const res = await jobApi.myFavorites({ page: page.value, size: size.value })
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) { /* 静默：保持空态 */ } finally { loading.value = false }
}

async function unfav(row) {
  const res = await jobApi.toggleFavorite(row.id)
  if (!res.data.favored) {
    ElMessage.success('已取消收藏')
    load()
  }
}

onMounted(load)
</script>

<style scoped>
.card-header-flex { display: flex; justify-content: space-between; align-items: center; }
.job-title { color: #62aaff; cursor: pointer; font-weight: 500; }
html.light .job-title { color: #1d6fd0; }
.job-title:hover { text-decoration: underline; }
.job-company { font-size: 12px; color: var(--foreground-muted); margin-top: 2px; }
</style>