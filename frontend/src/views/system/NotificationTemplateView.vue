<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { notificationApi } from '@/api/notification'
import type { NotificationTemplate, NotificationTemplateSave } from '@/api/notification'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'

const loading = ref(false)
const templates = ref<NotificationTemplate[]>([])
const filterCategory = ref('')

const dialogVisible = ref(false)
const dialogTitle = ref('新增模板')
const editingId = ref<number | null>(null)
const formLoading = ref(false)

const form = ref<NotificationTemplateSave>({
  templateCode: '',
  templateName: '',
  category: 'SYSTEM',
  subject: '',
  content: '',
  variables: [],
  channel: 'SITE'
})

const newVariable = ref('')

const categoryOptions = [
  { value: 'ACADEMIC', label: '教务通知' },
  { value: 'COURSE', label: '选课通知' },
  { value: 'GRADE', label: '成绩通知' },
  { value: 'LEAVE', label: '请假通知' },
  { value: 'SYSTEM', label: '系统通知' }
]

const channelOptions = [
  { value: 'SITE', label: '站内信' },
  { value: 'EMAIL', label: '邮件' },
  { value: 'ALL', label: '全渠道' }
]

const filteredTemplates = computed(() => {
  if (!filterCategory.value) return templates.value
  return templates.value.filter(t => t.category === filterCategory.value)
})

const loadTemplates = async () => {
  loading.value = true
  try {
    const res = await notificationApi.getTemplates()
    if (res.code === 200) {
      templates.value = res.data
    }
  } catch (error) {
    // handled
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  editingId.value = null
  dialogTitle.value = '新增通知模板'
  form.value = {
    templateCode: '',
    templateName: '',
    category: 'SYSTEM',
    subject: '',
    content: '',
    variables: [],
    channel: 'SITE'
  }
  dialogVisible.value = true
}

const handleEdit = async (row: NotificationTemplate) => {
  editingId.value = row.id
  dialogTitle.value = '编辑通知模板'
  form.value = {
    templateCode: row.templateCode,
    templateName: row.templateName,
    category: row.category,
    subject: row.subject,
    content: row.content,
    variables: [...(row.variables || [])],
    channel: row.channel
  }
  dialogVisible.value = true
}

const handleAddVariable = () => {
  const v = newVariable.value.trim()
  if (!v) return
  if (form.value.variables.includes(v)) {
    ElMessage.warning('变量已存在')
    return
  }
  form.value.variables.push(v)
  newVariable.value = ''
}

const handleRemoveVariable = (index: number) => {
  form.value.variables.splice(index, 1)
}

const handleSubmit = async () => {
  if (!form.value.templateCode || !form.value.templateName || !form.value.subject || !form.value.content) {
    ElMessage.warning('请填写完整模板信息')
    return
  }
  formLoading.value = true
  try {
    if (editingId.value) {
      await notificationApi.updateTemplate(editingId.value, form.value)
      ElMessage.success('模板更新成功')
    } else {
      await notificationApi.createTemplate(form.value)
      ElMessage.success('模板创建成功')
    }
    dialogVisible.value = false
    loadTemplates()
  } catch (error) {
    // handled
  } finally {
    formLoading.value = false
  }
}

const handleToggleStatus = async (row: NotificationTemplate) => {
  try {
    await notificationApi.toggleTemplateStatus(row.id)
    ElMessage.success(row.status === 1 ? '已禁用' : '已启用')
    loadTemplates()
  } catch (error) {
    // handled
  }
}

const handleDelete = async (row: NotificationTemplate) => {
  if (row.isSystem === 1) {
    ElMessage.warning('系统内置模板不允许删除')
    return
  }
  try {
    await ElMessageBox.confirm(`确定删除模板「${row.templateName}」吗？`, '确认删除', { type: 'warning' })
    await notificationApi.deleteTemplate(row.id)
    ElMessage.success('删除成功')
    loadTemplates()
  } catch (error) {
    // cancelled or error
  }
}

const getCategoryType = (category: string): string => {
  const map: Record<string, string> = {
    ACADEMIC: 'warning',
    COURSE: 'success',
    GRADE: '',
    LEAVE: 'info',
    SYSTEM: 'danger'
  }
  return map[category] || 'info'
}

onMounted(() => {
  loadTemplates()
})
</script>

<template>
  <div class="template-manage" v-loading="loading">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>通知模板管理</span>
          <div class="header-actions">
            <el-select v-model="filterCategory" placeholder="全部分类" clearable style="width: 140px; margin-right: 12px">
              <el-option v-for="opt in categoryOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
            </el-select>
            <el-button type="primary" :icon="Plus" @click="handleAdd">新增模板</el-button>
          </div>
        </div>
      </template>

      <el-table :data="filteredTemplates" stripe>
        <el-table-column prop="templateCode" label="模板编码" width="180" />
        <el-table-column prop="templateName" label="模板名称" width="160" />
        <el-table-column prop="categoryName" label="分类" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="getCategoryType(row.category)">{{ row.categoryName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="subject" label="标题模板" show-overflow-tooltip />
        <el-table-column prop="channelName" label="渠道" width="80" />
        <el-table-column label="变量" width="100">
          <template #default="{ row }">
            <span>{{ row.variables?.length || 0 }} 个</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              size="small"
              @change="handleToggleStatus(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="类型" width="80" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.isSystem === 1 ? 'danger' : 'info'">
              {{ row.isSystem === 1 ? '内置' : '自定义' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button
              type="danger" link size="small" :icon="Delete"
              :disabled="row.isSystem === 1"
              @click="handleDelete(row)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="680px" destroy-on-close>
      <el-form :model="form" label-width="100px">
        <el-form-item label="模板编码" required>
          <el-input v-model="form.templateCode" placeholder="如 COURSE_SELECTED" />
        </el-form-item>
        <el-form-item label="模板名称" required>
          <el-input v-model="form.templateName" placeholder="如 选课成功通知" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="分类" required>
              <el-select v-model="form.category" style="width: 100%">
                <el-option v-for="opt in categoryOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发送渠道">
              <el-select v-model="form.channel" style="width: 100%">
                <el-option v-for="opt in channelOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="标题模板" required>
          <el-input v-model="form.subject" placeholder="支持 ${变量名}，如 选课成功 - ${courseName}" />
        </el-form-item>
        <el-form-item label="内容模板" required>
          <el-input v-model="form.content" type="textarea" :rows="5"
            placeholder="支持 ${变量名} 占位符，发送时会自动替换" />
        </el-form-item>
        <el-form-item label="模板变量">
          <div class="variable-list">
            <el-tag
              v-for="(v, index) in form.variables" :key="v"
              closable size="small" style="margin: 0 6px 6px 0"
              @close="handleRemoveVariable(index)"
            >
              ${ {{ v }} }
            </el-tag>
          </div>
          <div class="variable-add">
            <el-input v-model="newVariable" size="small" placeholder="变量名" style="width: 200px; margin-right: 8px"
              @keyup.enter="handleAddVariable" />
            <el-button size="small" @click="handleAddVariable">添加</el-button>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="formLoading" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.template-manage {
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
}

.variable-list {
  margin-bottom: 8px;
  min-height: 24px;
}

.variable-add {
  display: flex;
  align-items: center;
}
</style>
