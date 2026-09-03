<template>
  <div class="auth-stage">
    <!-- 主题切换(登录前也能切换深/浅色,与顶栏同款交互) -->
    <button class="theme-toggle" :title="isDark ? '切换浅色' : '切换深色'" @click="onToggleTheme">
      {{ isDark ? '☀️' : '🌙' }}
    </button>

    <!-- ===== 左:表单列 ===== -->
    <div class="auth-left">
      <div class="form-col">
        <h1 class="auth-title">{{ mode === 'login' ? '欢迎回来' : '创建新账号' }}</h1>
        <p class="auth-sub">{{ mode === 'login' ? '登录你的账号,继续你的求职成长之旅' : '三步开启你的职业档案' }}</p>

        <!-- 登录表单 -->
        <el-form v-if="mode === 'login'" :model="loginForm" :rules="rules" ref="loginFormRef" size="large" @submit.prevent="handleLogin">
          <div class="field-label">用户名 *</div>
          <el-form-item prop="username">
            <div class="fx-input" @mousemove="onInputMove('loginUser', $event)" @mouseenter="fx.loginUser = true" @mouseleave="fx.loginUser = false">
              <input v-model="loginForm.username" class="fx-field" type="text" placeholder="username" autocomplete="off" @keyup.enter="handleLogin" />
              <span v-if="fx.loginUser" class="fx-glow fx-glow-top" :style="{ background: glowStyle(fx.pos.loginUser) }"></span>
              <span v-if="fx.loginUser" class="fx-glow fx-glow-bottom" :style="{ background: glowStyle(fx.pos.loginUser) }"></span>
            </div>
          </el-form-item>
          <div class="field-row">
            <div class="field-label">密码 *</div>
            <a class="forgot" @click.prevent="ElMessage.info('演示环境:请使用下方演示账号登录')">忘记密码?</a>
          </div>
          <el-form-item prop="password">
            <div class="fx-input" @mousemove="onInputMove('loginPwd', $event)" @mouseenter="fx.loginPwd = true" @mouseleave="fx.loginPwd = false">
              <input v-model="loginForm.password" class="fx-field" :type="showPwd ? 'text' : 'password'" placeholder="请输入密码" autocomplete="off" @keyup.enter="handleLogin" />
              <span v-if="fx.loginPwd" class="fx-glow fx-glow-top" :style="{ background: glowStyle(fx.pos.loginPwd) }"></span>
              <span v-if="fx.loginPwd" class="fx-glow fx-glow-bottom" :style="{ background: glowStyle(fx.pos.loginPwd) }"></span>
              <button type="button" class="fx-eye" @click="showPwd = !showPwd" tabindex="-1">
                {{ showPwd ? '🙈' : '👁' }}
              </button>
            </div>
          </el-form-item>
          <button class="btn-primary submit-btn" :disabled="loading" @click="handleLogin">
            <span v-if="!loading">登 录</span>
            <span v-else class="loading-dots"><i></i><i></i><i></i></span>
          </button>
        </el-form>

        <!-- 注册表单 -->
        <el-form v-else :model="regForm" :rules="regRules" ref="regFormRef" size="large" @submit.prevent="handleRegister">
          <div class="field-label">用户名 *</div>
          <el-form-item prop="username">
            <div class="fx-input" @mousemove="onInputMove('regUser', $event)" @mouseenter="fx.regUser = true" @mouseleave="fx.regUser = false">
              <input v-model="regForm.username" class="fx-field" type="text" placeholder="字母数字下划线" autocomplete="off" />
              <span v-if="fx.regUser" class="fx-glow fx-glow-top" :style="{ background: glowStyle(fx.pos.regUser) }"></span>
              <span v-if="fx.regUser" class="fx-glow fx-glow-bottom" :style="{ background: glowStyle(fx.pos.regUser) }"></span>
            </div>
          </el-form-item>
          <div class="field-label">密码 *</div>
          <el-form-item prop="password">
            <div class="fx-input" @mousemove="onInputMove('regPwd', $event)" @mouseenter="fx.regPwd = true" @mouseleave="fx.regPwd = false">
              <input v-model="regForm.password" class="fx-field" :type="showRegPwd ? 'text' : 'password'" placeholder="6~50 位" autocomplete="off" />
              <span v-if="fx.regPwd" class="fx-glow fx-glow-top" :style="{ background: glowStyle(fx.pos.regPwd) }"></span>
              <span v-if="fx.regPwd" class="fx-glow fx-glow-bottom" :style="{ background: glowStyle(fx.pos.regPwd) }"></span>
              <button type="button" class="fx-eye" @click="showRegPwd = !showRegPwd" tabindex="-1">
                {{ showRegPwd ? '🙈' : '👁' }}
              </button>
            </div>
          </el-form-item>
          <div class="field-label">邮箱 *</div>
          <el-form-item prop="email">
            <div class="fx-input" @mousemove="onInputMove('regEmail', $event)" @mouseenter="fx.regEmail = true" @mouseleave="fx.regEmail = false">
              <input v-model="regForm.email" class="fx-field" type="text" placeholder="you@example.com" autocomplete="off" />
              <span v-if="fx.regEmail" class="fx-glow fx-glow-top" :style="{ background: glowStyle(fx.pos.regEmail) }"></span>
              <span v-if="fx.regEmail" class="fx-glow fx-glow-bottom" :style="{ background: glowStyle(fx.pos.regEmail) }"></span>
            </div>
          </el-form-item>
          <div class="field-label">角色 *</div>
          <el-form-item prop="role">
            <div class="fx-input fx-select" @mousemove="onInputMove('regRole', $event)" @mouseenter="fx.regRole = true" @mouseleave="fx.regRole = false">
              <select v-model="regForm.role" class="fx-field fx-field-select">
                <option :value="0">学生</option>
                <option :value="1">校友</option>
                <option :value="2">HR</option>
                <option :value="3">导师</option>
              </select>
              <span v-if="fx.regRole" class="fx-glow fx-glow-top" :style="{ background: glowStyle(fx.pos.regRole) }"></span>
              <span v-if="fx.regRole" class="fx-glow fx-glow-bottom" :style="{ background: glowStyle(fx.pos.regRole) }"></span>
            </div>
          </el-form-item>
          <button class="btn-primary submit-btn" :disabled="loading" @click="handleRegister">
            <span v-if="!loading">注 册</span>
            <span v-else class="loading-dots"><i></i><i></i><i></i></span>
          </button>
        </el-form>

        <p class="switch-line">
          {{ mode === 'login' ? '还没有账号?' : '已有账号?' }}
          <a class="switch-link" @click="mode = mode === 'login' ? 'register' : 'login'">
            {{ mode === 'login' ? '注 册' : '去登录' }}
          </a>
        </p>

        <div class="divider"><span>或</span></div>

        <button class="social-btn" @click="ElMessage.info('演示环境暂未接入第三方登录')">
          <svg viewBox="0 0 24 24" width="17" height="17" fill="currentColor" aria-hidden="true">
            <path d="M12 .5C5.65.5.5 5.65.5 12c0 5.08 3.29 9.39 7.86 10.91.58.11.79-.25.79-.56 0-.27-.01-1.17-.02-2.12-3.2.7-3.87-1.36-3.87-1.36-.52-1.33-1.28-1.68-1.28-1.68-1.04-.71.08-.7.08-.7 1.15.08 1.76 1.19 1.76 1.19 1.03 1.75 2.69 1.25 3.34.95.1-.74.4-1.25.72-1.54-2.55-.29-5.23-1.28-5.23-5.68 0-1.26.45-2.28 1.19-3.09-.12-.29-.52-1.46.11-3.05 0 0 .97-.31 3.17 1.18a11 11 0 0 1 5.77 0c2.2-1.49 3.17-1.18 3.17-1.18.63 1.59.23 2.76.11 3.05.74.81 1.19 1.83 1.19 3.09 0 4.41-2.69 5.38-5.25 5.67.41.35.77 1.05.77 2.12 0 1.53-.01 2.76-.01 3.14 0 .31.21.67.8.56A10.52 10.52 0 0 0 23.5 12C23.5 5.65 18.35.5 12 .5Z" />
          </svg>
          使用 GitHub 继续
        </button>

        <p class="demo-tip">
          演示账号:学生 ftetest · HR demo_hr · 管理员 admin<span class="pwd">密码均为 Abc123456</span>
        </p>
      </div>
    </div>

    <!-- ===== 右:图画区(TechOrbitDisplay 技术轨道 + Ripple 波纹) ===== -->
    <div class="auth-right">
      <div class="ripple-field" aria-hidden="true">
        <span v-for="i in 10" :key="i" class="ripple-ring" :style="rippleStyle(i)"></span>
      </div>
      <div class="orbit-stage" aria-hidden="true">
        <div class="orbit-headline">
          <span class="orbit-headline-text">我要工作</span>
          <span class="orbit-headline-sub">大学生智能就业与互助成长平台</span>
        </div>
        <div v-for="n in orbitNodes" :key="n.label" class="orbit-node" :style="n.style">
          <div class="node-chip">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round" v-html="n.path"></svg>
          </div>
          <div class="node-label">{{ n.label }}</div>
        </div>
      </div>
      <div class="visual-quote">「让每一份努力都被看见,让每一次成长都可验证。」</div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '../api'
import { useAuthStore } from '../stores/auth'
import { useTheme } from '../composables/useTheme'

const router = useRouter()
const authStore = useAuthStore()
const { theme, toggle } = useTheme()
const isDark = ref(theme.value === 'dark')
watch(theme, v => { isDark.value = v === 'dark' })
const onToggleTheme = () => toggle()

const mode = ref('login')
const loading = ref(false)
const loginFormRef = ref()
const regFormRef = ref()
const showPwd = ref(false)
const showRegPwd = ref(false)

const loginForm = reactive({ username: '', password: '' })
const regForm = reactive({ username: '', password: '', email: '', role: 0 })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}
const regRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]{3,50}$/, message: '3~50位字母数字下划线', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 50, message: '密码长度6~50位', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

/* ===== 输入栏交互(login-1 / abishek1512 同款):鼠标悬停时上下边缘光点跟随 ===== */
const fx = reactive({
  loginUser: false, loginPwd: false, regUser: false, regPwd: false, regEmail: false, regRole: false,
  pos: { loginUser: 0, loginPwd: 0, regUser: 0, regPwd: 0, regEmail: 0, regRole: 0 }
})
function onInputMove(key, e) {
  const el = e.currentTarget
  const rect = el.getBoundingClientRect()
  fx.pos[key] = e.clientX - rect.left
}
function glowStyle(x) {
  return `radial-gradient(30px circle at ${x}px 0px, var(--primary, #409eff) 0%, transparent 70%)`
}

/* ===== 轨道图标画(modern-animated-sign-in / arunachalam 同款参数) ===== */
const ORBIT_PATHS = {
  chart: '<path d="M3 3v16a2 2 0 0 0 2 2h16"/><path d="M18 17V9"/><path d="M13 17V5"/><path d="M8 17v-3"/>',
  rocket: '<path d="M12 15v5s3.03-.55 4-2c1.08-1.62 0-5 0-5"/><path d="M4.5 16.5c-1.5 1.26-2 5-2 5s3.74-.5 5-2c.71-.84.7-2.13-.09-2.91a2.18 2.18 0 0 0-2.91-.09"/><path d="M9 12a22 22 0 0 1 2-3.95A12.88 12.88 0 0 1 22 2c0 2.72-.78 7.5-6 11a22.4 22.4 0 0 1-4 2z"/><path d="M9 12H4s.55-3.03 2-4c1.62-1.08 5 .05 5 .05"/><path d="M12 15v-5"/>',
  shield: '<path d="M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.24-2.72a1.17 1.17 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1z"/><path d="m9 12 2 2 4-4"/>',
  briefcase: '<path d="M16 20V4a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/><rect width="20" height="14" x="2" y="6" rx="2"/>',
  send: '<path d="M14.536 21.686a.5.5 0 0 0 .937-.024l6.5-19a.496.496 0 0 0-.635-.635l-19 6.5a.5.5 0 0 0-.024.937l7.93 3.18a2 2 0 0 1 1.112 1.11z"/><path d="m21.854 2.147-10.94 10.939"/>',
  sparkles: '<path d="M11.017 2.814a1 1 0 0 1 1.966 0l1.051 5.558a2 2 0 0 0 1.594 1.594l5.558 1.051a1 1 0 0 1 0 1.966l-5.558 1.051a2 2 0 0 0-1.594 1.594l-1.051 5.558a1 1 0 0 1-1.966 0l-1.051-5.558a2 2 0 0 0-1.594-1.594L2.814 12.98a1 1 0 0 1 0-1.966l5.558-1.051a2 2 0 0 0 1.594-1.594z"/>'
}
// 三层轨道:内(100)×2 中(150)×2 外(210)×2,与参考的 radius/duration/delay 阶梯一致
const orbitNodes = [
  { label: '薪资白皮书', path: ORBIT_PATHS.chart, radius: 100, dur: 20, delay: -20, size: 30, start: 0 },
  { label: 'AI 模拟舱', path: ORBIT_PATHS.rocket, radius: 100, dur: 20, delay: -10, size: 30, start: 180 },
  { label: '防篡改徽章', path: ORBIT_PATHS.shield, radius: 150, dur: 20, delay: 0, size: 40, start: 60, reverse: true },
  { label: '学习中心', path: ORBIT_PATHS.sparkles, radius: 150, dur: 20, delay: -14, size: 40, start: 240, reverse: true },
  { label: '职位广场', path: ORBIT_PATHS.briefcase, radius: 210, dur: 20, delay: -5, size: 50, start: 100, reverse: true },
  { label: '我的投递', path: ORBIT_PATHS.send, radius: 210, dur: 20, delay: -17, size: 50, start: 300, reverse: true }
].map(n => ({
  ...n,
  // CSS 变量直接挂节点本身;值自带单位,避免 keyframes 里 calc(var()*1px) 在部分 Chromium 失效
  style: {
    width: n.size + 'px',
    height: n.size + 'px',
    '--dur': n.dur + 's',
    '--radius': n.radius + 'px',
    '--radius-px': n.radius + 'px',
    '--start-deg': n.start + 'deg',
    '--delay': n.delay + 's',
    animationDirection: n.reverse ? 'reverse' : 'normal'
  }
}))

/* Ripple 同心圆波纹:10环,尺寸/延迟阶梯扩散(modern-animated-sign-in 的 Ripple 组件) */
function rippleStyle(i) {
  const size = 60 + i * 26
  return {
    width: size + 'px',
    height: size + 'px',
    animationDelay: (i * 0.55) + 's'
  }
}

async function handleLogin() {
  try {
    await loginFormRef.value.validate()
  } catch (e) {
    return
  }
  loading.value = true
  try {
    await authStore.login(loginForm)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (e) {
    // 错误已由 request.js 拦截器统一弹出
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  try {
    await regFormRef.value.validate()
  } catch (e) {
    return
  }
  loading.value = true
  try {
    await authApi.register(regForm)
    ElMessage.success('注册成功,请登录')
    mode.value = 'login'
    loginForm.username = regForm.username
    loginForm.password = ''
  } catch (e) {
    // 错误已由拦截器统一弹出
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-stage {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1fr 1.15fr;
}

/* 主题切换按钮:右上角悬浮,玻璃质感 */
.theme-toggle {
  position: fixed;
  top: 18px;
  right: 18px;
  z-index: 20;
  width: 38px;
  height: 38px;
  border-radius: 50%;
  border: 1px solid var(--hairline, rgba(128, 128, 128, 0.25));
  background: var(--glass, rgba(255, 255, 255, 0.1));
  backdrop-filter: blur(12px);
  font-size: 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.theme-toggle:hover {
  transform: scale(1.08);
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.25);
}
@media (max-width: 900px) {
  .auth-stage { grid-template-columns: 1fr; }
  .auth-right { display: none; }
}

/* ===== 左表单列 ===== */
.auth-left {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 32px;
}
.form-col { width: 350px; max-width: 100%; }

.auth-title {
  font-size: 1.9rem;
  font-weight: 720;
  letter-spacing: -0.02em;
  color: var(--foreground);
  margin: 0 0 8px;
}
.auth-sub { font-size: 0.92rem; color: var(--foreground-muted); margin: 0 0 30px; }

.field-label {
  font-size: 0.86rem;
  font-weight: 560;
  color: var(--foreground);
  margin: 4px 0 8px;
}
.field-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.forgot { font-size: 0.82rem; color: var(--primary); cursor: pointer; }
.forgot:hover { text-decoration: underline; }

/* ===== 输入栏(login-1 同款交互):悬停时上下边缘光点跟随鼠标 ===== */
.fx-input {
  position: relative;
  width: 100%;
}
.fx-field {
  width: 100%;
  height: 48px;
  padding: 0 14px;
  border: 2px solid var(--hairline-strong, rgba(128, 128, 128, 0.4));
  border-radius: 10px;
  background: var(--card-strong, rgba(255, 255, 255, 0.05));
  color: var(--foreground);
  font: inherit;
  font-size: 0.95rem;
  outline: none;
  transition: all 0.2s ease-in-out;
  box-sizing: border-box;
}
.fx-field::placeholder { color: var(--foreground-subtle); font-weight: 500; }
.fx-field:focus {
  border-color: var(--primary, #409eff);
  background: var(--card-hover, rgba(255, 255, 255, 0.1));
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.18);
}
html.light .fx-field { background: rgba(255, 255, 255, 0.9); border-color: rgba(29, 29, 31, 0.22); }
html.light .fx-field:focus { background: #fff; }

.fx-glow {
  position: absolute;
  left: 0; right: 0;
  height: 2px;
  z-index: 20;
  pointer-events: none;
  overflow: hidden;
}
.fx-glow-top { top: 0; border-radius: 10px 10px 0 0; }
.fx-glow-bottom { bottom: 0; border-radius: 0 0 10px 10px; background-position: 0 2px; }

.fx-eye {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 21;
  border: none;
  background: none;
  font-size: 15px;
  cursor: pointer;
  opacity: 0.6;
  padding: 4px;
}
.fx-eye:hover { opacity: 1; }

.fx-select select.fx-field { cursor: pointer; appearance: none; -webkit-appearance: none; }
.fx-field-select option { color: #1d1d1f; background: #fff; }

.submit-btn { width: 100%; height: 46px; margin-top: 10px; font-size: 0.98rem; }
.submit-btn:disabled { opacity: 0.7; cursor: wait; }

.loading-dots { display: inline-flex; gap: 5px; }
.loading-dots i {
  width: 6px; height: 6px; border-radius: 50%;
  background: currentColor;
  animation: dot-bounce 1.2s infinite;
}
.loading-dots i:nth-child(2) { animation-delay: 0.15s; }
.loading-dots i:nth-child(3) { animation-delay: 0.3s; }
@keyframes dot-bounce {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.5; }
  30% { transform: translateY(-5px); opacity: 1; }
}

.switch-line {
  text-align: center;
  font-size: 0.88rem;
  color: var(--foreground-muted);
  margin: 20px 0 0;
}
.switch-link { color: var(--primary); font-weight: 600; cursor: pointer; margin-left: 6px; }
.switch-link:hover { text-decoration: underline; }

.divider {
  position: relative;
  text-align: center;
  font-size: 0.8rem;
  color: var(--foreground-subtle);
  margin: 24px 0;
}
.divider::before {
  content: '';
  position: absolute;
  inset: 50% 0 auto;
  height: 1px;
  background: var(--hairline);
}
.divider span {
  position: relative;
  background: var(--background);
  padding: 0 12px;
}

.social-btn {
  width: 100%;
  height: 44px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 9px;
  border-radius: 12px;
  border: 1px solid var(--hairline-strong);
  background: var(--card-strong);
  color: var(--foreground);
  font-size: 0.92rem;
  font-weight: 520;
  cursor: pointer;
  transition: all 0.3s;
}
.social-btn:hover { background: var(--card-hover); border-color: var(--foreground-muted); }

.demo-tip {
  text-align: center;
  font-size: 0.75rem;
  line-height: 1.9;
  color: var(--foreground-subtle);
  margin-top: 22px;
  padding-top: 16px;
  border-top: 1px solid var(--hairline);
}
.pwd { display: block; opacity: 0.85; }

/* ===== 右图画区 ===== */
.auth-right {
  position: relative;
  overflow: hidden;
  border-left: 1px solid var(--hairline);
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(ellipse 60% 50% at 30% 20%, rgba(59, 130, 246, 0.14), transparent 70%),
              radial-gradient(ellipse 50% 45% at 75% 80%, rgba(139, 92, 246, 0.12), transparent 70%);
}
html.light .auth-right {
  background: radial-gradient(ellipse 60% 50% at 30% 20%, rgba(59, 130, 246, 0.10), transparent 70%),
              radial-gradient(ellipse 50% 45% at 75% 80%, rgba(139, 92, 246, 0.08), transparent 70%);
}

/* Ripple 波纹:同心圆扩散,mask 让环带逐渐显隐 */
.ripple-field {
  position: absolute;
  top: 42%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 0; height: 0;
  z-index: 0;
}
.ripple-ring {
  position: absolute;
  top: 50%; left: 50%;
  transform: translate(-50%, -50%) scale(0.1);
  border: 1px solid var(--hairline-strong, rgba(128, 128, 128, 0.35));
  border-radius: 50%;
  background: var(--card, rgba(255, 255, 255, 0.04));
  opacity: 0;
  animation: ripple-expand 5.5s ease-out infinite;
}
@keyframes ripple-expand {
  0% { opacity: 0; transform: translate(-50%, -50%) scale(0.1); }
  12% { opacity: 0.9; }
  100% { opacity: 0; transform: translate(-50%, -50%) scale(1); }
}

/* 轨道图标画 */
.orbit-stage {
  position: relative;
  z-index: 1;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.orbit-headline {
  position: absolute;
  text-align: center;
  z-index: 2;
  pointer-events: none;
}
.orbit-headline-text {
  display: block;
  font-size: clamp(34px, 4.2vw, 54px);
  font-weight: 720;
  letter-spacing: -0.02em;
  background: linear-gradient(180deg, var(--foreground), var(--foreground-subtle));
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}
.orbit-headline-sub {
  display: block;
  margin-top: 10px;
  font-size: 0.9rem;
  color: var(--foreground-muted);
  letter-spacing: 0.35em;
}

/* 轨道节点:rotate(360deg) translateY(radius) rotate(-360deg) 环绕,负延迟分布相位(modern-animated-sign-in 同款 keyframes)
   注意:animation shorthand 里含 var() 会被浏览器整条丢弃,必须用 longhand 子属性 */
.orbit-node {
  position: absolute;
  top: 50%;
  left: 50%;
  margin: 0;
  will-change: transform;
  animation-name: orbit-spin;
  animation-duration: var(--dur, 20s);
  animation-timing-function: linear;
  animation-iteration-count: infinite;
  animation-delay: var(--delay, 0s);
}
@keyframes orbit-spin {
  0% { transform: rotate(var(--start-deg)) translateY(var(--radius)) rotate(calc(var(--start-deg) * -1)) rotate(0deg); }
  100% { transform: rotate(var(--start-deg)) translateY(var(--radius)) rotate(calc(var(--start-deg) * -1)) rotate(360deg); }
}

.node-chip {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  border: 1px solid var(--hairline-strong, rgba(128, 128, 128, 0.3));
  background: var(--glass, rgba(20, 20, 30, 0.5));
  backdrop-filter: blur(8px);
  color: var(--primary, #409eff);
  box-shadow: 0 4px 24px rgba(64, 158, 255, 0.15);
}
.node-chip svg { width: 55%; height: 55%; }
html.light .node-chip { background: rgba(255, 255, 255, 0.75); box-shadow: 0 4px 18px rgba(64, 158, 255, 0.18); }

.node-label {
  position: absolute;
  top: calc(100% + 6px);
  left: 50%;
  transform: translateX(-50%);
  white-space: nowrap;
  font-size: 0.72rem;
  color: var(--foreground-muted);
  background: var(--card, rgba(20, 20, 30, 0.4));
  padding: 2px 8px;
  border-radius: 999px;
  border: 1px solid var(--hairline);
  backdrop-filter: blur(6px);
}
html.light .node-label { background: rgba(255, 255, 255, 0.8); }

.visual-quote {
  position: absolute;
  bottom: 7%;
  left: 50%;
  transform: translateX(-50%);
  z-index: 2;
  font-size: 0.85rem;
  color: var(--foreground-subtle);
  letter-spacing: 0.06em;
  white-space: nowrap;
}

@media (prefers-reduced-motion: reduce) {
  .orbit-node, .ripple-ring { animation: none; }
}
</style>
