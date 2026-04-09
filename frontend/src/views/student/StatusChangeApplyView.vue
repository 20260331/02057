<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { studentApi } from '@/api/student'
import { statusChangeApi, type StatusChangeApplicationData } from '@/api/statusChange'
import type { StudentInfo, AcademicStatus } from '@/api/types'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const studentLoading = ref(false)
const student = ref<StudentInfo | null>(null)

const form = reactive<StatusChangeApplicationData>({
  targetStatus: '',
  reason: '',
  effectiveDate: ''
})

const rules: FormRules = {
  targetStatus: [{ required: true, message: '请选择目标状态', trigger: 'change' }],
  reason: [{ required: true, message: '请填写申请原因', trigger: 'blur' }],
  effectiveDate: [{ required: true, message: '请选择期望生效日期', trigger: 'change' }]
}

const statusMap: Record<string, string> = {
  ENROLLED: '在读',
  SUSPENDED: '休学',
  WITHDRAWN: '退学',
  GRADUATED: '毕业',
  TRANSFERRED: '转学'
}

// 根据当前状态计算可选目标状态
const availableTargets = computed(() => {
  if (!student.value) return []
  const current = student.value.academicStatus
  const transitions: Record<string, string[]> = {
    ENROLLED: ['SUSPENDED', 'WITHDRAWN', 'GRADUATED', 'TRANSFERRED'],
    SUSPENDED: ['ENROLLED', 'WITHDRAWN'],
    WITHDRAWN: [],
    GRADUATED: [],
    TRANSFERRED: []
  }
  return (transitions[current] || []).map(code => ({
    value: code,
    label: statusMap[code] || code
  }))
})

// 是否需要教务处二级审批
const needsEscalation = computed(() => {
  return form.targetStatus === 'WITHDRAWN' || form.targetStatus === 'TRANSFERRED'
})

const loadStudent = async () => {
  studentLoading.value = true
  try {
    const res = await studentApi.getCurrentStudent()
    if (res.code === 200) {
      student.value = res.data
    }
  } catch {
    // handled by interceptor
  } finally {
    studentLoading.value = false
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await statusChangeApi.submitApplication(form)
    if (res.code === 200) {
      ElMessage.success('学籍异动申请已提交')
      router.push('/student/status-change/list')
    }
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  formRef.value?.resetFields()
}

loadStudent()
</script>

<template>
  <div class="status-change-apply" v-loading="studentLoading">
    <el-card>
      <template #header>
        <span>学籍异动申请</span>
      </template>

      <el-alert
        v-if="student && availableTargets.length === 0"
        title="当前学籍状态不允许变更"
        type="warning"
        :closable="false"
        show-icon
        style="margin-bottom: 20px"
      />

      <!-- 当前信息展示 -->
      <el-descriptions v-if="student" :column="2" border style="margin-bottom: 24px">
        <el-descriptions-item label="姓名">{{ student.name }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ student.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="院系">{{ student.department }}</el-descriptions-item>
        <el-descriptions-item label="班级">{{ student.classNo }}</el-descriptions-item>
        <el-descriptions-item label="当前学籍状态">
          <el-tag :type="student.academicStatus === 'ENROLLED' ? 'success' : 'warning'">
            {{ statusMap[student.academicStatus] || student.academicStatus }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        style="max-width: 720px"
        v-if="availableTargets.length > 0"
      >
        <el-form-item label="目标状态" prop="targetStatus">
          <el-select v-model="form.targetStatus" placeholder="请选择目标学籍状态">
            <el-option
              v-for="item in availableTargets"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="期望生效日期" prop="effectiveDate">
          <el-date-picker
            v-model="form.effectiveDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item label="申请原因" prop="reason">
          <el-input
            v-model="form.reason"
            type="textarea"
            :rows="4"
            placeholder="请详细说明学籍异动的原因"
          />
        </el-form-item>

        <el-alert
          v-if="needsEscalation"
          title="退学和转学申请需要辅导员和教务处两级审批"
          type="info"
          :closable="false"
          show-icon
          style="margin-bottom: 20px"
        />

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">提交申请</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style lang="scss" scoped>
.status-change-apply {
}
</style>
