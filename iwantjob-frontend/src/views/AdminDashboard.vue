<template>
  <div v-loading="loading">
    <!-- 统计卡片 -->
    <el-row :gutter="16" style="margin-bottom:16px">
      <el-col :xs="12" :sm="8" :md="4" v-for="s in cards" :key="s.key">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon">{{ s.icon }}</div>
          <div class="stat-value">{{ data[s.key] ?? 0 }}</div>
          <div class="stat-label">{{ s.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表 -->
    <el-row :gutter="16">
      <el-col :md="8" :sm="24" style="margin-bottom:16px">
        <el-card shadow="never"><template #header>👥 用户角色分布</template><div ref="roleChart" class="chart" /></el-card>
      </el-col>
      <el-col :md="8" :sm="24" style="margin-bottom:16px">
        <el-card shadow="never"><template #header>📋 投递状态分布</template><div ref="statusChart" class="chart" /></el-card>
      </el-col>
      <el-col :md="8" :sm="24" style="margin-bottom:16px">
        <el-card shadow="never"><template #header>🎯 招聘批次分布（在招）</template><div ref="batchChart" class="chart" /></el-card>
      </el-col>
      <el-col :md="12" :sm="24" style="margin-bottom:16px">
        <el-card shadow="never"><template #header>📈 近 7 天注册趋势</template><div ref="regChart" class="chart" /></el-card>
      </el-col>
      <el-col :md="12" :sm="24" style="margin-bottom:16px">
        <el-card shadow="never"><template #header>🔥 热门职位 TOP5（浏览量）</template><div ref="hotChart" class="chart" /></el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { adminApi } from '../api'

const data = ref({})
const loading = ref(false)
const charts = []

const cards = [
  { key: 'totalUsers', label: '注册用户', icon: '👥' },
  { key: 'totalJobs', label: '职位总数', icon: '💼' },
  { key: 'totalApplications', label: '投递总数', icon: '📨' },
  { key: 'totalPortfolios', label: '作品总数', icon: '🎨' },
  { key: 'totalCompanies', label: '企业数量', icon: '🏢' },
  { key: 'totalResumes', label: '简历总数', icon: '📄' }
]

const roleChart = ref(null)
const statusChart = ref(null)
const batchChart = ref(null)
const regChart = ref(null)
const hotChart = ref(null)

function pie(el, rows, colors) {
  const c = echarts.init(el)
  charts.push(c)
  c.setOption({
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie', radius: ['40%', '68%'], center: ['50%', '50%'],
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { formatter: '{b}: {c}' },
      data: (rows || []).map((r, i) => ({ ...r, itemStyle: { color: colors?.[i % (colors?.length || 1)] } }))
    }]
  })
}

function initCharts(d) {
  const palette = ['#4b3fe3', '#67c23a', '#e6a23c', '#f56c6c', '#409eff', '#909399']
  pie(roleChart.value, d.userRoleDist, palette)
  pie(statusChart.value, d.applicationStatusDist, palette)
  pie(batchChart.value, d.batchDist, ['#909399', '#67c23a', '#e6a23c', '#409eff'])

  const reg = echarts.init(regChart.value)
  charts.push(reg)
  reg.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 20, bottom: 30 },
    xAxis: { type: 'category', data: (d.reg7d || []).map(r => r.name) },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{ type: 'line', smooth: true, areaStyle: { opacity: .15 }, itemStyle: { color: '#4b3fe3' }, data: (d.reg7d || []).map(r => r.value) }]
  })

  const hot = echarts.init(hotChart.value)
  charts.push(hot)
  const hotRows = [...(d.hotJobs || [])].reverse()
  hot.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 150, right: 30, top: 10, bottom: 30 },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: hotRows.map(r => r.name), axisLabel: { width: 140, overflow: 'truncate' } },
    series: [{ type: 'bar', itemStyle: { color: '#e6a23c', borderRadius: 4 }, barMaxWidth: 18, data: hotRows.map(r => r.value) }]
  })
}

async function load() {
  loading.value = true
  try {
    const res = await adminApi.overview()
    data.value = res.data || {}
    await nextTick()
    initCharts(data.value)
  } finally { loading.value = false }
}

function resize() { charts.forEach(c => c.resize()) }

onMounted(() => {
  load()
  window.addEventListener('resize', resize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resize)
  charts.forEach(c => c.dispose())
})
</script>

<style scoped>
.stat-card { text-align: center; margin-bottom: 12px; }
.stat-icon { font-size: 28px; }
.stat-value { font-size: 26px; font-weight: 700; color: #1f2337; margin: 6px 0 2px; }
.stat-label { font-size: 13px; color: #909399; }
.chart { height: 300px; }
</style>