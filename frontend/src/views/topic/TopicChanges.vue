<script setup>
import { ref, reactive, computed, onMounted } from "vue";
import { useAuth } from "../../stores/auth";
import { get, post } from "../../api/advanced";
import { ElMessage } from "element-plus";
const auth = useAuth();
const rows = ref([]);
const topics = ref([]);
const loading = ref(true);
const dialog = ref(false);
const selected = ref(null);
const formRef = ref();
const saving = ref(false);
const form = reactive({ newTopicId: null, reason: "" });
const candidates = computed(() =>
  topics.value.filter((t) => t.status === "已通过" && t.major_id === auth.user.major_id),
);
async function refresh() {
  loading.value = true;
  try {
    [rows.value, topics.value] = await Promise.all([get("/topic-changes"), get("/topics")]);
  } finally {
    loading.value = false;
  }
}
function canReview(row) {
  return (
    row.status !== "已退回" &&
    row.approval_step < 4 &&
    ((row.approval_step === 1 && auth.user.id === row.old_teacher_id) ||
      (row.approval_step === 2 && auth.user.id === row.new_teacher_id) ||
      (row.approval_step === 3 && auth.role === "ADMIN"))
  );
}
async function save() {
  if (!(await formRef.value.validate().catch(() => false))) return;
  saving.value = true;
  try {
    await post("/topic-changes", form);
    dialog.value = false;
    ElMessage.success("申请已提交");
    await refresh();
  } finally {
    saving.value = false;
  }
}
async function approve(body) {
  await post(`/topic-changes/${selected.value.id}/approve`, body);
  selected.value = null;
  ElMessage.success("审批已记录");
  await refresh();
}
onMounted(refresh);
</script>
<template>
  <div>
    <PageHeader title="课题变更" description="原指导教师 → 新指导教师 → 管理员，逐级留痕">
      <el-button
        v-if="auth.role === 'STUDENT'"
        v-permission="'topic:read'"
        type="primary"
        @click="dialog = true"
        >申请变更</el-button
      >
    </PageHeader>
    <section class="panel">
      <DataTable :data="rows" :loading="loading">
        <el-table-column label="学生" width="100"
          ><template #default="{ row }">
            <router-link :to="auth.role === 'STUDENT' ? '/my-project' : '/students/' + row.student_id">
              <el-button link type="primary">{{ row.student_name }}</el-button></router-link
            >
          </template></el-table-column
        >
        <el-table-column prop="old_title" label="原课题" min-width="200" />
        <el-table-column prop="new_title" label="新课题" min-width="200" />
        <el-table-column prop="reason" label="变更理由" min-width="180" show-overflow-tooltip />
        <el-table-column prop="status" label="审批状态" width="140" />
        <el-table-column label="操作" fixed="right" width="150"
          ><template #default="{ row }">
            <el-button
              v-if="canReview(row)"
              v-permission="'topic:write'"
              link
              type="primary"
              @click="selected = row"
              >审批</el-button
            >
            <el-popover width="360" trigger="click"
              ><template #reference><el-button link>留痕</el-button></template>
              <p>{{ row.old_teacher }} → {{ row.new_teacher }} → 管理员</p>
              <p v-for="h in row.history" :key="h.id">
                {{ h.result }} · {{ h.comment }} · {{ h.createdAt }}
              </p></el-popover
            ></template
          ></el-table-column
        >
      </DataTable>
    </section>
    <el-dialog v-model="dialog" title="申请课题变更" width="560px">
      <el-form ref="formRef" :model="form" label-position="top">
        <el-form-item label="新课题" prop="newTopicId" :rules="[{ required: true, message: '请选择课题' }]">
          <el-select v-model="form.newTopicId" filterable
            ><el-option
              v-for="t in candidates"
              :key="t.id"
              :value="t.id"
              :label="t.title + ' · ' + t.teacher_name" /></el-select
        ></el-form-item>
        <el-form-item label="变更理由" prop="reason" :rules="[{ required: true, message: '请填写理由' }]">
          <el-input v-model="form.reason" type="textarea" :rows="4" maxlength="2000" /></el-form-item
      ></el-form>
      <template #footer
        ><el-button v-permission="'topic:write'" type="primary" :loading="saving" @click="save"
          >提交申请</el-button
        ></template
      ></el-dialog
    >
    <ReviewDialog :model-value="!!selected" @update:model-value="selected = null" @submit="approve" />
  </div>
</template>
