<template>
  <div class="report-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>报表列表</span>
          <div class="header-actions">
            <el-button @click="refreshReports">刷新</el-button>
            <el-button type="primary" @click="$router.push('/designer')">新建报表</el-button>
          </div>
        </div>
      </template>
      
      <!-- 搜索和筛选区域 -->
      <div class="search-filter-section" style="margin-bottom: 20px;">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索报表名称或描述"
              clearable
              @input="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :span="4">
            <el-select
              v-model="selectedCategory"
              placeholder="选择分类"
              clearable
              @change="handleSearch"
            >
              <el-option
                v-for="category in categories"
                :key="category"
                :label="category"
                :value="category"
              />
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-select
              v-model="selectedTags"
              placeholder="选择标签"
              multiple
              clearable
              @change="handleSearch"
            >
              <el-option
                v-for="tag in tags"
                :key="tag"
                :label="tag"
                :value="tag"
              />
            </el-select>
          </el-col>
          <el-col :span="4">
            <el-select
              v-model="selectedStatus"
              placeholder="选择状态"
              clearable
              @change="handleSearch"
            >
              <el-option label="草稿" value="draft" />
              <el-option label="已发布" value="published" />
              <el-option label="已归档" value="archived" />
            </el-select>
          </el-col>
          <el-col :span="4">
            <el-button @click="clearFilters">清除筛选</el-button>
          </el-col>
        </el-row>
      </div>

      <!-- 批量操作工具栏 -->
      <div v-if="selectedReports.length > 0" class="batch-actions" style="margin-bottom: 20px;">
        <el-alert
          :title="`已选择 ${selectedReports.length} 个报表`"
          type="info"
          :closable="false"
          show-icon
        >
          <template #default>
            <div class="batch-buttons">
              <el-button size="small" @click="batchPublish">批量发布</el-button>
              <el-button size="small" @click="batchArchive">批量归档</el-button>
              <el-button size="small" @click="batchDelete" type="danger">批量删除</el-button>
              <el-button size="small" @click="batchExport">批量导出</el-button>
              <el-button size="small" @click="clearSelection">取消选择</el-button>
            </div>
          </template>
        </el-alert>
      </div>
      
      <!-- 报表列表 -->
      <el-table
        :data="filteredReports"
        style="width: 100%"
        @selection-change="handleSelectionChange"
        v-loading="loading"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="name" label="报表名称" min-width="200">
          <template #default="scope">
            <div class="report-name">
              <span class="name-text">{{ scope.row.name }}</span>
              <el-tag v-if="scope.row.status" :type="getStatusType(scope.row.status)" size="small">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="120">
          <template #default="scope">
            <el-tag v-if="scope.row.category" type="info" size="small">
              {{ scope.row.category }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="tags" label="标签" width="200">
          <template #default="scope">
            <el-tag
              v-for="tag in scope.row.tags"
              :key="tag"
              size="small"
              style="margin-right: 4px; margin-bottom: 4px;"
            >
              {{ tag }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="version" label="版本" width="80">
          <template #default="scope">
            <el-tag v-if="scope.row.version" type="warning" size="small">
              v{{ scope.row.version }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160">
          <template #default="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" width="160">
          <template #default="scope">
            {{ formatDate(scope.row.updatedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button-group>
              <el-button size="small" @click="viewReport(scope.row.id)">查看</el-button>
              <el-button size="small" @click="editReport(scope.row.id)">编辑</el-button>
              <el-dropdown @command="handleCommand" trigger="click">
                <el-button size="small">
                  更多<el-icon class="el-icon--right"><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item :command="{action: 'duplicate', id: scope.row.id}">复制</el-dropdown-item>
                    <el-dropdown-item :command="{action: 'share', id: scope.row.id}">分享</el-dropdown-item>
                    <el-dropdown-item :command="{action: 'export', id: scope.row.id}">导出</el-dropdown-item>
                    <el-dropdown-item :command="{action: 'history', id: scope.row.id}">版本历史</el-dropdown-item>
                    <el-dropdown-item divided :command="{action: 'delete', id: scope.row.id}">删除</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-wrapper" style="margin-top: 20px; text-align: right;">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="totalReports"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 版本历史对话框 -->
    <el-dialog v-model="versionHistoryVisible" title="版本历史" width="800px">
      <el-table :data="versionHistory" style="width: 100%">
        <el-table-column prop="version" label="版本" width="100" />
        <el-table-column prop="createdAt" label="创建时间" width="160" />
        <el-table-column prop="description" label="描述" />
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button size="small" @click="restoreVersion(scope.row)">恢复</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, ArrowDown } from '@element-plus/icons-vue'
import axios from 'axios'

const router = useRouter()

// 响应式数据
const reports = ref([])
const loading = ref(false)
const searchKeyword = ref('')
const selectedCategory = ref('')
const selectedTags = ref([])
const selectedStatus = ref('')
const selectedReports = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const totalReports = ref(0)
const versionHistoryVisible = ref(false)
const versionHistory = ref([])

// 分类和标签数据
const categories = ref([])
const tags = ref([])

// 计算属性
const filteredReports = computed(() => {
  let filtered = reports.value

  // 关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(report => 
      report.name?.toLowerCase().includes(keyword) ||
      report.description?.toLowerCase().includes(keyword)
    )
  }

  // 分类筛选
  if (selectedCategory.value) {
    filtered = filtered.filter(report => report.category === selectedCategory.value)
  }

  // 标签筛选
  if (selectedTags.value.length > 0) {
    filtered = filtered.filter(report => 
      report.tags && selectedTags.value.some(tag => report.tags.includes(tag))
    )
  }

  // 状态筛选
  if (selectedStatus.value) {
    filtered = filtered.filter(report => report.status === selectedStatus.value)
  }

  return filtered
})

// 获取报表列表
const fetchReports = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/report/configs')
    reports.value = response.data
    totalReports.value = reports.value.length
    
    // 提取分类和标签
    extractCategoriesAndTags()
  } catch (error) {
    console.error('获取报表列表失败:', error)
    ElMessage.error('获取报表列表失败')
  } finally {
    loading.value = false
  }
}

// 提取分类和标签
const extractCategoriesAndTags = () => {
  const categorySet = new Set()
  const tagSet = new Set()
  
  reports.value.forEach(report => {
    if (report.category) {
      categorySet.add(report.category)
    }
    if (report.tags) {
      report.tags.forEach(tag => tagSet.add(tag))
    }
  })
  
  categories.value = Array.from(categorySet)
  tags.value = Array.from(tagSet)
}

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1
}

// 清除筛选
const clearFilters = () => {
  searchKeyword.value = ''
  selectedCategory.value = ''
  selectedTags.value = []
  selectedStatus.value = ''
  currentPage.value = 1
}

// 选择变化处理
const handleSelectionChange = (selection) => {
  selectedReports.value = selection
}

// 清除选择
const clearSelection = () => {
  selectedReports.value = []
}

// 批量操作
const batchPublish = async () => {
  try {
    await ElMessageBox.confirm(`确定要发布选中的 ${selectedReports.value.length} 个报表吗？`)
    
    const promises = selectedReports.value.map(report => 
      axios.put(`/api/report/configs/${report.id}`, { ...report, status: 'published' })
    )
    
    await Promise.all(promises)
    ElMessage.success('批量发布成功')
    fetchReports()
    clearSelection()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量发布失败:', error)
      ElMessage.error('批量发布失败')
    }
  }
}

const batchArchive = async () => {
  try {
    await ElMessageBox.confirm(`确定要归档选中的 ${selectedReports.value.length} 个报表吗？`)
    
    const promises = selectedReports.value.map(report => 
      axios.put(`/api/report/configs/${report.id}`, { ...report, status: 'archived' })
    )
    
    await Promise.all(promises)
    ElMessage.success('批量归档成功')
    fetchReports()
    clearSelection()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量归档失败:', error)
      ElMessage.error('批量归档失败')
    }
  }
}

const batchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedReports.value.length} 个报表吗？此操作不可恢复！`, '警告', {
      type: 'warning'
    })
    
    const promises = selectedReports.value.map(report => 
      axios.delete(`/api/report/configs/${report.id}`)
    )
    
    await Promise.all(promises)
    ElMessage.success('批量删除成功')
    fetchReports()
    clearSelection()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

// 批量导出
const batchExport = async () => {
  if (selectedReports.value.length === 0) {
    ElMessage.warning('请先选择要导出的报表')
    return
  }
  
  try {
    ElMessage.info('正在准备批量导出...')
    
    const response = await axios.post('/api/export/batch', {
      reportIds: selectedReports.value,
      format: 'excel',
      options: {
        includeCharts: true,
        includeData: true,
        multipleSheets: true
      }
    }, {
      responseType: 'blob'
    })
    
    const blob = new Blob([response.data], { 
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
    })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `批量导出_${new Date().toISOString().slice(0, 10)}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('批量导出成功')
  } catch (error) {
    console.error('批量导出失败:', error)
    ElMessage.error('批量导出失败')
  }
}

// 单个操作
const viewReport = (id) => {
  router.push(`/reports/view/${id}`)
}

const editReport = (id) => {
  router.push(`/reports/design/${id}`)
}

const handleCommand = async (command) => {
  const { action, id } = command
  
  switch (action) {
    case 'duplicate':
      await duplicateReport(id)
      break
    case 'share':
      await shareReport(id)
      break
    case 'export':
      await exportReport(id)
      break
    case 'history':
      await showVersionHistory(id)
      break
    case 'delete':
      await deleteReport(id)
      break
  }
}

const duplicateReport = async (id) => {
  try {
    const response = await axios.post(`/api/report/configs/${id}/duplicate`)
    ElMessage.success('报表复制成功')
    fetchReports()
  } catch (error) {
    console.error('复制报表失败:', error)
    ElMessage.error('复制报表失败')
  }
}

const shareReport = async (id) => {
  try {
    const response = await axios.post(`/api/report/configs/${id}/share`)
    ElMessage.success('报表分享成功')
    fetchReports()
  } catch (error) {
    console.error('分享报表失败:', error)
    ElMessage.error('分享报表失败')
  }
}

const exportReport = async (id) => {
  try {
    window.open(`/api/export/pdf/${id}`, '_blank')
    ElMessage.success('报表导出成功')
  } catch (error) {
    console.error('导出报表失败:', error)
    ElMessage.error('导出报表失败')
  }
}

const showVersionHistory = async (id) => {
  try {
    const response = await axios.get(`/api/report/configs/${id}/versions`)
    versionHistory.value = response.data
    versionHistoryVisible.value = true
  } catch (error) {
    console.error('获取版本历史失败:', error)
    ElMessage.error('获取版本历史失败')
  }
}

const restoreVersion = async (version) => {
  try {
    await ElMessageBox.confirm(`确定要恢复到版本 ${version.version} 吗？`)
    await axios.post(`/api/report/configs/${version.reportId}/restore`, { version: version.version })
    ElMessage.success('版本恢复成功')
    versionHistoryVisible.value = false
    fetchReports()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('版本恢复失败:', error)
      ElMessage.error('版本恢复失败')
    }
  }
}

const deleteReport = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个报表吗？此操作不可恢复！', '警告', {
      type: 'warning'
    })
    
    await axios.delete(`/api/report/configs/${id}`)
    ElMessage.success('报表删除成功')
    fetchReports()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除报表失败:', error)
      ElMessage.error('删除报表失败')
    }
  }
}

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

const handleCurrentChange = (page) => {
  currentPage.value = page
}

// 刷新报表列表
const refreshReports = () => {
  fetchReports()
}

// 工具函数
const getStatusType = (status) => {
  switch (status) {
    case 'draft': return 'info'
    case 'published': return 'success'
    case 'archived': return 'warning'
    default: return 'info'
  }
}

const getStatusText = (status) => {
  switch (status) {
    case 'draft': return '草稿'
    case 'published': return '已发布'
    case 'archived': return '已归档'
    default: return '未知'
  }
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

// 组件挂载时获取数据
onMounted(() => {
  fetchReports()
})
</script>

<style scoped>
.report-list {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.search-filter-section {
  background-color: #f5f7fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.batch-actions {
  margin-bottom: 20px;
}

.batch-buttons {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}

.report-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.name-text {
  font-weight: 500;
}

.pagination-wrapper {
  margin-top: 20px;
  text-align: right;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .search-filter-section {
    padding: 12px;
  }
  
  .search-filter-section .el-row {
    margin: 0 !important;
  }
  
  .search-filter-section .el-col {
    margin-bottom: 12px;
  }
  
  .search-filter-section .el-col:last-child {
    margin-bottom: 0;
  }
  
  .card-header {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }
  
  .card-header .el-button {
    width: 100%;
  }
  
  /* 移动端表格优化 */
  :deep(.el-table) {
    font-size: 12px;
  }
  
  :deep(.el-table th) {
    padding: 8px 4px;
  }
  
  :deep(.el-table td) {
    padding: 8px 4px;
  }
  
  :deep(.el-table .cell) {
    padding: 0 4px;
  }
  
  /* 移动端表格列隐藏 */
  :deep(.el-table .el-table__cell) {
    min-width: 80px;
  }
  
  /* 移动端操作按钮优化 */
  :deep(.el-button--small) {
    padding: 6px 8px;
    font-size: 11px;
  }
  
  /* 移动端标签优化 */
  :deep(.el-tag) {
    font-size: 10px;
    padding: 2px 6px;
    margin: 2px;
  }
}

@media (max-width: 480px) {
  .search-filter-section {
    padding: 8px;
  }
  
  :deep(.el-table) {
    font-size: 11px;
  }
  
  :deep(.el-table th) {
    padding: 6px 2px;
  }
  
  :deep(.el-table td) {
    padding: 6px 2px;
  }
  
  :deep(.el-table .cell) {
    padding: 0 2px;
  }
  
  /* 超小屏幕隐藏部分列 */
  :deep(.el-table .description-column) {
    display: none;
  }
  
  :deep(.el-table .tags-column) {
    display: none;
  }
}

/* 触摸优化 */
@media (hover: none) and (pointer: coarse) {
  :deep(.el-table tbody tr:hover) {
    background-color: transparent;
  }
  
  :deep(.el-table tbody tr:active) {
    background-color: #f5f7fa;
  }
  
  :deep(.el-button:active) {
    transform: scale(0.98);
  }
  
  :deep(.el-tag:active) {
    transform: scale(0.95);
  }
}

/* 移动端滚动优化 */
@media (max-width: 768px) {
  .report-list {
    overflow-x: auto;
  }
  
  :deep(.el-table) {
    min-width: 600px;
  }
  
  :deep(.el-table__body-wrapper) {
    overflow-x: auto;
  }
}
</style> 