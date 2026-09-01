<template>
  <el-container style="height: 100%">
    <el-aside width="210px" class="aside">
      <div class="brand">
        <span class="glass-icon sm">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <rect x="3" y="7.5" width="18" height="12.5" rx="2.5" />
            <path d="M9 7.5V6a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2v1.5" />
            <path d="M3 12.8h18" />
            <path d="M9.5 11.2v3M14.5 11.2v3" />
          </svg>
        </span>
        我要工作
      </div>
      <el-menu :default-active="route.path" router background-color="transparent" text-color="rgba(255, 255, 255, 0.64)" active-text-color="#ffffff">
        <el-menu-item index="/dashboard"><el-icon><HomeFilled /></el-icon>首页</el-menu-item>

        <!-- 学生侧（求职者） -->
        <template v-if="isStudent">
          <el-menu-item index="/market"><el-icon><DataAnalysis /></el-icon>岗位市场</el-menu-item>
          <el-menu-item index="/jobs"><el-icon><Briefcase /></el-icon>职位广场</el-menu-item>
          <el-menu-item index="/applied"><el-icon><Document /></el-icon>我的投递</el-menu-item>
          <el-menu-item index="/favorites"><el-icon><Star /></el-icon>我的收藏</el-menu-item>
          <el-menu-item index="/learning"><el-icon><Reading /></el-icon>学习中心</el-menu-item>
          <el-menu-item index="/resume-ai"><el-icon><MagicStick /></el-icon>AI 简历助手</el-menu-item>
          <el-menu-item index="/portfolio"><el-icon><Collection /></el-icon>作品广场</el-menu-item>
        </template>

        <!-- HR 侧（招聘方） -->
        <template v-if="isHr">
          <el-menu-item index="/hr/jobs"><el-icon><Management /></el-icon>职位管理</el-menu-item>
        </template>

        <!-- 通用 -->
        <el-menu-item index="/companies"><el-icon><OfficeBuilding /></el-icon>企业主页</el-menu-item>
        <el-menu-item index="/simulator"><el-icon><Monitor /></el-icon>AI 模拟舱</el-menu-item>
        <el-menu-item index="/salary"><el-icon><TrendCharts /></el-icon>薪资白皮书</el-menu-item>
        <el-menu-item index="/badges"><el-icon><Trophy /></el-icon>我的徽章</el-menu-item>

        <!-- 管理员 -->
        <el-menu-item v-if="isAdmin" index="/admin"><el-icon><Odometer /></el-icon>运营看板</el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-tag v-if="isHr && !isAdmin" type="danger" effect="dark" size="small" style="margin-right:10px">HR 工作台</el-tag>
          <el-tag v-if="isAdmin" type="warning" effect="dark" size="small" style="margin-right:10px">管理员</el-tag>
          {{ route.meta.title || '' }}
        </div>
        <div class="header-right">
          <!-- 通知铃铛 -->
          <el-badge :value="unread" :hidden="!unread" :max="99" class="bell-badge">
            <el-icon :size="20" class="bell" @click="openNotify"><Bell /></el-icon>
          </el-badge>
          <el-tag v-if="auth.points" type="warning" effect="plain" size="large">
            ⚡ 互助积分：{{ auth.points.balance }}
          </el-tag>
          <el-dropdown @command="onCommand">
            <span class="user-info">
              <el-avatar :size="30">{{ (auth.user?.username || 'U')[0].toUpperCase() }}</el-avatar>
              <span>{{ auth.user?.username }}</span>
              <el-tag size="small" type="info">{{ auth.roleName }}</el-tag>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main style="padding: 16px; overflow-y: auto; scroll-behavior: smooth">
        <router-view />
      </el-main>
    </el-container>
  </el-container>

  <!-- 通知抽屉 -->
  <el-drawer v-model="notifyDrawer" title="🔔 站内通知" size="420px">
    <div class="notify-toolbar">
      <span class="notify-count">共 {{ notifyTotal }} 条 · 未读 {{ unread }}</span>
      <el-button size="small" text type="primary" @click="markAllRead" :disabled="!unread">全部已读</el-button>
    </div>
    <div v-if="notifications.length === 0" class="notify-empty">暂无通知</div>
    <div
      v-for="n in notifications"
      :key="n.id"
      class="notify-item"
      :class="{ unread: !n.isRead }"
      @click="readOne(n)"
    >
      <div class="n-icon">{{ ['📢', '📨', '📅'][n.type] || '📢' }}</div>
      <div class="n-body">
        <div class="n-title">{{ n.title }}</div>
        <div class="n-content">{{ n.content }}</div>
        <div class="n-time">{{ formatTime(n.createdAt) }}</div>
      </div>
      <span v-if="!n.isRead" class="n-dot"></span>
    </div>
    <div class="notify-pager" v-if="notifyTotal > 10">
      <el-pagination
        layout="prev, pager, next"
        small
        :total="notifyTotal"
        :page-size="10"
        :current-page="notifyPage"
        @current-change="p => { notifyPage = p; loadNotifications() }"
      />
    </div>
  </el-drawer>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { notifyApi } from '../api'
import { DataAnalysis, Reading, MagicStick, Management, Star, Collection, OfficeBuilding, Odometer, Bell } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

// 学生/校友（求职者视角）；管理员也可浏览学生功能
const isStudent = computed(() => [0, 1, 9].includes(auth.user?.role))
// HR（招聘方视角）；管理员也可管理
const isHr = computed(() => [2, 9].includes(auth.user?.role))
const isAdmin = computed(() => auth.user?.role === 9)

// ===== 通知 =====
const unread = ref(0)
const notifyDrawer = ref(false)
const notifications = ref([])
const notifyTotal = ref(0)
const notifyPage = ref(1)
let timer = null

async function refreshUnread() {
  try {
    const res = await notifyApi.unreadCount()
    unread.value = res.data ?? 0
  } catch (e) { /* 静默 */ }
}

async function loadNotifications() {
  try {
    const res = await notifyApi.myNotifications({ page: notifyPage.value, size: 10 })
    notifications.value = res.data?.records || []
    notifyTotal.value = res.data?.total ?? 0
  } catch (e) { /* 静默 */ }
}

function openNotify() {
  notifyDrawer.value = true
  loadNotifications()
}

async function readOne(n) {
  if (!n.isRead) {
    try {
      await notifyApi.markRead(n.id)
      n.isRead = 1
      unread.value = Math.max(0, unread.value - 1)
    } catch (e) { /* 静默 */ }
  }
}

async function markAllRead() {
  try {
    await notifyApi.markAllRead()
    notifications.value.forEach(n => (n.isRead = 1))
    unread.value = 0
  } catch (e) { /* 静默 */ }
}

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').slice(0, 16)
}

onMounted(() => {
  if (!auth.user) auth.fetchUserInfo().catch(() => {})
  refreshUnread()
  timer = setInterval(refreshUnread, 30000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})

async function onCommand(cmd) {
  if (cmd === 'logout') {
    await auth.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
/* ===== 侧边栏：半透明深色玻璃 ===== */
.aside {
  background: rgba(10, 14, 35, 0.55);
  backdrop-filter: blur(20px) saturate(1.4);
  -webkit-backdrop-filter: blur(20px) saturate(1.4);
  border-right: 1px solid rgba(255, 255, 255, 0.1);
}
.brand {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}
.brand .glass-icon { color: #fff; }

/* ===== 菜单：透明底 + 玻璃 active 项 ===== */
.aside :deep(.el-menu) {
  border-right: none;
  background: transparent;
}
.aside :deep(.el-menu-item) {
  color: var(--g-text-secondary);
  height: 44px;
  line-height: 44px;
  margin: 3px 10px;
  border-radius: 10px;
  position: relative;
  transition: color 0.25s ease, background 0.25s ease;
}
.aside :deep(.el-menu-item:hover) {
  color: var(--g-text-primary);
  background: rgba(255, 255, 255, 0.06);
}
.aside :deep(.el-menu-item.is-active) {
  color: #fff;
  background: rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.16);
}
/* active 项左侧 3px 渐变指示条 */
.aside :deep(.el-menu-item.is-active)::before {
  content: '';
  position: absolute;
  left: 0;
  top: 22%;
  bottom: 22%;
  width: 3px;
  border-radius: 2px;
  background: linear-gradient(180deg, var(--g-accent) 0%, var(--g-fuchsia) 100%);
}
/* 微交互：悬停时底部渐变下划线从左展开（active 项已有指示条，不显示） */
.aside :deep(.el-menu-item)::after {
  content: '';
  position: absolute;
  left: 42px;
  right: 38%;
  bottom: 7px;
  height: 2px;
  border-radius: 1px;
  background: linear-gradient(90deg, var(--g-accent), transparent);
  transform: scaleX(0);
  transform-origin: left;
  transition: transform 0.3s ease;
}
.aside :deep(.el-menu-item:hover)::after { transform: scaleX(1); }
.aside :deep(.el-menu-item.is-active)::after { transform: scaleX(0); }

/* ===== 顶栏：半透明玻璃 + 底部高光边 ===== */
.header {
  background: rgba(10, 14, 35, 0.45);
  backdrop-filter: blur(18px) saturate(1.4);
  -webkit-backdrop-filter: blur(18px) saturate(1.4);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
  z-index: 5;
}
.header-left {
  font-size: 16px;
  font-weight: 600;
  display: flex;
  align-items: center;
  color: #fff;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 18px;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #fff;
}
.bell {
  cursor: pointer;
  color: rgba(255, 255, 255, 0.85);
  transition: color 0.2s;
}
.bell:hover { color: #fff; }

/* ===== 通知抽屉内容（抽屉外壳跟随全局 el-drawer 玻璃覆盖） ===== */
.notify-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.notify-count { font-size: 13px; color: var(--g-text-secondary); }
.notify-empty { text-align: center; color: var(--g-text-muted); padding: 40px 0; }
.notify-item {
  display: flex;
  gap: 12px;
  padding: 12px 14px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.04);
  border-radius: 10px;
  margin-bottom: 10px;
  cursor: pointer;
  position: relative;
  transition: all 0.2s;
}
.notify-item:hover { border-color: rgba(165, 180, 252, 0.45); background: rgba(255, 255, 255, 0.08); }
.notify-item.unread { background: rgba(99, 102, 241, 0.14); border-color: rgba(129, 140, 248, 0.35); }
.n-icon { font-size: 22px; }
.n-title { font-size: 14px; font-weight: 600; color: var(--g-text-primary); }
.n-content { font-size: 13px; color: var(--g-text-secondary); margin-top: 4px; line-height: 1.6; }
.n-time { font-size: 12px; color: var(--g-text-muted); margin-top: 6px; }
.n-dot {
  width: 8px; height: 8px;
  border-radius: 50%;
  background: #f56c6c;
  position: absolute;
  top: 12px; right: 12px;
}
.notify-pager { display: flex; justify-content: center; margin-top: 10px; }
</style>