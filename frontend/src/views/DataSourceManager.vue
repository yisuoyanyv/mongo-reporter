<template>
  <div class="datasource-manager">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>数据源管理</span>
          <el-button type="primary" size="small" @click="showAddDialog">新增数据源</el-button>
        </div>
      </template>
      
      <div v-if="loading" class="loading-container">
        <el-loading-spinner />
        <p>加载中...</p>
      </div>
      
      <div v-else-if="error" class="error-container">
        <p style="color: #f56c6c;">{{ error }}</p>
        <el-button @click="fetchDataSources" type="primary">重试</el-button>
      </div>
      
      <el-table v-else :data="dataSources" style="width:100%;margin-top:16px;">
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="uri" label="连接字符串" />
        <el-table-column prop="owner" label="创建者" />
        <el-table-column label="连接状态" width="120">
          <template #default="scope">
            <el-tag 
              :type="scope.row.connectionStatus === 'success' ? 'success' : 
                     scope.row.connectionStatus === 'error' ? 'danger' : 'info'"
              size="small"
            >
              {{ scope.row.connectionStatus === 'success' ? '正常' : 
                 scope.row.connectionStatus === 'error' ? '异常' : '未测试' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="scope">
            <el-button size="small" @click="testConnection(scope.row)">测试连接</el-button>
            <el-button size="small" @click="editDataSource(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteDataSource(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-empty v-if="!loading && !error && dataSources.length === 0" description="暂无数据源" />
    </el-card>
    
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑数据源' : '新增数据源'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入数据源名称" />
        </el-form-item>
        <el-form-item label="连接字符串" prop="uri">
          <div style="display: flex; gap: 8px;">
            <el-input v-model="form.uri" placeholder="mongodb://localhost:27017/database" />
            <el-button 
              type="primary" 
              size="small" 
              @click="testFormConnection" 
              :loading="testingConnection"
              style="flex-shrink: 0;"
            >
              测试连接
            </el-button>
          </div>
          <div v-if="connectionTestResult" style="margin-top: 8px;">
            <el-alert 
              :title="connectionTestResult.message" 
              :type="connectionTestResult.success ? 'success' : 'error'"
              :closable="false"
              show-icon
            />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveDataSource" :loading="saving">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const dataSources = ref([])
const loading = ref(false)
const saving = ref(false)
const testingConnection = ref(false)
const error = ref('')
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const connectionTestResult = ref(null)

const form = ref({
  name: '',
  uri: ''
})

const rules = {
  name: [
    { required: true, message: '请输入数据源名称', trigger: 'blur' }
  ],
  uri: [
    { required: true, message: '请输入连接字符串', trigger: 'blur' },
    { pattern: /^mongodb:\/\/.+/, message: '请输入有效的MongoDB连接字符串', trigger: 'blur' }
  ]
}

const fetchDataSources = async () => {
  loading.value = true
  error.value = ''
  console.log('开始获取数据源...')
  
  try {
    console.log('发送请求到 /api/datasource')
    const response = await axios.get('/api/datasource')
    console.log('API响应:', response)
    console.log('响应数据:', response.data)
    
    // 为每个数据源添加连接状态
    dataSources.value = response.data.map(ds => ({
      ...ds,
      connectionStatus: 'unknown' // 初始状态为未测试
    }))
    console.log('数据源列表已更新:', dataSources.value)
    ElMessage.success(`成功加载 ${response.data.length} 个数据源`)
  } catch (err) {
    console.error('获取数据源失败:', err)
    error.value = `获取数据源失败: ${err.response?.data?.message || err.message}`
    ElMessage.error(error.value)
  } finally {
    loading.value = false
  }
}

const testConnection = async (dataSource) => {
  try {
    const response = await axios.post('/api/datasource/test', { uri: dataSource.uri })
    const result = response.data
    
    // 更新数据源的连接状态
    const index = dataSources.value.findIndex(ds => ds.id === dataSource.id)
    if (index !== -1) {
      dataSources.value[index].connectionStatus = result.success ? 'success' : 'error'
    }
    
    if (result.success) {
      ElMessage.success('连接测试成功')
    } else {
      ElMessage.error(`连接测试失败: ${result.message}`)
    }
  } catch (err) {
    console.error('测试连接失败:', err)
    ElMessage.error(`测试连接失败: ${err.response?.data?.message || err.message}`)
    
    // 更新连接状态为错误
    const index = dataSources.value.findIndex(ds => ds.id === dataSource.id)
    if (index !== -1) {
      dataSources.value[index].connectionStatus = 'error'
    }
  }
}

const testFormConnection = async () => {
  if (!form.value.uri || !form.value.uri.trim()) {
    ElMessage.warning('请先输入连接字符串')
    return
  }
  
  testingConnection.value = true
  connectionTestResult.value = null
  
  try {
    const response = await axios.post('/api/datasource/test', { uri: form.value.uri })
    connectionTestResult.value = response.data
    
    if (response.data.success) {
      ElMessage.success('连接测试成功')
    } else {
      ElMessage.error(`连接测试失败: ${response.data.message}`)
    }
  } catch (err) {
    console.error('测试连接失败:', err)
    connectionTestResult.value = {
      success: false,
      message: err.response?.data?.message || err.message
    }
    ElMessage.error(`测试连接失败: ${err.response?.data?.message || err.message}`)
  } finally {
    testingConnection.value = false
  }
}

const showAddDialog = () => {
  isEdit.value = false
  form.value = { name: '', uri: '' }
  connectionTestResult.value = null
  dialogVisible.value = true
}

const editDataSource = (row) => {
  isEdit.value = true
  form.value = { ...row }
  connectionTestResult.value = null
  dialogVisible.value = true
}

const saveDataSource = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    saving.value = true
    
    if (isEdit.value) {
      await axios.put(`/api/datasource/${form.value.id}`, form.value)
      ElMessage.success('更新成功')
    } else {
      await axios.post('/api/datasource', form.value)
      ElMessage.success('添加成功')
    }
    
    dialogVisible.value = false
    await fetchDataSources()
  } catch (err) {
    console.error('保存数据源失败:', err)
    ElMessage.error(`保存失败: ${err.response?.data?.message || err.message}`)
  } finally {
    saving.value = false
  }
}

const deleteDataSource = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除数据源"${row.name}"吗？`, '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await axios.delete(`/api/datasource/${row.id}`)
    ElMessage.success('删除成功')
    await fetchDataSources()
  } catch (err) {
    if (err !== 'cancel') {
      console.error('删除数据源失败:', err)
      ElMessage.error(`删除失败: ${err.response?.data?.message || err.message}`)
    }
  }
}

onMounted(() => {
  console.log('数据源管理页面已挂载')
  fetchDataSources()
})
</script>

<style scoped>
.datasource-manager {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: #909399;
}

.loading-container p {
  margin-top: 10px;
}

.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: #f56c6c;
}

.error-container p {
  margin-bottom: 10px;
}
</style> 