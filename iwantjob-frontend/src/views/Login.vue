<template>
  <div class="login-bg">
    <el-card class="login-card glass">
      <div class="logo">
        <span class="logo-icon">💼</span>
        <h1>我要工作</h1>
        <p>大学生智能就业与互助成长平台</p>
      </div>

      <el-tabs v-model="activeTab" stretch>
        <el-tab-pane label="登 录" name="login">
          <el-form :model="loginForm" :rules="rules" ref="loginFormRef" size="large" @submit.prevent="handleLogin">
            <el-form-item prop="username">
              <el-input v-model="loginForm.username" placeholder="用户名" :prefix-icon="User" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="loginForm.password" type="password" placeholder="密码" show-password :prefix-icon="Lock" />
            </el-form-item>
            <el-button type="primary" size="large" style="width:100%" :loading="loading" @click="handleLogin">
              登 录
            </el-button>
            <div class="tip">演示账号：学生 ftetest · HR demo_hr · 管理员 admin（密码均为 Abc123456）</div>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="注 册" name="register">
          <el-form :model="regForm" :rules="regRules" ref="regFormRef" size="large" @submit.prevent="handleRegister">
            <el-form-item prop="username">
              <el-input v-model="regForm.username" placeholder="用户名（字母数字下划线）" :prefix-icon="User" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="regForm.password" type="password" placeholder="密码（6~50位）" show-password :prefix-icon="Lock" />
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
            <el-button type="primary" size="large" style="width:100%" :loading="loading" @click="handleRegister">
              注 册
            </el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
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
    // 错误已由 request.js 拦截器统一弹出，这里捕获避免 unhandled rejection
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
    ElMessage.success('注册成功，请登录')
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
/* 透明底：透出 App.vue 全局 aurora 背景 */
.login-bg {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
}
.login-card {
  width: 420px;
  padding: 10px 15px;
  border-radius: 24px;
}
.logo {
  text-align: center;
  margin-bottom: 15px;
}
.logo-icon {
  font-size: 42px;
}
.logo h1 {
  font-size: 24px;
  margin: 8px 0 4px;
  color: #fff;
  letter-spacing: 2px;
}
.logo p {
  font-size: 13px;
  color: var(--g-text-secondary);
}
.tip {
  text-align: center;
  font-size: 12px;
  color: var(--g-text-muted);
  margin-top: 10px;
}
/* Tab：白色下划线与激活文字（与全局 el-tabs 覆盖一致，这里兜底） */
.login-card :deep(.el-tabs__item) { color: var(--g-text-secondary); }
.login-card :deep(.el-tabs__item.is-active) { color: #fff; }
.login-card :deep(.el-tabs__item:hover) { color: rgba(255, 255, 255, 0.85); }
.login-card :deep(.el-tabs__active-bar) { background-color: #fff; }
.login-card :deep(.el-tabs__nav-wrap::after) { background-color: rgba(255, 255, 255, 0.14); }
</style>
