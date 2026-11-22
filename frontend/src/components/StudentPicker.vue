<script setup>
defineProps({
  modelValue: [Number, String],
  students: { type: Array, default: () => [] },
  multiple: Boolean,
});
defineEmits(["update:modelValue"]);
</script>
<template>
  <el-select
    :model-value="modelValue"
    filterable
    placeholder="请选择学生"
    @update:model-value="$emit('update:modelValue', $event)"
    ><el-option
      v-for="s in students"
      :key="s.student_id"
      :value="s.student_id"
      :label="s.name + ' · ' + (s.topic_title || '尚未选题')"
  /></el-select>
</template>
