<template>
  <div class="report-template-manager">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>报表模板管理</span>
          <div class="header-actions">
            <el-button @click="refreshTemplates" :loading="loading">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
            <el-button type="primary" @click="showCreateDialog">
              <el-icon><Plus /></el-icon>
              新建模板
            </el-button>
          </div>
        </div>
      </template>

      <!-- 搜索和筛选 -->
      <div class="search-section">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索模板名称或描述"
              clearable
              @input="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :span="4">
            <el-select v-model="selectedCategory" placeholder="选择分类" clearable @change="handleSearch">
              <el-option
                v-for="category in categories"
                :key="category"
                :label="category"
                :value="category"
              />
            </el-select>
          </el-col>
          <el-col :span="4">
            <el-select v-model="selectedStatus" placeholder="选择状态" clearable @change="handleSearch">
              <el-option label="草稿" value="draft" />
              <el-option label="已发布" value="published" />
              <el-option label="已归档" value="archived" />
            </el-select>
          </el-col>
          <el-col :span="4">
            <el-select v-model="sortBy" placeholder="排序方式" @change="handleSearch">
              <el-option label="最新创建" value="createdAt" />
              <el-option label="最多使用" value="usageCount" />
              <el-option label="最高评分" value="rating" />
            </el-select>
          </el-col>
        </el-row>
      </div>

      <!-- 模板列表 -->
      <div class="template-list" v-loading="loading">
        <el-row :gutter="20">
          <el-col
            v-for="template in filteredTemplates"
            :key="template.id"
            :xs="24"
            :sm="12"
            :md="8"
            :lg="6"
          >
            <el-card class="template-card" shadow="hover">
              <template #header>
                <div class="template-header">
                  <h3 class="template-name">{{ template.name }}</h3>
                  <el-tag v-if="template.isPublic" type="success" size="small">公开</el-tag>
                  <el-tag v-else type="info" size="small">私有</el-tag>
                </div>
              </template>
              
              <div class="template-content">
                <p class="template-description">{{ template.description || '暂无描述' }}</p>
                
                <div class="template-meta">
                  <div class="meta-item">
                    <el-icon><User /></el-icon>
                    <span>{{ template.owner }}</span>
                  </div>
                  <div class="meta-item">
                    <el-icon><Calendar /></el-icon>
                    <span>{{ formatDate(template.createdAt) }}</span>
                  </div>
                  <div class="meta-item">
                    <el-icon><View /></el-icon>
                    <span>使用 {{ template.usageCount || 0 }} 次</span>
                  </div>
                  <div class="meta-item">
                    <el-icon><Star /></el-icon>
                    <span>评分 {{ template.rating || 0 }}/5</span>
                  </div>
                </div>

                <div class="template-tags" v-if="template.tags && template.tags.length > 0">
                  <el-tag
                    v-for="tag in template.tags"
                    :key="tag"
                    size="small"
                    style="margin-right: 4px; margin-bottom: 4px;"
                  >
                    {{ tag }}
                  </el-tag>
                </div>

                <div class="template-actions">
                  <el-button size="small" type="primary" @click="useTemplate(template)">
                    使用模板
                  </el-button>
                  <el-button size="small" @click="previewTemplate(template)">
                    预览
                  </el-button>
                  <el-dropdown v-if="template.owner === currentUser" @command="handleTemplateAction">
                    <el-button size="small">
                      更多<el-icon class="el-icon--right"><ArrowDown /></el-icon>
                    </el-button>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item :command="{action: 'edit', template}">编辑</el-dropdown-item>
                        <el-dropdown-item :command="{action: 'duplicate', template}">复制</el-dropdown-item>
                        <el-dropdown-item :command="{action: 'share', template}">
                          {{ template.isPublic ? '取消分享' : '分享' }}
                        </el-dropdown-item>
                        <el-dropdown-item divided :command="{action: 'delete', template}">删除</el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 空状态 -->
        <el-empty v-if="filteredTemplates.length === 0 && !loading" description="暂无模板" />
      </div>
    </el-card>

    <!-- 创建模板对话框 -->
    <el-dialog v-model="createDialogVisible" title="新建模板" width="600px">
      <el-form :model="newTemplate" :rules="templateRules" ref="templateFormRef" label-width="100px">
        <el-form-item label="模板名称" prop="name">
          <el-input v-model="newTemplate.name" placeholder="请输入模板名称" />
        </el-form-item>
        <el-form-item label="模板描述" prop="description">
          <el-input
            v-model="newTemplate.description"
            type="textarea"
            :rows="3"
            placeholder="请输入模板描述"
          />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="newTemplate.category" placeholder="选择分类" clearable>
            <el-option
              v-for="category in categories"
              :key="category"
              :label="category"
              :value="category"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="标签">
          <el-select
            v-model="newTemplate.tags"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="请选择或输入标签"
          >
            <el-option
              v-for="tag in availableTags"
              :key="tag"
              :label="tag"
              :value="tag"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="是否公开">
          <el-switch v-model="newTemplate.isPublic" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="createTemplate" :loading="creating">
            创建
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Refresh, User, Calendar, View, Star, ArrowDown } from '@element-plus/icons-vue'
import axios from 'axios'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const creating = ref(false)
const templates = ref([])
const searchKeyword = ref('')
const selectedCategory = ref('')
const selectedStatus = ref('')
const sortBy = ref('createdAt')
const createDialogVisible = ref(false)
const currentUser = ref(localStorage.getItem('username') || '')

// 新模板表单
const newTemplate = ref({
  name: '',
  description: '',
  category: '',
  tags: [],
  isPublic: false
})

// 表单验证规则
const templateRules = {
  name: [
    { required: true, message: '请输入模板名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  description: [
    { max: 200, message: '描述不能超过200个字符', trigger: 'blur' }
  ]
}

// 计算属性
const categories = computed(() => {
  const cats = [...new Set(templates.value.map(t => t.category).filter(Boolean))]
  return cats.sort()
})

const availableTags = computed(() => {
  const tags = new Set()
  templates.value.forEach(template => {
    if (template.tags) {
      template.tags.forEach(tag => tags.add(tag))
    }
  })
  return Array.from(tags).sort()
})

const filteredTemplates = computed(() => {
  let filtered = templates.value

  // 关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(template =>
      template.name.toLowerCase().includes(keyword) ||
      (template.description && template.description.toLowerCase().includes(keyword))
    )
  }

  // 分类筛选
  if (selectedCategory.value) {
    filtered = filtered.filter(template => template.category === selectedCategory.value)
  }

  // 状态筛选
  if (selectedStatus.value) {
    filtered = filtered.filter(template => template.status === selectedStatus.value)
  }

  // 排序
  filtered.sort((a, b) => {
    switch (sortBy.value) {
      case 'usageCount':
        return (b.usageCount || 0) - (a.usageCount || 0)
      case 'rating':
        return (b.rating || 0) - (a.rating || 0)
      case 'createdAt':
      default:
        return new Date(b.createdAt) - new Date(a.createdAt)
    }
  })

  return filtered
})

// 方法
const fetchTemplates = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/templates')
    templates.value = response.data
  } catch (error) {
    console.error('获取模板失败:', error)
    ElMessage.error('获取模板失败')
  } finally {
    loading.value = false
  }
}

const refreshTemplates = () => {
  fetchTemplates()
}

const handleSearch = () => {
  // 搜索逻辑已在计算属性中实现
}

const showCreateDialog = () => {
  createDialogVisible.value = true
  newTemplate.value = {
    name: '',
    description: '',
    category: '',
    tags: [],
    isPublic: false
  }
}

const createTemplate = async () => {
  creating.value = true
  try {
    const response = await axios.post('/api/templates', newTemplate.value)
    ElMessage.success('模板创建成功')
    createDialogVisible.value = false
    fetchTemplates()
  } catch (error) {
    console.error('创建模板失败:', error)
    ElMessage.error('创建模板失败')
  } finally {
    creating.value = false
  }
}

const useTemplate = async (template) => {
  try {
    const response = await axios.post(`/api/templates/${template.id}/use`)
    ElMessage.success('模板应用成功')
    // 跳转到报表设计器，并传递模板配置
    router.push({
      path: '/reports/design',
      query: { template: template.id }
    })
  } catch (error) {
    console.error('使用模板失败:', error)
    ElMessage.error('使用模板失败')
  }
}

const previewTemplate = (template) => {
  // 实现模板预览功能
  ElMessage.info('模板预览功能开发中...')
}

const handleTemplateAction = async (command) => {
  const { action, template } = command
  
  switch (action) {
    case 'edit':
      router.push(`/templates/${template.id}/edit`)
      break
    case 'duplicate':
      await duplicateTemplate(template)
      break
    case 'share':
      await toggleTemplateShare(template)
      break
    case 'delete':
      await deleteTemplate(template)
      break
  }
}

const duplicateTemplate = async (template) => {
  try {
    const duplicatedTemplate = {
      ...template,
      name: `${template.name} (副本)`,
      isPublic: false
    }
    delete duplicatedTemplate.id
    delete duplicatedTemplate.createdAt
    delete duplicatedTemplate.updatedAt
    
    await axios.post('/api/templates', duplicatedTemplate)
    ElMessage.success('模板复制成功')
    fetchTemplates()
  } catch (error) {
    console.error('复制模板失败:', error)
    ElMessage.error('复制模板失败')
  }
}

const toggleTemplateShare = async (template) => {
  try {
    const updatedTemplate = {
      ...template,
      isPublic: !template.isPublic
    }
    await axios.put(`/api/templates/${template.id}`, updatedTemplate)
    ElMessage.success(template.isPublic ? '已取消分享' : '已分享')
    fetchTemplates()
  } catch (error) {
    console.error('切换分享状态失败:', error)
    ElMessage.error('操作失败')
  }
}

const deleteTemplate = async (template) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除模板 "${template.name}" 吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await axios.delete(`/api/templates/${template.id}`)
    ElMessage.success('模板删除成功')
    fetchTemplates()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除模板失败:', error)
      ElMessage.error('删除模板失败')
    }
  }
}

const formatDate = (dateString) => {
  if (!dateString) return '未知'
  return new Date(dateString).toLocaleDateString('zh-CN')
}

// 生命周期
onMounted(() => {
  fetchTemplates()
})
</script>

<style scoped>
.report-template-manager {
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

.search-section {
  margin-bottom: 20px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}

.template-list {
  margin-top: 20px;
}

.template-card {
  margin-bottom: 20px;
  transition: all 0.3s ease;
}

.template-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.template-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.template-name {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.template-content {
  padding: 10px 0;
}

.template-description {
  color: #606266;
  margin-bottom: 15px;
  line-height: 1.5;
}

.template-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 15px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #909399;
  font-size: 12px;
}

.template-tags {
  margin-bottom: 15px;
}

.template-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

@media (max-width: 768px) {
  .search-section .el-row {
    margin: 0;
  }
  
  .search-section .el-col {
    margin-bottom: 10px;
  }
  
  .template-meta {
    flex-direction: column;
    gap: 8px;
  }
  
  .template-actions {
    flex-direction: column;
  }
}
</style> 