import './assets/main.css'

import { createApp } from 'vue'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import router from './router/index.js'
import axios from 'axios'

// 导入ECharts主题
import * as echarts from 'echarts'
import darkTheme from 'echarts/lib/theme/dark'
import lightTheme from 'echarts/lib/theme/light'

// 注册主题
echarts.registerTheme('dark', darkTheme)
echarts.registerTheme('light', lightTheme)

// 请求拦截器
axios.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = 'Bearer ' + token
  }
  return config
}, error => {
  return Promise.reject(error)
})

// 响应拦截器
axios.interceptors.response.use(response => {
  return response
}, error => {
  if (error.response) {
    const { status } = error.response
    
    // 处理401/403错误
    if (status === 401 || status === 403) {
      console.log('认证失败，清除token并跳转到登录页面')
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      
      // 如果不是在登录页面，则跳转到登录页面
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
  }
  return Promise.reject(error)
})

const app = createApp(App)
app.use(ElementPlus)
app.use(router)
app.mount('#app')
