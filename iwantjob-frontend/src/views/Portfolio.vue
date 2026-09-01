<template>
  <div>
    <el-card shadow="never" style="margin-bottom:16px">
      <div class="toolbar">
        <el-tabs v-model="tab" @tab-change="onTab">
          <el-tab-pane label="🎨 作品广场" name="all" />
          <el-tab-pane label="📝 我的作品" name="mine" v-if="canPublish" />
        </el-tabs>
        <div class="toolbar-right">
          <el-input v-model="tag" placeholder="按技术标签过滤，如 Vue" clearable style="width:200px" @keyup.enter="load(1)" v-if="tab === 'all'" />
          <el-button type="primary" :icon="Plus" @click="openEdit(null)" v-if="canPublish">发布作品</el-button>
        </div>
      </div>
    </el-card>

    <div v-loading="loading">
      <el-row :gutter="16" v-if="list.length">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="p in list" :key="p.id" style="margin-bottom:16px">
          <el-card shadow="hover" class="p-card" @click="openDetail(p)">
            <div class="p-cover">{{ p.cover || '🚀' }}</div>
            <div class="p-title">{{ p.title }}</div>
            <div class="p-author">{{ p.authorRealName || p.authorName }}</div>
            <div class="p-tags">
              <el-tag v-for="t in splitTags(p.techTags)" :key="t" size="small" effect="plain" style="margin-right:6px">{{ t }}</el-tag>
            </div>
            <div class="p-desc">{{ p.description || '暂无描述' }}</div>
            <div class="p-foot">
              <span class="p-stat">👁 {{ p.viewCount || 0 }}</span>
              <el-button
                :type="p.liked ? 'danger' : 'default'" size="small" round
                :icon="p.liked ? Pointer : Star"
                @click.stop="toggleLike(p)"
              >{{ p.likeCount || 0 }}</el-button>
            </div>
            <div class="p-mine-ops" v-if="tab === 'mine'">
              <el-button size="small" text type="primary" @click.stop="openEdit(p)">编辑</el-button>
              <el-button size="small" text type="danger" @click.stop="removeOne(p)">删除</el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-empty v-else-if="!loading" description="还没有作品，快来发布第一个吧" />
    </div>

    <el-pagination
      style="margin-top:8px; justify-content:flex-end"
      layout="total, prev, pager, next"
      :total="total" :page-size="size" :current-page="page"
      @current-change="load"
    />

    <!-- 详情抽屉 -->
    <el-drawer v-model="detailDrawer" :title="current?.title || '作品详情'" size="480px">
      <template v-if="current">
        <div class="d-cover">{{ current.cover || '🚀' }}</div>
        <el-descriptions :column="1" border style="margin-top:12px">
          <el-descriptions-item label="作者">{{ current.authorRealName || current.authorName }}</el-descriptions-item>
          <el-descriptions-item label="技术栈">{{ current.techTags || '-' }}</el-descriptions-item>
          <el-descriptions-item label="浏览 / 点赞">{{ current.viewCount || 0 }} / {{ current.likeCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ (current.createdAt || '').slice(0, 10) }}</el-descriptions-item>
        </el-descriptions>
        <h4 style="margin:16px 0 8px">作品描述</h4>
        <p class="pre">{{ current.description || '暂无' }}</p>
        <div style="margin-top:16px; display:flex; gap:10px">
          <el-button v-if="current.repoUrl" type="primary" tag="a" :href="current.repoUrl" target="_blank">查看仓库</el-button>
          <el-button v-if="current.demoUrl" tag="a" :href="current.demoUrl" target="_blank">在线演示</el-button>
        </div>
      </template>
    </el-drawer>

    <!-- 发布/编辑弹窗 -->
    <el-dialog v-model="editDialog" :title="editForm.id ? '编辑作品' : '发布作品'" width="560px">
      <el-form :model="editForm" label-width="90px">
        <el-form-item label="标题" required>
          <el-input v-model="editForm.title" maxlength="100" placeholder="如：校园二手交易平台" />
        </el-form-item>
        <el-form-item label="封面 emoji">
          <el-input v-model="editForm.cover" maxlength="50" placeholder="如 🚀 🎮 📊" style="width:120px" />
        </el-form-item>
        <el-form-item label="技术标签">
          <el-input v-model="editForm.techTags" maxlength="200" placeholder="逗号分隔，如 Java,SpringBoot,Vue3" />
        </el-form-item>
        <el-form-item label="作品描述">
          <el-input v-model="editForm.description" type="textarea" :rows="4" maxlength="2000" placeholder="项目背景、你的角色、技术亮点" />
        </el-form-item>
        <el-form-item label="仓库链接">
          <el-input v-model="editForm.repoUrl" placeholder="https://github.com/..." />
        </el-form-item>
        <el-form-item label="演示链接">
          <el-input v-model="editForm.demoUrl" placeholder="https://..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Star, Pointer } from '@element-plus/icons-vue'
import { portfolioApi } from '../api'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const canPublish = computed(() => [0, 1].includes(auth.user?.role))

const tab = ref('all')
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(12)
const loading = ref(false)
const tag = ref('')

const detailDrawer = ref(false)
const current = ref(null)

const editDialog = ref(false)
const saving = ref(false)
const editForm = reactive({ id: null, title: '', cover: '', techTags: '', description: '', repoUrl: '', demoUrl: '' })

function onTab() { page.value = 1; load(1) }

async function load(p = page.value) {
  page.value = p
  loading.value = true
  try {
    const res = tab.value === 'mine'
      ? await portfolioApi.mine({ page: p, size: size.value })
      : await portfolioApi.list({ page: p, size: size.value, tag: tag.value || undefined })
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

function splitTags(s) { return (s || '').split(',').filter(Boolean).slice(0, 4) }

async function openDetail(p) {
  const res = await portfolioApi.detail(p.id)
  current.value = res.data
  detailDrawer.value = true
  const item = list.value.find(x => x.id === p.id)
  if (item) item.viewCount = res.data?.viewCount ?? item.viewCount
}

async function toggleLike(p) {
  const res = await portfolioApi.toggleLike(p.id)
  p.liked = res.data.liked
  p.likeCount = res.data.likeCount
}

function openEdit(p) {
  Object.assign(editForm, p ? {
    id: p.id, title: p.title, cover: p.cover || '', techTags: p.techTags || '',
    description: p.description || '', repoUrl: p.repoUrl || '', demoUrl: p.demoUrl || ''
  } : { id: null, title: '', cover: '', techTags: '', description: '', repoUrl: '', demoUrl: '' })
  editDialog.value = true
}

async function save() {
  if (!editForm.title.trim()) return ElMessage.warning('请填写作品标题')
  saving.value = true
  try {
    const { id, ...data } = editForm
    if (id) await portfolioApi.update(id, data)
    else await portfolioApi.create(data)
    ElMessage.success(id ? '更新成功' : '发布成功！作品已展示在广场')
    editDialog.value = false
    tab.value = 'mine'
    load(1)
  } finally { saving.value = false }
}

async function removeOne(p) {
  await ElMessageBox.confirm(`确定删除作品「${p.title}」？`, '提示', { type: 'warning' })
  await portfolioApi.remove(p.id)
  ElMessage.success('已删除')
  load()
}

onMounted(() => load(1))
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; }
.toolbar :deep(.el-tabs__header) { margin-bottom: 0; }
.toolbar-right { display: flex; gap: 10px; }
.p-card { cursor: pointer; }
.p-cover { font-size: 40px; text-align: center; margin: 6px 0 10px; }
.p-title { font-weight: 600; font-size: 15px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.p-author { font-size: 12px; color: #909399; margin: 4px 0 8px; }
.p-tags { min-height: 24px; margin-bottom: 8px; }
.p-desc { font-size: 13px; color: #606266; line-height: 1.6; height: 42px; overflow: hidden; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; }
.p-foot { display: flex; justify-content: space-between; align-items: center; margin-top: 10px; }
.p-stat { font-size: 13px; color: #909399; }
.p-mine-ops { margin-top: 8px; text-align: right; }
.d-cover { font-size: 56px; text-align: center; }
.pre { white-space: pre-wrap; color: #606266; font-size: 13px; line-height: 1.7; }
</style>