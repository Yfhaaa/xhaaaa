<script setup>
defineProps({ status: String });
</script>
<template>
  <el-tag
    :type="
      { 已通过: 'success', 已完成: 'success', 已退回: 'danger', 待审核: 'warning', 待提交: 'info' }[status] ||
      'info'
    "
    effect="light"
    round
    >{{ status || "待提交" }}</el-tag
  >
</template>
