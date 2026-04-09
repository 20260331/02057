<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import { leaveApi, type AttachmentInfo } from '@/api/leave'
import type { LeaveInfo } from '@/api/types'

const loading = ref(false)
const leaves = ref<LeaveInfo[]>([])

// 详情对话框
const detailDialogVisible = ref(false)
const currentLeave = ref<LeaveInfo | null>(null)
const attachments = ref<AttachmentInfo[]>([])
const attachmentLoading = ref(false)

// 销假对话框
const returnDialogVisible = ref(false)
const returnDate = ref('')

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await leaveApi.getMyLeaves()
    if (res.code === 200) {
      leaves.value = res.data
    }
  } catch (error) {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}

// 查看详情
const handleViewDetail = async (row: LeaveInfo) => {
  currentLeave.value = row
  detailDialogVisible.value = true
  
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
  
  // 使用 fetch 下载
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

// 打开销假对话框
const handleReturn = (row: LeaveInfo) => {
  currentLeave.value = row
  returnDate.value = new Date().toISOString().split('T')[0]
  returnDialogVisible.value = true
}

// 确认销假
const confirmReturn = async () => {
  if (!currentLeave.value || !returnDate.value) return
  
  try {
    await leaveApi.returnFromLeave(currentLeave.value.id, returnDate.value)
    ElMessage.success('销假成功')
    returnDialogVisible.value = false
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

// 获取状态文本和类型
const getStatusInfo = (status: string): { text: string; type: 'success' | 'warning' | 'info' | 'danger' } => {
  const map: Record<string, { text: string; type: 'success' | 'warning' | 'info' | 'danger' }> = {
    PENDING: { text: '待审批', type: 'warning' },
    COUNSELOR_APPROVED: { text: '辅导员已批', type: 'info' },
    APPROVED: { text: '已批准', type: 'success' },
    REJECTED: { text: '已拒绝', type: 'danger' },
    COMPLETED: { text: '已销假', type: 'info' }
  }
  return map[status] || { text: status, type: 'info' }
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="leave-list-container" v-loading="loading">
    <el-card>
      <template #header>
        <span>我的请假记录</span>
      </template>
      
      <el-table :data="leaves" stripe>
        <el-table-column prop="leaveType" label="类型" width="80">
          <template #default="{ row }">
            {{ row.leaveTypeText || getTypeText(row.leaveType) }}
          </template>
        </el-table-column>
        <el-table-column prop="startDate" label="开始日期" min-width="120" />
        <el-table-column prop="endDate" label="结束日期" min-width="120" />
        <el-table-column prop="duration" label="天数" width="70" />
        <el-table-column prop="reason" label="原因" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusInfo(row.status).type" size="small">
              {{ getStatusInfo(row.status).text }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="申请时间" min-width="160" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewDetail(row)">详情</el-button>
            <el-button 
              v-if="row.status === 'APPROVED'" 
              type="success" 
              link 
              size="small" 
              @click="handleReturn(row)"
            >
              销假
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="请假详情" width="600px">
      <el-descriptions v-if="currentLeave" :column="2" border>
        <el-descriptions-item label="请假类型">
          {{ currentLeave.leaveTypeText || getTypeText(currentLeave.leaveType) }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusInfo(currentLeave.status).type">
            {{ getStatusInfo(currentLeave.status).text }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="开始日期">{{ currentLeave.startDate }}</el-descriptions-item>
        <el-descriptions-item label="结束日期">{{ currentLeave.endDate }}</el-descriptions-item>
        <el-descriptions-item label="请假天数">{{ currentLeave.duration }} 天</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ currentLeave.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="请假原因" :span="2">{{ currentLeave.reason }}</el-descriptions-item>
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
      
      <!-- 审批历史 -->
      <div v-if="currentLeave?.approvalHistory?.length" class="approval-history">
        <h4>审批历史</h4>
        <el-timeline>
          <el-timeline-item
            v-for="record in currentLeave.approvalHistory"
            :key="record.id"
            :type="record.approved ? 'success' : 'danger'"
          >
            <p>{{ record.approverName }} ({{ record.approverRole }})</p>
            <p>{{ record.approved ? '批准' : '拒绝' }}: {{ record.comment }}</p>
            <p class="time">{{ record.createdAt }}</p>
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-dialog>
    
    <!-- 销假对话框 -->
    <el-dialog v-model="returnDialogVisible" title="销假" width="400px">
      <el-form>
        <el-form-item label="返校日期">
          <el-date-picker
            v-model="returnDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="returnDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmReturn">确认销假</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.leave-list-container {
}

.attachments-section {
  margin-top: 20px;
  
  h4 {
    margin-bottom: 10px;
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

.approval-history {
  margin-top: 20px;
  
  h4 {
    margin-bottom: 10px;
  }
  
  .time {
    font-size: 12px;
    color: #909399;
  }
}
</style>
