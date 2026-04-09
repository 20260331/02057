<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { systemApi, type SystemUser, type UserQuery, type UserCreateData, type SystemRole } from '@/api/system'

const loading = ref(false)
const tableData = ref<SystemUser[]>([])
const total = ref(0)

const queryForm = reactive<UserQuery>({
  page: 1,
  size: 10,
  username: '',
  realName: '',
  status: undefined
})

// 新增/编辑对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增用户')
const isEdit = ref(false)
const currentId = ref<number | null>(null)
const userForm = ref<UserCreateData>({
  username: '',
  realName: '',
  phone: '',
  email: ''
})

// 角色分配对话框
const roleDialogVisible = ref(false)
const allRoles = ref<SystemRole[]>([])
const selectedRoleIds = ref<number[]>([])
const roleUserName = ref('')

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await systemApi.queryUsers(queryForm)
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
  queryForm.username = ''
  queryForm.realName = ''
  queryForm.status = undefined
  queryForm.page = 1
  loadData()
}

// 分页变化
const handlePageChange = (page: number) => {
  queryForm.page = page
  loadData()
}

// 新增用户
const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增用户'
  currentId.value = null
  userForm.value = { username: '', realName: '', phone: '', email: '' }
  dialogVisible.value = true
}

// 编辑用户
const handleEdit = (row: SystemUser) => {
  isEdit.value = true
  dialogTitle.value = '编辑用户'
  currentId.value = row.id
  userForm.value = {
    username: row.username,
    realName: row.realName,
    phone: row.phone,
    email: row.email
  }
  dialogVisible.value = true
}

// 保存用户
const handleSave = async () => {
  try {
    if (isEdit.value && currentId.value) {
      await systemApi.updateUser(currentId.value, userForm.value)
      ElMessage.success('更新成功')
    } else {
      await systemApi.createUser(userForm.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    // 错误已在拦截器中处理
  }
}

// 删除用户
const handleDelete = async (row: SystemUser) => {
  try {
    await ElMessageBox.confirm(`确定要删除用户"${row.username}"吗？`, '确认删除')
    await systemApi.deleteUser(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    // 用户取消
  }
}

// 重置密码
const handleResetPassword = async (row: SystemUser) => {
  try {
    await ElMessageBox.confirm(`确定要重置用户"${row.username}"的密码吗？`, '确认重置')
    await systemApi.resetPassword(row.id)
    ElMessage.success('密码已重置为 123456')
  } catch (error) {
    // 用户取消
  }
}

// 分配角色
const handleAssignRole = async (row: SystemUser) => {
  currentId.value = row.id
  roleUserName.value = row.realName
  
  try {
    // 加载所有角色
    const res = await systemApi.queryRoles()
    if (res.code === 200) {
      allRoles.value = res.data
    }
    
    // 获取用户当前角色（从roles字段解析）
    selectedRoleIds.value = allRoles.value
      .filter(r => row.roles.includes(r.roleCode))
      .map(r => r.id)
    
    roleDialogVisible.value = true
  } catch (error) {
    // 错误已在拦截器中处理
  }
}

// 保存角色分配
const handleSaveRoles = async () => {
  if (!currentId.value) return
  try {
    await systemApi.assignRoles(currentId.value, selectedRoleIds.value)
    ElMessage.success('角色分配成功')
    roleDialogVisible.value = false
    loadData()
  } catch (error) {
    // 错误已在拦截器中处理
  }
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="user-manage-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :model="queryForm" inline>
        <el-form-item label="用户名">
          <el-input v-model="queryForm.username" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="queryForm.realName" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择" clearable style="width: 100px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 表格 -->
    <el-card class="table-card">
      <template #header>
        <div class="toolbar">
          <span>用户列表</span>
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增</el-button>
        </div>
      </template>
      
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="realName" label="姓名" min-width="90" />
        <el-table-column prop="phone" label="手机号" min-width="130" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="roles" label="角色" min-width="150">
          <template #default="{ row }">
            <el-tag v-for="role in row.roles" :key="role" size="small" class="role-tag">
              {{ role }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.statusText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录" min-width="160" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" link size="small" @click="handleAssignRole(row)">分配角色</el-button>
            <el-button type="warning" link size="small" @click="handleResetPassword(row)">重置密码</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
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
    
    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="userForm" label-width="80px">
        <el-form-item label="用户名" required>
          <el-input v-model="userForm.username" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="姓名" required>
          <el-input v-model="userForm.realName" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="userForm.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="userForm.email" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
    
    <!-- 角色分配对话框 -->
    <el-dialog v-model="roleDialogVisible" :title="`分配角色 - ${roleUserName}`" width="500px">
      <el-checkbox-group v-model="selectedRoleIds">
        <div v-for="role in allRoles" :key="role.id" class="role-item">
          <el-checkbox :value="role.id">
            {{ role.roleName }} ({{ role.roleCode }})
          </el-checkbox>
          <div class="role-desc">{{ role.description }}</div>
        </div>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveRoles">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.user-manage-container {
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

.role-tag {
  margin-right: 4px;
}

.role-item {
  margin-bottom: 12px;
  
  .role-desc {
    margin-left: 24px;
    font-size: 12px;
    color: #999;
  }
}
</style>
