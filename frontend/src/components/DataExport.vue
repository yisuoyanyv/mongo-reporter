<template>
  <div class="data-export">
    <el-dialog 
      v-model="visible" 
      title="数据导出" 
      width="600px"
      :before-close="handleClose"
    >
      <div class="export-content">
        <!-- 导出格式选择 -->
        <div class="format-section">
          <h4>选择导出格式</h4>
          <el-radio-group v-model="exportFormat" class="format-options">
            <el-radio-button label="pdf">
              <el-icon><Document /></el-icon>
              PDF
            </el-radio-button>
            <el-radio-button label="excel">
              <el-icon><Grid /></el-icon>
              Excel
            </el-radio-button>
            <el-radio-button label="image">
              <el-icon><Picture /></el-icon>
              图片
            </el-radio-button>
            <el-radio-button label="json">
              <el-icon><DataLine /></el-icon>
              JSON
            </el-radio-button>
          </el-radio-group>
        </div>

        <!-- 导出选项 -->
        <div class="options-section">
          <h4>导出选项</h4>
          
          <!-- PDF选项 -->
          <div v-if="exportFormat === 'pdf'" class="format-options">
            <el-form :model="pdfOptions" label-width="100px">
              <el-form-item label="页面大小">
                <el-select v-model="pdfOptions.pageSize">
                  <el-option label="A4" value="a4" />
                  <el-option label="A3" value="a3" />
                  <el-option label="Letter" value="letter" />
                </el-select>
              </el-form-item>
              <el-form-item label="方向">
                <el-radio-group v-model="pdfOptions.orientation">
                  <el-radio label="portrait">纵向</el-radio>
                  <el-radio label="landscape">横向</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="包含图表">
                <el-switch v-model="pdfOptions.includeCharts" />
              </el-form-item>
              <el-form-item label="包含数据表">
                <el-switch v-model="pdfOptions.includeTables" />
              </el-form-item>
            </el-form>
          </div>

          <!-- Excel选项 -->
          <div v-if="exportFormat === 'excel'" class="format-options">
            <el-form :model="excelOptions" label-width="100px">
              <el-form-item label="工作表名称">
                <el-input v-model="excelOptions.sheetName" placeholder="报表数据" />
              </el-form-item>
              <el-form-item label="包含图表">
                <el-switch v-model="excelOptions.includeCharts" />
              </el-form-item>
              <el-form-item label="包含原始数据">
                <el-switch v-model="excelOptions.includeRawData" />
              </el-form-item>
            </el-form>
          </div>

          <!-- 图片选项 -->
          <div v-if="exportFormat === 'image'" class="format-options">
            <el-form :model="imageOptions" label-width="100px">
              <el-form-item label="图片格式">
                <el-radio-group v-model="imageOptions.format">
                  <el-radio label="png">PNG</el-radio>
                  <el-radio label="jpg">JPG</el-radio>
                  <el-radio label="svg">SVG</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="图片质量">
                <el-slider 
                  v-model="imageOptions.quality" 
                  :min="1" 
                  :max="10" 
                  :step="1"
                  show-stops
                />
              </el-form-item>
              <el-form-item label="分辨率">
                <el-select v-model="imageOptions.resolution">
                  <el-option label="标准 (72 DPI)" value="72" />
                  <el-option label="高清 (150 DPI)" value="150" />
                  <el-option label="超清 (300 DPI)" value="300" />
                </el-select>
              </el-form-item>
            </el-form>
          </div>

          <!-- JSON选项 -->
          <div v-if="exportFormat === 'json'" class="format-options">
            <el-form :model="jsonOptions" label-width="100px">
              <el-form-item label="格式化">
                <el-switch v-model="jsonOptions.pretty" />
              </el-form-item>
              <el-form-item label="包含元数据">
                <el-switch v-model="jsonOptions.includeMetadata" />
              </el-form-item>
            </el-form>
          </div>
        </div>

        <!-- 数据范围选择 -->
        <div class="range-section">
          <h4>数据范围</h4>
          <el-radio-group v-model="dataRange">
            <el-radio label="all">全部数据</el-radio>
            <el-radio label="current">当前页面</el-radio>
            <el-radio label="custom">自定义范围</el-radio>
          </el-radio-group>
          
          <div v-if="dataRange === 'custom'" class="custom-range">
            <el-row :gutter="10">
              <el-col :span="12">
                <el-input-number 
                  v-model="customRange.start" 
                  :min="1" 
                  placeholder="开始行"
                />
              </el-col>
              <el-col :span="12">
                <el-input-number 
                  v-model="customRange.end" 
                  :min="1" 
                  placeholder="结束行"
                />
              </el-col>
            </el-row>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleClose">取消</el-button>
          <el-button type="primary" @click="handleExport" :loading="exporting">
            {{ exporting ? '导出中...' : '开始导出' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, Grid, Picture, DataLine } from '@element-plus/icons-vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  reportData: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:visible', 'export-complete'])

// 导出格式
const exportFormat = ref('pdf')

// 导出选项
const pdfOptions = reactive({
  pageSize: 'a4',
  orientation: 'portrait',
  includeCharts: true,
  includeTables: true
})

const excelOptions = reactive({
  sheetName: '报表数据',
  includeCharts: true,
  includeRawData: true
})

const imageOptions = reactive({
  format: 'png',
  quality: 8,
  resolution: '150'
})

const jsonOptions = reactive({
  pretty: true,
  includeMetadata: true
})

// 数据范围
const dataRange = ref('all')
const customRange = reactive({
  start: 1,
  end: 100
})

// 导出状态
const exporting = ref(false)

// 监听对话框关闭
const handleClose = () => {
  emit('update:visible', false)
}

// 导出处理
const handleExport = async () => {
  if (exporting.value) return
  
  exporting.value = true
  
  try {
    const exportConfig = {
      format: exportFormat.value,
      dataRange: dataRange.value,
      customRange: dataRange.value === 'custom' ? customRange : null,
      options: getFormatOptions()
    }
    
    // 调用后端导出API
    const response = await fetch('/api/report/export', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify({
        reportId: props.reportData.id,
        config: exportConfig
      })
    })
    
    if (response.ok) {
      const blob = await response.blob()
      downloadFile(blob, getFileName())
      ElMessage.success('导出成功！')
      emit('export-complete')
      handleClose()
    } else {
      throw new Error('导出失败')
    }
  } catch (error) {
    console.error('导出错误:', error)
    ElMessage.error('导出失败，请重试')
  } finally {
    exporting.value = false
  }
}

// 获取格式选项
const getFormatOptions = () => {
  switch (exportFormat.value) {
    case 'pdf':
      return pdfOptions
    case 'excel':
      return excelOptions
    case 'image':
      return imageOptions
    case 'json':
      return jsonOptions
    default:
      return {}
  }
}

// 生成文件名
const getFileName = () => {
  const timestamp = new Date().toISOString().slice(0, 10)
  const reportName = props.reportData.name || 'report'
  const format = exportFormat.value.toUpperCase()
  
  return `${reportName}_${timestamp}.${getFileExtension()}`
}

// 获取文件扩展名
const getFileExtension = () => {
  switch (exportFormat.value) {
    case 'pdf':
      return 'pdf'
    case 'excel':
      return 'xlsx'
    case 'image':
      return imageOptions.format
    case 'json':
      return 'json'
    default:
      return 'txt'
  }
}

// 下载文件
const downloadFile = (blob, filename) => {
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}
</script>

<style scoped>
.data-export {
  /* 组件样式 */
}

.export-content {
  max-height: 500px;
  overflow-y: auto;
}

.format-section,
.options-section,
.range-section {
  margin-bottom: 24px;
}

.format-section h4,
.options-section h4,
.range-section h4 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 14px;
  font-weight: 500;
}

.format-options {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.format-options .el-radio-button {
  display: flex;
  align-items: center;
  gap: 4px;
}

.custom-range {
  margin-top: 12px;
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 移动端响应式 */
@media (max-width: 768px) {
  .format-options {
    flex-direction: column;
  }
  
  .format-options .el-radio-button {
    width: 100%;
    justify-content: center;
  }
  
  .custom-range .el-row {
    flex-direction: column;
  }
  
  .custom-range .el-col {
    width: 100%;
    margin-bottom: 8px;
  }
}
</style> 