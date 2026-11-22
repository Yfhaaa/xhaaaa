<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from "vue";
import * as echarts from "echarts/core";
import { BarChart, PieChart, LineChart, FunnelChart } from "echarts/charts";
import { GridComponent, TooltipComponent, LegendComponent } from "echarts/components";
import { CanvasRenderer } from "echarts/renderers";
echarts.use([
  BarChart,
  PieChart,
  LineChart,
  FunnelChart,
  GridComponent,
  TooltipComponent,
  LegendComponent,
  CanvasRenderer,
]);
const props = defineProps({ option: Object });
const element = ref();
let chart;
let observer;
onMounted(() => {
  chart = echarts.init(element.value);
  chart.setOption(props.option);
  observer = new ResizeObserver(() => chart.resize());
  observer.observe(element.value);
});
watch(
  () => props.option,
  (value) => chart?.setOption(value, true),
  { deep: true },
);
onBeforeUnmount(() => {
  observer?.disconnect();
  chart?.dispose();
});
</script>
<template><div ref="element" class="chart" role="img" aria-label="毕业设计统计图表"></div></template>
