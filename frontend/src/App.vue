<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'

// 添加请求拦截器，自动添加Authorization header
axios.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 添加响应拦截器，处理401错误
axios.interceptors.response.use(
  (response) => {
    return response
  },
  (error) => {
    if (error.response && error.response.status === 401) {
      // Token过期或无效，清除本地存储并跳转到登录页
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      router.push('/login')
    }
    return Promise.reject(error)
  }
)

const router = useRouter()
const route = useRoute()
const isLoading = ref(false)

const isAuthenticated = computed(() => {
  return !!localStorage.getItem('token')
})

const username = computed(() => {
  const token = localStorage.getItem('token')
  if (token) {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]))
      return payload.sub || '用户'
    } catch (e) {
      return '用户'
    }
  }
  return '用户'
})

const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  ElMessage.success('已退出登录')
  router.push('/login')
}

const autoLogin = async () => {
  // 如果已经有token，检查是否有效
  const existingToken = localStorage.getItem('token')
  if (existingToken) {
    console.log('发现现有token，检查有效性...')
    try {
      // 测试token是否有效
      const response = await axios.get('/api/report/configs', {
        headers: { Authorization: `Bearer ${existingToken}` }
      })
      console.log('现有token有效')
      return
    } catch (error) {
      console.log('现有token无效，清除并重新登录')
      localStorage.removeItem('token')
      localStorage.removeItem('username')
    }
  }

  // 如果没有token或token无效，尝试自动登录
  if (!localStorage.getItem('token') && !isLoading.value) {
    isLoading.value = true
    console.log('开始自动登录...')
    
    try {
      const response = await axios.post('/api/auth/login', {
        username: 'admin',
        password: 'admin123'
      })
      
      console.log('登录响应:', response.data)
      
      if (response.data.token) {
        localStorage.setItem('token', response.data.token)
        localStorage.setItem('username', response.data.username)
        ElMessage.success('自动登录成功')
        console.log('自动登录成功，token已保存')
        
        // 如果当前在登录页面，跳转到报表页面
        if (router.currentRoute.value.path === '/login') {
          router.push('/reports')
        }
      } else {
        console.log('登录响应中没有token')
        ElMessage.error('自动登录失败：响应中没有token')
      }
    } catch (error) {
      console.error('自动登录失败:', error)
      ElMessage.error(`自动登录失败: ${error.response?.data?.error || error.message}`)
    } finally {
      isLoading.value = false
    }
  }
}

onMounted(() => {
  console.log('App组件挂载，开始自动登录检查...')
  autoLogin()
})
</script>

<template>
  <div id="app">
    <el-container v-if="isAuthenticated" style="height: 100vh;">
      <el-header style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); box-shadow: 0 2px 12px rgba(102, 126, 234, 0.3); display: flex; align-items: center; justify-content: space-between; padding: 0 20px;">
        <div style="display: flex; align-items: center; flex: 1;">
          <div style="display: flex; align-items: center; margin-right: 40px;">
            <el-icon style="color: white; margin-right: 12px; font-size: 28px;">
              <svg viewBox="0 0 1024 1024" width="28" height="28">
                <path fill="currentColor" d="M512 64C264.6 64 64 264.6 64 512s200.6 448 448 448 448-200.6 448-448S759.4 64 512 64zm0 820c-205.4 0-372-166.6-372-372s166.6-372 372-372 372 166.6 372 372-166.6 372-372 372z"/>
                <path fill="currentColor" d="M512 336c-97.2 0-176 78.8-176 176s78.8 176 176 176 176-78.8 176-176-78.8-176-176-176zm0 288c-61.9 0-112-50.1-112-112s50.1-112 112-112 112 50.1 112 112-50.1 112-112 112z"/>
              </svg>
            </el-icon>
            <h2 style="margin: 0; color: white; font-weight: 600; font-size: 24px; text-shadow: 0 1px 2px rgba(0,0,0,0.1);">MongoReporter</h2>
          </div>
          <el-menu mode="horizontal" :router="true" :default-active="route.path" style="border-bottom: none; flex: 1; background: transparent;">
            <el-menu-item index="/reports" style="min-width: 120px; color: rgba(255,255,255,0.8);">报表管理</el-menu-item>
            <el-menu-item index="/datasources" style="min-width: 120px; color: rgba(255,255,255,0.8);">数据源管理</el-menu-item>
          </el-menu>
        </div>
        <div style="display: flex; align-items: center; flex-shrink: 0;">
          <div style="display: flex; align-items: center; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 8px 16px; border-radius: 20px; margin-right: 15px; box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);">
            <el-icon style="color: white; margin-right: 8px; font-size: 16px;">
              <svg viewBox="0 0 1024 1024" width="16" height="16">
                <path fill="currentColor" d="M512 64C264.6 64 64 264.6 64 512s200.6 448 448 448 448-200.6 448-448S759.4 64 512 64zm0 820c-205.4 0-372-166.6-372-372s166.6-372 372-372 372 166.6 372 372-166.6 372-372 372z"/>
                <path fill="currentColor" d="M512 336c-97.2 0-176 78.8-176 176s78.8 176 176 176 176-78.8 176-176-78.8-176-176-176zm0 288c-61.9 0-112-50.1-112-112s50.1-112 112-112 112 50.1 112 112-50.1 112-112 112z"/>
              </svg>
            </el-icon>
            <span style="color: white; font-weight: 500; font-size: 14px;">欢迎，{{ username }}</span>
          </div>
          <el-button @click="logout" size="small" type="danger" plain style="border-radius: 6px; font-weight: 500;">
            <el-icon style="margin-right: 4px;">
              <svg viewBox="0 0 1024 1024" width="14" height="14">
                <path fill="currentColor" d="M512 64C264.6 64 64 264.6 64 512s200.6 448 448 448 448-200.6 448-448S759.4 64 512 64zm0 820c-205.4 0-372-166.6-372-372s166.6-372 372-372 372 166.6 372 372-166.6 372-372 372z"/>
                <path fill="currentColor" d="M512 336c-97.2 0-176 78.8-176 176s78.8 176 176 176 176-78.8 176-176-78.8-176-176-176zm0 288c-61.9 0-112-50.1-112-112s50.1-112 112-112 112 50.1 112 112-50.1 112-112 112z"/>
              </svg>
            </el-icon>
            退出
          </el-button>
        </div>
      </el-header>
      <el-main style="padding: 0; background: #f5f7fa;">
        <router-view />
      </el-main>
    </el-container>
    <router-view v-else />
  </div>
</template>

<style>
#app {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  width: 100%;
  height: 100vh;
  margin: 0;
  padding: 0;
}

/* 确保菜单正确显示 */
.el-menu {
  background-color: transparent !important;
  width: 100%;
}

.el-menu--horizontal > .el-menu-item {
  height: 60px;
  line-height: 60px;
  border-bottom: none;
  min-width: 120px;
  text-align: center;
  font-size: 14px;
  color: rgba(255,255,255,0.8) !important;
  font-weight: 500;
}

.el-menu--horizontal > .el-menu-item.is-active {
  border-bottom: 3px solid white;
  color: white !important;
  background-color: rgba(255,255,255,0.1);
  font-weight: 600;
}

.el-menu--horizontal > .el-menu-item:hover {
  background-color: rgba(255,255,255,0.15);
  color: white !important;
}

/* 确保头部布局正确 */
.el-header {
  padding: 0 20px;
  height: 60px;
  line-height: 60px;
  min-width: 0;
}

.el-main {
  overflow-y: auto;
  padding: 0;
  height: calc(100vh - 60px);
}

.el-container {
  height: 100vh;
}

/* 确保登录页面也能全屏显示 */
.el-container:not(.el-container) {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
