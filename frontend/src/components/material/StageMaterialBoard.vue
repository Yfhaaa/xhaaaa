<script setup>
/**
 * 过程材料看板（任务书 / 开题报告 / 中期检查 / 论文初稿与定稿 / 待我审核 共用）。
 *
 * - 学生：查看当前阶段最新状态、截止倒计时、历次版本时间线，按状态提交或重新提交。
 * - 教师：下达任务书、审核本人指导学生的材料（意见必填、留痕）。
 * - 管理员：按学生与阶段查看全部材料。
 *
 * 数据范围由后端按角色控制，前端只做展示层过滤。
 */
import { computed, onMounted, reactive, ref, watch } from "vue";
import { ElMessage } from "element-plus";
import { Clock, Download, Upload, View } from "@element-plus/icons-vue";
import * as materialApi from "../../api/material";
import { listStudents, listWorkflow } from "../../api/system";
import { useAuth } from "../../stores/auth";
import { dateText, stageNames } from "../../utils/format";
import FileUploader from "../FileUploader.vue";
import ReviewDialog from "../ReviewDialog.vue";
import StatusTag from "../StatusTag.vue";

const props = defineProps({
  /** 本页涉及的阶段编码，如 ['proposal'] 或 ['draft', 'final'] */
  stages: { type: Array, required: true },
  /** stage：按阶段管理；pending：汇总所有待审核材料 */
  mode: { type: String, default: "stage" },
  title: { type: String, required: true },
  description: { type: String, default: "" },
});

const auth = useAuth();
const isStudent = computed(() => auth.role === "STUDENT");
const isTeacher = computed(() => auth.role === "TEACHER");

const loading = ref(false);
const rows = ref([]);
const students = ref([]);
const workflow = ref([]);
const stage = ref(props.stages[0]);
const studentFilter = ref(null);
const latestOnly = ref(true);

/* ---------------- 数据加载 ---------------- */
async function refresh() {
  loading.value = true;
  try {
    const [materials, studentRows, steps] = await Promise.all([
      materialApi.listMaterials(),
      listStudents(),
      listWorkflow(),
    ]);
    rows.value = materials;
    students.value = studentRows;
    workflow.value = steps;
  } finally {
    loading.value = false;
  }
}
onMounted(refresh);

/* ---------------- 派生数据 ---------------- */
const stageRows = computed(() =>
  props.mode === "pending"
    ? rows.value.filter((r) => r.status === "待审核")
    : rows.value.filter((r) => r.stage === stage.value),
);

/** 每个学生在当前阶段的最新版本 */
const latestByStudent = computed(() => {
  const map = new Map();
  for (const r of stageRows.value) {
    const key = `${r.student_id}-${r.stage}`;
    if (!map.has(key) || map.get(key).version < r.version) map.set(key, r);
  }
  return [...map.values()];
});

const tableRows = computed(() => {
  const source = props.mode === "pending" || !latestOnly.value ? stageRows.value : latestByStudent.value;
  return source
    .filter((r) => !studentFilter.value || r.student_id === studentFilter.value)
    .sort((a, b) => new Date(b.submitted_at) - new Date(a.submitted_at));
});

const stats = computed(() => {
  const list = latestByStudent.value;
  const count = (s) => list.filter((r) => r.status === s).length;
  return [
    { label: "已提交学生", value: list.length, tone: "default" },
    { label: "待审核", value: count("待审核"), tone: "warning" },
    { label: "已通过", value: count("已通过"), tone: "success" },
    { label: "已退回", value: count("已退回"), tone: "danger" },
  ];
});

/** 当前阶段截止时间与剩余天数 */
const deadline = computed(() => {
  const step = workflow.value.find((w) => w.code === stage.value);
  if (!step?.deadline) return null;
  const days = Math.ceil((new Date(step.deadline) - Date.now()) / 86400000);
  let tone = "success";
  if (days < 0) tone = "danger";
  else if (days <= 7) tone = "warning";
  return { text: dateText(step.deadline).slice(0, 10), days, tone };
});

/* 学生视角：本人当前阶段的版本历史 */
const myVersions = computed(() =>
  stageRows.value.filter((r) => r.student_id === auth.user?.id).sort((a, b) => b.version - a.version),
);
const myLatest = computed(() => myVersions.value[0] || null);

/** 提交权限：教师只下达任务书；学生提交其余阶段，且仅在未提交或被退回时可提交 */
const canSubmit = computed(() => {
  if (props.mode === "pending" || !auth.can("material:write")) return false;
  if (stage.value === "task") return isTeacher.value;
  if (!isStudent.value) return false;
  return !myLatest.value || myLatest.value.status === "已退回";
});
const submitBlockedReason = computed(() => {
  if (!isStudent.value || stage.value === "task" || canSubmit.value) return "";
  if (myLatest.value?.status === "待审核") return "当前版本正在审核中";
  if (myLatest.value?.status === "已通过") return "当前阶段已审核通过";
  return "";
});

/* ---------------- 提交 ---------------- */
const dialog = ref(false);
const saving = ref(false);
const formRef = ref();
const form = reactive({ studentId: null, title: "", content: "", fileId: null });
const rules = {
  studentId: [{ required: true, message: "请选择指导学生", trigger: "change" }],
  title: [{ required: true, message: "请输入材料标题", trigger: "blur" }],
  fileId: [{ required: true, message: "请上传材料附件", trigger: "change" }],
};

function openSubmit() {
  Object.assign(form, {
    studentId: isTeacher.value ? (students.value[0]?.student_id ?? null) : auth.user?.id,
    title: `${stageNames[stage.value]}${myLatest.value ? `（第 ${myLatest.value.version + 1} 版）` : ""}`,
    content: "",
    fileId: null,
  });
  dialog.value = true;
}

async function submit() {
  if (!(await formRef.value.validate().catch(() => false))) return;
  saving.value = true;
  try {
    await materialApi.submitMaterial(stage.value, { ...form });
    dialog.value = false;
    ElMessage.success(stage.value === "task" ? "任务书已下达" : "材料已提交，等待指导教师审核");
    await refresh();
  } finally {
    saving.value = false;
  }
}

/* ---------------- 审核 ---------------- */
const reviewing = ref(null);
const reviewSaving = ref(false);
const reviewSummary = computed(() => {
  const r = reviewing.value;
  if (!r) return [];
  return [
    { label: "学生", value: r.student_name },
    { label: "阶段", value: stageNames[r.stage] },
    { label: "材料", value: r.title },
    { label: "版本", value: `v${r.version}（已修改 ${r.revision_count} 次）` },
    { label: "附件", value: r.original_name },
    { label: "提交时间", value: dateText(r.submitted_at) },
  ];
});
const canReview = (row) => isTeacher.value && row.status === "待审核" && auth.can("material:write");

async function doReview({ approved, comment }) {
  const row = reviewing.value;
  reviewSaving.value = true;
  try {
    await materialApi.reviewMaterial(row.stage, row.id, { approved, comment });
    ElMessage.success(approved ? "已审核通过" : "已退回，学生将收到修改通知");
    reviewing.value = null;
    await refresh();
  } finally {
    reviewSaving.value = false;
  }
}

/* ---------------- 详情 ---------------- */
const detail = ref(null);

const dossierLink = (row) => (isStudent.value ? "/my-project" : `/students/${row.student_id}`);

watch(stage, () => {
  studentFilter.value = null;
});
</script>

<template>
  <div class="gd-board">
    <PageHeader :title="title" :description="description">
      <el-tag v-if="deadline && mode === 'stage'" :type="deadline.tone" effect="light" round size="large">
        <el-icon><Clock /></el-icon>
        截止 {{ deadline.text }}
        <template v-if="deadline.days >= 0"> · 剩余 {{ deadline.days }} 天</template>
        <template v-else> · 已逾期 {{ -deadline.days }} 天</template>
      </el-tag>
      <el-tooltip :disabled="!submitBlockedReason" :content="submitBlockedReason" placement="bottom">
        <span>
          <el-button
            v-if="(isStudent && stage !== 'task') || (isTeacher && stage === 'task')"
            type="primary"
            :icon="Upload"
            :disabled="!canSubmit"
            @click="openSubmit"
          >
            {{ stage === "task" ? "下达任务书" : myLatest ? "提交新版本" : `提交${stageNames[stage]}` }}
          </el-button>
        </span>
      </el-tooltip>
    </PageHeader>

    <el-segmented
      v-if="stages.length > 1 && mode === 'stage'"
      v-model="stage"
      :options="stages.map((s) => ({ label: stageNames[s], value: s }))"
      class="gd-board__stages"
    />

    <!-- 学生视角 -->
    <template v-if="isStudent && mode === 'stage'">
      <el-skeleton :loading="loading && !rows.length" animated :rows="6">
        <template #default>
          <section v-if="myLatest" class="panel gd-board__current">
            <div class="gd-board__current-head">
              <div>
                <div class="gd-board__eyebrow">当前版本 · v{{ myLatest.version }}</div>
                <h2>{{ myLatest.title }}</h2>
              </div>
              <StatusTag :status="myLatest.status" size="large" />
            </div>
            <el-descriptions :column="3" size="small">
              <el-descriptions-item label="提交时间">{{
                dateText(myLatest.submitted_at)
              }}</el-descriptions-item>
              <el-descriptions-item label="审核时间">{{
                dateText(myLatest.reviewed_at)
              }}</el-descriptions-item>
              <el-descriptions-item label="修改次数">{{ myLatest.revision_count }} 次</el-descriptions-item>
            </el-descriptions>
            <el-alert
              v-if="myLatest.review_comment"
              :type="myLatest.status === '已退回' ? 'error' : 'success'"
              :title="`${myLatest.teacher_name || '指导教师'}的意见`"
              :description="myLatest.review_comment"
              :closable="false"
              show-icon
            />
            <el-alert
              v-else-if="myLatest.status === '待审核'"
              type="info"
              title="材料已提交，等待指导教师审核"
              :closable="false"
              show-icon
            />
            <div class="gd-board__current-actions">
              <el-button :icon="Download" @click="materialApi.downloadFile(myLatest)">下载附件</el-button>
            </div>
          </section>
          <section v-else class="panel">
            <el-empty
              :description="stage === 'task' ? '指导教师尚未下达任务书' : `尚未提交${stageNames[stage]}`"
            >
              <el-button v-if="canSubmit" type="primary" @click="openSubmit">立即提交</el-button>
            </el-empty>
          </section>

          <section v-if="myVersions.length > 1" class="panel">
            <h3 class="panel-title">版本历史</h3>
            <el-timeline>
              <el-timeline-item
                v-for="v in myVersions"
                :key="v.id"
                :timestamp="dateText(v.submitted_at)"
                :type="{ 已通过: 'success', 已退回: 'danger', 待审核: 'warning' }[v.status] || 'info'"
                placement="top"
              >
                <div class="gd-board__version">
                  <strong>v{{ v.version }} · {{ v.title }}</strong>
                  <StatusTag :status="v.status" />
                  <el-button link type="primary" @click="materialApi.downloadFile(v)">下载</el-button>
                </div>
                <p v-if="v.review_comment" class="subtle">意见：{{ v.review_comment }}</p>
              </el-timeline-item>
            </el-timeline>
          </section>
        </template>
      </el-skeleton>
    </template>

    <!-- 教师 / 管理员视角 -->
    <template v-else>
      <div v-if="mode === 'stage'" class="gd-board__stats">
        <div v-for="s in stats" :key="s.label" class="gd-board__stat" :class="`is-${s.tone}`">
          <span>{{ s.label }}</span>
          <strong>{{ s.value }}</strong>
        </div>
      </div>

      <section class="panel">
        <DataTable
          :data="tableRows"
          :loading="loading"
          status-field="status"
          :placeholder="mode === 'pending' ? '搜索学生或材料' : '搜索学生、标题或意见'"
        >
          <template #filters>
            <el-select
              v-model="studentFilter"
              placeholder="全部学生"
              clearable
              filterable
              style="width: 180px"
            >
              <el-option
                v-for="s in students"
                :key="s.student_id"
                :label="`${s.name}（${s.class_name || s.username}）`"
                :value="s.student_id"
              />
            </el-select>
            <el-switch
              v-if="mode === 'stage'"
              v-model="latestOnly"
              active-text="仅看最新版本"
              inline-prompt
              style="--el-switch-on-color: #175c4b"
            />
          </template>

          <el-table-column label="学生" min-width="110">
            <template #default="{ row }">
              <router-link :to="dossierLink(row)" class="gd-board__student">
                <el-avatar :size="26">{{ row.student_name?.slice(0, 1) }}</el-avatar>
                {{ row.student_name }}
              </router-link>
            </template>
          </el-table-column>
          <el-table-column v-if="mode === 'pending'" label="阶段" width="100">
            <template #default="{ row }">
              <el-tag effect="plain" size="small">{{ stageNames[row.stage] }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="title" label="材料标题" min-width="200" show-overflow-tooltip />
          <el-table-column label="版本" width="80" align="center">
            <template #default="{ row }"
              ><span class="gd-board__ver">v{{ row.version }}</span></template
            >
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }"><StatusTag :status="row.status" /></template>
          </el-table-column>
          <el-table-column prop="review_comment" label="审核意见" min-width="180" show-overflow-tooltip>
            <template #default="{ row }">
              <span :class="{ subtle: !row.review_comment }">{{ row.review_comment || "—" }}</span>
            </template>
          </el-table-column>
          <el-table-column label="提交时间" width="170">
            <template #default="{ row }">{{ dateText(row.submitted_at) }}</template>
          </el-table-column>
          <el-table-column label="操作" fixed="right" width="190">
            <template #default="{ row }">
              <el-button link type="primary" :icon="View" @click="detail = row">详情</el-button>
              <el-button link type="primary" :icon="Download" @click="materialApi.downloadFile(row)">
                下载
              </el-button>
              <el-button v-if="canReview(row)" link type="warning" @click="reviewing = row">审核</el-button>
            </template>
          </el-table-column>
        </DataTable>
      </section>
    </template>

    <!-- 提交对话框 -->
    <el-dialog
      v-model="dialog"
      :title="stage === 'task' ? '下达任务书' : `提交${stageNames[stage]}`"
      width="580px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item v-if="isTeacher" label="指导学生" prop="studentId">
          <el-select v-model="form.studentId" filterable style="width: 100%">
            <el-option
              v-for="s in students"
              :key="s.student_id"
              :label="`${s.name} · ${s.topic_title || '未确定课题'}`"
              :value="s.student_id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="材料标题" prop="title">
          <el-input v-model="form.title" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="补充说明">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="4"
            maxlength="2000"
            show-word-limit
            placeholder="可说明本版主要修改内容，便于指导教师审核"
          />
        </el-form-item>
        <el-form-item label="材料附件" prop="fileId">
          <FileUploader v-model="form.fileId" @update:model-value="formRef?.validateField('fileId')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">
          {{ stage === "task" ? "确认下达" : "提交审核" }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 审核对话框 -->
    <ReviewDialog
      :model-value="!!reviewing"
      :title="`审核 · ${reviewing ? stageNames[reviewing.stage] : ''}`"
      :summary="reviewSummary"
      :loading="reviewSaving"
      @update:model-value="reviewing = null"
      @submit="doReview"
    />

    <!-- 详情抽屉 -->
    <el-drawer :model-value="!!detail" :title="detail?.title" size="440px" @close="detail = null">
      <template v-if="detail">
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="学生">{{ detail.student_name }}</el-descriptions-item>
          <el-descriptions-item label="指导教师">{{ detail.teacher_name }}</el-descriptions-item>
          <el-descriptions-item label="阶段">{{ stageNames[detail.stage] }}</el-descriptions-item>
          <el-descriptions-item label="版本">v{{ detail.version }}</el-descriptions-item>
          <el-descriptions-item label="状态"><StatusTag :status="detail.status" /></el-descriptions-item>
          <el-descriptions-item label="修改次数">{{ detail.revision_count }} 次</el-descriptions-item>
          <el-descriptions-item label="附件">{{ detail.original_name }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ dateText(detail.submitted_at) }}</el-descriptions-item>
          <el-descriptions-item label="审核时间">{{ dateText(detail.reviewed_at) }}</el-descriptions-item>
        </el-descriptions>
        <h4>补充说明</h4>
        <p class="pre-wrap">{{ detail.content || "未填写" }}</p>
        <h4>审核意见</h4>
        <p class="pre-wrap">{{ detail.review_comment || "等待审核" }}</p>
        <el-button type="primary" plain :icon="Download" @click="materialApi.downloadFile(detail)">
          下载附件
        </el-button>
      </template>
    </el-drawer>
  </div>
</template>

<style scoped>
.gd-board__stages {
  margin-bottom: 16px;
  --el-segmented-item-selected-bg-color: #175c4b;
}
.gd-board__stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  margin-bottom: 16px;
}
.gd-board__stat {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 16px 18px;
  border: 1px solid #e6ece9;
  border-left: 4px solid #9fb8ad;
  border-radius: 10px;
  background: #fff;
}
.gd-board__stat span {
  color: #7d9088;
  font-size: 12px;
}
.gd-board__stat strong {
  color: #1f3a32;
  font:
    700 26px/1 Georgia,
    serif;
}
.gd-board__stat.is-warning {
  border-left-color: #d9a441;
}
.gd-board__stat.is-success {
  border-left-color: #34816b;
}
.gd-board__stat.is-danger {
  border-left-color: #d9634f;
}
.gd-board__current {
  display: flex;
  flex-direction: column;
  gap: 14px;
  border-top: 4px solid #175c4b;
}
.gd-board__current-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}
.gd-board__current-head h2 {
  margin: 4px 0 0;
  color: #1f3a32;
  font-size: 20px;
}
.gd-board__eyebrow {
  color: #a0874a;
  font-size: 12px;
  letter-spacing: 1px;
}
.gd-board__current-actions {
  display: flex;
  gap: 10px;
}
.gd-board__version {
  display: flex;
  gap: 10px;
  align-items: center;
}
.gd-board__student {
  display: inline-flex;
  gap: 8px;
  align-items: center;
  color: #175c4b;
  font-weight: 600;
}
.gd-board__student .el-avatar {
  background: #e3eee9;
  color: #175c4b;
  font-size: 12px;
}
.gd-board__ver {
  padding: 1px 7px;
  border-radius: 4px;
  background: #eef3f1;
  color: #4f665d;
  font-family: Consolas, monospace;
  font-size: 12px;
}
@media (max-width: 1100px) {
  .gd-board__stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
