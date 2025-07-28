<template>
  <div class="report-share">
    <el-card style="max-width:1200px;margin:40px auto;">
      <template #header>
        <div class="card-header">
          <span>报表分享</span>
          <div v-if="report">
            <el-button @click="refreshData" :loading="refreshing">
              <el-icon><Refresh /></el-icon>
              刷新数据
            </el-button>
            <el-dropdown @command="handleExport">
              <el-button type="primary">
                导出报表
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="pdf">导出PDF</el-dropdown-item>
                  <el-dropdown-item command="excel">导出Excel</el-dropdown-item>
                  <el-dropdown-item command="image">导出图片</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </template>
      
      <div v-if="report">
        <div class="report-info">
          <h2>{{ report.name }}</h2>
          <p v-if="report.description" class="description">{{ report.description }}</p>
        </div>
        
        <div class="report-content">
          <div v-for="widget in report.widgets" :key="widget.id" class="widget-container">
            <el-card shadow="hover">
              <template #header>
                <span>{{ getWidgetTitle(widget) }}</span>
              </template>
              <div class="chart-container">
                <div v-if="widget.name === 'table'" class="table-container">
                  <div v-if="tableData[widget.id] && tableData[widget.id].data" class="table-wrapper">
                    <!-- 搜索框 -->
                    <div v-if="widget.enableSearch" class="table-search" style="margin-bottom: 10px;">
                      <el-input
                        v-model="tableSearchText[widget.id]"
                        placeholder="搜索..."
                        style="width: 200px;"
                        @input="onTableSearch(widget)"
                        clearable
                      >
                        <template #prefix>
                          <el-icon><Search /></el-icon>
                        </template>
                      </el-input>
                    </div>
                    
                    <!-- 表格 -->
                    <el-table 
                      :data="getFilteredTableData(widget)" 
                      size="small" 
                      height="400" 
                      style="width: 100%"
                    >
                      <el-table-column 
                        v-for="column in getTableColumns(widget)" 
                        :key="column" 
                        :prop="column" 
                        :label="column" 
                        show-overflow-tooltip
                      />
                    </el-table>
                    
                    <!-- 分页 -->
                    <div v-if="widget.enablePagination" class="table-pagination" style="margin-top: 10px;">
                      <el-pagination
                        v-model:current-page="tableCurrentPage[widget.id]"
                        v-model:page-size="tablePageSize[widget.id]"
                        :page-sizes="[10, 20, 50, 100]"
                        :total="getFilteredTableData(widget).length"
                        layout="total, sizes, prev, pager, next, jumper"
                        @size-change="onTablePageSizeChange(widget)"
                        @current-change="onTableCurrentPageChange(widget)"
                      />
                    </div>
                    
                    <div class="table-info">
                      显示 {{ getFilteredTableData(widget).length }} 条数据，共 {{ tableData[widget.id].total }} 条
                    </div>
                  </div>
                  <div v-else class="table-loading">
                    <el-icon class="is-loading"><Loading /></el-icon>
                    <span>加载表格数据...</span>
                  </div>
                </div>
                <div v-else ref="chartRefs" class="chart" :id="'chart-' + widget.id"></div>
              </div>
            </el-card>
          </div>
        </div>
      </div>
      <div v-else>
        <el-alert 
          title="该报表未公开分享或不存在" 
          description="请联系报表创建者获取访问权限" 
          type="error" 
          show-icon
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { Loading, Search, Refresh, ArrowDown } from '@element-plus/icons-vue'
import html2canvas from 'html2canvas'
import jsPDF from 'jspdf'
import * as XLSX from 'xlsx'

const route = useRoute()
const report = ref({})
const chartRefs = ref([])
const tableData = ref({})
// 表格分页和搜索相关数据
const tableCurrentPage = ref({})
const tablePageSize = ref({})
const tableSearchText = ref({})
const refreshing = ref(false)

const loadReport = async (id) => {
  try {
    const reportId = id || route.params.id
    const response = await axios.get(`/api/report/configs/${reportId}`)
    report.value = response.data
    
    // 确保widget配置正确
    if (report.value.widgets) {
      report.value.widgets = report.value.widgets.map(widget => {
        // 合并配置，优先使用 config 中的配置
        const config = widget.config || {}
        return {
          ...widget,
          // 确保所有配置字段都存在
          title: widget.title || widget.label || widget.name,
          xField: widget.xField || config.xField || '',
          yField: widget.yField || config.yField || '',
          seriesField: widget.seriesField || config.seriesField || '',
          nameField: widget.nameField || config.nameField || '',
          valueField: widget.valueField || config.valueField || '',
          sizeField: widget.sizeField || config.sizeField || '',
          min: widget.min || config.min || 0,
          max: widget.max || config.max || 100,
          // 统计配置
          enableStats: widget.enableStats || config.enableStats || false,
          aggregation: widget.aggregation || config.aggregation || 'sum',
          groupBy: widget.groupBy || config.groupBy || '',
          sortBy: widget.sortBy || config.sortBy || '',
          sortOrder: widget.sortOrder || config.sortOrder || 'desc',
          limit: widget.limit || config.limit || 10,
          // 样式配置
          theme: widget.theme || config.theme || 'default',
          showLegend: widget.showLegend !== false && config.showLegend !== false,
          showLabel: widget.showLabel !== false && config.showLabel !== false,
          animation: widget.animation !== false && config.animation !== false,
          colors: widget.colors || config.colors || [],
          // 表格配置
          displayFields: widget.displayFields || config.displayFields || [],
          pageSize: widget.pageSize || config.pageSize || 10,
          enablePagination: widget.enablePagination !== false && config.enablePagination !== false,
          enableSearch: widget.enableSearch !== false && config.enableSearch !== false
        }
      })
    }
    
    // 加载表格数据
    await loadTableData()
    
    // 渲染图表
    await nextTick()
    debouncedRenderCharts()
  } catch (error) {
    console.error('加载报表失败:', error)
    ElMessage.error('加载报表失败')
  }
}

const loadTableData = async () => {
  for (const widget of report.value.widgets || []) {
    if (widget.name === 'table') {
      try {
        const response = await axios.post('/api/report/chart-data', {
          uri: report.value.dataSourceUri,
          collection: report.value.collection,
          filters: report.value.filters || [],
          widget: {
            name: widget.name,
            type: widget.name,
            displayFields: widget.displayFields,
            pageSize: widget.pageSize,
            enablePagination: widget.enablePagination,
            enableSearch: widget.enableSearch
          }
        })
        
        if (response.data.success) {
          tableData.value[widget.id] = {
            data: response.data.data || [],
            total: response.data.total || 0
          }
          
          // 初始化分页和搜索
          tableCurrentPage.value[widget.id] = 1
          tablePageSize.value[widget.id] = widget.pageSize || 10
          tableSearchText.value[widget.id] = ''
        }
      } catch (error) {
        console.error('加载表格数据失败:', error)
      }
    }
  }
}

const generateChartOption = async (widget) => {
  console.log('生成图表配置，widget:', widget)
  
  // 应用样式配置
  const baseOption = {
    title: { text: widget.title || widget.label || widget.name },
    tooltip: {},
    legend: { show: widget.showLegend !== false },
    animation: widget.animation !== false,
    color: widget.colors && widget.colors.length > 0 ? widget.colors : ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc']
  }
  
  try {
    // 获取真实数据
    const requestData = {
      uri: report.value.dataSourceUri,
      collection: report.value.collection,
      filters: report.value.filters || [],
      widget: {
        name: widget.name || widget.type,
        type: widget.type || widget.name,
        xField: widget.xField,
        yField: widget.yField,
        seriesField: widget.seriesField,
        nameField: widget.nameField,
        valueField: widget.valueField,
        sizeField: widget.sizeField,
        min: widget.min,
        max: widget.max,
        // 统计配置
        enableStats: widget.enableStats,
        aggregation: widget.aggregation,
        groupBy: widget.groupBy,
        sortBy: widget.sortBy,
        sortOrder: widget.sortOrder,
        limit: widget.limit,
        // 样式配置
        theme: widget.theme,
        showLegend: widget.showLegend,
        showLabel: widget.showLabel,
        animation: widget.animation,
        colors: widget.colors
      }
    }
    
    console.log('发送图表数据请求:', requestData)
    const response = await axios.post('/api/report/chart-data', requestData)
    
    console.log('分享页面图表数据响应:', widget.name, response.data)
    
    if (response.data.success) {
      // 使用后端返回的数据，与ReportViewer保持一致
      if (response.data.success) {
        if (widget.name === 'line' || widget.type === 'line') {
          return {
            ...baseOption,
            xAxis: { type: 'category', data: response.data.xAxis },
            yAxis: { type: 'value' },
            series: response.data.series.map(series => ({
              ...series,
              label: { show: widget.showLabel !== false }
            }))
          }
        } else if (widget.type === 'bar' || widget.name === 'bar') {
          return {
            ...baseOption,
            xAxis: { type: 'category', data: response.data.xAxis },
            yAxis: { type: 'value' },
            series: response.data.series.map(series => ({
              ...series,
              label: { show: widget.showLabel !== false }
            }))
          }
        } else if (widget.type === 'pie' || widget.name === 'pie') {
          return {
            ...baseOption,
            series: [{ 
              type: 'pie', 
              radius: '50%',
              data: response.data.series,
              label: { show: widget.showLabel !== false }
            }]
          }
        } else if (widget.name === 'scatter') {
          return {
            ...baseOption,
            xAxis: { type: 'value' },
            yAxis: { type: 'value' },
            series: response.data.series.map(series => ({
              ...series,
              label: { show: widget.showLabel !== false }
            }))
          }
        } else if (widget.name === 'gauge') {
          return {
            ...baseOption,
            series: [{
              type: 'gauge',
              min: response.data.min || widget.min || 0,
              max: response.data.max || widget.max || 100,
              data: response.data.series,
              detail: { show: widget.showLabel !== false }
            }]
          }
        } else if (widget.name === 'funnel') {
          return {
            ...baseOption,
            series: [{
              type: 'funnel',
              left: '10%',
              top: 60,
              bottom: 60,
              width: '80%',
              height: '80%',
              min: 0,
              max: 100,
              minSize: '0%',
              maxSize: '100%',
              sort: 'descending',
              gap: 2,
              label: {
                show: widget.showLabel !== false,
                position: 'inside'
              },
              labelLine: {
                length: 10,
                lineStyle: {
                  width: 1,
                  type: 'solid'
                }
              },
              itemStyle: {
                borderColor: '#fff',
                borderWidth: 1
              },
              emphasis: {
                label: {
                  fontSize: 20
                }
              },
              data: response.data.series
            }]
          }
        } else if (widget.name === 'radar') {
          return {
            ...baseOption,
            radar: {
              indicator: response.data.indicators || []
            },
            series: response.data.series.map(series => ({
              ...series,
              label: { show: widget.showLabel !== false }
            }))
          }
        }
      } else {
        console.warn('获取图表数据失败:', response.data.message)
        ElMessage.warning(`图表数据获取失败: ${response.data.message}`)
      }
    }
  } catch (error) {
    console.error('生成图表配置失败:', error)
  }
  
  return baseOption
}

let renderTimeout = null
const debouncedRenderCharts = () => {
  if (renderTimeout) {
    clearTimeout(renderTimeout)
  }
  renderTimeout = setTimeout(renderCharts, 100)
}

const renderCharts = async () => {
  console.log('开始渲染图表，widgets:', report.value.widgets)
  for (const widget of report.value.widgets || []) {
    if (widget.name !== 'table') {
      const chartId = `chart-${widget.id}`
      const chartDom = document.getElementById(chartId)
      console.log(`查找图表容器 ${chartId}:`, chartDom)
      
      if (chartDom) {
        // 清理现有图表
        const existingChart = echarts.getInstanceByDom(chartDom)
        if (existingChart) {
          existingChart.dispose()
        }
        
        // 根据主题配置初始化图表
        const theme = widget.theme || 'default'
        let chart
        if (theme === 'dark') {
          chart = echarts.init(chartDom, 'dark')
        } else if (theme === 'light') {
          chart = echarts.init(chartDom, 'light')
        } else {
          chart = echarts.init(chartDom)
        }
        
        const option = await generateChartOption(widget)
        console.log(`图表 ${chartId} 的配置:`, option)
        chart.setOption(option)
        console.log(`图表 ${chartId} 渲染完成`)
      } else {
        console.error(`未找到图表容器: ${chartId}`)
      }
    }
  }
}

const getWidgetTitle = (widget) => {
  // 如果有自定义标签，直接使用
  if (widget.label && widget.label !== widget.name) {
    return widget.label
  }
  
  // 根据图表类型生成友好的标题
  switch (widget.name) {
    case 'line':
      return '趋势分析'
    case 'bar':
      return '数据对比'
    case 'pie':
      return '占比分析'
    case 'scatter':
      return '散点分布'
    case 'gauge':
      return '指标监控'
    case 'table':
      return '数据明细'
    default:
      return widget.label || '数据展示'
  }
}

const getTableColumns = (widget) => {
  if (!tableData.value[widget.id] || !tableData.value[widget.id].data) {
    return []
  }
  
  const data = tableData.value[widget.id].data
  if (data.length === 0) return []
  
  // 如果有指定显示字段，使用指定字段
  if (widget.displayFields && widget.displayFields.length > 0) {
    return widget.displayFields
  }
  
  // 否则使用所有字段
  return Object.keys(data[0])
}

const getFilteredTableData = (widget) => {
  if (!tableData.value[widget.id] || !tableData.value[widget.id].data) {
    return []
  }
  
  let data = tableData.value[widget.id].data
  
  // 搜索过滤
  const searchText = tableSearchText.value[widget.id]
  if (searchText) {
    data = data.filter(item => {
      return Object.values(item).some(value => 
        String(value).toLowerCase().includes(searchText.toLowerCase())
      )
    })
  }
  
  return data
}

const onTableSearch = (widget) => {
  // 搜索时重置到第一页
  tableCurrentPage.value[widget.id] = 1
}

const onTablePageSizeChange = (widget) => {
  // 页面大小改变时重置到第一页
  tableCurrentPage.value[widget.id] = 1
}

const onTableCurrentPageChange = (widget) => {
  // 当前页改变时不需要特殊处理
}

const exportImage = () => {
  ElMessage.info('导出图片功能开发中...')
}

const exportToPDF = async () => {
  try {
    ElMessage.info('正在生成PDF...')
    
    const reportContainer = document.querySelector('.report-share')
    if (!reportContainer) {
      ElMessage.error('未找到报表容器')
      return
    }
    
    const canvas = await html2canvas(reportContainer, {
      scale: 2,
      useCORS: true,
      allowTaint: true,
      backgroundColor: '#ffffff'
    })
    
    const imgData = canvas.toDataURL('image/png')
    const pdf = new jsPDF('p', 'mm', 'a4')
    const imgWidth = 210
    const pageHeight = 295
    const imgHeight = (canvas.height * imgWidth) / canvas.width
    let heightLeft = imgHeight
    
    let position = 0
    
    pdf.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight)
    heightLeft -= pageHeight
    
    while (heightLeft >= 0) {
      position = heightLeft - imgHeight
      pdf.addPage()
      pdf.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight)
      heightLeft -= pageHeight
    }
    
    const fileName = `${report.value.name || '报表'}_${new Date().toISOString().slice(0, 10)}.pdf`
    pdf.save(fileName)
    ElMessage.success('PDF导出成功')
  } catch (error) {
    console.error('PDF导出失败:', error)
    ElMessage.error('PDF导出失败')
  }
}

const exportToExcel = async () => {
  try {
    ElMessage.info('正在生成Excel...')
    
    const workbook = XLSX.utils.book_new()
    
    // 导出表格数据
    for (const widget of report.value.widgets || []) {
      if (widget.name === 'table' && tableData.value[widget.id]) {
        const data = getFilteredTableData(widget)
        const worksheet = XLSX.utils.json_to_sheet(data)
        const sheetName = widget.label || '数据明细'
        XLSX.utils.book_append_sheet(workbook, worksheet, sheetName)
      }
    }
    
    // 导出图表数据（如果有的话）
    const chartData = []
    for (const widget of report.value.widgets || []) {
      if (widget.name !== 'table') {
        // 这里可以添加图表数据的导出逻辑
        chartData.push({
          图表类型: getWidgetTitle(widget),
          配置: JSON.stringify(widget.config || {})
        })
      }
    }
    
    if (chartData.length > 0) {
      const chartWorksheet = XLSX.utils.json_to_sheet(chartData)
      XLSX.utils.book_append_sheet(workbook, chartWorksheet, '图表配置')
    }
    
    const fileName = `${report.value.name || '报表'}_${new Date().toISOString().slice(0, 10)}.xlsx`
    XLSX.writeFile(workbook, fileName)
    ElMessage.success('Excel导出成功')
  } catch (error) {
    console.error('Excel导出失败:', error)
    ElMessage.error('Excel导出失败')
  }
}

const handleExport = (command) => {
  switch (command) {
    case 'pdf':
      exportToPDF()
      break
    case 'excel':
      exportToExcel()
      break
    case 'image':
      exportImage()
      break
    default:
      ElMessage.warning('未知的导出类型')
  }
}

const refreshData = async () => {
  try {
    refreshing.value = true
    ElMessage.info('正在刷新数据...')
    
    // 重新加载报表数据
    await loadReport(route.params.id)
    
    // 重新渲染图表
    await nextTick()
    debouncedRenderCharts()
    
    ElMessage.success('数据刷新成功')
  } catch (error) {
    console.error('刷新数据失败:', error)
    ElMessage.error('刷新数据失败')
  } finally {
    refreshing.value = false
  }
}

onMounted(async () => {
  if (route.params.id) {
    await loadReport(route.params.id)
  }
})

onUnmounted(() => {
  // 清理定时器
  if (renderTimeout) {
    clearTimeout(renderTimeout)
  }
  
  // 清理所有图表实例
  for (const widget of report.value.widgets || []) {
    if (widget.name !== 'table') {
      const chartDom = document.getElementById(`chart-${widget.id}`)
      if (chartDom) {
        const existingChart = echarts.getInstanceByDom(chartDom)
        if (existingChart) {
          existingChart.dispose()
        }
      }
    }
  }
  })
</script>

<style scoped>
.report-share {
  padding: 20px;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.report-info {
  margin-bottom: 20px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  backdrop-filter: blur(10px);
}

.report-info h2 {
  margin: 0 0 10px 0;
  color: #fff;
  font-size: 24px;
  font-weight: bold;
}

.report-info .description {
  margin: 0;
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
  line-height: 1.5;
}

.report-content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.widget-container {
  transition: transform 0.3s ease;
}

.widget-container:hover {
  transform: translateY(-2px);
}

.chart-container {
  position: relative;
  min-height: 300px;
}

.chart {
  width: 100%;
  height: 400px;
}

.table-container {
  width: 100%;
}

.table-wrapper {
  width: 100%;
}

.table-search {
  margin-bottom: 10px;
}

.table-pagination {
  margin-top: 10px;
  display: flex;
  justify-content: center;
}

.table-info {
  margin-top: 10px;
  text-align: center;
  color: #666;
  font-size: 12px;
}

.table-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #666;
}

.table-loading .el-icon {
  margin-right: 8px;
  font-size: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .report-content {
    grid-template-columns: 1fr;
  }
  
  .card-header {
    flex-direction: column;
    gap: 10px;
  }
  
  .report-info h2 {
    font-size: 20px;
  }
}
</style> 