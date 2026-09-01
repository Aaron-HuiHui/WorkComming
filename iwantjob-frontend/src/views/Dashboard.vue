<template>
  <div class="dashboard">
    <!-- ========== Hero 工作室卡：超大排版 + 不对称底栏 + 3D 堆叠视差 ========== -->
    <section
      ref="heroRef"
      class="hero nbg"
      @mousemove="onHeroMove"
      @mouseleave="onHeroLeave"
    >
      <div class="hero-glow hero-glow-a"></div>
      <div class="hero-glow hero-glow-b"></div>

      <span class="glass-pill ai-pill">
        <span class="ai-dot"></span> AI 状态 · 就绪
      </span>

      <div class="hero-center">
        <p class="hero-kicker" v-reveal>AI-Powered Career Growth</p>
        <h1 class="hero-title" v-reveal="90">
          <span class="hero-line">我要工作</span>
          <span class="hero-line"><em class="flow">让求职之路流动起来</em></span>
        </h1>
      </div>

      <!-- 3D 堆叠玻璃片：随鼠标微倾视差，不遮挡文字 -->
      <StudioStack :tilt="tilt" class="hero-stack" />

      <!-- 不对称底栏：左行动区 + 右个人简介卡 -->
      <div class="hero-bottom">
        <div class="hero-info" v-reveal="190">
          <p class="hero-sub">
            你好，{{ auth.user?.username || '同学' }} —— 打破信息差，让每一步努力都被看见
          </p>
          <div class="hero-actions">
            <button class="btn-primary" @click="$router.push('/simulator')">
              进入 AI 模拟舱
              <GlassIcon name="arrow-right" :size="15" class="btn-arrow" />
            </button>
            <button class="btn-ghost" @click="$router.push('/jobs')">浏览职位广场</button>
          </div>
        </div>
        <div class="hero-me" v-reveal="300">
          <span class="hero-me-avatar">{{ (auth.user?.username || 'U')[0].toUpperCase() }}</span>
          <div class="hero-me-body">
            <div class="hero-me-name">
              {{ auth.user?.username || '同学' }}
              <span class="hero-me-role">{{ auth.roleName || '学生' }}</span>
            </div>
            <div class="hero-me-meta">
              <template v-if="isHrOnly">
                <span>在招职位 {{ hrJobTotal }}</span>
                <span class="dot-sep"></span>
                <span>收到投递 {{ hrAppTotal }}</span>
              </template>
              <template v-else>
                <span>互助积分 {{ auth.points?.balance ?? 0 }}</span>
                <span class="dot-sep"></span>
                <span>徽章 {{ myBadgeCount }}/{{ badgeTotal }}</span>
              </template>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ========== 数据带：一行 4 张近黑玻璃卡 ========== -->
    <div class="sec">
      <div class="sec-head" v-reveal>
        <h2 class="sec-title">成长数据</h2>
        <span class="sec-label">Growth Metrics</span>
      </div>
      <div class="stats-grid">
        <section
          class="nbg nbg-hover stat-card"
          v-for="(s, i) in stats"
          :key="s.title"
          v-reveal="100 + i * 90"
        >
          <span class="glass-icon" :class="'gi-' + s.theme">
            <GlassIcon :name="s.icon" :size="24" />
          </span>
          <div class="stat-body">
            <div class="stat-value">
              {{ s.value }}<span class="stat-extra" v-if="s.extra">{{ s.extra }}</span>
            </div>
            <div class="stat-title">{{ s.title }}</div>
            <div class="stat-desc">{{ s.desc }}</div>
          </div>
        </section>
      </div>
    </div>

    <!-- ========== 三大核心能力：精选卡模式 ========== -->
    <div class="sec">
      <div class="sec-head" v-reveal>
        <h2 class="sec-title">三大核心能力</h2>
        <span class="sec-label">Core Capabilities</span>
      </div>
      <div class="feature-grid">
        <section
          class="nbg nbg-hover feature-card"
          :class="'fc-' + f.theme"
          v-for="(f, i) in features"
          :key="f.title"
          v-reveal="i * 110"
          @click="$router.push(f.path)"
        >
          <div class="feature-top">
            <span class="feature-tag">#0{{ i + 1 }} / {{ f.tag }}</span>
            <span class="glass-icon" :class="'gi-' + f.theme">
              <GlassIcon :name="f.icon" :size="24" />
            </span>
          </div>
          <h3>{{ f.title }}</h3>
          <p>{{ f.desc }}</p>
          <span class="feature-link">
            进入
            <GlassIcon name="arrow-right" :size="14" class="fl-arrow" />
          </span>
        </section>
      </div>
    </div>

    <!-- ========== 底部两卡：本周提示 + 愿景 ========== -->
    <div class="bottom-grid">
      <section class="nbg nbg-hover tip-card" v-reveal>
        <span class="glass-icon gi-amber"><GlassIcon name="sparkles" :size="24" /></span>
        <div class="tip-body">
          <h3>本周提示</h3>
          <p>完成一次 AI 模拟演练，或为薪资白皮书贡献一条真实 offer 数据，即可赚取互助积分与成长徽章。</p>
        </div>
        <span class="glass-pill tip-pill">每日成长</span>
      </section>

      <section class="nbg quote-card" v-reveal="120">
        <div class="quote-glow"></div>
        <GlassIcon name="sparkles" :size="20" class="quote-mark" />
        <p class="quote-text">打破<em>信息差</em></p>
        <span class="quote-sub">「我要工作」· 与你同行</span>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import GlassIcon from '../components/GlassIcon.vue'
import StudioStack from '../components/StudioStack.vue'
import { useAuthStore } from '../stores/auth'
import { userApi, jobApi, simulatorApi, badgeApi } from '../api'

const auth = useAuthStore()
const myBadgeCount = ref(0)
const badgeTotal = ref(0)
const sessionTotal = ref(0)
const appliedTotal = ref(0)
// HR 角色化数据
const isHrOnly = computed(() => auth.user?.role === 2)
const hrJobTotal = ref(0)
const hrAppTotal = ref(0)
const hrViewTotal = ref(0)

/* ---------- Hero 鼠标视差（相机随鼠标微动） ---------- */
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
onBeforeUnmount(() => cancelAnimationFrame(raf))

const stats = computed(() => {
  // HR 角色化数据卡（招聘方视角）
  if (isHrOnly.value) {
    return [
      { icon: 'briefcase', title: '在招职位', value: hrJobTotal.value, extra: '个', desc: '我发布的职位', theme: 'violet' },
      { icon: 'send', title: '收到投递', value: hrAppTotal.value, extra: '份', desc: '候选人简历', theme: 'blue' },
      { icon: 'chart', title: '累计浏览', value: hrViewTotal.value, extra: '次', desc: '职位总曝光量', theme: 'fuchsia' },
      { icon: 'zap', title: '互助积分', value: auth.points?.balance ?? 0, extra: `累计 ${auth.points?.totalEarned ?? 0}`, desc: '平台激励体系', theme: 'sky' }
    ]
  }
  return [
    {
      icon: 'zap',
      title: '互助积分',
      value: auth.points?.balance ?? 0,
      extra: `累计 ${auth.points?.totalEarned ?? 0}`,
      desc: '答题分享赚取，可兑换权益',
      theme: 'violet'
    },
    {
      icon: 'medal',
      title: '我的徽章',
      value: myBadgeCount.value,
      extra: `/${badgeTotal.value}`,
      desc: '防篡改链上可验证',
      theme: 'blue'
    },
    {
      icon: 'gamepad',
      title: '模拟演练',
      value: sessionTotal.value,
      extra: '次',
      desc: 'AI 软技能评估',
      theme: 'fuchsia'
    },
    {
      icon: 'send',
      title: '职位投递',
      value: appliedTotal.value,
      extra: '追踪中',
      desc: '实时跟踪投递状态',
      theme: 'sky'
    }
  ]
})

const features = [
  {
    icon: 'chart',
    title: '薪资白皮书',
    desc: '学长学姐真实 offer 脱敏数据聚合，按城市/岗位/学历生成统计报告，打破信息差',
    tag: '数据驱动 · 匿名贡献',
    path: '/salary',
    theme: 'violet'
  },
  {
    icon: 'rocket',
    title: 'AI 职业模拟舱',
    desc: '沉浸式剧情演练职场情境，AI 实时反馈沟通、协作等软技能并生成评估报告',
    tag: '情境演练 · 智能评估',
    path: '/simulator',
    theme: 'blue'
  },
  {
    icon: 'shield-check',
    title: '防篡改徽章',
    desc: '成长履历哈希锁定铸造，企业可在线查验，让简历背书真实可信',
    tag: '哈希锁定 · 在线查验',
    path: '/badges',
    theme: 'fuchsia'
  }
]

onMounted(async () => {
  if (isHrOnly.value) {
    // HR 视角：招聘方数据（不再调用学生侧接口）
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
  max-width: 1440px;
  margin: 0 auto;
  padding: 4px 2px 40px;
  perspective: 1200px;
}

/* ---------- 近黑玻璃基类（暗黑工作室卡） ---------- */
.nbg {
  position: relative;
  background: rgba(8, 10, 26, 0.62);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 22px;
  backdrop-filter: blur(22px) saturate(1.4);
  -webkit-backdrop-filter: blur(22px) saturate(1.4);
  box-shadow: 0 24px 70px rgba(2, 6, 32, 0.55), inset 0 1px 0 rgba(255, 255, 255, 0.16);
  overflow: hidden;
}
.nbg-hover {
  transition: transform 0.3s ease, box-shadow 0.3s ease, border-color 0.3s ease, background 0.3s ease;
}
.nbg-hover:hover {
  transform: translateY(-5px);
  border-color: rgba(255, 255, 255, 0.24);
  background: rgba(10, 13, 32, 0.7);
  box-shadow: 0 34px 86px rgba(2, 6, 32, 0.68), inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

/* ---------- Hero 工作室大卡 ---------- */
.hero {
  position: relative;
  display: flex;
  flex-direction: column;
  min-height: 460px;
  padding: 46px 48px 38px;
  border-radius: 28px;
  box-shadow: 0 24px 80px rgba(2, 6, 32, 0.6), inset 0 1px 0 rgba(255, 255, 255, 0.16);
}
.hero-glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  pointer-events: none;
}
.hero-glow-a {
  width: 360px; height: 360px; top: -150px; left: -100px;
  background: radial-gradient(circle, rgba(139, 92, 246, 0.28), transparent 70%);
}
.hero-glow-b {
  width: 320px; height: 320px; bottom: -160px; right: -80px;
  background: radial-gradient(circle, rgba(56, 189, 248, 0.22), transparent 70%);
}

/* AI 状态药丸（右上角） */
.ai-pill {
  position: absolute;
  top: 24px; right: 24px;
  z-index: 3;
}
.ai-dot {
  width: 7px; height: 7px;
  border-radius: 50%;
  background: var(--g-emerald);
  box-shadow: 0 0 8px var(--g-emerald);
  animation: g-pulse 2s ease-in-out infinite;
}

/* 超大排版：英文小标签 + 两行大标题 */
.hero-center {
  position: relative;
  z-index: 2;
  padding: 34px 0 6px;
  text-align: center;
}
.hero-kicker {
  margin: 0 0 16px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 4px;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.4);
}
.hero-title {
  margin: 0;
  font-size: 52px;
  font-weight: 800;
  letter-spacing: -1.5px;
  line-height: 1.16;
  color: var(--g-text-primary);
  text-shadow: 0 6px 40px rgba(2, 6, 32, 0.55);
}
.hero-line { display: block; }
.hero-title em.flow {
  font-style: normal;
  background: linear-gradient(120deg, var(--g-violet), var(--g-blue), var(--g-fuchsia));
  background-size: 200% auto;
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  animation: g-shine 4s linear infinite;
}

/* 3D 堆叠片：居右悬浮，位于文字之后不遮挡阅读 */
.hero-stack {
  position: absolute;
  right: 4%;
  top: 47%;
  transform: translateY(-52%);
  z-index: 1;
}

/* 不对称底栏：左行动区 + 右个人卡 */
.hero-bottom {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
  margin-top: auto;
  padding-top: 34px;
}
.hero-info { max-width: 540px; }
.hero-sub {
  margin: 0 0 18px;
  font-size: 14px;
  line-height: 1.8;
  color: var(--g-text-secondary);
}
.hero-actions { display: flex; flex-wrap: wrap; gap: 12px; }

/* 主按钮：白底深字 + 深紫影 + 箭头微动 */
.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  border: none;
  border-radius: 14px;
  padding: 13px 26px;
  font-size: 14px;
  font-weight: 700;
  color: #33185e;
  background: linear-gradient(135deg, #ffffff, #e8eaff);
  cursor: pointer;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
  box-shadow: 0 12px 32px rgba(139, 92, 246, 0.42);
}
.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 16px 42px rgba(139, 92, 246, 0.55);
}
.btn-primary .btn-arrow { transition: transform 0.25s ease; }
.btn-primary:hover .btn-arrow { transform: translateX(4px); }

/* 玻璃描边按钮：hover 背景加深 */
.btn-ghost {
  display: inline-flex;
  align-items: center;
  border: 1px solid rgba(255, 255, 255, 0.22);
  border-radius: 14px;
  padding: 13px 26px;
  font-size: 14px;
  font-weight: 500;
  color: var(--g-text-primary);
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  cursor: pointer;
  transition: background 0.25s ease, transform 0.25s ease, border-color 0.25s ease;
}
.btn-ghost:hover {
  background: rgba(255, 255, 255, 0.14);
  border-color: rgba(255, 255, 255, 0.34);
  transform: translateY(-2px);
}

/* 右下个人简介小卡（近黑玻璃） */
.hero-me {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 20px 14px 14px;
  border-radius: 18px;
  border: 1px solid rgba(255, 255, 255, 0.14);
  background: rgba(6, 8, 20, 0.55);
  backdrop-filter: blur(16px) saturate(1.4);
  -webkit-backdrop-filter: blur(16px) saturate(1.4);
  box-shadow: 0 16px 40px rgba(2, 6, 32, 0.5), inset 0 1px 0 rgba(255, 255, 255, 0.12);
}
.hero-me-avatar {
  width: 44px; height: 44px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(135deg, var(--g-violet), var(--g-blue));
  flex-shrink: 0;
}
.hero-me-name {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 700;
  color: var(--g-text-primary);
}
.hero-me-role {
  padding: 2px 9px;
  border-radius: 999px;
  border: 1px solid rgba(139, 92, 246, 0.4);
  background: rgba(139, 92, 246, 0.16);
  font-size: 11px;
  font-weight: 500;
  letter-spacing: 1px;
  color: #c4b5fd;
}
.hero-me-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
  font-size: 12px;
  color: var(--g-text-muted);
}
.dot-sep {
  width: 3px; height: 3px;
  border-radius: 50%;
  background: var(--g-text-muted);
}

/* ---------- Section 头：中文大标题 + 英文小标签 ---------- */
.sec { margin-top: 40px; }
.sec-head {
  display: flex;
  align-items: baseline;
  gap: 14px;
  margin-bottom: 18px;
}
.sec-title {
  margin: 0;
  font-size: 26px;
  font-weight: 800;
  letter-spacing: -0.5px;
  color: var(--g-text-primary);
}
.sec-label {
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 3px;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.4);
}

/* ---------- 数据带 ---------- */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}
.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 22px 20px;
}
.stat-body { min-width: 0; }
.stat-value {
  font-size: 30px;
  font-weight: 800;
  line-height: 1.1;
  letter-spacing: -0.5px;
  color: var(--g-text-primary);
  font-variant-numeric: tabular-nums;
}
.stat-extra {
  font-size: 12px;
  font-weight: 500;
  letter-spacing: 0;
  color: var(--g-text-muted);
  margin-left: 6px;
}
.stat-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--g-text-secondary);
  margin-top: 5px;
}
.stat-desc {
  font-size: 12px;
  color: var(--g-text-muted);
  margin-top: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ---------- 三大核心能力：精选卡 ---------- */
.feature-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}
.feature-card {
  display: flex;
  flex-direction: column;
  padding: 26px 24px 22px;
  cursor: pointer;
}
/* 顶部 3px 主题渐变条 */
.feature-card::before {
  content: '';
  position: absolute;
  inset: 0 0 auto 0;
  height: 3px;
  opacity: 0.8;
  transition: opacity 0.3s ease, height 0.3s ease;
}
.feature-card.fc-violet::before  { background: linear-gradient(90deg, var(--g-violet), transparent 78%); }
.feature-card.fc-blue::before    { background: linear-gradient(90deg, var(--g-blue), transparent 78%); }
.feature-card.fc-fuchsia::before { background: linear-gradient(90deg, var(--g-fuchsia), transparent 78%); }
.feature-card:hover::before { height: 4px; opacity: 1; }

.feature-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 20px;
}
.feature-tag {
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 2px;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.42);
}
.feature-card:hover .glass-icon { transform: scale(1.08) rotate(-4deg); }
.feature-card h3 {
  margin: 0 0 10px;
  font-size: 20px;
  font-weight: 800;
  letter-spacing: -0.3px;
  color: var(--g-text-primary);
}
.feature-card p {
  margin: 0;
  font-size: 13px;
  line-height: 1.75;
  color: var(--g-text-secondary);
  min-height: 68px;
}
/* 底部描边按钮式链接 */
.feature-link {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  margin-top: 20px;
  padding: 9px 18px;
  width: fit-content;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  font-size: 13px;
  font-weight: 600;
  color: var(--g-text-primary);
  transition: background 0.3s ease, border-color 0.3s ease;
}
.feature-link .fl-arrow { transition: transform 0.3s ease; }
.feature-card:hover .feature-link {
  background: rgba(255, 255, 255, 0.12);
  border-color: rgba(255, 255, 255, 0.36);
}
.feature-card:hover .feature-link .fl-arrow { transform: translateX(3px); }
.feature-card.fc-violet:hover .fl-arrow  { color: var(--g-violet); }
.feature-card.fc-blue:hover .fl-arrow    { color: var(--g-blue); }
.feature-card.fc-fuchsia:hover .fl-arrow { color: var(--g-fuchsia); }

/* ---------- 底部两卡 ---------- */
.bottom-grid {
  display: grid;
  grid-template-columns: 7fr 5fr;
  gap: 16px;
  margin-top: 40px;
}
.tip-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px 26px;
}
.tip-body { min-width: 0; }
.tip-body h3 {
  margin: 0 0 5px;
  font-size: 15px;
  font-weight: 700;
  color: var(--g-text-primary);
}
.tip-body p {
  margin: 0;
  font-size: 13px;
  line-height: 1.7;
  color: var(--g-text-secondary);
}
.tip-pill { margin-left: auto; flex-shrink: 0; }

.quote-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 26px;
  text-align: center;
}
.quote-glow {
  position: absolute;
  width: 240px; height: 240px;
  left: 50%; top: 50%;
  transform: translate(-50%, -50%);
  background: radial-gradient(circle, rgba(232, 121, 249, 0.18), transparent 70%);
  filter: blur(30px);
  pointer-events: none;
}
.quote-mark { color: var(--g-amber); position: relative; }
.quote-text {
  position: relative;
  margin: 0;
  font-size: 24px;
  font-weight: 800;
  letter-spacing: 1px;
  color: var(--g-text-primary);
}
.quote-text em {
  font-style: normal;
  background: linear-gradient(120deg, var(--g-sky), var(--g-fuchsia));
  background-size: 200% auto;
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  animation: g-shine 4s linear infinite;
}
.quote-sub {
  position: relative;
  font-size: 12px;
  color: var(--g-text-muted);
}

/* ---------- 动画 ---------- */
@keyframes g-shine { to { background-position: 200% center; } }
@keyframes g-pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

/* ---------- 响应式 ---------- */
@media (max-width: 1100px) {
  .hero-stack { right: 2%; transform: translateY(-52%) scale(0.85); }
}
@media (max-width: 992px) {
  .hero {
    min-height: 0;
    padding: 36px 24px 30px;
    border-radius: 24px;
  }
  .hero-stack { display: none; }
  .hero-title { font-size: 38px; letter-spacing: -0.8px; }
  .hero-bottom { flex-direction: column; align-items: stretch; }
  .hero-info { max-width: none; }
  .hero-me { justify-content: flex-start; }
  .stats-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .feature-grid { grid-template-columns: 1fr; }
  .bottom-grid { grid-template-columns: 1fr; }
}
@media (max-width: 560px) {
  .stats-grid { grid-template-columns: 1fr; }
  .hero-title { font-size: 30px; }
  .hero-kicker { letter-spacing: 2.5px; }
}
</style>
