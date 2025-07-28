<template>
  <div class="system-settings">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>系统设置</span>
          <el-button type="primary" @click="saveSettings">保存设置</el-button>
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
          </el-form>
        </el-tab-pane>

        <!-- 主题设置 -->
        <el-tab-pane label="主题设置" name="theme">
          <el-form :model="themeSettings" label-width="120px">
            <el-form-item label="主题模式">
              <el-radio-group v-model="themeSettings.mode">
                <el-radio label="light">浅色主题</el-radio>
                <el-radio label="dark">深色主题</el-radio>
                <el-radio label="auto">跟随系统</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="主色调">
              <el-color-picker v-model="themeSettings.primaryColor" />
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
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

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
  fontSize: 14
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

// 加载设置
const loadSettings = async () => {
  try {
    const response = await axios.get('/api/settings')
    const settings = response.data
    
    if (settings.basic) Object.assign(basicSettings.value, settings.basic)
    if (settings.theme) Object.assign(themeSettings.value, settings.theme)
    if (settings.notification) Object.assign(notificationSettings.value, settings.notification)
    if (settings.security) Object.assign(securitySettings.value, settings.security)
    if (settings.performance) Object.assign(performanceSettings.value, settings.performance)
    if (settings.backup) Object.assign(backupSettings.value, settings.backup)
    
  } catch (error) {
    console.error('加载设置失败:', error)
    ElMessage.warning('使用默认设置')
  }
}

// 保存设置
const saveSettings = async () => {
  try {
    const settings = {
      basic: basicSettings.value,
      theme: themeSettings.value,
      notification: notificationSettings.value,
      security: securitySettings.value,
      performance: performanceSettings.value,
      backup: backupSettings.value
    }
    
    await axios.post('/api/settings', settings)
    ElMessage.success('设置保存成功')
    
  } catch (error) {
    console.error('保存设置失败:', error)
    ElMessage.error('保存设置失败')
  }
}

onMounted(() => {
  loadSettings()
})
</script>

<style scoped>
.system-settings {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.el-form-item {
  margin-bottom: 20px;
}

.el-divider {
  margin: 24px 0;
}
</style> 