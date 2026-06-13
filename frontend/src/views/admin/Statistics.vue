<template>
  <div>
    <h2>统计分析</h2>
    <el-form inline style="margin:20px 0">
      <el-form-item label="年份">
        <!-- 修复1：添加 placeholder 和 clearable；label 使用字符串 "2026年" 避免数字类型导致的回显异常 -->
        <el-select
          v-model="year"
          placeholder="选择年份"
          clearable
          style="width: 130px"
          @change="onFilterChange"
        >
          <el-option
            v-for="y in yearOptions"
            :key="y"
            :label="y + '年'"
            :value="y"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="月份(可选)">
        <!-- 修复2：显式指定宽度；placeholder 确保 null 值时显示提示文字 -->
        <el-select
          v-model="month"
          placeholder="全部月份"
          clearable
          style="width: 130px"
          @change="onFilterChange"
        >
          <el-option
            v-for="m in 12"
            :key="m"
            :label="m + '月'"
            :value="m"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="success" :icon="Download" @click="exportCSV" :loading="exportLoading">导出 CSV</el-button>
      </el-form-item>
    </el-form>

    <!-- 当前筛选时间范围提示 -->
    <div style="margin-bottom: 16px; display: flex; align-items: center; gap: 12px;">
      <el-tag type="primary" effect="plain" size="large">
        <el-icon style="margin-right: 4px;"><component :is="Timer" /></el-icon>
        {{ selectedTimeRange }}
      </el-tag>
      <el-button
        v-if="month != null && month !== ''"
        type="warning"
        size="small"
        plain
        @click="month = null; onFilterChange()"
      >
        清除月份筛选，查看全年
      </el-button>
    </div>

    <el-row :gutter="20">
      <el-col :span="24">
        <el-card><template #header>报名人数统计</template>
          <div ref="regChart" style="height:350px"></div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="12">
        <el-card><template #header>各科目通过率</template>
          <div ref="passRateChart" style="height:350px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card><template #header>教练工作量</template>
          <div ref="coachChart" style="height:350px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 教练工作量数据表格（修复总学时无数据、学员数量小数问题） -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <span>教练工作量明细表</span>
              <span style="font-size: 12px; color: var(--text-secondary);">
                统计时段：{{ selectedTimeRange }}
              </span>
            </div>
          </template>
          <el-table
            :data="coachWorkloadTableData"
            stripe
            empty-text="暂无数据"
            max-height="400"
          >
            <el-table-column prop="name" label="教练姓名" width="140" />
            <el-table-column prop="studentCount" label="学员数量（人）" width="140" align="center">
              <template #default="{ row }">
                <span>{{ row.studentCount }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="totalHours" label="总学时（小时）" width="160" align="center">
              <template #default="{ row }">
                <span>{{ row.totalHoursDisplay }}</span>
              </template>
            </el-table-column>
            <el-table-column label="学员人均学时（小时）" min-width="180" align="center">
              <template #default="{ row }">
                <span>{{ row.avgHoursPerStudent }}</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { adminApi } from '../../api'
import { ElMessage } from 'element-plus'
import { Download, Timer } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const year = ref(new Date().getFullYear())
const month = ref(null)
const exportLoading = ref(false)
const yearOptions = Array.from({ length: 5 }, (_, i) => new Date().getFullYear() - i)
const regChart = ref(null); const passRateChart = ref(null); const coachChart = ref(null)
let regChartInstance = null; let passRateChartInstance = null; let coachChartInstance = null
// 存储教练工作量表格数据，供图表和表格共用
const coachWorkloadData = ref({ labels: [], studentCounts: [], totalHours: [] })

// 当前筛选时间范围的文本展示（修复时间选择无反馈问题）
const selectedTimeRange = computed(() => {
  if (month.value) {
    return `${year.value}年${month.value}月（${year.value}-${String(month.value).padStart(2, '0')}-01 至 ${year.value}-${String(month.value).padStart(2, '0')}-${new Date(year.value, month.value, 0).getDate()}）`
  }
  return `${year.value}年 全年（${year.value}-01-01 至 ${year.value}-12-31）`
})

// 教练工作量表格数据（修复学员数量小数、总学时无数据问题）
// 将原始统计数组转换为表格行，强制学员数量为整数，总学时保留一位小数
const coachWorkloadTableData = computed(() => {
  const { labels = [], studentCounts = [], totalHours = [] } = coachWorkloadData.value
  return labels.map((name, i) => {
    const studentCount = Number(studentCounts[i]) || 0
    const hours = Number(totalHours[i]) || 0
    const hoursDisplay = hours.toFixed(1)
    const avg = studentCount > 0 ? (hours / studentCount).toFixed(1) : '-'
    return {
      name,
      studentCount: studentCount,               // 整数
      totalHours: hours,
      totalHoursDisplay: hoursDisplay,          // 保留一位小数显示
      avgHoursPerStudent: avg                   // 人均学时，保留一位小数；无学员时显示 "-"
    }
  })
})

/**
 * 筛选条件变更的统一处理函数
 * 确保 year/month 值已在 v-model 中同步后再触发数据加载，
 * 避免 @change 与 v-model 之间的时序竞态导致下拉框回显异常
 */
function onFilterChange() {
  // 年份为必选：clearable 清除后自动回退到当前年份
  if (!year.value) {
    year.value = new Date().getFullYear()
  }
  // v-model 已同步更新 year/month，此处仅触发数据刷新
  loadStats()
}

onMounted(() => { loadStats() })
onBeforeUnmount(() => {
  regChartInstance?.dispose()
  passRateChartInstance?.dispose()
  coachChartInstance?.dispose()
  window.removeEventListener('resize', handleResize)
})

function handleResize() {
  regChartInstance?.resize()
  passRateChartInstance?.resize()
  coachChartInstance?.resize()
}

async function loadStats() {
  try {
    // 修复：month 为 null/undefined 时传 undefined，axios 会自动剥离该参数，
    // 避免发送 month=null 字符串导致后端解析 400 错误
    const allRes = await adminApi().getAllStats(
      year.value,
      month.value || void 0
    )

    // 存储教练工作量数据，供图表和表格共用
    if (allRes.data?.coachWorkload) {
      coachWorkloadData.value = allRes.data.coachWorkload
    }

    await nextTick()
    initRegChart(allRes.data?.registration)
    initPassRateChart(allRes.data?.passRate)
    initCoachChart(allRes.data?.coachWorkload)
    window.addEventListener('resize', handleResize)
  } catch (e) {
    // 网络错误由 API 拦截器统一提示，此处仅阻止异常传播
  }
}

async function exportCSV() {
  exportLoading.value = true
  try {
    const res = await adminApi().exportStats(year.value, month.value || void 0)
    const blob = new Blob([res], { type: 'text/csv;charset=UTF-8' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `统计报表_${year.value}${month.value ? '_' + month.value + '月' : ''}.csv`
    a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e) {
    ElMessage.error('导出失败')
  } finally {
    exportLoading.value = false
  }
}

function initRegChart(data) {
  if (!regChart.value) return
  if (regChartInstance) regChartInstance.dispose()
  regChartInstance = echarts.init(regChart.value)
  regChartInstance.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: data?.labels || [] },
    yAxis: { type: 'value', name: '报名人数' },
    series: [{ name: '报名人数', type: 'line', data: data?.data || [], smooth: true, areaStyle: { opacity: 0.3 } }]
  })
}

function initPassRateChart(data) {
  if (!passRateChart.value) return
  if (passRateChartInstance) passRateChartInstance.dispose()
  passRateChartInstance = echarts.init(passRateChart.value)
  passRateChartInstance.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: data?.labels || [] },
    yAxis: { type: 'value', name: '通过率(%)', max: 100 },
    series: [{ name: '通过率', type: 'bar', data: data?.data || [], itemStyle: { color: '#67C23A' } }]
  })
}

function initCoachChart(data) {
  if (!coachChart.value) return
  if (coachChartInstance) coachChartInstance.dispose()
  coachChartInstance = echarts.init(coachChart.value)
  // 修复：学员数量使用左侧Y轴（整数），总学时使用右侧Y轴（一位小数），避免数据显示混淆
  const studentData = (data?.studentCounts || []).map(v => Number(v) || 0)
  const hoursData = (data?.totalHours || []).map(v => {
    const n = Number(v)
    return isNaN(n) ? 0 : Math.round(n * 10) / 10  // 保留一位小数
  })
  coachChartInstance.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: function(params) {
        const name = params[0]?.name || ''
        const stu = params.find(p => p.seriesName === '学员数量')
        const hr = params.find(p => p.seriesName === '总学时')
        let html = `<strong>${name}</strong><br/>`
        if (stu) html += `${stu.marker} 学员数量：<b>${stu.value} 人</b><br/>`
        if (hr) html += `${hr.marker} 总学时：<b>${typeof hr.value === 'number' ? hr.value.toFixed(1) : hr.value} 小时</b>`
        return html
      }
    },
    legend: { data: ['学员数量', '总学时'] },
    xAxis: { type: 'category', data: data?.labels || [] },
    yAxis: [
      {
        type: 'value',
        name: '学员数量（人）',
        minInterval: 1,          // 强制整数刻度间隔，避免显示小数
        axisLabel: { formatter: '{value}' }
      },
      {
        type: 'value',
        name: '总学时（小时）',
        axisLabel: { formatter: (v) => v.toFixed(1) }
      }
    ],
    series: [
      {
        name: '学员数量',
        type: 'bar',
        yAxisIndex: 0,           // 绑定左Y轴（整数）
        data: studentData,
        itemStyle: { color: '#409EFF' }
      },
      {
        name: '总学时',
        type: 'bar',
        yAxisIndex: 1,           // 绑定右Y轴（一位小数）
        data: hoursData,
        itemStyle: { color: '#67C23A' }
      }
    ]
  })
}
</script>
