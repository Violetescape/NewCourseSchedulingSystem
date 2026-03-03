import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  // 新增 server 配置用于跨域代理
  server: {
    proxy: {
      // 匹配请求路径中以 /api 开头的请求
      '/api': {
        target: 'http://localhost:8080', // 你的 SpringBoot 后端地址
        changeOrigin: true,              // 允许跨域
        rewrite: (path) => path.replace(/^\/api/, '') // 将 /api 前缀去掉再发给后端
      }
    }
  }
})
