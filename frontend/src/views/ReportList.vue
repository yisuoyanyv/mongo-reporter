<template>
  <el-card>
    <template #header>
      <span>报表管理</span>
      <el-button type="primary" size="small" @click="$router.push('/designer')" style="float:right;">新建报表</el-button>
    </template>
    <el-table :data="reports" style="width:100%;margin-top:16px;">
      <el-table-column prop="name" label="报表名称" />
      <el-table-column prop="chartType" label="图表类型" />
      <el-table-column label="操作" width="180">
        <template #default="scope">
          <el-button size="small" @click="loadReport(scope.row)">加载</el-button>
          <el-button size="small" type="danger" @click="deleteReport(scope.row)">删除</el-button>
          <el-button size="small" @click="shareReport(scope.row)">分享链接</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
const reports = ref([])
const router = useRouter()
function fetchReports() {
  axios.get('/api/report/list').then(res => {
    reports.value = res.data
  })
}
function loadReport(row) {
  // 跳转到设计器并传递报表ID
  router.push({ path: '/designer', query: { id: row.id } })
}
function deleteReport(row) {
  axios.delete(`/api/report/delete/${row.id}`).then(fetchReports)
}
function shareReport(row) {
  const url = `${window.location.origin}/#/share/${row.id}`
  navigator.clipboard.writeText(url)
  window.$message?.success('分享链接已复制')
}
onMounted(fetchReports)
</script> 