<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules, UploadFile, UploadFiles } from 'element-plus'
import { Upload, Delete } from '@element-plus/icons-vue'
import { leaveApi, type LeaveRequestData } from '@/api/leave'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const uploadLoading = ref(false)
const createdLeaveId = ref<number | null>(null)

const form = reactive<LeaveRequestData>({
  leaveType: '',
  startDate: '',
  endDate: '',
  reason: '',
  urgent: false
})

// 已上传的附件列表
const uploadedFiles = ref<{ id: number; fileName: string; fileSize: string }[]>([])

const rules: FormRules = {
  leaveType: [{ required: true, message: '请选择请假类型', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }],
  reason: [{ required: true, message: '请填写请假原因', trigger: 'blur' }]
}

// 获取上传 URL
const getUploadUrl = () => {
  const baseUrl = import.meta.env.VITE_API_BASE_URL || '/api'
  return `${baseUrl}/files/leave/${createdLeaveId.value}`
}

// 获取上传请求头
const getUploadHeaders = () => {
  const token = localStorage.getItem('token')
  return {
    Authorization: `Bearer ${token}`
  }
}

// 上传前检查
const beforeUpload = (file: File) => {
  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'application/pdf', 
    'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document']
  
  if (!allowedTypes.includes(file.type)) {
    ElMessage.error('仅支持图片、PDF和Word文档')
    return false
  }
  
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过10MB')
    return false
  }
  
  return true
}

// 上传成功
const handleUploadSuccess = (response: any, file: UploadFile, fileList: UploadFiles) => {
  if (response.code === 200) {
    uploadedFiles.value.push({
      id: response.data.id,
      fileName: response.data.fileName,
      fileSize: response.data.fileSize
    })
    ElMessage.success('附件上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

// 上传失败
const handleUploadError = () => {
  ElMessage.error('附件上传失败，请重试')
}

// 删除附件
const handleDeleteFile = async (file: { id: number; fileName: string }) => {
  try {
    await leaveApi.deleteAttachment(file.id)
    uploadedFiles.value = uploadedFiles.value.filter(f => f.id !== file.id)
    ElMessage.success('删除成功')
  } catch (error) {
    // 错误已在拦截器中处理
  }
}

// 提交申请
const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  
  loading.value = true
  try {
    const res = await leaveApi.submitLeave(form)
    if (res.code === 200) {
      createdLeaveId.value = res.data
      ElMessage.success('请假申请提交成功，现在可以上传证明材料')
      // 提交成功后留在页面，让用户可以上传附件
    }
  } catch (error) {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}

// 完成并返回
const handleFinish = () => {
  router.push('/leave/list')
}

// 重置表单
const handleReset = () => {
  formRef.value?.resetFields()
  uploadedFiles.value = []
  createdLeaveId.value = null
}
</script>

<template>
  <div class="leave-apply-container">
    <el-card>
      <template #header>
        <span>请假申请</span>
      </template>
      
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        style="max-width: 720px"
      >
        <el-form-item label="请假类型" prop="leaveType">
          <el-select v-model="form.leaveType" placeholder="请选择" :disabled="!!createdLeaveId">
            <el-option label="病假" value="SICK" />
            <el-option label="事假" value="PERSONAL" />
            <el-option label="公假" value="OFFICIAL" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker
            v-model="form.startDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            :disabled="!!createdLeaveId"
          />
        </el-form-item>
        
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker
            v-model="form.endDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            :disabled="!!createdLeaveId"
          />
        </el-form-item>
        
        <el-form-item label="请假原因" prop="reason">
          <el-input
            v-model="form.reason"
            type="textarea"
            :rows="4"
            placeholder="请详细说明请假原因"
            :disabled="!!createdLeaveId"
          />
        </el-form-item>
        
        <el-form-item label="紧急请假">
          <el-switch v-model="form.urgent" :disabled="!!createdLeaveId" />
          <span class="tip">紧急请假将优先处理</span>
        </el-form-item>
        
        <!-- 附件上传区域 -->
        <el-form-item label="证明材料">
          <div class="upload-section">
            <el-upload
              v-if="createdLeaveId"
              :action="getUploadUrl()"
              :headers="getUploadHeaders()"
              :before-upload="beforeUpload"
              :on-success="handleUploadSuccess"
              :on-error="handleUploadError"
              :show-file-list="false"
              accept=".jpg,.jpeg,.png,.gif,.pdf,.doc,.docx"
            >
              <el-button type="primary" :icon="Upload">上传附件</el-button>
            </el-upload>
            <span v-else class="upload-tip">请先提交申请后再上传附件</span>
            
            <div class="file-list" v-if="uploadedFiles.length > 0">
              <div class="file-item" v-for="file in uploadedFiles" :key="file.id">
                <span class="file-name">{{ file.fileName }}</span>
                <span class="file-size">{{ file.fileSize }}</span>
                <el-button type="danger" link :icon="Delete" @click="handleDeleteFile(file)">删除</el-button>
              </div>
            </div>
            
            <div class="upload-hint">
              <p>支持格式：图片(JPG/PNG/GIF)、PDF、Word文档</p>
              <p>单个文件不超过10MB</p>
            </div>
          </div>
        </el-form-item>
        
        <el-form-item>
          <template v-if="!createdLeaveId">
            <el-button type="primary" :loading="loading" @click="handleSubmit">提交申请</el-button>
            <el-button @click="handleReset">重置</el-button>
          </template>
          <template v-else>
            <el-button type="success" @click="handleFinish">完成</el-button>
            <span class="success-tip">申请已提交，可继续上传附件或点击完成返回</span>
          </template>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style lang="scss" scoped>
.leave-apply-container {
}

.tip {
  margin-left: 10px;
  color: #909399;
  font-size: 12px;
}

.upload-section {
  width: 100%;
}

.upload-tip {
  color: #909399;
  font-size: 14px;
}

.file-list {
  margin-top: 12px;
  
  .file-item {
    display: flex;
    align-items: center;
    padding: 8px 12px;
    background: #f5f7fa;
    border-radius: 4px;
    margin-bottom: 8px;
    
    .file-name {
      flex: 1;
      color: #303133;
    }
    
    .file-size {
      color: #909399;
      font-size: 12px;
      margin-right: 12px;
    }
  }
}

.upload-hint {
  margin-top: 8px;
  
  p {
    margin: 0;
    color: #909399;
    font-size: 12px;
    line-height: 1.6;
  }
}

.success-tip {
  margin-left: 12px;
  color: #67c23a;
  font-size: 14px;
}
</style>
