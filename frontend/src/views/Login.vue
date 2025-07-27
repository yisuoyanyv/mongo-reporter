<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="login-title">登录 MongoReporter</div>
      </template>
      <el-form :model="form" @submit.native.prevent="onLogin">
        <el-form-item label="用户名">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onLogin" style="width:100%">登录</el-button>
        </el-form-item>
        <el-form-item>
          <el-link @click="goRegister">没有账号？注册</el-link>
        </el-form-item>
        <el-form-item style="text-align:center;color:#999;font-size:12px;">
          <div>测试账号：admin / admin123</div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const form = ref({ username: 'admin', password: 'admin123' })
const router = useRouter()

const onLogin = async () => {
  try {
    const response = await axios.post('/api/auth/login', form.value)
    if (response.data.token) {
      localStorage.setItem('token', response.data.token)
      localStorage.setItem('username', response.data.username)
      ElMessage.success('登录成功')
      router.push('/reports')
    } else {
      ElMessage.error(response.data.error || '登录失败')
    }
  } catch (error) {
    console.error('登录失败:', error)
    ElMessage.error('登录失败，请检查用户名和密码')
  }
}

const goRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: #f5f7fa;
}

.login-card {
  width: 340px;
}

.login-title {
  font-size: 22px;
  font-weight: bold;
  text-align: center;
  margin-bottom: 16px;
}
</style> 