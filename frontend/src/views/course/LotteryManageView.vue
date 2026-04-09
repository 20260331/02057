<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Medal } from '@element-plus/icons-vue'
import { courseApi, type LotteryCourse, type SelectionPhaseInfo } from '@/api/course'

const loading = ref(false)
const executing = ref(false)
const pendingCourses = ref<LotteryCourse[]>([])
const currentPhaseInfo = ref<SelectionPhaseInfo | null>(null)

const phaseText = computed(() => currentPhaseInfo.value?.currentPhaseName || '未知')

const phaseTagType = computed(() => {
  if (!currentPhaseInfo.value) return 'info'
  const map: Record<string, string> = {
    NOT_STARTED: 'info',
    PRE_SELECTION: 'warning',
    FORMAL_SELECTION: 'success',
    ADJUSTMENT: '',
    CLOSED: 'danger'
  }
  return map[currentPhaseInfo.value.currentPhase] || 'info'
})

const getCategoryText = (category: string) => {
  const map: Record<string, string> = {
    REQUIRED: '必修',
    ELECTIVE: '选修',
    GENERAL: '通识'
  }
  return map[category] || category
}

const loadData = async () => {
  loading.value = true
  try {
    const [phaseRes, coursesRes] = await Promise.all([
      courseApi.getPhaseInfo(),
      courseApi.getLotteryPendingCourses()
    ])
    if (phaseRes.code === 200) {
      currentPhaseInfo.value = phaseRes.data
    }
    if (coursesRes.code === 200) {
      pendingCourses.value = coursesRes.data
    }
  } catch (error) {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

const handleExecuteLottery = async (course: LotteryCourse) => {
  try {
    await ElMessageBox.confirm(
      `确定对课程「${course.courseName}」(${course.courseCode}) 执行抽签吗？\n` +
      `当前待抽签人数：${course.pendingCount}，剩余名额：${course.capacity - course.enrolledCount}`,
      '确认执行抽签',
      { type: 'warning', confirmButtonText: '执行抽签', cancelButtonText: '取消' }
    )

    executing.value = true
    const res = await courseApi.executeLottery(course.courseId)
    if (res.code === 200) {
      ElMessage.success(`课程「${course.courseName}」抽签完成`)
      loadData()
    }
  } catch (error) {
    // user cancelled or error
  } finally {
    executing.value = false
  }
}

const handleExecuteAll = async () => {
  if (pendingCourses.value.length === 0) {
    ElMessage.info('没有需要抽签的课程')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定对所有 ${pendingCourses.value.length} 门课程执行批量抽签吗？此操作不可撤销。`,
      '确认批量抽签',
      { type: 'warning', confirmButtonText: '全部执行', cancelButtonText: '取消' }
    )

    executing.value = true
    const res = await courseApi.executeLotteryAll()
    if (res.code === 200) {
      ElMessage.success(`批量抽签完成，共处理 ${res.data.processedCount} 门课程`)
      loadData()
    }
  } catch (error) {
    // user cancelled or error
  } finally {
    executing.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="lottery-manage-container">
    <el-card class="status-card">
      <template #header>
        <div class="card-header">
          <span>选课抽签管理</span>
          <div>
            <el-button :icon="Refresh" @click="loadData" :loading="loading">刷新</el-button>
            <el-button
              type="danger"
              :icon="Medal"
              @click="handleExecuteAll"
              :loading="executing"
              :disabled="pendingCourses.length === 0"
            >
              批量执行抽签
            </el-button>
          </div>
        </div>
      </template>

      <el-descriptions :column="3" border class="status-info">
        <el-descriptions-item label="当前选课阶段">
          <el-tag :type="phaseTagType as any">{{ phaseText }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="需抽签课程数">
          <span class="highlight-num">{{ pendingCourses.length }}</span> 门
        </el-descriptions-item>
        <el-descriptions-item label="总待抽签人次">
          <span class="highlight-num">
            {{ pendingCourses.reduce((sum, c) => sum + c.pendingCount, 0) }}
          </span> 人次
        </el-descriptions-item>
      </el-descriptions>

      <el-alert
        v-if="currentPhaseInfo?.currentPhase === 'PRE_SELECTION'"
        title="当前处于预选阶段，建议在预选结束后再执行抽签"
        type="warning"
        :closable="false"
        show-icon
        style="margin-top: 16px"
      />
    </el-card>

    <el-card class="table-card">
      <template #header>
        <span>待抽签课程列表</span>
      </template>

      <el-empty v-if="pendingCourses.length === 0 && !loading" description="暂无需要抽签的课程" />

      <el-table v-else :data="pendingCourses" v-loading="loading" stripe>
        <el-table-column prop="courseCode" label="课程编号" width="120" />
        <el-table-column prop="courseName" label="课程名称" min-width="150" />
        <el-table-column prop="credits" label="学分" width="70" />
        <el-table-column prop="teacherName" label="授课教师" width="100" />
        <el-table-column prop="schedule" label="上课时间" width="110" />
        <el-table-column label="类别" width="80">
          <template #default="{ row }">
            <el-tag size="small">{{ getCategoryText(row.category) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="课程容量" width="90" align="center">
          <template #default="{ row }">
            {{ row.enrolledCount }} / {{ row.capacity }}
          </template>
        </el-table-column>
        <el-table-column label="待抽签人数" width="110" align="center">
          <template #default="{ row }">
            <el-tag type="danger" size="small">{{ row.pendingCount }} 人</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="剩余名额" width="90" align="center">
          <template #default="{ row }">
            <span :class="{ 'text-danger': row.capacity - row.enrolledCount <= 0 }">
              {{ Math.max(0, row.capacity - row.enrolledCount) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="中签率" width="90" align="center">
          <template #default="{ row }">
            <span v-if="row.pendingCount > 0">
              {{ Math.min(100, Math.round((row.capacity - row.enrolledCount) / row.pendingCount * 100)) }}%
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              :icon="Medal"
              @click="handleExecuteLottery(row)"
              :loading="executing"
            >
              执行抽签
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card class="tips-card">
      <template #header>
        <span>抽签说明</span>
      </template>
      <div class="tips-content">
        <p><strong>抽签机制说明：</strong></p>
        <ol>
          <li>在<strong>预选阶段</strong>，若课程报名人数超出容量上限，超出的学生将自动进入"待抽签"状态</li>
          <li>预选阶段结束后，管理员可以对需要抽签的课程逐一执行抽签或批量执行</li>
          <li>抽签采用<strong>随机算法</strong>，在所有待抽签学生中随机抽取，直到填满剩余名额</li>
          <li>中签的学生状态变更为"已选"，未中签的学生状态变更为"抽签未中"</li>
          <li>抽签完成后进入<strong>正选阶段</strong>，未中签学生可以在剩余名额中先到先得</li>
        </ol>
      </div>
    </el-card>
  </div>
</template>

<style lang="scss" scoped>
.lottery-manage-container {
}

.status-card,
.table-card,
.tips-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.highlight-num {
  font-size: 20px;
  font-weight: bold;
  color: #409eff;
}

.text-danger {
  color: #f56c6c;
  font-weight: bold;
}

.tips-content {
  color: #606266;
  line-height: 1.8;

  ol {
    padding-left: 20px;

    li {
      margin-bottom: 6px;
    }
  }
}
</style>
