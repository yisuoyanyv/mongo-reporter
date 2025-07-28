<template>
  <div class="report-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>报表列表</span>
          <el-button type="primary" @click="$router.push('/designer')">新建报表</el-button>
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
            <el-button @click="resetFilters">重置筛选</el-button>
          </el-col>
        </el-row>
      </div>
      
      <el-table :data="filteredReports" style="width: 100%">
        <el-table-column prop="name" label="报表名称" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="category" label="分类">
          <template #default="scope">
            <el-tag v-if="scope.row.category" type="primary">{{ scope.row.category }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="tags" label="标签">
          <template #default="scope">
            <el-tag
              v-for="tag in scope.row.tags"
              :key="tag"
              size="small"
              style="margin-right: 4px;"
            >
              {{ tag }}
            </el-tag>
            <span v-if="!scope.row.tags || scope.row.tags.length === 0">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag
              :type="getStatusType(scope.row.status)"
              size="small"
            >
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="owner" label="创建者" />
        <el-table-column prop="createdAt" label="创建时间">
          <template #default="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="修改时间">
          <template #default="scope">
            {{ formatDate(scope.row.updatedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" @click="viewReport(scope.row)">查看</el-button>
            <el-button size="small" type="primary" @click="editReport(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteReport(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

const router = useRouter()
const reports = ref([])
const categories = ref([])
const tags = ref([])

// 搜索和筛选状态
const searchKeyword = ref('')
const selectedCategory = ref('')
const selectedTags = ref([])
const selectedStatus = ref('')

const formatDate = (dateString) => {
  if (!dateString) return '-'
  try {
    const date = new Date(dateString)
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch (error) {
    return dateString
  }
}

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
    default: return '草稿'
  }
}

// 过滤报表
const filteredReports = computed(() => {
  return reports.value.filter(report => {
    // 关键词搜索
    if (searchKeyword.value) {
      const keyword = searchKeyword.value.toLowerCase()
      const nameMatch = report.name && report.name.toLowerCase().includes(keyword)
      const descMatch = report.description && report.description.toLowerCase().includes(keyword)
      if (!nameMatch && !descMatch) return false
    }
    
    // 分类筛选
    if (selectedCategory.value && report.category !== selectedCategory.value) {
      return false
    }
    
    // 标签筛选
    if (selectedTags.value.length > 0) {
      const hasSelectedTag = selectedTags.value.some(tag => 
        report.tags && report.tags.includes(tag)
      )
      if (!hasSelectedTag) return false
    }
    
    // 状态筛选
    if (selectedStatus.value && report.status !== selectedStatus.value) {
      return false
    }
    
    return true
  })
})

const loadReports = async () => {
  try {
    const response = await axios.get('/api/report/configs')
    reports.value = response.data
  } catch (error) {
    console.error('加载报表列表失败:', error)
    ElMessage.error('加载报表列表失败')
  }
}

const loadCategories = async () => {
  try {
    const response = await axios.get('/api/report/categories')
    categories.value = response.data
  } catch (error) {
    console.error('加载分类列表失败:', error)
  }
}

const loadTags = async () => {
  try {
    const response = await axios.get('/api/report/tags')
    tags.value = response.data
  } catch (error) {
    console.error('加载标签列表失败:', error)
  }
}

const handleSearch = () => {
  // 搜索逻辑已通过computed实现
}

const resetFilters = () => {
  searchKeyword.value = ''
  selectedCategory.value = ''
  selectedTags.value = []
  selectedStatus.value = ''
}

const viewReport = (report) => {
  router.push(`/viewer/${report.id}`)
}

const editReport = (report) => {
  router.push(`/designer/${report.id}`)
}

const deleteReport = async (report) => {
  try {
    await ElMessageBox.confirm('确定要删除这个报表吗？', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await axios.delete(`/api/report/configs/${report.id}`)
    ElMessage.success('删除成功')
    loadReports()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除报表失败:', error)
      ElMessage.error('删除报表失败')
    }
  }
}

onMounted(() => {
  loadReports()
  loadCategories()
  loadTags()
})
</script>

<style scoped>
.search-filter-section {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style> 