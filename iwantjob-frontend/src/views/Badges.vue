<template>
  <div>
    <!-- 我的徽章 -->
    <el-card shadow="never" style="margin-bottom:16px">
      <template #header>
        <div class="card-header-flex">
          <span>🏅 我的徽章（{{ myBadges.length }}/{{ templates.length }}）</span>
          <el-tooltip content="徽章哈希上链锁定，任何篡改都会导致校验失败，可写入简历作为可信背书">
            <el-tag type="success" effect="plain" size="small">🔒 防篡改保护中</el-tag>
          </el-tooltip>
        </div>
      </template>

      <el-row v-if="myBadges.length" :gutter="16">
        <el-col :span="6" v-for="b in myBadges" :key="b.id">
          <div class="badge-card" :class="rarityClass(b.rarity)">
            <div class="badge-icon">{{ rarityIcon(b.rarity) }}</div>
            <h4>{{ b.name }}</h4>
            <p>{{ b.description }}</p>
            <div class="badge-meta">
              <el-tag size="small" :type="['info', 'warning', 'danger'][b.rarity]">{{ b.rarityDesc }}</el-tag>
              <span class="finger" v-if="b.fingerprint">指纹 {{ b.fingerprint }}…</span>
            </div>
            <div class="earned">获得于 {{ (b.earnedAt || '').slice(0, 10) }}</div>
          </div>
        </el-col>
      </el-row>
      <el-empty v-else description="还没有徽章，去完成模拟舱演练、贡献薪资数据、帮助他人来解锁吧！" :image-size="80" />
    </el-card>

    <!-- 全部徽章图鉴 -->
    <el-card shadow="never">
      <template #header>
        <div class="card-header-flex">
          <span>🎖️ 徽章图鉴（达成条件即自动解锁）</span>
          <el-button v-if="isAdmin" type="primary" size="small" @click="showTemplateDialog = true">+ 新建徽章模板</el-button>
        </div>
      </template>
      <el-table :data="templates" stripe>
        <el-table-column label="徽章" min-width="200">
          <template #default="{ row }">
            <span style="font-size:16px;margin-right:6px">{{ rarityIcon(row.rarity) }}</span>
            <b>{{ row.name }}</b>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="220" />
        <el-table-column label="达成条件" min-width="140">
          <template #default="{ row }">{{ row.conditionDesc }} × {{ row.threshold }}</template>
        </el-table-column>
        <el-table-column label="稀有度" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="['info', 'warning', 'danger'][row.rarity]">{{ row.rarityDesc }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" fixed="right">
          <template #default="{ row }">
            <el-tag v-if="earned(row.id)" type="success" size="small">已获得</el-tag>
            <el-tag v-else type="info" size="small" effect="plain">未解锁</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 管理员：新建徽章模板弹窗 -->
    <el-dialog v-model="showTemplateDialog" title="新建徽章模板" width="520px">
      <el-form :model="tplForm" label-width="90px">
        <el-form-item label="徽章名称" required>
          <el-input v-model="tplForm.name" maxlength="50" placeholder="如：面经分享达人" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="tplForm.description" type="textarea" :rows="2" maxlength="200" placeholder="如：累计分享 5 篇面经" />
        </el-form-item>
        <el-form-item label="条件类型" required>
          <el-select v-model="tplForm.conditionType" style="width:100%">
            <el-option :value="0" label="分享面经" />
            <el-option :value="1" label="帮助他人" />
            <el-option :value="2" label="薪资贡献" />
            <el-option :value="3" label="模拟舱完成" />
            <el-option :value="4" label="项目评价" />
          </el-select>
        </el-form-item>
        <el-form-item label="达成阈值" required>
          <el-input-number v-model="tplForm.threshold" :min="1" style="width:160px" />
        </el-form-item>
        <el-form-item label="稀有度" required>
          <el-select v-model="tplForm.rarity" style="width:160px">
            <el-option :value="0" label="普通" />
            <el-option :value="1" label="稀有" />
            <el-option :value="2" label="史诗" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showTemplateDialog = false">取消</el-button>
        <el-button type="primary" :loading="tplSaving" @click="createTemplate">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { userApi, badgeApi, adminApi } from '../api'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const isAdmin = computed(() => auth.user?.role === 9)

const myBadges = ref([])
const templates = ref([])
const showTemplateDialog = ref(false)
const tplSaving = ref(false)
const tplForm = reactive({ name: '', description: '', conditionType: 0, threshold: 1, rarity: 0 })

const earned = id => myBadges.value.some(b => b.badgeId === id)
const rarityIcon = r => ['🎖️', '🏆', '👑'][r] || '🎖️'
const rarityClass = r => ['r-common', 'r-rare', 'r-epic'][r] || 'r-common'

async function loadTemplates() {
  try {
    const res = await badgeApi.templates()
    templates.value = res.data || []
  } catch (e) { /* 静默 */ }
}

async function createTemplate() {
  if (!tplForm.name.trim()) return ElMessage.warning('请填写徽章名称')
  tplSaving.value = true
  try {
    await adminApi.createBadgeTemplate(tplForm)
    ElMessage.success('徽章模板创建成功')
    showTemplateDialog.value = false
    Object.assign(tplForm, { name: '', description: '', conditionType: 0, threshold: 1, rarity: 0 })
    loadTemplates()
  } catch (e) {
    // 错误已由拦截器弹出
  } finally {
    tplSaving.value = false
  }
}

onMounted(async () => {
  const [mine] = await Promise.all([
    userApi.myBadges().catch(() => ({ data: [] })),
    loadTemplates()
  ])
  myBadges.value = mine.data || []
})
</script>

<style scoped>
.badge-card {
  border: 1px solid #dcdfe6; border-radius: 10px; padding: 18px; text-align: center;
  transition: transform .2s, box-shadow .2s; background: #fff;
}
.badge-card:hover { transform: translateY(-3px); box-shadow: 0 4px 12px rgba(0,0,0,.08); }
.badge-icon { font-size: 44px; }
.badge-card h4 { margin: 8px 0 4px; }
.badge-card p { font-size: 12px; color: #909399; min-height: 36px; line-height: 1.6; }
.badge-meta { display: flex; align-items: center; justify-content: center; gap: 8px; margin-top: 6px; }
.finger { font-family: Consolas, monospace; font-size: 11px; color: #c0c4cc; }
.earned { font-size: 11px; color: #c0c4cc; margin-top: 6px; }
.r-common { border-color: #dcdfe6; }
.r-rare { border-color: #e6a23c; background: #fdf6ec; }
.r-epic { border-color: #f56c6c; background: #fef0f0; }
</style>
