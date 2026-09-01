<template>
  <div class="hr-jobs">
    <!-- 概览 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="8">
        <div class="h-stat violet">
          <div class="hs-icon">📋</div>
          <div>
            <div class="hs-value">{{ publishedTotal }}</div>
            <div class="hs-label">我发布的职位</div>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="h-stat blue">
          <div class="hs-icon">👥</div>
          <div>
            <div class="hs-value">{{ totalApplications }}</div>
            <div class="hs-label">收到投递总数</div>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="h-stat fuchsia">
          <div class="hs-icon">🔥</div>
          <div>
            <div class="hs-value">{{ pendingCount }}</div>
            <div class="hs-label">待处理投递</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 职位列表 -->
    <el-card shadow="never" style="margin-top:16px">
      <template #header>
        <div class="card-head">
          <span>📌 我发布的职位（点击查看候选人）</span>
          <el-button type="primary" size="small" @click="showPublish = true">+ 发布新职位</el-button>
        </div>
      </template>
      <el-table :data="jobs" stripe @row-click="row => openCandidates(row)" class="job-table">
        <el-table-column prop="title" label="职位" min-width="160" />
        <el-table-column prop="companyName" label="公司" width="110" />
        <el-table-column label="类型" width="76">
          <template #default="{ row }">
            <el-tag :type="['info', 'success', 'warning'][row.jobType]" size="small">{{ ['实习', '校招', '社招'][row.jobType] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="城市" width="76" />
        <el-table-column prop="salaryRange" label="薪资" width="110" />
        <el-table-column label="浏览量" width="80" prop="viewCount" />
        <el-table-column label="投递数" width="90">
          <template #default="{ row }">
            <el-tag effect="plain" :type="row.applicationCount > 0 ? 'danger' : 'info'" size="small">
              {{ row.applicationCount }} 人
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" text @click.stop="openCandidates(row)">查看候选人</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 候选人抽屉 -->
    <el-drawer v-model="candDrawer" :title="`候选人管理 · ${activeJob?.title || ''}`" size="min(820px, 95vw)">
      <!-- 候选人列表 -->
      <div v-if="!viewingCandidate" v-loading="candLoading">
        <div class="cand-tip">
          <el-icon><InfoFilled /></el-icon>
          点击候选人行查看完整档案（基本资料 + 徽章背书 + 投递简历）
        </div>
        <el-table :data="candidates" stripe @row-click="row => openDetail(row)" class="cand-table">
          <el-table-column label="候选人" min-width="140">
            <template #default="{ row }">
              <div class="cand-name">
                <el-avatar :size="32" style="background:#7c3aed">{{ (row.realName || row.username || '?')[0] }}</el-avatar>
                <div>
                  <div class="cn">{{ row.realName || row.username }}</div>
                  <div class="cs">{{ row.username }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="school" label="学校" width="130" />
          <el-table-column prop="major" label="专业" width="100" />
          <el-table-column prop="graduationYear" label="毕业年份" width="90" />
          <el-table-column label="投递时间" width="150">
            <template #default="{ row }">{{ (row.appliedAt || '').replace('T', ' ').slice(0, 16) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="96">
            <template #default="{ row }">
              <el-tag :type="statusMeta(row.status).type" size="small">{{ statusMeta(row.status).label }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
        <div class="pager" v-if="candTotal > 10">
          <el-pagination
            layout="prev, pager, next"
            :total="candTotal"
            :page-size="10"
            :current-page="candPage"
            @current-change="p => { candPage = p; loadCandidates() }"
          />
        </div>
      </div>

      <!-- 候选人详情 -->
      <div v-else class="cand-detail">
        <el-button text @click="viewingCandidate = null" style="margin-bottom:12px">← 返回候选人列表</el-button>

        <!-- 基本信息卡 -->
        <div class="d-hero">
          <el-avatar :size="56" style="background:linear-gradient(135deg,#7c3aed,#a78bfa);font-size:24px">
            {{ (detail.realName || detail.username || '?')[0] }}
          </el-avatar>
          <div class="d-hero-info">
            <div class="d-name">
              {{ detail.realName || detail.username }}
              <el-tag v-if="detail.graduationYear" size="small" effect="plain">{{ detail.graduationYear }} 届</el-tag>
            </div>
            <div class="d-sub">{{ detail.school }} · {{ detail.major }} · 投递于 {{ (detail.appliedAt || '').replace('T', ' ').slice(0, 16) }}</div>
            <div class="d-status-line">
              当前状态：
              <el-tag :type="statusMeta(detail.status).type">{{ statusMeta(detail.status).label }}</el-tag>
            </div>
          </div>
          <div class="d-score" v-if="detail.resumeAiScore != null">
            <div class="ds-num">{{ detail.resumeAiScore }}</div>
            <div class="ds-label">简历 AI 分</div>
          </div>
        </div>

        <!-- 技能/简介 -->
        <div class="d-section">
          <div class="d-sec-title">🛠 技能标签</div>
          <div class="d-skills">
            <span v-for="s in (detail.skills || '').split(',').filter(Boolean)" :key="s" class="skill-chip">{{ s }}</span>
            <span v-if="!(detail.skills)" class="d-empty-inline">未填写</span>
          </div>
        </div>
        <div class="d-section">
          <div class="d-sec-title">👤 个人简介</div>
          <div class="d-bio">{{ detail.bio || '未填写' }}</div>
        </div>

        <!-- 徽章背书 -->
        <div class="d-section">
          <div class="d-sec-title">🏅 徽章背书（防篡改可验证）</div>
          <div v-if="detail.badges && detail.badges.length" class="d-badges">
            <div v-for="b in detail.badges" :key="b.name" class="badge-chip" :class="['rare-0', 'rare-1', 'rare-2'][b.rarity]">
              <span class="b-icon">🏅</span>
              <div>
                <div class="b-name">{{ b.name }} <span class="b-fp" v-if="b.fingerprint">#{{ b.fingerprint }}</span></div>
                <div class="b-meta">{{ ['普通', '稀有', '史诗'][b.rarity] }} · {{ ['未铸造', '链上锁定'][b.fingerprint ? 1 : 0] }}</div>
              </div>
            </div>
          </div>
          <div v-else class="d-empty-inline">暂无徽章</div>
        </div>

        <!-- 求职信 -->
        <div class="d-section" v-if="detail.coverLetter">
          <div class="d-sec-title">💬 求职信</div>
          <div class="d-cover">{{ detail.coverLetter }}</div>
        </div>

        <!-- 简历内容 -->
        <div class="d-section" v-if="parsedResume">
          <div class="d-sec-title">📄 投递简历：{{ detail.resumeTitle }}</div>
          <div class="d-resume">
            <div v-if="parsedResume.basic" class="dr-block">
              <div class="dr-label">基本信息</div>
              <div class="dr-basic">
                <span v-if="parsedResume.basic.name">{{ parsedResume.basic.name }}</span>
                <span v-if="parsedResume.basic.school">{{ parsedResume.basic.school }}</span>
                <span v-if="parsedResume.basic.major">{{ parsedResume.basic.major }}</span>
                <span v-if="parsedResume.basic.graduationYear">{{ parsedResume.basic.graduationYear }} 届</span>
              </div>
            </div>
            <div v-if="parsedResume.skills?.length" class="dr-block">
              <div class="dr-label">技能</div>
              <div class="dr-skills"><span v-for="s in parsedResume.skills" :key="s" class="skill-chip sm">{{ s }}</span></div>
            </div>
            <div v-if="parsedResume.projects?.length" class="dr-block">
              <div class="dr-label">项目经历</div>
              <div v-for="pj in parsedResume.projects" :key="pj.name" class="dr-item">
                <div class="dri-title">{{ pj.name }} <span class="dri-role">{{ pj.role }}</span></div>
                <div class="dri-desc">{{ pj.desc }}</div>
              </div>
            </div>
            <div v-if="parsedResume.internships?.length" class="dr-block">
              <div class="dr-label">实习经历</div>
              <div v-for="it in parsedResume.internships" :key="it.company" class="dr-item">
                <div class="dri-title">{{ it.company }} <span class="dri-role">{{ it.role }}</span></div>
                <div class="dri-desc">{{ it.desc }}</div>
              </div>
            </div>
          </div>
        </div>
        <div class="d-section" v-else>
          <div class="d-sec-title">📄 投递简历</div>
          <div class="d-empty-inline">该投递未附带简历</div>
        </div>

        <!-- 状态流转 -->
        <div class="d-action">
          <div class="d-sec-title">⚙️ 推进流程</div>
          <div class="d-flow">
            <div
              v-for="s in statusFlow"
              :key="s.value"
              class="flow-step"
              :class="{
                active: detail.status === s.value && detail.status !== 4,
                rejected: detail.status === 4 && s.value === 4,
                done: detail.status > s.value && detail.status !== 4 && s.value !== 4
              }"
              @click="updateStatus(s.value)"
            >
              <div class="fs-dot">{{ ['投递', '初筛', '面试', '录用', '拒绝'][s.value] }}</div>
            </div>
          </div>
          <el-input
            v-model="remark"
            type="textarea"
            :rows="2"
            placeholder="备注（将同步给候选人可见），例如：Java 基础扎实，约下周三下午面试"
            style="margin-top:12px"
          />
        </div>
      </div>
    </el-drawer>

    <!-- 发布职位弹窗 -->
    <el-dialog v-model="showPublish" title="发布新职位" width="600px">
      <el-form :model="pubForm" label-width="90px">
        <el-form-item label="职位标题" required>
          <el-input v-model="pubForm.title" placeholder="如：Java 后端开发工程师" />
        </el-form-item>
        <el-form-item label="公司名称" required>
          <el-select
            v-model="pubForm.companyId"
            filterable allow-create default-first-option
            placeholder="选择已入驻企业，或输入新公司名"
            style="width:100%"
            @change="onCompanyPick"
          >
            <el-option v-for="c in companyOptions" :key="c.id" :value="c.id" :label="c.name" />
          </el-select>
          <div v-if="pubForm.companyName && !pubForm.companyId" class="pub-company-hint">
            「{{ pubForm.companyName }}」未入驻企业主页，发布后可在企业主页申请认领
          </div>
        </el-form-item>
        <el-row>
          <el-col :span="12">
            <el-form-item label="职位类型" required>
              <el-select v-model="pubForm.jobType" style="width:100%">
                <el-option :value="0" label="实习" />
                <el-option :value="1" label="校招" />
                <el-option :value="2" label="社招" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="薪资范围">
              <el-input v-model="pubForm.salaryRange" placeholder="如：20k-35k" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="工作城市">
          <el-input v-model="pubForm.location" placeholder="如：北京" />
        </el-form-item>
        <el-form-item label="职位描述">
          <el-input v-model="pubForm.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="任职要求">
          <el-input v-model="pubForm.requirements" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPublish = false">取消</el-button>
        <el-button type="primary" :loading="publishing" @click="publish">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { InfoFilled } from '@element-plus/icons-vue'
import { jobApi, companyApi } from '../api'

const jobs = ref([])
const publishedTotal = ref(0)
const candDrawer = ref(false)
const activeJob = ref(null)
const candidates = ref([])
const candTotal = ref(0)
const candPage = ref(1)
const candLoading = ref(false)
const viewingCandidate = ref(null)
const detail = ref({})
const remark = ref('')
const showPublish = ref(false)
const publishing = ref(false)
const companyOptions = ref([])
const pendingCount = ref(0)
const pubForm = ref({
  title: '', companyName: '', companyId: null, jobType: 1, salaryRange: '', location: '',
  description: '', requirements: ''
})

const totalApplications = computed(() => jobs.value.reduce((s, j) => s + (j.applicationCount || 0), 0))

const statusFlow = [
  { value: 0 }, { value: 1 }, { value: 2 }, { value: 3 }, { value: 4 }
]

function statusMeta(s) {
  return {
    0: { label: '已投递', type: 'info' },
    1: { label: '初筛通过', type: 'primary' },
    2: { label: '面试中', type: 'warning' },
    3: { label: '已录用', type: 'success' },
    4: { label: '已拒绝', type: 'danger' }
  }[s] || { label: '未知', type: 'info' }
}

const parsedResume = computed(() => {
  if (!detail.value?.resumeContentJson) return null
  try {
    return JSON.parse(detail.value.resumeContentJson)
  } catch (e) {
    return null
  }
})

onMounted(() => {
  loadJobs()
  loadCompanies()
})

async function loadJobs() {
  try {
    const res = await jobApi.myPublished({ page: 1, size: 50 })
    jobs.value = res.data?.records || []
    publishedTotal.value = res.data?.total ?? 0
    await refreshPendingCount()
  } catch (e) {
    ElMessage.error('职位加载失败')
  }
}

// 真实统计「待处理投递」：并行拉取各职位候选人，按 status=0 汇总（不随抽屉操作漂移）
async function refreshPendingCount() {
  const withApps = jobs.value.filter(j => j.applicationCount > 0)
  if (!withApps.length) {
    pendingCount.value = 0
    return
  }
  const results = await Promise.all(
    withApps.map(j => jobApi.jobApplications(j.id, { page: 1, size: 100 }).catch(() => null))
  )
  pendingCount.value = results.reduce(
    (s, r) => s + (r?.data?.records || []).filter(c => c.status === 0).length, 0
  )
}

async function loadCompanies() {
  try {
    const res = await companyApi.list()
    companyOptions.value = res.data || []
  } catch (e) { /* 静默：企业下拉非关键路径 */ }
}

// 选择已有企业 → 同步 companyName；手输新公司名（allow-create）→ companyId 置空
function onCompanyPick(val) {
  if (val == null) return
  if (typeof val === 'number') {
    const c = companyOptions.value.find(x => x.id === val)
    if (c) pubForm.value.companyName = c.name
  } else {
    pubForm.value.companyName = val
    pubForm.value.companyId = null
  }
}

async function openCandidates(job) {
  activeJob.value = job
  viewingCandidate.value = null
  candPage.value = 1
  candDrawer.value = true
  await loadCandidates()
}

async function loadCandidates() {
  if (!activeJob.value) return
  candLoading.value = true
  try {
    const res = await jobApi.jobApplications(activeJob.value.id, { page: candPage.value, size: 10 })
    candidates.value = res.data?.records || []
    candTotal.value = res.data?.total ?? 0
  } catch (e) {
    ElMessage.error('候选人加载失败')
  } finally {
    candLoading.value = false
  }
}

async function openDetail(row) {
  try {
    const res = await jobApi.candidateDetail(row.id)
    detail.value = res.data || {}
    remark.value = detail.value.hrRemark || ''
    viewingCandidate.value = row.id
  } catch (e) {
    ElMessage.error('候选人详情加载失败')
  }
}

async function updateStatus(status) {
  if (!viewingCandidate.value) return
  const labels = ['标记为已投递', '通过初筛', '进入面试', '发Offer录用', '拒绝该候选人']
  const confirmTypes = { 4: 'warning' }
  try {
    await ElMessageBox.confirm(
      `确定将候选人「${detail.value.realName || detail.value.username}」${labels[status]}？`,
      '状态流转确认',
      { type: confirmTypes[status] || 'info' }
    )
  } catch (e) {
    return
  }
  try {
    await jobApi.updateApplicationStatus(viewingCandidate.value, {
      status,
      hrRemark: remark.value || null
    })
    ElMessage.success(`已${labels[status]}`)
    detail.value.status = status
    // 同步刷新列表与统计
    loadCandidates()
    refreshPendingCount()
  } catch (e) {
    ElMessage.error('状态更新失败')
  }
}

async function publish() {
  if (!pubForm.value.title || !pubForm.value.companyName) {
    ElMessage.warning('请填写职位标题和公司名称')
    return
  }
  publishing.value = true
  try {
    await jobApi.publish(pubForm.value)
    ElMessage.success(pubForm.value.companyId ? '发布成功，已同步至企业主页' : '发布成功')
    showPublish.value = false
    pubForm.value = { title: '', companyName: '', companyId: null, jobType: 1, salaryRange: '', location: '', description: '', requirements: '' }
    loadJobs()
  } catch (e) {
    ElMessage.error('发布失败：' + (e.message || ''))
  } finally {
    publishing.value = false
  }
}
</script>

<style scoped>
.stat-row { margin-bottom: 4px; }
.h-stat {
  display: flex; align-items: center; gap: 14px;
  background: #fff; border-radius: 14px; padding: 18px 20px;
  box-shadow: 0 4px 16px rgba(30, 20, 80, 0.08);
  transition: transform .25s;
}
.h-stat:hover { transform: translateY(-3px); }
.hs-icon { width: 46px; height: 46px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 22px; }
.h-stat.violet .hs-icon { background: linear-gradient(135deg, #ede9fe, #ddd6fe); }
.h-stat.blue .hs-icon { background: linear-gradient(135deg, #dbeafe, #bfdbfe); }
.h-stat.fuchsia .hs-icon { background: linear-gradient(135deg, #fae8ff, #f5d0fe); }
.hs-value { font-size: 24px; font-weight: 700; color: #1f2337; line-height: 1.1; }
.hs-label { font-size: 12px; color: #909399; margin-top: 3px; }
.card-head { display: flex; justify-content: space-between; align-items: center; }
.job-table :deep(tbody tr) { cursor: pointer; }
.cand-tip {
  display: flex; align-items: center; gap: 6px;
  font-size: 13px; color: #909399; margin-bottom: 12px;
}
.cand-table :deep(tbody tr) { cursor: pointer; }
.cand-name { display: flex; align-items: center; gap: 10px; }
.cn { font-weight: 600; font-size: 14px; }
.cs { font-size: 12px; color: #c0c4cc; }
.pager { display: flex; justify-content: center; margin-top: 14px; }
/* 详情 */
.d-hero {
  display: flex; align-items: center; gap: 16px;
  background: linear-gradient(135deg, #f5f3ff, #ede9fe);
  border-radius: 14px; padding: 18px 20px; margin-bottom: 16px;
}
.d-hero-info { flex: 1; }
.d-name { font-size: 18px; font-weight: 700; color: #1f2337; display: flex; align-items: center; gap: 8px; }
.d-sub { font-size: 13px; color: #6b7280; margin-top: 4px; }
.d-status-line { font-size: 13px; margin-top: 8px; display: flex; align-items: center; gap: 6px; color: #444; }
.d-score { text-align: center; }
.ds-num { font-size: 30px; font-weight: 800; color: #7c3aed; line-height: 1; }
.ds-label { font-size: 12px; color: #909399; margin-top: 4px; }
.d-section { margin-bottom: 18px; }
.d-sec-title { font-size: 14px; font-weight: 700; color: #1f2337; margin-bottom: 10px; }
.d-skills { display: flex; flex-wrap: wrap; gap: 8px; }
.skill-chip {
  background: #ede9fe; color: #6d28d9;
  padding: 5px 14px; border-radius: 999px; font-size: 13px; font-weight: 500;
}
.skill-chip.sm { padding: 3px 10px; font-size: 12px; background: #f5f3ff; }
.d-bio { font-size: 14px; color: #555; line-height: 1.8; background: #fafafa; border-radius: 10px; padding: 12px 14px; }
.d-empty-inline { font-size: 13px; color: #c0c4cc; }
.d-badges { display: flex; flex-wrap: wrap; gap: 10px; }
.badge-chip {
  display: flex; align-items: center; gap: 10px;
  border-radius: 12px; padding: 10px 14px; border: 1px solid;
}
.badge-chip.rare-0 { background: #f8fafc; border-color: #e2e8f0; }
.badge-chip.rare-1 { background: #eff6ff; border-color: #bfdbfe; }
.badge-chip.rare-2 { background: #faf5ff; border-color: #e9d5ff; }
.b-icon { font-size: 22px; }
.b-name { font-size: 13px; font-weight: 700; color: #1f2337; }
.b-fp { font-size: 11px; color: #a78bfa; font-family: monospace; }
.b-meta { font-size: 11px; color: #909399; margin-top: 2px; }
.d-cover { font-size: 14px; color: #555; line-height: 1.8; background: #fffbeb; border-radius: 10px; padding: 12px 14px; border: 1px dashed #fde68a; }
.d-resume { border: 1px solid #f0f0f5; border-radius: 12px; padding: 16px; }
.dr-block { margin-bottom: 14px; }
.dr-block:last-child { margin-bottom: 0; }
.dr-label { font-size: 12px; color: #909399; margin-bottom: 8px; font-weight: 600; }
.dr-basic { display: flex; flex-wrap: wrap; gap: 12px; font-size: 14px; color: #333; }
.dr-skills { display: flex; flex-wrap: wrap; gap: 6px; }
.dr-item { padding: 8px 0; border-bottom: 1px dashed #f0f0f5; }
.dr-item:last-child { border-bottom: none; }
.dri-title { font-size: 14px; font-weight: 600; color: #1f2337; }
.dri-role { font-size: 12px; color: #7c3aed; font-weight: normal; margin-left: 8px; }
.dri-desc { font-size: 13px; color: #6b7280; margin-top: 4px; line-height: 1.7; }
.d-action { background: #fafafa; border-radius: 12px; padding: 14px; }
.d-flow { display: flex; gap: 8px; }
.flow-step {
  flex: 1; text-align: center; cursor: pointer;
  border: 1px solid #e5e7eb; border-radius: 10px; padding: 10px 0;
  font-size: 13px; color: #6b7280; transition: all .2s; background: #fff;
}
.flow-step:hover { border-color: #a78bfa; color: #7c3aed; }
.flow-step.active { background: #ede9fe; border-color: #7c3aed; color: #6d28d9; font-weight: 700; }
.flow-step.done { background: #f0fdf4; border-color: #86efac; color: #16a34a; }
.flow-step.rejected { background: #fef2f2; border-color: #f87171; color: #dc2626; font-weight: 700; }
.pub-company-hint { font-size: 12px; color: #909399; margin-top: 4px; line-height: 1.5; }
</style>