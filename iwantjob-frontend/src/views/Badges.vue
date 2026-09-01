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
      <template #header>🎖️ 徽章图鉴（达成条件即自动解锁）</template>
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { userApi, badgeApi } from '../api'

const myBadges = ref([])
const templates = ref([])

const earned = id => myBadges.value.some(b => b.badgeId === id)
const rarityIcon = r => ['🎖️', '🏆', '👑'][r] || '🎖️'
const rarityClass = r => ['r-common', 'r-rare', 'r-epic'][r] || 'r-common'

onMounted(async () => {
  const [mine, all] = await Promise.all([
    userApi.myBadges().catch(() => ({ data: [] })),
    badgeApi.templates()
  ])
  myBadges.value = mine.data || []
  templates.value = all.data || []
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
