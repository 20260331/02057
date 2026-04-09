<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { notificationApi } from '@/api/notification'
import type { NotificationInfo, NotificationTemplate, NotificationSend } from '@/api/notification'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Promotion, Delete, View } from '@element-plus/icons-vue'

const activeTab = ref('send')

// ===== 发送表单 =====
const sendLoading = ref(false)
const templates = ref<NotificationTemplate[]>([])
const sendForm = ref<NotificationSend>({
  templateId: undefined,
  title: '',
  content: '',
  category: 'SYSTEM',
  priority: 'NORMAL',
  targetType: 'ALL',
  targetValue: '',
  variables: {}
})
const useTemplate = ref(false)
const selectedTemplate = ref<NotificationTemplate | null>(null)

const categoryOptions = [
  { value: 'ACADEMIC', label: '教务通知' },
  { value: 'COURSE', label: '选课通知' },
  { value: 'GRADE', label: '成绩通知' },
  { value: 'LEAVE', label: '请假通知' },
  { value: 'SYSTEM', label: '系统通知' }
]

const priorityOptions = [
  { value: 'LOW', label: '低' },
  { value: 'NORMAL', label: '普通' },
  { value: 'HIGH', label: '高' },
  { value: 'URGENT', label: '紧急' }
]

const targetOptions = [
  { value: 'ALL', label: '全体用户' },
  { value: 'ROLE', label: '按角色' },
  { value: 'USER', label: '指定用户ID' }
]

const roleOptions = [
  { value: 'STUDENT', label: '学生' },
  { value: 'TEACHER', label: '教师' },
  { value: 'COUNSELOR', label: '辅导员' },
  { value: 'DEPARTMENT_HEAD', label: '系主任' },
  { value: 'ACADEMIC_AFFAIRS', label: '教务处' },
  { value: 'ADMIN', label: '管理员' }
]

const selectedRoles = ref<string[]>([])

watch(selectedRoles, (val) => {
  sendForm.value.targetValue = val.join(',')
})

const previewTitle = computed(() => {
  if (!selectedTemplate.value || !sendForm.value.variables) return ''
  let title = selectedTemplate.value.subject
  for (const [k, v] of Object.entries(sendForm.value.variables)) {
    title = title.replace(new RegExp(`\\$\\{${k}\\}`, 'g'), v || `\${${k}}`)
  }
  return title
})

const previewContent = computed(() => {
  if (!selectedTemplate.value || !sendForm.value.variables) return ''
  let content = selectedTemplate.value.content
  for (const [k, v] of Object.entries(sendForm.value.variables)) {
    content = content.replace(new RegExp(`\\$\\{${k}\\}`, 'g'), v || `\${${k}}`)
  }
  return content
})

const loadTemplates = async () => {
  try {
    const res = await notificationApi.getTemplates()
    if (res.code === 200) {
      templates.value = res.data.filter(t => t.status === 1)
    }
  } catch (error) {
    // handled
  }
}

const handleTemplateChange = (templateId: number | undefined) => {
  if (templateId) {
    const tpl = templates.value.find(t => t.id === templateId)
    if (tpl) {
      selectedTemplate.value = tpl
      sendForm.value.category = tpl.category
      const vars: Record<string, string> = {}
      for (const v of (tpl.variables || [])) {
        vars[v] = ''
      }
      sendForm.value.variables = vars
    }
  } else {
    selectedTemplate.value = null
    sendForm.value.variables = {}
  }
}

const handleSend = async () => {
  if (useTemplate.value) {
    if (!sendForm.value.templateId) {
      ElMessage.warning('请选择通知模板')
      return
    }
  } else {
    if (!sendForm.value.title || !sendForm.value.content) {
      ElMessage.warning('请填写通知标题和内容')
      return
    }
  }
  if (sendForm.value.targetType === 'ROLE' && !sendForm.value.targetValue) {
    ElMessage.warning('请选择目标角色')
    return
  }
  if (sendForm.value.targetType === 'USER' && !sendForm.value.targetValue) {
    ElMessage.warning('请输入目标用户ID')
    return
  }

  try {
    await ElMessageBox.confirm('确定发送该通知吗？', '确认发送', { type: 'warning' })
  } catch {
    return
  }

  sendLoading.value = true
  try {
    const payload: NotificationSend = {
      ...sendForm.value,
      templateId: useTemplate.value ? sendForm.value.templateId : undefined,
      title: useTemplate.value ? undefined : sendForm.value.title,
      content: useTemplate.value ? undefined : sendForm.value.content
    }
    await notificationApi.sendNotification(payload)
    ElMessage.success('通知已发送')
    resetSendForm()
    loadHistory()
  } catch (error) {
    // handled
  } finally {
    sendLoading.value = false
  }
}

const resetSendForm = () => {
  sendForm.value = {
    templateId: undefined,
    title: '',
    content: '',
    category: 'SYSTEM',
    priority: 'NORMAL',
    targetType: 'ALL',
    targetValue: '',
    variables: {}
  }
  selectedTemplate.value = null
  selectedRoles.value = []
  useTemplate.value = false
}

// ===== 发送记录 =====
const historyLoading = ref(false)
const historyList = ref<NotificationInfo[]>([])
const detailDialogVisible = ref(false)
const detailData = ref<NotificationInfo | null>(null)

const loadHistory = async () => {
  historyLoading.value = true
  try {
    const res = await notificationApi.getAllNotifications()
    if (res.code === 200) {
      historyList.value = res.data
    }
  } catch (error) {
    // handled
  } finally {
    historyLoading.value = false
  }
}

const handleViewDetail = (row: NotificationInfo) => {
  detailData.value = row
  detailDialogVisible.value = true
}

const handleDeleteNotification = async (row: NotificationInfo) => {
  try {
    await ElMessageBox.confirm('确定删除该通知记录吗？', '确认删除', { type: 'warning' })
    await notificationApi.deleteNotification(row.id)
    ElMessage.success('删除成功')
    loadHistory()
  } catch (error) {
    // cancelled or error
  }
}

const getPriorityType = (priority: string) => {
  const map: Record<string, string> = { LOW: 'info', NORMAL: '', HIGH: 'warning', URGENT: 'danger' }
  return map[priority] || 'info'
}

onMounted(() => {
  loadTemplates()
  loadHistory()
})
</script>

<template>
  <div class="notification-manage">
    <el-tabs v-model="activeTab">
      <!-- 发送通知 -->
      <el-tab-pane label="发送通知" name="send">
        <el-card>
          <el-form :model="sendForm" label-width="100px">
            <el-form-item label="发送方式">
              <el-radio-group v-model="useTemplate">
                <el-radio :value="false">自定义内容</el-radio>
                <el-radio :value="true">使用模板</el-radio>
              </el-radio-group>
            </el-form-item>

            <template v-if="useTemplate">
              <el-form-item label="选择模板" required>
                <el-select v-model="sendForm.templateId" placeholder="请选择通知模板" style="width: 100%"
                  @change="handleTemplateChange">
                  <el-option v-for="tpl in templates" :key="tpl.id" :label="`[${tpl.categoryName}] ${tpl.templateName}`"
                    :value="tpl.id" />
                </el-select>
              </el-form-item>

              <template v-if="selectedTemplate">
                <el-form-item v-for="varName in (selectedTemplate.variables || [])" :key="varName"
                  :label="varName">
                  <el-input v-model="sendForm.variables![varName]" :placeholder="`请输入 ${varName}`" />
                </el-form-item>

                <el-form-item label="预览">
                  <div class="preview-box">
                    <div class="preview-title">{{ previewTitle }}</div>
                    <div class="preview-content">{{ previewContent }}</div>
                  </div>
                </el-form-item>
              </template>
            </template>

            <template v-else>
              <el-form-item label="通知分类">
                <el-select v-model="sendForm.category" style="width: 200px">
                  <el-option v-for="opt in categoryOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
                </el-select>
              </el-form-item>
              <el-form-item label="通知标题" required>
                <el-input v-model="sendForm.title" placeholder="请输入通知标题" />
              </el-form-item>
              <el-form-item label="通知内容" required>
                <el-input v-model="sendForm.content" type="textarea" :rows="5" placeholder="请输入通知内容" />
              </el-form-item>
            </template>

            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="优先级">
                  <el-select v-model="sendForm.priority" style="width: 100%">
                    <el-option v-for="opt in priorityOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="发送目标" required>
                  <el-select v-model="sendForm.targetType" style="width: 100%">
                    <el-option v-for="opt in targetOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item v-if="sendForm.targetType === 'ROLE'" label="目标角色">
              <el-checkbox-group v-model="selectedRoles">
                <el-checkbox v-for="r in roleOptions" :key="r.value" :value="r.value" :label="r.label" />
              </el-checkbox-group>
            </el-form-item>

            <el-form-item v-if="sendForm.targetType === 'USER'" label="用户ID">
              <el-input v-model="sendForm.targetValue" placeholder="多个ID用逗号分隔，如 1,2,3" />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" :icon="Promotion" :loading="sendLoading" @click="handleSend">
                发送通知
              </el-button>
              <el-button @click="resetSendForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <!-- 发送记录 -->
      <el-tab-pane label="发送记录" name="history">
        <el-card v-loading="historyLoading">
          <el-table :data="historyList" stripe>
            <el-table-column prop="title" label="标题" show-overflow-tooltip />
            <el-table-column prop="categoryName" label="分类" width="100" />
            <el-table-column label="优先级" width="80" align="center">
              <template #default="{ row }">
                <el-tag size="small" :type="getPriorityType(row.priority)">{{ row.priorityName }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="targetTypeName" label="目标" width="100" />
            <el-table-column prop="senderName" label="发送人" width="100" />
            <el-table-column prop="sendTime" label="发送时间" width="170" />
            <el-table-column label="操作" width="130" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link size="small" :icon="View" @click="handleViewDetail(row)">查看</el-button>
                <el-button type="danger" link size="small" :icon="Delete" @click="handleDeleteNotification(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="通知详情" width="600px">
      <template v-if="detailData">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="标题" :span="2">{{ detailData.title }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{ detailData.categoryName }}</el-descriptions-item>
          <el-descriptions-item label="优先级">{{ detailData.priorityName }}</el-descriptions-item>
          <el-descriptions-item label="发送目标">{{ detailData.targetTypeName }}</el-descriptions-item>
          <el-descriptions-item label="目标值">{{ detailData.targetValue || '全体' }}</el-descriptions-item>
          <el-descriptions-item label="发送人">{{ detailData.senderName }}</el-descriptions-item>
          <el-descriptions-item label="发送时间">{{ detailData.sendTime }}</el-descriptions-item>
          <el-descriptions-item label="使用模板" :span="2">{{ detailData.templateName || '自定义' }}</el-descriptions-item>
        </el-descriptions>
        <div class="detail-content">
          <h4>通知内容</h4>
          <div class="content-box">{{ detailData.content }}</div>
        </div>
      </template>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.notification-manage {
}

.preview-box {
  width: 100%;
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 12px 16px;

  .preview-title {
    font-weight: bold;
    font-size: 15px;
    margin-bottom: 8px;
    color: #303133;
  }

  .preview-content {
    font-size: 14px;
    color: #606266;
    line-height: 1.6;
    white-space: pre-wrap;
  }
}

.detail-content {
  margin-top: 16px;

  h4 {
    margin: 0 0 8px;
    color: #303133;
  }

  .content-box {
    background: #fafafa;
    border: 1px solid #ebeef5;
    border-radius: 4px;
    padding: 12px;
    line-height: 1.8;
    white-space: pre-wrap;
    color: #606266;
  }
}
</style>
