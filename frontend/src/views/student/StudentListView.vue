<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Download, Upload } from '@element-plus/icons-vue'
import { studentApi, type StudentQuery, type UpdateStudentRequest, type AcademicStatusChangeRequest } from '@/api/student'
import type { StudentInfo } from '@/api/types'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const loading = ref(false)
const tableData = ref<StudentInfo[]>([])
const total = ref(0)

// 判断是否有学籍状态变更权限
const canChangeStatus = computed(() => {
  return authStore.permissions.includes('student:status:change') ||
         authStore.roles.includes('ADMIN') ||
         authStore.roles.includes('ACADEMIC_AFFAIRS') ||
         authStore.roles.includes('COUNSELOR')
})

const queryForm = reactive<StudentQuery>({
  page: 1,
  size: 10,
  studentNo: '',
  name: '',
  department: '',
  academicStatus: ''
})

// 编辑对话框
const editDialogVisible = ref(false)
const currentStudent = ref<StudentInfo | null>(null)
const editForm = ref<UpdateStudentRequest>({})

// 学籍状态变更对话框
const statusDialogVisible = ref(false)
const statusForm = ref({
  targetStatus: '',
  reason: '',
  effectiveDate: new Date().toISOString().split('T')[0]
})

// 导入对话框
const importDialogVisible = ref(false)

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await studentApi.queryPage(queryForm)
    if (res.code === 200) {
      tableData.value = res.data.records
      total.value = res.data.total
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
  queryForm.studentNo = ''
  queryForm.name = ''
  queryForm.department = ''
  queryForm.academicStatus = ''
  queryForm.page = 1
  loadData()
}

// 分页变化
const handlePageChange = (page: number) => {
  queryForm.page = page
  loadData()
}

// 编辑学生
const handleEdit = (row: StudentInfo) => {
  currentStudent.value = row
  editForm.value = {
    phone: row.phone,
    email: row.email,
    classNo: row.classNo
  }
  editDialogVisible.value = true
}

// 保存编辑
const saveEdit = async () => {
  if (!currentStudent.value) return
  try {
    await studentApi.updateStudent(currentStudent.value.id, editForm.value)
    ElMessage.success('保存成功')
    editDialogVisible.value = false
    loadData()
  } catch (error) {
    // 错误已在拦截器中处理
  }
}

// 打开学籍状态变更对话框
const handleChangeStatus = (row: StudentInfo) => {
  currentStudent.value = row
  statusForm.value = {
    targetStatus: '',
    reason: '',
    effectiveDate: new Date().toISOString().split('T')[0]
  }
  statusDialogVisible.value = true
}

// 保存学籍状态变更
const saveStatusChange = async () => {
  if (!currentStudent.value) return
  if (!statusForm.value.targetStatus) {
    ElMessage.warning('请选择目标状态')
    return
  }
  if (!statusForm.value.reason.trim()) {
    ElMessage.warning('请填写变更原因')
    return
  }
  try {
    await studentApi.changeAcademicStatus(currentStudent.value.id, statusForm.value)
    ElMessage.success('学籍状态变更成功')
    statusDialogVisible.value = false
    loadData()
  } catch (error) {
    // 错误已在拦截器中处理
  }
}

// 导出
const handleExport = async () => {
  try {
    await studentApi.exportStudents(queryForm)
    ElMessage.success('导出成功')
  } catch (error) {
    // 错误已在拦截器中处理
  }
}

// 下载导入模板
const handleDownloadTemplate = async () => {
  try {
    await studentApi.downloadImportTemplate()
  } catch (error) {
    // 错误已在拦截器中处理
  }
}

// 导入文件上传
const handleImportUpload = async (options: { file: File }) => {
  try {
    const res = await studentApi.importStudents(options.file)
    if (res.code === 200) {
      ElMessage.success(`导入完成：成功 ${res.data.successCount} 条，失败 ${res.data.failCount} 条`)
      importDialogVisible.value = false
      loadData()
    }
  } catch (error) {
    // 错误已在拦截器中处理
  }
}

// 获取状态文本
const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    ENROLLED: '在读', SUSPENDED: '休学', WITHDRAWN: '退学',
    GRADUATED: '毕业', TRANSFERRED: '转学'
  }
  return map[status] || status
}

const getStatusType = (status: string): 'success' | 'warning' | 'danger' | 'info' => {
  const map: Record<string, 'success' | 'warning' | 'danger' | 'info'> = {
    ENROLLED: 'success', SUSPENDED: 'warning', WITHDRAWN: 'danger',
    GRADUATED: 'info', TRANSFERRED: 'info'
  }
  return map[status] || 'info'
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="student-list-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :model="queryForm" inline>
        <el-form-item label="学号">
          <el-input v-model="queryForm.studentNo" placeholder="请输入学号" clearable />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="queryForm.name" placeholder="请输入姓名" clearable />
        </el-form-item>
        <el-form-item label="院系">
          <el-input v-model="queryForm.department" placeholder="请输入院系" clearable />
        </el-form-item>
        <el-form-item label="学籍状态">
          <el-select v-model="queryForm.academicStatus" placeholder="请选择" clearable style="width: 120px">
            <el-option label="在读" value="ENROLLED" />
            <el-option label="休学" value="SUSPENDED" />
            <el-option label="退学" value="WITHDRAWN" />
            <el-option label="毕业" value="GRADUATED" />
            <el-option label="转学" value="TRANSFERRED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 工具栏 -->
    <el-card class="table-card">
      <template #header>
        <div class="toolbar">
          <span>学生列表</span>
          <div>
            <el-button type="success" :icon="Upload" @click="importDialogVisible = true">导入</el-button>
            <el-button type="warning" :icon="Download" @click="handleExport">导出</el-button>
          </div>
        </div>
      </template>
      
      <!-- 表格 -->
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="studentNo" label="学号" min-width="120" />
        <el-table-column prop="name" label="姓名" min-width="90" />
        <el-table-column prop="gender" label="性别" width="70">
          <template #default="{ row }">{{ row.gender === 'M' ? '男' : '女' }}</template>
        </el-table-column>
        <el-table-column prop="department" label="院系" min-width="140" />
        <el-table-column prop="major" label="专业" min-width="140" />
        <el-table-column prop="classNo" label="班级" min-width="110" />
        <el-table-column prop="phone" label="联系电话" min-width="130" />
        <el-table-column prop="academicStatus" label="学籍状态" min-width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.academicStatus)" size="small">
              {{ getStatusText(row.academicStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button 
              v-if="canChangeStatus" 
              type="warning" 
              link 
              size="small" 
              @click="handleChangeStatus(row)"
            >
              变更学籍
            </el-button>
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
    
    <!-- 编辑对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑学生信息" width="500px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="联系电话">
          <el-input v-model="editForm.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" />
        </el-form-item>
        <el-form-item label="班级">
          <el-input v-model="editForm.classNo" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>
    
    <!-- 学籍状态变更对话框 -->
    <el-dialog v-model="statusDialogVisible" title="变更学籍状态" width="500px">
      <el-descriptions v-if="currentStudent" :column="2" border style="margin-bottom: 20px">
        <el-descriptions-item label="学号">{{ currentStudent.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ currentStudent.name }}</el-descriptions-item>
        <el-descriptions-item label="当前状态">
          <el-tag :type="getStatusType(currentStudent.academicStatus)">
            {{ getStatusText(currentStudent.academicStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="院系">{{ currentStudent.department }}</el-descriptions-item>
      </el-descriptions>
      <el-form :model="statusForm" label-width="100px">
        <el-form-item label="目标状态" required>
          <el-select v-model="statusForm.targetStatus" placeholder="请选择目标状态" style="width: 100%">
            <el-option label="在读" value="ENROLLED" />
            <el-option label="休学" value="SUSPENDED" />
            <el-option label="退学" value="WITHDRAWN" />
            <el-option label="毕业" value="GRADUATED" />
            <el-option label="转学" value="TRANSFERRED" />
          </el-select>
        </el-form-item>
        <el-form-item label="生效日期" required>
          <el-date-picker 
            v-model="statusForm.effectiveDate" 
            type="date" 
            placeholder="选择生效日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="变更原因" required>
          <el-input 
            v-model="statusForm.reason" 
            type="textarea" 
            :rows="3" 
            placeholder="请填写学籍状态变更原因（必填）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveStatusChange">确认变更</el-button>
      </template>
    </el-dialog>
    
    <!-- 导入对话框 -->
    <el-dialog v-model="importDialogVisible" title="导入学生数据" width="500px">
      <div style="margin-bottom: 16px;">
        <el-button type="primary" link @click="handleDownloadTemplate">下载导入模板</el-button>
      </div>
      <el-upload
        drag
        :auto-upload="true"
        :show-file-list="false"
        accept=".xlsx,.xls"
        :http-request="handleImportUpload"
      >
        <el-icon class="el-icon--upload"><Upload /></el-icon>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip">只能上传 xlsx/xls 文件</div>
        </template>
      </el-upload>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.student-list-container {
}

.search-card {
  margin-bottom: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>
