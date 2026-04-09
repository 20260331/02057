<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User } from '@element-plus/icons-vue'
import { studentApi, type UpdateContactRequest } from '@/api/student'
import type { StudentInfo } from '@/api/types'

const loading = ref(false)
const student = ref<StudentInfo | null>(null)
const editDialogVisible = ref(false)
const editForm = ref<UpdateContactRequest>({
  phone: '',
  email: '',
  address: ''
})

// 加载学生信息
const loadStudentInfo = async () => {
  loading.value = true
  try {
    const res = await studentApi.getCurrentStudent()
    if (res.code === 200) {
      student.value = res.data
    }
  } catch (error) {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}

// 打开编辑对话框
const openEditDialog = () => {
  if (student.value) {
    editForm.value = {
      phone: student.value.phone || '',
      email: student.value.email || '',
      address: ''
    }
    editDialogVisible.value = true
  }
}

// 保存联系方式
const saveContact = async () => {
  try {
    await studentApi.updateMyContact(editForm.value)
    ElMessage.success('联系方式更新成功')
    editDialogVisible.value = false
    loadStudentInfo()
  } catch (error) {
    // 错误已在拦截器中处理
  }
}

// 获取性别文本
const getGenderText = (gender: string) => {
  return gender === 'M' ? '男' : '女'
}

// 获取学籍状态文本
const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    ENROLLED: '在读',
    SUSPENDED: '休学',
    WITHDRAWN: '退学',
    GRADUATED: '毕业',
    TRANSFERRED: '转学'
  }
  return statusMap[status] || status
}

// 获取状态标签类型
const getStatusType = (status: string): 'success' | 'warning' | 'info' | 'danger' => {
  const typeMap: Record<string, 'success' | 'warning' | 'info' | 'danger'> = {
    ENROLLED: 'success',
    SUSPENDED: 'warning',
    WITHDRAWN: 'danger',
    GRADUATED: 'info',
    TRANSFERRED: 'info'
  }
  return typeMap[status] || 'info'
}

onMounted(() => {
  loadStudentInfo()
})
</script>

<template>
  <div class="student-info-container" v-loading="loading">
    <el-card v-if="student" class="info-card">
      <template #header>
        <div class="card-header">
          <span>个人信息</span>
          <el-button type="primary" size="small" @click="openEditDialog">
            修改联系方式
          </el-button>
        </div>
      </template>
      
      <el-descriptions :column="2" border>
        <el-descriptions-item label="学号">{{ student.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ student.name }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ getGenderText(student.gender) }}</el-descriptions-item>
        <el-descriptions-item label="出生日期">{{ student.birthDate }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ student.idNumber }}</el-descriptions-item>
        <el-descriptions-item label="学籍状态">
          <el-tag :type="getStatusType(student.academicStatus)">
            {{ getStatusText(student.academicStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="院系">{{ student.department }}</el-descriptions-item>
        <el-descriptions-item label="专业">{{ student.major }}</el-descriptions-item>
        <el-descriptions-item label="班级">{{ student.classNo }}</el-descriptions-item>
        <el-descriptions-item label="入学日期">{{ student.enrollmentDate }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ student.phone }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ student.email }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
    
    <!-- 非学生用户提示 -->
    <el-card v-else-if="!loading" class="info-card">
      <el-empty description="当前账号不是学生账号，无法查看个人信息">
        <template #image>
          <el-icon style="font-size: 60px; color: #909399;"><User /></el-icon>
        </template>
        <p style="color: #909399; margin-top: 10px;">请使用学生账号登录（如 student01）</p>
      </el-empty>
    </el-card>
    
    <!-- 编辑联系方式对话框 -->
    <el-dialog v-model="editDialogVisible" title="修改联系方式" width="500px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="联系电话">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="家庭住址">
          <el-input v-model="editForm.address" type="textarea" placeholder="请输入家庭住址" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveContact">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.student-info-container {
}

.info-card {
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
