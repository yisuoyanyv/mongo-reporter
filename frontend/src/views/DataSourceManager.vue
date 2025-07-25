<template>
  <el-card>
    <template #header>
      <span>数据源管理</span>
      <el-button type="primary" size="small" @click="dialogVisible=true" style="float:right;">新增数据源</el-button>
    </template>
    <el-table :data="dataSources" style="width:100%;margin-top:16px;">
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="uri" label="连接字符串" />
      <el-table-column label="操作" width="160">
        <template #default="scope">
          <el-button size="small" @click="editDataSource(scope.$index, scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteDataSource(scope.$index)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="dialogVisible" :title="editIndex === -1 ? '新增数据源' : '编辑数据源'" width="400px">
      <el-form :model="form">
        <el-form-item label="名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="连接字符串">
          <el-input v-model="form.uri" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="addDataSource">确定</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const dataSources = ref([])
const dialogVisible = ref(false)
const form = ref({ name: '', uri: '' })
const editIndex = ref(-1)

function fetchData() {
  axios.get('/api/datasource').then(res => {
    dataSources.value = res.data
  })
}
function editDataSource(index, row) {
  form.value = { ...row }
  editIndex.value = index
  dialogVisible.value = true
}
function addDataSource() {
  if (editIndex.value === -1) {
    axios.post('/api/datasource', form.value).then(() => {
      dialogVisible.value = false
      form.value = { name: '', uri: '' }
      fetchData()
    })
  } else {
    axios.put(`/api/datasource/${editIndex.value}`, form.value).then(() => {
      dialogVisible.value = false
      form.value = { name: '', uri: '' }
      editIndex.value = -1
      fetchData()
    })
  }
}
function deleteDataSource(index) {
  axios.delete(`/api/datasource/${index}`).then(fetchData)
}
onMounted(fetchData)
</script> 