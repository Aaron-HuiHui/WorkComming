import { ref, watchEffect } from 'vue'

const STORAGE_KEY = 'iwantjob-theme'
const theme = ref('dark')

let initialized = false

function persist(value) {
  try { localStorage.setItem(STORAGE_KEY, value) } catch (e) { /* ignore */ }
}

function applyTheme(value) {
  const root = document.documentElement
  root.classList.toggle('light', value === 'light')
  // EP 暗色变量表挂在 .dark 上,与项目自定义 .light 互斥同步
  root.classList.toggle('dark', value !== 'light')
}

function initTheme() {
  if (initialized) return
  initialized = true
  let saved = null
  try { saved = localStorage.getItem(STORAGE_KEY) } catch (e) { /* ignore */ }
  if (saved === 'light' || saved === 'dark') {
    theme.value = saved
  } else {
    // 首访跟随系统
    const preferLight = window.matchMedia?.('(prefers-color-scheme: light)').matches
    theme.value = preferLight ? 'light' : 'dark'
  }
  watchEffect(() => applyTheme(theme.value))
}

export function useTheme() {
  initTheme()
  const toggle = () => { theme.value = theme.value === 'dark' ? 'light' : 'dark'; persist(theme.value) }
  const setDark = () => { theme.value = 'dark'; persist('dark') }
  const setLight = () => { theme.value = 'light'; persist('light') }
  return { theme, toggle, setDark, setLight, isDark: () => theme.value === 'dark' }
}
