<template>
  <div class="login-container">
    <el-card class="login-card">
      <div slot="header" class="login-title">注册账号</div>
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
function onRegister() {
  axios.post('/api/auth/register', form.value).then(res => {
    window.$message?.success(res.data.msg || '注册成功')
    router.push('/login')
  })
}
function goLogin() {
  router.push('/login')
}
</script>
<style scoped>
.login-container { display:flex;justify-content:center;align-items:center;height:100vh;background:#f5f7fa; }
.login-card { width:340px; }
.login-title { font-size:22px;font-weight:bold;text-align:center;margin-bottom:16px; }
</style> 