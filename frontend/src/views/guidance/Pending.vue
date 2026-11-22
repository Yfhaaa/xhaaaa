<script setup>
/** 待我审核：汇总本人指导学生各阶段待审核的材料。 */
import StageMaterialBoard from "../../components/material/StageMaterialBoard.vue";
</script>

<template>
  <StageMaterialBoard
    mode="pending"
    :stages="['task', 'proposal', 'midterm', 'draft', 'final']"
    title="待我审核"
    description="集中处理指导学生提交的各阶段材料"
  />
</template>
