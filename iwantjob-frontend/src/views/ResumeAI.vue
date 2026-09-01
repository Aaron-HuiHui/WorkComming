<template>
  <div class="resume-ai">
    <!-- 左侧：我的简历 -->
    <div class="side">
      <el-card shadow="never">
        <template #header>
          <div class="side-head">
            <span>📄 我的简历</span>
            <el-button size="small" type="primary" text @click="createDialog = true">+ 新建</el-button>
          </div>
        </template>
        <div v-if="resumes.length === 0" class="empty-tip">
          暂无简历，点击右上角「+ 新建」创建第一份简历，即可体验 AI 优化
        </div>
        <div
          v-for="r in resumes"
          :key="r.id"
          class="resume-item"
          :class="{ active: currentResume?.id === r.id }"
          @click="selectResume(r)"
        >
          <div class="r-title">{{ r.title || '未命名简历' }}</div>
          <div class="r-meta">
            <el-tag v-if="r.aiScore != null" size="small" :type="r.aiScore >= 80 ? 'success' : r.aiScore >= 60 ? 'warning' : 'danger'">
              AI 评分 {{ r.aiScore }}
            </el-tag>
            <span class="r-ver">v{{ r.version }}</span>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 右侧：AI 对话区 -->
    <div class="chat-area">
      <el-card shadow="never" class="chat-card">
        <template #header>
          <div class="chat-head">
            <span>🤖 AI 简历智能助手</span>
            <el-tag size="small" effect="plain" type="warning">AI 能力 · 已就绪</el-tag>
          </div>
        </template>

        <div class="messages" ref="msgBox">
          <div v-for="(m, i) in messages" :key="i" class="msg" :class="m.role">
            <div class="avatar">{{ m.role === 'ai' ? '🤖' : '🙋' }}</div>
            <div class="bubble">
              <div class="bubble-title" v-if="m.title">{{ m.title }}</div>
              <div class="bubble-text" :style="m.html ? '' : 'white-space:pre-wrap'">{{ m.text }}</div>
            </div>
          </div>
          <div v-if="thinking" class="msg ai">
            <div class="avatar">🤖</div>
            <div class="bubble typing">
              <span></span><span></span><span></span>
            </div>
          </div>
        </div>

        <div class="quick-actions">
          <button class="qa-btn" :disabled="!currentResume || thinking" @click="runOptimize(0)">✨ 一键润色</button>
          <button class="qa-btn" :disabled="!currentResume || thinking" @click="runOptimize(1)">🌍 翻译英文版</button>
          <button class="qa-btn" :disabled="!currentResume || thinking" @click="runOptimize(2)">💪 强化亮点</button>
          <button class="qa-btn" :disabled="!currentResume || thinking" @click="runScore">📊 AI 评分</button>
        </div>

        <div class="input-bar">
          <el-input
            v-model="input"
            :placeholder="currentResume ? '描述你的优化需求，例如：帮我优化项目经历的描述' : '请先在左侧选择一份简历'"
            :disabled="!currentResume"
            @keyup.enter="send"
          />
          <el-button type="primary" :loading="thinking" :disabled="!currentResume" @click="send">发送</el-button>
        </div>
      </el-card>
    </div>

    <!-- 新建简历弹窗 -->
    <el-dialog v-model="createDialog" title="新建简历" width="560px">
      <el-form label-width="90px">
        <el-form-item label="简历标题">
          <el-input v-model="newResume.title" placeholder="如：张三-后端开发简历" />
        </el-form-item>
        <el-form-item label="简历内容">
          <el-input
            v-model="newResume.contentJson"
            type="textarea"
            :rows="10"
            placeholder='JSON 格式，示例：
{
  "basic": {"name": "张三", "school": "xx大学", "major": "软件工程"},
  "skills": ["Java", "Spring", "MySQL"],
  "projects": [{"name": "xx系统", "role": "后端", "desc": "描述"}],
  "internships": []
}'
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialog = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="createResume">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { resumeApi } from '../api'

const resumes = ref([])
const currentResume = ref(null)
const messages = ref([
  {
    role: 'ai',
    title: '欢迎使用 AI 简历智能助手',
    text: '我可以帮你：\n✨ 润色简历表达，让语言更专业\n🌍 翻译为英文简历，应对外企投递\n💪 强化亮点经历，突出量化成果\n📊 对简历进行 AI 评分并给出改进建议\n\n左侧选择一份简历，或点击下方快捷按钮开始！'
  }
])
const input = ref('')
const thinking = ref(false)
const msgBox = ref(null)
const createDialog = ref(false)
const creating = ref(false)
const newResume = ref({ title: '', contentJson: '' })

onMounted(loadResumes)

async function loadResumes() {
  try {
    const res = await resumeApi.myResumes()
    resumes.value = Array.isArray(res.data) ? res.data : (res.data?.records || [])
    if (resumes.value.length > 0 && !currentResume.value) {
      selectResume(resumes.value[0])
    }
  } catch (e) { /* 静默 */ }
}

function selectResume(r) {
  currentResume.value = r
  pushMsg('ai', `已选择「${r.title || '未命名简历'}」，需要我做什么？试试快捷按钮或直接输入需求～`)
}

function pushMsg(role, text, title) {
  messages.value.push({ role, text, title })
  nextTick(() => {
    if (msgBox.value) msgBox.value.scrollTop = msgBox.value.scrollHeight
  })
}

async function runOptimize(type) {
  const typeDesc = { 0: '一键润色', 1: '翻译英文版', 2: '强化亮点' }[type]
  pushMsg('user', `请帮我${typeDesc}这份简历`)
  thinking.value = true
  try {
    const res = await resumeApi.optimize({
      resumeId: currentResume.value.id,
      type,
      targetLang: type === 1 ? 'en' : null
    })
    const d = res.data || {}
    pushMsg('ai', d.optimizedText || '（AI 未返回优化结果）', `✨ ${typeDesc}完成 · AI 反馈：${d.feedback || '无'}`)
  } catch (e) {
    pushMsg('ai', '优化失败：' + (e.message || '服务异常，请稍后再试'))
  } finally {
    thinking.value = false
  }
}

async function runScore() {
  pushMsg('user', '请给这份简历打个分')
  thinking.value = true
  try {
    const res = await resumeApi.score(currentResume.value.id)
    const d = res.data || {}
    const score = d.aiScore ?? 0
    const level = score >= 80 ? '优秀 🏆' : score >= 60 ? '良好，仍有提升空间 👍' : '需要重点打磨 💪'
    pushMsg('ai', `AI 评分：${score} / 100（${level}）\n\n${d.feedback || ''}`, '📊 简历评分报告')
    // 刷新列表评分
    await loadResumes()
    const found = resumes.value.find(r => r.id === currentResume.value.id)
    if (found) currentResume.value = found
  } catch (e) {
    pushMsg('ai', '评分失败：' + (e.message || '服务异常'))
  } finally {
    thinking.value = false
  }
}

async function send() {
  const text = input.value.trim()
  if (!text) return
  pushMsg('user', text)
  input.value = ''
  thinking.value = true
  try {
    // 意图识别：包含"翻译/英文"走翻译，包含"评分/打分"走评分，其余走润色
    if (/翻译|英文|english/i.test(text)) {
      await resumeApi.optimize({ resumeId: currentResume.value.id, type: 1, targetLang: 'en' })
        .then(res => pushMsg('ai', res.data?.optimizedText || '（无结果）', '🌍 英文版简历'))
    } else if (/评分|打分|分数|score/i.test(text)) {
      const res = await resumeApi.score(currentResume.value.id)
      pushMsg('ai', `AI 评分：${res.data?.aiScore ?? 0} / 100\n\n${res.data?.feedback || ''}`, '📊 简历评分报告')
      await loadResumes()
    } else {
      const res = await resumeApi.optimize({ resumeId: currentResume.value.id, type: 0 })
      pushMsg('ai', res.data?.optimizedText || '（无结果）', '✨ 优化结果')
    }
  } catch (e) {
    pushMsg('ai', '处理失败：' + (e.message || '服务异常，请稍后再试'))
  } finally {
    thinking.value = false
  }
}

async function createResume() {
  if (!newResume.value.title) {
    ElMessage.warning('请填写简历标题')
    return
  }
  try {
    JSON.parse(newResume.value.contentJson || '{}')
  } catch (e) {
    ElMessage.error('简历内容必须是合法 JSON')
    return
  }
  creating.value = true
  try {
    await resumeApi.create(newResume.value)
    ElMessage.success('创建成功')
    createDialog.value = false
    newResume.value = { title: '', contentJson: '' }
    await loadResumes()
  } catch (e) {
    ElMessage.error('创建失败：' + (e.message || '服务异常'))
  } finally {
    creating.value = false
  }
}
</script>

<style scoped>
.resume-ai {
  display: flex;
  gap: 16px;
  height: calc(100vh - 130px);
}
.side { width: 280px; flex-shrink: 0; }
.side-head { display: flex; justify-content: space-between; align-items: center; }
.empty-tip { font-size: 13px; color: #909399; line-height: 1.8; padding: 10px 0; }
.resume-item {
  padding: 12px 14px;
  border: 1px solid #f0f0f5;
  border-radius: 10px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all .2s;
}
.resume-item:hover { border-color: #c4b5fd; background: #faf9ff; }
.resume-item.active { border-color: #8b5cf6; background: #f5f3ff; }
.r-title { font-size: 14px; font-weight: 600; color: #1f2337; margin-bottom: 6px; }
.r-meta { display: flex; gap: 8px; align-items: center; }
.r-ver { font-size: 12px; color: #c0c4cc; }
.chat-area { flex: 1; min-width: 0; }
.chat-card { height: 100%; display: flex; flex-direction: column; }
.chat-card :deep(.el-card__body) { flex: 1; display: flex; flex-direction: column; min-height: 0; padding: 14px; }
.chat-head { display: flex; justify-content: space-between; align-items: center; }
.messages { flex: 1; overflow-y: auto; padding: 6px 4px; }
.msg { display: flex; gap: 10px; margin-bottom: 16px; }
.msg.user { flex-direction: row-reverse; }
.avatar {
  width: 34px; height: 34px;
  border-radius: 50%;
  background: #f5f3ff;
  display: flex; align-items: center; justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}
.bubble {
  max-width: 76%;
  background: #f7f8fa;
  border-radius: 4px 14px 14px 14px;
  padding: 12px 16px;
  font-size: 14px;
  line-height: 1.8;
  color: #333;
}
.msg.user .bubble {
  background: linear-gradient(135deg, #7c3aed, #6d28d9);
  color: #fff;
  border-radius: 14px 4px 14px 14px;
}
.bubble-title {
  font-size: 13px;
  font-weight: 700;
  color: #6d28d9;
  margin-bottom: 6px;
  padding-bottom: 6px;
  border-bottom: 1px dashed #e5e7eb;
}
.msg.user .bubble-title { color: #ddd6fe; border-color: rgba(255,255,255,.3); }
.bubble-text { word-break: break-word; }
.typing { display: flex; gap: 5px; align-items: center; padding: 16px 18px; }
.typing span {
  width: 7px; height: 7px; border-radius: 50%;
  background: #a78bfa;
  animation: blink 1.2s infinite;
}
.typing span:nth-child(2) { animation-delay: .2s; }
.typing span:nth-child(3) { animation-delay: .4s; }
@keyframes blink { 0%, 80%, 100% { opacity: .25; } 40% { opacity: 1; } }
.quick-actions {
  display: flex;
  gap: 10px;
  padding: 10px 0;
  border-top: 1px solid #f0f0f5;
}
.qa-btn {
  flex: 1;
  border: 1px solid #ddd6fe;
  background: #faf9ff;
  color: #6d28d9;
  border-radius: 10px;
  padding: 9px 0;
  font-size: 13px;
  cursor: pointer;
  transition: all .2s;
  white-space: nowrap;
}
.qa-btn:hover:not(:disabled) { background: #ede9fe; border-color: #8b5cf6; }
.qa-btn:disabled { opacity: .45; cursor: not-allowed; }
.input-bar { display: flex; gap: 10px; padding-top: 4px; }
</style>