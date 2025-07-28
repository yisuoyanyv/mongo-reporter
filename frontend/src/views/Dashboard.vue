<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <!-- 统计卡片 -->
      <el-col :span="6" v-for="stat in stats" :key="stat.title">
        <el-card class="stat-card" :body-style="{ padding: '20px' }">
          <div class="stat-content">
            <div class="stat-icon" :style="{ backgroundColor: stat.color }">
              <el-icon :size="24" color="white">
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

    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- 最近报表 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最近报表</span>
              <el-button type="primary" size="small" @click="$router.push('/reports')">
                查看全部
              </el-button>
            </div>
          </template>
          <div v-if="recentReports.length === 0" class="empty-state">
            <el-empty description="暂无报表" />
          </div>
          <div v-else>
            <div v-for="report in recentReports" :key="report.id" class="report-item">
              <div class="report-info">
                <div class="report-name">{{ report.name }}</div>
                <div class="report-desc">{{ report.description }}</div>
                <div class="report-meta">
                  <el-tag v-if="report.category" size="small" type="primary">
                    {{ report.category }}
                  </el-tag>
                  <span class="report-time">{{ formatDate(report.updatedAt) }}</span>
                </div>
              </div>
              <div class="report-actions">
                <el-button size="small" @click="viewReport(report)">查看</el-button>
                <el-button size="small" type="primary" @click="editReport(report)">编辑</el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 数据源状态 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>数据源状态</span>
              <el-button type="primary" size="small" @click="$router.push('/datasources')">
                管理数据源
              </el-button>
            </div>
          </template>
          <div v-if="dataSources.length === 0" class="empty-state">
            <el-empty description="暂无数据源" />
          </div>
          <div v-else>
            <div v-for="ds in dataSources" :key="ds.id" class="datasource-item">
              <div class="datasource-info">
                <div class="datasource-name">{{ ds.name }}</div>
                <div class="datasource-uri">{{ ds.uri }}</div>
                <div class="datasource-status">
                  <el-tag 
                    :type="ds.connectionStatus === 'success' ? 'success' : 'danger'"
                    size="small"
                  >
                    {{ ds.connectionStatus === 'success' ? '连接正常' : '连接异常' }}
                  </el-tag>
                </div>
              </div>
              <div class="datasource-actions">
                <el-button size="small" @click="testConnection(ds)">测试</el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- 快速操作 -->
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>快速操作</span>
          </template>
          <div class="quick-actions">
            <el-button type="primary" size="large" @click="$router.push('/designer')">
              <el-icon><Plus /></el-icon>
              新建报表
            </el-button>
            <el-button type="success" size="large" @click="$router.push('/datasources')">
              <el-icon><Connection /></el-icon>
              添加数据源
            </el-button>
            <el-button type="warning" size="large" @click="showTemplateDialog">
              <el-icon><Document /></el-icon>
              使用模板
            </el-button>
            <el-button type="info" size="large" @click="showHelpDialog">
              <el-icon><QuestionFilled /></el-icon>
              使用帮助
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 模板选择对话框 -->
    <el-dialog v-model="templateDialogVisible" title="选择模板" width="600px">
      <div class="template-list">
        <el-card 
          v-for="template in templates" 
          :key="template.id" 
          class="template-card"
          @click="applyTemplate(template)"
        >
          <div class="template-info">
            <h4>{{ template.name }}</h4>
            <p>{{ template.description }}</p>
            <div class="template-tags">
              <el-tag v-for="tag in template.tags" :key="tag" size="small">
                {{ tag }}
              </el-tag>
            </div>
          </div>
        </el-card>
      </div>
    </el-dialog>

    <!-- 帮助对话框 -->
    <el-dialog v-model="helpDialogVisible" title="使用帮助" width="800px">
      <div class="help-content">
        <h3>快速开始</h3>
        <ol>
          <li>添加数据源：在"数据源管理"中添加MongoDB连接</li>
          <li>创建报表：点击"新建报表"开始设计</li>
          <li>选择数据：选择数据源和集合</li>
          <li>设计图表：拖拽组件到画布并配置</li>
          <li>保存报表：填写信息并保存</li>
        </ol>
        
        <h3>常用功能</h3>
        <ul>
          <li><strong>报表分类</strong>：使用分类和标签组织报表</li>
          <li><strong>数据过滤</strong>：设置条件过滤数据</li>
          <li><strong>图表配置</strong>：自定义图表样式和主题</li>
          <li><strong>报表分享</strong>：生成分享链接供他人查看</li>
          <li><strong>数据导出</strong>：支持PDF、Excel、图片导出</li>
        </ul>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { 
  Document, 
  Connection, 
  PieChart, 
  DataAnalysis,
  Plus,
  QuestionFilled
} from '@element-plus/icons-vue'

const router = useRouter()

// 统计数据
const stats = ref([
  { title: '总报表数', value: 0, icon: 'Document', color: '#409EFF' },
  { title: '数据源数', value: 0, icon: 'Connection', color: '#67C23A' },
  { title: '图表总数', value: 0, icon: 'PieChart', color: '#E6A23C' },
  { title: '今日访问', value: 0, icon: 'DataAnalysis', color: '#F56C6C' }
])

// 数据
const recentReports = ref([])
const dataSources = ref([])
const templates = ref([
  {
    id: 'sales-dashboard',
    name: '销售仪表板',
    description: '包含销售趋势、产品分析、地区分布等图表',
    tags: ['销售', '仪表板', '趋势分析']
  },
  {
    id: 'user-analysis',
    name: '用户分析报表',
    description: '用户行为分析、活跃度统计、留存分析',
    tags: ['用户', '分析', '行为']
  },
  {
    id: 'product-report',
    name: '产品报表',
    description: '产品销量、库存、分类统计',
    tags: ['产品', '库存', '销量']
  }
])

// 对话框状态
const templateDialogVisible = ref(false)
const helpDialogVisible = ref(false)

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '-'
  try {
    const date = new Date(dateString)
    return date.toLocaleString('zh-CN', {
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch (error) {
    return dateString
  }
}

// 加载数据
const loadData = async () => {
  try {
    // 加载报表列表
    const reportsResponse = await axios.get('/api/report/configs')
    const reports = reportsResponse.data || []
    recentReports.value = reports.slice(0, 5)
    stats.value[0].value = reports.length

    // 加载数据源列表
    const datasourcesResponse = await axios.get('/api/datasource')
    dataSources.value = datasourcesResponse.data || []
    stats.value[1].value = dataSources.value.length

    // 计算图表总数
    const totalWidgets = reports.reduce((sum, report) => {
      return sum + (report.widgets ? report.widgets.length : 0)
    }, 0)
    stats.value[2].value = totalWidgets

    // 模拟今日访问数
    stats.value[3].value = Math.floor(Math.random() * 50) + 10

  } catch (error) {
    console.error('加载仪表板数据失败:', error)
    ElMessage.error('加载数据失败')
  }
}

// 查看报表
const viewReport = (report) => {
  router.push(`/viewer/${report.id}`)
}

// 编辑报表
const editReport = (report) => {
  router.push(`/designer/${report.id}`)
}

// 测试连接
const testConnection = async (datasource) => {
  try {
    const response = await axios.post('/api/datasource/test', {
      dataSource: datasource
    })
    
    if (response.data.success) {
      ElMessage.success('连接测试成功')
      // 更新连接状态
      datasource.connectionStatus = 'success'
    } else {
      ElMessage.error('连接测试失败')
      datasource.connectionStatus = 'error'
    }
  } catch (error) {
    ElMessage.error('连接测试失败')
    datasource.connectionStatus = 'error'
  }
}

// 显示模板对话框
const showTemplateDialog = () => {
  templateDialogVisible.value = true
}

// 应用模板
const applyTemplate = (template) => {
  templateDialogVisible.value = false
  ElMessage.success(`已选择模板: ${template.name}`)
  // 这里可以跳转到设计器并应用模板
  router.push('/designer')
}

// 显示帮助对话框
const showHelpDialog = () => {
  helpDialogVisible.value = true
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.stat-card {
  margin-bottom: 20px;
}

.stat-content {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.stat-title {
  font-size: 14px;
  color: #909399;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.report-item, .datasource-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #EBEEF5;
}

.report-item:last-child, .datasource-item:last-child {
  border-bottom: none;
}

.report-name, .datasource-name {
  font-weight: 500;
  margin-bottom: 4px;
}

.report-desc, .datasource-uri {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.report-meta, .datasource-status {
  display: flex;
  align-items: center;
  gap: 8px;
}

.report-time {
  font-size: 12px;
  color: #C0C4CC;
}

.empty-state {
  text-align: center;
  padding: 40px 0;
}

.quick-actions {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.template-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 16px;
}

.template-card {
  cursor: pointer;
  transition: all 0.3s;
}

.template-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.template-info h4 {
  margin: 0 0 8px 0;
  color: #303133;
}

.template-info p {
  margin: 0 0 12px 0;
  color: #606266;
  font-size: 14px;
}

.template-tags {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.help-content h3 {
  color: #303133;
  margin-bottom: 12px;
}

.help-content ol, .help-content ul {
  padding-left: 20px;
  line-height: 1.6;
}

.help-content li {
  margin-bottom: 8px;
}
</style> 