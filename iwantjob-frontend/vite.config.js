import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      // 前端请求 /api/** 代理到网关（8080），由网关路由到核心/职位微服务
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})