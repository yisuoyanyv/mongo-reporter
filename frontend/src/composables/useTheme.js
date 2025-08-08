import { ref, watch, onMounted } from 'vue'

// 主题类型
export const THEME_TYPES = {
  LIGHT: 'light',
  DARK: 'dark',
  AUTO: 'auto'
}

// 主题配置
const themes = {
  light: {
    name: '浅色主题',
    primary: '#409EFF',
    success: '#67C23A',
    warning: '#E6A23C',
    danger: '#F56C6C',
    info: '#909399',
    background: '#f5f7fa',
    surface: '#ffffff',
    text: '#303133',
    textSecondary: '#606266',
    border: '#DCDFE6',
    shadow: '0 2px 12px 0 rgba(0, 0, 0, 0.1)',
    gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    cardBackground: '#ffffff',
    cardShadow: '0 2px 8px rgba(0, 0, 0, 0.1)',
    sidebarBackground: '#ffffff',
    headerBackground: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    footerBackground: '#f5f7fa'
  },
  dark: {
    name: '深色主题',
    primary: '#409EFF',
    success: '#67C23A',
    warning: '#E6A23C',
    danger: '#F56C6C',
    info: '#909399',
    background: '#1a1a1a',
    surface: '#2d2d2d',
    text: '#ffffff',
    textSecondary: '#b0b0b0',
    border: '#404040',
    shadow: '0 2px 12px 0 rgba(0, 0, 0, 0.3)',
    gradient: 'linear-gradient(135deg, #2c3e50 0%, #34495e 100%)',
    cardBackground: '#2d2d2d',
    cardShadow: '0 2px 8px rgba(0, 0, 0, 0.3)',
    sidebarBackground: '#2d2d2d',
    headerBackground: 'linear-gradient(135deg, #2c3e50 0%, #34495e 100%)',
    footerBackground: '#1a1a1a'
  },
  blue: {
    name: '蓝色主题',
    primary: '#1890ff',
    success: '#52c41a',
    warning: '#faad14',
    danger: '#ff4d4f',
    info: '#1890ff',
    background: '#f0f2f5',
    surface: '#ffffff',
    text: '#262626',
    textSecondary: '#595959',
    border: '#d9d9d9',
    shadow: '0 2px 8px rgba(24, 144, 255, 0.15)',
    gradient: 'linear-gradient(135deg, #1890ff 0%, #096dd9 100%)',
    cardBackground: '#ffffff',
    cardShadow: '0 2px 8px rgba(24, 144, 255, 0.1)',
    sidebarBackground: '#001529',
    headerBackground: 'linear-gradient(135deg, #1890ff 0%, #096dd9 100%)',
    footerBackground: '#f0f2f5'
  },
  green: {
    name: '绿色主题',
    primary: '#52c41a',
    success: '#52c41a',
    warning: '#faad14',
    danger: '#ff4d4f',
    info: '#1890ff',
    background: '#f6ffed',
    surface: '#ffffff',
    text: '#262626',
    textSecondary: '#595959',
    border: '#b7eb8f',
    shadow: '0 2px 8px rgba(82, 196, 26, 0.15)',
    gradient: 'linear-gradient(135deg, #52c41a 0%, #389e0d 100%)',
    cardBackground: '#ffffff',
    cardShadow: '0 2px 8px rgba(82, 196, 26, 0.1)',
    sidebarBackground: '#f6ffed',
    headerBackground: 'linear-gradient(135deg, #52c41a 0%, #389e0d 100%)',
    footerBackground: '#f6ffed'
  },
  purple: {
    name: '紫色主题',
    primary: '#722ed1',
    success: '#52c41a',
    warning: '#faad14',
    danger: '#ff4d4f',
    info: '#1890ff',
    background: '#f9f0ff',
    surface: '#ffffff',
    text: '#262626',
    textSecondary: '#595959',
    border: '#d3adf7',
    shadow: '0 2px 8px rgba(114, 46, 209, 0.15)',
    gradient: 'linear-gradient(135deg, #722ed1 0%, #531dab 100%)',
    cardBackground: '#ffffff',
    cardShadow: '0 2px 8px rgba(114, 46, 209, 0.1)',
    sidebarBackground: '#f9f0ff',
    headerBackground: 'linear-gradient(135deg, #722ed1 0%, #531dab 100%)',
    footerBackground: '#f9f0ff'
  }
}

// 当前主题状态
const currentTheme = ref(THEME_TYPES.LIGHT)
const isDark = ref(false)
const customPrimaryColor = ref('#409EFF')

// 获取系统主题偏好
const getSystemTheme = () => {
  return window.matchMedia('(prefers-color-scheme: dark)').matches ? THEME_TYPES.DARK : THEME_TYPES.LIGHT
}

// 应用主题
const applyTheme = (theme) => {
  const root = document.documentElement
  const themeConfig = themes[theme] || themes.light
  
  // 设置CSS变量
  Object.entries(themeConfig).forEach(([key, value]) => {
    root.style.setProperty(`--el-color-${key}`, value)
    root.style.setProperty(`--theme-${key}`, value)
  })
  
  // 设置自定义主色调
  if (customPrimaryColor.value) {
    root.style.setProperty('--el-color-primary', customPrimaryColor.value)
    root.style.setProperty('--theme-primary', customPrimaryColor.value)
  }
  
  // 设置Element Plus主题
  if (theme === THEME_TYPES.DARK) {
    root.classList.add('dark')
    isDark.value = true
  } else {
    root.classList.remove('dark')
    isDark.value = false
  }
  
  // 保存主题设置
  localStorage.setItem('theme', theme)
  localStorage.setItem('customPrimaryColor', customPrimaryColor.value)
  currentTheme.value = theme
}

// 切换主题
const toggleTheme = () => {
  const newTheme = currentTheme.value === THEME_TYPES.LIGHT ? THEME_TYPES.DARK : THEME_TYPES.LIGHT
  applyTheme(newTheme)
}

// 设置主题
const setTheme = (theme) => {
  if (theme === THEME_TYPES.AUTO) {
    const systemTheme = getSystemTheme()
    applyTheme(systemTheme)
  } else {
    applyTheme(theme)
  }
}

// 设置自定义主色调
const setCustomPrimaryColor = (color) => {
  customPrimaryColor.value = color
  applyTheme(currentTheme.value)
}

// 获取当前主题配置
const getCurrentThemeConfig = () => {
  const theme = currentTheme.value === THEME_TYPES.AUTO ? getSystemTheme() : currentTheme.value
  return themes[theme] || themes.light
}

// 获取所有可用主题
const getAvailableThemes = () => {
  return Object.entries(themes).map(([key, config]) => ({
    key,
    name: config.name,
    primary: config.primary,
    background: config.background
  }))
}

// 监听系统主题变化
const watchSystemTheme = () => {
  const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
  mediaQuery.addEventListener('change', (e) => {
    if (currentTheme.value === THEME_TYPES.AUTO) {
      const newTheme = e.matches ? THEME_TYPES.DARK : THEME_TYPES.LIGHT
      applyTheme(newTheme)
    }
  })
}

// 初始化主题
const initTheme = () => {
  const savedTheme = localStorage.getItem('theme') || THEME_TYPES.LIGHT
  const savedPrimaryColor = localStorage.getItem('customPrimaryColor') || '#409EFF'
  
  customPrimaryColor.value = savedPrimaryColor
  setTheme(savedTheme)
  watchSystemTheme()
}

// 导出主题管理函数
export function useTheme() {
  onMounted(() => {
    initTheme()
  })
  
  return {
    currentTheme,
    isDark,
    themes,
    THEME_TYPES,
    customPrimaryColor,
    toggleTheme,
    setTheme,
    setCustomPrimaryColor,
    getCurrentThemeConfig,
    getAvailableThemes,
    applyTheme
  }
} 