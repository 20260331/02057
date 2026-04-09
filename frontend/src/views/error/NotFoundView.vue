<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}

const goBack = () => {
  router.back()
}
</script>

<template>
  <div class="not-found-container">
    <div class="content">
      <div class="error-code">404</div>
      <div class="error-message">抱歉，您访问的页面不存在</div>
      <div class="actions">
        <el-button type="primary" @click="goHome">返回首页</el-button>
        <el-button @click="goBack">返回上页</el-button>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.not-found-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: #f0f2f5;
}

.content {
  text-align: center;
  
  .error-code {
    font-size: 120px;
    font-weight: bold;
    color: #1890ff;
    line-height: 1;
    margin-bottom: 20px;
  }
  
  .error-message {
    font-size: 20px;
    color: #606266;
    margin-bottom: 30px;
  }
  
  .actions {
    .el-button {
      margin: 0 10px;
    }
  }
}
</style>
