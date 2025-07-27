<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <div class="register-title">注册 MongoReporter</div>
      </template>
      <el-form :model="form" @submit.native.prevent="onRegister">
        <el-form-item label="用户名">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onRegister" style="width:100%">注册</el-button>
        </el-form-item>
        <el-form-item>
          <el-link @click="goLogin">已有账号？登录</el-link>
        </el-form-item>
        <el-form-item style="text-align:center;color:#999;font-size:12px;">
          <div>默认测试账号：testuser / test123</div>
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

const form = ref({ username: 'testuser', password: 'test123' })
const router = useRouter()

const onRegister = async () => {
  try {
    const response = await axios.post('/api/auth/register', form.value)
    if (response.data.success) {
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    } else {
      ElMessage.error(response.data.error || '注册失败')
    }
  } catch (error) {
    console.error('注册失败:', error)
    ElMessage.error('注册失败，请检查用户名是否已存在')
  }
}

const goLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: #f5f7fa;
}

.register-card {
  width: 340px;
}

.register-title {
  font-size: 22px;
  font-weight: bold;
  text-align: center;
  margin-bottom: 16px;
}
</style> 