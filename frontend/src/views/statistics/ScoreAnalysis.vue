<script setup>
import { ref, computed, onMounted } from "vue";
import { get, download } from "../../api/advanced";
import ChartPanel from "../../components/ChartPanel.vue";
const rows = ref([]);
const loading = ref(true);
const option = computed(() => {
  const bins = [0, 0, 0, 0, 0];
  for (const s of rows.value) {
    const v = Number(s.total_score);
    bins[v >= 90 ? 0 : v >= 80 ? 1 : v >= 70 ? 2 : v >= 60 ? 3 : 4]++;
  }
  return {
    color: ["#32775d"],
    tooltip: {},
    xAxis: { type: "category", data: ["优秀", "良好", "中等", "及格", "不及格"] },
    yAxis: { type: "value", minInterval: 1 },
    series: [{ type: "bar", data: bins }],
  };
});
onMounted(async () => {
  try {
    rows.value = await get("/scores");
  } finally {
    loading.value = false;
  }
});
</script>
<template>
  <div>
    <PageHeader title="成绩分析"
      ><el-button @click="download('/exports/scores', '成绩表.xlsx')">导出成绩表</el-button>
    </PageHeader>
    <section class="panel">
      <el-skeleton :loading="loading" :rows="5" animated
        ><template #default> <ChartPanel :option="option" /></template
      ></el-skeleton>
    </section>
  </div>
</template>
