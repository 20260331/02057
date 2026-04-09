<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import { leaveApi, type AttachmentInfo } from '@/api/leave'
import type { LeaveInfo } from '@/api/types'

const loading = ref(false)
const pendingLeaves = ref<LeaveInfo[]>([])
const onlyUrgent = ref(false)

// 审批对话框
const approvalDialogVisible = ref(false)
const currentLeave = ref<LeaveInfo | null>(null)
const approvalForm = ref({
  approved: true,
  comment: ''
})

// 附件
const attachments = ref<AttachmentInfo[]>([])
const attachmentLoading = ref(false)

// 加载待审批列表
const loadData = async () => {
  loading.value = true
  try {
    const res = onlyUrgent.value
      ? await leaveApi.getUrgentPendingForCounselor()
      : await leaveApi.getPendingForCounselor()
    if (res.code === 200) {
      pendingLeaves.value = res.data
    }
  } catch (error) {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}

// 打开审批对话框
const handleApprove = async (row: LeaveInfo) => {
  currentLeave.value = row
  approvalForm.value = { approved: true, comment: '' }
  approvalDialogVisible.value = true
  
  // 加载附件
  attachmentLoading.value = true
  try {
    const res = await leaveApi.getAttachments(row.id)
    if (res.code === 200) {
      attachments.value = res.data
    }
  } catch (error) {
    attachments.value = []
  } finally {
    attachmentLoading.value = false
  }
}

// 下载附件
const handleDownload = (att: AttachmentInfo) => {
  const baseUrl = import.meta.env.VITE_API_BASE_URL || '/api'
  const token = localStorage.getItem('token')
  
  fetch(`${baseUrl}/files/download/${att.id}`, {
    headers: { Authorization: `Bearer ${token}` }
  })
    .then(res => res.blob())
    .then(blob => {
      const url = window.URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = att.fileName
      a.click()
      window.URL.revokeObjectURL(url)
    })
    .catch(() => {
      ElMessage.error('下载失败')
    })
}

// 提交审批
const submitApproval = async () => {
  if (!currentLeave.value) return
  
  if (!approvalForm.value.comment) {
    ElMessage.warning('请填写审批意见')
    return
  }
  
  try {
    await leaveApi.approveLeave(currentLeave.value.id, approvalForm.value)
    ElMessage.success('审批成功')
    approvalDialogVisible.value = false
    loadData()
  } catch (error) {
    // 错误已在拦截器中处理
  }
}

// 获取类型文本
const getTypeText = (type: string) => {
  const map: Record<string, string> = {
    SICK: '病假',
    PERSONAL: '事假',
    OFFICIAL: '公假'
  }
  return map[type] || type
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="leave-approval-container" v-loading="loading">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>待审批请假</span>
          <div class="header-actions">
            <el-switch
              v-model="onlyUrgent"
              active-text="只看紧急请假"
              @change="loadData"
            />
          </div>
        </div>
      </template>
      
      <el-table :data="pendingLeaves" stripe>
        <el-table-column prop="studentName" label="学生姓名" width="100" />
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="classNo" label="班级" width="100" />
        <el-table-column label="类型" width="80">
          <template #default="{ row }">
            {{ row.leaveTypeText || getTypeText(row.leaveType) }}
          </template>
        </el-table-column>
        <el-table-column prop="startDate" label="开始日期" width="120" />
        <el-table-column prop="endDate" label="结束日期" width="120" />
        <el-table-column prop="duration" label="天数" width="70" />
        <el-table-column prop="reason" label="原因" show-overflow-tooltip />
        <el-table-column label="紧急" width="60">
          <template #default="{ row }">
            <el-tag v-if="row.urgent" type="danger" size="small">紧急</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleApprove(row)">审批</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-empty v-if="pendingLeaves.length === 0" description="暂无待审批的请假" />
    </el-card>
    
    <!-- 审批对话框 -->
    <el-dialog v-model="approvalDialogVisible" title="审批请假" width="500px">
      <el-descriptions v-if="currentLeave" :column="2" border class="leave-info">
        <el-descriptions-item label="学生">{{ currentLeave.studentName }}</el-descriptions-item>
        <el-descriptions-item label="类型">
          {{ currentLeave.leaveTypeText || getTypeText(currentLeave.leaveType) }}
        </el-descriptions-item>
        <el-descriptions-item label="时间">{{ currentLeave.startDate }} 至 {{ currentLeave.endDate }}</el-descriptions-item>
        <el-descriptions-item label="天数">{{ currentLeave.duration }} 天</el-descriptions-item>
        <el-descriptions-item label="原因" :span="2">{{ currentLeave.reason }}</el-descriptions-item>
      </el-descriptions>
      
      <!-- 附件列表 -->
      <div class="attachments-section" v-loading="attachmentLoading">
        <h4>证明材料</h4>
        <div v-if="attachments.length > 0" class="attachment-list">
          <div v-for="att in attachments" :key="att.id" class="attachment-item">
            <span class="file-name">{{ att.fileName }}</span>
            <span class="file-size">{{ att.fileSize }}</span>
            <el-button type="primary" link :icon="Download" @click="handleDownload(att)">下载</el-button>
          </div>
        </div>
        <div v-else class="no-attachment">暂无附件</div>
      </div>
      
      <el-form class="approval-form">
        <el-form-item label="审批结果">
          <el-radio-group v-model="approvalForm.approved">
            <el-radio :value="true">批准</el-radio>
            <el-radio :value="false">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审批意见">
          <el-input
            v-model="approvalForm.comment"
            type="textarea"
            :rows="3"
            placeholder="请填写审批意见"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="approvalDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitApproval">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.leave-approval-container {
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.leave-info {
  margin-bottom: 20px;
}

.attachments-section {
  margin: 16px 0;
  
  h4 {
    margin-bottom: 10px;
    font-size: 14px;
    color: #606266;
  }
  
  .attachment-list {
    .attachment-item {
      display: flex;
      align-items: center;
      padding: 8px 12px;
      background: #f5f7fa;
      border-radius: 4px;
      margin-bottom: 8px;
      
      .file-name {
        flex: 1;
        color: #303133;
      }
      
      .file-size {
        color: #909399;
        font-size: 12px;
        margin-right: 12px;
      }
    }
  }
  
  .no-attachment {
    color: #909399;
    font-size: 14px;
  }
}

.approval-form {
  margin-top: 20px;
}
</style>
