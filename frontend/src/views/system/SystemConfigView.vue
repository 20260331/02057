<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { systemConfigApi, type SystemConfig } from '@/api/system'
import { courseApi, type SelectionPhaseInfo } from '@/api/course'

const activeTab = ref('semester')
const loading = ref(false)

const configs = ref<SystemConfig[]>([])
const phaseInfo = ref<SelectionPhaseInfo | null>(null)

// 学期表单
const semesterForm = ref({
  current_semester: '',
  semester_start_date: '',
  semester_end_date: ''
})

// 选课阶段时间表单
const selectionForm = ref({
  selection_phase: '',
  lottery_enabled: 'true',
  pre_selection_start_time: '',
  pre_selection_end_time: '',
  formal_selection_start_time: '',
  formal_selection_end_time: '',
  adjustment_start_time: '',
  adjustment_end_time: ''
})

const phaseTagType = computed(() => {
  if (!phaseInfo.value) return 'info'
  const map: Record<string, string> = {
    NOT_STARTED: 'info',
    PRE_SELECTION: 'warning',
    FORMAL_SELECTION: 'success',
    ADJUSTMENT: '',
    CLOSED: 'danger'
  }
  return map[phaseInfo.value.currentPhase] || 'info'
})

const loadConfigs = async () => {
  loading.value = true
  try {
    const [configRes, phaseRes] = await Promise.all([
      systemConfigApi.getAllConfigs(),
      courseApi.getPhaseInfo()
    ])
    if (configRes.code === 200) {
      configs.value = configRes.data
      configRes.data.forEach(config => {
        if (config.configKey === 'current_semester') semesterForm.value.current_semester = config.configValue
        if (config.configKey === 'semester_start_date') semesterForm.value.semester_start_date = config.configValue
        if (config.configKey === 'semester_end_date') semesterForm.value.semester_end_date = config.configValue
        if (config.configKey === 'selection_phase') selectionForm.value.selection_phase = config.configValue
        if (config.configKey === 'lottery_enabled') selectionForm.value.lottery_enabled = config.configValue
        if (config.configKey === 'pre_selection_start_time') selectionForm.value.pre_selection_start_time = config.configValue
        if (config.configKey === 'pre_selection_end_time') selectionForm.value.pre_selection_end_time = config.configValue
        if (config.configKey === 'formal_selection_start_time') selectionForm.value.formal_selection_start_time = config.configValue
        if (config.configKey === 'formal_selection_end_time') selectionForm.value.formal_selection_end_time = config.configValue
        if (config.configKey === 'adjustment_start_time') selectionForm.value.adjustment_start_time = config.configValue
        if (config.configKey === 'adjustment_end_time') selectionForm.value.adjustment_end_time = config.configValue
      })
    }
    if (phaseRes.code === 200) {
      phaseInfo.value = phaseRes.data
    }
  } catch (error) {
    // handled
  } finally {
    loading.value = false
  }
}

const saveSemesterConfig = async () => {
  try {
    await systemConfigApi.batchUpdateConfigs({
      current_semester: semesterForm.value.current_semester,
      semester_start_date: semesterForm.value.semester_start_date,
      semester_end_date: semesterForm.value.semester_end_date
    })
    ElMessage.success('学期配置保存成功')
  } catch (error) {
    // handled
  }
}

const saveSelectionConfig = async () => {
  try {
    await systemConfigApi.batchUpdateConfigs({
      selection_phase: selectionForm.value.selection_phase,
      lottery_enabled: selectionForm.value.lottery_enabled,
      pre_selection_start_time: selectionForm.value.pre_selection_start_time,
      pre_selection_end_time: selectionForm.value.pre_selection_end_time,
      formal_selection_start_time: selectionForm.value.formal_selection_start_time,
      formal_selection_end_time: selectionForm.value.formal_selection_end_time,
      adjustment_start_time: selectionForm.value.adjustment_start_time,
      adjustment_end_time: selectionForm.value.adjustment_end_time
    })
    ElMessage.success('选课配置保存成功')
    // 重新加载阶段信息
    const phaseRes = await courseApi.getPhaseInfo()
    if (phaseRes.code === 200) {
      phaseInfo.value = phaseRes.data
    }
  } catch (error) {
    // handled
  }
}

onMounted(() => {
  loadConfigs()
})
</script>

<template>
  <div class="system-config-container">
    <el-card>
      <el-tabs v-model="activeTab">
        <!-- 学期配置 -->
        <el-tab-pane label="学年学期设置" name="semester">
          <el-form :model="semesterForm" label-width="120px" style="max-width: 600px" v-loading="loading">
            <el-form-item label="当前学期">
              <el-input v-model="semesterForm.current_semester" placeholder="如: 2024-2025-1" />
            </el-form-item>
            <el-form-item label="学期开始日期">
              <el-date-picker 
                v-model="semesterForm.semester_start_date" 
                type="date" 
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="学期结束日期">
              <el-date-picker 
                v-model="semesterForm.semester_end_date" 
                type="date"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveSemesterConfig">保存配置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <!-- 选课阶段配置 -->
        <el-tab-pane label="选课阶段管理" name="selection">
          <div v-loading="loading">
            <!-- 当前阶段状态 -->
            <el-card shadow="never" class="phase-status-card">
              <template #header>
                <span>当前选课状态</span>
              </template>
              <el-descriptions v-if="phaseInfo" :column="2" border>
                <el-descriptions-item label="当前阶段">
                  <el-tag :type="phaseTagType as any" size="large">{{ phaseInfo.currentPhaseName }}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="抽签机制">
                  <el-tag :type="phaseInfo.lotteryEnabled ? 'success' : 'info'" size="small">
                    {{ phaseInfo.lotteryEnabled ? '已启用' : '未启用' }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="允许选课">
                  <el-tag :type="phaseInfo.canSelect ? 'success' : 'danger'" size="small">
                    {{ phaseInfo.canSelect ? '是' : '否' }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="允许退课">
                  <el-tag :type="phaseInfo.canWithdraw ? 'success' : 'danger'" size="small">
                    {{ phaseInfo.canWithdraw ? '是' : '否' }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="选课模式" :span="2">
                  {{ phaseInfo.selectionMode }}
                </el-descriptions-item>
              </el-descriptions>
            </el-card>

            <!-- 阶段时间配置 -->
            <el-card shadow="never" class="phase-config-card">
              <template #header>
                <span>阶段时间配置</span>
              </template>
              <el-alert
                title="系统会根据以下时间自动判定当前处于哪个选课阶段，无需手动切换。各阶段时间不应重叠。"
                type="info"
                :closable="false"
                show-icon
                style="margin-bottom: 20px"
              />

              <el-form :model="selectionForm" label-width="130px">
                <!-- 抽签开关 -->
                <el-divider content-position="left">基本设置</el-divider>
                <el-form-item label="预选抽签机制">
                  <el-switch
                    v-model="selectionForm.lottery_enabled"
                    active-value="true"
                    inactive-value="false"
                    active-text="启用"
                    inactive-text="关闭"
                  />
                  <span class="form-tip">启用后，预选阶段超出容量的选课将进入抽签队列</span>
                </el-form-item>

                <!-- 手动阶段（回退方案） -->
                <el-form-item label="手动阶段覆盖">
                  <el-select v-model="selectionForm.selection_phase" style="width: 200px">
                    <el-option label="未开始" value="NOT_STARTED" />
                    <el-option label="预选阶段" value="PRE_SELECTION" />
                    <el-option label="正选阶段" value="FORMAL_SELECTION" />
                    <el-option label="补退选" value="ADJUSTMENT" />
                    <el-option label="已结束" value="CLOSED" />
                  </el-select>
                  <span class="form-tip">仅在未配置分阶段时间时作为回退使用</span>
                </el-form-item>

                <!-- 预选阶段 -->
                <el-divider content-position="left">
                  <el-tag type="warning">预选阶段</el-tag>
                  <span class="divider-desc">学生报名意向课程，超出容量后抽签</span>
                </el-divider>
                <el-form-item label="开始时间">
                  <el-date-picker 
                    v-model="selectionForm.pre_selection_start_time" 
                    type="datetime"
                    value-format="YYYY-MM-DD HH:mm:ss"
                    placeholder="预选开始时间"
                    style="width: 100%"
                  />
                </el-form-item>
                <el-form-item label="结束时间">
                  <el-date-picker 
                    v-model="selectionForm.pre_selection_end_time" 
                    type="datetime"
                    value-format="YYYY-MM-DD HH:mm:ss"
                    placeholder="预选结束时间"
                    style="width: 100%"
                  />
                </el-form-item>

                <!-- 正选阶段 -->
                <el-divider content-position="left">
                  <el-tag type="success">正选阶段</el-tag>
                  <span class="divider-desc">先到先得，抽签结果公布后的补选时段</span>
                </el-divider>
                <el-form-item label="开始时间">
                  <el-date-picker 
                    v-model="selectionForm.formal_selection_start_time" 
                    type="datetime"
                    value-format="YYYY-MM-DD HH:mm:ss"
                    placeholder="正选开始时间"
                    style="width: 100%"
                  />
                </el-form-item>
                <el-form-item label="结束时间">
                  <el-date-picker 
                    v-model="selectionForm.formal_selection_end_time" 
                    type="datetime"
                    value-format="YYYY-MM-DD HH:mm:ss"
                    placeholder="正选结束时间"
                    style="width: 100%"
                  />
                </el-form-item>

                <!-- 补退选阶段 -->
                <el-divider content-position="left">
                  <el-tag>补退选阶段</el-tag>
                  <span class="divider-desc">最后调整，补选余量课程或退课</span>
                </el-divider>
                <el-form-item label="开始时间">
                  <el-date-picker 
                    v-model="selectionForm.adjustment_start_time" 
                    type="datetime"
                    value-format="YYYY-MM-DD HH:mm:ss"
                    placeholder="补退选开始时间"
                    style="width: 100%"
                  />
                </el-form-item>
                <el-form-item label="结束时间">
                  <el-date-picker 
                    v-model="selectionForm.adjustment_end_time" 
                    type="datetime"
                    value-format="YYYY-MM-DD HH:mm:ss"
                    placeholder="补退选结束时间"
                    style="width: 100%"
                  />
                </el-form-item>

                <el-form-item>
                  <el-button type="primary" @click="saveSelectionConfig">保存选课配置</el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<style lang="scss" scoped>
.system-config-container {
}

.phase-status-card {
  margin-bottom: 20px;
}

.phase-config-card {
  margin-bottom: 20px;
}

.form-tip {
  margin-left: 12px;
  color: #909399;
  font-size: 12px;
}

.divider-desc {
  margin-left: 8px;
  color: #909399;
  font-size: 12px;
  font-weight: normal;
}
</style>
