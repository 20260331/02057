<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, Upload, Refresh, Delete, Plus, Edit } from '@element-plus/icons-vue'
import { dataManageApi, type BackupInfo, type DictItem, type DictTypeItem, type DictValueItem } from '@/api/system'

const activeTab = ref('backup')
const loading = ref(false)
const exportLoading = ref(false)

// 数据
const backups = ref<BackupInfo[]>([])
const dictionary = ref<DictItem[]>([])

// 字典编辑相关
const dictDialogVisible = ref(false)
const dictDialogType = ref<'type' | 'item'>('type')
const dictDialogMode = ref<'add' | 'edit'>('add')
const currentDictCode = ref('')
const dictTypeForm = ref<Partial<DictTypeItem>>({})
const dictItemForm = ref<Partial<DictValueItem>>({})

// 加载备份列表
const loadBackups = async () => {
  loading.value = true
  try {
    const res = await dataManageApi.getBackupList()
    if (res.code === 200) {
      backups.value = res.data
    }
  } catch (error) {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}

// 创建备份
const createBackup = async () => {
  try {
    await ElMessageBox.confirm('确定要创建数据库备份吗？此操作可能需要一些时间。', '确认备份')
    loading.value = true
    const res = await dataManageApi.createBackup()
    if (res.code === 200) {
      ElMessage.success(res.data.message)
      loadBackups()
    }
  } catch (error) {
    // 用户取消或错误
  } finally {
    loading.value = false
  }
}

// 删除备份
const deleteBackup = async (fileName: string) => {
  try {
    await ElMessageBox.confirm(`确定要删除备份文件 "${fileName}" 吗？此操作不可恢复。`, '确认删除', { type: 'warning' })
    await dataManageApi.deleteBackup(fileName)
    ElMessage.success('删除成功')
    loadBackups()
  } catch (error) {
    // 用户取消或错误
  }
}

// 恢复备份（异步：先发起恢复，再轮询状态）
const restoreBackup = async (fileName: string) => {
  try {
    await ElMessageBox.confirm(
      `确定要从备份 "${fileName}" 恢复数据吗？此操作将覆盖当前数据，请谨慎操作！`, 
      '危险操作', 
      { type: 'error', confirmButtonText: '确认恢复', cancelButtonText: '取消' }
    )
    loading.value = true
    const res = await dataManageApi.restoreBackup(fileName)
    if (res.code !== 200) {
      loading.value = false
      return
    }
    ElMessage.info('恢复任务已启动，正在后台执行…')
    let resolved = false // 已得到最终结果（SUCCESS/FAILED）后不再提示“查询恢复状态失败”
    const poll = async (): Promise<void> => {
      try {
        const statusRes = await dataManageApi.getRestoreStatus()
        const data = statusRes.data as { status: string; message?: string }
        if (data?.status === 'RUNNING') {
          setTimeout(poll, 2000)
          return
        }
        resolved = true
        loading.value = false
        if (data?.status === 'SUCCESS') {
          ElMessage.success(data?.message || '数据已成功从备份恢复')
          fetchBackupList()
        } else if (data?.status === 'FAILED') {
          ElMessage.error(data?.message || '恢复失败')
        }
      } catch {
        loading.value = false
        if (!resolved) ElMessage.error('查询恢复状态失败')
      }
    }
    setTimeout(poll, 2000)
  } catch (_) {
    loading.value = false
  }
}

// 下载备份文件
const downloadBackup = (fileName: string) => {
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.error('请先登录')
    return
  }
  
  const baseUrl = import.meta.env.VITE_API_BASE_URL || '/api'
  const downloadUrl = `${baseUrl}/system/data/backup/download/${fileName}`
  
  fetch(downloadUrl, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
    .then(response => {
      if (!response.ok) {
        throw new Error('下载失败')
      }
      return response.blob()
    })
    .then(blob => {
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = fileName
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
      ElMessage.success('下载成功')
    })
    .catch(error => {
      console.error('下载失败:', error)
      ElMessage.error('下载失败，请重试')
    })
}

// 加载数据字典
const loadDictionary = async () => {
  loading.value = true
  try {
    const res = await dataManageApi.getDataDictionary()
    if (res.code === 200) {
      dictionary.value = res.data
    }
  } catch (error) {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}

// 导出数据 - 直接触发文件下载
const exportData = (type: string) => {
  const typeNames: Record<string, string> = {
    student: '学生数据',
    course: '课程数据',
    grade: '成绩数据',
    leave: '请假记录'
  }
  
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.error('请先登录')
    return
  }
  
  const baseUrl = import.meta.env.VITE_API_BASE_URL || '/api'
  const downloadUrl = `${baseUrl}/system/data/export/${type}`
  
  exportLoading.value = true
  
  fetch(downloadUrl, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
    .then(response => {
      if (!response.ok) {
        throw new Error('导出失败')
      }
      const disposition = response.headers.get('Content-Disposition')
      let fileName = `${type}_export.csv`
      if (disposition) {
        const match = disposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/)
        if (match && match[1]) {
          fileName = decodeURIComponent(match[1].replace(/['"]/g, ''))
        }
      }
      return response.blob().then(blob => ({ blob, fileName }))
    })
    .then(({ blob, fileName }) => {
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = fileName
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
      ElMessage.success(`${typeNames[type]}导出成功`)
    })
    .catch(error => {
      console.error('导出失败:', error)
      ElMessage.error('导出失败，请重试')
    })
    .finally(() => {
      exportLoading.value = false
    })
}

// 切换标签
const handleTabChange = (tab: string) => {
  if (tab === 'backup') {
    loadBackups()
  } else if (tab === 'dictionary') {
    loadDictionary()
  }
}

// 格式化日期
const formatDate = (dateStr: string) => {
  return new Date(dateStr).toLocaleString('zh-CN')
}

// 添加字典类型
const showAddDictType = () => {
  dictDialogType.value = 'type'
  dictDialogMode.value = 'add'
  dictTypeForm.value = { sortOrder: 0 }
  dictDialogVisible.value = true
}

// 添加字典项
const showAddDictItem = (dictCode: string) => {
  dictDialogType.value = 'item'
  dictDialogMode.value = 'add'
  currentDictCode.value = dictCode
  dictItemForm.value = { dictCode, sortOrder: 0 }
  dictDialogVisible.value = true
}

// 保存字典
const saveDictionary = async () => {
  try {
    if (dictDialogType.value === 'type') {
      if (!dictTypeForm.value.dictCode || !dictTypeForm.value.dictName) {
        ElMessage.warning('请填写完整信息')
        return
      }
      if (dictDialogMode.value === 'add') {
        await dataManageApi.createDictType(dictTypeForm.value)
        ElMessage.success('添加成功')
      }
    } else {
      if (!dictItemForm.value.itemValue || !dictItemForm.value.itemLabel) {
        ElMessage.warning('请填写完整信息')
        return
      }
      if (dictDialogMode.value === 'add') {
        await dataManageApi.createDictItem(dictItemForm.value)
        ElMessage.success('添加成功')
      }
    }
    dictDialogVisible.value = false
    loadDictionary()
  } catch (error) {
    // 错误已在拦截器中处理
  }
}

// 删除字典项
const deleteDictItem = async (item: DictItem, value: { value: string; label: string }) => {
  if (item.isSystem === 1) {
    ElMessage.warning('系统内置字典不允许删除')
    return
  }
  
  try {
    await ElMessageBox.confirm(`确定要删除字典项 "${value.label}" 吗？`, '确认删除', { type: 'warning' })
    // 需要先获取字典项的 ID
    const res = await dataManageApi.getDictItems(item.code)
    if (res.code === 200) {
      const dictItem = res.data.find(d => d.itemValue === value.value)
      if (dictItem) {
        await dataManageApi.deleteDictItem(dictItem.id)
        ElMessage.success('删除成功')
        loadDictionary()
      }
    }
  } catch (error) {
    // 用户取消或错误
  }
}

// 删除字典类型
const deleteDictType = async (item: DictItem) => {
  if (item.isSystem === 1) {
    ElMessage.warning('系统内置字典不允许删除')
    return
  }
  
  try {
    await ElMessageBox.confirm(`确定要删除字典类型 "${item.name}" 及其所有字典项吗？`, '确认删除', { type: 'warning' })
    // 需要先获取字典类型的 ID
    const res = await dataManageApi.getDictTypes()
    if (res.code === 200) {
      const dictType = res.data.find(d => d.dictCode === item.code)
      if (dictType) {
        await dataManageApi.deleteDictType(dictType.id)
        ElMessage.success('删除成功')
        loadDictionary()
      }
    }
  } catch (error) {
    // 用户取消或错误
  }
}

onMounted(() => {
  loadBackups()
})
</script>

<template>
  <div class="data-manage-container">
    <el-card>
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 数据备份与恢复 -->
        <el-tab-pane label="数据备份与恢复" name="backup">
          <div class="section-header">
            <h4>数据库备份管理</h4>
            <div class="toolbar">
              <el-button type="primary" :icon="Download" @click="createBackup" :loading="loading">
                创建备份
              </el-button>
              <el-button :icon="Refresh" @click="loadBackups">刷新列表</el-button>
            </div>
          </div>
          
          <el-alert 
            type="info" 
            :closable="false" 
            style="margin-bottom: 16px"
            title="备份说明"
            description="系统会自动备份数据库到服务器。恢复操作需要在服务器端手动执行，以确保数据安全。"
          />
          
          <el-table :data="backups" v-loading="loading" stripe>
            <el-table-column prop="fileName" label="备份文件名" min-width="200" />
            <el-table-column prop="fileSize" label="文件大小" width="120" />
            <el-table-column label="创建时间" width="180">
              <template #default="{ row }">
                {{ formatDate(row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link :icon="Download" @click="downloadBackup(row.fileName)">下载</el-button>
                <el-button type="warning" link @click="restoreBackup(row.fileName)">恢复</el-button>
                <el-button type="danger" link :icon="Delete" @click="deleteBackup(row.fileName)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!loading && backups.length === 0" description="暂无备份文件" />
        </el-tab-pane>
        
        <!-- 数据导入导出 -->
        <el-tab-pane label="数据导入导出" name="export">
          <div class="export-section">
            <h4>数据导出</h4>
            <p class="description">导出系统数据为 CSV 文件，可用于数据分析或迁移。</p>
            <div class="export-buttons">
              <el-button type="primary" :icon="Download" :loading="exportLoading" @click="exportData('student')">导出学生数据</el-button>
              <el-button type="primary" :icon="Download" :loading="exportLoading" @click="exportData('course')">导出课程数据</el-button>
              <el-button type="primary" :icon="Download" :loading="exportLoading" @click="exportData('grade')">导出成绩数据</el-button>
              <el-button type="primary" :icon="Download" :loading="exportLoading" @click="exportData('leave')">导出请假记录</el-button>
            </div>
          </div>
          
          <el-divider />
          
          <div class="import-section">
            <h4>数据导入</h4>
            <p class="description">从 Excel 文件批量导入数据。请先下载模板，按格式填写后上传。</p>
            <el-alert 
              type="warning" 
              :closable="false" 
              style="margin-bottom: 16px"
              title="注意事项"
              description="导入数据前请确保数据格式正确，错误的数据可能导致导入失败。建议先在测试环境验证。"
            />
            <div class="import-buttons">
              <el-upload action="#" :auto-upload="false" :show-file-list="false">
                <el-button type="success" :icon="Upload">导入学生数据</el-button>
              </el-upload>
              <el-upload action="#" :auto-upload="false" :show-file-list="false">
                <el-button type="success" :icon="Upload">导入课程数据</el-button>
              </el-upload>
            </div>
          </div>
        </el-tab-pane>
        
        <!-- 数据字典 -->
        <el-tab-pane label="数据字典维护" name="dictionary">
          <div class="section-header">
            <h4>系统数据字典</h4>
            <div class="toolbar">
              <el-button type="primary" :icon="Plus" @click="showAddDictType">添加字典类型</el-button>
              <el-button :icon="Refresh" @click="loadDictionary">刷新</el-button>
            </div>
          </div>
          
          <el-alert 
            type="info" 
            :closable="false" 
            style="margin-bottom: 16px"
            title="数据字典说明"
            description="数据字典定义了系统中使用的各类枚举值和状态码。系统内置字典不允许修改或删除。"
          />
          
          <el-collapse v-loading="loading">
            <el-collapse-item 
              v-for="item in dictionary" 
              :key="item.code" 
              :name="item.code"
            >
              <template #title>
                <div class="dict-title">
                  <span>{{ item.name }} ({{ item.code }})</span>
                  <el-tag v-if="item.isSystem === 1" size="small" type="info" style="margin-left: 8px">系统内置</el-tag>
                  <el-button 
                    v-if="item.isSystem !== 1" 
                    type="danger" 
                    link 
                    size="small" 
                    :icon="Delete" 
                    style="margin-left: 12px"
                    @click.stop="deleteDictType(item)"
                  >
                    删除
                  </el-button>
                </div>
              </template>
              
              <div class="dict-content">
                <div class="dict-toolbar" v-if="item.isSystem !== 1">
                  <el-button type="primary" size="small" :icon="Plus" @click.stop="showAddDictItem(item.code)">
                    添加字典项
                  </el-button>
                </div>
                
                <el-table :data="item.values" size="small" border>
                  <el-table-column prop="value" label="编码值" width="150">
                    <template #default="{ row }">
                      <el-tag size="small">{{ row.value }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="label" label="显示名称" width="150" />
                  <el-table-column prop="cssClass" label="样式" width="100">
                    <template #default="{ row }">
                      <el-tag v-if="row.cssClass" :type="row.cssClass" size="small">{{ row.cssClass }}</el-tag>
                      <span v-else>-</span>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="100" v-if="item.isSystem !== 1">
                    <template #default="{ row }">
                      <el-button type="danger" link size="small" :icon="Delete" @click="deleteDictItem(item, row)">删除</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </el-collapse-item>
          </el-collapse>
          <el-empty v-if="!loading && dictionary.length === 0" description="暂无数据字典" />
        </el-tab-pane>
      </el-tabs>
    </el-card>
    
    <!-- 字典编辑对话框 -->
    <el-dialog 
      v-model="dictDialogVisible" 
      :title="dictDialogType === 'type' ? '添加字典类型' : '添加字典项'"
      width="500px"
    >
      <!-- 字典类型表单 -->
      <el-form v-if="dictDialogType === 'type'" :model="dictTypeForm" label-width="100px">
        <el-form-item label="字典编码" required>
          <el-input v-model="dictTypeForm.dictCode" placeholder="如: order_status" />
        </el-form-item>
        <el-form-item label="字典名称" required>
          <el-input v-model="dictTypeForm.dictName" placeholder="如: 订单状态" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="dictTypeForm.description" type="textarea" placeholder="字典描述" />
        </el-form-item>
        <el-form-item label="排序号">
          <el-input-number v-model="dictTypeForm.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      
      <!-- 字典项表单 -->
      <el-form v-else :model="dictItemForm" label-width="100px">
        <el-form-item label="字典编码">
          <el-input v-model="dictItemForm.dictCode" disabled />
        </el-form-item>
        <el-form-item label="编码值" required>
          <el-input v-model="dictItemForm.itemValue" placeholder="如: PENDING" />
        </el-form-item>
        <el-form-item label="显示名称" required>
          <el-input v-model="dictItemForm.itemLabel" placeholder="如: 待处理" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="dictItemForm.description" placeholder="字典项描述" />
        </el-form-item>
        <el-form-item label="样式类">
          <el-select v-model="dictItemForm.cssClass" placeholder="选择样式" clearable>
            <el-option label="成功(绿色)" value="success" />
            <el-option label="警告(橙色)" value="warning" />
            <el-option label="危险(红色)" value="danger" />
            <el-option label="信息(灰色)" value="info" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序号">
          <el-input-number v-model="dictItemForm.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dictDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveDictionary">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.data-manage-container {
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  
  h4 {
    margin: 0;
    color: #303133;
  }
  
  .toolbar {
    display: flex;
    gap: 12px;
  }
}

.description {
  color: #909399;
  font-size: 14px;
  margin-bottom: 16px;
}

.export-section,
.import-section {
  h4 {
    margin: 0 0 8px 0;
    color: #303133;
  }
}

.export-buttons,
.import-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.dict-title {
  display: flex;
  align-items: center;
}

.dict-content {
  .dict-toolbar {
    margin-bottom: 12px;
  }
}
</style>
