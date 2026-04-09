<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { courseApi, type ScheduleItem, type CourseSelection, type SelectionPhaseInfo } from '@/api/course'

const loading = ref(false)
const scheduleData = ref<ScheduleItem[]>([])
const selectionData = ref<CourseSelection[]>([])
const phaseInfo = ref<SelectionPhaseInfo | null>(null)

const weekDays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

const sections = [
  { label: '第1-2节', start: 1, end: 2 },
  { label: '第3-4节', start: 3, end: 4 },
  { label: '第5-6节', start: 5, end: 6 },
  { label: '第7-8节', start: 7, end: 8 },
  { label: '第9-10节', start: 9, end: 10 }
]

const phaseTagType = computed(() => {
  if (!phaseInfo.value) return 'info'
  const map: Record<string, string> = {
    NOT_STARTED: 'info',
    PRE_SELECTION: 'warning',
    FORMAL_SELECTION: 'success',
    ADJUSTMENT: '',
    CLOSED: 'danger'
  }
  return map[phaseInfo.value.currentPhase] || 'info'
})

const loadSchedule = async () => {
  loading.value = true
  try {
    const [scheduleRes, selectionRes, phaseRes] = await Promise.all([
      courseApi.getMySchedule(),
      courseApi.getMySelections(),
      courseApi.getPhaseInfo()
    ])
    
    if (scheduleRes.code === 200) {
      scheduleData.value = scheduleRes.data
    }
    if (selectionRes.code === 200) {
      selectionData.value = selectionRes.data
    }
    if (phaseRes.code === 200) {
      phaseInfo.value = phaseRes.data
    }
  } catch (error) {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}

// 获取指定时间段的课程
const getCourseAt = (dayOfWeek: number, startSection: number) => {
  return scheduleData.value.find(
    item => item.dayOfWeek === dayOfWeek && item.startSection === startSection
  )
}

// 退课
const handleWithdraw = async (selection: CourseSelection) => {
  try {
    await ElMessageBox.confirm(`确定要退选课程"${selection.courseName}"吗？`, '确认退课')
    await courseApi.withdrawCourse(selection.courseId)
    ElMessage.success('退课成功')
    loadSchedule()
  } catch (error) {
    // 用户取消
  }
}

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    SELECTED: '已选',
    WITHDRAWN: '已退',
    LOTTERY_PENDING: '待抽签',
    LOTTERY_FAILED: '未中签'
  }
  return map[status] || status
}

const getStatusTagType = (status: string) => {
  const map: Record<string, string> = {
    SELECTED: 'success',
    WITHDRAWN: 'info',
    LOTTERY_PENDING: 'warning',
    LOTTERY_FAILED: 'danger'
  }
  return map[status] || 'info'
}

const totalCredits = computed(() => {
  return selectionData.value
    .filter(s => s.status === 'SELECTED')
    .reduce((sum, s) => sum + (s.credits || 0), 0)
})

onMounted(() => {
  loadSchedule()
})
</script>

<template>
  <div class="schedule-container" v-loading="loading">
    <!-- 选课阶段提示 -->
    <el-alert
      v-if="phaseInfo"
      :title="`当前选课阶段：${phaseInfo.currentPhaseName}`"
      :description="phaseInfo.selectionMode"
      :type="phaseInfo.canSelect ? 'success' : 'info'"
      :closable="true"
      show-icon
      class="phase-alert"
    />

    <!-- 课表视图 -->
    <el-card class="schedule-card">
      <template #header>
        <div class="card-header">
          <span>我的课表</span>
          <div>
            <el-tag v-if="phaseInfo" :type="phaseTagType as any" size="small" style="margin-right: 12px">
              {{ phaseInfo.currentPhaseName }}
            </el-tag>
            <span class="total-credits">总学分：{{ totalCredits }}</span>
          </div>
        </div>
      </template>
      
      <div class="schedule-table">
        <table>
          <thead>
            <tr>
              <th class="time-col">时间</th>
              <th v-for="(day, index) in weekDays" :key="index">{{ day }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="section in sections" :key="section.start">
              <td class="time-col">{{ section.label }}</td>
              <td v-for="dayIndex in 7" :key="dayIndex">
                <div 
                  v-if="getCourseAt(dayIndex, section.start)" 
                  class="course-cell"
                >
                  <div class="course-name">{{ getCourseAt(dayIndex, section.start)?.courseName }}</div>
                  <div class="course-info">{{ getCourseAt(dayIndex, section.start)?.location }}</div>
                  <div class="course-info">{{ getCourseAt(dayIndex, section.start)?.teacherName }}</div>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </el-card>
    
    <!-- 已选课程列表 -->
    <el-card class="selection-card">
      <template #header>
        <span>已选课程</span>
      </template>
      
      <el-table :data="selectionData" stripe>
        <el-table-column prop="courseCode" label="课程编号" width="120" />
        <el-table-column prop="courseName" label="课程名称" width="180" />
        <el-table-column prop="credits" label="学分" width="70" />
        <el-table-column prop="teacherName" label="授课教师" width="100" />
        <el-table-column prop="schedule" label="上课时间" width="120" />
        <el-table-column prop="location" label="上课地点" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button 
              v-if="(row.status === 'SELECTED' || row.status === 'LOTTERY_PENDING') && phaseInfo?.canWithdraw"
              type="danger" 
              link 
              size="small" 
              @click="handleWithdraw(row)"
            >
              {{ row.status === 'LOTTERY_PENDING' ? '取消报名' : '退课' }}
            </el-button>
            <span v-else-if="(row.status === 'SELECTED' || row.status === 'LOTTERY_PENDING') && !phaseInfo?.canWithdraw" class="text-muted">
              不在退课时段
            </span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<style lang="scss" scoped>
.schedule-container {
}

.phase-alert {
  margin-bottom: 16px;
}

.text-muted {
  color: #c0c4cc;
  font-size: 12px;
}

.schedule-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.total-credits {
  color: #409eff;
  font-weight: bold;
}

.schedule-table {
  overflow-x: auto;
  
  table {
    width: 100%;
    border-collapse: collapse;
    
    th, td {
      border: 1px solid #ebeef5;
      padding: 10px;
      text-align: center;
      min-width: 120px;
      height: 100px;
      vertical-align: top;
    }
    
    th {
      background: #f5f7fa;
      font-weight: bold;
    }
    
    .time-col {
      width: 100px;
      min-width: 100px;
      background: #f5f7fa;
    }
  }
}

.course-cell {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 4px;
  padding: 8px;
  height: 100%;
  
  .course-name {
    font-weight: bold;
    margin-bottom: 4px;
  }
  
  .course-info {
    font-size: 12px;
    opacity: 0.9;
  }
}

.selection-card {
  margin-top: 20px;
}
</style>
