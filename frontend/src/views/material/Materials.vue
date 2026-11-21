<script setup>
import * as materialApi from "../../api/material";
import { listStudents } from "../../api/system";
import { downloadFile } from "../../api/material";
import { ref, reactive, computed, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { stageNames, dateText } from "../../utils/format";
import { useAuth } from "../../stores/auth";
import StatusTag from "../../components/StatusTag.vue";
const auth = useAuth(),
  rows = ref([]),
  students = ref([]),
  stage = ref("proposal"),
  loading = ref(false),
  saving = ref(false),
  dialog = ref(false),
  file = ref(null),
  formRef = ref(),
  studentFilter = ref("");
const form = reactive({ studentId: null, title: "", content: "" });
const filtered = computed(() =>
  rows.value.filter(
    (r) =>
      r.stage === stage.value && (!studentFilter.value || r.student_id === studentFilter.value),
  ),
);
const canSubmit = computed(
  () =>
    auth.can("material:write") &&
    ((auth.role === "TEACHER" && stage.value === "task") ||
      (auth.role === "STUDENT" && stage.value !== "task")),
);
const rules = {
  title: [{ required: true, message: "请输入材料标题" }],
  studentId: [{ required: true, message: "请选择指导学生" }],
};
async function refresh() {
  loading.value = true;
  try {
    [rows.value, students.value] = await Promise.all([materialApi.listMaterials(), listStudents()]);
  } finally {
    loading.value = false;
  }
}
function open() {
  Object.assign(form, {
    studentId: students.value[0]?.student_id,
    title: stageNames[stage.value],
    content: "",
  });
  file.value = null;
  dialog.value = true;
}
async function save() {
  if (!(await formRef.value.validate().catch(() => false))) return;
  if (!file.value) {
    ElMessage.warning("请选择材料附件");
    return;
  }
  if (file.value.size > 20 * 1024 * 1024) {
    ElMessage.warning("文件不能超过20MB");
    return;
  }
  saving.value = true;
  try {
    const body = new FormData();
    body.append("file", file.value);
    const uploaded = await materialApi.uploadFile(body);
    await materialApi.submitMaterial(stage.value, { ...form, fileId: uploaded.id });
    dialog.value = false;
    ElMessage.success(stage.value === "task" ? "任务书已下达" : "材料已提交审核");
    await refresh();
  } finally {
    saving.value = false;
  }
}
async function review(row, approved) {
  try {
    const { value } = await ElMessageBox.prompt(
      approved ? "填写通过意见" : "填写具体修改要求",
      "材料审核",
      {
        inputType: "textarea",
        inputValidator: (v) => !!v?.trim() || "审核意见不能为空",
        confirmButtonText: approved ? "通过" : "退回",
      },
    );
    await materialApi.reviewMaterial(row.stage, row.id, { approved, comment: value });
    ElMessage.success("审核完成");
    await refresh();
  } catch {}
}
onMounted(refresh);
</script>
<template>
  <div v-loading="loading">
    <div class="page-heading">
      <div>
        <h1>过程材料</h1>
        <p>保留每一次提交与反馈，让设计过程可追溯</p>
      </div>
      <el-button
        v-if="canSubmit"
        type="primary"
        @click="open()"
      >
        ＋ {{ stage === "task" ? "下达任务书" : "提交" + stageNames[stage] }}
      </el-button>
    </div>
    <section class="panel">
      <el-tabs v-model="stage">
        <el-tab-pane
          v-for="(label, key) in stageNames"
          :key="key"
          :label="label"
          :name="key"
        />
      </el-tabs>
      <div class="toolbar">
        <el-select
          v-if="auth.role !== 'STUDENT'"
          v-model="studentFilter"
          placeholder="全部学生"
          clearable
          style="width: 220px"
        >
          <el-option
            v-for="s in students"
            :key="s.student_id"
            :label="s.name"
            :value="s.student_id"
          />
        </el-select>
        <span class="subtle">{{ stageNames[stage] }} · 共 {{ filtered.length }} 个版本</span>
      </div>
      <div class="note-box">
        {{
          stage === "task"
            ? "任务书由指导教师下达，学生可查看下载。"
            : "按任务书 → 开题 → 中检 → 初稿 → 定稿依次推进。" +
              "材料被退回后可修改并重新提交，历史版本与意见始终保留。"
        }}
      </div>
      <el-table :data="filtered">
        <el-table-column
          prop="student_name"
          label="学生"
          width="85"
        />
        <el-table-column
          prop="title"
          label="材料标题"
          min-width="180"
          show-overflow-tooltip
        />
        <el-table-column
          label="版本"
          width="70"
        >
          <template #default="{ row }">v{{ row.version }}</template>
        </el-table-column>
        <el-table-column
          label="状态"
          width="100"
        >
          <template #default="{ row }"><StatusTag :status="row.status" /></template>
        </el-table-column>
        <el-table-column
          prop="review_comment"
          label="审核意见"
          min-width="160"
          show-overflow-tooltip
        />
        <el-table-column
          label="提交时间"
          width="170"
        >
          <template #default="{ row }">{{ dateText(row.submitted_at) }}</template>
        </el-table-column>
        <el-table-column
          label="操作"
          fixed="right"
          width="210"
        >
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              @click="downloadFile(row)"
            >
              下载
            </el-button>
            <el-popover
              trigger="click"
              width="380"
            >
              <template #reference>
                <el-button
                  link
                  type="primary"
                >
                  详情
                </el-button>
              </template>
              <h4>{{ row.title }} · v{{ row.version }}</h4>
              <p class="pre-wrap">{{ row.content || "未填写补充说明" }}</p>
              <p>审核意见：{{ row.review_comment || "等待审核" }}</p>
              <p class="subtle">修改 {{ row.revision_count }} 次 · {{ row.original_name }}</p>
            </el-popover>
            <template v-if="auth.role === 'TEACHER' && row.status === '待审核'">
              <el-button
                link
                type="primary"
                @click="review(row, true)"
              >
                通过
              </el-button>
              <el-button
                link
                type="danger"
                @click="review(row, false)"
              >
                退回
              </el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </section>
    <el-dialog
      v-model="dialog"
      :title="stage === 'task' ? '下达任务书' : '提交' + stageNames[stage]"
      width="560px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
      >
        <el-form-item
          v-if="auth.role === 'TEACHER'"
          label="指导学生"
          prop="studentId"
        >
          <el-select v-model="form.studentId">
            <el-option
              v-for="s in students"
              :key="s.student_id"
              :label="s.name + ' · ' + (s.topic_title || '未选题')"
              :value="s.student_id"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          label="材料标题"
          prop="title"
        >
          <el-input
            v-model="form.title"
            maxlength="200"
          />
        </el-form-item>
        <el-form-item label="内容说明">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="4"
            maxlength="10000"
          />
        </el-form-item>
        <el-form-item label="材料附件">
          <input
            aria-label="材料附件"
            type="file"
            accept=".pdf,.doc,.docx,.txt,.zip"
            @change="file = $event.target.files[0]"
          />
        </el-form-item>
        <div class="subtle">支持 PDF / Word / TXT / ZIP，单文件不超过20MB。</div>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button
          type="primary"
          :loading="saving"
          @click="save"
        >
          {{ stage === "task" ? "确认下达" : "提交审核" }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>
