import axios from 'axios'
import { ElMessage } from 'element-plus'

// 统一 axios 实例：走 Vite 代理（/api -> localhost:8080）
const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截：注入 JWT
request.interceptors.request.use(config => {
  const token = localStorage.getItem('accessToken')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截：统一处理 Result<T> 与 401
request.interceptors.response.use(
  response => {
    const res = response.data
    // 非标准 Result（如文件流）直接返回
    if (res.code === undefined) return res
    if (res.code === 0) return res
    // 业务错误
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message))
  },
  error => {
    if (error.response) {
      const { status, data } = error.response
      if (status === 401) {
        localStorage.removeItem('accessToken')
        localStorage.removeItem('refreshToken')
        if (window.location.hash !== '#/login') {
          ElMessage.error('登录已过期，请重新登录')
          window.location.href = '#/login'
        }
      } else {
        ElMessage.error((data && data.message) || `请求错误 ${status}`)
      }
    } else {
      ElMessage.error('网络异常，请检查后端服务')
    }
    return Promise.reject(error)
  }
)

export default request
