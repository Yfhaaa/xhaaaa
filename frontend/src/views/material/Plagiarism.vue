<script setup>
import { ref, reactive, onMounted } from "vue";
import { useAuth } from "../../stores/auth";
import { get, post } from "../../api/advanced";
import { downloadFile } from "../../api/material";
import { ElMessage } from "element-plus";
const auth = useAuth();
const rows = ref([]);
const students = ref([]);
const loading = ref(true);
const saving = ref(false);
const dialog = ref(false);
const formRef = ref();
const form = reactive({ studentId: null, platform: "", rate: 0, fileId: null });
async function refresh() {
  loading.value = true;
  try {
    [rows.value, students.value] = await Promise.all([get("/plagiarism-checks"), get("/students")]);
  } finally {
    loading.value = false;
  }
}
async function save() {
  if (!(await formRef.value.validate().catch(() => false))) return;
  saving.value = true;
  try {
    await post("/plagiarism-checks", form);
    dialog.value = false;
    ElMessage.success("查重结果已登记");
    await refresh();
  } finally {
    saving.value = false;
  }
}
onMounted(refresh);
</script>
<template>
  <div>
    <PageHeader title="查重记录" description="查重报告绑定定稿版本，默认阈值30%，可由流程节点调整">
      <el-button
        v-if="auth.role !== 'STUDENT'"
        v-permission="'material:write'"
        type="primary"
        @click="
          dialog = true;
          form.fileId = null;
        "
        >登记查重</el-button
      ></PageHeader
    >
    <section class="panel">
      <DataTable :data="rows" :loading="loading"
        ><el-table-column label="学生" width="100"
          ><template #default="{ row }">
            <router-link :to="auth.role === 'STUDENT' ? '/my-project' : '/students/' + row.student_id">
              <el-button link type="primary">{{ row.student_name }}</el-button></router-link
            >
          </template></el-table-column
        >
        <el-table-column prop="platform" label="查重平台" min-width="160" />
        <el-table-column prop="version" label="定稿版本" width="100" />
        <el-table-column label="重复率 / 阈值" width="150"
          ><template #default="{ row }">
            <el-tag :type="row.rate > row.threshold ? 'danger' : 'success'"
              >{{ row.rate }}% / {{ row.threshold }}%</el-tag
            >
          </template></el-table-column
        ><el-table-column prop="registrar_name" label="登记人" width="110" />
        <el-table-column prop="created_at" label="登记时间" min-width="180" />
        <el-table-column label="操作" fixed="right" width="110"
          ><template #default="{ row }">
            <el-button link type="primary" @click="downloadFile(row)">下载报告</el-button></template
          ></el-table-column
        >
      </DataTable>
    </section>
    <el-dialog v-model="dialog" title="登记定稿查重结果" width="560px">
      <el-form ref="formRef" :model="form" label-position="top"
        ><el-form-item label="学生" prop="studentId" :rules="[{ required: true, message: '请选择学生' }]"
          ><StudentPicker v-model="form.studentId" :students="students"
        /></el-form-item>
        <el-form-item label="查重平台" prop="platform" :rules="[{ required: true, message: '请填写平台' }]">
          <el-input v-model="form.platform" maxlength="100" /></el-form-item
        ><el-form-item label="重复率（%）">
          <el-input-number v-model="form.rate" :min="0" :max="100" :precision="2"
        /></el-form-item>
        <el-form-item label="报告附件" prop="fileId" :rules="[{ required: true, message: '请上传报告' }]">
          <FileUploader v-model="form.fileId" /></el-form-item></el-form
      ><template #footer>
        <el-button v-permission="'material:write'" type="primary" :loading="saving" @click="save"
          >登记结果</el-button
        ></template
      ></el-dialog
    >
  </div>
</template>
