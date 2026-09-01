<template>
  <el-card shadow="never">
    <template #header>
      <div class="card-header-flex">
        <span>我的投递</span>
        <el-button :icon="Refresh" circle size="small" @click="load" />
      </div>
    </template>

    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="jobTitle" label="职位" min-width="180" />
      <el-table-column prop="companyName" label="公司" min-width="140" />
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="statusMap[row.status]?.type" size="small">{{ statusMap[row.status]?.label || '未知' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="hrRemark" label="HR 备注" min-width="150" show-overflow-tooltip />
      <el-table-column label="投递时间" width="110">
        <template #default="{ row }">{{ (row.appliedAt || '').slice(0, 10) }}</template>
      </el-table-column>
    </el-table>

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
import { Refresh } from '@element-plus/icons-vue'
import { jobApi } from '../api'

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)

const statusMap = {
  0: { label: '已投递', type: 'info' },
  1: { label: '初筛中', type: 'primary' },
  2: { label: '面试中', type: 'warning' },
  3: { label: '已录用', type: 'success' },
  4: { label: '已拒绝', type: 'danger' }
}

async function load() {
  loading.value = true
  try {
    const res = await jobApi.myApplied({ page: page.value, size: size.value })
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>
