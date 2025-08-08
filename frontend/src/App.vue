<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Menu, ArrowDown, Close, DataBoard, Document, Connection, User, Setting, Moon, Sunny, Files } from '@element-plus/icons-vue'
import Login from './views/Login.vue'
import { useTheme } from './composables/useTheme'

const router = useRouter()
const route = useRoute()

const isLoggedIn = ref(false)
const username = ref('')
const userAvatar = ref('')
const showMobileMenu = ref(false)

// 主题管理
const { currentTheme, isDark, toggleTheme, THEME_TYPES } = useTheme()

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
  <div id="app" :class="{ 'dark-theme': isDark }">
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
              <el-menu-item index="/templates" style="min-width: 120px; color: rgba(255,255,255,0.8);">模板管理</el-menu-item>
              <el-menu-item index="/datasources" style="min-width: 120px; color: rgba(255,255,255,0.8);">数据源管理</el-menu-item>
              <el-menu-item index="/users" style="min-width: 120px; color: rgba(255,255,255,0.8);">用户管理</el-menu-item>
              <el-menu-item index="/settings" style="min-width: 120px; color: rgba(255,255,255,0.9) !important; font-weight: 700; background: linear-gradient(135deg, rgba(255,255,255,0.2) 0%, rgba(255,255,255,0.1) 100%) !important; border: 2px solid rgba(255,255,255,0.3) !important; border-radius: 12px !important; margin: 0 8px !important; padding: 0 16px !important; position: relative; overflow: hidden;">
                <span style="display: flex; align-items: center; gap: 8px; font-size: 14px;">
                  <span style="font-size: 16px;">⚙️</span>
                  系统设置
                </span>
                <div style="position: absolute; top: 0; left: -100%; width: 100%; height: 100%; background: linear-gradient(90deg, transparent, rgba(255,255,255,0.3), transparent); transition: left 0.6s ease;"></div>
              </el-menu-item>
            </el-menu>
          </nav>
          
          <!-- 移动端菜单按钮 -->
          <div class="mobile-menu-toggle" @click="toggleMobileMenu">
            <el-icon><Menu /></el-icon>
          </div>
          
          <!-- 用户信息和主题切换 -->
          <div class="user-section" style="display: flex !important; align-items: center; gap: 16px; position: relative; z-index: 1001; visibility: visible !important; opacity: 1 !important;">
            <!-- 主题切换按钮 -->
            <el-button
              class="theme-toggle"
              link
              @click="toggleTheme"
              :title="isDark ? '切换到浅色主题' : '切换到深色主题'"
              style="display: flex !important; align-items: center; justify-content: center; visibility: visible !important; opacity: 1 !important; color: white !important; padding: 10px; border-radius: 8px; background: rgba(255,255,255,0.1); backdrop-filter: blur(10px); border: 1px solid rgba(255,255,255,0.2);"
            >
              <el-icon :size="20">
                <Sunny v-if="isDark" />
                <Moon v-else />
              </el-icon>
            </el-button>
            
            <!-- 用户信息 -->
            <el-dropdown @command="handleUserCommand">
              <span class="user-info" style="display: flex !important; align-items: center; gap: 10px; cursor: pointer; padding: 8px 12px; border-radius: 8px; background: rgba(255,255,255,0.1); backdrop-filter: blur(10px); border: 1px solid rgba(255,255,255,0.2); visibility: visible !important; opacity: 1 !important;">
                <el-avatar :size="32" :src="userAvatar">{{ username?.charAt(0)?.toUpperCase() }}</el-avatar>
                <span class="username" style="color: white; font-weight: 600; font-size: 0.9rem; text-shadow: 0 1px 2px rgba(0,0,0,0.3); display: inline-block !important; visibility: visible !important; opacity: 1 !important;">{{ username }}</span>
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
          <div class="mobile-menu-actions">
            <!-- 移动端主题切换 -->
            <el-button
              class="theme-toggle-mobile"
              link
              @click="toggleTheme"
              :title="isDark ? '切换到浅色主题' : '切换到深色主题'"
            >
              <el-icon :size="18">
                <Sunny v-if="isDark" />
                <Moon v-else />
              </el-icon>
            </el-button>
            <el-button link @click="closeMobileMenu">
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
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
            <el-menu-item index="/templates">
              <el-icon><Files /></el-icon>
              <span>模板管理</span>
            </el-menu-item>
            <el-menu-item index="/datasources">
              <el-icon><Connection /></el-icon>
              <span>数据源管理</span>
            </el-menu-item>
            <el-menu-item index="/users">
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </el-menu-item>
            <el-menu-item index="/settings" style="font-weight: 700; color: var(--el-color-primary); background: linear-gradient(135deg, rgba(64, 158, 255, 0.1) 0%, rgba(64, 158, 255, 0.05) 100%) !important; border: 2px solid rgba(64, 158, 255, 0.3) !important; border-radius: 12px !important; margin: 8px 16px !important; position: relative; overflow: hidden;">
              <el-icon><Setting /></el-icon>
              <span style="display: flex; align-items: center; gap: 8px; font-size: 14px;">
                <span style="font-size: 16px;">⚙️</span>
                系统设置
              </span>
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
  transition: all 0.3s ease;
}

/* 深色主题样式 */
#app.dark-theme {
  background-color: var(--theme-background, #1a1a1a);
  color: var(--theme-text, #ffffff);
}

.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--theme-gradient, linear-gradient(135deg, #667eea 0%, #764ba2 100%));
  transition: all 0.3s ease;
}

.app-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--theme-background, #f5f7fa);
  transition: all 0.3s ease;
}

.app-header {
  background: var(--theme-gradient, linear-gradient(135deg, #667eea 0%, #764ba2 100%));
  color: white;
  padding: 0;
  box-shadow: var(--theme-shadow, 0 4px 20px rgba(0,0,0,0.15));
  position: relative;
  z-index: 1000;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255,255,255,0.1);
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 70px;
  max-width: 1400px;
  margin: 0 auto;
  position: relative;
  z-index: 1000;
}

.logo-section {
  display: flex;
  align-items: center;
  gap: 12px;
}

.app-title {
  margin: 0;
  font-size: 1.6rem;
  font-weight: 700;
  color: white;
  text-shadow: 0 2px 8px rgba(0,0,0,0.3);
  background: linear-gradient(135deg, #ffffff 0%, #f0f0f0 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 0.5px;
  transition: all 0.3s ease;
}

.app-title:hover {
  transform: scale(1.05);
  text-shadow: 0 4px 12px rgba(0,0,0,0.4);
}

.desktop-nav {
  flex: 1;
  display: flex;
  justify-content: center;
  margin: 0 40px;
}

.mobile-menu-toggle {
  display: none;
  cursor: pointer;
  padding: 10px;
  color: white;
  font-size: 20px;
  transition: all 0.3s ease;
  border-radius: 8px;
  background: rgba(255,255,255,0.1);
  backdrop-filter: blur(10px);
}

.mobile-menu-toggle:hover {
  background-color: rgba(255,255,255,0.2);
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(0,0,0,0.2);
}

.user-section {
  display: flex !important;
  align-items: center;
  gap: 16px;
  position: relative;
  z-index: 1001;
  visibility: visible !important;
  opacity: 1 !important;
  min-width: auto !important;
  max-width: none !important;
  width: auto !important;
  height: auto !important;
}

.theme-toggle {
  color: white !important;
  padding: 10px;
  border-radius: 8px;
  transition: all 0.3s ease;
  background: rgba(255,255,255,0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255,255,255,0.2);
  display: flex !important;
  align-items: center;
  justify-content: center;
  visibility: visible !important;
  opacity: 1 !important;
  min-width: auto !important;
  max-width: none !important;
  width: auto !important;
  height: auto !important;
}

.theme-toggle:hover {
  background-color: rgba(255,255,255,0.2) !important;
  transform: scale(1.1) rotate(180deg);
  box-shadow: 0 4px 12px rgba(0,0,0,0.2);
}

.user-info {
  display: flex !important;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 8px;
  transition: all 0.3s ease;
  background: rgba(255,255,255,0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255,255,255,0.2);
  visibility: visible !important;
  opacity: 1 !important;
  min-width: auto !important;
  max-width: none !important;
  width: auto !important;
  height: auto !important;
}

.user-info:hover {
  background-color: rgba(255,255,255,0.2);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.2);
}

.username {
  color: white;
  font-weight: 600;
  font-size: 0.9rem;
  text-shadow: 0 1px 2px rgba(0,0,0,0.3);
  display: inline-block !important;
  visibility: visible !important;
  opacity: 1 !important;
  min-width: auto !important;
  max-width: none !important;
  width: auto !important;
  height: auto !important;
}

.app-main {
  flex: 1;
  overflow: auto;
  background-color: var(--theme-background, #f5f7fa);
  transition: all 0.3s ease;
}

/* 移动端菜单 */
.mobile-menu-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0,0,0,0.6);
  z-index: 999;
  backdrop-filter: blur(8px);
  animation: fadeIn 0.3s ease;
}

.mobile-menu {
  position: fixed;
  top: 0;
  left: 0;
  width: 300px;
  height: 100vh;
  background: linear-gradient(135deg, var(--theme-surface, #ffffff) 0%, var(--theme-background, #f5f7fa) 100%);
  z-index: 1001;
  box-shadow: var(--theme-shadow, 4px 0 24px rgba(0,0,0,0.15));
  transform: translateX(0);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-right: 1px solid rgba(0,0,0,0.1);
}

.mobile-menu-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px;
  background: var(--theme-gradient, linear-gradient(135deg, #667eea 0%, #764ba2 100%));
  color: white;
  border-bottom: 1px solid rgba(255,255,255,0.1);
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.mobile-menu-header h3 {
  margin: 0;
  font-size: 1.3rem;
  font-weight: 700;
  text-shadow: 0 2px 8px rgba(0,0,0,0.3);
  background: linear-gradient(135deg, #ffffff 0%, #f0f0f0 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.mobile-menu-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.theme-toggle-mobile {
  color: white !important;
  padding: 8px;
  border-radius: 8px;
  transition: all 0.3s ease;
  background: rgba(255,255,255,0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255,255,255,0.2);
}

.theme-toggle-mobile:hover {
  background-color: rgba(255,255,255,0.2) !important;
  transform: scale(1.1) rotate(180deg);
  box-shadow: 0 4px 12px rgba(0,0,0,0.2);
}

.mobile-nav {
  padding: 20px 0;
  height: calc(100vh - 80px);
  overflow-y: auto;
}

.mobile-nav :deep(.el-menu) {
  border: none;
  background: transparent;
}

.mobile-nav :deep(.el-menu-item) {
  margin: 4px 16px;
  border-radius: 8px;
  transition: all 0.3s ease;
  color: var(--theme-text, #303133);
  font-weight: 500;
  position: relative;
  overflow: hidden;
}

.mobile-nav :deep(.el-menu-item::before) {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(102, 126, 234, 0.1), transparent);
  transition: left 0.5s ease;
}

.mobile-nav :deep(.el-menu-item:hover::before) {
  left: 100%;
}

.mobile-nav :deep(.el-menu-item:hover) {
  background: rgba(102, 126, 234, 0.1) !important;
  transform: translateX(8px);
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.2);
}

.mobile-nav :deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, rgba(255,255,255,0.95) 0%, rgba(255,255,255,0.9) 100%) !important;
  color: #1a1a1a !important;
  box-shadow: 0 4px 12px rgba(0,0,0,0.2);
  transform: translateX(8px);
  border: 1px solid rgba(255,255,255,0.5);
  font-weight: 600;
  text-shadow: 0 1px 2px rgba(0,0,0,0.1);
}

.mobile-nav :deep(.el-menu-item.is-active::after) {
  content: '';
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  width: 6px;
  height: 6px;
  background: var(--theme-primary, #667eea);
  border-radius: 50%;
  animation: pulse 2s infinite;
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
    display: inline-block !important;
  }
  
  .header-content {
    padding: 0 16px;
  }
  
  .app-title {
    font-size: 1.3rem;
  }
  
  .theme-toggle {
    padding: 8px;
  }
  
  .user-info {
    padding: 6px 10px;
  }
}

@media (max-width: 480px) {
  .header-content {
    padding: 0 12px;
  }
  
  .app-title {
    font-size: 1.1rem;
  }
  
  .mobile-menu {
    width: 100%;
  }
  
  .user-section {
    gap: 8px;
  }
  
  .theme-toggle {
    padding: 6px;
  }
  
  .user-info {
    padding: 4px 8px;
  }
}

/* 全局样式优化 */
:deep(.el-menu--horizontal) {
  border-bottom: none;
  background: transparent;
}

:deep(.el-menu-item) {
  color: rgba(255,255,255,0.9) !important;
  transition: all 0.3s ease;
  border-radius: 8px;
  margin: 0 4px;
  font-weight: 500;
  position: relative;
  overflow: hidden;
}

:deep(.el-menu-item::before) {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
  transition: left 0.5s ease;
}

:deep(.el-menu-item:hover::before) {
  left: 100%;
}

:deep(.el-menu-item:hover) {
  color: white !important;
  background: rgba(255,255,255,0.15) !important;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.2);
  backdrop-filter: blur(10px);
}

:deep(.el-menu-item.is-active) {
  color: #1a1a1a !important;
  background: rgba(255,255,255,0.9) !important;
  box-shadow: 0 4px 16px rgba(0,0,0,0.3);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255,255,255,0.5);
  transform: translateY(-2px);
  font-weight: 600;
  text-shadow: 0 1px 2px rgba(0,0,0,0.1);
}

:deep(.el-menu-item.is-active::after) {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 60%;
  height: 2px;
  background: linear-gradient(90deg, transparent, #ffffff, transparent);
  border-radius: 1px;
  animation: pulse 2s infinite;
}

/* 系统设置菜单特殊样式 */
:deep(.el-menu-item[index="/settings"]) {
  color: rgba(255,255,255,0.9) !important;
  font-weight: 700 !important;
  background: linear-gradient(135deg, rgba(255,255,255,0.2) 0%, rgba(255,255,255,0.1) 100%) !important;
  border: 2px solid rgba(255,255,255,0.3) !important;
  border-radius: 12px !important;
  margin: 0 8px !important;
  padding: 0 16px !important;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease !important;
  box-shadow: 0 4px 12px rgba(0,0,0,0.2) !important;
}

:deep(.el-menu-item[index="/settings"]:hover) {
  color: white !important;
  background: linear-gradient(135deg, rgba(255,255,255,0.3) 0%, rgba(255,255,255,0.2) 100%) !important;
  border-color: rgba(255,255,255,0.5) !important;
  transform: translateY(-3px) scale(1.05) !important;
  box-shadow: 0 8px 25px rgba(0,0,0,0.3) !important;
}

:deep(.el-menu-item[index="/settings"].is-active) {
  color: #1a1a1a !important;
  background: linear-gradient(135deg, rgba(255,255,255,0.95) 0%, rgba(255,255,255,0.9) 100%) !important;
  border-color: rgba(255,255,255,0.8) !important;
  transform: translateY(-3px) scale(1.05) !important;
  box-shadow: 0 8px 25px rgba(0,0,0,0.4) !important;
  font-weight: 800 !important;
}

:deep(.el-menu-item[index="/settings"]::before) {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.4), transparent);
  transition: left 0.6s ease;
}

:deep(.el-menu-item[index="/settings"]:hover::before) {
  left: 100%;
}

:deep(.el-menu-item[index="/settings"]::after) {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 0;
  height: 3px;
  background: linear-gradient(90deg, #ffffff, #f0f0f0, #ffffff);
  border-radius: 2px;
  transition: width 0.3s ease;
}

:deep(.el-menu-item[index="/settings"]:hover::after) {
  width: 80%;
}

:deep(.el-menu-item[index="/settings"].is-active::after) {
  width: 80%;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 0.6;
    transform: translateX(-50%) scaleX(1);
  }
  50% {
    opacity: 1;
    transform: translateX(-50%) scaleX(1.2);
  }
}

/* 深色主题下的菜单样式 */
.dark-theme :deep(.el-menu-item) {
  color: rgba(255,255,255,0.9) !important;
}

.dark-theme :deep(.el-menu-item:hover) {
  color: white !important;
  background: rgba(255,255,255,0.15) !important;
}

.dark-theme :deep(.el-menu-item.is-active) {
  color: #1a1a1a !important;
  background: rgba(255,255,255,0.95) !important;
  font-weight: 600;
  text-shadow: 0 1px 2px rgba(0,0,0,0.1);
}

/* 动画效果 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.app-container {
  animation: fadeIn 0.3s ease;
}

.header-content {
  animation: slideIn 0.4s ease;
}

/* 滚动条样式 */
.app-main::-webkit-scrollbar {
  width: 8px;
}

.app-main::-webkit-scrollbar-track {
  background: var(--theme-background, #f5f7fa);
}

.app-main::-webkit-scrollbar-thumb {
  background: var(--theme-border, #DCDFE6);
  border-radius: 4px;
}

.app-main::-webkit-scrollbar-thumb:hover {
  background: var(--theme-textSecondary, #606266);
}

/* 深色主题滚动条 */
.dark-theme .app-main::-webkit-scrollbar-track {
  background: var(--theme-background, #1a1a1a);
}

.dark-theme .app-main::-webkit-scrollbar-thumb {
  background: var(--theme-border, #404040);
}

.dark-theme .app-main::-webkit-scrollbar-thumb:hover {
  background: var(--theme-textSecondary, #b0b0b0);
}

/* 移动端菜单滚动条 */
.mobile-nav::-webkit-scrollbar {
  width: 4px;
}

.mobile-nav::-webkit-scrollbar-track {
  background: transparent;
}

.mobile-nav::-webkit-scrollbar-thumb {
  background: rgba(0,0,0,0.2);
  border-radius: 2px;
}

.mobile-nav::-webkit-scrollbar-thumb:hover {
  background: rgba(0,0,0,0.3);
}

/* 深色主题移动端菜单滚动条 */
.dark-theme .mobile-nav::-webkit-scrollbar-thumb {
  background: rgba(255,255,255,0.2);
}

.dark-theme .mobile-nav::-webkit-scrollbar-thumb:hover {
  background: rgba(255,255,255,0.3);
}

/* 移动端系统设置菜单特殊样式 */
.mobile-nav :deep(.el-menu-item[index="/settings"]) {
  font-weight: 700 !important;
  color: var(--el-color-primary) !important;
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.1) 0%, rgba(64, 158, 255, 0.05) 100%) !important;
  border: 2px solid rgba(64, 158, 255, 0.3) !important;
  border-radius: 12px !important;
  margin: 8px 16px !important;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease !important;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2) !important;
}

.mobile-nav :deep(.el-menu-item[index="/settings"]:hover) {
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.2) 0%, rgba(64, 158, 255, 0.1) 100%) !important;
  border-color: rgba(64, 158, 255, 0.5) !important;
  transform: translateX(8px) scale(1.02) !important;
  box-shadow: 0 6px 20px rgba(64, 158, 255, 0.3) !important;
}

.mobile-nav :deep(.el-menu-item[index="/settings"].is-active) {
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.3) 0%, rgba(64, 158, 255, 0.2) 100%) !important;
  border-color: rgba(64, 158, 255, 0.8) !important;
  transform: translateX(8px) scale(1.02) !important;
  box-shadow: 0 6px 20px rgba(64, 158, 255, 0.4) !important;
  font-weight: 800 !important;
}

.mobile-nav :deep(.el-menu-item[index="/settings"]::before) {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(64, 158, 255, 0.3), transparent);
  transition: left 0.6s ease;
}

.mobile-nav :deep(.el-menu-item[index="/settings"]:hover::before) {
  left: 100%;
}
</style>
