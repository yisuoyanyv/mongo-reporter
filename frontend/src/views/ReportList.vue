<template>
  <div class="report-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>报表列表</span>
          <el-button type="primary" @click="$router.push('/designer')">新建报表</el-button>
        </div>
      </template>
      
      <el-table :data="reports" style="width: 100%">
        <el-table-column prop="name" label="报表名称" />
        <el-table-column prop="description" label="描述" />
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const reports = ref([])

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

const loadReports = async () => {
  try {
    const response = await axios.get('/api/report/configs')
    reports.value = response.data
  } catch (error) {
    console.error('加载报表列表失败:', error)
    ElMessage.error('加载报表列表失败')
  }
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
</style> 