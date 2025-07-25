<template>
  <el-card style="max-width:800px;margin:40px auto;">
    <template #header>
      <span>报表分享</span>
    </template>
    <div v-if="report">
      <div style="font-size:20px;font-weight:bold;margin-bottom:12px;">{{ report.name }}</div>
      <component :is="getChartComponent(report)" :option="getChartOption(report)" style="height:400px;width:100%" />
    </div>
    <div v-else>
      <el-alert title="该报表未公开或不存在" type="error" />
    </div>
  </el-card>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import VChart from 'vue-echarts'
import { useRoute } from 'vue-router'
const route = useRoute()
const report = ref(null)
function getChartComponent(r) {
  if (!r) return null
  if (r.chartType === 'bar' || r.chartType === 'line' || r.chartType === 'pie') return VChart
  return null
}
function getChartOption(r) {
  if (!r) return {}
  return r.chartConfig || {}
}
onMounted(() => {
  axios.get(`/api/report/get/${route.params.id}`).then(res => {
    if (res.data && res.data.publicShare) {
      report.value = res.data
    } else {
      report.value = null
    }
  })
})
</script> 