<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <!-- 欢迎区域 -->
      <el-col :span="24">
        <el-card class="welcome-card">
          <div class="welcome-content">
            <div class="welcome-text">
              <h2>欢迎使用 MongoReporter</h2>
              <p>MongoDB报表生成系统 - 让数据可视化变得简单</p>
            </div>
            <div class="welcome-actions">
              <el-button type="primary" @click="$router.push('/designer')">
                <el-icon><Plus /></el-icon>
                创建新报表
              </el-button>
              <el-button @click="$router.push('/reports')">
                <el-icon><Document /></el-icon>
                查看所有报表
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>


    <el-row :gutter="20" class="section">
      <!-- 统计卡片 -->
      <el-col :xs="24" :sm="12" :md="6" v-for="stat in stats" :key="stat.title">
        <el-card class="stat-card" :class="stat.type">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="24">
                <component :is="stat.icon" />
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stat.value }}</div>
              <div class="stat-title">{{ stat.title }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="section">
      <!-- 图表区域 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>报表创建趋势</span>
              <el-button link @click="refreshChartData('reportTrend')">刷新</el-button>
            </div>
          </template>
          <div class="chart-container">
            <v-chart :option="reportTrendOption" :style="{ height: '300px' }" />
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>用户活跃度</span>
              <el-button link @click="refreshChartData('userActivity')">刷新</el-button>
            </div>
          </template>
          <div class="chart-container">
            <v-chart :option="userActivityOption" :style="{ height: '300px' }" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="section">
      <!-- 报表分类统计 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>报表分类统计</span>
              <el-button link @click="refreshChartData('reportCategories')">刷新</el-button>
            </div>
          </template>
          <div class="chart-container">
            <v-chart :option="reportCategoriesOption" :style="{ height: '300px' }" />
          </div>
        </el-card>
      </el-col>

      <!-- 系统监控快捷入口 -->
      <el-col :span="12">
        <el-card class="quick-monitor-card">
          <template #header>
            <div class="card-header">
              <span>系统监控</span>
              <el-button link @click="$router.push('/monitor')">前往</el-button>
            </div>
          </template>
          <div class="quick-monitor">
            <el-icon :size="28" style="margin-right:8px"><Connection /></el-icon>
            <div>
              <div style="font-weight:600;">查看CPU/内存/磁盘等实时性能</div>
              <div style="opacity:0.8;font-size:12px;">更详细的数据请前往系统监控页面</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- 最近报表 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最近报表</span>
              <el-button link @click="$router.push('/reports')">查看全部</el-button>
            </div>
          </template>
          <div v-if="recentReports.length === 0" class="empty-state">
            <el-empty description="暂无报表" />
          </div>
          <div v-else class="recent-reports">
            <div v-for="report in recentReports" :key="report.id" class="report-item">
              <div class="report-info">
                <div class="report-name">{{ report.name }}</div>
                <div class="report-meta">
                  <el-tag v-if="report.publicShare" size="small" type="success">公开</el-tag>
                  <el-tag v-else size="small" type="info">私有</el-tag>
                  <span class="report-date">{{ formatDate(report.updatedAt) }}</span>
                </div>
              </div>
              <div class="report-actions">
                <el-button size="small" @click="viewReport(report.id)">查看</el-button>
                <el-button size="small" @click="editReport(report.id)">编辑</el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 系统信息 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>系统信息</span>
              <el-button link @click="refreshSystemInfo">刷新</el-button>
            </div>
          </template>
          <div v-if="systemInfo" class="system-info">
            <div class="info-item">
              <span class="info-label">系统状态:</span>
              <el-tag type="success">运行中</el-tag>
            </div>
            <div class="info-item">
              <span class="info-label">版本:</span>
              <span>{{ systemInfo.version }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">Java版本:</span>
              <span>{{ systemInfo.javaVersion }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">操作系统:</span>
              <span>{{ systemInfo.osName }} {{ systemInfo.osVersion }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">启动时间:</span>
              <span>{{ formatDate(systemInfo.startTime) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">内存使用:</span>
              <span>{{ formatMemory(systemInfo.totalMemory, systemInfo.freeMemory) }}</span>
            </div>
          </div>
          <div v-else class="loading-state">
            <el-skeleton :rows="5" animated />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- 快速操作 -->
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>快速操作</span>
            </div>
          </template>
          <div class="quick-actions">
            <el-button-group>
              <el-button @click="$router.push('/datasources')">
                <el-icon><Connection /></el-icon>
                管理数据源
              </el-button>
              <el-button @click="$router.push('/designer')">
                <el-icon><Edit /></el-icon>
                设计报表
              </el-button>
              <el-button @click="$router.push('/reports')">
                <el-icon><List /></el-icon>
                报表列表
              </el-button>
              <el-button @click="exportAllReports">
                <el-icon><Download /></el-icon>
                导出报表
              </el-button>
            </el-button-group>
          </div>
          
          <!-- 新增：快速报表创建 -->
          <div class="quick-report-creation" style="margin-top: 20px;">
            <h4>快速创建报表</h4>
            <div class="quick-report-templates">
              <el-card 
                v-for="template in quickTemplates" 
                :key="template.id" 
                class="quick-template-card"
                @click="createQuickReport(template)"
              >
                <div class="template-icon">
                  <el-icon :size="32">
                    <component :is="template.icon" />
                  </el-icon>
                </div>
                <div class="template-info">
                  <div class="template-name">{{ template.name }}</div>
                  <div class="template-desc">{{ template.description }}</div>
                </div>
              </el-card>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 新增：实时通知区域 -->
    <el-row :gutter="20" class="section">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>系统通知</span>
              <div class="notification-actions">
                <el-button link @click="markAllAsRead">全部标记为已读</el-button>
                <el-button link @click="clearNotifications">清空通知</el-button>
              </div>
            </div>
          </template>
          <div v-if="notifications.length === 0" class="empty-state">
            <el-empty description="暂无通知" />
          </div>
          <div v-else class="notifications">
            <div v-for="notification in notifications" :key="notification.id" 
                 class="notification-item" :class="{ 'unread': !notification.read }">
              <div class="notification-icon">
                <el-icon :size="20" :class="notification.type">
                  <component :is="notification.icon" />
                </el-icon>
              </div>
              <div class="notification-content">
                <div class="notification-title">{{ notification.title }}</div>
                <div class="notification-message">{{ notification.message }}</div>
                <div class="notification-time">{{ formatTime(notification.time) }}</div>
              </div>
              <div class="notification-actions">
                <el-button v-if="!notification.read" size="small" link @click="markAsRead(notification.id)">
                  标记已读
                </el-button>
                <el-button size="small" link @click="dismissNotification(notification.id)">
                  关闭
                </el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 新增：系统状态监控 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>数据源状态</span>
              <el-button link @click="refreshDataSourceStatus">刷新</el-button>
            </div>
          </template>
          <div v-if="dataSourceStatus.length === 0" class="empty-state">
            <el-empty description="暂无数据源" />
          </div>
          <div v-else class="datasource-status">
            <div v-for="ds in dataSourceStatus" :key="ds.id" class="datasource-item">
              <div class="datasource-info">
                <div class="datasource-name">{{ ds.name }}</div>
                <div class="datasource-url">{{ ds.url }}</div>
              </div>
              <div class="datasource-status">
                <el-tag :type="ds.status === 'connected' ? 'success' : 'danger'" size="small">
                  {{ ds.status === 'connected' ? '已连接' : '连接失败' }}
                </el-tag>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最近活动</span>
              <el-button link @click="refreshRecentActivity">刷新</el-button>
            </div>
          </template>
          <div v-if="recentActivity.length === 0" class="empty-state">
            <el-empty description="暂无活动" />
          </div>
          <div v-else class="recent-activity">
            <div v-for="activity in recentActivity" :key="activity.id" class="activity-item">
              <div class="activity-icon">
                <el-icon :size="16">
                  <component :is="activity.icon" />
                </el-icon>
              </div>
              <div class="activity-content">
                <div class="activity-text">{{ activity.text }}</div>
                <div class="activity-time">{{ formatTime(activity.time) }}</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Plus, Document, Connection, Edit, List, Download,
  PieChart, User, DataAnalysis, Files
} from '@element-plus/icons-vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, BarChart, PieChart as EChartsPieChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import axios from 'axios'

// 注册ECharts组件
use([
  CanvasRenderer,
  LineChart,
  BarChart,
  EChartsPieChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
])

const router = useRouter()

// 响应式数据
const stats = ref([
  { title: '总报表数', value: 0, icon: 'Files', type: 'primary' },
  { title: '公开报表', value: 0, icon: 'Document', type: 'success' },
  { title: '数据源', value: 0, icon: 'DataAnalysis', type: 'warning' },
  { title: '用户数', value: 0, icon: 'User', type: 'info' }
])

const recentReports = ref([])
const systemInfo = ref(null)

// 图表数据
const reportTrendOption = ref({})
const userActivityOption = ref({})
const reportCategoriesOption = ref({})

// 通知数据
const notifications = ref([])
const dataSourceStatus = ref([])
const recentActivity = ref([])

// 新增：快速报表模板
const quickTemplates = ref([
  { id: 1, name: '用户行为分析', description: '分析用户在网站上的行为模式', icon: 'User' },
  { id: 2, name: '销售数据报表', description: '统计每日、每周、每月的销售数据', icon: 'PieChart' },
  { id: 3, name: '网站访问统计', description: '监控网站流量和用户访问情况', icon: 'DataAnalysis' },
  { id: 4, name: '错误日志报告', description: '收集和分析系统错误日志', icon: 'Files' }
])

// 自动刷新定时器
let refreshTimer = null

// 获取系统统计信息
const fetchSystemStats = async () => {
  try {
    const response = await axios.get('/api/system/stats')
    const data = response.data
    
    stats.value[0].value = data.reports.total
    stats.value[1].value = data.reports.public
    stats.value[2].value = data.dataSources
    stats.value[3].value = data.users
  } catch (error) {
    console.error('获取系统统计失败:', error)
  }
}

// 获取最近报表
const fetchRecentReports = async () => {
  try {
    const response = await axios.get('/api/report/configs')
    const reports = response.data
    recentReports.value = reports.slice(0, 5) // 只显示最近5个
  } catch (error) {
    console.error('获取最近报表失败:', error)
  }
}

// 获取系统信息
const fetchSystemInfo = async () => {
  try {
    const response = await axios.get('/api/system/info')
    systemInfo.value = response.data
  } catch (error) {
    console.error('获取系统信息失败:', error)
  }
}

// 获取报表趋势数据
const fetchReportTrend = async () => {
  try {
    const response = await axios.get('/api/system/charts/report-trend')
    const data = response.data
    
    reportTrendOption.value = {
      title: {
        text: data.title,
        left: 'center'
      },
      tooltip: {
        trigger: 'axis'
      },
      xAxis: {
        type: 'category',
        data: data.dates
      },
      yAxis: {
        type: 'value'
      },
      series: [{
        data: data.reportCounts,
        type: 'line',
        smooth: true,
        areaStyle: {
          opacity: 0.3
        }
      }]
    }
  } catch (error) {
    console.error('获取报表趋势数据失败:', error)
  }
}

// 获取用户活跃度数据
const fetchUserActivity = async () => {
  try {
    const response = await axios.get('/api/system/charts/user-activity')
    const data = response.data
    
    userActivityOption.value = {
      title: {
        text: data.title,
        left: 'center'
      },
      tooltip: {
        trigger: 'axis'
      },
      xAxis: {
        type: 'category',
        data: data.timeSlots
      },
      yAxis: {
        type: 'value'
      },
      series: [{
        data: data.activeUsers,
        type: 'bar',
        itemStyle: {
          color: '#409EFF'
        }
      }]
    }
  } catch (error) {
    console.error('获取用户活跃度数据失败:', error)
  }
}

// 获取报表分类数据
const fetchReportCategories = async () => {
  try {
    const response = await axios.get('/api/system/charts/report-categories')
    const data = response.data
    
    reportCategoriesOption.value = {
      title: {
        text: data.title,
        left: 'center'
      },
      tooltip: {
        trigger: 'item'
      },
      series: [{
        type: 'pie',
        radius: '50%',
        data: data.categories.map((category, index) => ({
          name: category,
          value: data.counts[index]
        })),
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }]
    }
  } catch (error) {
    console.error('获取报表分类数据失败:', error)
  }
}

// 已移除：系统性能数据由“系统监控”页面负责

// 刷新图表数据
const refreshChartData = async (chartType) => {
  try {
    // 清除缓存
    await axios.post('/api/system/cache/clear')
    
    switch (chartType) {
      case 'reportTrend':
        await fetchReportTrend()
        break
      case 'userActivity':
        await fetchUserActivity()
        break
      case 'reportCategories':
        await fetchReportCategories()
        break
      
    }
    ElMessage.success('数据已刷新')
  } catch (error) {
    console.error('刷新数据失败:', error)
    ElMessage.error('刷新数据失败')
  }
}

// 刷新系统信息
const refreshSystemInfo = () => {
  fetchSystemStats()
  fetchSystemInfo()
  fetchReportTrend()
  fetchUserActivity()
  fetchReportCategories()
  ElMessage.success('系统信息已刷新')
}

// 查看报表
const viewReport = (id) => {
  router.push(`/reports/view/${id}`)
}

// 编辑报表
const editReport = (id) => {
  router.push(`/reports/design/${id}`)
}

// 导出所有报表
const exportAllReports = () => {
  ElMessage.info('导出功能开发中...')
}

// 快速创建报表
const createQuickReport = (template) => {
  ElMessage.success(`即将创建一个基于 "${template.name}" 的快速报表。`)
  // 实际创建报表的逻辑需要调用后端API
  // 例如：router.push('/designer/new?templateId=' + template.id)
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN')
}

// 格式化时间
const formatTime = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleTimeString('zh-CN', { hour: 'numeric', minute: 'numeric' })
}

// 格式化内存使用
const formatMemory = (total, free) => {
  if (!total || !free) return '未知'
  const used = total - free
  const usedMB = Math.round(used / 1024 / 1024)
  const totalMB = Math.round(total / 1024 / 1024)
  const percentage = Math.round((used / total) * 100)
  return `${usedMB}MB / ${totalMB}MB (${percentage}%)`
}

// 添加通知
const addNotification = (type, icon, title, message) => {
  const id = Date.now()
  notifications.value.push({ id, type, icon, title, message, time: new Date() })
  ElMessage.success(`通知: ${title}`)
}

// 关闭通知
const dismissNotification = (id) => {
  const index = notifications.value.findIndex(n => n.id === id)
  if (index !== -1) {
    notifications.value.splice(index, 1)
  }
}

// 清空所有通知
const clearNotifications = () => {
  notifications.value = []
  ElMessage.success('已清空所有通知')
}

// 标记通知为已读
const markAsRead = (id) => {
  const index = notifications.value.findIndex(n => n.id === id)
  if (index !== -1) {
    notifications.value[index].read = true
    ElMessage.success('通知已标记为已读')
  }
}

// 标记所有通知为已读
const markAllAsRead = () => {
  notifications.value.forEach(n => n.read = true)
  ElMessage.success('所有通知已标记为已读')
}

// 刷新数据源状态
const refreshDataSourceStatus = async () => {
  try {
    const response = await axios.get('/api/datasource/status')
    dataSourceStatus.value = response.data
    ElMessage.success('数据源状态已刷新')
  } catch (error) {
    console.error('刷新数据源状态失败:', error)
    addNotification('error', 'DataAnalysis', '数据源状态刷新失败', '无法连接到数据源或获取状态')
  }
}

// 刷新最近活动
const refreshRecentActivity = async () => {
  try {
    const response = await axios.get('/api/system/activity')
    recentActivity.value = response.data.slice(0, 10) // 只显示最近10条
    ElMessage.success('最近活动已刷新')
  } catch (error) {
    console.error('刷新最近活动失败:', error)
    addNotification('error', 'User', '最近活动刷新失败', '无法获取最近活动')
  }
}

// 启动自动刷新
const startAutoRefresh = () => {
  refreshTimer = setInterval(() => {
    fetchSystemStats()
    refreshDataSourceStatus()
    refreshRecentActivity()
  }, 30000) // 每30秒刷新一次
}

// 停止自动刷新
const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

// 组件挂载时获取数据
onMounted(() => {
  fetchSystemStats()
  fetchRecentReports()
  fetchSystemInfo()
  fetchReportTrend()
  fetchUserActivity()
  fetchReportCategories()
  refreshDataSourceStatus()
  refreshRecentActivity()
  startAutoRefresh()
})

// 组件卸载时清理定时器
onUnmounted(() => {
  stopAutoRefresh()
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.welcome-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.welcome-text h2 {
  margin: 0 0 10px 0;
  font-size: 28px;
}

.welcome-text p {
  margin: 0;
  opacity: 0.9;
}

.welcome-actions {
  display: flex;
  gap: 10px;
}

.stat-card {
  height: 120px;
  transition: transform 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.stat-icon {
  margin-right: 15px;
  padding: 10px;
  border-radius: 8px;
  background: rgba(64, 158, 255, 0.1);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.stat-title {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section { margin-top: 20px; }
.quick-monitor { display:flex; align-items:center; padding:12px; }
.quick-monitor-card :deep(.el-card__body) { padding: 16px; }

.chart-container {
  width: 100%;
  height: 300px;
}

.recent-reports {
  max-height: 300px;
  overflow-y: auto;
}

.report-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.report-item:last-child {
  border-bottom: none;
}

.report-info {
  flex: 1;
}

.report-name {
  font-weight: 500;
  margin-bottom: 5px;
}

.report-meta {
  display: flex;
  align-items: center;
  gap: 10px;
}

.report-date {
  font-size: 12px;
  color: #909399;
}

.report-actions {
  display: flex;
  gap: 5px;
}

.system-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 5px 0;
}

.info-label {
  font-weight: 500;
  color: #606266;
}

.quick-actions {
  display: flex;
  justify-content: center;
  gap: 10px;
}

.quick-report-creation {
  margin-top: 20px;
}

.quick-report-templates {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 15px;
  padding: 10px;
}

.quick-template-card {
  cursor: pointer;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 15px;
  text-align: center;
}

.quick-template-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.template-icon {
  margin-bottom: 10px;
  color: #409EFF;
  font-size: 40px;
}

.template-info {
  flex: 1;
}

.template-name {
  font-weight: 600;
  font-size: 16px;
  color: #303133;
  margin-bottom: 5px;
}

.template-desc {
  font-size: 13px;
  color: #909399;
  line-height: 1.4;
}

.empty-state, .loading-state {
  padding: 20px;
  text-align: center;
}

/* 通知样式 */
.notifications {
  max-height: 300px;
  overflow-y: auto;
}

.notification-item {
  display: flex;
  align-items: center;
  padding: 12px 15px;
  border-bottom: 1px solid #f0f0f0;
  background-color: #fdf6ec;
  transition: background-color 0.3s ease;
}

.notification-item:hover {
  background-color: #fef0e6;
}

.notification-item:last-child {
  border-bottom: none;
}

.notification-item.unread {
  background-color: #fffbe6;
  border-left: 4px solid #e6a23c; /* 未读通知的特殊样式 */
}

.notification-icon {
  margin-right: 12px;
  padding: 8px;
  border-radius: 50%;
  background-color: #fffbe6;
  display: flex;
  align-items: center;
  justify-content: center;
}

.notification-icon.success {
  background-color: #f0f9ff;
  color: #409eff;
}

.notification-icon.warning {
  background-color: #fdf6ec;
  color: #e6a23c;
}

.notification-icon.error {
  background-color: #fef0f0;
  color: #f56c6c;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-title {
  font-weight: 600;
  margin-bottom: 4px;
  color: #303133;
  font-size: 14px;
}

.notification-message {
  font-size: 13px;
  color: #606266;
  margin-bottom: 4px;
  line-height: 1.4;
}

.notification-time {
  font-size: 12px;
  color: #909399;
}

.notification-actions {
  display: flex;
  gap: 8px;
  margin-left: 12px;
}

/* 数据源状态样式 */
.datasource-status {
  max-height: 300px;
  overflow-y: auto;
}

.datasource-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 15px;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.3s ease;
}

.datasource-item:hover {
  background-color: #f5f7fa;
}

.datasource-item:last-child {
  border-bottom: none;
}

.datasource-info {
  flex: 1;
  min-width: 0;
}

.datasource-name {
  font-weight: 500;
  margin-bottom: 4px;
  color: #303133;
  font-size: 14px;
}

.datasource-url {
  font-size: 12px;
  color: #909399;
  word-break: break-all;
}

.datasource-status .el-tag {
  font-size: 12px;
  padding: 4px 8px;
}

/* 最近活动样式 */
.recent-activity {
  max-height: 300px;
  overflow-y: auto;
}

.activity-item {
  display: flex;
  align-items: center;
  padding: 12px 15px;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.3s ease;
}

.activity-item:hover {
  background-color: #f5f7fa;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-icon {
  margin-right: 12px;
  padding: 6px;
  border-radius: 50%;
  background-color: #e1f3d8;
  color: #67c23a;
  display: flex;
  align-items: center;
  justify-content: center;
}

.activity-content {
  flex: 1;
  min-width: 0;
}

.activity-text {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
  line-height: 1.4;
}

.activity-time {
  font-size: 12px;
  color: #909399;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .dashboard {
    padding: 10px;
  }
  
  .welcome-content {
    flex-direction: column;
    text-align: center;
    gap: 20px;
  }
  
  .welcome-text h2 {
    font-size: 24px;
  }
  
  .welcome-actions {
    flex-direction: column;
    width: 100%;
  }
  
  .welcome-actions .el-button {
    width: 100%;
    margin-bottom: 10px;
  }
  
  .quick-actions {
    flex-direction: column;
  }
  
  .quick-actions .el-button-group {
    display: flex;
    flex-direction: column;
    width: 100%;
  }
  
  .quick-actions .el-button {
    width: 100%;
    margin-bottom: 8px;
  }
  
  .report-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .report-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  .chart-container {
    height: 250px;
  }
  
  .stat-card {
    height: 100px;
    margin-bottom: 10px;
  }
  
  .stat-value {
    font-size: 20px;
  }
  
  .stat-title {
    font-size: 12px;
  }
  
  .card-header {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }
  
  .system-info .info-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 5px;
  }

  .notifications, .datasource-status, .recent-activity {
    max-height: 250px; /* 调整高度以适应移动端 */
  }

  .notification-item, .datasource-item, .activity-item {
    padding: 8px 12px;
  }

  .notification-icon, .activity-icon {
    padding: 4px;
  }

  .notification-title, .activity-text {
    font-size: 13px;
  }

  .notification-message, .datasource-url {
    font-size: 11px;
  }

  .notification-time, .activity-time {
    font-size: 10px;
  }
}

@media (max-width: 480px) {
  .dashboard {
    padding: 8px;
  }
  
  .welcome-text h2 {
    font-size: 20px;
  }
  
  .welcome-text p {
    font-size: 14px;
  }
  
  .chart-container {
    height: 200px;
  }
  
  .stat-card {
    height: 90px;
  }
  
  .stat-value {
    font-size: 18px;
  }
  
  .stat-title {
    font-size: 11px;
  }
  
  .report-name {
    font-size: 14px;
  }
  
  .report-date {
    font-size: 11px;
  }
  
  .info-label {
    font-size: 13px;
  }
}

/* 触摸优化 */
@media (hover: none) and (pointer: coarse) {
  .stat-card:hover {
    transform: none;
  }
  
  .stat-card:active {
    transform: scale(0.98);
  }
  
  .el-button:active {
    transform: scale(0.98);
  }
  
  .report-item:active {
    background-color: #f5f7fa;
  }
}

/* 移动端图表优化 */
@media (max-width: 768px) {
  .chart-container {
    touch-action: pan-x pan-y;
  }
  
  .chart-container .echarts {
    touch-action: pan-x pan-y;
  }
}
</style> 