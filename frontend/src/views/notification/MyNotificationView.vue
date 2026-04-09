<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { notificationApi } from '@/api/notification'
import type { NotificationInfo } from '@/api/notification'
import { ElMessage } from 'element-plus'
import { Bell, Check, View } from '@element-plus/icons-vue'

const loading = ref(false)
const notifications = ref<NotificationInfo[]>([])
const filterCategory = ref('')
const filterRead = ref('')

const detailDialogVisible = ref(false)
const detailData = ref<NotificationInfo | null>(null)

const unreadCount = computed(() => notifications.value.filter(n => !n.isRead).length)

const filteredList = computed(() => {
  let list = notifications.value
  if (filterCategory.value) {
    list = list.filter(n => n.category === filterCategory.value)
  }
  if (filterRead.value === 'unread') {
    list = list.filter(n => !n.isRead)
  } else if (filterRead.value === 'read') {
    list = list.filter(n => n.isRead)
  }
  return list
})

const categoryOptions = [
  { value: 'ACADEMIC', label: '教务通知' },
  { value: 'COURSE', label: '选课通知' },
  { value: 'GRADE', label: '成绩通知' },
  { value: 'LEAVE', label: '请假通知' },
  { value: 'SYSTEM', label: '系统通知' }
]

const getCategoryType = (category: string) => {
  const map: Record<string, string> = {
    ACADEMIC: 'warning', COURSE: 'success', GRADE: '', LEAVE: 'info', SYSTEM: 'danger'
  }
  return map[category] || 'info'
}

const getPriorityType = (priority: string) => {
  const map: Record<string, string> = { LOW: 'info', NORMAL: '', HIGH: 'warning', URGENT: 'danger' }
  return map[priority] || 'info'
}

const loadNotifications = async () => {
  loading.value = true
  try {
    const res = await notificationApi.getMyNotifications()
    if (res.code === 200) {
      notifications.value = res.data
    }
  } catch (error) {
    // handled
  } finally {
    loading.value = false
  }
}

const handleView = async (row: NotificationInfo) => {
  try {
    const res = await notificationApi.getNotificationDetail(row.id)
    if (res.code === 200) {
      detailData.value = res.data
      detailDialogVisible.value = true
      row.isRead = true
    }
  } catch (error) {
    // handled
  }
}

const handleMarkAllRead = async () => {
  try {
    await notificationApi.markAllAsRead()
    ElMessage.success('已全部标记为已读')
    notifications.value.forEach(n => (n.isRead = true))
  } catch (error) {
    // handled
  }
}

const formatTime = (time: string) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  const diffMin = Math.floor(diffMs / 60000)
  if (diffMin < 1) return '刚刚'
  if (diffMin < 60) return `${diffMin}分钟前`
  const diffHours = Math.floor(diffMin / 60)
  if (diffHours < 24) return `${diffHours}小时前`
  const diffDays = Math.floor(diffHours / 24)
  if (diffDays < 7) return `${diffDays}天前`
  return time.substring(0, 16).replace('T', ' ')
}

onMounted(() => {
  loadNotifications()
})
</script>

<template>
  <div class="my-notifications" v-loading="loading">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon class="header-icon"><Bell /></el-icon>
            <span>我的通知</span>
            <el-badge v-if="unreadCount > 0" :value="unreadCount" class="unread-badge" />
          </div>
          <div class="header-actions">
            <el-select v-model="filterCategory" placeholder="全部分类" clearable size="small" style="width: 120px; margin-right: 8px">
              <el-option v-for="opt in categoryOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
            </el-select>
            <el-select v-model="filterRead" placeholder="全部状态" clearable size="small" style="width: 120px; margin-right: 12px">
              <el-option label="未读" value="unread" />
              <el-option label="已读" value="read" />
            </el-select>
            <el-button size="small" :icon="Check" :disabled="unreadCount === 0" @click="handleMarkAllRead">
              全部已读
            </el-button>
          </div>
        </div>
      </template>

      <div v-if="filteredList.length === 0" class="empty-state">
        <el-empty description="暂无通知" :image-size="100" />
      </div>

      <div v-else class="notification-list">
        <div
          v-for="item in filteredList" :key="item.id"
          class="notification-item"
          :class="{ unread: !item.isRead }"
          @click="handleView(item)"
        >
          <div class="item-left">
            <div class="item-dot" :class="{ active: !item.isRead }" />
            <div class="item-body">
              <div class="item-title">
                <span class="title-text">{{ item.title }}</span>
                <el-tag size="small" :type="getCategoryType(item.category)" style="margin-left: 8px">
                  {{ item.categoryName }}
                </el-tag>
                <el-tag v-if="item.priority === 'HIGH' || item.priority === 'URGENT'"
                  size="small" :type="getPriorityType(item.priority)" style="margin-left: 4px">
                  {{ item.priorityName }}
                </el-tag>
              </div>
              <div class="item-preview">{{ item.content?.substring(0, 100) }}{{ (item.content?.length || 0) > 100 ? '...' : '' }}</div>
              <div class="item-meta">
                <span>{{ item.senderName }}</span>
                <span class="meta-sep">·</span>
                <span>{{ formatTime(item.sendTime) }}</span>
              </div>
            </div>
          </div>
          <el-button type="primary" link :icon="View" @click.stop="handleView(item)">查看</el-button>
        </div>
      </div>
    </el-card>

    <!-- 通知详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="通知详情" width="600px" destroy-on-close>
      <template v-if="detailData">
        <div class="detail-header">
          <h3>{{ detailData.title }}</h3>
          <div class="detail-meta">
            <el-tag size="small" :type="getCategoryType(detailData.category)">{{ detailData.categoryName }}</el-tag>
            <el-tag v-if="detailData.priority !== 'NORMAL'" size="small" :type="getPriorityType(detailData.priority)">
              {{ detailData.priorityName }}
            </el-tag>
            <span class="meta-info">发送人：{{ detailData.senderName }}</span>
            <span class="meta-info">{{ detailData.sendTime }}</span>
          </div>
        </div>
        <el-divider />
        <div class="detail-body">{{ detailData.content }}</div>
      </template>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.my-notifications {
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .header-left {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;

    .header-icon {
      font-size: 20px;
      color: #409eff;
    }
  }

  .header-actions {
    display: flex;
    align-items: center;
  }

  .unread-badge {
    :deep(.el-badge__content) {
      top: -2px;
    }
  }
}

.notification-list {
  .notification-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 14px 16px;
    border-bottom: 1px solid #f0f0f0;
    cursor: pointer;
    transition: background 0.2s;

    &:hover {
      background: #f5f7fa;
    }

    &:last-child {
      border-bottom: none;
    }

    &.unread {
      background: #f0f7ff;

      &:hover {
        background: #e6f1fc;
      }

      .item-title .title-text {
        font-weight: 600;
      }
    }

    .item-left {
      display: flex;
      align-items: flex-start;
      flex: 1;
      min-width: 0;
    }

    .item-dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: #dcdfe6;
      margin-top: 7px;
      margin-right: 12px;
      flex-shrink: 0;

      &.active {
        background: #409eff;
      }
    }

    .item-body {
      flex: 1;
      min-width: 0;

      .item-title {
        display: flex;
        align-items: center;
        margin-bottom: 4px;

        .title-text {
          font-size: 14px;
          color: #303133;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }

      .item-preview {
        font-size: 13px;
        color: #909399;
        margin-bottom: 4px;
        line-height: 1.5;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }

      .item-meta {
        font-size: 12px;
        color: #c0c4cc;

        .meta-sep {
          margin: 0 4px;
        }
      }
    }
  }
}

.empty-state {
  padding: 40px 0;
}

.detail-header {
  h3 {
    margin: 0 0 8px;
    font-size: 18px;
    color: #303133;
  }

  .detail-meta {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-wrap: wrap;

    .meta-info {
      font-size: 13px;
      color: #909399;
    }
  }
}

.detail-body {
  font-size: 14px;
  line-height: 1.8;
  color: #606266;
  white-space: pre-wrap;
}
</style>
