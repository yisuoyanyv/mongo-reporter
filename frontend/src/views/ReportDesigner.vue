<template>
  <el-container style="height: 100vh;">
    <el-aside width="220px" style="background:#f5f7fa;">
      <div class="sidebar-title">组件库</div>
      <draggable v-model="widgets" group="widgets" item-key="name" :clone="cloneWidget" :sort="false">
        <transition-group>
          <div v-for="item in widgets" :key="item.name" class="widget-item">
            <el-card shadow="hover">{{ item.label }}</el-card>
          </div>
        </transition-group>
      </draggable>
      <el-divider />
      <div style="padding:8px;">
        <el-form label-width="60px" size="small">
          <el-form-item label="数据源">
            <el-select v-model="selectedDataSource" @change="onDataSourceChange" style="width:140px">
              <el-option v-for="(ds, idx) in dataSources" :key="idx" :label="ds.name" :value="ds.uri" />
            </el-select>
          </el-form-item>
          <el-form-item label="集合">
            <el-select v-model="selectedCollection" @change="onCollectionChange" style="width:140px">
              <el-option v-for="col in collections" :key="col" :label="col" :value="col" />
            </el-select>
          </el-form-item>
          <el-form-item label="字段映射">
            <el-select v-model="fieldMapping.x" placeholder="X轴" style="width:60px;margin-right:4px">
              <el-option v-for="f in fields" :key="f" :label="f" :value="f" />
            </el-select>
            <el-select v-model="fieldMapping.y" placeholder="Y轴" style="width:60px">
              <el-option v-for="f in fields" :key="f" :label="f" :value="f" />
            </el-select>
          </el-form-item>
          <el-form-item label="公开分享">
            <el-switch v-model="publicShare" />
          </el-form-item>
        </el-form>
      </div>
    </el-aside>
    <el-container>
      <el-header style="background:#fff;box-shadow:0 1px 4px #eee;display:flex;align-items:center;">
        <span style="font-size:18px;font-weight:bold;">报表设计器</span>
        <el-button type="primary" style="margin-left:20px;" @click="saveReport">保存报表</el-button>
        <el-button style="margin-left:10px;" @click="loadReport">加载报表</el-button>
        <el-button style="margin-left:10px;" @click="exportImage">导出图片</el-button>
        <el-button style="margin-left:10px;" @click="exportPDF">导出PDF</el-button>
        <el-button style="margin-left:10px;" @click="exportExcel">导出Excel</el-button>
      </el-header>
      <el-main>
        <div class="designer-main">
          <draggable v-model="canvas" group="widgets" item-key="id" class="designer-canvas" :sort="true">
            <transition-group>
              <div v-for="item in canvas" :key="item.id" class="canvas-item">
                <el-card shadow="always">
                  <component :is="getChartComponent(item)" v-if="item.name !== 'table'" :option="getChartOption(item)" style="height:240px;width:100%" />
                  <div v-else>表格（后续支持）</div>
                </el-card>
              </div>
            </transition-group>
          </draggable>
          <div class="chart-preview" style="width:360px;">
            <el-card shadow="never" style="height:100%;">
              <component :is="getChartComponent(previewChart)" v-if="previewChart && previewChart.name !== 'table'" :option="getChartOption(previewChart)" style="height:320px;width:100%" />
              <div v-else>图表预览区（后续支持echarts等）</div>
            </el-card>
            <el-divider />
            <el-form label-width="60px" size="small" style="margin-top:16px;">
              <el-form-item label="标题">
                <el-input v-model="chartConfig.title" />
              </el-form-item>
              <el-form-item label="主色">
                <el-color-picker v-model="chartConfig.color" />
              </el-form-item>
              <el-form-item label="图例">
                <el-switch v-model="chartConfig.legend" />
              </el-form-item>
            </el-form>
          </div>
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import draggable from 'vuedraggable'
import { use } from 'echarts/core'
import VChart from 'vue-echarts'
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import axios from 'axios'
import html2canvas from 'html2canvas'
import jsPDF from 'jspdf'
import * as XLSX from 'xlsx'

use([BarChart, LineChart, PieChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent, CanvasRenderer])

const widgets = ref([
  { name: 'table', label: '表格' },
  { name: 'bar', label: '柱状图' },
  { name: 'line', label: '折线图' },
  { name: 'pie', label: '饼图' },
])
const canvas = ref([])
const previewChart = ref(null)
const chartConfig = ref({ title: '演示图表', color: '#409EFF', legend: true })

const dataSources = ref([])
const selectedDataSource = ref('')
const collections = ref([])
const selectedCollection = ref('')
const fields = ref([])
const fieldMapping = ref({ x: '', y: '' })
const publicShare = ref(false)

const route = useRoute()
const reportId = ref(route.query.id || null)
watch(() => route.query.id, (val) => { reportId.value = val; if (val) loadReportById(val) })

function cloneWidget(widget) {
  return { ...widget, id: Date.now() + Math.random(), name: widget.name, label: widget.label }
}
function getChartComponent(item) {
  if (!item) return null
  if (item.name === 'bar' || item.name === 'line' || item.name === 'pie') return VChart
  return null
}
function getChartOption(item) {
  if (!item) return {}
  // 动态数据示例，后续可接入真实数据
  if (item.name === 'bar') {
    return {
      title: { text: chartConfig.value.title },
      tooltip: {},
      xAxis: { data: fields.value.length ? fields.value.slice(0,3) : ['A','B','C'] },
      yAxis: {},
      color: [chartConfig.value.color],
      legend: chartConfig.value.legend ? {} : undefined,
      series: [{ type: 'bar', data: [5, 20, 36] }]
    }
  }
  if (item.name === 'line') {
    return {
      title: { text: chartConfig.value.title },
      tooltip: {},
      xAxis: { data: fields.value.length ? fields.value.slice(0,3) : ['A','B','C'] },
      yAxis: {},
      color: [chartConfig.value.color],
      legend: chartConfig.value.legend ? {} : undefined,
      series: [{ type: 'line', data: [15, 10, 26] }]
    }
  }
  if (item.name === 'pie') {
    return {
      title: { text: chartConfig.value.title },
      tooltip: {},
      color: [chartConfig.value.color],
      legend: chartConfig.value.legend ? { top: 'bottom' } : undefined,
      series: [{ type: 'pie', data: fields.value.length ? fields.value.slice(0,3).map((f,i)=>({ value: (i+1)*10, name: f })) : [ { value: 10, name: 'A' }, { value: 20, name: 'B' }, { value: 30, name: 'C' } ] }]
    }
  }
  return {}
}
function fetchDataSources() {
  axios.get('/api/datasource').then(res => {
    dataSources.value = res.data
  })
}
function onDataSourceChange(uri) {
  axios.post('/api/report/collections', { uri }).then(res => {
    collections.value = res.data
    selectedCollection.value = ''
    fields.value = []
    fieldMapping.value = { x: '', y: '' }
  })
}
function onCollectionChange(col) {
  axios.post('/api/report/fields', { uri: selectedDataSource.value, collection: col }).then(res => {
    fields.value = res.data
    fieldMapping.value = { x: '', y: '' }
  })
}
function saveReport() {
  const config = {
    id: reportId.value,
    name: '演示报表',
    dataSource: selectedDataSource.value,
    collection: selectedCollection.value,
    chartType: canvas.value.length ? canvas.value[0].name : '',
    fieldMapping: { ...fieldMapping.value },
    chartConfig: { ...chartConfig.value },
    publicShare: publicShare.value
  }
  axios.post('/api/report/save', config).then(res => {
    window.$message?.success('保存成功')
    if (res.data.id) reportId.value = res.data.id
  })
}
function loadReport() {
  axios.get('/api/report/list').then(res => {
    if (res.data.length) {
      const r = res.data[0]
      canvas.value = [{ name: r.chartType, label: r.chartType === 'bar' ? '柱状图' : r.chartType === 'line' ? '折线图' : '饼图', id: Date.now() + Math.random() }]
      reportId.value = r.id
    }
  })
}
function loadReportById(id) {
  axios.get(`/api/report/get/${id}`).then(res => {
    const r = res.data
    if (r) {
      canvas.value = [{ name: r.chartType, label: r.chartType === 'bar' ? '柱状图' : r.chartType === 'line' ? '折线图' : '饼图', id: Date.now() + Math.random() }]
      reportId.value = r.id
      selectedDataSource.value = r.dataSource
      selectedCollection.value = r.collection
      fieldMapping.value = { ...r.fieldMapping }
      if (r.chartConfig) chartConfig.value = { ...chartConfig.value, ...r.chartConfig }
      publicShare.value = !!r.publicShare
      if (r.dataSource) onDataSourceChange(r.dataSource)
      if (r.collection) onCollectionChange(r.collection)
    }
  })
}
function exportImage() {
  const el = document.querySelector('.designer-canvas')
  html2canvas(el).then(canvas => {
    const link = document.createElement('a')
    link.href = canvas.toDataURL('image/png')
    link.download = 'report.png'
    link.click()
  })
}
function exportPDF() {
  const el = document.querySelector('.designer-canvas')
  html2canvas(el).then(canvas => {
    const imgData = canvas.toDataURL('image/png')
    const pdf = new jsPDF('landscape')
    pdf.addImage(imgData, 'PNG', 10, 10, 260, 120)
    pdf.save('report.pdf')
  })
}
function exportExcel() {
  // 简单导出字段映射和示例数据
  const ws = XLSX.utils.aoa_to_sheet([
    ['X轴', 'Y轴'],
    [fieldMapping.value.x, fieldMapping.value.y],
    ['A', 5], ['B', 20], ['C', 36]
  ])
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '报表数据')
  XLSX.writeFile(wb, 'report.xlsx')
}
onMounted(() => {
  fetchDataSources()
  if (reportId.value) loadReportById(reportId.value)
})
</script>

<style scoped>
.sidebar-title {
  font-weight: bold;
  font-size: 18px;
  margin: 16px 0 12px 16px;
}
.widget-item {
  margin: 12px 8px;
  cursor: grab;
}
.designer-main {
  display: flex;
  height: 100%;
}
.designer-canvas {
  flex: 1;
  min-height: 400px;
  background: #fafcff;
  border: 1px dashed #d3dce6;
  margin: 16px;
  padding: 16px;
  border-radius: 8px;
}
.canvas-item {
  margin-bottom: 12px;
}
.chart-preview {
  width: 360px;
  margin: 16px 0 16px 0;
}
</style> 