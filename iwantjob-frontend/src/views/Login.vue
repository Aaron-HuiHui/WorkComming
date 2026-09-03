<template>
  <div class="auth-stage">
    <!-- 主题切换(登录前也能切换深/浅色,与顶栏同款交互) -->
    <button class="theme-toggle" :title="isDark ? '切换浅色' : '切换深色'" @click="onToggleTheme">
      {{ isDark ? '☀️' : '🌙' }}
    </button>
    <!-- ===== 左:表单列(auth-ui 结构:350px 居中,标签在输入框上方) ===== -->
    <div class="auth-left">
      <div class="form-col">
        <h1 class="auth-title">{{ mode === 'login' ? '登录你的账号' : '创建新账号' }}</h1>
        <p class="auth-sub">{{ mode === 'login' ? '欢迎回来,继续你的求职成长之旅' : '三步开启你的职业档案' }}</p>

        <!-- 登录表单 -->
        <el-form v-if="mode === 'login'" :model="loginForm" :rules="rules" ref="loginFormRef" size="large" @submit.prevent="handleLogin">
          <div class="field-label">用户名</div>
          <el-form-item prop="username">
            <el-input v-model="loginForm.username" placeholder="username" :prefix-icon="User" />
          </el-form-item>
          <div class="field-row">
            <div class="field-label">密码</div>
            <a class="forgot" @click.prevent="ElMessage.info('演示环境:请使用下方演示账号登录')">忘记密码?</a>
          </div>
          <el-form-item prop="password">
            <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password :prefix-icon="Lock" @keyup.enter="handleLogin" />
          </el-form-item>
          <button class="btn-primary submit-btn" :disabled="loading" @click="handleLogin">
            <span v-if="!loading">登 录</span>
            <span v-else class="loading-dots"><i></i><i></i><i></i></span>
          </button>
        </el-form>

        <!-- 注册表单 -->
        <el-form v-else :model="regForm" :rules="regRules" ref="regFormRef" size="large" @submit.prevent="handleRegister">
          <div class="field-label">用户名</div>
          <el-form-item prop="username">
            <el-input v-model="regForm.username" placeholder="字母数字下划线" :prefix-icon="User" />
          </el-form-item>
          <div class="field-label">密码</div>
          <el-form-item prop="password">
            <el-input v-model="regForm.password" type="password" placeholder="6~50 位" show-password :prefix-icon="Lock" />
          </el-form-item>
          <div class="field-label">邮箱</div>
          <el-form-item prop="email">
            <el-input v-model="regForm.email" placeholder="you@example.com" :prefix-icon="Message" />
          </el-form-item>
          <div class="field-label">角色</div>
          <el-form-item prop="role">
            <el-select v-model="regForm.role" placeholder="选择角色" style="width:100%">
              <el-option :value="0" label="学生" />
              <el-option :value="1" label="校友" />
              <el-option :value="2" label="HR" />
              <el-option :value="3" label="导师" />
            </el-select>
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

    <!-- ===== 右:视觉区(auth-ui 结构:整幅背景 + 底部渐隐 + 居中证言) ===== -->
    <div class="auth-right">
      <div class="visual-beams" aria-hidden="true">
        <div v-for="row in 3" :key="row" class="beam-row" :style="{ top: 6 - row * 9 + 'rem', right: -14 - row * 10 + 'rem' }">
          <div v-for="bar in 3" :key="bar" class="beam-bar" :style="{ height: row === 3 ? '30rem' : '20rem' }"></div>
        </div>
        <div class="visual-grid"></div>
      </div>
      <div class="visual-fade"></div>
      <div class="visual-quote">
        <blockquote>「让每一份努力都被看见,让每一次成长都可验证。」</blockquote>
        <p>我要工作 · 大学生就业赋能平台</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Message } from '@element-plus/icons-vue'
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

/* 分隔线(auth-ui after 伪元素同款) */
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

/* ===== 右视觉区 ===== */
.auth-right {
  position: relative;
  overflow: hidden;
  border-left: 1px solid var(--hairline);
}
.visual-beams {
  position: absolute;
  inset: 0;
  overflow: hidden;
  background: var(--background-soft);
}
.beam-row {
  position: absolute;
  display: flex;
  gap: 10rem;
  transform: rotate(-20deg);
  filter: blur(2px);
  z-index: 0;
}
.beam-bar {
  width: 10rem;
  background: linear-gradient(90deg, var(--primary) 0%, var(--primary-soft) 100%);
  opacity: 0.5;
  border-radius: 4px;
}
html.light .beam-bar { opacity: 0.3; }
.visual-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(var(--hairline) 1px, transparent 1px),
    linear-gradient(90deg, var(--hairline) 1px, transparent 1px);
  background-size: 64px 64px;
  mask-image: radial-gradient(ellipse 70% 70% at 60% 30%, black 20%, transparent 80%);
  -webkit-mask-image: radial-gradient(ellipse 70% 70% at 60% 30%, black 20%, transparent 80%);
}

.visual-fade {
  position: absolute;
  inset: auto 0 0 0;
  height: 140px;
  background: linear-gradient(to top, var(--background) 0%, transparent 100%);
}

.visual-quote {
  position: absolute;
  inset: auto 0 0;
  padding: 0 24px 40px;
  text-align: center;
  z-index: 2;
}
.visual-quote blockquote {
  margin: 0 0 8px;
  font-size: 1.12rem;
  font-weight: 620;
  letter-spacing: -0.01em;
  color: var(--foreground);
}
.visual-quote p {
  margin: 0;
  font-size: 0.8rem;
  letter-spacing: 0.18em;
  color: var(--foreground-subtle);
}
</style>
