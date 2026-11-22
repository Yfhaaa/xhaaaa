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
    <PageHeader title="审核留痕"></PageHeader>
    <section class="panel">
      <el-skeleton :loading="loading" animated :rows="5"
        ><template #default>
          <div class="toolbar">
            <span class="subtle">材料的所有版本均保留审核人、审核时间、处理结果与意见</span>
            <router-link to="/materials">
              <el-button type="primary" plain> 处理材料待办 ↗ </el-button>
            </router-link>
          </div>
          <DataTable :loading="loading" :data="reviews">
            <el-table-column label="学生" width="100"
              ><template #default="{ row }">
                <router-link :to="auth.role === 'STUDENT' ? '/my-project' : '/students/' + row.student_id">
                  <el-button link type="primary">{{ row.student_name }}</el-button></router-link
                >
              </template></el-table-column
            >
            <el-table-column label="业务环节" width="110">
              <template #default="{ row }">
                {{
                  stageNames[row.business_type] ||
                  { topic: "课题审核", selection: "双选确认" }[row.business_type]
                }}
              </template>
            </el-table-column>
            <el-table-column prop="reviewer_name" label="审核人" width="100" />
            <el-table-column label="结果" width="100">
              <template #default="{ row }"><StatusTag :status="row.result" /></template>
            </el-table-column>
            <el-table-column prop="comment" label="审核意见" min-width="240" />
            <el-table-column label="时间" width="180">
              <template #default="{ row }">{{ dateText(row.created_at) }}</template>
            </el-table-column>
          </DataTable>
        </template></el-skeleton
      >
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
        <el-button v-permission="'material:write'" type="primary" :loading="saving" @click="save">
          保存记录
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>
