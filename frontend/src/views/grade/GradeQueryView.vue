<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { gradeApi } from '@/api/grade'
import type { TranscriptData } from '@/api/grade'
import type { GradeInfo, GPAInfo } from '@/api/types'
import { ElMessage } from 'element-plus'
import { Printer, Download, Document } from '@element-plus/icons-vue'

const loading = ref(false)
const hasFailingGrades = ref(false)
const grades = ref<GradeInfo[]>([])
const gpaInfo = ref<GPAInfo | null>(null)
const selectedSemester = ref('')

const transcriptLoading = ref(false)
const transcriptData = ref<TranscriptData | null>(null)
const showTranscriptDialog = ref(false)

const semesterOptions = computed(() => {
  const semesters = new Set(grades.value.map(g => g.semester).filter(Boolean))
  return Array.from(semesters).sort().reverse()
})

const loadData = async () => {
  loading.value = true
  try {
    const [gradesRes, gpaRes, failingRes] = await Promise.all([
      gradeApi.getMyGrades(selectedSemester.value || undefined),
      gradeApi.getMyGPA(),
      gradeApi.hasMyFailingGrades()
    ])

    if (gradesRes.code === 200) {
      grades.value = gradesRes.data
    }
    if (gpaRes.code === 200) {
      gpaInfo.value = gpaRes.data
    }
    if (failingRes.code === 200) {
      hasFailingGrades.value = failingRes.data
    }
  } catch (error) {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

const handleSemesterChange = () => {
  loadData()
}

const getGradeColor = (grade: string) => {
  const colors: Record<string, string> = {
    A: '#67c23a',
    B: '#409eff',
    C: '#e6a23c',
    D: '#f56c6c',
    F: '#909399'
  }
  return colors[grade] || '#909399'
}

const handleGenerateTranscript = async () => {
  transcriptLoading.value = true
  try {
    const res = await gradeApi.getMyTranscript()
    if (res.code === 200) {
      transcriptData.value = res.data
      showTranscriptDialog.value = true
    }
  } catch (error) {
    ElMessage.error('生成成绩单失败')
  } finally {
    transcriptLoading.value = false
  }
}

const handlePrintTranscript = () => {
  if (!transcriptData.value?.htmlContent) return
  const printWindow = window.open('', '_blank')
  if (printWindow) {
    printWindow.document.write(transcriptData.value.htmlContent)
    printWindow.document.close()
    printWindow.onload = () => {
      printWindow.print()
    }
  }
}

const handleDownloadTranscript = () => {
  if (!transcriptData.value?.htmlContent) return
  const blob = new Blob([transcriptData.value.htmlContent], { type: 'text/html;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `成绩单_${transcriptData.value.studentNo}_${transcriptData.value.studentName}.html`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
  ElMessage.success('成绩单已下载')
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="grade-query-container" v-loading="loading">
    <!-- 成绩预警 -->
    <el-alert
      v-if="hasFailingGrades"
      class="grade-warning"
      type="warning"
      show-icon
      title="存在不及格课程，请及时关注学习情况，并与任课教师或辅导员沟通。"
    />

    <!-- GPA 卡片 -->
    <el-row :gutter="20" class="gpa-row">
      <el-col :span="6">
        <el-card class="gpa-card">
          <div class="gpa-value">{{ gpaInfo?.cumulativeGPA?.toFixed(2) || '0.00' }}</div>
          <div class="gpa-label">累计 GPA</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="gpa-card">
          <div class="gpa-value">{{ gpaInfo?.totalCredits || 0 }}</div>
          <div class="gpa-label">总学分</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="gpa-card">
          <div class="gpa-value">{{ gpaInfo?.earnedCredits || 0 }}</div>
          <div class="gpa-label">已获学分</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="gpa-card">
          <div class="gpa-value">{{ grades.length }}</div>
          <div class="gpa-label">课程数</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 成绩列表 -->
    <el-card class="grade-card">
      <template #header>
        <div class="card-header">
          <span>成绩列表</span>
          <div class="header-actions">
            <el-select
              v-model="selectedSemester"
              placeholder="选择学期"
              clearable
              @change="handleSemesterChange"
              style="width: 160px; margin-right: 12px"
            >
              <el-option
                v-for="sem in semesterOptions"
                :key="sem"
                :label="sem"
                :value="sem"
              />
            </el-select>
            <el-button
              type="primary"
              :icon="Document"
              :loading="transcriptLoading"
              @click="handleGenerateTranscript"
            >
              生成成绩单
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="grades" stripe>
        <el-table-column prop="courseCode" label="课程编号" width="120" />
        <el-table-column prop="courseName" label="课程名称" width="200" />
        <el-table-column prop="credits" label="学分" width="70" />
        <el-table-column prop="score" label="分数" width="80" />
        <el-table-column prop="letterGrade" label="等级" width="80">
          <template #default="{ row }">
            <span :style="{ color: getGradeColor(row.letterGrade), fontWeight: 'bold' }">
              {{ row.letterGrade }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="gradePoints" label="绩点" width="80" />
        <el-table-column prop="semester" label="学期" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'APPROVED' ? 'success' : 'info'" size="small">
              {{ row.status === 'APPROVED' ? '已确认' : row.status }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 成绩单预览弹窗 -->
    <el-dialog
      v-model="showTranscriptDialog"
      title="学业成绩单预览"
      width="900px"
      top="3vh"
      destroy-on-close
    >
      <div v-if="transcriptData" class="transcript-preview">
        <!-- 成绩单结构化预览 -->
        <div class="tp-header">
          <h2>{{ transcriptData.schoolName }}</h2>
          <h3>学 业 成 绩 单</h3>
          <p class="en-title">OFFICIAL ACADEMIC TRANSCRIPT</p>
        </div>

        <div class="tp-student-info">
          <el-descriptions :column="3" border size="small">
            <el-descriptions-item label="姓名">{{ transcriptData.studentName }}</el-descriptions-item>
            <el-descriptions-item label="学号">{{ transcriptData.studentNo }}</el-descriptions-item>
            <el-descriptions-item label="性别">{{ transcriptData.gender }}</el-descriptions-item>
            <el-descriptions-item label="学院">{{ transcriptData.department }}</el-descriptions-item>
            <el-descriptions-item label="专业">{{ transcriptData.major }}</el-descriptions-item>
            <el-descriptions-item label="班级">{{ transcriptData.classNo }}</el-descriptions-item>
            <el-descriptions-item label="入学日期">{{ transcriptData.enrollmentDate }}</el-descriptions-item>
            <el-descriptions-item label="学籍状态">{{ transcriptData.academicStatus || '在读' }}</el-descriptions-item>
            <el-descriptions-item label="生成日期">{{ transcriptData.generatedDate }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="tp-summary">
          <el-row :gutter="16">
            <el-col :span="6">
              <div class="tp-stat">
                <span class="tp-stat-val gpa">{{ transcriptData.cumulativeGPA?.toFixed(2) || '0.00' }}</span>
                <span class="tp-stat-lbl">累计GPA</span>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="tp-stat">
                <span class="tp-stat-val">{{ transcriptData.totalCredits || 0 }}</span>
                <span class="tp-stat-lbl">总修学分</span>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="tp-stat">
                <span class="tp-stat-val">{{ transcriptData.earnedCredits || 0 }}</span>
                <span class="tp-stat-lbl">已获学分</span>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="tp-stat">
                <span class="tp-stat-val">{{ transcriptData.totalCourses || 0 }}/{{ transcriptData.passedCourses || 0 }}</span>
                <span class="tp-stat-lbl">课程总数/通过</span>
              </div>
            </el-col>
          </el-row>
        </div>

        <div v-for="sem in transcriptData.semesters" :key="sem.semester" class="tp-semester">
          <div class="tp-semester-header">
            <span class="tp-semester-name">{{ sem.semester }}</span>
            <span class="tp-semester-meta">
              GPA: <b>{{ sem.semesterGPA?.toFixed(2) || '-' }}</b> &nbsp; 学分: <b>{{ sem.semesterCredits || 0 }}</b>
            </span>
          </div>
          <el-table :data="sem.courses" size="small" stripe border>
            <el-table-column prop="courseCode" label="课程编号" width="120" />
            <el-table-column prop="courseName" label="课程名称" />
            <el-table-column prop="credits" label="学分" width="70" align="center" />
            <el-table-column prop="score" label="成绩" width="70" align="center" />
            <el-table-column prop="letterGrade" label="等级" width="70" align="center">
              <template #default="{ row }">
                <span :style="{ color: getGradeColor(row.letterGrade), fontWeight: 'bold' }">
                  {{ row.letterGrade || '-' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="gradePoints" label="绩点" width="70" align="center" />
          </el-table>
        </div>
      </div>

      <template #footer>
        <el-button @click="showTranscriptDialog = false">关闭</el-button>
        <el-button type="primary" :icon="Printer" @click="handlePrintTranscript">
          打印成绩单
        </el-button>
        <el-button type="success" :icon="Download" @click="handleDownloadTranscript">
          下载成绩单
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.grade-query-container {
}

.gpa-row {
  margin-bottom: 20px;
}

.grade-warning {
  margin-bottom: 16px;
}

.gpa-card {
  text-align: center;

  .gpa-value {
    font-size: 32px;
    font-weight: bold;
    color: #409eff;
  }

  .gpa-label {
    font-size: 14px;
    color: #909399;
    margin-top: 8px;
  }
}

.grade-card {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .header-actions {
    display: flex;
    align-items: center;
  }
}

/* 成绩单预览样式 */
.transcript-preview {
  max-height: 65vh;
  overflow-y: auto;
  padding: 0 8px;
}

.tp-header {
  text-align: center;
  padding-bottom: 16px;
  border-bottom: 3px double #333;
  margin-bottom: 16px;

  h2 {
    margin: 0 0 4px;
    font-size: 22px;
    letter-spacing: 6px;
  }

  h3 {
    margin: 0;
    font-size: 18px;
    letter-spacing: 4px;
    font-weight: normal;
  }

  .en-title {
    font-family: "Times New Roman", serif;
    font-size: 12px;
    color: #999;
    margin: 4px 0 0;
  }
}

.tp-student-info {
  margin-bottom: 16px;
}

.tp-summary {
  margin-bottom: 20px;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 6px;

  .tp-stat {
    text-align: center;

    .tp-stat-val {
      display: block;
      font-size: 22px;
      font-weight: bold;
      color: #303133;

      &.gpa {
        color: #409eff;
      }
    }

    .tp-stat-lbl {
      font-size: 12px;
      color: #909399;
    }
  }
}

.tp-semester {
  margin-bottom: 16px;

  .tp-semester-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 12px;
    background: #ecf5ff;
    border-left: 3px solid #409eff;
    margin-bottom: 8px;
    border-radius: 0 4px 4px 0;

    .tp-semester-name {
      font-weight: bold;
      color: #303133;
    }

    .tp-semester-meta {
      font-size: 13px;
      color: #606266;
    }
  }
}
</style>
