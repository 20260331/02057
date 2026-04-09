<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { courseApi, type RosterItem } from '@/api/course'
import { gradeApi, type GradeStatistics, type GradeChangeLog } from '@/api/grade'
import type { CourseInfo, GradeInfo } from '@/api/types'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const loading = ref(false)
const courses = ref<CourseInfo[]>([])
const selectedCourseId = ref<number | null>(null)
const selectedCourse = ref<CourseInfo | null>(null)
const roster = ref<RosterItem[]>([])
const grades = ref<GradeInfo[]>([])
const statistics = ref<GradeStatistics | null>(null)
const changeLogs = ref<GradeChangeLog[]>([])
const activeTab = ref('entry')

// 成绩录入表单
const gradeForm = ref<Array<{ 
  id?: number
  studentId: number
  studentNo: string
  studentName: string
  score: number | null
  status?: string
  statusText?: string
}>>([])

// 单个成绩修改对话框
const editDialogVisible = ref(false)
const editForm = ref({
  id: 0,
  studentNo: '',
  studentName: '',
  oldScore: 0,
  newScore: 0,
  reason: ''
})

// 判断是否为管理员或教务处
const isAdmin = computed(() => {
  return authStore.roles?.some(r => r === 'ADMIN' || r === 'ACADEMIC_AFFAIRS')
})

// 是否有待审批的成绩
const hasPendingApproval = computed(() => {
  return grades.value.some(g => g.status === 'SUBMITTED')
})

// 加载课程
const loadCourses = async () => {
  try {
    let res
    if (isAdmin.value) {
      res = await courseApi.queryCourses({ page: 1, size: 100 })
      if (res.code === 200) {
        courses.value = res.data.records
      }
    } else {
      res = await courseApi.getTeacherCourses()
      if (res.code === 200) {
        courses.value = res.data
      }
    }
  } catch (error) {
    // 错误已在拦截器处理
  }
}

// 选择课程
const handleCourseChange = async () => {
  if (!selectedCourseId.value) {
    roster.value = []
    gradeForm.value = []
    grades.value = []
    statistics.value = null
    changeLogs.value = []
    selectedCourse.value = null
    return
  }
  
  selectedCourse.value = courses.value.find(c => c.id === selectedCourseId.value) || null
  
  loading.value = true
  try {
    const [rosterRes, gradesRes] = await Promise.all([
      courseApi.getCourseRoster(selectedCourseId.value),
      gradeApi.getCourseGrades(selectedCourseId.value)
    ])
    
    if (rosterRes.code === 200) {
      roster.value = rosterRes.data
    }
    
    if (gradesRes.code === 200) {
      grades.value = gradesRes.data
      
      gradeForm.value = rosterRes.data.map(r => {
        const existingGrade = gradesRes.data?.find(g => g.studentId === r.studentId)
        return {
          id: existingGrade?.id,
          studentId: r.studentId,
          studentNo: r.studentNo,
          studentName: r.studentName,
          score: existingGrade?.score ?? null,
          status: existingGrade?.status,
          statusText: existingGrade?.statusText
        }
      })
    }
    
    await Promise.all([loadStatistics(), loadChangeLogs()])
  } catch (error) {
    // 错误已在拦截器处理
  } finally {
    loading.value = false
  }
}

// 加载统计数据
const loadStatistics = async () => {
  if (!selectedCourseId.value) return
  try {
    const res = await gradeApi.getStatistics(selectedCourseId.value)
    if (res.code === 200) {
      statistics.value = res.data
    }
  } catch (error) {
    // 忽略
  }
}

// 加载修改记录
const loadChangeLogs = async () => {
  if (!selectedCourseId.value) return
  try {
    const res = await gradeApi.getChangeLogs(selectedCourseId.value)
    if (res.code === 200) {
      changeLogs.value = res.data
    }
  } catch (error) {
    // 忽略
  }
}

// 保存成绩
const handleSave = async () => {
  if (!selectedCourseId.value) {
    ElMessage.warning('请先选择课程')
    return
  }
  
  console.log('gradeForm:', gradeForm.value)
  
  const validGrades = gradeForm.value
    .filter(g => g.score !== null && g.score >= 0 && g.score <= 100)
    .map(g => ({ studentId: g.studentId, score: g.score! }))
  
  console.log('validGrades:', validGrades)
  
  if (validGrades.length === 0) {
    ElMessage.warning('没有有效的成绩数据')
    return
  }
  
  try {
    const requestData = {
      courseId: selectedCourseId.value,
      grades: validGrades
    }
    console.log('发送请求:', requestData)
    
    await gradeApi.batchSaveGrades(requestData)
    ElMessage.success('保存成功')
    handleCourseChange()
  } catch (error) {
    console.error('保存失败:', error)
  }
}

// 提交成绩
const handleSubmit = async () => {
  if (!selectedCourseId.value) return
  
  try {
    await ElMessageBox.confirm('提交后成绩将进入审核流程，确定要提交吗？', '确认提交', { type: 'warning' })
    
    // 获取有效成绩
    const validGrades = gradeForm.value
      .filter(g => g.score !== null && g.score >= 0 && g.score <= 100)
      .map(g => ({ studentId: g.studentId, score: g.score! }))
    
    if (validGrades.length === 0) {
      ElMessage.warning('没有有效的成绩数据')
      return
    }
    
    // 使用新的一步完成保存和提交的 API
    await gradeApi.batchSaveAndSubmitGrades({
      courseId: selectedCourseId.value,
      grades: validGrades
    })
    
    ElMessage.success('提交成功，等待审核')
    handleCourseChange()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('提交失败:', error)
    }
  }
}

// 审批成绩
const handleApprove = async () => {
  if (!selectedCourseId.value) return
  
  try {
    await ElMessageBox.confirm('确定要审批通过该课程的所有成绩吗？', '确认审批', { type: 'warning' })
    await gradeApi.approveGrades(selectedCourseId.value)
    ElMessage.success('审批成功')
    handleCourseChange()
  } catch (error) {
    // 用户取消或错误
  }
}

// 打开单个成绩修改对话框
const handleEditGrade = (row: typeof gradeForm.value[0]) => {
  if (!row.id) {
    ElMessage.warning('该学生尚未录入成绩')
    return
  }
  editForm.value = {
    id: row.id,
    studentNo: row.studentNo,
    studentName: row.studentName,
    oldScore: row.score || 0,
    newScore: row.score || 0,
    reason: ''
  }
  editDialogVisible.value = true
}

// 提交成绩修改
const submitEditGrade = async () => {
  if (!editForm.value.reason.trim()) {
    ElMessage.warning('请填写修改原因')
    return
  }
  if (editForm.value.newScore < 0 || editForm.value.newScore > 100) {
    ElMessage.warning('分数必须在 0-100 之间')
    return
  }
  
  try {
    await gradeApi.updateGrade(editForm.value.id, {
      score: editForm.value.newScore,
      reason: editForm.value.reason
    })
    ElMessage.success('修改成功')
    editDialogVisible.value = false
    handleCourseChange()
  } catch (error) {
    // 错误已在拦截器处理
  }
}

// 获取状态标签类型
const getStatusType = (status?: string) => {
  const map: Record<string, 'info' | 'warning' | 'success'> = {
    DRAFT: 'info',
    SUBMITTED: 'warning',
    APPROVED: 'success'
  }
  return map[status || ''] || 'info'
}

// 获取等级颜色
const getGradeColor = (score: number | null | string) => {
  if (score === null) return '#909399'
  const s = typeof score === 'string' ? (score === 'A' ? 90 : score === 'B' ? 80 : score === 'C' ? 70 : score === 'D' ? 60 : 50) : score
  if (s >= 90) return '#67c23a'
  if (s >= 80) return '#409eff'
  if (s >= 70) return '#e6a23c'
  if (s >= 60) return '#f56c6c'
  return '#909399'
}

// 获取等级
const getLetterGrade = (score: number | null) => {
  if (score === null) return '-'
  if (score >= 90) return 'A'
  if (score >= 80) return 'B'
  if (score >= 70) return 'C'
  if (score >= 60) return 'D'
  return 'F'
}

// 格式化时间
const formatTime = (time: string) => {
  return time ? time.replace('T', ' ').substring(0, 19) : ''
}

onMounted(() => {
  loadCourses()
})
</script>

<template>
  <div class="grade-entry-container">
    <!-- 课程选择 -->
    <el-card class="select-card">
      <el-form inline>
        <el-form-item label="选择课程">
          <el-select v-model="selectedCourseId" placeholder="请选择课程" @change="handleCourseChange" style="width: 300px" filterable>
            <el-option 
              v-for="course in courses" 
              :key="course.id" 
              :label="`${course.courseCode} - ${course.name} (${course.semester})`" 
              :value="course.id" 
            />
          </el-select>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 标签页 -->
    <el-card v-if="selectedCourseId" v-loading="loading">
      <el-tabs v-model="activeTab">
        <!-- 成绩录入 -->
        <el-tab-pane label="成绩录入" name="entry">
          <div class="toolbar">
            <div class="course-info" v-if="selectedCourse">
              <span>{{ selectedCourse.name }}</span>
              <el-tag size="small" style="margin-left: 8px">{{ selectedCourse.semester }}</el-tag>
              <span style="margin-left: 16px; color: #909399;">授课教师: {{ selectedCourse.teacherName }}</span>
            </div>
            <div class="actions">
              <el-button type="primary" @click="handleSave">保存草稿</el-button>
              <el-button type="success" @click="handleSubmit">提交审核</el-button>
              <el-button v-if="isAdmin && hasPendingApproval" type="warning" @click="handleApprove">审批通过</el-button>
            </div>
          </div>
          
          <el-table :data="gradeForm" stripe border style="margin-top: 16px">
            <el-table-column prop="studentNo" label="学号" min-width="120" />
            <el-table-column prop="studentName" label="姓名" min-width="100" />
            <el-table-column label="分数" min-width="150">
              <template #default="{ row }">
                <el-input-number 
                  v-model="row.score" 
                  :min="0" 
                  :max="100" 
                  :precision="1"
                  :disabled="row.status === 'APPROVED'"
                  size="small"
                />
              </template>
            </el-table-column>
            <el-table-column label="等级" width="80" align="center">
              <template #default="{ row }">
                <span :style="{ color: getGradeColor(row.score), fontWeight: 'bold' }">
                  {{ getLetterGrade(row.score) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.status" :type="getStatusType(row.status)" size="small">
                  {{ row.statusText || row.status }}
                </el-tag>
                <span v-else style="color: #909399">未录入</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" align="center">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="handleEditGrade(row)" :disabled="!row.id">调整</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        
        <!-- 成绩统计 -->
        <el-tab-pane label="成绩统计" name="statistics">
          <div v-if="statistics && statistics.totalStudents > 0">
            <el-row :gutter="20" class="stat-row">
              <el-col :span="6">
                <div class="stat-item">
                  <div class="stat-value">{{ statistics.totalStudents }}</div>
                  <div class="stat-label">总人数</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="stat-item">
                  <div class="stat-value">{{ statistics.averageScore?.toFixed(1) }}</div>
                  <div class="stat-label">平均分</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="stat-item">
                  <div class="stat-value">{{ statistics.passRate?.toFixed(1) }}%</div>
                  <div class="stat-label">及格率</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="stat-item">
                  <div class="stat-value">{{ statistics.excellentRate?.toFixed(1) }}%</div>
                  <div class="stat-label">优秀率</div>
                </div>
              </el-col>
            </el-row>
            
            <el-row :gutter="20" style="margin-top: 20px">
              <el-col :span="12">
                <el-card shadow="never">
                  <template #header>分数范围</template>
                  <div class="score-range">
                    <div class="range-item">
                      <span class="range-label">最高分</span>
                      <span class="range-value high">{{ statistics.maxScore }}</span>
                    </div>
                    <div class="range-item">
                      <span class="range-label">最低分</span>
                      <span class="range-value low">{{ statistics.minScore }}</span>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="12">
                <el-card shadow="never">
                  <template #header>等级分布</template>
                  <div class="grade-distribution">
                    <div v-for="(count, grade) in statistics.distribution" :key="grade" class="dist-item">
                      <span class="dist-grade" :style="{ color: getGradeColor(grade) }">{{ grade }}</span>
                      <el-progress :percentage="Math.round(count / statistics.totalStudents * 100)" :stroke-width="16" :show-text="false" />
                      <span class="dist-count">{{ count }}人</span>
                    </div>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </div>
          <el-empty v-else description="暂无统计数据，请先录入并审批成绩" />
        </el-tab-pane>
        
        <!-- 修改记录 -->
        <el-tab-pane label="修改记录" name="logs">
          <el-table :data="changeLogs" stripe v-if="changeLogs.length > 0">
            <el-table-column prop="studentNo" label="学号" width="120" />
            <el-table-column prop="studentName" label="姓名" width="100" />
            <el-table-column label="原成绩" width="80" align="center">
              <template #default="{ row }">{{ row.oldScore }}</template>
            </el-table-column>
            <el-table-column label="新成绩" width="80" align="center">
              <template #default="{ row }">{{ row.newScore }}</template>
            </el-table-column>
            <el-table-column prop="reason" label="修改原因" min-width="200" />
            <el-table-column prop="operatorName" label="操作人" width="100" />
            <el-table-column label="操作时间" width="170">
              <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无修改记录" />
        </el-tab-pane>
      </el-tabs>
    </el-card>
    
    <el-empty v-else-if="courses.length === 0" description="暂无可管理的课程" />
    <el-card v-else>
      <el-empty description="请先选择课程" />
    </el-card>
    
    <!-- 成绩修改对话框 -->
    <el-dialog v-model="editDialogVisible" title="调整成绩" width="450px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="学号">
          <el-input v-model="editForm.studentNo" disabled />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="editForm.studentName" disabled />
        </el-form-item>
        <el-form-item label="原成绩">
          <el-input-number v-model="editForm.oldScore" disabled />
        </el-form-item>
        <el-form-item label="新成绩">
          <el-input-number v-model="editForm.newScore" :min="0" :max="100" :precision="1" />
        </el-form-item>
        <el-form-item label="修改原因">
          <el-input v-model="editForm.reason" type="textarea" :rows="3" placeholder="请填写修改原因（必填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEditGrade">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.grade-entry-container {
}

.select-card {
  margin-bottom: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .course-info {
    font-size: 16px;
    font-weight: 500;
  }
}

.stat-row {
  .stat-item {
    text-align: center;
    padding: 20px;
    background: #f5f7fa;
    border-radius: 8px;
    
    .stat-value {
      font-size: 32px;
      font-weight: bold;
      color: #409eff;
    }
    
    .stat-label {
      font-size: 14px;
      color: #909399;
      margin-top: 8px;
    }
  }
}

.score-range {
  display: flex;
  justify-content: space-around;
  
  .range-item {
    text-align: center;
    
    .range-label {
      display: block;
      color: #909399;
      margin-bottom: 8px;
    }
    
    .range-value {
      font-size: 28px;
      font-weight: bold;
      
      &.high { color: #67c23a; }
      &.low { color: #f56c6c; }
    }
  }
}

.grade-distribution {
  .dist-item {
    display: flex;
    align-items: center;
    margin-bottom: 12px;
    
    .dist-grade {
      width: 30px;
      font-weight: bold;
      font-size: 16px;
    }
    
    .el-progress {
      flex: 1;
      margin: 0 12px;
    }
    
    .dist-count {
      width: 50px;
      text-align: right;
      color: #606266;
    }
  }
}
</style>
