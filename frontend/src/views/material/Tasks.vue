<script setup>
/** 任务书：指导教师下达，学生查看与下载。 */
import StageMaterialBoard from "../../components/material/StageMaterialBoard.vue";
</script>

<template>
  <StageMaterialBoard
    :stages="['task']"
    title="任务书"
    description="指导教师下达任务书，明确设计内容、要求与进度安排"
  />
</template>
