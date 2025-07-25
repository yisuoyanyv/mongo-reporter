<script setup>
import { useRouter, useRoute } from 'vue-router'
import { computed } from 'vue'
const router = useRouter()
const route = useRoute()
const activeMenu = computed(() => route.path)
function go(path) {
  if (route.path !== path) router.push(path)
}
</script>

<template>
  <el-container style="height:100vh;">
    <el-header height="60px" style="background:#2d8cf0;color:#fff;display:flex;align-items:center;font-size:22px;">
      <span style="font-weight:bold;">Mongo Reporter 数据报表平台</span>
      <el-menu
        mode="horizontal"
        :default-active="activeMenu"
        background-color="#2d8cf0"
        text-color="#fff"
        active-text-color="#ffd04b"
        style="margin-left:40px;flex:1;">
        <el-menu-item index="/designer" @click="go('/designer')">报表设计器</el-menu-item>
        <el-menu-item index="/datasource" @click="go('/datasource')">数据源管理</el-menu-item>
        <el-menu-item index="/reports" @click="go('/reports')">报表管理</el-menu-item>
      </el-menu>
    </el-header>
    <el-main style="padding:0;">
      <router-view />
    </el-main>
  </el-container>
</template>

<style scoped>
</style>
