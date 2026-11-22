<script setup>
defineProps({ modelValue: Object, reference: Object });
const emit = defineEmits(["change"]);
</script>
<template>
  <div class="toolbar statistics-filters">
    <el-select
      v-for="(label, key) in {
        majorId: '专业',
        classId: '班级',
        teacherId: '指导教师',
        cohort: '届次',
      }"
      :key="key"
      v-model="modelValue[key]"
      :placeholder="label"
      clearable
      @change="emit('change')"
    >
      <el-option
        v-for="row in reference[
          { majorId: 'majors', classId: 'classes', teacherId: 'teachers', cohort: 'cohorts' }[key]
        ] || []"
        :key="row.id"
        :label="row.name"
        :value="key === 'cohort' ? row.name : row.id"
    /></el-select>
  </div>
</template>
