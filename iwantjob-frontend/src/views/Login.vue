<template>
  <div class="login-stage">
    <!-- 品牌区(桌面双栏左侧) -->
    <div class="login-brand">
      <div class="brand-inner">
        <div class="brand-mark">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <rect x="3" y="7.5" width="18" height="12.5" rx="2.5" />
            <path d="M9 7.5V6a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2v1.5" />
            <path d="M3 12.8h18" />
            <path d="M9.5 11.2v3M14.5 11.2v3" />
          </svg>
        </div>
        <h1 class="brand-title">我要工作</h1>
        <p class="brand-slogan">大学生智能就业<br />与互助成长平台</p>
        <div class="brand-points">
          <div class="bp-item"><span class="bp-dot violet"></span>薪资白皮书 · 打破信息差</div>
          <div class="bp-item"><span class="bp-dot blue"></span>AI 模拟舱 · 情境演练</div>
          <div class="bp-item"><span class="bp-dot fuchsia"></span>防篡改徽章 · 可信履历</div>
        </div>
      </div>
    </div>

    <!-- 表单卡 -->
    <div class="login-card glass-card">
      <div class="card-head">
        <h2 class="card-title">{{ activeTab === 'login' ? '欢迎回来' : '创建账号' }}</h2>
        <p class="card-sub">{{ activeTab === 'login' ? '继续你的求职成长之旅' : '三步开启你的职业档案' }}</p>
      </div>

      <!-- Tab 切换器(苹果分段控件风) -->
      <div class="seg-control">
        <button :class="{ on: activeTab === 'login' }" @click="activeTab = 'login'">登录</button>
        <button :class="{ on: activeTab === 'register' }" @click="activeTab = 'register'">注册</button>
        <span class="seg-thumb" :class="{ right: activeTab === 'register' }"></span>
      </div>

      <!-- 登录 -->
      <el-form v-if="activeTab === 'login'" :model="loginForm" :rules="rules" ref="loginFormRef" size="large" @submit.prevent="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" placeholder="用户名" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="密码" show-password :prefix-icon="Lock" @keyup.enter="handleLogin" />
        </el-form-item>
        <button class="btn-primary submit-btn" :disabled="loading" @click="handleLogin">
          <span v-if="!loading">登 录</span>
          <span v-else class="loading-dots"><i></i><i></i><i></i></span>
        </button>
        <div class="demo-tip">
          演示账号:学生 ftetest · HR demo_hr · 管理员 admin<br />
          <span class="pwd">密码均为 Abc123456</span>
        </div>
      </el-form>

      <!-- 注册 -->
      <el-form v-else :model="regForm" :rules="regRules" ref="regFormRef" size="large" @submit.prevent="handleRegister">
        <el-form-item prop="username">
          <el-input v-model="regForm.username" placeholder="用户名(字母数字下划线)" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="regForm.password" type="password" placeholder="密码(6~50位)" show-password :prefix-icon="Lock" />
        </el-form-item>
        <el-form-item prop="email">
          <el-input v-model="regForm.email" placeholder="邮箱" :prefix-icon="Message" />
        </el-form-item>
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
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Message } from '@element-plus/icons-vue'
import { authApi } from '../api'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const activeTab = ref('login')
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
    activeTab.value = 'login'
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
.login-stage {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.1fr 1fr;
  align-items: center;
  gap: 60px;
  max-width: 1120px;
  margin: 0 auto;
  padding: 40px 32px;
}
@media (max-width: 900px) {
  .login-stage { grid-template-columns: 1fr; gap: 36px; }
  .login-brand { display: none; }
}

/* 品牌区 */
.brand-inner { padding-left: 12px; }
.brand-mark {
  width: 54px;
  height: 54px;
  border-radius: 15px;
  display: grid;
  place-items: center;
  color: var(--primary-foreground);
  background: var(--gradient-accent);
  padding: 12px;
  margin-bottom: 26px;
  box-shadow: 0 12px 40px rgba(41, 151, 255, 0.3);
}
.brand-mark svg { width: 100%; height: 100%; }
.brand-title {
  font-size: clamp(2rem, 4vw, 2.9rem);
  font-weight: 750;
  letter-spacing: -0.02em;
  margin: 0 0 12px;
  color: var(--foreground);
}
.brand-slogan {
  font-size: 1.08rem;
  line-height: 1.75;
  color: var(--foreground-muted);
  margin: 0 0 36px;
}
.brand-points { display: flex; flex-direction: column; gap: 14px; }
.bp-item {
  display: flex;
  align-items: center;
  gap: 11px;
  font-size: 0.92rem;
  color: var(--foreground-muted);
}
.bp-dot { width: 8px; height: 8px; border-radius: 50%; }
.bp-dot.violet { background: #a259ff; box-shadow: 0 0 10px #a259ff; }
.bp-dot.blue { background: #2997ff; box-shadow: 0 0 10px #2997ff; }
.bp-dot.fuchsia { background: #ff5ca8; box-shadow: 0 0 10px #ff5ca8; }

/* 表单卡 */
.login-card { padding: 38px 34px 30px; width: 100%; max-width: 420px; justify-self: center; }
.card-head { margin-bottom: 26px; }
.card-title {
  font-size: 1.55rem;
  font-weight: 720;
  letter-spacing: -0.02em;
  color: var(--foreground);
  margin: 0 0 6px;
}
.card-sub { font-size: 0.88rem; color: var(--foreground-subtle); margin: 0; }

/* 苹果分段控件 */
.seg-control {
  position: relative;
  display: grid;
  grid-template-columns: 1fr 1fr;
  background: var(--card);
  border: 1px solid var(--hairline);
  border-radius: 12px;
  padding: 3px;
  margin-bottom: 24px;
}
.seg-thumb {
  position: absolute;
  top: 3px;
  left: 3px;
  width: calc(50% - 4px);
  height: calc(100% - 6px);
  border-radius: 9px;
  background: var(--card-strong);
  border: 1px solid var(--hairline-strong);
  transition: transform 0.38s cubic-bezier(0.16, 1, 0.3, 1);
}
.seg-thumb.right { transform: translateX(calc(100% + 2px)); }
.seg-control button {
  position: relative;
  z-index: 1;
  padding: 9px 0;
  border: none;
  background: transparent;
  font-size: 0.9rem;
  font-weight: 560;
  color: var(--foreground-muted);
  cursor: pointer;
  border-radius: 9px;
  transition: color 0.25s;
}
.seg-control button.on { color: var(--foreground); }

.submit-btn {
  width: 100%;
  margin-top: 6px;
  height: 46px;
  font-size: 1rem;
}
.submit-btn:disabled { opacity: 0.7; cursor: wait; }

/* 加载三点 */
.loading-dots { display: inline-flex; gap: 5px; }
.loading-dots i {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
  animation: dot-bounce 1.2s infinite;
}
.loading-dots i:nth-child(2) { animation-delay: 0.15s; }
.loading-dots i:nth-child(3) { animation-delay: 0.3s; }
@keyframes dot-bounce {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.5; }
  30% { transform: translateY(-5px); opacity: 1; }
}

.demo-tip {
  text-align: center;
  font-size: 0.76rem;
  line-height: 1.8;
  color: var(--foreground-subtle);
  margin-top: 16px;
  padding-top: 14px;
  border-top: 1px solid var(--hairline);
}
.pwd { opacity: 0.8; }
</style>
