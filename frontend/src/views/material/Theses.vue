<script setup>
/** 论文提交：初稿与定稿两个阶段，定稿审核前须通过查重。 */
import StageMaterialBoard from "../../components/material/StageMaterialBoard.vue";
</script>

<template>
  <StageMaterialBoard
    :stages="['draft', 'final']"
    title="论文提交"
    description="论文初稿与定稿分阶段提交；定稿须先登记查重结果且重复率不超过阈值"
  />
</template>
