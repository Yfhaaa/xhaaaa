<script setup>
/** 开题报告：学生提交，指导教师审核，退回可修改重提并保留版本。 */
import StageMaterialBoard from "../../components/material/StageMaterialBoard.vue";
</script>

<template>
  <StageMaterialBoard
    :stages="['proposal']"
    title="开题报告"
    description="提交开题报告，审核意见与历次版本全程留痕"
  />
</template>
