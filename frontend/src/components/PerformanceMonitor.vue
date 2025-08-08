<template>
  <div class="performance-monitor">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>性能监控</span>
          <el-button link @click="refreshPerformance">刷新</el-button>
        </div>
      </template>
      
      <div class="performance-metrics">
        <div class="metric-item">
          <div class="metric-label">响应时间</div>
          <div class="metric-value">{{ responseTime }}ms</div>
        </div>
        
        <div class="metric-item">
          <div class="metric-label">内存使用</div>
          <div class="metric-value">{{ memoryUsage }}%</div>
        </div>
        
        <div class="metric-item">
          <div class="metric-label">CPU使用</div>
          <div class="metric-value">{{ cpuUsage }}%</div>
        </div>
        
        <div class="metric-item">
          <div class="metric-label">活跃连接</div>
          <div class="metric-value">{{ activeConnections }}</div>
        </div>
      </div>
      
      <div class="performance-chart">
        <v-chart :option="performanceOption" :style="{ height: '200px' }" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  GridComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import axios from 'axios'

// 注册ECharts组件
use([
  CanvasRenderer,
  LineChart,
  TitleComponent,
  TooltipComponent,
  GridComponent
])

const responseTime = ref(0)
const memoryUsage = ref(0)
const cpuUsage = ref(0)
const activeConnections = ref(0)
const performanceOption = ref({})

let performanceTimer = null

// 获取性能数据
const fetchPerformanceData = async () => {
  try {
    const startTime = Date.now()
    const response = await axios.get('/api/system/charts/system-performance')
    const endTime = Date.now()
    
    responseTime.value = endTime - startTime
    
    if (response.data && response.data.values) {
      const values = response.data.values
      memoryUsage.value = Math.round(values[1] || 0)
      cpuUsage.value = Math.round(values[0] || 0)
    }
    
    // 模拟活跃连接数
    activeConnections.value = Math.floor(Math.random() * 50) + 10
    
    updatePerformanceChart()
  } catch (error) {
    console.error('获取性能数据失败:', error)
  }
}

// 更新性能图表
const updatePerformanceChart = () => {
  const now = new Date()
  const time = now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', second: '2-digit' })
  
  if (!performanceOption.value.series) {
    performanceOption.value = {
      title: {
        text: '系统性能趋势',
        left: 'center',
        textStyle: {
          fontSize: 12
        }
      },
      tooltip: {
        trigger: 'axis'
      },
      xAxis: {
        type: 'category',
        data: []
      },
      yAxis: {
        type: 'value',
        max: 100
      },
      series: [
        {
          name: 'CPU使用率',
          type: 'line',
          data: [],
          smooth: true,
          itemStyle: { color: '#409EFF' }
        },
        {
          name: '内存使用率',
          type: 'line',
          data: [],
          smooth: true,
          itemStyle: { color: '#67C23A' }
        }
      ]
    }
  }
  
  const xAxisData = performanceOption.value.xAxis.data
  const cpuData = performanceOption.value.series[0].data
  const memoryData = performanceOption.value.series[1].data
  
  xAxisData.push(time)
  cpuData.push(cpuUsage.value)
  memoryData.push(memoryUsage.value)
  
  // 保持最近20个数据点
  if (xAxisData.length > 20) {
    xAxisData.shift()
    cpuData.shift()
    memoryData.shift()
  }
}

// 刷新性能数据
const refreshPerformance = () => {
  fetchPerformanceData()
}

// 组件挂载时启动监控
onMounted(() => {
  fetchPerformanceData()
  performanceTimer = setInterval(fetchPerformanceData, 30000) // 每30秒更新一次
})

// 组件卸载时清理定时器
onUnmounted(() => {
  if (performanceTimer) {
    clearInterval(performanceTimer)
    performanceTimer = null
  }
})
</script>

<style scoped>
.performance-monitor {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.performance-metrics {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 15px;
  margin-bottom: 20px;
}

.metric-item {
  text-align: center;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
}

.metric-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 5px;
}

.metric-value {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.performance-chart {
  margin-top: 20px;
}
</style> 