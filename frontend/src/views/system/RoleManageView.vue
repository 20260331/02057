<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { request } from '@/api/request'

interface Role {
  id: number
  roleCode: string
  roleName: string
  description: string
  sortOrder: number
  status: number
}

interface Permission {
  id: number
  permissionCode: string
  permissionName: string
  resource: string
  action: string
  description: string
}

const loading = ref(false)
const roles = ref<Role[]>([])

// 编辑对话框
const editDialogVisible = ref(false)
const editForm = ref<Partial<Role>>({})
const currentRoleId = ref<number | null>(null)

// 权限配置对话框
const permDialogVisible = ref(false)
const allPermissions = ref<Permission[]>([])
const selectedPermissions = ref<number[]>([])
const permRoleName = ref('')

// 加载角色列表
const loadRoles = async () => {
  loading.value = true
  try {
    const res = await request.get<Role[]>('/system/roles')
    if (res.code === 200) {
      roles.value = res.data
    }
  } catch (error) {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}

// 编辑角色
const handleEdit = (row: Role) => {
  currentRoleId.value = row.id
  editForm.value = {
    roleName: row.roleName,
    description: row.description,
    sortOrder: row.sortOrder,
    status: row.status
  }
  editDialogVisible.value = true
}

// 保存角色
const handleSaveRole = async () => {
  if (!currentRoleId.value) return
  try {
    await request.put(`/system/roles/${currentRoleId.value}`, editForm.value)
    ElMessage.success('保存成功')
    editDialogVisible.value = false
    loadRoles()
  } catch (error) {
    // 错误已在拦截器中处理
  }
}

// 权限配置
const handlePermission = async (row: Role) => {
  currentRoleId.value = row.id
  permRoleName.value = row.roleName
  
  try {
    // 加载所有权限
    const permRes = await request.get<Permission[]>('/system/roles/permissions/all')
    if (permRes.code === 200) {
      allPermissions.value = permRes.data
    }
    
    // 加载角色已有权限
    const rolePermRes = await request.get<number[]>(`/system/roles/${row.id}/permissions`)
    if (rolePermRes.code === 200) {
      selectedPermissions.value = rolePermRes.data
    }
    
    permDialogVisible.value = true
  } catch (error) {
    // 错误已在拦截器中处理
  }
}

// 保存权限配置
const handleSavePermissions = async () => {
  if (!currentRoleId.value) return
  try {
    await request.post(`/system/roles/${currentRoleId.value}/permissions`, selectedPermissions.value)
    ElMessage.success('权限配置成功')
    permDialogVisible.value = false
  } catch (error) {
    // 错误已在拦截器中处理
  }
}

// 按资源分组权限
const groupedPermissions = () => {
  const groups: Record<string, Permission[]> = {}
  allPermissions.value.forEach(p => {
    if (!groups[p.resource]) {
      groups[p.resource] = []
    }
    groups[p.resource].push(p)
  })
  return groups
}

// 资源名称映射
const resourceNames: Record<string, string> = {
  'student': '学生管理',
  'course': '课程管理',
  'course_selection': '选课管理',
  'course_schedule': '课表管理',
  'course_roster': '课程名单',
  'grade': '成绩管理',
  'grade_statistics': '成绩统计',
  'leave': '请假管理',
  'leave_statistics': '请假统计',
  'user': '用户管理',
  'role': '角色管理',
  'permission': '权限管理',
  'config': '系统配置',
  'audit': '审计日志',
  'backup': '数据备份'
}

onMounted(() => {
  loadRoles()
})
</script>

<template>
  <div class="role-manage-container">
    <el-card>
      <template #header>
        <span>角色管理</span>
      </template>
      
      <el-table :data="roles" v-loading="loading" stripe>
        <el-table-column prop="roleCode" label="角色编码" width="180" />
        <el-table-column prop="roleName" label="角色名称" width="150" />
        <el-table-column prop="description" label="描述" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="primary" link size="small" @click="handlePermission(row)">权限配置</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 编辑对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑角色" width="500px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="角色名称">
          <el-input v-model="editForm.roleName" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="editForm.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="editForm.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="editForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveRole">保存</el-button>
      </template>
    </el-dialog>
    
    <!-- 权限配置对话框 -->
    <el-dialog v-model="permDialogVisible" :title="`权限配置 - ${permRoleName}`" width="700px">
      <div class="permission-container">
        <el-checkbox-group v-model="selectedPermissions">
          <div v-for="(perms, resource) in groupedPermissions()" :key="resource" class="permission-group">
            <div class="group-title">{{ resourceNames[resource] || resource }}</div>
            <div class="group-items">
              <el-checkbox v-for="perm in perms" :key="perm.id" :label="perm.id">
                {{ perm.permissionName }}
              </el-checkbox>
            </div>
          </div>
        </el-checkbox-group>
      </div>
      <template #footer>
        <el-button @click="permDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSavePermissions">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.role-manage-container {
}

.permission-container {
  max-height: 400px;
  overflow-y: auto;
}

.permission-group {
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #eee;
  
  &:last-child {
    border-bottom: none;
  }
  
  .group-title {
    font-weight: bold;
    margin-bottom: 8px;
    color: #409eff;
  }
  
  .group-items {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    
    .el-checkbox {
      margin-right: 16px;
      margin-bottom: 4px;
    }
  }
}
</style>
