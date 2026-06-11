<template>
  <div>
    <h2>统计分析</h2>
    <el-form inline style="margin:20px 0">
      <el-form-item label="年份">
        <el-select v-model="year" @change="loadStats">
          <el-option v-for="y in yearOptions" :key="y" :label="y" :value="y" />
        </el-select>
      </el-form-item>
      <el-form-item label="月份(可选)">
        <el-select v-model="month" @change="loadStats" clearable placeholder="全部月份">
          <el-option v-for="m in 12" :key="m" :label="m+'月'" :value="m" />
        </el-select>
      </el-form-item>
    </el-form>

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
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { adminApi } from '../../api'
import * as echarts from 'echarts'

const year = ref(new Date().getFullYear())
const month = ref(null)
const yearOptions = Array.from({ length: 5 }, (_, i) => new Date().getFullYear() - i)
const regChart = ref(null); const passRateChart = ref(null); const coachChart = ref(null)
let regChartInstance = null; let passRateChartInstance = null; let coachChartInstance = null

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
    const allRes = await adminApi().getAllStats(year.value)

    await nextTick()
    initRegChart(allRes.data?.registration)
    initPassRateChart(allRes.data?.passRate)
    initCoachChart(allRes.data?.coachWorkload)
    window.addEventListener('resize', handleResize)
  } catch (e) {}
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
  coachChartInstance.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['学员数量', '总学时'] },
    xAxis: { type: 'category', data: data?.labels || [] },
    yAxis: [{ type: 'value', name: '学员数量' }, { type: 'value', name: '总学时' }],
    series: [
      { name: '学员数量', type: 'bar', data: data?.studentCounts || [] },
      { name: '总学时', type: 'bar', data: (data?.totalHours || []).map(h => Number(h) || 0) }
    ]
  })
}
</script>
