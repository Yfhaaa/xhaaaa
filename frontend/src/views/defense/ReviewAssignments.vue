<script setup>
import { ref, reactive, onMounted } from "vue";
import { get, post } from "../../api/advanced";
import { downloadFile } from "../../api/material";
import { ElMessage } from "element-plus";
const rows = ref([]);
const students = ref([]);
const reference = ref({ teachers: [] });
const loading = ref(true);
const saving = ref(false);
const dialog = ref(false);
const selected = ref(null);
const formRef = ref();
const form = reactive({ studentIds: [], reviewerId: null, score: 85, approved: true, comment: "" });
async function refresh() {
  loading.value = true;
  try {
    [rows.value, students.value, reference.value] = await Promise.all([
      get("/thesis-reviews"),
      get("/students"),
      get("/reference"),
    ]);
  } finally {
    loading.value = false;
  }
}
async function assign() {
  if (!(await formRef.value.validate().catch(() => false))) return;
  saving.value = true;
  try {
    await post("/thesis-reviews/assign", {
      studentIds: form.studentIds,
      reviewerId: form.reviewerId || undefined,
    });
    dialog.value = false;
    ElMessage.success("分配完成");
    await refresh();
  } finally {
    saving.value = false;
  }
}
async function review() {
  if (!(await formRef.value.validate().catch(() => false))) return;
  saving.value = true;
  try {
    await post(`/thesis-reviews/${selected.value.id}/review`, form);
    selected.value = null;
    ElMessage.success("评阅完成");
    await refresh();
  } finally {
    saving.value = false;
  }
}
onMounted(refresh);
</script>
<template>
  <div>
    <PageHeader title="评阅分配" description="定稿通过后分配评阅教师，自动回避本人指导教师">
      <el-button v-permission="'system:manage'" type="primary" @click="dialog = true"
        >分配 / 批量随机分配</el-button
      >
    </PageHeader>
    <section class="panel">
      <DataTable :data="rows" :loading="loading"
        ><el-table-column prop="student_name" label="学生" width="100" />
        <el-table-column prop="title" label="定稿论文" min-width="220" />
        <el-table-column prop="reviewer_name" label="评阅教师" width="110" />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column prop="score" label="评阅分" width="90" />
        <el-table-column label="答辩资格" width="110"
          ><template #default="{ row }">
            {{
              row.agree_defense === 1 ? "同意答辩" : row.agree_defense === 0 ? "不同意" : "待评阅"
            }}</template
          ></el-table-column
        >
        <el-table-column prop="comment" label="评阅意见" min-width="200" />
        <el-table-column label="操作" fixed="right" width="150"
          ><template #default="{ row }">
            <el-button link type="primary" @click="downloadFile(row)">论文</el-button></template
          ></el-table-column
        >
      </DataTable>
    </section>
    <el-dialog v-model="dialog" title="分配论文评阅" width="560px">
      <el-form ref="formRef" :model="form" label-position="top"
        ><el-form-item
          label="学生（可多选）"
          prop="studentIds"
          :rules="[{ required: true, type: 'array', min: 1, message: '请选择学生' }]"
        >
          <el-select v-model="form.studentIds" multiple filterable
            ><el-option
              v-for="s in students"
              :key="s.student_id"
              :label="s.name"
              :value="s.student_id" /></el-select></el-form-item
        ><el-form-item label="评阅教师（留空为随机）">
          <el-select v-model="form.reviewerId" clearable
            ><el-option
              v-for="t in reference.teachers"
              :key="t.id"
              :label="t.name"
              :value="t.id" /></el-select></el-form-item
      ></el-form>
      <template #footer
        ><el-button :loading="saving" type="primary" @click="assign">确认分配</el-button></template
      >
    </el-dialog>
  </div>
</template>
