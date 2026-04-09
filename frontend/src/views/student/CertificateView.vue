<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Printer, Search, Document } from '@element-plus/icons-vue'
import { certificateApi, certTypeOptions, type CertificateInfo, type CertificateRequest } from '@/api/certificate'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const isStudent = computed(() => authStore.roles.includes('STUDENT'))
const isAdmin = computed(() =>
  authStore.roles.includes('ADMIN') ||
  authStore.roles.includes('ACADEMIC_AFFAIRS') ||
  authStore.roles.includes('COUNSELOR')
)

const loading = ref(false)
const certificates = ref<CertificateInfo[]>([])
const availableTypes = ref<string[]>([])

// 申请对话框
const applyDialogVisible = ref(false)
const applyForm = reactive<CertificateRequest>({
  certType: '',
  purpose: '',
  copies: 1
})

// 预览对话框
const previewDialogVisible = ref(false)
const currentCert = ref<CertificateInfo | null>(null)

// 验证对话框
const verifyDialogVisible = ref(false)
const verifyCertNo = ref('')
const verifyResult = ref<CertificateInfo | null>(null)
const verifyLoading = ref(false)

// 可用类型过滤
const filteredTypeOptions = computed(() => {
  return certTypeOptions.filter(opt => availableTypes.value.includes(opt.value))
})

const loadData = async () => {
  loading.value = true
  try {
    if (isStudent.value) {
      const [certRes, typeRes] = await Promise.all([
        certificateApi.getMyCertificates(),
        certificateApi.getAvailableTypes()
      ])
      if (certRes.code === 200) certificates.value = certRes.data
      if (typeRes.code === 200) availableTypes.value = typeRes.data
    } else if (isAdmin.value) {
      const res = await certificateApi.getRecentCertificates()
      if (res.code === 200) certificates.value = res.data
      availableTypes.value = certTypeOptions.map(o => o.value)
    }
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

const openApplyDialog = () => {
  applyForm.certType = ''
  applyForm.purpose = ''
  applyForm.copies = 1
  applyDialogVisible.value = true
}

const submitApply = async () => {
  if (!applyForm.certType) {
    ElMessage.warning('请选择证明类型')
    return
  }

  try {
    const res = await certificateApi.generate(applyForm)
    if (res.code === 200) {
      ElMessage.success('证明已生成')
      applyDialogVisible.value = false
      currentCert.value = res.data
      previewDialogVisible.value = true
      loadData()
    }
  } catch {
    // handled by interceptor
  }
}

const showPreview = async (row: CertificateInfo) => {
  try {
    const res = await certificateApi.getById(row.id)
    if (res.code === 200) {
      currentCert.value = res.data
      previewDialogVisible.value = true
    }
  } catch {
    // handled by interceptor
  }
}

const handlePrint = () => {
  if (!currentCert.value) return
  const printWindow = window.open('', '_blank')
  if (!printWindow) return

  printWindow.document.write(`
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="utf-8">
      <title>${currentCert.value.certTitle}</title>
      <style>
        body { font-family: "SimSun", "宋体", serif; margin: 40px; color: #333; }
        .certificate h1 { text-align: center; font-size: 26px; letter-spacing: 8px; margin-bottom: 30px; }
        .cert-no { text-align: right; color: #666; font-size: 13px; margin-bottom: 20px; }
        .content { line-height: 2; font-size: 16px; text-indent: 2em; }
        .content p { margin: 10px 0; }
        .content b { font-weight: bold; }
        .footer { margin-top: 60px; text-align: right; }
        .footer p { margin: 5px 0; }
        .school { font-size: 16px; font-weight: bold; }
        .date { font-size: 14px; }
        .seal { font-size: 13px; color: #c00; }
        .grade-table { width: 100%; border-collapse: collapse; margin: 16px 0; font-size: 14px; }
        .grade-table th, .grade-table td { border: 1px solid #333; padding: 6px 10px; text-align: center; }
        .grade-table th { background: #f0f0f0; }
        @media print { body { margin: 60px; } }
      </style>
    </head>
    <body>
      ${currentCert.value.certContent}
      <script>window.onload = function() { window.print(); }<\/script>
    </body>
    </html>
  `)
  printWindow.document.close()
}

const handleRevoke = async (row: CertificateInfo) => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入作废原因', '作废证明', {
      confirmButtonText: '确认作废',
      cancelButtonText: '取消',
      inputType: 'textarea',
      inputValidator: (val: string) => val?.trim() ? true : '请输入作废原因',
      type: 'warning'
    })
    await certificateApi.revoke(row.id, reason)
    ElMessage.success('证明已作废')
    loadData()
  } catch {
    // cancelled or error
  }
}

const handleVerify = async () => {
  if (!verifyCertNo.value.trim()) {
    ElMessage.warning('请输入证明编号')
    return
  }
  verifyLoading.value = true
  verifyResult.value = null
  try {
    const res = await certificateApi.verify(verifyCertNo.value.trim())
    if (res.code === 200) {
      verifyResult.value = res.data
    }
  } catch {
    // handled by interceptor
  } finally {
    verifyLoading.value = false
  }
}

const getStatusType = (status: string) => {
  return status === 'VALID' ? 'success' : 'danger'
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="certificate-view" v-loading="loading">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>学籍证明材料管理</span>
          <div>
            <el-button type="info" :icon="Search" @click="verifyDialogVisible = true">验证证明</el-button>
            <el-button v-if="isStudent" type="primary" :icon="Document" @click="openApplyDialog">
              申请证明
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="certificates" stripe>
        <el-table-column prop="certNo" label="证明编号" width="200" />
        <el-table-column prop="certTypeText" label="证明类型" width="120" />
        <el-table-column v-if="isAdmin" prop="studentName" label="学生姓名" width="100" />
        <el-table-column v-if="isAdmin" prop="studentNo" label="学号" width="120" />
        <el-table-column prop="purpose" label="用途" show-overflow-tooltip />
        <el-table-column prop="copies" label="份数" width="60" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ row.statusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="generatedByName" label="生成人" width="100" />
        <el-table-column prop="createdAt" label="生成时间" width="170" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="showPreview(row)">查看</el-button>
            <el-button
              v-if="isAdmin && row.status === 'VALID'"
              type="danger" link size="small"
              @click="handleRevoke(row)"
            >作废</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="certificates.length === 0" description="暂无证明记录" />
    </el-card>

    <!-- 申请证明对话框 -->
    <el-dialog v-model="applyDialogVisible" title="申请学籍证明" width="500px">
      <el-form :model="applyForm" label-width="100px">
        <el-form-item label="证明类型" required>
          <el-select v-model="applyForm.certType" placeholder="请选择证明类型" style="width: 100%">
            <el-option
              v-for="opt in filteredTypeOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="用途说明">
          <el-input
            v-model="applyForm.purpose"
            type="textarea"
            :rows="2"
            placeholder="如：办理签证、求职、考研等（选填）"
          />
        </el-form-item>
        <el-form-item label="份数">
          <el-input-number v-model="applyForm.copies" :min="1" :max="10" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitApply">生成证明</el-button>
      </template>
    </el-dialog>

    <!-- 证明预览对话框 -->
    <el-dialog v-model="previewDialogVisible" title="证明预览" width="700px">
      <template v-if="currentCert">
        <div class="cert-meta">
          <el-descriptions :column="3" border size="small">
            <el-descriptions-item label="证明编号">{{ currentCert.certNo }}</el-descriptions-item>
            <el-descriptions-item label="类型">{{ currentCert.certTypeText }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getStatusType(currentCert.status)" size="small">{{ currentCert.statusText }}</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="cert-preview" v-html="currentCert.certContent" />

        <div v-if="currentCert.status === 'REVOKED'" class="revoked-info">
          <el-alert title="该证明已作废" type="error" :closable="false" show-icon>
            <template #default>
              <p>作废时间：{{ currentCert.revokedAt }}</p>
              <p>作废原因：{{ currentCert.revokeReason }}</p>
            </template>
          </el-alert>
        </div>
      </template>
      <template #footer>
        <el-button @click="previewDialogVisible = false">关闭</el-button>
        <el-button
          v-if="currentCert && currentCert.status === 'VALID'"
          type="primary"
          :icon="Printer"
          @click="handlePrint"
        >打印</el-button>
      </template>
    </el-dialog>

    <!-- 验证证明对话框 -->
    <el-dialog v-model="verifyDialogVisible" title="验证证明" width="600px">
      <el-form inline @submit.prevent="handleVerify">
        <el-form-item label="证明编号" style="flex: 1">
          <el-input
            v-model="verifyCertNo"
            placeholder="请输入证明编号"
            clearable
            style="width: 300px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="verifyLoading" @click="handleVerify">验证</el-button>
        </el-form-item>
      </el-form>

      <div v-if="verifyResult" class="verify-result">
        <el-alert
          :title="verifyResult.status === 'VALID' ? '证明有效' : '证明已作废'"
          :type="verifyResult.status === 'VALID' ? 'success' : 'error'"
          show-icon
          :closable="false"
          style="margin-bottom: 16px"
        />
        <el-descriptions :column="2" border>
          <el-descriptions-item label="证明编号">{{ verifyResult.certNo }}</el-descriptions-item>
          <el-descriptions-item label="证明类型">{{ verifyResult.certTypeText }}</el-descriptions-item>
          <el-descriptions-item label="学生姓名">{{ verifyResult.studentName }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ verifyResult.studentNo }}</el-descriptions-item>
          <el-descriptions-item label="院系">{{ verifyResult.department }}</el-descriptions-item>
          <el-descriptions-item label="生成时间">{{ verifyResult.createdAt }}</el-descriptions-item>
          <el-descriptions-item v-if="verifyResult.status === 'REVOKED'" label="作废原因" :span="2">
            {{ verifyResult.revokeReason }}
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <template #footer>
        <el-button @click="verifyDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.certificate-view {
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.cert-meta {
  margin-bottom: 16px;
}

.cert-preview {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 40px;
  background: #fff;
  min-height: 300px;
  font-family: "SimSun", "宋体", serif;

  :deep(h1) {
    text-align: center;
    font-size: 24px;
    letter-spacing: 6px;
    margin-bottom: 24px;
    color: #333;
  }

  :deep(.cert-no) {
    text-align: right;
    color: #888;
    font-size: 13px;
    margin-bottom: 16px;
  }

  :deep(.content) {
    line-height: 2;
    font-size: 15px;
    text-indent: 2em;

    p {
      margin: 8px 0;
    }

    b {
      font-weight: bold;
      text-decoration: underline;
    }
  }

  :deep(.footer) {
    margin-top: 48px;
    text-align: right;

    .school {
      font-size: 15px;
      font-weight: bold;
    }

    .date {
      font-size: 14px;
      color: #555;
    }

    .seal {
      font-size: 13px;
      color: #c00;
      margin-top: 8px;
    }
  }

  :deep(.grade-table) {
    width: 100%;
    border-collapse: collapse;
    margin: 12px 0;
    font-size: 13px;
    text-indent: 0;

    th, td {
      border: 1px solid #999;
      padding: 5px 8px;
      text-align: center;
    }

    th {
      background: #f5f5f5;
      font-weight: bold;
    }
  }
}

.revoked-info {
  margin-top: 16px;

  p {
    margin: 4px 0;
    font-size: 13px;
  }
}

.verify-result {
  margin-top: 16px;
}
</style>
