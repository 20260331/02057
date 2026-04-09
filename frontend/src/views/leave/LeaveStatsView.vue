<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { leaveApi, type LeaveStatistics } from '@/api/leave'

const loading = ref(false)
const stats = ref<LeaveStatistics | null>(null)
const dateRange = ref<[string, string] | null>(null)

const loadData = async () => {
  loading.value = true
  try {
    const params: { startDate?: string; endDate?: string } = {}
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    const res = await leaveApi.getStatistics(params)
    if (res.code === 200) {
      stats.value = res.data
    }
  } catch (error) {
    ElMessage.error('加载请假统计失败')
  } finally {
    loading.value = false
  }
}

const handleRangeChange = () => {
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="leave-stats-container" v-loading="loading">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>请假统计与分析</span>
          <div class="header-actions">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              @change="handleRangeChange"
              style="width: 260px"
            />
          </div>
        </div>
      </template>

      <el-row :gutter="16" class="summary-row" v-if="stats">
        <el-col :xs="12" :sm="8" :md="4">
          <el-card shadow="hover" class="summary-card">
            <div class="summary-value">{{ stats.totalLeaves }}</div>
            <div class="summary-label">请假总数</div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="8" :md="4">
          <el-card shadow="hover" class="summary-card">
            <div class="summary-value">{{ stats.totalDays }}</div>
            <div class="summary-label">总请假天数</div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="8" :md="4">
          <el-card shadow="hover" class="summary-card">
            <div class="summary-value">{{ stats.pendingLeaves }}</div>
            <div class="summary-label">待审批</div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="8" :md="4">
          <el-card shadow="hover" class="summary-card">
            <div class="summary-value">{{ stats.approvedLeaves }}</div>
            <div class="summary-label">已批准</div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="8" :md="4">
          <el-card shadow="hover" class="summary-card">
            <div class="summary-value urgent">{{ stats.urgentLeaves }}</div>
            <div class="summary-label">紧急请假</div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="8" :md="4">
          <el-card shadow="hover" class="summary-card">
            <div class="summary-value">{{ stats.rejectedLeaves }}</div>
            <div class="summary-label">已拒绝</div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="16" class="summary-row" v-if="stats">
        <el-col :xs="12" :sm="8" :md="6">
          <el-card shadow="hover" class="summary-card warning">
            <div class="summary-value">{{ stats.todayOnLeaveCount }}</div>
            <div class="summary-label">今日请假（缺勤人数）</div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="8" :md="6">
          <el-card shadow="hover" class="summary-card danger">
            <div class="summary-value">{{ stats.overdueReturnCount }}</div>
            <div class="summary-label">未按时销假记录</div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="16" class="tables-row" v-if="stats">
        <el-col :xs="24" :md="12">
          <el-card>
            <template #header>
              <span>按请假类型统计</span>
            </template>
            <el-table :data="stats.byType" size="small">
              <el-table-column prop="leaveTypeText" label="类型" />
              <el-table-column prop="leaveCount" label="请假次数" width="100" />
              <el-table-column prop="totalDays" label="总天数" width="100" />
            </el-table>
          </el-card>
        </el-col>

        <el-col :xs="24" :md="12">
          <el-card>
            <template #header>
              <span>按院系统计</span>
            </template>
            <el-table :data="stats.byDepartment" size="small">
              <el-table-column prop="department" label="院系" />
              <el-table-column prop="leaveCount" label="请假次数" width="100" />
              <el-table-column prop="studentCount" label="涉及学生数" width="110" />
              <el-table-column prop="totalDays" label="总天数" width="100" />
            </el-table>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<style lang="scss" scoped>
.leave-stats-container {
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.summary-row {
  margin-bottom: 16px;
}

.summary-card {
  text-align: center;

  .summary-value {
    font-size: 24px;
    font-weight: bold;
    color: #303133;
  }

  .summary-label {
    margin-top: 4px;
    font-size: 13px;
    color: #909399;
  }

  &.warning .summary-value {
    color: #faad14;
  }

  &.danger .summary-value {
    color: #ff4d4f;
  }

  .urgent {
    color: #ff4d4f;
  }
}

.tables-row {
  margin-top: 8px;
}
</style>

