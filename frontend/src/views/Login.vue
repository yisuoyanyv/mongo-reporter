<template>
  <div class="login-container">
    <el-card class="login-card">
      <div slot="header" class="login-title">登录 Mongo Reporter</div>
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
      </el-form>
    </el-card>
  </div>
</template>
<script setup>
import { ref } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
const form = ref({ username: '', password: '' })
const router = useRouter()
function onLogin() {
  axios.post('/api/auth/login', form.value).then(res => {
    if (res.data.token) {
      localStorage.setItem('token', res.data.token)
      localStorage.setItem('username', res.data.username)
      router.push('/designer')
    } else {
      window.$message?.error(res.data.error || '登录失败')
    }
  })
}
function goRegister() {
  router.push('/register')
}
</script>
<style scoped>
.login-container { display:flex;justify-content:center;align-items:center;height:100vh;background:#f5f7fa; }
.login-card { width:340px; }
.login-title { font-size:22px;font-weight:bold;text-align:center;margin-bottom:16px; }
</style> 