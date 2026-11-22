<script setup>
import * as guidanceApi from "../../api/guidance";
import { listStudents } from "../../api/system";
import { ref, reactive, onMounted } from "vue";
import { ElMessage } from "element-plus";
import { dateText, stageNames } from "../../utils/format";
import { useAuth } from "../../stores/auth";
import StatusTag from "../../components/StatusTag.vue";
const auth = useAuth();
const guidance = ref([]);
const reviews = ref([]);
const students = ref([]);
const dialog = ref(false);
const loading = ref(false);
const saving = ref(false);
const formRef = ref();
const form = reactive({ studentId: null, guidanceAt: "", content: "" });
const rules = {
  studentId: [{ required: true, message: "请选择学生" }],
  guidanceAt: [{ required: true, message: "请选择指导时间" }],
  content: [{ required: true, message: "请填写指导内容" }],
};
async function refresh() {
  loading.value = true;
  try {
    [guidance.value, reviews.value, students.value] = await Promise.all([
      guidanceApi.listGuidance(),
      guidanceApi.listReviews(),
      listStudents(),
    ]);
  } finally {
    loading.value = false;
  }
}
function open() {
  Object.assign(form, {
    studentId: students.value[0]?.student_id,
    guidanceAt: new Date().toLocaleString("sv-SE").replace(" ", "T"),
    content: "",
  });
  dialog.value = true;
}
async function save() {
  if (!(await formRef.value.validate().catch(() => false))) return;
  saving.value = true;
  try {
    await guidanceApi.createGuidance(form);
    dialog.value = false;
    ElMessage.success("指导记录已保存");
    await refresh();
  } finally {
    saving.value = false;
  }
}
onMounted(refresh);
</script>
<template>
  <div>
    <PageHeader title="指导记录"
      ><el-button v-if="auth.role === 'TEACHER'" v-permission="'guidance:write'" @click="open()"
        >记录指导</el-button
      ></PageHeader
    >
    <section class="panel">
      <el-skeleton :loading="loading" animated :rows="5"
        ><template #default>
          <DataTable :loading="loading" :data="guidance">
            <el-table-column label="学生" width="100"
              ><template #default="{ row }">
                <router-link :to="auth.role === 'STUDENT' ? '/my-project' : '/students/' + row.student_id">
                  <el-button link type="primary">{{ row.student_name }}</el-button></router-link
                >
              </template></el-table-column
            >
            <el-table-column prop="teacher_name" label="指导教师" width="110" />
            <el-table-column label="指导时间" width="180">
              <template #default="{ row }">{{ dateText(row.guidance_at) }}</template>
            </el-table-column>
            <el-table-column prop="content" label="指导内容与修改建议" min-width="300" />
          </DataTable> </template
      ></el-skeleton>
    </section>
    <el-dialog v-model="dialog" title="新增指导记录" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="指导学生" prop="studentId">
          <el-select v-model="form.studentId">
            <el-option v-for="s in students" :key="s.student_id" :value="s.student_id" :label="s.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="指导时间" prop="guidanceAt">
          <el-date-picker v-model="form.guidanceAt" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
        <el-form-item label="指导内容与修改建议" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="6" maxlength="10000" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button v-permission="'guidance:write'" type="primary" :loading="saving" @click="save">
          保存记录
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>
