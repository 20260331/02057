<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete } from '@element-plus/icons-vue'
import { courseApi, type CourseQuery, type CourseFormData, type SelectionPhaseInfo } from '@/api/course'
import type { CourseInfo } from '@/api/types'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(false)
const tableData = ref<CourseInfo[]>([])
const total = ref(0)
const phaseInfo = ref<SelectionPhaseInfo | null>(null)

// 判断是否有管理权限
const hasManagePermission = computed(() => {
  const roles = userStore.userInfo?.roles || []
  return roles.some(r => ['ADMIN', 'ACADEMIC_AFFAIRS'].includes(r))
})

const isStudent = computed(() => {
  const roles = userStore.userInfo?.roles || []
  return roles.includes('STUDENT')
})

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

const selectButtonText = computed(() => {
  if (!phaseInfo.value) return '选课'
  if (phaseInfo.value.currentPhase === 'PRE_SELECTION') return '预选报名'
  return '选课'
})

const queryForm = reactive<CourseQuery>({
  page: 1,
  size: 10,
  keyword: '',
  semester: '',
  category: ''
})

// 课程详情对话框
const detailDialogVisible = ref(false)
const currentCourse = ref<CourseInfo | null>(null)

// 课程编辑对话框
const editDialogVisible = ref(false)
const editDialogTitle = ref('添加课程')
const editingCourseId = ref<number | null>(null)
const courseForm = ref<CourseFormData>({
  courseCode: '',
  name: '',
  credits: 3,
  teacherName: '',
  schedule: '',
  location: '',
  capacity: 50,
  semester: '2024-2025-1',
  category: 'REQUIRED',
  department: '',
  description: ''
})

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const [courseRes, phaseRes] = await Promise.all([
      courseApi.queryCourses(queryForm),
      courseApi.getPhaseInfo()
    ])
    if (courseRes.code === 200) {
      tableData.value = courseRes.data.records
      total.value = courseRes.data.total
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

// 搜索
const handleSearch = () => {
  queryForm.page = 1
  loadData()
}

// 重置
const handleReset = () => {
  queryForm.keyword = ''
  queryForm.semester = ''
  queryForm.category = ''
  queryForm.page = 1
  loadData()
}

// 分页变化
const handlePageChange = (page: number) => {
  queryForm.page = page
  loadData()
}

// 查看详情
const handleViewDetail = (row: CourseInfo) => {
  currentCourse.value = row
  detailDialogVisible.value = true
}

// 选课
const handleSelect = async (row: CourseInfo) => {
  try {
    await ElMessageBox.confirm(`确定要选择课程"${row.name}"吗？`, '确认选课')
    
    const res = await courseApi.selectCourse(row.id)
    if (res.code === 200) {
      if (res.data.success) {
        if (res.data.status === 'LOTTERY_PENDING' || res.data.message?.includes('抽签')) {
          ElMessage.warning(res.data.message)
        } else {
          ElMessage.success(res.data.message || '选课成功')
        }
        loadData()
      } else {
        ElMessage.warning(res.data.message)
      }
    }
  } catch (error) {
    // 用户取消
  }
}

// 添加课程
const handleAdd = () => {
  editDialogTitle.value = '添加课程'
  editingCourseId.value = null
  courseForm.value = {
    courseCode: '',
    name: '',
    credits: 3,
    teacherName: '',
    schedule: '',
    location: '',
    capacity: 50,
    semester: '2024-2025-1',
    category: 'REQUIRED',
    department: '',
    description: ''
  }
  editDialogVisible.value = true
}

// 编辑课程
const handleEdit = (row: CourseInfo) => {
  editDialogTitle.value = '编辑课程'
  editingCourseId.value = row.id
  courseForm.value = {
    courseCode: row.courseCode,
    name: row.name,
    credits: row.credits,
    teacherName: row.teacherName || '',
    schedule: row.schedule || '',
    location: row.location || '',
    capacity: row.capacity,
    semester: row.semester,
    category: row.category,
    department: row.department || '',
    description: row.description || ''
  }
  editDialogVisible.value = true
}

// 保存课程
const handleSave = async () => {
  if (!courseForm.value.courseCode || !courseForm.value.name) {
    ElMessage.warning('请填写课程编号和名称')
    return
  }
  
  try {
    if (editingCourseId.value) {
      await courseApi.updateCourse(editingCourseId.value, courseForm.value)
      ElMessage.success('更新成功')
    } else {
      await courseApi.createCourse(courseForm.value)
      ElMessage.success('添加成功')
    }
    editDialogVisible.value = false
    loadData()
  } catch (error) {
    // 错误已在拦截器中处理
  }
}

// 删除课程
const handleDelete = async (row: CourseInfo) => {
  try {
    await ElMessageBox.confirm(`确定要删除课程"${row.name}"吗？`, '确认删除', { type: 'warning' })
    await courseApi.deleteCourse(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    // 用户取消或错误
  }
}

// 获取类别文本
const getCategoryText = (category: string) => {
  const map: Record<string, string> = {
    REQUIRED: '必修',
    ELECTIVE: '选修',
    GENERAL: '通识'
  }
  return map[category] || category
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="course-list-container">
    <!-- 选课阶段状态栏 -->
    <el-card v-if="phaseInfo && isStudent" class="phase-banner-card">
      <div class="phase-banner">
        <div class="phase-left">
          <el-tag :type="phaseTagType as any" size="large" effect="dark">{{ phaseInfo.currentPhaseName }}</el-tag>
          <span class="phase-desc">{{ phaseInfo.selectionMode }}</span>
        </div>
        <div v-if="phaseInfo.nextPhaseName && phaseInfo.nextPhaseStart" class="phase-right">
          <span class="next-phase-hint">下一阶段：{{ phaseInfo.nextPhaseName }} ({{ phaseInfo.nextPhaseStart?.substring(0, 16) }})</span>
        </div>
      </div>
    </el-card>

    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :model="queryForm" inline>
        <el-form-item label="关键词">
          <el-input v-model="queryForm.keyword" placeholder="课程名/编号/教师" clearable />
        </el-form-item>
        <el-form-item label="学期">
          <el-select v-model="queryForm.semester" placeholder="请选择" clearable style="width: 140px">
            <el-option label="2024-2025-1" value="2024-2025-1" />
            <el-option label="2024-2025-2" value="2024-2025-2" />
            <el-option label="2025-2026-1" value="2025-2026-1" />
          </el-select>
        </el-form-item>
        <el-form-item label="类别">
          <el-select v-model="queryForm.category" placeholder="请选择" clearable style="width: 100px">
            <el-option label="必修" value="REQUIRED" />
            <el-option label="选修" value="ELECTIVE" />
            <el-option label="通识" value="GENERAL" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          <el-button v-if="hasManagePermission" type="success" :icon="Plus" @click="handleAdd">添加课程</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 表格 -->
    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="courseCode" label="课程编号" min-width="120" />
        <el-table-column prop="name" label="课程名称" min-width="150" />
        <el-table-column prop="credits" label="学分" width="70" />
        <el-table-column prop="teacherName" label="授课教师" min-width="100" />
        <el-table-column prop="schedule" label="上课时间" min-width="110" />
        <el-table-column prop="location" label="上课地点" min-width="110" />
        <el-table-column prop="semester" label="学期" min-width="120" />
        <el-table-column label="容量" width="80">
          <template #default="{ row }">
            {{ row.enrolledCount }}/{{ row.capacity }}
          </template>
        </el-table-column>
        <el-table-column prop="category" label="类别" width="70">
          <template #default="{ row }">
            <el-tag size="small">{{ getCategoryText(row.category) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" :width="hasManagePermission ? 200 : 120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewDetail(row)">详情</el-button>
            <el-button 
              v-if="isStudent"
              type="success" 
              link 
              size="small" 
              @click="handleSelect(row)"
              :disabled="!phaseInfo?.canSelect || (phaseInfo?.currentPhase !== 'PRE_SELECTION' && row.availableCapacity <= 0)"
            >
              {{ selectButtonText }}
            </el-button>
            <template v-if="hasManagePermission">
              <el-button type="warning" link size="small" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
              <el-button type="danger" link size="small" :icon="Delete" @click="handleDelete(row)">删除</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <el-pagination
        class="pagination"
        v-model:current-page="queryForm.page"
        :page-size="queryForm.size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="handlePageChange"
      />
    </el-card>
    
    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="课程详情" width="600px">
      <el-descriptions v-if="currentCourse" :column="2" border>
        <el-descriptions-item label="课程编号">{{ currentCourse.courseCode }}</el-descriptions-item>
        <el-descriptions-item label="课程名称">{{ currentCourse.name }}</el-descriptions-item>
        <el-descriptions-item label="学分">{{ currentCourse.credits }}</el-descriptions-item>
        <el-descriptions-item label="授课教师">{{ currentCourse.teacherName }}</el-descriptions-item>
        <el-descriptions-item label="上课时间">{{ currentCourse.schedule }}</el-descriptions-item>
        <el-descriptions-item label="上课地点">{{ currentCourse.location }}</el-descriptions-item>
        <el-descriptions-item label="学期">{{ currentCourse.semester }}</el-descriptions-item>
        <el-descriptions-item label="类别">{{ getCategoryText(currentCourse.category) }}</el-descriptions-item>
        <el-descriptions-item label="课程容量">{{ currentCourse.capacity }}</el-descriptions-item>
        <el-descriptions-item label="已选人数">{{ currentCourse.enrolledCount }}</el-descriptions-item>
        <el-descriptions-item label="课程描述" :span="2">{{ currentCourse.description || '暂无' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
    
    <!-- 编辑对话框 -->
    <el-dialog v-model="editDialogVisible" :title="editDialogTitle" width="650px">
      <el-form :model="courseForm" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="课程编号" required>
              <el-input v-model="courseForm.courseCode" placeholder="如: CS101" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="课程名称" required>
              <el-input v-model="courseForm.name" placeholder="如: 计算机导论" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学分">
              <el-input-number v-model="courseForm.credits" :min="0.5" :max="10" :step="0.5" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="课程容量">
              <el-input-number v-model="courseForm.capacity" :min="1" :max="500" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="授课教师">
              <el-input v-model="courseForm.teacherName" placeholder="教师姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="开课院系">
              <el-input v-model="courseForm.department" placeholder="如: 计算机学院" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="上课时间">
              <el-input v-model="courseForm.schedule" placeholder="如: 周一1-2节" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="上课地点">
              <el-input v-model="courseForm.location" placeholder="如: 教学楼A101" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学期">
              <el-select v-model="courseForm.semester" style="width: 100%">
                <el-option label="2024-2025-1" value="2024-2025-1" />
                <el-option label="2024-2025-2" value="2024-2025-2" />
                <el-option label="2025-2026-1" value="2025-2026-1" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="课程类别">
              <el-select v-model="courseForm.category" style="width: 100%">
                <el-option label="必修" value="REQUIRED" />
                <el-option label="选修" value="ELECTIVE" />
                <el-option label="通识" value="GENERAL" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="课程描述">
          <el-input v-model="courseForm.description" type="textarea" :rows="3" placeholder="课程简介" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.course-list-container {
}

.phase-banner-card {
  margin-bottom: 16px;
}

.phase-banner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.phase-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.phase-desc {
  color: #606266;
  font-size: 13px;
}

.next-phase-hint {
  color: #909399;
  font-size: 12px;
}

.search-card {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>
