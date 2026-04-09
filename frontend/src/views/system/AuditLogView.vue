<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import { request } from '@/api/request'
import type { PageResult } from '@/api/types'

interface AuditLog {
  id: number
  userId: number
  username: string
  module: string
  operation: string
  method: string
  requestUrl: string
  requestMethod: string
  ipAddress: string
  executionTime: number
  status: number
  createdAt: string
}

const loading = ref(false)
const tableData = ref<AuditLog[]>([])
const total = ref(0)

const queryForm = reactive({
  page: 1,
  size: 10,
  username: '',
  operation: '',
  dateRange: [] as string[]
})

const loadData = async () => {
  loading.value = true
  try {
    const params: Record<string, unknown> = {
      page: queryForm.page,
      size: queryForm.size
    }
    
    if (queryForm.username) {
      params.username = queryForm.username
    }
    if (queryForm.operation) {
      params.operation = queryForm.operation
    }
    if (queryForm.dateRange && queryForm.dateRange.length === 2) {
      params.startDate = queryForm.dateRange[0]
      params.endDate = queryForm.dateRange[1]
    }
    
    const res = await request.get<PageResult<AuditLog>>('/system/audit-logs', { params })
    if (res.code === 200) {
      tableData.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    console.error('加载审计日志失败', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryForm.page = 1
  loadData()
}

const handleReset = () => {
  queryForm.username = ''
  queryForm.operation = ''
  queryForm.dateRange = []
  queryForm.page = 1
  loadData()
}

const handlePageChange = (page: number) => {
  queryForm.page = page
  loadData()
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  return dateStr.replace('T', ' ').substring(0, 19)
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="audit-log-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :model="queryForm" inline>
        <el-form-item label="用户名">
          <el-input v-model="queryForm.username" placeholder="请输入" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="操作类型">
          <el-input v-model="queryForm.operation" placeholder="请输入" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="queryForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 表格 -->
    <el-card>
      <template #header>
        <span>审计日志</span>
      </template>
      
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="operation" label="操作" min-width="120" />
        <el-table-column prop="module" label="模块" min-width="120" />
        <el-table-column prop="requestUrl" label="请求URL" min-width="200" show-overflow-tooltip />
        <el-table-column prop="requestMethod" label="方法" width="80" />
        <el-table-column prop="ipAddress" label="IP地址" min-width="130" />
        <el-table-column prop="executionTime" label="耗时(ms)" min-width="100" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="操作时间" width="170">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
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
  </div>
</template>

<style lang="scss" scoped>
.audit-log-container {
}

.search-card {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>
