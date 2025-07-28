<template>
  <div class="report-viewer">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ report.name }}</span>
          <div>
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
            <el-dropdown @command="handleShare">
              <el-button>
                分享
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="link">复制链接</el-dropdown-item>
                  <el-dropdown-item command="qrcode">生成二维码</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button @click="$router.push('/reports')">返回列表</el-button>
          </div>
        </div>
      </template>
      
      <div v-if="report.description" class="report-description">
        <p>{{ report.description }}</p>
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
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { Loading, Search } from '@element-plus/icons-vue'
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
          enablePagination: widget.enablePagination || config.enablePagination || false,
          enableSearch: widget.enableSearch || config.enableSearch || false,
          // 保存原始配置
          config: config
        }
      })
    }
    
    await nextTick()
    
    // 加载表格数据
    for (const widget of report.value.widgets || []) {
      if (widget.name === 'table') {
        await loadTableData(widget)
      }
    }
    
    debouncedRenderCharts()
  } catch (error) {
    console.error('加载报表失败:', error)
    ElMessage.error('加载报表失败')
  }
}

// 防抖函数
let renderTimeout = null
const debouncedRenderCharts = () => {
  if (renderTimeout) {
    clearTimeout(renderTimeout)
  }
  renderTimeout = setTimeout(() => {
    renderCharts()
  }, 200)
}

const renderCharts = async () => {
  for (const widget of report.value.widgets || []) {
    if (widget.name === 'table') {
      // 表格组件的渲染逻辑
      await loadTableData(widget)
    } else {
      const chartDom = document.getElementById(`chart-${widget.id}`)
      if (chartDom) {
        // 检查是否已存在图表实例，如果存在则销毁
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
        chart.setOption(option)
        console.log('渲染图表:', widget.name, '主题:', theme, option)
      } else {
        console.warn('找不到图表容器:', `chart-${widget.id}`)
      }
    }
  }
}

const generateChartOption = async (widget) => {
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
    const response = await axios.post('/api/report/chart-data', {
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
    })
    
    console.log('报表查看器图表数据响应:', widget.name, response.data)
    
    if (response.data.success) {
      if (widget.type === 'line' || widget.name === 'line') {
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
  } catch (error) {
    console.error('获取图表数据出错:', error)
    ElMessage.error('获取图表数据出错')
  }
  
  // 如果获取真实数据失败，返回默认配置
  return getDefaultChartOption(widget)
}

const getDefaultChartOption = (widget) => {
  const baseOption = {
    title: { text: widget.title || widget.label || widget.name },
    tooltip: {},
    legend: { show: widget.showLegend !== false },
    animation: widget.animation !== false,
    color: widget.colors && widget.colors.length > 0 ? widget.colors : ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc']
  }
  
  if (widget.type === 'line' || widget.name === 'line') {
    return {
      ...baseOption,
      xAxis: { type: 'category', data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'] },
      yAxis: { type: 'value' },
      series: [{ 
        type: 'line', 
        data: [820, 932, 901, 934, 1290, 1330, 1320],
        smooth: true,
        label: { show: widget.showLabel !== false }
      }]
    }
  } else if (widget.type === 'bar' || widget.name === 'bar') {
    return {
      ...baseOption,
      xAxis: { type: 'category', data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'] },
      yAxis: { type: 'value' },
      series: [{ 
        type: 'bar', 
        data: [820, 932, 901, 934, 1290, 1330, 1320],
        label: { show: widget.showLabel !== false }
      }]
    }
  } else if (widget.type === 'pie' || widget.name === 'pie') {
    return {
      ...baseOption,
      series: [{ 
        type: 'pie', 
        radius: '50%',
        data: [
          { value: 1048, name: '电子产品' },
          { value: 735, name: '办公用品' },
          { value: 580, name: '生活用品' },
          { value: 484, name: '其他' }
        ],
        label: { show: widget.showLabel !== false }
      }]
    }
  } else if (widget.name === 'scatter') {
    return {
      ...baseOption,
      xAxis: { type: 'value' },
      yAxis: { type: 'value' },
      series: [{ 
        type: 'scatter', 
        data: [[10, 20], [15, 25], [20, 30], [25, 35], [30, 40]],
        label: { show: widget.showLabel !== false }
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
        data: [
          { value: 100, name: '访问' },
          { value: 80, name: '咨询' },
          { value: 60, name: '订单' },
          { value: 40, name: '付款' },
          { value: 20, name: '成交' }
        ]
      }]
    }
  } else if (widget.name === 'radar') {
    return {
      ...baseOption,
      radar: {
        indicator: [
          { name: '销售', max: 100 },
          { name: '管理', max: 100 },
          { name: '技术', max: 100 },
          { name: '客服', max: 100 },
          { name: '研发', max: 100 },
          { name: '市场', max: 100 }
        ]
      },
      series: [{
        type: 'radar',
        data: [
          {
            value: [80, 70, 90, 85, 95, 75],
            name: '能力评估'
          }
        ],
        label: { show: widget.showLabel !== false }
      }]
    }
  } else if (widget.name === 'gauge') {
    return {
      ...baseOption,
      series: [{
        type: 'gauge',
        min: widget.min || 0,
        max: widget.max || 100,
        data: [{ value: 75, name: '完成率' }],
        detail: { show: widget.showLabel !== false }
      }]
    }
  }
  
  return baseOption
}

const loadTableData = async (widget) => {
  try {
    console.log('加载表格数据，widget配置:', widget)
    const response = await axios.post('/api/report/chart-data', {
      uri: report.value.dataSourceUri,
      collection: report.value.collection,
      filters: report.value.filters || [],
      widget: {
        name: 'table',
        sortBy: widget.sortBy || widget.config?.sortBy,
        sortOrder: widget.sortOrder || widget.config?.sortOrder,
        limit: widget.limit || widget.pageSize || widget.config?.pageSize || 50,
        displayFields: widget.displayFields || widget.config?.displayFields
      }
    })
    
    if (response.data.success) {
      const data = response.data.data
      const columns = data.length > 0 ? Object.keys(data[0]) : []
      
      tableData.value[widget.id] = {
        data: data,
        columns: columns,
        total: response.data.total
      }
      
      console.log('表格数据加载成功:', {
        widgetId: widget.id,
        dataLength: data.length,
        columns: columns,
        tableData: tableData.value[widget.id]
      })
    } else {
      console.warn('获取表格数据失败:', response.data.message)
      ElMessage.warning(`表格数据获取失败: ${response.data.message}`)
      tableData.value[widget.id] = { data: [], columns: [], total: 0 }
    }
  } catch (error) {
    console.error('获取表格数据出错:', error)
    ElMessage.error('获取表格数据出错')
    tableData.value[widget.id] = { data: [], columns: [], total: 0 }
  }
}

const getTableColumns = (widget) => {
  if (!tableData.value[widget.id]) {
    console.log('表格数据不存在，无法获取列:', widget.id)
    return []
  }
  
  const displayFields = widget.displayFields || []
  if (!displayFields || displayFields.length === 0) {
    const columns = tableData.value[widget.id].columns || []
    console.log('使用所有列:', columns)
    return columns
  }
  
  const filteredColumns = displayFields.filter(field => 
    tableData.value[widget.id].columns.includes(field)
  )
  console.log('使用过滤后的列:', filteredColumns)
  return filteredColumns
}

// 表格分页和搜索相关方法
const getFilteredTableData = (widget) => {
  if (!tableData.value[widget.id]) {
    console.log('表格数据不存在:', widget.id)
    return []
  }
  
  let data = [...tableData.value[widget.id].data]
  console.log('获取过滤后的表格数据:', {
    widgetId: widget.id,
    originalDataLength: data.length,
    enableSearch: widget.enableSearch,
    enablePagination: widget.enablePagination
  })
  
  // 搜索过滤
  const searchText = tableSearchText.value[widget.id] || ''
  if (searchText && widget.enableSearch) {
    data = data.filter(item => {
      return Object.values(item).some(value => 
        String(value).toLowerCase().includes(searchText.toLowerCase())
      )
    })
  }
  
  // 分页
  if (widget.enablePagination) {
    const pageSize = tablePageSize.value[widget.id] || widget.pageSize || 10
    const currentPage = tableCurrentPage.value[widget.id] || 1
    const start = (currentPage - 1) * pageSize
    const end = start + pageSize
    data = data.slice(start, end)
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

const exportImage = () => {
  ElMessage.info('导出图片功能开发中...')
}

const exportToPDF = async () => {
  try {
    ElMessage.info('正在生成PDF...')
    
    const reportContainer = document.querySelector('.report-viewer')
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

const shareReport = () => {
  const shareUrl = `${window.location.origin}/share/${route.params.id}`
  
  // 复制到剪贴板
  navigator.clipboard.writeText(shareUrl).then(() => {
    ElMessage.success('分享链接已复制到剪贴板')
  }).catch(() => {
    // 如果剪贴板API不可用，显示链接
    prompt('分享链接：', shareUrl)
  })
}

const generateQRCode = () => {
  const shareUrl = `${window.location.origin}/report/${route.params.id}`
  
  // 这里可以集成二维码生成库
  ElMessage.info('二维码功能开发中...')
  console.log('分享链接:', shareUrl)
}

const handleShare = (command) => {
  switch (command) {
    case 'link':
      shareReport()
      break
    case 'qrcode':
      generateQRCode()
      break
    default:
      ElMessage.warning('未知的分享类型')
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
.report-viewer {
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header span {
  font-size: 18px;
  font-weight: bold;
  color: #2c3e50;
}

.report-description {
  margin-bottom: 20px;
  padding: 15px;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  border-radius: 8px;
  border-left: 4px solid #409eff;
  color: white;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.report-content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(450px, 1fr));
  gap: 25px;
  margin-top: 20px;
}

.widget-container {
  min-height: 350px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.widget-container:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.chart-container {
  height: 450px;
  position: relative;
  border-radius: 8px;
  overflow: hidden;
}

.chart {
  width: 100%;
  height: 100%;
  border-radius: 8px;
}

.table-container {
  height: 100%;
}

.table-wrapper {
  position: relative;
  width: 100%;
  background-color: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  transition: box-shadow 0.3s ease;
}

.table-wrapper:hover {
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

.table-search {
  padding: 15px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px 8px 0 0;
}

.table-pagination {
  padding: 15px;
  background-color: #f8f9fa;
  border-top: 1px solid #ebeef5;
}

.table-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  border-radius: 8px;
  color: white;
}

.table-loading .is-loading {
  margin-bottom: 10px;
  font-size: 24px;
}

.table-info {
  margin-top: 15px;
  text-align: center;
  color: #606266;
  font-size: 13px;
  padding: 10px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

/* 按钮样式优化 */
.el-button {
  border-radius: 6px;
  transition: all 0.3s ease;
}

.el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* 卡片样式优化 */
:deep(.el-card) {
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

:deep(.el-card:hover) {
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

:deep(.el-card__header) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 12px 12px 0 0;
  padding: 15px 20px;
}

:deep(.el-card__body) {
  padding: 20px;
}
</style> 