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

    <!-- 刷新工具条 -->
    <div class="toolbar">
      <span class="updated-at" v-if="updatedAt">数据更新于 {{ updatedAt }}</span>
      <el-button size="small" :icon="Refresh" :loading="loading" @click="load">刷新</el-button>
    </div>

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
import { Refresh } from '@element-plus/icons-vue'
import { adminApi } from '../api'
import { chartPalette, onChartThemeRebuild } from '../composables/useChartTheme'

const data = ref({})
const loading = ref(false)
const charts = []
const updatedAt = ref('')

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
  const P = chartPalette()
  c.setOption({
    tooltip: { trigger: 'item', backgroundColor: P.tooltipBg, borderColor: P.tooltipBorder, textStyle: { color: P.tooltipText } },
    series: [{
      type: 'pie', radius: ['40%', '68%'], center: ['50%', '50%'],
      itemStyle: { borderRadius: 6, borderColor: P.itemBorder, borderWidth: 2 },
      label: { formatter: '{b}: {c}', color: P.textSecondary },
      data: (rows || []).map((r, i) => ({ ...r, itemStyle: { color: colors?.[i % (colors?.length || 1)] } }))
    }]
  })
}

// 近 7 天注册趋势：后端只返回有数据的日期，前端补全 7 天（缺数补 0），避免趋势误读
function fillReg7d(rows) {
  const map = new Map((rows || []).map(r => [r.name, r.value]))
  const days = []
  const today = new Date()
  for (let i = 6; i >= 0; i--) {
    const d = new Date(today)
    d.setDate(today.getDate() - i)
    const key = `${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
    days.push({ name: key, value: map.get(key) ?? 0 })
  }
  return days
}

function initCharts(d) {
  const palette = ['#4b3fe3', '#67c23a', '#e6a23c', '#f56c6c', '#409eff', '#909399']
  pie(roleChart.value, d.userRoleDist, palette)
  pie(statusChart.value, d.applicationStatusDist, palette)
  pie(batchChart.value, d.batchDist, ['#909399', '#67c23a', '#e6a23c', '#409eff'])

  const regRows = fillReg7d(d.reg7d)
  const reg = echarts.init(regChart.value)
  charts.push(reg)
  const P2 = chartPalette()
  reg.setOption({
    tooltip: { trigger: 'axis', backgroundColor: P2.tooltipBg, borderColor: P2.tooltipBorder, textStyle: { color: P2.tooltipText } },
    grid: { left: 40, right: 20, top: 20, bottom: 30 },
    xAxis: { type: 'category', data: regRows.map(r => r.name), axisLabel: { color: P2.textSecondary }, axisLine: { lineStyle: { color: P2.axisLine } } },
    yAxis: { type: 'value', minInterval: 1, axisLabel: { color: P2.textSecondary }, splitLine: { lineStyle: { color: P2.splitLine } } },
    series: [{ type: 'line', smooth: true, areaStyle: { opacity: .15 }, itemStyle: { color: '#4b3fe3' }, data: regRows.map(r => r.value) }]
  })

  const hot = echarts.init(hotChart.value)
  charts.push(hot)
  const hotRows = [...(d.hotJobs || [])].reverse()
  const P3 = chartPalette()
  hot.setOption({
    tooltip: { trigger: 'axis', backgroundColor: P3.tooltipBg, borderColor: P3.tooltipBorder, textStyle: { color: P3.tooltipText } },
    grid: { left: 150, right: 30, top: 10, bottom: 30 },
    xAxis: { type: 'value', axisLabel: { color: P3.textSecondary }, splitLine: { lineStyle: { color: P3.splitLine } } },
    yAxis: { type: 'category', data: hotRows.map(r => r.name), axisLabel: { width: 140, overflow: 'truncate', color: P3.textSecondary }, axisLine: { lineStyle: { color: P3.axisLine } } },
    series: [{ type: 'bar', itemStyle: { color: '#e6a23c', borderRadius: 4 }, barMaxWidth: 18, data: hotRows.map(r => r.value) }]
  })
}

onChartThemeRebuild(() => {
  if (data.value && Object.keys(data.value).length) {
    charts.forEach(ch => ch.dispose())
    charts.length = 0
    initCharts(data.value)
  }
})

async function load() {
  loading.value = true
  try {
    const res = await adminApi.overview()
    data.value = res.data || {}
    updatedAt.value = new Date().toLocaleTimeString('zh-CN', { hour12: false })
    charts.forEach(c => c.dispose())
    charts.length = 0
    await nextTick()
    initCharts(data.value)
  } catch (e) { /* 静默 */ } finally { loading.value = false }
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
.toolbar { display: flex; justify-content: flex-end; align-items: center; gap: 12px; margin-bottom: 12px; }
.updated-at { font-size: 12px; color: var(--foreground-subtle); }
.stat-card { text-align: center; margin-bottom: 12px; }
.stat-icon { font-size: 28px; }
.stat-value { font-size: 26px; font-weight: 700; color: var(--foreground); margin: 6px 0 2px; }
.stat-label { font-size: 13px; color: var(--foreground-muted); }
.chart { height: 300px; }
</style>