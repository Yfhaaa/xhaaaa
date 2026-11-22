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
    <PageHeader title="我的评阅" description="独立评阅定稿论文，评阅结论决定答辩准入" />
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
            <el-button link type="primary" @click="downloadFile(row)">论文</el-button
            ><el-button
              v-if="row.status === '待评阅'"
              v-permission="'material:write'"
              link
              type="primary"
              @click="
                selected = row;
                form.comment = '';
                form.score = 85;
                form.approved = true;
              "
              >评阅</el-button
            ></template
          ></el-table-column
        >
      </DataTable>
    </section>
    <el-dialog
      :model-value="!!selected"
      title="填写论文评阅"
      width="560px"
      @update:model-value="selected = null"
      ><el-form ref="formRef" :model="form" label-position="top">
        <el-form-item label="评阅分"
          ><el-input-number v-model="form.score" :min="0" :max="100" :precision="2"
        /></el-form-item>
        <el-form-item label="是否同意答辩"
          ><el-switch v-model="form.approved" active-text="同意" inactive-text="不同意"
        /></el-form-item>
        <el-form-item
          label="评阅意见"
          prop="comment"
          :rules="[{ required: true, message: '请填写评阅意见' }]"
        >
          <el-input v-model="form.comment" type="textarea" :rows="5" maxlength="2000" /></el-form-item
      ></el-form>
      <template #footer
        ><el-button v-permission="'defense:write'" :loading="saving" type="primary" @click="review"
          >提交评阅</el-button
        ></template
      >
    </el-dialog>
  </div>
</template>
