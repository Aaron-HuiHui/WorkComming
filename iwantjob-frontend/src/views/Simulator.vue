<template>
  <div class="simulator">
    <!-- ============ 场景选择:hero-1 式 AI 聊天 Hero ============ -->
    <template v-if="stage === 'select'">
      <section class="sim-hero">
        <!-- 斜置光柱背景(hero-1 同款:白→蓝渐变条,-20°,模糊) -->
        <div class="hero-beams" aria-hidden="true">
          <div v-for="row in 3" :key="row" class="beam-row" :style="{ top: -34 - row * 9 + 'rem', right: -26 - row * 12 + 'rem' }">
            <div v-for="bar in 3" :key="bar" class="beam-bar" :style="{ height: row === 3 ? '30rem' : '20rem' }"></div>
          </div>
        </div>

        <div class="hero-center">
          <div class="hero-pill">
            <span class="pill-emoji">🥳</span>
            AI 职业教练已就绪
          </div>
          <h1 class="hero-title">在情境中演练,<br />在演练中<span class="text-gradient">遇见更好的自己</span></h1>
          <p class="hero-sub">选择一个职场情境,AI 教练陪你走完每一步,实时反馈你的软技能表现</p>

          <!-- AI 聊天输入条(hero-1 同款胶囊:p-3 圆角全圆) -->
          <div class="chat-bar">
            <span class="chat-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 8V4H8" /><rect x="4" y="8" width="16" height="12" rx="2" /><path d="M2 14h2M20 14h2M15 13v2M9 13v2" /></svg>
            </span>
            <input
              v-model="keyword"
              class="chat-input"
              type="text"
              placeholder="描述你想演练的情境,如「汇报」「冲突」…"
              @keyup.enter="startByKeyword"
            />
            <button class="chat-send" :disabled="!keyword.trim()" @click="startByKeyword" title="开始演练">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 2 11 13" /><path d="M22 2 15 22l-4-9-9-4Z" /></svg>
            </button>
          </div>

          <!-- 推荐 chips(hero-1 同款建议词) -->
          <div class="chip-row">
            <button
              v-for="s in chipScenarios"
              :key="s.id"
              class="chip"
              @click="startSession(s)"
            >{{ s.title }}</button>
          </div>
        </div>
      </section>

      <!-- 场景卡片(hero-1 之下保留完整信息) -->
      <section v-if="scenarios.length" class="scene-grid">
        <div
          v-for="s in scenarios"
          :key="s.id"
          class="glass-card glass-card-hover scene-card"
          @click="startSession(s)"
        >
          <div class="sc-top">
            <span class="sc-type">{{ s.typeDesc }}</span>
            <el-rate :model-value="s.difficulty" disabled size="small" />
          </div>
          <h3 class="sc-title">{{ s.title }}</h3>
          <p class="sc-desc">{{ s.description }}</p>
          <span class="sc-enter">开始演练 →</span>
        </div>
      </section>
      <div v-else-if="!scenarios.length" class="empty-hint">
        <el-empty description="场景加载中…" :image-size="60" />
      </div>

      <!-- 历史记录 -->
      <section class="glass-card history-card" v-if="history.length">
        <div class="history-head">📊 我的演练历史</div>
        <el-table :data="history" stripe>
          <el-table-column prop="scenarioTitle" label="场景" min-width="180" />
          <el-table-column label="得分" width="120">
            <template #default="{ row }">
              <el-tag v-if="row.overallScore != null" type="success">{{ row.overallScore }} 分</el-tag>
              <el-tag v-else type="info">进行中</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.overallScore != null" type="success" size="small">已完成</el-tag>
              <el-tag v-else type="warning" size="small" effect="plain">进行中</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="startedAt" label="时间" width="180">
            <template #default="{ row }">{{ (row.startedAt || '').replace('T', ' ').slice(0, 16) }}</template>
          </el-table-column>
        </el-table>
      </section>
    </template>

    <!-- ============ 演练进行中 ============ -->
    <div v-else-if="stage === 'playing'" class="playing-wrap">
      <div class="glass-card playing-card">
        <div class="playing-head">
          <span class="playing-title">🎮 {{ session.scenarioTitle }}</span>
          <el-tag type="warning" round>第 {{ step }} 步</el-tag>
        </div>

        <div class="node-desc">
          <div class="scene-label">当前情境</div>
          <p>{{ currentNode.nodeDesc }}</p>
        </div>

        <div class="options">
          <div class="scene-label">请做出你的选择:</div>
          <div
            v-for="opt in currentNode.options"
            :key="opt.id"
            class="option-item"
            :class="{ chosen: chosenId === opt.id }"
            @click="choose(opt)"
          >
            <span>{{ opt.optionText }}</span>
            <el-tag v-if="opt.softSkillTags" size="small" type="info" effect="plain">{{ opt.softSkillTags }}</el-tag>
          </div>
        </div>

        <el-dialog v-model="showFeedback" title="🤖 AI 教练反馈" width="460px" :close-on-click-modal="false">
          <div class="ai-feedback">
            <p class="fb-text">{{ feedback }}</p>
            <div class="fb-tags">
              <el-tag v-for="t in tags.split(',')" :key="t" type="success" effect="plain">⚡ {{ t }}</el-tag>
            </div>
          </div>
          <template #footer>
            <el-button v-if="!finished" type="primary" @click="showFeedback = false">继续下一步</el-button>
            <el-button v-else type="success" @click="viewReport">查看演练报告</el-button>
          </template>
        </el-dialog>
      </div>
    </div>

    <!-- ============ 报告 ============ -->
    <div v-else class="playing-wrap">
      <div class="glass-card report-card">
        <div class="playing-head">
          <span class="playing-title">📋 演练报告 · {{ report.scenarioTitle }}</span>
          <el-button size="small" round @click="backToSelect">再练一次</el-button>
        </div>

        <div class="score-hero">
          <div class="score-circle">
            <div class="score-num">{{ report.overallScore ?? '--' }}</div>
            <div class="score-label">综合得分</div>
          </div>
          <div class="score-dims">
            <div v-for="(v, k) in report.dimensionScores" :key="k" class="dim-row">
              <span class="dim-name">{{ k }}</span>
              <el-progress :percentage="v" :stroke-width="10" style="flex:1" />
            </div>
          </div>
        </div>

        <el-divider />
        <h4 class="report-h">💬 总体评价</h4>
        <p class="report-text">{{ report.summary }}</p>
        <h4 class="report-h">💡 改进建议</h4>
        <p class="report-text">{{ report.suggestions }}</p>

        <el-divider />
        <h4 class="report-h">🧭 全程选择回放</h4>
        <el-timeline style="margin-top:12px">
          <el-timeline-item v-for="(c, i) in report.choices || []" :key="c.id" :timestamp="'第' + (i + 1) + '步'">
            <p class="tl-q">{{ c.nodeDesc }}</p>
            <p class="tl-a">你选择了:<b>{{ c.userChoice }}</b></p>
            <p class="tl-f" v-if="c.aiFeedback">AI:{{ c.aiFeedback }}</p>
          </el-timeline-item>
        </el-timeline>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { simulatorApi } from '../api'

const stage = ref('select') // select / playing / report
const scenarios = ref([])
const history = ref([])
const keyword = ref('')

const session = ref({})
const currentNode = ref({})
const step = ref(1)
const showFeedback = ref(false)
const feedback = ref('')
const tags = ref('')
const chosenId = ref(null)
const finished = ref(false)
const report = ref({})

// hero 推荐 chips:前 5 个场景
const chipScenarios = computed(() => scenarios.value.slice(0, 5))

onMounted(async () => {
  try {
    const [s, h] = await Promise.all([
      simulatorApi.scenarios(),
      simulatorApi.mySessions({ page: 1, size: 10 }).catch(() => ({ data: null }))
    ])
    scenarios.value = s.data || []
    history.value = h.data?.records || []
  } catch (e) { /* 静默 */ }
})

// 输入条:按关键词匹配场景(标题/描述/类型),命中即开始
function startByKeyword() {
  const k = keyword.value.trim()
  if (!k) return
  const hit = scenarios.value.find(s =>
    (s.title || '').includes(k) || (s.description || '').includes(k) || (s.typeDesc || '').includes(k)
  )
  if (hit) {
    startSession(hit)
  } else {
    ElMessage.info('没找到匹配的情境,试试下方推荐或卡片')
  }
}

async function startSession(s) {
  try {
    const res = await simulatorApi.start(s.id)
    session.value = res.data
    currentNode.value = res.data.currentNode
    step.value = 1
    finished.value = false
    chosenId.value = null
    stage.value = 'playing'
  } catch (e) {
    // 限流或幂等拦截已在拦截器提示
  }
}

async function choose(opt) {
  if (chosenId.value) return
  try {
    await ElMessageBox.confirm(`确定选择「${opt.optionText}」吗?`, '确认选择', {
      confirmButtonText: '确定',
      cancelButtonText: '再想想',
      type: 'info'
    })
  } catch {
    return
  }

  chosenId.value = opt.id
  try {
    const res = await simulatorApi.choose({
      sessionId: session.value.sessionId,
      optionId: opt.id
    })
    const d = res.data
    feedback.value = d.aiFeedback
    tags.value = d.softSkillTags || ''
    finished.value = !!d.finished
    showFeedback.value = true

    if (d.finished) {
      // 练习完成:徽章事件由后端触发
      ElMessage.success('🎉 演练完成!')
    } else if (d.nextNode) {
      currentNode.value = d.nextNode
      step.value++
    }
  } catch (e) {
    chosenId.value = null
  } finally {
    if (!finished.value) setTimeout(() => (chosenId.value = null), 400)
  }
}

async function viewReport() {
  showFeedback.value = false
  const res = await simulatorApi.report(session.value.sessionId)
  report.value = res.data || {}
  stage.value = 'report'
}

function backToSelect() {
  stage.value = 'select'
  simulatorApi.mySessions({ page: 1, size: 10 }).then(h => (history.value = h.data?.records || []))
}
</script>

<style scoped>
.simulator { max-width: 1200px; margin: 0 auto; }

/* ============ AI 聊天 Hero ============ */
.sim-hero {
  position: relative;
  border-radius: 28px;
  overflow: hidden;
  background: var(--background-soft);
  border: 1px solid var(--hairline);
  margin-bottom: 22px;
}
/* 斜置光柱(hero-1:白→蓝渐变条,-20°,模糊,右上角) */
.hero-beams {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}
.beam-row {
  position: absolute;
  display: flex;
  gap: 10rem;
  transform: rotate(-20deg);
  filter: blur(3px);
}
.beam-bar {
  width: 10rem;
  border-radius: 6px;
  background: linear-gradient(90deg, var(--primary) 0%, var(--primary-soft) 100%);
  opacity: 0.42;
}
html.light .beam-bar { opacity: 0.24; }

.hero-center {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 72px 24px 64px;
}

.hero-pill {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 18px;
  border-radius: 9999px;
  background: var(--card-strong);
  border: 1px solid var(--hairline);
  font-size: 0.82rem;
  color: var(--foreground-muted);
  margin-bottom: 26px;
}
.pill-emoji {
  display: inline-grid;
  place-items: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: var(--background);
  font-size: 0.8rem;
}

.hero-title {
  font-size: clamp(2rem, 4.6vw, 3.4rem);
  font-weight: 760;
  letter-spacing: -0.03em;
  line-height: 1.16;
  color: var(--foreground);
  margin: 0 0 16px;
}
.hero-sub {
  font-size: 1.02rem;
  color: var(--foreground-muted);
  margin: 0 0 34px;
  max-width: 520px;
  line-height: 1.7;
}

/* AI 聊天输入条(hero-1:rounded-full p-3) */
.chat-bar {
  display: flex;
  align-items: center;
  gap: 6px;
  width: 100%;
  max-width: 560px;
  padding: 10px;
  border-radius: 9999px;
  background: var(--card-strong);
  border: 1px solid var(--hairline-strong);
  backdrop-filter: blur(20px) saturate(1.5);
  -webkit-backdrop-filter: blur(20px) saturate(1.5);
  transition: border-color 0.3s, box-shadow 0.3s;
}
.chat-bar:focus-within {
  border-color: var(--primary);
  box-shadow: 0 0 0 4px var(--primary-soft);
}
.chat-icon {
  display: grid;
  place-items: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  color: var(--foreground-muted);
  flex-shrink: 0;
}
.chat-icon svg { width: 19px; height: 19px; }
.chat-input {
  flex: 1;
  min-width: 0;
  border: none;
  outline: none;
  background: transparent;
  font-size: 0.95rem;
  color: var(--foreground);
  font-family: inherit;
}
.chat-input::placeholder { color: var(--foreground-subtle); }
.chat-send {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  border: none;
  display: grid;
  place-items: center;
  background: var(--primary);
  color: var(--primary-foreground);
  cursor: pointer;
  flex-shrink: 0;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}
.chat-send:hover:not(:disabled) { filter: brightness(1.12); transform: scale(1.06); }
.chat-send:disabled { opacity: 0.45; cursor: not-allowed; }
.chat-send svg { width: 17px; height: 17px; }

/* 推荐 chips */
.chip-row {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 10px;
  margin-top: 24px;
}
.chip {
  padding: 8px 16px;
  border-radius: 9999px;
  border: 1px solid var(--hairline);
  background: var(--card);
  color: var(--foreground-muted);
  font-size: 0.84rem;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}
.chip:hover {
  border-color: var(--primary);
  color: var(--primary);
  transform: translateY(-2px);
}

/* ============ 场景卡片 ============ */
.scene-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 22px;
}
@media (max-width: 1100px) { .scene-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 640px) { .scene-grid { grid-template-columns: 1fr; } }

.scene-card { padding: 20px; cursor: pointer; position: relative; }
.sc-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.sc-type {
  font-size: 0.74rem;
  font-weight: 600;
  color: var(--primary-text);
  letter-spacing: 0.05em;
}
.sc-title { font-size: 1.04rem; font-weight: 660; color: var(--foreground); margin: 0 0 8px; }
.sc-desc {
  font-size: 0.83rem;
  color: var(--foreground-muted);
  line-height: 1.65;
  min-height: 42px;
  margin: 0 0 12px;
}
.sc-enter {
  font-size: 0.8rem;
  color: var(--foreground-subtle);
  transition: color 0.3s;
}
.scene-card:hover .sc-enter { color: var(--primary); }

.empty-hint { padding: 20px 0; }

/* ============ 历史 ============ */
.history-card { padding: 20px; }
.history-head {
  font-size: 0.95rem;
  font-weight: 640;
  color: var(--foreground);
  margin-bottom: 14px;
}

/* ============ 演练中 / 报告 ============ */
.playing-wrap { max-width: 760px; margin: 0 auto; }
.playing-card, .report-card { padding: 28px; }
.playing-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 22px;
}
.playing-title { font-size: 1.08rem; font-weight: 660; color: var(--foreground); }

.scene-label {
  font-size: 0.78rem;
  color: var(--foreground-subtle);
  margin-bottom: 8px;
  letter-spacing: 0.06em;
}
.node-desc {
  background: var(--card);
  border: 1px solid var(--hairline);
  border-radius: 16px;
  padding: 18px 20px;
  margin-bottom: 22px;
}
.node-desc p { line-height: 1.85; margin: 0; color: var(--foreground); font-size: 0.95rem; }

.option-item {
  border: 1px solid var(--hairline);
  border-radius: 14px;
  padding: 15px 18px;
  margin-bottom: 10px;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  color: var(--foreground);
  font-size: 0.92rem;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}
.option-item:hover { border-color: var(--primary); background: var(--primary-soft); transform: translateX(4px); }
.option-item.chosen { border-color: var(--success); background: rgba(48, 209, 88, 0.1); }

.fb-text { line-height: 1.85; color: var(--foreground); }
.fb-tags { display: flex; gap: 8px; flex-wrap: wrap; margin-top: 14px; }

.score-hero { display: flex; gap: 40px; align-items: center; }
@media (max-width: 640px) { .score-hero { flex-direction: column; } }
.score-circle {
  width: 140px; height: 140px; border-radius: 50%;
  background: var(--gradient-accent);
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  color: var(--primary-foreground); flex-shrink: 0;
  box-shadow: 0 12px 40px rgba(41, 151, 255, 0.3);
}
.score-num { font-size: 40px; font-weight: 750; }
.score-label { font-size: 12px; opacity: 0.85; }
.score-dims { flex: 1; min-width: 0; }
.dim-row { display: flex; align-items: center; gap: 12px; margin-bottom: 10px; }
.dim-name { width: 84px; font-size: 0.84rem; color: var(--foreground-muted); text-align: right; }
.report-h { color: var(--foreground); font-size: 1rem; margin: 8px 0; }
.report-text { line-height: 1.85; color: var(--foreground-muted); }
.tl-q { color: var(--foreground-muted); font-size: 0.84rem; margin: 0 0 4px; }
.tl-a { margin: 0 0 4px; color: var(--foreground); }
.tl-f { color: var(--success); font-size: 0.84rem; margin: 0; }
</style>
