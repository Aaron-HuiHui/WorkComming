<template>
  <div class="app-shell">
    <!-- ===== 侧边栏:hover 展开 ===== -->
    <aside class="sidebar" :class="{ expanded: sidebarOpen }" @mouseenter="sidebarOpen = true" @mouseleave="sidebarOpen = false">
     <div class="sb-inner">
      <div class="brand" @click="router.push('/dashboard')">
        <span class="brand-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <rect x="3" y="7.5" width="18" height="12.5" rx="2.5" />
            <path d="M9 7.5V6a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2v1.5" />
            <path d="M3 12.8h18" />
            <path d="M9.5 11.2v3M14.5 11.2v3" />
          </svg>
        </span>
        <transition name="fade-text">
          <span v-if="sidebarOpen" class="brand-name">我要工作</span>
        </transition>
      </div>

      <nav class="nav-scroll">
        <div v-for="group in groups" :key="group.label" class="nav-group">
          <transition name="fade-text">
            <div v-if="sidebarOpen" class="group-label">{{ group.label }}</div>
          </transition>
          <router-link
            v-for="item in group.items"
            :key="item.path"
            :to="item.path"
            class="nav-item"
            :class="{ active: isActive(item.path) }"
          >
            <span class="nav-icon"><el-icon><component :is="item.icon" /></el-icon></span>
            <transition name="fade-text">
              <span v-if="sidebarOpen" class="nav-text">{{ item.title }}</span>
            </transition>
            <span v-if="isActive(item.path)" class="active-dot"></span>
          </router-link>
        </div>
      </nav>

      <div class="sidebar-foot">
        <el-dropdown @command="onCommand" placement="top-start">
          <div class="nav-item user-item">
            <span class="nav-avatar">{{ (auth.user?.username || 'U')[0].toUpperCase() }}</span>
            <transition name="fade-text">
              <span v-if="sidebarOpen" class="nav-text">{{ auth.user?.username }}</span>
            </transition>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item disabled>
                {{ auth.user?.username }} · {{ auth.roleName }}
              </el-dropdown-item>
              <el-dropdown-item divided command="toggle-theme">
                {{ isDark ? '🌙 切换浅色模式' : '☀️ 切换深色模式' }}
              </el-dropdown-item>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
     </div>
    </aside>

    <!-- ===== 主区 ===== -->
    <div class="main-col">
      <header class="topbar">
        <div class="topbar-left">
          <h1 class="page-title">{{ route.meta.title || '首页' }}</h1>
        </div>
        <div class="topbar-right">
          <button class="theme-toggle" @click="toggleTheme" :title="isDark ? '切换浅色' : '切换深色'">
            <svg v-if="isDark" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="4.2" />
              <path d="M12 2.5v2.2M12 19.3v2.2M2.5 12h2.2M19.3 12h2.2M4.9 4.9l1.6 1.6M17.5 17.5l1.6 1.6M4.9 19.1l1.6-1.6M17.5 6.5l1.6-1.6" />
            </svg>
            <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <path d="M20.5 14.2A8.6 8.6 0 0 1 9.8 3.5a8.6 8.6 0 1 0 10.7 10.7Z" />
            </svg>
          </button>

          <el-tag v-if="isHr && !isAdmin" effect="plain" size="small" round>HR 工作台</el-tag>
          <el-tag v-if="isAdmin" effect="plain" size="small" round>管理员</el-tag>

          <button class="bell-btn" @click="openNotify" title="站内通知">
            <el-badge :value="unread" :hidden="!unread" :max="99">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                <path d="M18 8.6a6 6 0 1 0-12 0c0 6.5-2.4 7.9-2.4 7.9h16.8S18 15.1 18 8.6" />
                <path d="M13.7 20a2 2 0 0 1-3.4 0" />
              </svg>
            </el-badge>
          </button>

          <div v-if="auth.points" class="points-pill">⚡ {{ auth.points.balance }}</div>
        </div>
      </header>

      <main class="page-body">
        <router-view v-slot="{ Component }">
          <transition name="page-fade" mode="out-in" :duration="300">
            <component :is="Component" :key="route.path" />
          </transition>
        </router-view>
      </main>
    </div>

    <!-- ===== 通知抽屉 ===== -->
    <el-drawer v-model="notifyDrawer" title="站内通知" size="420px">
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { notifyApi } from '../api'
import { visibleGroups } from '../config/menu'
import { useTheme } from '../composables/useTheme'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const { theme, toggle } = useTheme()
const isDark = computed(() => theme.value === 'dark')

// 侧边栏 hover 展开(Aceternity 风格)
const sidebarOpen = ref(false)

// 菜单:单一配置源派生
const groups = computed(() => visibleGroups(auth.user?.role))
const isHr = computed(() => [2, 9].includes(auth.user?.role))
const isAdmin = computed(() => auth.user?.role === 9)

function isActive(path) {
  if (path === '/dashboard') return route.path === '/dashboard'
  return route.path.startsWith(path)
}

// ===== 通知(逻辑原样平移) =====
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
  } else if (cmd === 'toggle-theme') {
    toggle()
  }
}

function toggleTheme() { toggle() }
</script>

<style scoped>
.app-shell {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

/* ===== 侧边栏 ===== */
.sidebar {
  width: 68px;
  height: 100vh;
  position: relative;
  transition: width 0.45s cubic-bezier(0.16, 1, 0.3, 1);
  z-index: 10;
}
.sidebar.expanded { width: 216px; }
.sb-inner {
  position: absolute;
  inset: 0;
  width: 216px;
  display: flex;
  flex-direction: column;
  background: var(--glass);
  backdrop-filter: blur(28px) saturate(1.6);
  -webkit-backdrop-filter: blur(28px) saturate(1.6);
  border-right: 1px solid var(--hairline);
  transform: translateX(0);
  transition: transform 0.45s cubic-bezier(0.16, 1, 0.3, 1);
  will-change: transform;
}
.sidebar:not(.expanded) .sb-inner { transform: translateX(-148px); }

.brand {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  gap: 12px;
  cursor: pointer;
  flex-shrink: 0;
  overflow: hidden;
}
.brand-icon {
  width: 34px;
  height: 34px;
  display: grid;
  place-items: center;
  color: var(--foreground);
  background: var(--gradient-accent);
  border-radius: 10px;
  padding: 6px;
  flex-shrink: 0;
}
.brand-icon svg { width: 100%; height: 100%; }
.brand-name {
  font-size: 1.02rem;
  font-weight: 700;
  color: var(--foreground);
  white-space: nowrap;
}

.nav-scroll {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 10px 0;
}
.nav-scroll::-webkit-scrollbar { width: 0; }

.nav-group { margin-bottom: 10px; }
.group-label {
  padding: 6px 22px 4px;
  font-size: 0.66rem;
  font-weight: 600;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--foreground-subtle);
  white-space: nowrap;
}

.nav-item {
  position: relative;
  display: flex;
  align-items: center;
  height: 44px;
  margin: 2px 10px;
  padding: 0 11px;
  border-radius: 12px;
  gap: 13px;
  color: var(--foreground-muted);
  text-decoration: none;
  transition: background 0.25s ease, color 0.25s ease;
  overflow: hidden;
  white-space: nowrap;
  cursor: pointer;
}
.nav-item:hover { background: var(--card-hover); color: var(--foreground); }
.nav-item.active { background: var(--primary-soft); color: var(--primary); }

.nav-icon {
  width: 24px;
  height: 24px;
  display: grid;
  place-items: center;
  font-size: 18px;
  flex-shrink: 0;
}
.nav-text { font-size: 0.9rem; font-weight: 500; }

.active-dot {
  position: absolute;
  right: 10px;
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: var(--primary);
  box-shadow: 0 0 8px var(--primary);
}

.sidebar-foot {
  padding: 10px 0 14px;
  border-top: 1px solid var(--hairline);
}
.user-item { cursor: pointer; }
.nav-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  font-size: 0.82rem;
  font-weight: 700;
  color: var(--primary-foreground);
  background: var(--gradient-accent);
  flex-shrink: 0;
}

/* 文字淡入淡出 */
.fade-text-enter-active { transition: opacity 0.3s ease 0.12s; }
.fade-text-leave-active { transition: opacity 0.1s ease; }
.fade-text-enter-from, .fade-text-leave-to { opacity: 0; }

/* ===== 主列 ===== */
.main-col {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.topbar {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px;
  border-bottom: 1px solid var(--hairline);
  background: var(--glass);
  backdrop-filter: blur(28px) saturate(1.6);
  -webkit-backdrop-filter: blur(28px) saturate(1.6);
  z-index: 5;
  flex-shrink: 0;
}

.page-title {
  font-size: 1.12rem;
  font-weight: 650;
  color: var(--foreground);
  letter-spacing: -0.01em;
  margin: 0;
}

.topbar-right { display: flex; align-items: center; gap: 16px; }

.theme-toggle {
  width: 36px;
  height: 36px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  border: 1px solid var(--hairline);
  background: var(--card);
  color: var(--foreground-muted);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}
.theme-toggle:hover { color: var(--primary); border-color: var(--primary); transform: rotate(18deg); }
.theme-toggle svg { width: 17px; height: 17px; }

.bell-btn {
  width: 36px;
  height: 36px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  border: 1px solid var(--hairline);
  background: var(--card);
  color: var(--foreground-muted);
  cursor: pointer;
  transition: all 0.3s;
}
.bell-btn:hover { color: var(--primary); border-color: var(--primary); }
.bell-btn svg { width: 17px; height: 17px; }
.bell-btn :deep(.el-badge) { display: grid; place-items: center; }

.points-pill {
  padding: 5px 14px;
  border-radius: 9999px;
  background: var(--primary-soft);
  color: var(--primary);
  font-size: 0.82rem;
  font-weight: 600;
}

.page-body {
  flex: 1;
  overflow-y: auto;
  scroll-behavior: smooth;
  padding: 28px;
}

/* 页面切换过渡 */
.page-fade-enter-active { transition: opacity 0.28s ease, transform 0.28s ease; }
.page-fade-leave-active { transition: opacity 0.14s ease; }
.page-fade-enter-from { opacity: 0; transform: translateY(10px); }
.page-fade-leave-to { opacity: 0; }

/* ===== 通知抽屉 ===== */
.notify-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}
.notify-count { font-size: 0.85rem; color: var(--foreground-muted); }
.notify-empty {
  padding: 60px 0;
  text-align: center;
  color: var(--foreground-subtle);
  font-size: 0.9rem;
}
.notify-item {
  display: flex;
  gap: 14px;
  padding: 14px;
  border-radius: 14px;
  border: 1px solid var(--hairline);
  background: var(--card);
  margin-bottom: 10px;
  cursor: pointer;
  transition: background 0.25s;
  position: relative;
}
.notify-item:hover { background: var(--card-hover); }
.notify-item.unread { border-color: var(--primary-soft); }
.n-icon { font-size: 1.3rem; }
.n-title { font-weight: 600; font-size: 0.9rem; color: var(--foreground); }
.n-content {
  font-size: 0.84rem;
  color: var(--foreground-muted);
  margin: 4px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.n-time { font-size: 0.74rem; color: var(--foreground-subtle); }
.n-dot {
  position: absolute;
  top: 14px;
  right: 14px;
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--primary);
  box-shadow: 0 0 6px var(--primary);
}
.notify-pager { display: flex; justify-content: center; margin-top: 12px; }

/* 移动端 */
@media (max-width: 768px) {
  .sidebar { width: 60px; }
  .sidebar.expanded { width: 190px; }
  .page-body { padding: 16px; }
  .topbar { padding: 0 16px; }
  .points-pill { display: none; }
}
</style>
