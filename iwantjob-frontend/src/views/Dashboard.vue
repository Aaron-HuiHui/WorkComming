<template>
  <div class="dashboard">
    <!-- 刮刮乐入场(每日首次) -->
    <ScratchOverlay v-model="scratchShow" @done="onScratchDone" />

    <!-- ===== Hero ===== -->
    <section class="hero" ref="heroRef" @mousemove="onHeroMove" @mouseleave="onHeroLeave">
      <div class="hero-inner" :style="heroStyle">
        <div class="hero-pill">
          <span class="pulse-dot"></span>
          AI 引擎 · 就绪
        </div>
        <h1 class="hero-title">
          {{ greeting }},<span class="username">{{ auth.user?.username || '同学' }}</span>
        </h1>
        <p class="hero-sub">
          {{ isHrOnly ? '今天有新的人才在等你,看看候选人进展。' : '今天也要为心仪的 offer 努力,从一次模拟演练开始。' }}
        </p>
        <div class="hero-actions">
          <button class="btn-primary" @click="router.push(isHrOnly ? '/hr/jobs' : '/simulator')">
            {{ isHrOnly ? '管理职位' : '进入模拟舱' }}
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12h14M13 6l6 6-6 6"/></svg>
          </button>
          <button class="btn-ghost" @click="router.push(isHrOnly ? '/companies' : '/jobs')">
            {{ isHrOnly ? '企业主页' : '浏览职位' }}
          </button>
        </div>
      </div>
      <!-- 日期角标 -->
      <div class="hero-date">
        <span class="d-month">{{ dateStr.month }}</span>
        <span class="d-day">{{ dateStr.day }}</span>
        <span class="d-week">{{ dateStr.week }}</span>
      </div>
    </section>

    <!-- ===== 统计带 ===== -->
    <section class="stats-band">
      <div v-for="s in stats" :key="s.title" class="glass-card glass-card-hover stat-card">
        <div class="stat-top">
          <div class="stat-icon" :class="s.theme">
            <GlassIcon :name="s.icon" />
          </div>
          <span class="stat-extra">{{ s.extra }}</span>
        </div>
        <div class="stat-value">{{ s.value }}</div>
        <div class="stat-title">{{ s.title }}</div>
        <div class="stat-desc">{{ s.desc }}</div>
      </div>
    </section>

    <!-- ===== 三大创新点 ===== -->
    <section class="features">
      <div class="section-head">
        <h2 class="section-title">为大学生就业打造的<span class="text-gradient">三大创新</span></h2>
        <p class="section-sub">数据驱动 · 情境演练 · 可信履历</p>
      </div>
      <div class="feature-grid">
        <div
          v-for="f in features"
          :key="f.title"
          class="glass-card glass-card-hover feature-card"
          @click="router.push(f.path)"
        >
          <div class="feature-icon" :class="f.theme"><GlassIcon :name="f.icon" /></div>
          <h3 class="feature-title">{{ f.title }}</h3>
          <p class="feature-desc">{{ f.desc }}</p>
          <div class="feature-tag">{{ f.tag }}</div>
          <span class="feature-arrow">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12h14M13 6l6 6-6 6"/></svg>
          </span>
        </div>
      </div>
    </section>

    <!-- ===== 底部愿景 ===== -->
    <section class="vision glass-card">
      <p class="vision-quote">「让每一份努力都被看见,让每一次成长都可验证。」</p>
      <p class="vision-sub">我要工作 · 大学生就业赋能平台</p>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import GlassIcon from '../components/GlassIcon.vue'
import ScratchOverlay from '../components/ScratchOverlay.vue'
import { useAuthStore } from '../stores/auth'
import { userApi, jobApi, simulatorApi, badgeApi } from '../api'

const router = useRouter()
const auth = useAuthStore()

// ===== 刮刮乐(每日首次进入触发;刮完或跳过才写标记,中途刷新可再来) =====
const scratchShow = ref(false)
onMounted(() => {
  try {
    const today = new Date().toISOString().slice(0, 10)
    if (localStorage.getItem('iwantjob-scratch-date') !== today) {
      scratchShow.value = true
    }
  } catch (e) { /* ignore */ }
})
function onScratchDone() {
  scratchShow.value = false
  try { localStorage.setItem('iwantjob-scratch-date', new Date().toISOString().slice(0, 10)) } catch (e) { /* ignore */ }
}

// ===== 问候语 =====
const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了'
  if (h < 12) return '早上好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})
const dateStr = computed(() => {
  const d = new Date()
  return {
    month: `${d.getMonth() + 1}月`,
    day: String(d.getDate()).padStart(2, '0'),
    week: ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][d.getDay()]
  }
})

// ===== Hero 视差 =====
const heroRef = ref(null)
const tilt = ref({ x: 0, y: 0 })
let raf = 0
function onHeroMove(e) {
  const el = heroRef.value
  if (!el) return
  const rect = el.getBoundingClientRect()
  const x = ((e.clientX - rect.left) / rect.width) * 2 - 1
  const y = ((e.clientY - rect.top) / rect.height) * 2 - 1
  cancelAnimationFrame(raf)
  raf = requestAnimationFrame(() => { tilt.value = { x, y } })
}
function onHeroLeave() {
  cancelAnimationFrame(raf)
  tilt.value = { x: 0, y: 0 }
}
const heroStyle = computed(() => ({
  transform: `perspective(1200px) rotateX(${-tilt.value.y * 1.2}deg) rotateY(${tilt.value.x * 1.6}deg) translateZ(0)`,
  transition: 'transform 0.25s ease-out'
}))

// ===== 统计数据(角色化) =====
const myBadgeCount = ref(0)
const badgeTotal = ref(0)
const sessionTotal = ref(0)
const appliedTotal = ref(0)
const isHrOnly = computed(() => auth.user?.role === 2)
const hrJobTotal = ref(0)
const hrAppTotal = ref(0)
const hrViewTotal = ref(0)

const stats = computed(() => {
  if (isHrOnly.value) {
    return [
      { icon: 'briefcase', title: '在招职位', value: hrJobTotal.value, extra: '个', desc: '我发布的职位', theme: 'violet' },
      { icon: 'send', title: '收到投递', value: hrAppTotal.value, extra: '份', desc: '候选人简历', theme: 'blue' },
      { icon: 'chart', title: '累计浏览', value: hrViewTotal.value, extra: '次', desc: '职位总曝光量', theme: 'fuchsia' },
      { icon: 'zap', title: '互助积分', value: auth.points?.balance ?? 0, extra: `累计 ${auth.points?.totalEarned ?? 0}`, desc: '平台激励体系', theme: 'sky' }
    ]
  }
  return [
    { icon: 'zap', title: '互助积分', value: auth.points?.balance ?? 0, extra: `累计 ${auth.points?.totalEarned ?? 0}`, desc: '答题分享赚取,可兑换权益', theme: 'violet' },
    { icon: 'medal', title: '我的徽章', value: myBadgeCount.value, extra: `/${badgeTotal.value}`, desc: '防篡改链上可验证', theme: 'blue' },
    { icon: 'gamepad', title: '模拟演练', value: sessionTotal.value, extra: '次', desc: 'AI 软技能评估', theme: 'fuchsia' },
    { icon: 'send', title: '职位投递', value: appliedTotal.value, extra: '追踪中', desc: '实时跟踪投递状态', theme: 'sky' }
  ]
})

const features = [
  { icon: 'chart', title: '薪资白皮书', desc: '学长学姐真实 offer 脱敏数据聚合,按城市/岗位/学历生成统计报告,打破信息差', tag: '数据驱动 · 匿名贡献', path: '/salary', theme: 'violet' },
  { icon: 'rocket', title: 'AI 职业模拟舱', desc: '沉浸式剧情演练职场情境,AI 实时反馈沟通、协作等软技能并生成评估报告', tag: '情境演练 · 智能评估', path: '/simulator', theme: 'blue' },
  { icon: 'shield-check', title: '防篡改徽章', desc: '成长履历哈希锁定铸造,企业可在线查验,让简历背书真实可信', tag: '哈希锁定 · 在线查验', path: '/badges', theme: 'fuchsia' }
]

onMounted(async () => {
  if (!auth.user) auth.fetchUserInfo().catch(() => {})
  if (isHrOnly.value) {
    jobApi.myPublished({ page: 1, size: 50 }).then(res => {
      const records = res.data?.records || []
      hrJobTotal.value = res.data?.total ?? records.length
      hrAppTotal.value = records.reduce((s, j) => s + (j.applicationCount || 0), 0)
      hrViewTotal.value = records.reduce((s, j) => s + (j.viewCount || 0), 0)
    }).catch(() => {})
    return
  }
  userApi.myBadges().then(res => {
    const d = res.data
    myBadgeCount.value = Array.isArray(d) ? d.length : (d?.records?.length ?? 0)
  }).catch(() => {})
  badgeApi.templates().then(res => {
    badgeTotal.value = (res.data || []).length
  }).catch(() => {})
  simulatorApi.mySessions({ page: 1, size: 1 }).then(res => {
    sessionTotal.value = res.data?.total ?? 0
  }).catch(() => {})
  jobApi.myApplied({ page: 1, size: 1 }).then(res => {
    appliedTotal.value = res.data?.total ?? 0
  }).catch(() => {})
})
</script>

<style scoped>
.dashboard {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 40px;
  padding-bottom: 40px;
}

/* ===== Hero ===== */
.hero {
  position: relative;
  padding: 72px 8px 56px;
  border-radius: 28px;
  overflow: hidden;
}
.hero-inner { transform-style: preserve-3d; will-change: transform; }

.hero-pill {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 16px;
  border-radius: 9999px;
  background: var(--card);
  border: 1px solid var(--hairline);
  color: var(--foreground-muted);
  font-size: 0.8rem;
  letter-spacing: 0.06em;
  margin-bottom: 26px;
  backdrop-filter: blur(16px);
}
.pulse-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--success);
  box-shadow: 0 0 0 0 rgba(48, 209, 88, 0.5);
  animation: pulse 2.2s infinite;
}
@keyframes pulse {
  0% { box-shadow: 0 0 0 0 rgba(48, 209, 88, 0.45); }
  70% { box-shadow: 0 0 0 9px rgba(48, 209, 88, 0); }
  100% { box-shadow: 0 0 0 0 rgba(48, 209, 88, 0); }
}

.hero-title {
  font-size: clamp(2.4rem, 5.4vw, 4rem);
  font-weight: 750;
  letter-spacing: -0.03em;
  line-height: 1.12;
  margin: 0 0 18px;
  color: var(--foreground);
}
.username {
  background: var(--gradient-accent);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.hero-sub {
  font-size: 1.06rem;
  color: var(--foreground-muted);
  margin: 0 0 34px;
  max-width: 560px;
  line-height: 1.7;
}

.hero-actions { display: flex; gap: 14px; flex-wrap: wrap; }
.hero-actions .btn-primary svg { width: 16px; height: 16px; }

.hero-date {
  position: absolute;
  right: 32px;
  top: 40px;
  text-align: center;
  display: flex;
  flex-direction: column;
  line-height: 1;
}
.d-month { font-size: 0.8rem; color: var(--foreground-subtle); margin-bottom: 6px; letter-spacing: 0.1em; }
.d-day {
  font-size: 3.4rem;
  font-weight: 750;
  letter-spacing: -0.04em;
  color: var(--foreground);
  font-variant-numeric: tabular-nums;
}
.d-week { font-size: 0.8rem; color: var(--foreground-subtle); margin-top: 8px; }

@media (max-width: 768px) { .hero-date { display: none; } }

/* ===== 统计带 ===== */
.stats-band {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 18px;
}
@media (max-width: 1024px) { .stats-band { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 560px) { .stats-band { grid-template-columns: 1fr; } }

.stat-card { padding: 24px; }
.stat-top { display: flex; align-items: center; justify-content: space-between; margin-bottom: 18px; }
.stat-extra { font-size: 0.78rem; color: var(--foreground-subtle); }

.stat-icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  font-size: 19px;
}
.stat-icon.violet { background: rgba(162, 89, 255, 0.14); color: #a259ff; }
.stat-icon.blue { background: rgba(41, 151, 255, 0.14); color: #2997ff; }
.stat-icon.fuchsia { background: rgba(255, 92, 168, 0.14); color: #ff5ca8; }
.stat-icon.sky { background: rgba(100, 210, 255, 0.14); color: #64d2ff; }
html.light .stat-icon.violet { color: #7d4fff; }
html.light .stat-icon.blue { color: #0071e3; }
html.light .stat-icon.fuchsia { color: #ff4f9a; }
html.light .stat-icon.sky { color: #0a84c9; }

.stat-value {
  font-size: 2.3rem;
  font-weight: 750;
  letter-spacing: -0.03em;
  color: var(--foreground);
  font-variant-numeric: tabular-nums;
  line-height: 1;
}
.stat-title { font-size: 0.92rem; font-weight: 600; color: var(--foreground); margin: 10px 0 4px; }
.stat-desc { font-size: 0.8rem; color: var(--foreground-subtle); }

/* ===== 三大创新 ===== */
.section-head { text-align: center; margin-bottom: 32px; }
.section-title {
  font-size: clamp(1.6rem, 3.2vw, 2.3rem);
  font-weight: 720;
  letter-spacing: -0.02em;
  margin: 0 0 10px;
  color: var(--foreground);
}
.section-sub { color: var(--foreground-subtle); font-size: 0.92rem; margin: 0; letter-spacing: 0.18em; }

.feature-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 18px; }
@media (max-width: 1024px) { .feature-grid { grid-template-columns: 1fr; } }

.feature-card {
  padding: 30px 26px;
  cursor: pointer;
  position: relative;
}
.feature-icon {
  width: 52px;
  height: 52px;
  border-radius: 15px;
  display: grid;
  place-items: center;
  font-size: 24px;
  margin-bottom: 20px;
}
.feature-icon.violet { background: rgba(162, 89, 255, 0.14); color: #a259ff; }
.feature-icon.blue { background: rgba(41, 151, 255, 0.14); color: #2997ff; }
.feature-icon.fuchsia { background: rgba(255, 92, 168, 0.14); color: #ff5ca8; }
html.light .feature-icon.violet { color: #7d4fff; }
html.light .feature-icon.blue { color: #0071e3; }
html.light .feature-icon.fuchsia { color: #ff4f9a; }

.feature-title { font-size: 1.18rem; font-weight: 680; color: var(--foreground); margin: 0 0 10px; letter-spacing: -0.01em; }
.feature-desc {
  font-size: 0.88rem;
  color: var(--foreground-muted);
  line-height: 1.7;
  margin: 0 0 18px;
  min-height: 66px;
}
.feature-tag {
  display: inline-block;
  padding: 5px 13px;
  border-radius: 9999px;
  background: var(--primary-soft);
  color: var(--primary);
  font-size: 0.75rem;
  font-weight: 600;
  letter-spacing: 0.04em;
}
.feature-arrow {
  position: absolute;
  right: 22px;
  bottom: 20px;
  width: 18px;
  height: 18px;
  color: var(--foreground-subtle);
  opacity: 0;
  transform: translateX(-6px);
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}
.feature-card:hover .feature-arrow { opacity: 1; transform: translateX(0); color: var(--primary); }

/* ===== 愿景 ===== */
.vision { padding: 44px 36px; text-align: center; }
.vision-quote {
  font-size: clamp(1.2rem, 2.4vw, 1.55rem);
  font-weight: 620;
  letter-spacing: -0.01em;
  color: var(--foreground);
  margin: 0 0 12px;
}
.vision-sub { color: var(--foreground-subtle); font-size: 0.85rem; letter-spacing: 0.2em; margin: 0; }
</style>
