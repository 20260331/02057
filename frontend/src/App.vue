<script setup lang="ts">
// 根组件
</script>

<template>
  <el-config-provider :locale="zhCn">
    <router-view />
  </el-config-provider>
</template>

<script lang="ts">
import zhCn from 'element-plus/es/locale/lang/zh-cn'

export default {
  data() {
    return {
      zhCn
    }
  }
}
</script>

<style lang="scss">
#app {
  width: 100%;
  height: 100%;
}
</style>
