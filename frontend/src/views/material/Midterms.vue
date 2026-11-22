<script setup>
/** 中期检查：学生提交中期报告，指导教师检查进度并给出意见。 */
import StageMaterialBoard from "../../components/material/StageMaterialBoard.vue";
</script>

<template>
  <StageMaterialBoard
    :stages="['midterm']"
    title="中期检查"
    description="对照任务书检查阶段进度，及时发现并纠正问题"
  />
</template>
