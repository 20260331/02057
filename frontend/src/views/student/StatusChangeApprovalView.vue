<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { statusChangeApi, type StatusChangeInfo } from '@/api/statusChange'

const loading = ref(false)
const pendingList = ref<StatusChangeInfo[]>([])

const approvalDialogVisible = ref(false)
const currentApp = ref<StatusChangeInfo | null>(null)
const approvalForm = ref({
  approved: true,
  comment: ''
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await statusChangeApi.getPendingForCounselor()
    if (res.code === 200) {
      pendingList.value = res.data
    }
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

const handleApprove = async (row: StatusChangeInfo) => {
  try {
    const res = await statusChangeApi.getApplicationDetail(row.id)
    if (res.code === 200) {
      currentApp.value = res.data
      approvalForm.value = { approved: true, comment: '' }
      approvalDialogVisible.value = true
    }
  } catch {
    // handled by interceptor
  }
}

const submitApproval = async () => {
  if (!currentApp.value) return

  if (!approvalForm.value.comment) {
    ElMessage.warning('请填写审批意见')
    return
  }

  try {
    await statusChangeApi.approveApplication(currentApp.value.id, approvalForm.value)
    ElMessage.success('审批成功')
    approvalDialogVisible.value = false
    loadData()
  } catch {
    // handled by interceptor
  }
}

const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    PENDING: 'warning',
    COUNSELOR_APPROVED: '',
    APPROVED: 'success',
    REJECTED: 'danger',
    CANCELLED: 'info'
  }
  return map[status] || 'info'
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="status-change-approval" v-loading="loading">
    <el-card>
      <template #header>
        <span>学籍异动审批</span>
      </template>

      <el-table :data="pendingList" stripe>
        <el-table-column prop="studentName" label="学生姓名" width="100" />
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="department" label="院系" width="120" />
        <el-table-column prop="classNo" label="班级" width="100" />
        <el-table-column label="当前状态" width="90">
          <template #default="{ row }">{{ row.currentStatusText }}</template>
        </el-table-column>
        <el-table-column label="目标状态" width="90">
          <template #default="{ row }">
            <el-tag
              :type="['WITHDRAWN', 'TRANSFERRED'].includes(row.targetStatus) ? 'danger' : 'warning'"
              size="small"
            >{{ row.targetStatusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="effectiveDate" label="期望生效日期" width="130" />
        <el-table-column prop="reason" label="申请原因" show-overflow-tooltip />
        <el-table-column label="审批状态" width="110">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ row.statusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="申请时间" width="170" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleApprove(row)">审批</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="pendingList.length === 0" description="暂无待审批的学籍异动申请" />
    </el-card>

    <!-- 审批对话框 -->
    <el-dialog v-model="approvalDialogVisible" title="审批学籍异动" width="600px">
      <template v-if="currentApp">
        <el-descriptions :column="2" border class="app-info">
          <el-descriptions-item label="学生">{{ currentApp.studentName }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ currentApp.studentNo }}</el-descriptions-item>
          <el-descriptions-item label="院系">{{ currentApp.department }}</el-descriptions-item>
          <el-descriptions-item label="班级">{{ currentApp.classNo }}</el-descriptions-item>
          <el-descriptions-item label="当前状态">{{ currentApp.currentStatusText }}</el-descriptions-item>
          <el-descriptions-item label="目标状态">
            <el-tag
              :type="['WITHDRAWN', 'TRANSFERRED'].includes(currentApp.targetStatus) ? 'danger' : 'warning'"
              size="small"
            >{{ currentApp.targetStatusText }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="期望生效日期">{{ currentApp.effectiveDate }}</el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ currentApp.createdAt }}</el-descriptions-item>
          <el-descriptions-item label="申请原因" :span="2">{{ currentApp.reason }}</el-descriptions-item>
        </el-descriptions>

        <!-- 已有审批记录 -->
        <div class="approval-history" v-if="currentApp.approvalHistory && currentApp.approvalHistory.length > 0">
          <h4>审批记录</h4>
          <el-timeline>
            <el-timeline-item
              v-for="record in currentApp.approvalHistory"
              :key="record.id"
              :type="record.approved ? 'success' : 'danger'"
              :timestamp="record.createdAt"
            >
              <p>
                <strong>{{ record.approverName }}</strong>
                ({{ record.approverRole === 'COUNSELOR' ? '辅导员' : '教务处' }})
                <el-tag :type="record.approved ? 'success' : 'danger'" size="small" style="margin-left: 8px">
                  {{ record.approved ? '批准' : '拒绝' }}
                </el-tag>
              </p>
              <p class="approval-comment">{{ record.comment }}</p>
            </el-timeline-item>
          </el-timeline>
        </div>

        <el-alert
          v-if="currentApp.targetStatus === 'WITHDRAWN' || currentApp.targetStatus === 'TRANSFERRED'"
          :title="currentApp.status === 'PENDING'
            ? '该申请涉及退学/转学，辅导员审批通过后还需教务处审批'
            : '该申请已经辅导员审批通过，等待教务处最终审批'"
          type="info"
          :closable="false"
          show-icon
          style="margin: 16px 0"
        />

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
      </template>

      <template #footer>
        <el-button @click="approvalDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitApproval">提交审批</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.status-change-approval {
}

.app-info {
  margin-bottom: 16px;
}

.approval-history {
  margin: 16px 0;

  h4 {
    margin-bottom: 12px;
    font-size: 14px;
    color: #303133;
  }

  .approval-comment {
    color: #606266;
    font-size: 13px;
    margin-top: 4px;
  }
}

.approval-form {
  margin-top: 20px;
}
</style>
