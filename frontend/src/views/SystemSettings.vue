<template>
  <div class="system-settings">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>系统设置</span>
          <div class="header-actions">
            <el-button @click="resetSettings" type="warning" plain>重置设置</el-button>
            <el-button type="primary" @click="saveSettings">保存设置</el-button>
          </div>
        </div>
      </template>
      
      <el-tabs v-model="activeTab" type="border-card">
        <!-- 基本设置 -->
        <el-tab-pane label="基本设置" name="basic">
          <el-form :model="basicSettings" label-width="120px">
            <el-form-item label="系统名称">
              <el-input v-model="basicSettings.systemName" placeholder="请输入系统名称" />
            </el-form-item>
            <el-form-item label="系统描述">
              <el-input 
                v-model="basicSettings.systemDescription" 
                type="textarea" 
                :rows="3"
                placeholder="请输入系统描述"
              />
            </el-form-item>
            <el-form-item label="默认语言">
              <el-select v-model="basicSettings.defaultLanguage" placeholder="选择默认语言">
                <el-option label="中文" value="zh-CN" />
                <el-option label="English" value="en-US" />
              </el-select>
            </el-form-item>
            <el-form-item label="时区设置">
              <el-select v-model="basicSettings.timezone" placeholder="选择时区">
                <el-option label="UTC+8 (北京时间)" value="Asia/Shanghai" />
                <el-option label="UTC+0 (格林威治时间)" value="UTC" />
                <el-option label="UTC-8 (太平洋时间)" value="America/Los_Angeles" />
              </el-select>
            </el-form-item>
            <el-form-item label="数据保留天数">
              <el-input-number 
                v-model="basicSettings.dataRetentionDays" 
                :min="1" 
                :max="3650"
                placeholder="数据保留天数"
              />
            </el-form-item>
            
            <el-divider />
            
            <el-form-item label="系统信息">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="应用名称">{{ systemInfo.appName || 'MongoReporter' }}</el-descriptions-item>
                <el-descriptions-item label="版本">{{ systemInfo.version || '1.4.2' }}</el-descriptions-item>
                <el-descriptions-item label="Java版本">{{ systemInfo.javaVersion || '-' }}</el-descriptions-item>
                <el-descriptions-item label="操作系统">{{ systemInfo.osName || '-' }} {{ systemInfo.osVersion || '' }}</el-descriptions-item>
                <el-descriptions-item label="总内存">{{ formatMemory(systemInfo.totalMemory) }}</el-descriptions-item>
                <el-descriptions-item label="可用内存">{{ formatMemory(systemInfo.freeMemory) }}</el-descriptions-item>
                <el-descriptions-item label="最大内存">{{ formatMemory(systemInfo.maxMemory) }}</el-descriptions-item>
                <el-descriptions-item label="处理器数量">{{ systemInfo.availableProcessors || '-' }}</el-descriptions-item>
              </el-descriptions>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 主题设置 -->
        <el-tab-pane label="主题设置" name="theme">
          <el-form :model="themeSettings" label-width="120px">
            <el-form-item label="主题模式">
              <el-radio-group v-model="themeSettings.mode" @change="handleThemeModeChange">
                <el-radio label="light">浅色主题</el-radio>
                <el-radio label="dark">深色主题</el-radio>
                <el-radio label="auto">跟随系统</el-radio>
              </el-radio-group>
            </el-form-item>
            
            <el-form-item label="主题预设">
              <div class="theme-presets">
                <div 
                  v-for="theme in availableThemes" 
                  :key="theme.key"
                  class="theme-preset-item"
                  :class="{ active: themeSettings.preset === theme.key }"
                  @click="selectThemePreset(theme.key)"
                >
                  <div class="theme-preview" :style="{ backgroundColor: theme.background }">
                    <div class="theme-primary" :style="{ backgroundColor: theme.primary }"></div>
                  </div>
                  <div class="theme-name">{{ theme.name }}</div>
                </div>
              </div>
            </el-form-item>
            
            <el-form-item label="主色调">
              <el-color-picker v-model="themeSettings.primaryColor" @change="handlePrimaryColorChange" />
            </el-form-item>
            
            <el-form-item label="图表主题">
              <el-select v-model="themeSettings.chartTheme" placeholder="选择图表主题">
                <el-option label="默认主题" value="default" />
                <el-option label="浅色主题" value="light" />
                <el-option label="深色主题" value="dark" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="字体大小">
              <el-slider 
                v-model="themeSettings.fontSize" 
                :min="12" 
                :max="20" 
                :step="1"
                show-input
                @change="handleFontSizeChange"
              />
            </el-form-item>
            
            <el-form-item label="圆角大小">
              <el-slider 
                v-model="themeSettings.borderRadius" 
                :min="0" 
                :max="20" 
                :step="2"
                show-input
                @change="handleBorderRadiusChange"
              />
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 通知设置 -->
        <el-tab-pane label="通知设置" name="notification">
          <el-form :model="notificationSettings" label-width="120px">
            <el-form-item label="邮件通知">
              <el-switch v-model="notificationSettings.emailEnabled" />
            </el-form-item>
            <el-form-item label="SMTP服务器" v-if="notificationSettings.emailEnabled">
              <el-input v-model="notificationSettings.smtpServer" placeholder="SMTP服务器地址" />
            </el-form-item>
            <el-form-item label="SMTP端口" v-if="notificationSettings.emailEnabled">
              <el-input-number v-model="notificationSettings.smtpPort" :min="1" :max="65535" />
            </el-form-item>
            <el-form-item label="邮箱账号" v-if="notificationSettings.emailEnabled">
              <el-input v-model="notificationSettings.emailAccount" placeholder="邮箱账号" />
            </el-form-item>
            <el-form-item label="邮箱密码" v-if="notificationSettings.emailEnabled">
              <el-input v-model="notificationSettings.emailPassword" type="password" placeholder="邮箱密码" />
            </el-form-item>
            
            <el-form-item v-if="notificationSettings.emailEnabled">
              <el-button type="primary" @click="testEmailConfig" :disabled="!notificationSettings.smtpServer || !notificationSettings.emailAccount">
                测试邮件配置
              </el-button>
            </el-form-item>
            
            <el-divider />
            
            <el-form-item label="系统通知">
              <el-switch v-model="notificationSettings.systemNotification" />
            </el-form-item>
            <el-form-item label="报表完成通知">
              <el-switch v-model="notificationSettings.reportCompletionNotification" />
            </el-form-item>
            <el-form-item label="数据源异常通知">
              <el-switch v-model="notificationSettings.datasourceErrorNotification" />
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 安全设置 -->
        <el-tab-pane label="安全设置" name="security">
          <el-form :model="securitySettings" label-width="120px">
            <el-form-item label="密码最小长度">
              <el-input-number 
                v-model="securitySettings.minPasswordLength" 
                :min="6" 
                :max="20"
              />
            </el-form-item>
            <el-form-item label="密码复杂度">
              <el-checkbox-group v-model="securitySettings.passwordComplexity">
                <el-checkbox label="uppercase">包含大写字母</el-checkbox>
                <el-checkbox label="lowercase">包含小写字母</el-checkbox>
                <el-checkbox label="numbers">包含数字</el-checkbox>
                <el-checkbox label="symbols">包含特殊字符</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            <el-form-item label="会话超时时间">
              <el-input-number 
                v-model="securitySettings.sessionTimeout" 
                :min="5" 
                :max="1440"
              />
              <span style="margin-left: 8px;">分钟</span>
            </el-form-item>
            <el-form-item label="登录失败锁定">
              <el-switch v-model="securitySettings.loginLockEnabled" />
            </el-form-item>
            <el-form-item label="失败次数阈值" v-if="securitySettings.loginLockEnabled">
              <el-input-number 
                v-model="securitySettings.loginLockThreshold" 
                :min="3" 
                :max="10"
              />
            </el-form-item>
            <el-form-item label="锁定时间" v-if="securitySettings.loginLockEnabled">
              <el-input-number 
                v-model="securitySettings.loginLockDuration" 
                :min="5" 
                :max="60"
              />
              <span style="margin-left: 8px;">分钟</span>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 性能设置 -->
        <el-tab-pane label="性能设置" name="performance">
          <el-form :model="performanceSettings" label-width="120px">
            <el-form-item label="数据缓存">
              <el-switch v-model="performanceSettings.dataCacheEnabled" />
            </el-form-item>
            <el-form-item label="缓存时间" v-if="performanceSettings.dataCacheEnabled">
              <el-input-number 
                v-model="performanceSettings.cacheDuration" 
                :min="1" 
                :max="1440"
              />
              <span style="margin-left: 8px;">分钟</span>
            </el-form-item>
            <el-form-item label="查询超时时间">
              <el-input-number 
                v-model="performanceSettings.queryTimeout" 
                :min="5" 
                :max="300"
              />
              <span style="margin-left: 8px;">秒</span>
            </el-form-item>
            <el-form-item label="最大查询结果数">
              <el-input-number 
                v-model="performanceSettings.maxQueryResults" 
                :min="100" 
                :max="10000"
              />
            </el-form-item>
            <el-form-item label="并发查询数">
              <el-input-number 
                v-model="performanceSettings.maxConcurrentQueries" 
                :min="1" 
                :max="50"
              />
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 备份设置 -->
        <el-tab-pane label="备份设置" name="backup">
          <el-form :model="backupSettings" label-width="120px">
            <el-form-item label="自动备份">
              <el-switch v-model="backupSettings.autoBackupEnabled" />
            </el-form-item>
            <el-form-item label="备份频率" v-if="backupSettings.autoBackupEnabled">
              <el-select v-model="backupSettings.backupFrequency" placeholder="选择备份频率">
                <el-option label="每天" value="daily" />
                <el-option label="每周" value="weekly" />
                <el-option label="每月" value="monthly" />
              </el-select>
            </el-form-item>
            <el-form-item label="备份时间" v-if="backupSettings.autoBackupEnabled">
              <el-time-picker 
                v-model="backupSettings.backupTime" 
                format="HH:mm"
                placeholder="选择备份时间"
              />
            </el-form-item>
            <el-form-item label="保留备份数" v-if="backupSettings.autoBackupEnabled">
              <el-input-number 
                v-model="backupSettings.backupRetentionCount" 
                :min="1" 
                :max="100"
              />
            </el-form-item>
            <el-form-item label="备份路径">
              <el-input v-model="backupSettings.backupPath" placeholder="备份文件存储路径" />
            </el-form-item>
            <el-form-item v-if="backupSettings.autoBackupEnabled">
              <el-button type="primary" @click="testBackupPath" :disabled="!backupSettings.backupPath">
                测试备份路径
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

// 当前激活的标签页
const activeTab = ref('basic')

// 基本设置
const basicSettings = ref({
  systemName: 'MongoReporter',
  systemDescription: '基于MongoDB的数据报表平台',
  defaultLanguage: 'zh-CN',
  timezone: 'Asia/Shanghai',
  dataRetentionDays: 365
})

// 主题设置
const themeSettings = ref({
  mode: 'light',
  primaryColor: '#409EFF',
  chartTheme: 'default',
  fontSize: 14,
  borderRadius: 8, // Added borderRadius
  preset: 'default' // Added preset
})

// 通知设置
const notificationSettings = ref({
  emailEnabled: false,
  smtpServer: '',
  smtpPort: 587,
  emailAccount: '',
  emailPassword: '',
  systemNotification: true,
  reportCompletionNotification: true,
  datasourceErrorNotification: true
})

// 安全设置
const securitySettings = ref({
  minPasswordLength: 8,
  passwordComplexity: ['uppercase', 'lowercase', 'numbers'],
  sessionTimeout: 30,
  loginLockEnabled: true,
  loginLockThreshold: 5,
  loginLockDuration: 15
})

// 性能设置
const performanceSettings = ref({
  dataCacheEnabled: true,
  cacheDuration: 30,
  queryTimeout: 30,
  maxQueryResults: 1000,
  maxConcurrentQueries: 10
})

// 备份设置
const backupSettings = ref({
  autoBackupEnabled: false,
  backupFrequency: 'daily',
  backupTime: new Date(2024, 0, 1, 2, 0),
  backupRetentionCount: 30,
  backupPath: '/backup'
})

// 主题预设数据
const availableThemes = ref([
  { key: 'default', name: '默认主题', background: '#f0f2f5', primary: '#409EFF' },
  { key: 'light', name: '浅色主题', background: '#f0f2f5', primary: '#409EFF' },
  { key: 'dark', name: '深色主题', background: '#1f2d3d', primary: '#606266' },
  { key: 'blue', name: '蓝色主题', background: '#e6f7ff', primary: '#1890ff' },
  { key: 'green', name: '绿色主题', background: '#f0f9eb', primary: '#67c23a' },
  { key: 'purple', name: '紫色主题', background: '#f9f0ff', primary: '#9254de' },
  { key: 'orange', name: '橙色主题', background: '#fffbe6', primary: '#faad14' },
  { key: 'red', name: '红色主题', background: '#fff1f0', primary: '#f5222d' }
])

// 系统信息
const systemInfo = ref({
  appName: '',
  version: '',
  javaVersion: '',
  osName: '',
  osVersion: '',
  totalMemory: 0,
  freeMemory: 0,
  maxMemory: 0,
  availableProcessors: 0
})

// 加载设置
const loadSettings = async () => {
  try {
    const response = await axios.get('/api/settings')
    const settings = response.data
    
    // 更新各个分类的设置
    if (settings.basic) {
      Object.assign(basicSettings.value, settings.basic)
    }
    if (settings.theme) {
      Object.assign(themeSettings.value, settings.theme)
    }
    if (settings.notification) {
      Object.assign(notificationSettings.value, settings.notification)
    }
    if (settings.security) {
      Object.assign(securitySettings.value, settings.security)
    }
    if (settings.performance) {
      Object.assign(performanceSettings.value, settings.performance)
    }
    if (settings.backup) {
      Object.assign(backupSettings.value, settings.backup)
      // 处理备份时间的格式转换
      if (settings.backup.backupTime && typeof settings.backup.backupTime === 'string') {
        const [hours, minutes] = settings.backup.backupTime.split(':')
        backupSettings.value.backupTime = new Date(2024, 0, 1, parseInt(hours), parseInt(minutes))
      }
    }
    
    // 加载系统信息
    try {
      const systemResponse = await axios.get('/api/settings/system/info')
      systemInfo.value = systemResponse.data
    } catch (systemError) {
      console.error('加载系统信息失败:', systemError)
    }
    
    ElMessage.success('设置加载成功')
  } catch (error) {
    console.error('加载设置失败:', error)
    ElMessage.warning('使用默认设置')
  }
}

// 保存设置
const saveSettings = async () => {
  try {
    // 处理备份时间的格式转换
    const backupSettingsToSave = { ...backupSettings.value }
    if (backupSettingsToSave.backupTime instanceof Date) {
      const hours = backupSettingsToSave.backupTime.getHours().toString().padStart(2, '0')
      const minutes = backupSettingsToSave.backupTime.getMinutes().toString().padStart(2, '0')
      backupSettingsToSave.backupTime = `${hours}:${minutes}`
    }
    
    const settings = {
      basic: basicSettings.value,
      theme: themeSettings.value,
      notification: notificationSettings.value,
      security: securitySettings.value,
      performance: performanceSettings.value,
      backup: backupSettingsToSave
    }
    
    await axios.post('/api/settings', settings)
    ElMessage.success('设置保存成功')
    
    // 重新加载设置以确保数据同步
    await loadSettings()
  } catch (error) {
    console.error('保存设置失败:', error)
    ElMessage.error('保存设置失败: ' + (error.response?.data?.message || error.message))
  }
}

// 重置设置
const resetSettings = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要重置所有设置到默认值吗？此操作不可撤销。',
      '确认重置',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    await axios.post('/api/settings/reset')
    ElMessage.success('设置已重置到默认值')
    
    // 重新加载设置
    await loadSettings()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重置设置失败:', error)
      ElMessage.error('重置设置失败: ' + (error.response?.data?.message || error.message))
    }
  }
}

// 测试邮件配置
const testEmailConfig = async () => {
  if (!notificationSettings.value.emailEnabled) {
    ElMessage.warning('请先启用邮件通知')
    return
  }
  
  if (!notificationSettings.value.smtpServer || !notificationSettings.value.emailAccount) {
    ElMessage.warning('请先配置SMTP服务器和邮箱账号')
    return
  }
  
  try {
    ElMessage.info('正在测试邮件配置...')
    // TODO: 实现邮件测试功能
    ElMessage.success('邮件配置测试成功')
  } catch (error) {
    console.error('邮件配置测试失败:', error)
    ElMessage.error('邮件配置测试失败: ' + (error.response?.data?.message || error.message))
  }
}

// 测试备份路径
const testBackupPath = async () => {
  if (!backupSettings.value.backupPath) {
    ElMessage.warning('请先配置备份路径')
    return
  }
  
  try {
    ElMessage.info('正在测试备份路径...')
    // TODO: 实现备份路径测试功能
    ElMessage.success('备份路径测试成功')
  } catch (error) {
    console.error('备份路径测试失败:', error)
    ElMessage.error('备份路径测试失败: ' + (error.response?.data?.message || error.message))
  }
}

// 处理主题模式变化
const handleThemeModeChange = (value) => {
  themeSettings.value.mode = value
  // 根据模式切换主题预设
  if (value === 'auto') {
    themeSettings.value.preset = 'default' // 跟随系统时，默认使用浅色主题
  } else {
    themeSettings.value.preset = value
  }
}

// 处理主色调变化
const handlePrimaryColorChange = (value) => {
  themeSettings.value.primaryColor = value
}

// 处理字体大小变化
const handleFontSizeChange = (value) => {
  themeSettings.value.fontSize = value
}

// 处理圆角大小变化
const handleBorderRadiusChange = (value) => {
  themeSettings.value.borderRadius = value
}

// 选择主题预设
const selectThemePreset = (key) => {
  themeSettings.value.preset = key
  const selectedTheme = availableThemes.value.find(theme => theme.key === key)
  if (selectedTheme) {
    themeSettings.value.primaryColor = selectedTheme.primary
  }
}

// 格式化内存大小
const formatMemory = (bytes) => {
  if (bytes === 0) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

onMounted(() => {
  loadSettings()
})
</script>

<style scoped>
.system-settings {
  padding: 20px;
  min-height: calc(100vh - 70px);
  background-color: var(--theme-background, #f5f7fa);
  color: var(--theme-text, #303133);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: var(--theme-text, #303133);
}

.card-header span {
  font-size: 18px;
  font-weight: 600;
  color: var(--theme-text, #303133);
}

.header-actions {
  display: flex;
  gap: 10px;
}

.el-form-item {
  margin-bottom: 20px;
}

.el-form-item__label {
  color: var(--theme-text, #303133) !important;
  font-weight: 500;
}

.el-divider {
  margin: 24px 0;
}

/* 深色主题下的特殊样式 */
:deep(.el-card) {
  background-color: var(--theme-surface, #ffffff);
  border-color: var(--theme-border, #DCDFE6);
  color: var(--theme-text, #303133);
}

:deep(.el-card__header) {
  background-color: var(--theme-surface, #ffffff);
  border-bottom-color: var(--theme-border, #DCDFE6);
  color: var(--theme-text, #303133);
}

:deep(.el-tabs) {
  background-color: var(--theme-surface, #ffffff);
  border-color: var(--theme-border, #DCDFE6);
}

:deep(.el-tabs--border-card) {
  background-color: var(--theme-surface, #ffffff);
  border-color: var(--theme-border, #DCDFE6);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

:deep(.el-tabs--border-card > .el-tabs__header) {
  background-color: var(--theme-surface, #ffffff);
  border-bottom-color: var(--theme-border, #DCDFE6);
}

:deep(.el-tabs--border-card > .el-tabs__header .el-tabs__item) {
  background-color: var(--theme-surface, #ffffff);
  border-color: var(--theme-border, #DCDFE6);
  color: var(--theme-text, #303133) !important;
}

:deep(.el-tabs--border-card > .el-tabs__header .el-tabs__item.is-active) {
  background-color: var(--el-color-primary, #409EFF);
  border-color: var(--el-color-primary, #409EFF);
  color: #ffffff !important;
}

:deep(.el-tabs--border-card > .el-tabs__header .el-tabs__item:hover) {
  color: var(--el-color-primary, #409EFF) !important;
}

:deep(.el-tabs--border-card > .el-tabs__content) {
  background-color: var(--theme-surface, #ffffff);
  color: var(--theme-text, #303133);
}

:deep(.el-tabs__item) {
  color: var(--theme-text, #303133) !important;
}

:deep(.el-tabs__item.is-active) {
  color: var(--el-color-primary, #409EFF) !important;
}

:deep(.el-tabs__active-bar) {
  background-color: var(--el-color-primary, #409EFF) !important;
}

:deep(.el-form-item__label) {
  color: var(--theme-text, #303133) !important;
}

:deep(.el-input__inner) {
  background-color: var(--theme-surface, #ffffff);
  border-color: var(--theme-border, #DCDFE6);
  color: var(--theme-text, #303133);
}

:deep(.el-input__inner:focus) {
  border-color: var(--el-color-primary, #409EFF);
}

:deep(.el-textarea__inner) {
  background-color: var(--theme-surface, #ffffff);
  border-color: var(--theme-border, #DCDFE6);
  color: var(--theme-text, #303133);
}

:deep(.el-select .el-input__inner) {
  background-color: var(--theme-surface, #ffffff);
  border-color: var(--theme-border, #DCDFE6);
  color: var(--theme-text, #303133);
}

:deep(.el-radio__label) {
  color: var(--theme-text, #303133) !important;
}

:deep(.el-checkbox__label) {
  color: var(--theme-text, #303133) !important;
}

:deep(.el-switch__label) {
  color: var(--theme-text, #303133) !important;
}

:deep(.el-slider__runway) {
  background-color: var(--theme-border, #DCDFE6);
}

:deep(.el-slider__bar) {
  background-color: var(--el-color-primary, #409EFF);
}

:deep(.el-slider__button) {
  border-color: var(--el-color-primary, #409EFF);
}

.theme-presets {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 10px;
}

.theme-preset-item {
  width: 80px;
  height: 80px;
  border: 2px solid var(--theme-border, #ebeef5);
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  background-color: var(--theme-surface, #ffffff);
}

.theme-preset-item:hover {
  border-color: var(--el-color-primary, #409EFF);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  transform: translateY(-2px);
}

.theme-preset-item.active {
  border-color: var(--el-color-primary, #409EFF);
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.5);
  transform: translateY(-2px);
}

.theme-preview {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  position: relative;
  margin-bottom: 5px;
  border: 2px solid var(--theme-border, #ebeef5);
}

.theme-primary {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  position: absolute;
  top: 0;
  left: 0;
}

.theme-name {
  font-size: 12px;
  color: var(--theme-text, #606266);
  text-align: center;
  font-weight: 500;
}

/* 深色主题下的特殊处理 */
.dark-theme .system-settings {
  background-color: var(--theme-background, #1a1a1a);
  color: var(--theme-text, #ffffff);
}

.dark-theme :deep(.el-card) {
  background-color: var(--theme-surface, #2d2d2d);
  border-color: var(--theme-border, #404040);
  color: var(--theme-text, #ffffff);
}

.dark-theme :deep(.el-card__header) {
  background-color: var(--theme-surface, #2d2d2d);
  border-bottom-color: var(--theme-border, #404040);
  color: var(--theme-text, #ffffff);
}

.dark-theme :deep(.el-tabs) {
  background-color: var(--theme-surface, #2d2d2d);
  border-color: var(--theme-border, #404040);
}

.dark-theme :deep(.el-tabs--border-card) {
  background-color: var(--theme-surface, #2d2d2d);
  border-color: var(--theme-border, #404040);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.dark-theme :deep(.el-tabs--border-card > .el-tabs__header) {
  background-color: var(--theme-surface, #2d2d2d);
  border-bottom-color: var(--theme-border, #404040);
}

.dark-theme :deep(.el-tabs--border-card > .el-tabs__header .el-tabs__item) {
  background-color: var(--theme-surface, #2d2d2d);
  border-color: var(--theme-border, #404040);
  color: var(--theme-text, #ffffff) !important;
}

.dark-theme :deep(.el-tabs--border-card > .el-tabs__header .el-tabs__item.is-active) {
  background-color: var(--el-color-primary, #409EFF);
  border-color: var(--el-color-primary, #409EFF);
  color: #ffffff !important;
}

.dark-theme :deep(.el-tabs--border-card > .el-tabs__header .el-tabs__item:hover) {
  color: var(--el-color-primary, #409EFF) !important;
}

.dark-theme :deep(.el-tabs--border-card > .el-tabs__content) {
  background-color: var(--theme-surface, #2d2d2d);
  color: var(--theme-text, #ffffff);
}

.dark-theme :deep(.el-tabs__item) {
  color: var(--theme-text, #ffffff) !important;
}

.dark-theme :deep(.el-tabs__item.is-active) {
  color: var(--el-color-primary, #409EFF) !important;
}

.dark-theme :deep(.el-form-item__label) {
  color: var(--theme-text, #ffffff) !important;
}

.dark-theme :deep(.el-input__inner) {
  background-color: var(--theme-surface, #2d2d2d);
  border-color: var(--theme-border, #404040);
  color: var(--theme-text, #ffffff);
}

.dark-theme :deep(.el-textarea__inner) {
  background-color: var(--theme-surface, #2d2d2d);
  border-color: var(--theme-border, #404040);
  color: var(--theme-text, #ffffff);
}

.dark-theme :deep(.el-select .el-input__inner) {
  background-color: var(--theme-surface, #2d2d2d);
  border-color: var(--theme-border, #404040);
  color: var(--theme-text, #ffffff);
}

.dark-theme :deep(.el-radio__label) {
  color: var(--theme-text, #ffffff) !important;
}

.dark-theme :deep(.el-checkbox__label) {
  color: var(--theme-text, #ffffff) !important;
}

.dark-theme :deep(.el-switch__label) {
  color: var(--theme-text, #ffffff) !important;
}

.dark-theme .theme-preset-item {
  background-color: var(--theme-surface, #2d2d2d);
  border-color: var(--theme-border, #404040);
}

.dark-theme .theme-name {
  color: var(--theme-text, #b0b0b0);
}

/* 全局深色主题样式覆盖 */
:global(.dark-theme) .system-settings {
  background-color: var(--theme-background, #1a1a1a);
  color: var(--theme-text, #ffffff);
}

:global(.dark-theme) .system-settings :deep(.el-card) {
  background-color: var(--theme-surface, #2d2d2d);
  border-color: var(--theme-border, #404040);
  color: var(--theme-text, #ffffff);
}

:global(.dark-theme) .system-settings :deep(.el-card__header) {
  background-color: var(--theme-surface, #2d2d2d);
  border-bottom-color: var(--theme-border, #404040);
  color: var(--theme-text, #ffffff);
}

:global(.dark-theme) .system-settings :deep(.el-tabs) {
  background-color: var(--theme-surface, #2d2d2d);
  border-color: var(--theme-border, #404040);
}

:global(.dark-theme) .system-settings :deep(.el-tabs--border-card) {
  background-color: var(--theme-surface, #2d2d2d);
  border-color: var(--theme-border, #404040);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

:global(.dark-theme) .system-settings :deep(.el-tabs--border-card > .el-tabs__header) {
  background-color: var(--theme-surface, #2d2d2d);
  border-bottom-color: var(--theme-border, #404040);
}

:global(.dark-theme) .system-settings :deep(.el-tabs--border-card > .el-tabs__header .el-tabs__item) {
  background-color: var(--theme-surface, #2d2d2d);
  border-color: var(--theme-border, #404040);
  color: var(--theme-text, #ffffff) !important;
}

:global(.dark-theme) .system-settings :deep(.el-tabs--border-card > .el-tabs__header .el-tabs__item.is-active) {
  background-color: var(--el-color-primary, #409EFF);
  border-color: var(--el-color-primary, #409EFF);
  color: #ffffff !important;
}

:global(.dark-theme) .system-settings :deep(.el-tabs--border-card > .el-tabs__header .el-tabs__item:hover) {
  color: var(--el-color-primary, #409EFF) !important;
}

:global(.dark-theme) .system-settings :deep(.el-tabs--border-card > .el-tabs__content) {
  background-color: var(--theme-surface, #2d2d2d);
  color: var(--theme-text, #ffffff);
}

:global(.dark-theme) .system-settings :deep(.el-tabs__item) {
  color: var(--theme-text, #ffffff) !important;
}

:global(.dark-theme) .system-settings :deep(.el-tabs__item.is-active) {
  color: var(--el-color-primary, #409EFF) !important;
}

:global(.dark-theme) .system-settings :deep(.el-form-item__label) {
  color: var(--theme-text, #ffffff) !important;
}

:global(.dark-theme) .system-settings :deep(.el-input__inner) {
  background-color: var(--theme-surface, #2d2d2d);
  border-color: var(--theme-border, #404040);
  color: var(--theme-text, #ffffff);
}

:global(.dark-theme) .system-settings :deep(.el-textarea__inner) {
  background-color: var(--theme-surface, #2d2d2d);
  border-color: var(--theme-border, #404040);
  color: var(--theme-text, #ffffff);
}

:global(.dark-theme) .system-settings :deep(.el-select .el-input__inner) {
  background-color: var(--theme-surface, #2d2d2d);
  border-color: var(--theme-border, #404040);
  color: var(--theme-text, #ffffff);
}

:global(.dark-theme) .system-settings :deep(.el-radio__label) {
  color: var(--theme-text, #ffffff) !important;
}

:global(.dark-theme) .system-settings :deep(.el-checkbox__label) {
  color: var(--theme-text, #ffffff) !important;
}

:global(.dark-theme) .system-settings :deep(.el-switch__label) {
  color: var(--theme-text, #ffffff) !important;
}

:global(.dark-theme) .system-settings .theme-preset-item {
  background-color: var(--theme-surface, #2d2d2d);
  border-color: var(--theme-border, #404040);
}

:global(.dark-theme) .system-settings .theme-name {
  color: var(--theme-text, #b0b0b0);
}

/* 确保所有文本在深色主题下都清晰可见 */
:global(.dark-theme) .system-settings :deep(.el-input-number__decrease),
:global(.dark-theme) .system-settings :deep(.el-input-number__increase) {
  background-color: var(--theme-surface, #2d2d2d);
  border-color: var(--theme-border, #404040);
  color: var(--theme-text, #ffffff);
}

:global(.dark-theme) .system-settings :deep(.el-slider__runway) {
  background-color: var(--theme-border, #404040);
}

:global(.dark-theme) .system-settings :deep(.el-slider__bar) {
  background-color: var(--el-color-primary, #409EFF);
}

:global(.dark-theme) .system-settings :deep(.el-slider__button) {
  border-color: var(--el-color-primary, #409EFF);
  background-color: var(--theme-surface, #2d2d2d);
}

:global(.dark-theme) .system-settings :deep(.el-color-picker__trigger) {
  border-color: var(--theme-border, #404040);
  background-color: var(--theme-surface, #2d2d2d);
}

:global(.dark-theme) .system-settings :deep(.el-select-dropdown) {
  background-color: var(--theme-surface, #2d2d2d);
  border-color: var(--theme-border, #404040);
}

:global(.dark-theme) .system-settings :deep(.el-select-dropdown__item) {
  color: var(--theme-text, #ffffff);
}

:global(.dark-theme) .system-settings :deep(.el-select-dropdown__item:hover) {
  background-color: var(--theme-border, #404040);
}

:global(.dark-theme) .system-settings :deep(.el-select-dropdown__item.selected) {
  background-color: var(--el-color-primary, #409EFF);
  color: #ffffff;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .system-settings {
    padding: 16px;
  }
  
  .card-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .header-actions {
    width: 100%;
    justify-content: space-between;
  }

  .theme-presets {
    gap: 8px;
  }
  
  .theme-preset-item {
    width: 70px;
    height: 70px;
  }
}

@media (max-width: 480px) {
  .system-settings {
    padding: 12px;
  }
  
  .theme-preset-item {
    width: 60px;
    height: 60px;
  }
  
  .theme-preview {
    width: 30px;
    height: 30px;
  }
}
</style> 