<template>
  <div class="learning">
    <!-- 页头 -->
    <el-card shadow="never" class="head-card">
      <div class="head">
        <div>
          <h2>📚 学习中心</h2>
          <p>刷透题库 · 模拟面试 · 情境演练，多维度提升求职技能</p>
        </div>
        <div class="head-actions">
          <el-button type="primary" @click="startMockInterview">🎯 开始模拟面试</el-button>
          <el-button @click="$router.push('/simulator')">🚀 进入 AI 模拟舱</el-button>
        </div>
      </div>
    </el-card>

    <el-row :gutter="16">
      <!-- 左侧：分类 -->
      <el-col :span="5">
        <el-card shadow="never" class="cat-card">
          <template #header>分类</template>
          <div
            v-for="c in categories"
            :key="c.value"
            class="cat-item"
            :class="{ active: activeCategory === c.value }"
            @click="switchCategory(c.value)"
          >
            <span class="cat-icon">{{ c.icon }}</span>
            <span>{{ c.label }}</span>
            <span class="cat-count" v-if="c.value !== null">{{ countByCat(c.value) }}</span>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：题目列表 -->
      <el-col :span="19">
        <el-card shadow="never">
          <template #header>
            <div class="list-head">
              <span>{{ activeLabel }} 题目（{{ total }} 题）</span>
              <el-tag effect="plain" type="info" size="small">点击题目查看考点解析</el-tag>
            </div>
          </template>

          <div v-if="loading" class="loading-box">
            <el-skeleton :rows="6" animated />
          </div>

          <template v-else>
            <div v-if="questions.length === 0" class="empty-box">
              <el-empty description="该分类暂无题目" />
            </div>

            <div
              v-for="(q, i) in questions"
              :key="q.id"
              class="q-item"
              @click="showDetail(q)"
            >
              <div class="q-index">{{ (page - 1) * size + i + 1 }}</div>
              <div class="q-body">
                <div class="q-text">{{ q.questionText }}</div>
                <div class="q-meta">
                  <el-tag v-if="q.subCategory" size="small" effect="plain" type="primary">{{ q.subCategory }}</el-tag>
                  <el-tag size="small" :type="['', 'success', 'warning', 'danger'][q.difficulty]">
                    {{ ['简单', '简单', '中等', '困难'][q.difficulty] }}
                  </el-tag>
                  <span class="q-cat">{{ ['技术', '行为', '综合'][q.category] }}</span>
                </div>
              </div>
              <el-icon class="q-arrow"><ArrowRight /></el-icon>
            </div>

            <div class="pager">
              <el-pagination
                layout="prev, pager, next"
                :total="total"
                :page-size="size"
                :current-page="page"
                @current-change="p => { page = p; applyLocalFilter() }"
              />
            </div>
          </template>
        </el-card>
      </el-col>
    </el-row>

    <!-- 题目详情抽屉 -->
    <el-drawer v-model="drawer" :title="detail?.subCategory || '题目详情'" size="480px">
      <div v-if="detail">
        <div class="d-meta">
          <el-tag size="small" effect="plain">{{ ['技术', '行为', '综合'][detail.category] }}</el-tag>
          <el-tag size="small" :type="['', 'success', 'warning', 'danger'][detail.difficulty]">
            难度：{{ ['简单', '简单', '中等', '困难'][detail.difficulty] }}
          </el-tag>
        </div>
        <h3 class="d-question">{{ detail.questionText }}</h3>
        <el-divider content-position="left">💡 考点关键词</el-divider>
        <div class="d-keywords">
          <span v-for="k in keywords" :key="k" class="kw-chip">{{ k }}</span>
          <span v-if="keywords.length === 0" class="d-empty">暂无考点数据</span>
        </div>
        <el-alert type="info" :closable="false" style="margin-top:20px">
          <template #title>
            模拟面试时，AI 将根据这些考点评估你的回答。试着覆盖以上关键词组织答案！
          </template>
        </el-alert>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { interviewApi } from '../api'

const router = useRouter()

const categories = [
  { value: null, label: '全部题目', icon: '🗂️' },
  { value: 0, label: '技术面试', icon: '💻' },
  { value: 1, label: '行为面试', icon: '🗣️' },
  { value: 2, label: '综合面试', icon: '🧩' }
]

const activeCategory = ref(null)
const questions = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(8)
const loading = ref(false)
const drawer = ref(false)
const detail = ref(null)
const allQuestions = ref([])

const activeLabel = computed(() => categories.find(c => c.value === activeCategory.value)?.label || '全部')
const keywords = computed(() =>
  (detail.value?.expectedKeywords || '').split(',').map(s => s.trim()).filter(Boolean)
)

function countByCat(v) {
  return allQuestions.value.filter(q => q.category === v).length
}

// 单次请求全量（题库量级小），前端本地分页 + 分类过滤
async function loadAllForCount() {
  loading.value = true
  try {
    const res = await interviewApi.questions({ page: 1, size: 200 })
    allQuestions.value = res.data?.records || []
    applyLocalFilter()
  } catch (e) {
    ElMessage.error('题目加载失败')
  } finally {
    loading.value = false
  }
}

function applyLocalFilter() {
  const filtered = activeCategory.value === null
    ? allQuestions.value
    : allQuestions.value.filter(q => q.category === activeCategory.value)
  total.value = filtered.length
  questions.value = filtered.slice((page.value - 1) * size.value, page.value * size.value)
}

function switchCategory(v) {
  activeCategory.value = v
  page.value = 1
  applyLocalFilter()
}

async function showDetail(q) {
  try {
    const res = await interviewApi.questionDetail(q.id)
    detail.value = res.data
  } catch (e) {
    detail.value = q
  }
  drawer.value = true
}

async function startMockInterview() {
  const type = activeCategory.value ?? 0
  router.push({ path: '/simulator' })
  ElMessage.info('模拟面试入口：可在 AI 模拟舱中选择场景开始演练')
}

onMounted(() => {
  loadAllForCount()
})
</script>

<style scoped>
.head-card { margin-bottom: 16px; }
.head { display: flex; justify-content: space-between; align-items: center; }
.head h2 { margin: 0 0 6px; }
.head p { margin: 0; color: var(--foreground-muted); font-size: 13px; }
.cat-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 11px 12px;
  border-radius: 10px;
  cursor: pointer;
  font-size: 14px;
  color: var(--foreground);
  transition: all .2s;
  margin-bottom: 4px;
}
.cat-item:hover { background: rgba(139, 92, 246, 0.10); color: #c4b5fd; }
html.light .cat-item:hover { background: #f5f3ff; color: #4b3fe3; }
.cat-item.active { background: rgba(139, 92, 246, 0.18); color: #ddd6fe; font-weight: 600; }
html.light .cat-item.active { background: #ede9fe; color: #4b3fe3; }
.cat-count {
  margin-left: auto;
  font-size: 12px;
  background: var(--card-strong);
  border: 1px solid var(--hairline);
  border-radius: 10px;
  padding: 1px 8px;
  color: var(--foreground-muted);
}
.list-head { display: flex; justify-content: space-between; align-items: center; }
.q-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  border: 1px solid var(--hairline);
  border-radius: 12px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: all .2s;
}
.q-item:hover { border-color: #a78bfa; background: rgba(139, 92, 246, 0.08); transform: translateX(3px); }
html.light .q-item:hover { background: #faf9ff; }
.q-index {
  width: 30px; height: 30px;
  border-radius: 9px;
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.35), rgba(167, 139, 250, 0.35));
  color: #ddd6fe;
  font-weight: 700;
  display: flex; align-items: center; justify-content: center;
  font-size: 13px;
  flex-shrink: 0;
}
html.light .q-index { color: #4b2d8c; }
.q-body { flex: 1; min-width: 0; }
.q-text {
  font-size: 14px;
  font-weight: 500;
  color: var(--foreground);
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.q-meta { display: flex; gap: 8px; align-items: center; }
.q-cat { font-size: 12px; color: var(--foreground-muted); }
.q-arrow { color: var(--foreground-subtle); }
html.light .q-arrow { color: #6b7280; }
.pager { display: flex; justify-content: center; margin-top: 14px; }
.loading-box, .empty-box { padding: 10px 0; }
.d-meta { display: flex; gap: 8px; margin-bottom: 14px; }
.d-question { font-size: 16px; line-height: 1.7; color: var(--foreground); }
.d-keywords { display: flex; flex-wrap: wrap; gap: 8px; }
.kw-chip {
  background: linear-gradient(135deg, #ede9fe, #ddd6fe);
  color: #6d28d9;
  padding: 5px 12px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 500;
}
.d-empty { color: var(--foreground-muted); font-size: 13px; }
</style>