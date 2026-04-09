<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { statusChangeApi, type StatusChangeInfo } from '@/api/statusChange'

const loading = ref(false)
const applications = ref<StatusChangeInfo[]>([])
const detailDialogVisible = ref(false)
const currentApp = ref<StatusChangeInfo | null>(null)

const loadData = async () => {
  loading.value = true
  try {
    const res = await statusChangeApi.getMyApplications()
    if (res.code === 200) {
      applications.value = res.data
    }
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

const showDetail = async (row: StatusChangeInfo) => {
  try {
    const res = await statusChangeApi.getApplicationDetail(row.id)
    if (res.code === 200) {
      currentApp.value = res.data
      detailDialogVisible.value = true
    }
  } catch {
    // handled by interceptor
  }
}

const handleCancel = async (row: StatusChangeInfo) => {
  try {
    await ElMessageBox.confirm('确定要取消该学籍异动申请吗？', '确认取消', {
      type: 'warning'
    })
    await statusChangeApi.cancelApplication(row.id)
    ElMessage.success('已取消申请')
    loadData()
  } catch {
    // cancelled or error
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
  <div class="status-change-list" v-loading="loading">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的学籍异动申请</span>
          <el-button type="primary" size="small" @click="$router.push('/student/status-change/apply')">
            新建申请
          </el-button>
        </div>
      </template>

      <el-table :data="applications" stripe>
        <el-table-column prop="createdAt" label="申请时间" width="170" />
        <el-table-column label="当前状态" width="90">
          <template #default="{ row }">{{ row.currentStatusText }}</template>
        </el-table-column>
        <el-table-column label="目标状态" width="90">
          <template #default="{ row }">{{ row.targetStatusText }}</template>
        </el-table-column>
        <el-table-column prop="effectiveDate" label="期望生效日期" width="130" />
        <el-table-column prop="reason" label="申请原因" show-overflow-tooltip />
        <el-table-column label="审批状态" width="110">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ row.statusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="showDetail(row)">详情</el-button>
            <el-button
              v-if="row.status === 'PENDING'"
              type="danger" link size="small"
              @click="handleCancel(row)"
            >取消</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="applications.length === 0" description="暂无学籍异动申请记录" />
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="申请详情" width="600px">
      <template v-if="currentApp">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="学生">{{ currentApp.studentName }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ currentApp.studentNo }}</el-descriptions-item>
          <el-descriptions-item label="院系">{{ currentApp.department }}</el-descriptions-item>
          <el-descriptions-item label="班级">{{ currentApp.classNo }}</el-descriptions-item>
          <el-descriptions-item label="当前状态">{{ currentApp.currentStatusText }}</el-descriptions-item>
          <el-descriptions-item label="目标状态">{{ currentApp.targetStatusText }}</el-descriptions-item>
          <el-descriptions-item label="期望生效日期">{{ currentApp.effectiveDate }}</el-descriptions-item>
          <el-descriptions-item label="审批状态">
            <el-tag :type="getStatusType(currentApp.status)" size="small">{{ currentApp.statusText }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请原因" :span="2">{{ currentApp.reason }}</el-descriptions-item>
          <el-descriptions-item label="申请时间" :span="2">{{ currentApp.createdAt }}</el-descriptions-item>
        </el-descriptions>

        <!-- 审批历史 -->
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
      </template>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.status-change-list {
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.approval-history {
  margin-top: 20px;

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
</style>
