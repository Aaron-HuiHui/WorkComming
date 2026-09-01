import { defineStore } from 'pinia'
import { authApi, userApi } from '../api'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('accessToken') || '',
    refreshToken: localStorage.getItem('refreshToken') || '',
    user: null,
    points: null
  }),

  getters: {
    isLoggedIn: state => !!state.token,
    role: state => (state.user ? state.user.role : null),
    roleName: state => {
      const map = { 0: '学生', 1: '校友', 2: 'HR', 3: '导师', 9: '管理员' }
      return state.user ? map[state.user.role] || '未知' : ''
    }
  },

  actions: {
    async login(form) {
      const res = await authApi.login(form)
      this.token = res.data.accessToken
      this.refreshToken = res.data.refreshToken
      localStorage.setItem('accessToken', this.token)
      localStorage.setItem('refreshToken', this.refreshToken)
      await this.fetchUserInfo()
    },

    async fetchUserInfo() {
      const [userRes, pointsRes] = await Promise.all([
        userApi.me(),
        userApi.points().catch(() => null)
      ])
      this.user = userRes.data
      localStorage.setItem('userInfo', JSON.stringify(this.user))
      if (pointsRes) this.points = pointsRes.data
    },

    async logout() {
      try {
        await authApi.logout(this.token)
      } catch (e) {
        // 登出失败不阻塞前端清理
      }
      this.token = ''
      this.refreshToken = ''
      this.user = null
      this.points = null
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('userInfo')
    }
  }
})
