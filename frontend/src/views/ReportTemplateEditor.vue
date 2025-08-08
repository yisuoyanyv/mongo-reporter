<template>
  <div class="report-template-editor">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑模板' : '新建模板' }}</span>
          <div class="header-actions">
            <el-button @click="$router.push('/templates')">返回列表</el-button>
            <el-button type="primary" @click="saveTemplate" :loading="saving">
              保存模板
            </el-button>
          </div>
        </div>
      </template>

      <el-form :model="templateForm" :rules="formRules" ref="formRef" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="模板名称" prop="name">
              <el-input v-model="templateForm.name" placeholder="请输入模板名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类" prop="category">
              <el-select v-model="templateForm.category" placeholder="选择分类" clearable>
                <el-option label="销售报表" value="sales" />
                <el-option label="财务报表" value="finance" />
                <el-option label="运营报表" value="operations" />
                <el-option label="用户报表" value="users" />
                <el-option label="系统报表" value="system" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="模板描述" prop="description">
          <el-input
            v-model="templateForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入模板描述"
          />
        </el-form-item>

        <el-form-item label="标签">
          <el-select
            v-model="templateForm.tags"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="请选择或输入标签"
          >
            <el-option label="图表" value="chart" />
            <el-option label="表格" value="table" />
            <el-option label="统计" value="statistics" />
            <el-option label="分析" value="analysis" />
            <el-option label="监控" value="monitor" />
          </el-select>
        </el-form-item>

        <el-form-item label="是否公开">
          <el-switch v-model="templateForm.isPublic" />
        </el-form-item>

        <el-divider content-position="left">模板配置</el-divider>

        <el-form-item label="组件配置">
          <div class="widgets-config">
            <el-button @click="addWidget" type="primary" size="small">
              <el-icon><Plus /></el-icon>
              添加组件
            </el-button>
            
            <div class="widgets-list">
              <el-card
                v-for="(widget, index) in templateForm.widgets"
                :key="index"
                class="widget-card"
                shadow="hover"
              >
                <template #header>
                  <div class="widget-header">
                    <span>组件 {{ index + 1 }}</span>
                    <el-button @click="removeWidget(index)" type="danger" size="small" text>
                      <el-icon><Delete /></el-icon>
                    </el-button>
                  </div>
                </template>
                
                <el-form-item label="组件类型">
                  <el-select v-model="widget.type" placeholder="选择组件类型">
                    <el-option label="图表" value="chart" />
                    <el-option label="表格" value="table" />
                    <el-option label="统计卡片" value="statistic" />
                    <el-option label="文本" value="text" />
                  </el-select>
                </el-form-item>
                
                <el-form-item label="组件标题">
                  <el-input v-model="widget.title" placeholder="请输入组件标题" />
                </el-form-item>
                
                <el-form-item label="数据源">
                  <el-select v-model="widget.dataSource" placeholder="选择数据源">
                    <el-option
                      v-for="ds in dataSources"
                      :key="ds.id"
                      :label="ds.name"
                      :value="ds.id"
                    />
                  </el-select>
                </el-form-item>
                
                <el-form-item label="集合名称">
                  <el-input v-model="widget.collection" placeholder="请输入集合名称" />
                </el-form-item>
              </el-card>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="布局配置">
          <el-radio-group v-model="templateForm.layout.type">
            <el-radio label="grid">网格布局</el-radio>
            <el-radio label="flex">弹性布局</el-radio>
            <el-radio label="fixed">固定布局</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="主题配置">
          <el-select v-model="templateForm.theme.name" placeholder="选择主题">
            <el-option label="默认主题" value="default" />
            <el-option label="深色主题" value="dark" />
            <el-option label="浅色主题" value="light" />
            <el-option label="蓝色主题" value="blue" />
            <el-option label="绿色主题" value="green" />
          </el-select>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import axios from 'axios'

const route = useRoute()
const router = useRouter()

// 响应式数据
const saving = ref(false)
const dataSources = ref([])
const formRef = ref()

// 模板表单
const templateForm = ref({
  name: '',
  description: '',
  category: '',
  tags: [],
  isPublic: false,
  widgets: [],
  layout: {
    type: 'grid'
  },
  theme: {
    name: 'default'
  }
})

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入模板名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  description: [
    { max: 200, message: '描述不能超过200个字符', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ]
}

// 计算属性
const isEdit = computed(() => route.params.id !== undefined)

// 方法
const fetchDataSources = async () => {
  try {
    const response = await axios.get('/api/datasource')
    dataSources.value = response.data
  } catch (error) {
    console.error('获取数据源失败:', error)
    ElMessage.error('获取数据源失败')
  }
}

const fetchTemplate = async (id) => {
  try {
    const response = await axios.get(`/api/templates/${id}`)
    const template = response.data
    
    templateForm.value = {
      name: template.name || '',
      description: template.description || '',
      category: template.category || '',
      tags: template.tags || [],
      isPublic: template.isPublic || false,
      widgets: template.widgets || [],
      layout: template.layout || { type: 'grid' },
      theme: template.theme || { name: 'default' }
    }
  } catch (error) {
    console.error('获取模板失败:', error)
    ElMessage.error('获取模板失败')
  }
}

const addWidget = () => {
  templateForm.value.widgets.push({
    type: 'chart',
    title: '',
    dataSource: '',
    collection: '',
    config: {}
  })
}

const removeWidget = (index) => {
  templateForm.value.widgets.splice(index, 1)
}

const saveTemplate = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    saving.value = true
    
    if (isEdit.value) {
      await axios.put(`/api/templates/${route.params.id}`, templateForm.value)
      ElMessage.success('模板更新成功')
    } else {
      await axios.post('/api/templates', templateForm.value)
      ElMessage.success('模板创建成功')
    }
    
    router.push('/templates')
  } catch (error) {
    console.error('保存模板失败:', error)
    ElMessage.error('保存模板失败')
  } finally {
    saving.value = false
  }
}

// 生命周期
onMounted(async () => {
  await fetchDataSources()
  
  if (isEdit.value) {
    await fetchTemplate(route.params.id)
  }
})
</script>

<style scoped>
.report-template-editor {
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

.widgets-config {
  width: 100%;
}

.widgets-list {
  margin-top: 20px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.widget-card {
  margin-bottom: 0;
}

.widget-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

@media (max-width: 768px) {
  .widgets-list {
    grid-template-columns: 1fr;
  }
}
</style> 