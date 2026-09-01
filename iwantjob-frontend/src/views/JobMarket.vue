<template>
  <div class="market">
    <!-- 页面标题（大字排版） -->
    <div class="page-head" v-reveal>
      <h1 class="page-title">岗位<em>市场</em></h1>
      <p class="page-sub">实时聚合在招职位与企业动态 · 数据驱动求职决策</p>
    </div>

    <!-- 总览玻璃卡 -->
    <div class="ov-grid">
      <section class="glass glass-hover ov-card" v-for="(c, i) in overviewCards" :key="c.label" v-reveal="80 + i * 80">
        <span class="glass-icon" :class="'gi-' + c.theme">
          <GlassIcon :name="c.icon" :size="24" />
        </span>
        <div class="ov-body">
          <div class="ov-value">{{ c.value }}<span class="ov-unit" v-if="c.unit">{{ c.unit }}</span></div>
          <div class="ov-label">{{ c.label }}</div>
        </div>
      </section>
    </div>

    <!-- 图表 2x2 -->
    <div class="chart-grid">
      <section class="glass chart-card" v-reveal="60">
        <header class="chart-head">
          <h3>职位类型分布</h3>
          <span>实习 / 校招 / 社招占比</span>
        </header>
        <div ref="typeChartRef" class="chart"></div>
      </section>

      <section class="glass chart-card" v-reveal="140">
        <header class="chart-head">
          <h3>城市分布</h3>
          <span>在招职位地域热力</span>
        </header>
        <div ref="cityChartRef" class="chart"></div>
      </section>

      <section class="glass chart-card" v-reveal="220">
        <header class="chart-head">
          <h3>薪资段分布</h3>
          <span>按月薪下限归类</span>
        </header>
        <div ref="salaryChartRef" class="chart"></div>
      </section>

      <section class="glass chart-card" v-reveal="300">
        <header class="chart-head">
          <h3>热门职位 TOP</h3>
          <span>按浏览量排序</span>
        </header>
        <div ref="hotChartRef" class="chart"></div>
      </section>
    </div>

    <!-- 高薪职位速览 -->
    <section class="glass table-card" v-reveal="80">
      <header class="table-head">
        <div class="th-left">
          <span class="glass-icon sm gi-amber"><GlassIcon name="flame" :size="18" /></span>
          <h3>高薪资职位速览</h3>
        </div>
        <button class="th-link" @click="$router.push('/jobs')">
          查看全部职位
          <GlassIcon name="arrow-right" :size="14" />
        </button>
      </header>
      <el-table :data="topSalaryJobs" stripe>
        <el-table-column prop="title" label="职位" min-width="160" />
        <el-table-column prop="companyName" label="公司" width="130" />
        <el-table-column label="类型" width="86">
          <template #default="{ row }">
            <el-tag :type="['info', 'success', 'warning'][row.jobType]" size="small">{{ ['实习', '校招', '社招'][row.jobType] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="城市" width="90" />
        <el-table-column prop="salaryRange" label="薪资" width="130">
          <template #default="{ row }">
            <span class="salary-text">{{ row.salaryRange }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90">
          <template #default="{ row }">
            <el-button size="small" type="primary" text @click="viewDetail(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { jobApi } from '../api'
import { useRouter } from 'vue-router'
import GlassIcon from '../components/GlassIcon.vue'

const router = useRouter()
const stats = ref({})
const topSalaryJobs = ref([])

const typeChartRef = ref(null)
const cityChartRef = ref(null)
const salaryChartRef = ref(null)
const hotChartRef = ref(null)

const charts = []
const hotTop = computed(() => stats.value.hotJobs?.[0]?.value ?? 0)

/* ================= 字体与色彩体系（本次重构核心） ================= */
// 统一中文无衬线字体栈：苹方 / 鸿蒙黑 / 微软雅黑，观感一致、渲染清晰
const FONT =
  "'PingFang SC', 'HarmonyOS Sans SC', 'Microsoft YaHei UI', 'Microsoft YaHei', 'Noto Sans SC', system-ui, sans-serif"
const TEXT_PRIMARY = 'rgba(255, 255, 255, 0.92)'
const TEXT_SECONDARY = 'rgba(255, 255, 255, 0.62)'
const TEXT_MUTED = 'rgba(255, 255, 255, 0.4)'
const AXIS_LINE = 'rgba(255, 255, 255, 0.14)'
// 极光色板（与全站 --g-* 对齐）
const PALETTE = ['#8b5cf6', '#60a5fa', '#38bdf8', '#e879f9', '#34d399', '#fbbf24', '#f472b6']

const TOOLTIP = {
  backgroundColor: 'rgba(13, 16, 42, 0.92)',
  borderColor: 'rgba(255, 255, 255, 0.14)',
  borderWidth: 1,
  padding: [8, 12],
  textStyle: { color: TEXT_PRIMARY, fontSize: 12, fontFamily: FONT },
  extraCssText: 'backdrop-filter: blur(12px); border-radius: 10px; box-shadow: 0 8px 28px rgba(2,6,32,.5);'
}

const overviewCards = computed(() => [
  { icon: 'briefcase', theme: 'violet', value: stats.value.totalJobs ?? 0, label: '在招职位', unit: '个' },
  { icon: 'building', theme: 'blue', value: stats.value.totalCompanies ?? 0, label: '参与企业', unit: '家' },
  { icon: 'map-pin', theme: 'fuchsia', value: stats.value.cityDist?.length ?? 0, label: '覆盖城市', unit: '座' },
  { icon: 'flame', theme: 'amber', value: hotTop.value, label: '最高职位浏览量', unit: '次' }
])

function initChart(el, option) {
  const chart = echarts.init(el)
  chart.setOption(option)
  charts.push(chart)
  return chart
}

onMounted(async () => {
  try {
    const res = await jobApi.stats()
    stats.value = res.data || {}
    await nextTick()
    renderCharts()
  } catch (e) {
    ElMessage.error('统计加载失败')
  }
  try {
    const res = await jobApi.search({ page: 1, size: 5 })
    const list = res.data?.records || []
    topSalaryJobs.value = [...list].sort((a, b) => {
      const pa = parseFloat(a.salaryRange) || 0
      const pb = parseFloat(b.salaryRange) || 0
      return pb - pa
    }).slice(0, 5)
  } catch (e) { /* 静默 */ }

  window.addEventListener('resize', handleResize)
})

function renderCharts() {
  const d = stats.value

  /* ---------- 职位类型分布：环形图 ---------- */
  if (typeChartRef.value) {
    const data = (d.typeDist || []).filter(x => x.value > 0)
    initChart(typeChartRef.value, {
      tooltip: { ...TOOLTIP, trigger: 'item', formatter: '{b}<br/>数量：<b>{c}</b>（{d}%）' },
      legend: {
        bottom: 4,
        icon: 'circle',
        itemWidth: 9,
        itemHeight: 9,
        itemGap: 18,
        textStyle: { color: TEXT_SECONDARY, fontSize: 12, fontFamily: FONT }
      },
      color: PALETTE,
      series: [{
        type: 'pie',
        radius: ['50%', '72%'],
        center: ['50%', '44%'],
        itemStyle: {
          borderRadius: 7,
          borderColor: 'rgba(10, 14, 35, 0.9)',
          borderWidth: 3
        },
        label: { show: false },
        emphasis: {
          scaleSize: 6,
          label: {
            show: true,
            fontSize: 13,
            fontWeight: 700,
            fontFamily: FONT,
            color: TEXT_PRIMARY,
            formatter: '{b}\n{d}%'
          }
        },
        data
      }]
    })
  }

  /* ---------- 城市分布：环形图 + 精排标签（重点重构） ---------- */
  if (cityChartRef.value) {
    const data = (d.cityDist || []).filter(x => x.value > 0)
    initChart(cityChartRef.value, {
      tooltip: { ...TOOLTIP, trigger: 'item', formatter: '{b}<br/>在招职位：<b>{c}</b> 个（{d}%）' },
      color: PALETTE,
      series: [{
        type: 'pie',
        radius: ['46%', '68%'],
        center: ['50%', '47%'],
        itemStyle: {
          borderRadius: 7,
          borderColor: 'rgba(10, 14, 35, 0.9)',
          borderWidth: 3
        },
        label: {
          // 城市名（600 字重白色）+ 数量（次级灰白）两行精排，彻底替换原「北京: 15」拥挤样式
          formatter: '{name|{b}}\n{num|{c} 个}',
          rich: {
            name: { fontSize: 13, fontWeight: 600, color: TEXT_PRIMARY, fontFamily: FONT, lineHeight: 19, align: 'center' },
            num: { fontSize: 11, color: TEXT_MUTED, fontFamily: FONT, lineHeight: 15, align: 'center' }
          }
        },
        labelLine: {
          length: 12,
          length2: 8,
          lineStyle: { color: 'rgba(255, 255, 255, 0.22)' }
        },
        emphasis: { scaleSize: 6 },
        data
      }]
    })
  }

  /* ---------- 薪资段分布：渐变柱状 ---------- */
  if (salaryChartRef.value) {
    const sd = d.salaryDist || []
    initChart(salaryChartRef.value, {
      tooltip: { ...TOOLTIP, trigger: 'axis', axisPointer: { type: 'shadow', shadowStyle: { color: 'rgba(255,255,255,0.05)' } } },
      grid: { left: 42, right: 16, top: 26, bottom: 32 },
      xAxis: {
        type: 'category',
        data: sd.map(x => x.name),
        axisLine: { lineStyle: { color: AXIS_LINE } },
        axisTick: { show: false },
        axisLabel: { color: TEXT_SECONDARY, fontSize: 11.5, fontFamily: FONT, interval: 0 }
      },
      yAxis: {
        type: 'value',
        minInterval: 1,
        axisLabel: { color: TEXT_MUTED, fontSize: 11, fontFamily: FONT },
        splitLine: { lineStyle: { color: 'rgba(255,255,255,0.07)' } }
      },
      series: [{
        type: 'bar',
        barWidth: 30,
        itemStyle: {
          borderRadius: [7, 7, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#a78bfa' },
            { offset: 1, color: 'rgba(124, 58, 237, 0.45)' }
          ])
        },
        label: {
          show: true,
          position: 'top',
          color: TEXT_SECONDARY,
          fontSize: 11,
          fontFamily: FONT
        },
        data: sd.map(x => x.value)
      }]
    })
  }

  /* ---------- 热门职位 TOP：横向条形 ---------- */
  if (hotChartRef.value) {
    const hj = (d.hotJobs || []).slice(0, 8).reverse()
    initChart(hotChartRef.value, {
      tooltip: { ...TOOLTIP, trigger: 'axis', axisPointer: { type: 'shadow', shadowStyle: { color: 'rgba(255,255,255,0.05)' } } },
      grid: { left: 8, right: 46, top: 8, bottom: 8, containLabel: true },
      xAxis: {
        type: 'value',
        axisLabel: { color: TEXT_MUTED, fontSize: 10.5, fontFamily: FONT },
        splitLine: { lineStyle: { color: 'rgba(255,255,255,0.06)' } }
      },
      yAxis: {
        type: 'category',
        data: hj.map(x => x.name),
        axisLine: { show: false },
        axisTick: { show: false },
        axisLabel: {
          width: 150,
          overflow: 'truncate',
          color: TEXT_SECONDARY,
          fontSize: 12,
          fontFamily: FONT
        }
      },
      series: [{
        type: 'bar',
        barWidth: 12,
        itemStyle: {
          borderRadius: [0, 6, 6, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: 'rgba(96, 165, 250, 0.55)' },
            { offset: 1, color: '#60a5fa' }
          ])
        },
        label: {
          show: true,
          position: 'right',
          color: TEXT_PRIMARY,
          fontSize: 11.5,
          fontWeight: 600,
          fontFamily: FONT
        },
        data: hj.map(x => x.value)
      }]
    })
  }
}

function viewDetail(row) {
  router.push('/jobs')
}

function handleResize() {
  charts.forEach(c => c.resize())
}

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  charts.forEach(c => c.dispose())
})
</script>

<style scoped>
.market {
  max-width: 1440px;
  margin: 0 auto;
  padding: 4px 2px 40px;
}

/* ---------- 页面标题 ---------- */
.page-head { margin: 10px 2px 22px; }
.page-title {
  margin: 0;
  font-size: 34px;
  font-weight: 800;
  letter-spacing: -0.8px;
  color: var(--g-text-primary);
}
.page-title em {
  font-style: normal;
  background: linear-gradient(120deg, var(--g-violet), var(--g-blue));
  background-size: 200% auto;
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  animation: g-shine 4s linear infinite;
}
.page-sub {
  margin: 8px 0 0;
  font-size: 13px;
  letter-spacing: 0.4px;
  color: var(--g-text-muted);
}

/* ---------- 总览玻璃卡 ---------- */
.ov-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}
.ov-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 22px 22px;
}
.ov-value {
  font-size: 30px;
  font-weight: 800;
  line-height: 1.1;
  color: var(--g-text-primary);
  font-variant-numeric: tabular-nums;
  letter-spacing: -0.5px;
}
.ov-unit {
  font-size: 12px;
  font-weight: 500;
  color: var(--g-text-muted);
  margin-left: 5px;
}
.ov-label {
  font-size: 13px;
  font-weight: 500;
  color: var(--g-text-secondary);
  margin-top: 5px;
  letter-spacing: 0.3px;
}

/* ---------- 图表卡 ---------- */
.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  margin-top: 16px;
}
.chart-card { padding: 22px 22px 14px; }
.chart-head { margin-bottom: 6px; }
.chart-head h3 {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  letter-spacing: 0.2px;
  color: var(--g-text-primary);
}
.chart-head span {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: var(--g-text-muted);
  letter-spacing: 0.3px;
}
.chart { width: 100%; height: 296px; }

/* ---------- 表格卡 ---------- */
.table-card {
  margin-top: 16px;
  padding: 20px 22px 12px;
}
.table-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}
.th-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.th-left h3 {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  color: var(--g-text-primary);
}
.th-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border: none;
  background: transparent;
  color: var(--g-accent);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: color 0.25s ease;
}
.th-link:hover { color: var(--g-accent-2); }
.th-link svg { transition: transform 0.25s ease; }
.th-link:hover svg { transform: translateX(3px); }
.salary-text {
  color: #fbbf24;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
}

@keyframes g-shine { to { background-position: 200% center; } }

/* ---------- 响应式 ---------- */
@media (max-width: 992px) {
  .ov-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .chart-grid { grid-template-columns: 1fr; }
  .page-title { font-size: 26px; }
}
@media (max-width: 560px) {
  .ov-grid { grid-template-columns: 1fr; }
}
</style>
