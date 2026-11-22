<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from "vue";
import { get } from "../../api/advanced";
import ChartPanel from "../../components/ChartPanel.vue";
const data = ref({ stages: [], groups: [], teacherLoad: [], scoreDistribution: {}, trend: {}, warnings: [] });
const busy = ref(false);
let timer;
/** 顶部核心指标：由学生进度明细汇总 */
const kpis = computed(() => {
  const list = data.value.students || [];
  const avg = list.length ? Math.round(list.reduce((a, b) => a + b.progress, 0) / list.length) : 0;
  return [
    { label: "在册学生", value: list.length, unit: "人" },
    { label: "平均进度", value: avg, unit: "%" },
    { label: "已确定课题", value: list.filter((s) => s.topic_id).length, unit: "人" },
    { label: "成绩已发布", value: list.filter((s) => s.score_published).length, unit: "人" },
    { label: "完成归档", value: list.filter((s) => s.archived).length, unit: "人" },
    { label: "逾期预警", value: (data.value.warnings || []).length, unit: "项", alert: true },
  ];
});
const updatedText = computed(() =>
  data.value.updatedAt ? new Date(data.value.updatedAt).toLocaleString("zh-CN", { hour12: false }) : "正在加载",
);
async function refresh() {
  if (busy.value) return;
  busy.value = true;
  try {
    data.value = await get("/statistics/board");
  } finally {
    busy.value = false;
  }
}
const labels = {
  selection: "确定选题",
  task: "任务书",
  proposal: "开题",
  midterm: "中检",
  draft: "初稿",
  final: "定稿",
  defense: "答辩",
};
const common = {
  textStyle: { color: "#afcfc5" },
  tooltip: { trigger: "item" },
  color: ["#53bd9c", "#d8be7b", "#75b8c7", "#9cc1a8", "#639c85", "#baab7c", "#4a8c7b"],
};
const funnel = computed(() => ({
  ...common,
  series: [
    {
      type: "funnel",
      left: "10%",
      width: "80%",
      sort: "none",
      label: { color: "#e5f4ee", formatter: "{b}: {c}", position: "inside" },
      data: data.value.stages.map((s) => ({ ...s, name: labels[s.name] })),
    },
  ],
}));
function bar(names, values, type = "bar") {
  return {
    ...common,
    tooltip: { trigger: "axis" },
    grid: { left: 40, right: 20, top: 20, bottom: 65, containLabel: true },
    xAxis: {
      type: "category",
      data: names,
      axisLabel: { color: "#afcfc5", rotate: names.length > 10 ? 45 : 0 },
    },
    yAxis: { type: "value", axisLabel: { color: "#afcfc5" }, splitLine: { lineStyle: { color: "#28544d" } } },
    series: [{ type, data: values, smooth: true }],
  };
}
const majors = computed(() =>
  bar(
    data.value.groups.map((g) => g.name),
    data.value.groups.map((g) => g.average_progress),
  ),
);
const load = computed(() =>
  bar(
    data.value.teacherLoad.map((g) => g.name),
    data.value.teacherLoad.map((g) => g.value),
  ),
);
const scores = computed(() => ({
  ...common,
  series: [
    {
      type: "pie",
      radius: ["35%", "65%"],
      label: { color: "#d6ece7" },
      data: Object.entries(data.value.scoreDistribution)
        .map(([name, value]) => ({ name, value }))
        .filter((item) => item.value > 0),
    },
  ],
}));
const trend = computed(() =>
  bar(
    Object.keys(data.value.trend).map((d) => d.slice(5)),
    Object.values(data.value.trend),
    "line",
  ),
);
function fullscreen() {
  if (document.fullscreenElement) document.exitFullscreen();
  else document.documentElement.requestFullscreen();
}
onMounted(() => {
  refresh();
  timer = setInterval(refresh, 30000);
});
onBeforeUnmount(() => clearInterval(timer));
</script>
<template>
  <div class="board">
    <header>
      <div>
        <h1>毕业设计 · 全过程数据大屏</h1>
        <span class="subtle">更新于 {{ updatedText }} · 每 30 秒自动刷新</span>
      </div>
      <div class="actions">
        <el-button :loading="busy" @click="refresh">刷新</el-button
        ><el-button @click="fullscreen">全屏</el-button>
        <el-button @click="$router.push('/')">返回工作台</el-button>
      </div>
    </header>
    <div class="board-kpis">
      <div v-for="k in kpis" :key="k.label" class="board-kpi" :class="{ alert: k.alert && k.value }">
        <span>{{ k.label }}</span>
        <strong>{{ k.value }}<small>{{ k.unit }}</small></strong>
      </div>
    </div>
    <div class="board-grid">
      <section class="board-card">
        <h3>各阶段完成漏斗</h3>
        <ChartPanel :option="funnel" />
      </section>
      <section class="board-card">
        <h3>专业进度对比（%）</h3>
        <ChartPanel :option="majors" />
      </section>
      <section class="board-card">
        <h3>教师指导负载</h3>
        <ChartPanel :option="load" />
      </section>
      <section class="board-card">
        <h3>成绩分布</h3>
        <ChartPanel :option="scores" />
      </section>
      <section class="board-card">
        <h3>近30天提交趋势</h3>
        <ChartPanel :option="trend" />
      </section>
      <section class="board-card">
        <h3>逾期预警 · {{ data.warnings.length }} 项</h3>
        <div v-if="!data.warnings.length" class="board-clear">
          <strong>✓</strong>
          <span>当前无逾期事项，各节点按期推进</span>
        </div>
        <div v-else class="warning-window">
          <div class="warning-list">
            <p v-for="(warning, index) in [...data.warnings, ...data.warnings]" :key="index">
              {{ warning.student }} · {{ warning.stage }} <br />截止 {{ warning.deadline }}
            </p>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.board-kpis {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 14px;
  margin-bottom: 16px;
}
.board-kpi {
  padding: 14px 18px;
  border: 1px solid rgb(216 190 123 / 22%);
  border-radius: 10px;
  background: linear-gradient(135deg, rgb(255 255 255 / 6%), rgb(255 255 255 / 2%));
}
.board-kpi span {
  color: #8fb3a5;
  font-size: 12px;
}
.board-kpi strong {
  display: block;
  margin-top: 6px;
  color: #e9d39a;
  font: 700 30px/1 Georgia, serif;
}
.board-kpi small {
  margin-left: 4px;
  color: #8fb3a5;
  font: 400 12px sans-serif;
}
.board-kpi.alert strong {
  color: #f08c7d;
}
.board-clear {
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: center;
  justify-content: center;
  height: 220px;
  color: #8fb3a5;
}
.board-clear strong {
  display: grid;
  place-items: center;
  width: 64px;
  height: 64px;
  border: 2px solid rgb(126 207 170 / 50%);
  border-radius: 50%;
  color: #7ecfaa;
  font-size: 30px;
}
@media (max-width: 1100px) {
  .board-kpis {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}
</style>
