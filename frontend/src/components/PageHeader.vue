<script setup>
defineProps({ title: String, description: { type: String, default: "毕业设计全过程协同管理" } });
</script>
<template>
  <div class="page-heading">
    <div>
      <h1>{{ title }}</h1>
      <p>{{ description }}</p>
    </div>
    <div class="actions"><slot /></div>
  </div>
</template>
