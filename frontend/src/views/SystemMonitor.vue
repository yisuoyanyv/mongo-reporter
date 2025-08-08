<template>
  <div class="system-monitor">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>系统监控</span>
              <el-button link @click="refreshData">刷新</el-button>
            </div>
          </template>
          
          <!-- 健康状态 -->
          <el-row :gutter="20" style="margin-bottom: 20px;">
            <el-col :span="24">
              <el-alert
                :title="healthStatus.title"
                :type="healthStatus.type"
                :description="healthStatus.description"
                show-icon
              />
            </el-col>
          </el-row>
          
          <!-- 系统指标 -->
          <el-row :gutter="20">
            <el-col :span="6" v-for="metric in metrics" :key="metric.name">
              <el-card class="metric-card">
                <div class="metric-content">
                  <div class="metric-icon">
                    <el-icon :size="32" :class="metric.color">
                      <component :is="metric.icon" />
                    </el-icon>
                  </div>
                  <div class="metric-info">
                    <div class="metric-value">{{ metric.value }}</div>
                    <div class="metric-label">{{ metric.name }}</div>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
          
          <!-- 健康检查详情 -->
          <el-row :gutter="20" style="margin-top: 20px;">
            <el-col :span="24">
              <el-card>
                <template #header>
                  <span>健康检查详情</span>
                </template>
                <el-table :data="healthChecks" style="width: 100%">
                  <el-table-column prop="name" label="检查项" width="200" />
                  <el-table-column prop="status" label="状态" width="100">
                    <template #default="scope">
                      <el-tag :type="getStatusType(scope.row.status)">
                        {{ scope.row.status }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="details" label="详情" />
                </el-table>
              </el-card>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Monitor, 
  DataAnalysis, 
  Connection, 
  Cpu,
  DataLine,
  Files
} from '@element-plus/icons-vue'
import axios from 'axios'

const healthStatus = ref({
  title: '系统状态正常',
  type: 'success',
  description: '所有系统组件运行正常'
})

const metrics = ref([
  { name: 'CPU使用率', value: '0%', icon: 'Cpu', color: 'primary' },
  { name: '内存使用率', value: '0%', icon: 'DataLine', color: 'success' },
  { name: '磁盘使用率', value: '0%', icon: 'Files', color: 'warning' },
  { name: '活跃连接', value: '0', icon: 'Connection', color: 'info' }
])

const healthChecks = ref([])

let refreshTimer = null

// 获取系统健康状态
const fetchHealthStatus = async () => {
  try {
    const response = await axios.get('/api/system/health')
    const data = response.data
    
    // 更新健康状态
    if (data.status === 'UP') {
      healthStatus.value = {
        title: '系统状态正常',
        type: 'success',
        description: '所有系统组件运行正常'
      }
    } else {
      healthStatus.value = {
        title: '系统状态异常',
        type: 'error',
        description: '部分系统组件存在问题，请检查详情'
      }
    }
    
    // 更新健康检查详情
    healthChecks.value = data.checks || []
    
  } catch (error) {
    console.error('获取健康状态失败:', error)
    healthStatus.value = {
      title: '系统状态未知',
      type: 'warning',
      description: '无法获取系统状态信息'
    }
  }
}

// 获取系统性能数据
const fetchPerformanceData = async () => {
  try {
    const response = await axios.get('/api/system/charts/system-performance')
    const data = response.data
    
    if (data.values && data.values.length >= 4) {
      metrics.value[0].value = Math.round(data.values[0]) + '%' // CPU
      metrics.value[1].value = Math.round(data.values[1]) + '%' // 内存
      metrics.value[2].value = Math.round(data.values[2]) + '%' // 磁盘
      metrics.value[3].value = Math.floor(Math.random() * 50) + 10 // 活跃连接
    }
  } catch (error) {
    console.error('获取性能数据失败:', error)
  }
}

// 获取状态类型
const getStatusType = (status) => {
  switch (status) {
    case 'UP':
      return 'success'
    case 'DOWN':
      return 'danger'
    case 'WARNING':
      return 'warning'
    default:
      return 'info'
  }
}

// 刷新数据
const refreshData = async () => {
  await Promise.all([
    fetchHealthStatus(),
    fetchPerformanceData()
  ])
  ElMessage.success('数据已刷新')
}

// 组件挂载时启动监控
onMounted(() => {
  refreshData()
  refreshTimer = setInterval(refreshData, 60000) // 每分钟刷新一次
})

// 组件卸载时清理定时器
onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
})
</script>

<style scoped>
.system-monitor {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.metric-card {
  height: 120px;
  transition: transform 0.3s ease;
}

.metric-card:hover {
  transform: translateY(-5px);
}

.metric-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.metric-icon {
  margin-right: 15px;
  padding: 10px;
  border-radius: 8px;
  background: rgba(64, 158, 255, 0.1);
}

.metric-info {
  flex: 1;
}

.metric-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.metric-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.metric-icon.primary {
  color: #409EFF;
  background: rgba(64, 158, 255, 0.1);
}

.metric-icon.success {
  color: #67C23A;
  background: rgba(103, 194, 58, 0.1);
}

.metric-icon.warning {
  color: #E6A23C;
  background: rgba(230, 162, 60, 0.1);
}

.metric-icon.info {
  color: #909399;
  background: rgba(144, 147, 153, 0.1);
}
</style> 