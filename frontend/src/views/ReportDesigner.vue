<template>
  <div class="report-designer">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>报表设计器</span>
          <div>
            <el-button @click="saveReport" type="primary">保存报表</el-button>
            <el-button @click="$router.push('/reports')">返回列表</el-button>
          </div>
        </div>
      </template>
      
      <el-form :model="reportForm" label-width="100px" style="margin-bottom: 20px;">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="报表名称">
              <el-input v-model="reportForm.name" placeholder="请输入报表名称" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="数据源">
              <el-select v-model="reportForm.dataSourceUri" placeholder="选择数据源" @change="onDataSourceChange">
                <el-option v-for="ds in dataSources" :key="ds.uri" :label="ds.name" :value="ds.uri" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="集合">
              <el-select v-model="reportForm.collection" placeholder="选择集合" @change="onCollectionChange">
                <el-option v-for="col in collections" :key="col" :label="col" :value="col" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="描述">
          <el-input v-model="reportForm.description" type="textarea" placeholder="请输入报表描述" />
        </el-form-item>
        
        <!-- 数据过滤条件 -->
        <el-form-item label="数据过滤">
          <div class="filter-container">
            <div v-for="(filter, index) in reportForm.filters" :key="index" class="filter-item">
              <el-row :gutter="10">
                <el-col :span="6">
                  <el-select v-model="filter.field" placeholder="选择字段" @change="onFilterFieldChange(index)">
                    <el-option v-for="field in fields" :key="field" :label="field" :value="field" />
                  </el-select>
                </el-col>
                <el-col :span="4">
                  <el-select v-model="filter.operator" placeholder="操作符" @change="onFilterOperatorChange">
                    <el-option label="等于" value="eq" />
                    <el-option label="不等于" value="ne" />
                    <el-option label="大于" value="gt" />
                    <el-option label="大于等于" value="gte" />
                    <el-option label="小于" value="lt" />
                    <el-option label="小于等于" value="lte" />
                    <el-option label="包含" value="in" />
                    <el-option label="不包含" value="nin" />
                    <el-option label="正则" value="regex" />
                  </el-select>
                </el-col>
                <el-col :span="6">
                  <el-input v-model="filter.value" placeholder="输入值" @blur="onFilterValueChange" />
                </el-col>
                <el-col :span="4">
                  <el-select v-model="filter.logic" placeholder="逻辑" @change="onFilterLogicChange">
                    <el-option label="且" value="and" />
                    <el-option label="或" value="or" />
                  </el-select>
                </el-col>
                <el-col :span="4">
                  <el-button type="danger" size="small" @click="removeFilter(index)">删除</el-button>
                </el-col>
              </el-row>
            </div>
            <el-button type="primary" size="small" @click="addFilter">添加过滤条件</el-button>
          </div>
        </el-form-item>
      </el-form>
      
      <el-row :gutter="20">
        <!-- 左侧面板：组件库和字段列表 -->
        <el-col :span="6">
          <el-card style="margin-bottom: 20px;">
            <template #header>
              <span>组件库</span>
            </template>
            <draggable v-model="widgets" group="widgets" item-key="name" :clone="cloneWidget" :sort="false">
              <template #item="{ element }">
                <div class="widget-item">
                  <el-card shadow="hover">{{ element.label }}</el-card>
                </div>
              </template>
            </draggable>
          </el-card>
          
          <!-- 字段列表 -->
          <el-card v-if="fields.length > 0">
            <template #header>
              <span>字段列表</span>
            </template>
            <div class="fields-container">
              <div 
                v-for="field in fields" 
                :key="field" 
                class="field-item"
                draggable="true"
                @dragstart="onFieldDragStart(field)"
                @click="previewFieldData(field)"
              >
                <el-icon><Document /></el-icon>
                <span>{{ field }}</span>
                <el-tag size="small" type="info">{{ getFieldType(field) }}</el-tag>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <!-- 中间面板：设计画布 -->
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>设计画布</span>
            </template>
            <draggable 
              v-model="canvas" 
              group="widgets" 
              item-key="id" 
              class="designer-canvas" 
              :sort="true"
              @drop="onCanvasDrop"
            >
              <template #item="{ element }">
                <div class="canvas-item">
                  <el-card shadow="always">
                    <template #header>
                      <div style="display: flex; justify-content: space-between; align-items: center;">
                        <span>{{ element.label || element.name }}</span>
                        <div>
                          <el-button size="small" @click="configureWidget(element)">配置</el-button>
                          <el-button size="small" type="danger" @click="removeWidget(element)">删除</el-button>
                        </div>
                      </div>
                    </template>
                    <div v-if="element.name === 'table'" class="table-placeholder">
                      <div v-if="tableData[element.id]" class="table-container">
                        <!-- 搜索框 -->
                        <div v-if="element.config?.enableSearch" class="table-search" style="margin-bottom: 10px;">
                          <el-input
                            v-model="tableSearchText[element.id]"
                            placeholder="搜索..."
                            style="width: 200px;"
                            @input="onTableSearch(element)"
                            clearable
                          >
                            <template #prefix>
                              <el-icon><Search /></el-icon>
                            </template>
                          </el-input>
                        </div>
                        
                        <!-- 表格 -->
                        <el-table 
                          :data="getFilteredTableData(element)" 
                          size="small" 
                          height="200" 
                          style="width: 100%"
                        >
                          <el-table-column 
                            v-for="column in getTableColumns(element)" 
                            :key="column" 
                            :prop="column" 
                            :label="column" 
                            show-overflow-tooltip
                          />
                        </el-table>
                        
                        <!-- 分页 -->
                        <div v-if="element.config?.enablePagination" class="table-pagination" style="margin-top: 10px;">
                          <el-pagination
                            v-model:current-page="tableCurrentPage[element.id]"
                            v-model:page-size="tablePageSize[element.id]"
                            :page-sizes="[10, 20, 50, 100]"
                            :total="getFilteredTableData(element).length"
                            layout="total, sizes, prev, pager, next, jumper"
                            @size-change="onTablePageSizeChange(element)"
                            @current-change="onTableCurrentPageChange(element)"
                          />
                        </div>
                        
                        <div class="table-info">
                          显示 {{ getFilteredTableData(element).length }} 条数据，共 {{ tableData[element.id].total }} 条
                        </div>
                      </div>
                      <div v-else class="table-loading">
                        <el-icon class="is-loading"><Loading /></el-icon>
                        <span>加载表格数据...</span>
                      </div>
                    </div>
                    <div v-else :id="'designer-chart-' + element.id" class="chart-container"></div>
                  </el-card>
                </div>
              </template>
            </draggable>
          </el-card>
        </el-col>
        
        <!-- 右侧面板：数据预览 -->
        <el-col :span="6">
          <el-card>
            <template #header>
              <span>数据预览</span>
              <el-button size="small" @click="refreshPreview" style="margin-left: 10px;">刷新</el-button>
            </template>
            <div v-if="previewData.length > 0" class="preview-container">
              <el-table :data="previewData.slice(0, 10)" size="small" height="400">
                <el-table-column 
                  v-for="field in fields" 
                  :key="field" 
                  :prop="field" 
                  :label="field" 
                  show-overflow-tooltip
                />
              </el-table>
              <div class="preview-info">
                显示前10条数据，共 {{ previewData.length }} 条
              </div>
            </div>
            <div v-else class="preview-empty">
              <el-empty description="暂无数据预览" />
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
    
    <!-- 组件配置对话框 -->
    <el-dialog v-model="configDialogVisible" :title="`配置${currentWidget?.label || '组件'}`" width="800px">
      <el-form :model="widgetConfig" label-width="120px">
        <el-form-item label="组件标题">
          <el-input v-model="widgetConfig.title" placeholder="请输入组件标题" />
        </el-form-item>
        
        <!-- 字段配置 -->
        <el-form-item label="字段配置">
          <el-tabs v-model="activeTab" type="card">
            <el-tab-pane label="基础配置" name="basic">
              <template v-if="currentWidget?.name === 'line' || currentWidget?.name === 'bar'">
                <el-form-item label="X轴字段">
                  <el-select v-model="widgetConfig.xField" placeholder="选择X轴字段">
                    <el-option v-for="field in fields" :key="field" :label="field" :value="field" />
                  </el-select>
                </el-form-item>
                <el-form-item label="Y轴字段">
                  <el-select v-model="widgetConfig.yField" placeholder="选择Y轴字段">
                    <el-option v-for="field in fields" :key="field" :label="field" :value="field" />
                  </el-select>
                </el-form-item>
                <el-form-item label="系列字段">
                  <el-select v-model="widgetConfig.seriesField" placeholder="选择系列字段（可选）">
                    <el-option label="无" value="" />
                    <el-option v-for="field in fields" :key="field" :label="field" :value="field" />
                  </el-select>
                </el-form-item>
              </template>
              
              <template v-if="currentWidget?.name === 'pie'">
                <el-form-item label="名称字段">
                  <el-select v-model="widgetConfig.nameField" placeholder="选择名称字段">
                    <el-option v-for="field in fields" :key="field" :label="field" :value="field" />
                  </el-select>
                </el-form-item>
                <el-form-item label="数值字段">
                  <el-select v-model="widgetConfig.valueField" placeholder="选择数值字段">
                    <el-option v-for="field in fields" :key="field" :label="field" :value="field" />
                  </el-select>
                </el-form-item>
              </template>
              
              <template v-if="currentWidget?.name === 'scatter'">
                <el-form-item label="X轴字段">
                  <el-select v-model="widgetConfig.xField" placeholder="选择X轴字段">
                    <el-option v-for="field in fields" :key="field" :label="field" :value="field" />
                  </el-select>
                </el-form-item>
                <el-form-item label="Y轴字段">
                  <el-select v-model="widgetConfig.yField" placeholder="选择Y轴字段">
                    <el-option v-for="field in fields" :key="field" :label="field" :value="field" />
                  </el-select>
                </el-form-item>
                <el-form-item label="大小字段">
                  <el-select v-model="widgetConfig.sizeField" placeholder="选择大小字段（可选）">
                    <el-option label="无" value="" />
                    <el-option v-for="field in fields" :key="field" :label="field" :value="field" />
                  </el-select>
                </el-form-item>
              </template>
              
              <template v-if="currentWidget?.name === 'gauge'">
                <el-form-item label="数值字段">
                  <el-select v-model="widgetConfig.valueField" placeholder="选择数值字段">
                    <el-option v-for="field in fields" :key="field" :label="field" :value="field" />
                  </el-select>
                </el-form-item>
                <el-form-item label="最小值">
                  <el-input-number v-model="widgetConfig.min" :min="0" />
                </el-form-item>
                <el-form-item label="最大值">
                  <el-input-number v-model="widgetConfig.max" :min="0" />
                </el-form-item>
              </template>
              
              <template v-if="currentWidget?.name === 'table'">
                <el-form-item label="显示字段">
                  <el-select v-model="widgetConfig.displayFields" multiple placeholder="选择要显示的字段">
                    <el-option v-for="field in fields" :key="field" :label="field" :value="field" />
                  </el-select>
                </el-form-item>
                <el-form-item label="每页显示数量">
                  <el-input-number v-model="widgetConfig.pageSize" :min="10" :max="100" />
                </el-form-item>
                <el-form-item label="启用分页">
                  <el-switch v-model="widgetConfig.enablePagination" />
                </el-form-item>
                <el-form-item label="启用搜索">
                  <el-switch v-model="widgetConfig.enableSearch" />
                </el-form-item>
              </template>
            </el-tab-pane>
            
            <el-tab-pane label="统计配置" name="stats">
              <el-form-item label="启用统计">
                <el-switch v-model="widgetConfig.enableStats" />
              </el-form-item>
              
              <template v-if="widgetConfig.enableStats">
                <el-form-item label="统计函数">
                  <el-select v-model="widgetConfig.aggregation" placeholder="选择统计函数">
                    <el-option label="求和" value="sum" />
                    <el-option label="平均值" value="avg" />
                    <el-option label="计数" value="count" />
                    <el-option label="最大值" value="max" />
                    <el-option label="最小值" value="min" />
                    <el-option label="标准差" value="std" />
                    <el-option label="方差" value="variance" />
                  </el-select>
                </el-form-item>
                
                <el-form-item label="分组字段">
                  <el-select v-model="widgetConfig.groupBy" placeholder="选择分组字段">
                    <el-option label="无" value="" />
                    <el-option v-for="field in fields" :key="field" :label="field" :value="field" />
                  </el-select>
                </el-form-item>
                
                <el-form-item label="排序">
                  <el-select v-model="widgetConfig.sortBy" placeholder="选择排序字段">
                    <el-option label="无" value="" />
                    <el-option v-for="field in fields" :key="field" :label="field" :value="field" />
                  </el-select>
                  <el-select v-model="widgetConfig.sortOrder" style="margin-left: 10px;">
                    <el-option label="升序" value="asc" />
                    <el-option label="降序" value="desc" />
                  </el-select>
                </el-form-item>
                
                <el-form-item label="限制数量">
                  <el-input-number v-model="widgetConfig.limit" :min="1" :max="1000" />
                </el-form-item>
              </template>
            </el-tab-pane>
            
            <el-tab-pane label="样式配置" name="style">
              <el-form-item label="图表主题">
                <el-select v-model="widgetConfig.theme" placeholder="选择主题">
                  <el-option label="默认" value="default" />
                  <el-option label="暗色" value="dark" />
                  <el-option label="浅色" value="light" />
                </el-select>
              </el-form-item>
              
              <el-form-item label="显示图例">
                <el-switch v-model="widgetConfig.showLegend" />
              </el-form-item>
              
              <el-form-item label="显示标签">
                <el-switch v-model="widgetConfig.showLabel" />
              </el-form-item>
              
              <el-form-item label="动画效果">
                <el-switch v-model="widgetConfig.animation" />
              </el-form-item>
              
              <el-form-item label="颜色配置">
                <div class="color-picker-container">
                  <el-button 
                    v-for="(color, index) in widgetConfig.colors" 
                    :key="index"
                    size="small"
                    @click="removeColor(index)"
                    style="margin-right: 8px; margin-bottom: 8px;"
                  >
                    <div 
                      :style="{ 
                        width: '20px', 
                        height: '20px', 
                        backgroundColor: color, 
                        borderRadius: '2px',
                        marginRight: '8px'
                      }"
                    ></div>
                    {{ color }}
                    <el-icon style="margin-left: 4px;"><Close /></el-icon>
                  </el-button>
                  <el-color-picker 
                    v-model="newColor" 
                    show-alpha 
                    @change="addColor"
                    style="margin-bottom: 8px;"
                  />
                  <div style="margin-top: 8px;">
                    <el-button size="small" @click="addDefaultColors">添加默认颜色</el-button>
                    <el-button size="small" @click="clearColors">清空颜色</el-button>
                  </div>
                </div>
              </el-form-item>
            </el-tab-pane>
          </el-tabs>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="configDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveWidgetConfig">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import draggable from 'vuedraggable'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { Document, Loading, Close, Search } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const reportForm = ref({
  name: '',
  description: '',
  dataSourceUri: '',
  collection: '',
  widgets: [],
  fieldMapping: {},
  filters: []
})

const dataSources = ref([])
const collections = ref([])
const fields = ref([])
const previewData = ref([])
const fieldMapping = ref({})

const widgets = ref([
  { name: 'line', label: '折线图' },
  { name: 'bar', label: '柱状图' },
  { name: 'pie', label: '饼图' },
  { name: 'table', label: '表格' },
  { name: 'gauge', label: '仪表盘' },
  { name: 'funnel', label: '漏斗图' },
  { name: 'scatter', label: '散点图' },
  { name: 'radar', label: '雷达图' }
])

const canvas = ref([])
const tableData = ref({})
// 表格分页和搜索相关数据
const tableCurrentPage = ref({})
const tablePageSize = ref({})
const tableSearchText = ref({})

const configDialogVisible = ref(false)
const currentWidget = ref(null)
const widgetConfig = ref({})
const activeTab = ref('basic')
const newColor = ref('')

const onDataSourceChange = async () => {
  try {
    const response = await axios.get(`/api/report/collections?uri=${reportForm.value.dataSourceUri}`)
    collections.value = response.data
    reportForm.value.collection = ''
    fields.value = []
    previewData.value = []
  } catch (error) {
    console.error('获取集合列表失败:', error)
  }
}

const onCollectionChange = async () => {
  try {
    const response = await axios.get(`/api/report/fields?uri=${reportForm.value.dataSourceUri}&collection=${reportForm.value.collection}`)
    fields.value = response.data
    await loadPreviewData()
  } catch (error) {
    console.error('获取字段列表失败:', error)
  }
}

const loadPreviewData = async () => {
  if (!reportForm.value.dataSourceUri || !reportForm.value.collection) return
  
  try {
    const response = await axios.get(`/api/report/data?uri=${reportForm.value.dataSourceUri}&collection=${reportForm.value.collection}&limit=100`)
    previewData.value = response.data
  } catch (error) {
    console.error('获取预览数据失败:', error)
  }
}

const refreshPreview = () => {
  loadPreviewData()
}

const getFieldType = (field) => {
  if (previewData.value.length === 0) return 'unknown'
  
  const sampleValue = previewData.value[0][field]
  if (typeof sampleValue === 'number') return 'number'
  if (typeof sampleValue === 'boolean') return 'boolean'
  if (sampleValue instanceof Date) return 'date'
  return 'string'
}

const previewFieldData = (field) => {
  if (previewData.value.length === 0) return
  
  const values = previewData.value.slice(0, 5).map(item => item[field])
  const uniqueValues = [...new Set(values)]
  ElMessage.info(`${field}: ${uniqueValues.join(', ')}${uniqueValues.length < values.length ? '...' : ''}`)
}

const onFieldDragStart = (field) => {
  event.dataTransfer.setData('text/plain', field)
  event.dataTransfer.effectAllowed = 'copy'
}

const onCanvasDrop = (event) => {
  const droppedField = event.dataTransfer.getData('text/plain')
  if (droppedField && fields.value.includes(droppedField)) {
    // 可以在这里处理字段拖拽到画布的逻辑
    console.log('字段拖拽到画布:', droppedField)
  }
}

// 过滤功能
const addFilter = () => {
  reportForm.value.filters.push({
    field: '',
    operator: 'eq',
    value: '',
    logic: 'and'
  })
  
  // 触发图表重新渲染
  debouncedRenderCharts()
}

const removeFilter = (index) => {
  reportForm.value.filters.splice(index, 1)
  
  // 触发图表重新渲染
  debouncedRenderCharts()
}

const onFilterFieldChange = (index) => {
  // 根据字段类型调整操作符选项
  const filter = reportForm.value.filters[index]
  const fieldType = getFieldType(filter.field)
  
  // 重置操作符
  if (fieldType === 'number') {
    filter.operator = 'eq'
  } else if (fieldType === 'string') {
    filter.operator = 'eq'
  } else if (fieldType === 'date') {
    filter.operator = 'eq'
  }
  
  // 触发图表重新渲染
  debouncedRenderCharts()
}

const onFilterOperatorChange = () => {
  // 触发图表重新渲染
  debouncedRenderCharts()
}

const onFilterValueChange = () => {
  // 延迟触发图表重新渲染，避免频繁更新
  setTimeout(() => {
    debouncedRenderCharts()
  }, 500)
}

const onFilterLogicChange = () => {
  // 延迟触发图表重新渲染
  setTimeout(() => {
    debouncedRenderCharts()
  }, 300)
}

// 统计功能
const getAggregationOptions = (fieldType) => {
  const options = ['count']
  
  if (fieldType === 'number') {
    options.push('sum', 'avg', 'max', 'min', 'std', 'variance')
  }
  
  return options
}

const calculateStats = (data, field, aggregation) => {
  if (!data || data.length === 0) return 0
  
  const values = data.map(item => item[field]).filter(val => val !== null && val !== undefined)
  
  switch (aggregation) {
    case 'sum':
      return values.reduce((sum, val) => sum + Number(val), 0)
    case 'avg':
      return values.reduce((sum, val) => sum + Number(val), 0) / values.length
    case 'count':
      return values.length
    case 'max':
      return Math.max(...values.map(val => Number(val)))
    case 'min':
      return Math.min(...values.map(val => Number(val)))
    case 'std':
      const avg = values.reduce((sum, val) => sum + Number(val), 0) / values.length
      const variance = values.reduce((sum, val) => sum + Math.pow(Number(val) - avg, 2), 0) / values.length
      return Math.sqrt(variance)
    case 'variance':
      const mean = values.reduce((sum, val) => sum + Number(val), 0) / values.length
      return values.reduce((sum, val) => sum + Math.pow(Number(val) - mean, 2), 0) / values.length
    default:
      return 0
  }
}

const cloneWidget = (widget) => {
  return {
    id: Date.now() + Math.random(),
    name: widget.name,
    label: widget.label,
    config: {}
  }
}

const removeWidget = (widget) => {
  const index = canvas.value.findIndex(item => item.id === widget.id)
  if (index > -1) {
    canvas.value.splice(index, 1)
    ElMessage.success('组件已删除')
  }
}

// 防抖函数
let renderTimeout = null
const debouncedRenderCharts = () => {
  if (renderTimeout) {
    clearTimeout(renderTimeout)
  }
  renderTimeout = setTimeout(() => {
    renderDesignerCharts()
  }, 200)
}

const renderDesignerCharts = async () => {
  // 检查组件是否还存在
  if (!canvas.value) {
    console.log('画布已卸载，跳过图表渲染')
    return
  }
  
  for (const element of canvas.value) {
    // 再次检查当前元素是否还存在
    if (!canvas.value.find(item => item.id === element.id)) {
      console.log('元素已卸载，跳过渲染:', element.id)
      continue
    }
    
    if (element.name === 'table') {
      // 获取表格数据
      await loadTableData(element)
    } else {
      const chartDom = document.getElementById(`designer-chart-${element.id}`)
      if (chartDom) {
        // 检查是否已存在图表实例，如果存在则销毁
        const existingChart = echarts.getInstanceByDom(chartDom)
        if (existingChart) {
          existingChart.dispose()
        }
        
        const chart = echarts.init(chartDom)
        const option = await getChartOption(element)
        if (option) {
          console.log('设置图表配置:', element.name, option)
          chart.setOption(option)
          console.log('图表配置已应用:', element.name)
        } else {
          console.warn('图表配置为空:', element.name)
        }
      }
    }
  }
}

const loadTableData = async (element) => {
  if (!element.config || !reportForm.value.dataSourceUri || !reportForm.value.collection) {
    return
  }
  
  try {
    const requestData = {
      uri: reportForm.value.dataSourceUri,
      collection: reportForm.value.collection,
      filters: reportForm.value.filters || [],
      widget: {
        name: 'table',
        sortBy: element.config.sortBy,
        sortOrder: element.config.sortOrder,
        limit: element.config.limit || element.config.pageSize || 50,
        displayFields: element.config.displayFields
      }
    }
    
    console.log('表格数据请求:', requestData)
    const response = await axios.post('/api/report/chart-data', requestData)
    console.log('表格数据响应:', response.data)
    
    // 检查组件是否还存在
    if (!canvas.value || !canvas.value.find(item => item.id === element.id)) {
      console.log('组件已卸载，跳过数据更新')
      return
    }
    
    if (response.data.success) {
      const data = response.data.data
      const columns = data.length > 0 ? Object.keys(data[0]) : []
      
      tableData.value[element.id] = {
        data: data,
        columns: columns,
        total: response.data.total
      }
    } else {
      console.error('表格数据获取失败:', response.data.message)
      ElMessage.error('表格数据获取失败: ' + response.data.message)
    }
  } catch (error) {
    // 检查组件是否还存在
    if (!canvas.value || !canvas.value.find(item => item.id === element.id)) {
      console.log('组件已卸载，跳过错误处理')
      return
    }
    console.error('获取表格数据失败:', error)
    ElMessage.error('获取表格数据失败')
  }
}

const getChartOption = async (element) => {
  // 应用样式配置
  const baseOption = {
    title: { text: element.config?.title || element.label || element.name },
    tooltip: {},
    legend: { show: element.config?.showLegend !== false },
    animation: element.config?.animation !== false,
    color: element.config?.colors && element.config.colors.length > 0 ? element.config.colors : ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc']
  }
  
  // 如果有配置，尝试获取真实数据
  if (element.config && reportForm.value.dataSourceUri && reportForm.value.collection) {
    try {
      // 构建请求参数，包含过滤条件和统计配置
      const requestData = {
        uri: reportForm.value.dataSourceUri,
        collection: reportForm.value.collection,
        filters: reportForm.value.filters || [],
        widget: {
          name: element.name,
          xField: element.config.xField,
          yField: element.config.yField,
          seriesField: element.config.seriesField,
          nameField: element.config.nameField,
          valueField: element.config.valueField,
          sizeField: element.config.sizeField,
          min: element.config.min,
          max: element.config.max,
          // 统计配置
          enableStats: element.config.enableStats,
          aggregation: element.config.aggregation,
          groupBy: element.config.groupBy,
          sortBy: element.config.sortBy,
          sortOrder: element.config.sortOrder,
          limit: element.config.limit,
          // 样式配置
          theme: element.config.theme,
          showLegend: element.config.showLegend,
          showLabel: element.config.showLabel,
          animation: element.config.animation,
          colors: element.config.colors
        }
      }
      
      const response = await axios.post('/api/report/chart-data', requestData)
      
      console.log('图表数据响应:', element.name, response.data)
      
      // 检查组件是否还存在
      if (!canvas.value || !canvas.value.find(item => item.id === element.id)) {
        console.log('组件已卸载，跳过图表配置更新')
        return null
      }
      
      if (response.data.success) {
        if (element.name === 'line') {
          const option = {
            ...baseOption,
            xAxis: { type: 'category', data: response.data.xAxis },
            yAxis: { type: 'value' },
            series: response.data.series.map(series => ({
              ...series,
              label: { show: element.config?.showLabel !== false },
              emphasis: {
                focus: 'series'
              }
            }))
          }
          console.log('折线图配置:', option)
          return option
        } else if (element.name === 'bar') {
          const option = {
            ...baseOption,
            xAxis: { type: 'category', data: response.data.xAxis },
            yAxis: { type: 'value' },
            series: response.data.series.map(series => ({
              ...series,
              label: { show: element.config?.showLabel !== false }
            }))
          }
          console.log('柱状图配置:', option)
          return option
        } else if (element.name === 'pie') {
          const option = {
            ...baseOption,
            series: [{ 
              type: 'pie', 
              radius: '50%',
              data: response.data.series,
              label: { 
                show: element.config?.showLabel !== false,
                formatter: '{b}: {c} ({d}%)'
              },
              emphasis: {
                itemStyle: {
                  shadowBlur: 10,
                  shadowOffsetX: 0,
                  shadowColor: 'rgba(0, 0, 0, 0.5)'
                }
              }
            }]
          }
          console.log('饼图配置:', option)
          return option
        } else if (element.name === 'scatter') {
          const option = {
            ...baseOption,
            xAxis: { type: 'value' },
            yAxis: { type: 'value' },
            series: response.data.series.map(series => ({
              ...series,
              label: { show: element.config?.showLabel !== false }
            }))
          }
          console.log('散点图配置:', option)
          return option
        } else if (element.name === 'gauge') {
          const option = {
            ...baseOption,
            series: [{
              type: 'gauge',
              min: response.data.min || element.config?.min || 0,
              max: response.data.max || element.config?.max || 100,
              data: response.data.series,
              detail: { show: element.config?.showLabel !== false }
            }]
          }
          console.log('仪表盘配置:', option)
          return option
        } else if (element.name === 'table') {
          // 表格组件不需要返回ECharts配置
          return null
        }
      } else {
        console.error('后端返回失败:', response.data.message)
      }
    } catch (error) {
      // 检查组件是否还存在
      if (!canvas.value || !canvas.value.find(item => item.id === element.id)) {
        console.log('组件已卸载，跳过错误处理')
        return null
      }
      console.error('获取图表数据失败:', error)
      console.error('请求数据:', requestData)
    }
  }
  
  // 如果获取真实数据失败，返回默认配置
  return getDefaultChartOption(element)
}

const getDefaultChartOption = (element) => {
  const baseOption = {
    title: { text: element.config?.title || element.label || element.name },
    tooltip: {},
    legend: { show: element.config?.showLegend !== false },
    animation: element.config?.animation !== false,
    color: element.config?.colors && element.config.colors.length > 0 ? element.config.colors : ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc']
  }
  
  if (element.name === 'line') {
    return {
      ...baseOption,
      xAxis: { type: 'category', data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'] },
      yAxis: { type: 'value' },
      series: [{ 
        type: 'line', 
        data: [820, 932, 901, 934, 1290, 1330, 1320],
        smooth: true,
        label: { show: element.config?.showLabel !== false }
      }]
    }
  } else if (element.name === 'bar') {
    return {
      ...baseOption,
      xAxis: { type: 'category', data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'] },
      yAxis: { type: 'value' },
      series: [{ 
        type: 'bar', 
        data: [820, 932, 901, 934, 1290, 1330, 1320],
        label: { show: element.config?.showLabel !== false }
      }]
    }
  } else if (element.name === 'pie') {
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
        label: { show: element.config?.showLabel !== false }
      }]
    }
  } else if (element.name === 'scatter') {
    return {
      ...baseOption,
      xAxis: { type: 'value' },
      yAxis: { type: 'value' },
      series: [{ 
        type: 'scatter', 
        data: [[10, 20], [15, 25], [20, 30], [25, 35], [30, 40]],
        label: { show: element.config?.showLabel !== false }
      }]
    }
  } else if (element.name === 'gauge') {
    return {
      ...baseOption,
      series: [{
        type: 'gauge',
        min: element.config?.min || 0,
        max: element.config?.max || 100,
        data: [{ value: 75, name: '完成率' }],
        detail: { show: element.config?.showLabel !== false }
      }]
    }
  }
  
  return baseOption
}

const saveReport = async () => {
  try {
    // 确保每个widget都包含配置信息
    const widgetsWithConfig = canvas.value.map(widget => ({
      ...widget,
      // 将config中的字段直接合并到widget对象中，这样后端可以访问到
      xField: widget.config?.xField,
      yField: widget.config?.yField,
      seriesField: widget.config?.seriesField,
      nameField: widget.config?.nameField,
      valueField: widget.config?.valueField,
      sizeField: widget.config?.sizeField,
      title: widget.config?.title,
      min: widget.config?.min,
      max: widget.config?.max,
      // 新增的统计配置
      enableStats: widget.config?.enableStats,
      aggregation: widget.config?.aggregation,
      groupBy: widget.config?.groupBy,
      sortBy: widget.config?.sortBy,
      sortOrder: widget.config?.sortOrder,
      limit: widget.config?.limit,
      // 样式配置
      theme: widget.config?.theme,
      showLegend: widget.config?.showLegend,
      showLabel: widget.config?.showLabel,
      animation: widget.config?.animation,
      colors: widget.config?.colors,
      // 表格配置
      displayFields: widget.config?.displayFields,
      pageSize: widget.config?.pageSize,
      enablePagination: widget.config?.enablePagination,
      enableSearch: widget.config?.enableSearch
    }))
    
    const reportData = {
      ...reportForm.value,
      widgets: widgetsWithConfig,
      filters: reportForm.value.filters || [] // 确保过滤条件被保存
    }
    
    console.log('保存报表时的过滤条件:', reportForm.value.filters)
    
    if (route.params.id) {
      await axios.put(`/api/report/configs/${route.params.id}`, reportData)
      ElMessage.success('报表更新成功')
    } else {
      await axios.post('/api/report/configs', reportData)
      ElMessage.success('报表保存成功')
    }
    
    router.push('/reports')
  } catch (error) {
    console.error('保存报表失败:', error)
    ElMessage.error('保存报表失败')
  }
}

const loadReport = async (id) => {
  try {
    const response = await axios.get(`/api/report/configs/${id}`)
    const report = response.data
    console.log('加载报表时的原始数据:', report)
    reportForm.value = {
      name: report.name,
      description: report.description,
      dataSourceUri: report.dataSourceUri,
      collection: report.collection,
      widgets: report.widgets || [],
      fieldMapping: report.fieldMapping || {},
      filters: report.filters || []
    }
    console.log('加载报表后的过滤条件:', reportForm.value.filters)
    
    // 将widget配置转换为内部格式
    canvas.value = (report.widgets || []).map(widget => ({
      id: widget.id,
      name: widget.name,
      label: widget.label,
      config: {
        title: widget.title,
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
        colors: widget.colors,
        // 表格配置
        displayFields: widget.displayFields,
        pageSize: widget.pageSize,
        enablePagination: widget.enablePagination,
        enableSearch: widget.enableSearch
      }
    }))
    
    // 加载字段和预览数据
    if (report.dataSourceUri && report.collection) {
      await onCollectionChange()
      // 触发图表重新渲染以应用过滤条件
      setTimeout(() => {
        debouncedRenderCharts()
      }, 500)
    }
  } catch (error) {
    console.error('加载报表失败:', error)
    ElMessage.error('加载报表失败')
  }
}

const loadDataSources = async () => {
  try {
    const response = await axios.get('/api/datasource')
    dataSources.value = response.data
  } catch (error) {
    console.error('加载数据源失败:', error)
  }
}

const configureWidget = (widget) => {
  currentWidget.value = widget
  widgetConfig.value = {
    title: widget.config?.title || widget.label,
    xField: widget.config?.xField || '',
    yField: widget.config?.yField || '',
    seriesField: widget.config?.seriesField || '',
    nameField: widget.config?.nameField || '',
    valueField: widget.config?.valueField || '',
    sizeField: widget.config?.sizeField || '',
    min: widget.config?.min || 0,
    max: widget.config?.max || 100,
    // 统计配置
    enableStats: widget.config?.enableStats || false,
    aggregation: widget.config?.aggregation || 'sum',
    groupBy: widget.config?.groupBy || '',
    sortBy: widget.config?.sortBy || '',
    sortOrder: widget.config?.sortOrder || 'desc',
    limit: widget.config?.limit || 10,
    // 样式配置
    theme: widget.config?.theme || 'default',
    showLegend: widget.config?.showLegend !== false,
    showLabel: widget.config?.showLabel !== false,
    animation: widget.config?.animation !== false,
    colors: widget.config?.colors || [],
    // 表格配置
    displayFields: widget.config?.displayFields || [],
    pageSize: widget.config?.pageSize || 10,
    enablePagination: widget.config?.enablePagination || false,
    enableSearch: widget.config?.enableSearch || false
  }
  activeTab.value = 'basic'
  configDialogVisible.value = true
}

// 颜色管理方法
const addColor = (color) => {
  if (color && !widgetConfig.value.colors.includes(color)) {
    widgetConfig.value.colors.push(color)
    newColor.value = ''
  }
}

const removeColor = (index) => {
  widgetConfig.value.colors.splice(index, 1)
}

const addDefaultColors = () => {
  const defaultColors = ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4']
  defaultColors.forEach(color => {
    if (!widgetConfig.value.colors.includes(color)) {
      widgetConfig.value.colors.push(color)
    }
  })
}

const clearColors = () => {
  widgetConfig.value.colors = []
}

const saveWidgetConfig = () => {
  if (currentWidget.value) {
    currentWidget.value.config = { ...widgetConfig.value }
    configDialogVisible.value = false
    ElMessage.success(`组件配置已保存: ${currentWidget.value.label}`)
    
    // 重新渲染图表
    debouncedRenderCharts()
  }
}

const getTableColumns = (element) => {
  if (!tableData.value[element.id]) {
    return []
  }
  
  if (!element.config || !element.config.displayFields || element.config.displayFields.length === 0) {
    return tableData.value[element.id].columns || []
  }
  
  return element.config.displayFields.filter(field => 
    tableData.value[element.id].columns.includes(field)
  )
}

// 表格分页和搜索相关方法
const getFilteredTableData = (element) => {
  if (!tableData.value[element.id]) {
    return []
  }
  
  let data = [...tableData.value[element.id].data]
  
  // 搜索过滤
  const searchText = tableSearchText.value[element.id] || ''
  if (searchText && element.config?.enableSearch) {
    data = data.filter(item => {
      return Object.values(item).some(value => 
        String(value).toLowerCase().includes(searchText.toLowerCase())
      )
    })
  }
  
  // 分页
  if (element.config?.enablePagination) {
    const pageSize = tablePageSize.value[element.id] || element.config.pageSize || 10
    const currentPage = tableCurrentPage.value[element.id] || 1
    const start = (currentPage - 1) * pageSize
    const end = start + pageSize
    data = data.slice(start, end)
  }
  
  return data
}

const onTableSearch = (element) => {
  // 搜索时重置到第一页
  tableCurrentPage.value[element.id] = 1
}

const onTablePageSizeChange = (element) => {
  // 页面大小改变时重置到第一页
  tableCurrentPage.value[element.id] = 1
}

const onTableCurrentPageChange = (element) => {
  // 当前页改变时不需要特殊处理
}

onMounted(async () => {
  await loadDataSources()
  
  if (route.params.id) {
    await loadReport(route.params.id)
    // 等待DOM更新后渲染图表
    debouncedRenderCharts()
  }
})

onUnmounted(() => {
  // 清理定时器
  if (renderTimeout) {
    clearTimeout(renderTimeout)
  }
  
  // 清理所有图表实例
  for (const element of canvas.value || []) {
    if (element.name !== 'table') {
      const chartDom = document.getElementById(`designer-chart-${element.id}`)
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
.report-designer {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.widget-item {
  margin-bottom: 10px;
  cursor: move;
}

.fields-container {
  max-height: 300px;
  overflow-y: auto;
}

.field-item {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  margin-bottom: 8px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.field-item:hover {
  background-color: #f5f7fa;
  border-color: #409eff;
}

.field-item .el-icon {
  margin-right: 8px;
  color: #909399;
}

.field-item span {
  flex: 1;
  margin-right: 8px;
  font-size: 14px;
}

.designer-canvas {
  min-height: 400px;
  border: 2px dashed #ddd;
  padding: 20px;
}

.canvas-item {
  margin-bottom: 20px;
}

.chart-container {
  height: 240px;
  width: 100%;
}

.table-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 240px;
  color: #909399;
  font-size: 14px;
}

.table-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.table-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #909399;
}

.table-loading .is-loading {
  margin-bottom: 8px;
  font-size: 24px;
}

.preview-container {
  height: 400px;
  overflow: hidden;
}

.preview-info {
  text-align: center;
  padding: 8px;
  color: #909399;
  font-size: 12px;
  border-top: 1px solid #e4e7ed;
}

.preview-empty {
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.filter-container {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 16px;
  background-color: #fafafa;
}

.filter-item {
  margin-bottom: 12px;
  padding: 12px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background-color: white;
}

.filter-item:last-child {
  margin-bottom: 0;
}

.filter-item .el-row {
  align-items: center;
}

.filter-item .el-col {
  display: flex;
  align-items: center;
}

.filter-item .el-select,
.filter-item .el-input {
  width: 100%;
}

.color-picker-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.color-picker-container .el-button {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 4px 8px;
  border: 1px solid #e4e7ed;
  background-color: white;
}

.color-picker-container .el-button:hover {
  border-color: #409eff;
}

.color-picker-container .el-icon {
  font-size: 12px;
  color: #909399;
}

.color-picker-container .el-button:hover .el-icon {
  color: #f56c6c;
}
</style> 