<template>
  <div>
    <!-- 场景选择 -->
    <template v-if="stage === 'select'">
      <el-card shadow="never" style="margin-bottom:16px">
        <template #header>🚀 AI 职业模拟舱 · 选择场景开始演练</template>
        <el-row :gutter="16">
          <el-col :span="6" v-for="s in scenarios" :key="s.id">
            <el-card shadow="hover" class="scenario-card" @click="startSession(s)">
              <div class="sc-type">{{ s.typeDesc }}</div>
              <h3>{{ s.title }}</h3>
              <p>{{ s.description }}</p>
              <el-rate :model-value="s.difficulty" disabled size="small" style="margin-top:8px" />
            </el-card>
          </el-col>
        </el-row>
      </el-card>

      <!-- 历史记录 -->
      <el-card shadow="never">
        <template #header>📊 我的演练历史</template>
        <el-table :data="history" stripe>
          <el-table-column prop="scenarioTitle" label="场景" min-width="180" />
          <el-table-column label="得分" width="120">
            <template #default="{ row }">
              <el-tag v-if="row.overallScore != null" type="success">{{ row.overallScore }} 分</el-tag>
              <el-tag v-else type="info">进行中</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="statusDesc" label="状态" width="100" />
          <el-table-column prop="startedAt" label="时间" width="180">
            <template #default="{ row }">{{ (row.startedAt || '').replace('T', ' ').slice(0, 16) }}</template>
          </el-table-column>
        </el-table>
      </el-card>
    </template>

    <!-- 演练进行中 -->
    <el-card v-else-if="stage === 'playing'" shadow="never" class="playing-card">
      <template #header>
        <div class="card-header-flex">
          <span>🎮 {{ session.scenarioTitle }}</span>
          <el-tag type="warning">第 {{ step }} 步</el-tag>
        </div>
      </template>

      <!-- 情境描述 -->
      <div class="node-desc">
        <div class="scene-label">当前情境</div>
        <p>{{ currentNode.nodeDesc }}</p>
      </div>

      <!-- 选项 -->
      <div class="options">
        <div class="options-label">请做出你的选择：</div>
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

      <!-- AI 反馈 -->
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
    </el-card>

    <!-- 报告 -->
    <el-card v-else shadow="never">
      <template #header>
        <div class="card-header-flex">
          <span>📋 演练报告 · {{ report.scenarioTitle }}</span>
          <el-button size="small" @click="backToSelect">再练一次</el-button>
        </div>
      </template>

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
      <h4>💬 总体评价</h4>
      <p class="report-text">{{ report.summary }}</p>
      <h4>💡 改进建议</h4>
      <p class="report-text">{{ report.suggestions }}</p>

      <el-divider />
      <h4>🧭 全程选择回放</h4>
      <el-timeline style="margin-top:12px">
        <el-timeline-item v-for="(c, i) in report.choices || []" :key="c.id" :timestamp="'第' + (i + 1) + '步'">
          <p class="tl-q">{{ c.nodeDesc }}</p>
          <p class="tl-a">你选择了：<b>{{ c.userChoice }}</b></p>
          <p class="tl-f" v-if="c.aiFeedback">AI：{{ c.aiFeedback }}</p>
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { simulatorApi } from '../api'

const stage = ref('select') // select / playing / report
const scenarios = ref([])
const history = ref([])

const session = ref({})
const currentNode = ref({})
const step = ref(1)
const showFeedback = ref(false)
const feedback = ref('')
const tags = ref('')
const chosenId = ref(null)
const finished = ref(false)
const report = ref({})

onMounted(async () => {
  const [s, h] = await Promise.all([
    simulatorApi.scenarios(),
    simulatorApi.mySessions({ page: 1, size: 10 })
  ])
  scenarios.value = s.data || []
  history.value = h.data?.records || []
})

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
    await ElMessageBox.confirm(`确定选择「${opt.optionText}」吗？`, '确认选择', {
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
      // 练习完成：徽章事件由后端触发
      ElMessage.success('🎉 演练完成！')
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
.scenario-card { cursor: pointer; transition: transform .2s; text-align: left; }
.scenario-card:hover { transform: translateY(-4px); }
.sc-type { font-size: 12px; color: #e6a23c; }
.scenario-card h3 { margin: 6px 0; }
.scenario-card p { font-size: 13px; color: #909399; min-height: 40px; }
.playing-card { max-width: 760px; margin: 0 auto; }
.scene-label, .options-label { font-size: 12px; color: #909399; margin-bottom: 8px; }
.node-desc { background: #f4f6fb; border-radius: 8px; padding: 16px; margin-bottom: 20px; }
.node-desc p { line-height: 1.8; margin: 0; }
.option-item {
  border: 1px solid #dcdfe6; border-radius: 8px; padding: 14px 16px; margin-bottom: 10px;
  cursor: pointer; display: flex; justify-content: space-between; align-items: center; gap: 10px;
  transition: all .2s;
}
.option-item:hover { border-color: #409eff; background: #f0f7ff; }
.option-item.chosen { border-color: #67c23a; background: #f0f9eb; }
.fb-text { line-height: 1.8; }
.fb-tags { display: flex; gap: 8px; flex-wrap: wrap; margin-top: 12px; }
.score-hero { display: flex; gap: 40px; align-items: center; }
.score-circle {
  width: 140px; height: 140px; border-radius: 50%; background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex; flex-direction: column; align-items: center; justify-content: center; color: #fff; flex-shrink: 0;
}
.score-num { font-size: 40px; font-weight: 700; }
.score-label { font-size: 12px; opacity: .85; }
.score-dims { flex: 1; }
.dim-row { display: flex; align-items: center; gap: 12px; margin-bottom: 10px; }
.dim-name { width: 84px; font-size: 13px; color: #606266; text-align: right; }
.report-text { line-height: 1.8; color: #606266; }
.tl-q { color: #909399; font-size: 13px; margin: 0 0 4px; }
.tl-a { margin: 0 0 4px; }
.tl-f { color: #67c23a; font-size: 13px; margin: 0; }
</style>
