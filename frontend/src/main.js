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

axios.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = 'Bearer ' + token
  return config
})

const app = createApp(App)
app.use(ElementPlus)
app.use(router)
app.mount('#app')
