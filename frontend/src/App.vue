<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Menu, ArrowDown, Close, DataBoard, Document, Connection, User, Setting } from '@element-plus/icons-vue'
import Login from './views/Login.vue'

const router = useRouter()
const route = useRoute()

const isLoggedIn = ref(false)
const username = ref('')
const userAvatar = ref('')
const showMobileMenu = ref(false)

// 检查登录状态
const checkLoginStatus = () => {
  const token = localStorage.getItem('token')
  const storedUsername = localStorage.getItem('username')
  
  if (token && storedUsername) {
    isLoggedIn.value = true
    username.value = storedUsername
    console.log('自动登录成功，token已保存')
    
    // 如果当前在登录页面，跳转到仪表板页面
    if (router.currentRoute.value.path === '/login') {
      router.push('/dashboard')
    }
  } else {
    isLoggedIn.value = false
    // 尝试自动登录
    autoLogin()
  }
}

// 自动登录
const autoLogin = async () => {
  try {
    console.log('尝试自动登录...')
    const response = await fetch('/api/auth/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        username: 'admin',
        password: 'admin123'
      })
    })
    
    if (response.ok) {
      const data = await response.json()
      if (data.token) {
        localStorage.setItem('token', data.token)
        localStorage.setItem('username', data.username || 'admin')
        isLoggedIn.value = true
        username.value = data.username || 'admin'
        console.log('自动登录成功')
        
        // 如果当前在登录页面，跳转到仪表板页面
        if (router.currentRoute.value.path === '/login') {
          router.push('/dashboard')
        }
        return
      }
    }
  } catch (error) {
    console.log('自动登录失败:', error)
  }
  
  // 自动登录失败，跳转到登录页面
  if (router.currentRoute.value.path !== '/login') {
    router.push('/login')
  }
}

// 处理登录成功
const handleLoginSuccess = (userData) => {
  isLoggedIn.value = true
  username.value = userData.username
  localStorage.setItem('token', userData.token)
  localStorage.setItem('username', userData.username)
  
  ElMessage.success('登录成功')
  router.push('/dashboard')
}

// 处理用户命令
const handleUserCommand = (command) => {
  switch (command) {
    case 'profile':
      ElMessage.info('个人资料功能开发中...')
      break
    case 'settings':
      router.push('/settings')
      break
    case 'logout':
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      isLoggedIn.value = false
      username.value = ''
      ElMessage.success('已退出登录')
      router.push('/login')
      break
  }
}

// 移动端菜单控制
const toggleMobileMenu = () => {
  showMobileMenu.value = !showMobileMenu.value
}

const closeMobileMenu = () => {
  showMobileMenu.value = false
}

// 监听窗口大小变化
const handleResize = () => {
  if (window.innerWidth > 768) {
    showMobileMenu.value = false
  }
}

onMounted(() => {
  checkLoginStatus()
  window.addEventListener('resize', handleResize)
})

// 监听路由变化
watch(() => route.path, () => {
  if (window.innerWidth <= 768) {
    showMobileMenu.value = false
  }
})
</script>

<template>
  <div id="app">
    <!-- 登录页面 -->
    <div v-if="!isLoggedIn" class="login-container">
      <Login @login-success="handleLoginSuccess" />
    </div>
    
    <!-- 主应用界面 -->
    <div v-else class="app-container">
      <!-- 顶部导航栏 -->
      <header class="app-header">
        <div class="header-content">
          <div class="logo-section">
            <h1 class="app-title">MongoReporter</h1>
          </div>
          
          <!-- 桌面端导航菜单 -->
          <nav class="desktop-nav">
            <el-menu mode="horizontal" :router="true" :default-active="route.path" style="border-bottom: none; flex: 1; background: transparent;">
              <el-menu-item index="/dashboard" style="min-width: 120px; color: rgba(255,255,255,0.8);">仪表板</el-menu-item>
              <el-menu-item index="/reports" style="min-width: 120px; color: rgba(255,255,255,0.8);">报表管理</el-menu-item>
              <el-menu-item index="/datasources" style="min-width: 120px; color: rgba(255,255,255,0.8);">数据源管理</el-menu-item>
              <el-menu-item index="/users" style="min-width: 120px; color: rgba(255,255,255,0.8);">用户管理</el-menu-item>
              <el-menu-item index="/settings" style="min-width: 120px; color: rgba(255,255,255,0.8);">系统设置</el-menu-item>
            </el-menu>
          </nav>
          
          <!-- 移动端菜单按钮 -->
          <div class="mobile-menu-toggle" @click="toggleMobileMenu">
            <el-icon><Menu /></el-icon>
          </div>
          
          <!-- 用户信息 -->
          <div class="user-section">
            <el-dropdown @command="handleUserCommand">
              <span class="user-info">
                <el-avatar :size="32" :src="userAvatar">{{ username?.charAt(0)?.toUpperCase() }}</el-avatar>
                <span class="username">{{ username }}</span>
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人资料</el-dropdown-item>
                  <el-dropdown-item command="settings">设置</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </header>
      
      <!-- 移动端侧边菜单 -->
      <div v-if="showMobileMenu" class="mobile-menu-overlay" @click="closeMobileMenu"></div>
      <div v-if="showMobileMenu" class="mobile-menu">
        <div class="mobile-menu-header">
          <h3>MongoReporter</h3>
          <el-button type="text" @click="closeMobileMenu">
            <el-icon><Close /></el-icon>
          </el-button>
        </div>
        <nav class="mobile-nav">
          <el-menu mode="vertical" :router="true" :default-active="route.path" @select="closeMobileMenu">
            <el-menu-item index="/dashboard">
              <el-icon><DataBoard /></el-icon>
              <span>仪表板</span>
            </el-menu-item>
            <el-menu-item index="/reports">
              <el-icon><Document /></el-icon>
              <span>报表管理</span>
            </el-menu-item>
            <el-menu-item index="/datasources">
              <el-icon><Connection /></el-icon>
              <span>数据源管理</span>
            </el-menu-item>
            <el-menu-item index="/users">
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </el-menu-item>
            <el-menu-item index="/settings">
              <el-icon><Setting /></el-icon>
              <span>系统设置</span>
            </el-menu-item>
          </el-menu>
        </nav>
      </div>
      
      <!-- 主内容区域 -->
      <main class="app-main">
        <router-view />
      </main>
    </div>
  </div>
</template>

<style scoped>
#app {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.app-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 0;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  position: relative;
  z-index: 1000;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 60px;
}

.logo-section {
  display: flex;
  align-items: center;
}

.app-title {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 600;
  color: white;
}

.desktop-nav {
  flex: 1;
  display: flex;
  justify-content: center;
}

.mobile-menu-toggle {
  display: none;
  cursor: pointer;
  padding: 8px;
  color: white;
  font-size: 20px;
}

.user-section {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: rgba(255,255,255,0.1);
}

.username {
  color: white;
  font-weight: 500;
}

.app-main {
  flex: 1;
  overflow: auto;
  background-color: #f5f7fa;
}

/* 移动端菜单 */
.mobile-menu-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0,0,0,0.5);
  z-index: 999;
}

.mobile-menu {
  position: fixed;
  top: 0;
  left: 0;
  width: 280px;
  height: 100vh;
  background-color: white;
  z-index: 1001;
  box-shadow: 2px 0 8px rgba(0,0,0,0.1);
  transform: translateX(0);
  transition: transform 0.3s ease;
}

.mobile-menu-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.mobile-menu-header h3 {
  margin: 0;
  font-size: 1.2rem;
}

.mobile-nav {
  padding: 20px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .desktop-nav {
    display: none;
  }
  
  .mobile-menu-toggle {
    display: block;
  }
  
  .username {
    display: none;
  }
  
  .header-content {
    padding: 0 15px;
  }
  
  .app-title {
    font-size: 1.2rem;
  }
}

@media (max-width: 480px) {
  .header-content {
    padding: 0 10px;
  }
  
  .app-title {
    font-size: 1rem;
  }
  
  .mobile-menu {
    width: 100%;
  }
}

/* 全局样式优化 */
:deep(.el-menu--horizontal) {
  border-bottom: none;
}

:deep(.el-menu-item) {
  color: rgba(255,255,255,0.8) !important;
}

:deep(.el-menu-item:hover) {
  color: white !important;
  background-color: rgba(255,255,255,0.1) !important;
}

:deep(.el-menu-item.is-active) {
  color: white !important;
  background-color: rgba(255,255,255,0.2) !important;
}
</style>
