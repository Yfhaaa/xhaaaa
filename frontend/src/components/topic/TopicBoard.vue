<script setup>
/**
 * 课题看板（课题库 / 课题申报 / 课题审核 共用）。
 *
 * - library：学生以卡片浏览已通过课题并申请；教师、管理员以表格浏览全部可见课题。
 * - mine：指导教师申报课题、查看审核结果、被退回后修改重提。
 * - audit：院系管理员审核待审课题，意见必填并留痕。
 */
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Plus, User, School, View } from "@element-plus/icons-vue";
import * as topicApi from "../../api/topic";
import { getReference } from "../../api/system";
import { useAuth } from "../../stores/auth";
import { dateText } from "../../utils/format";
import ReviewDialog from "../ReviewDialog.vue";
import StatusTag from "../StatusTag.vue";

const props = defineProps({
  mode: { type: String, default: "library" },
  title: { type: String, required: true },
  description: { type: String, default: "" },
});

const auth = useAuth();
const isStudent = computed(() => auth.role === "STUDENT");

const loading = ref(false);
const topics = ref([]);
const selections = ref([]);
const reference = ref({ majors: [] });

async function refresh() {
  loading.value = true;
  try {
    const [topicRows, selectionRows, referenceData] = await Promise.all([
      topicApi.listTopics(),
      topicApi.listSelections(),
      getReference(),
    ]);
    topics.value = topicRows;
    selections.value = selectionRows;
    reference.value = referenceData;
  } finally {
    loading.value = false;
  }
}
onMounted(refresh);

/* ---------------- 列表 ---------------- */
const rows = computed(() => {
  if (props.mode === "audit") return topics.value.filter((t) => t.status === "待审核");
  if (props.mode === "mine") return topics.value.filter((t) => t.teacher_id === auth.user?.id);
  return topics.value;
});

/* 学生：卡片检索与本人选题状态 */
const keyword = ref("");
const majorFilter = ref(null);
const onlyAvailable = ref(false);
const cardRows = computed(() =>
  rows.value
    .filter((t) => t.status === "已通过")
    .filter((t) => !majorFilter.value || t.major_id === majorFilter.value)
    .filter((t) => !onlyAvailable.value || t.selected_count < t.capacity)
    .filter((t) => !keyword.value || `${t.title}${t.teacher_name}${t.description}`.includes(keyword.value)),
);
const mySelection = computed(() => {
  const mine = selections.value.filter((s) => s.student_id === auth.user?.id);
  return mine.find((s) => s.status === "已通过") || mine.find((s) => s.status === "待审核") || null;
});
const appliedTopicIds = computed(
  () => new Set(selections.value.filter((s) => s.status !== "已退回").map((s) => s.topic_id)),
);
function applyDisabledReason(topic) {
  if (appliedTopicIds.value.has(topic.id)) return "已申请";
  if (mySelection.value?.status === "已通过") return "你已确定课题";
  if (mySelection.value?.status === "待审核") return "已有待确认的申请";
  if (topic.selected_count >= topic.capacity) return "名额已满";
  return "";
}
async function apply(topic) {
  await ElMessageBox.confirm(
    `确认申请「${topic.title}」？指导教师确认后即确定师生关系，同一时间只能有一个待确认申请。`,
    "提交选题申请",
    { type: "info", confirmButtonText: "确认申请", cancelButtonText: "再看看" },
  );
  await topicApi.applyTopic(topic.id);
  ElMessage.success("申请已提交，等待指导教师确认");
  await refresh();
}

/* ---------------- 申报 / 修改 ---------------- */
const dialog = ref(false);
const saving = ref(false);
const formRef = ref();
const form = reactive({ id: null, title: "", description: "", majorId: null, capacity: 1 });
const rules = {
  title: [
    { required: true, message: "请输入课题名称", trigger: "blur" },
    { min: 6, max: 60, message: "课题名称 6–60 个字", trigger: "blur" },
  ],
  majorId: [{ required: true, message: "请选择面向专业", trigger: "change" }],
  description: [
    { required: true, message: "请填写研究内容与要求", trigger: "blur" },
    { min: 30, message: "研究内容不少于 30 字，便于学生了解课题", trigger: "blur" },
  ],
};
function openForm(topic) {
  Object.assign(form, {
    id: topic?.id ?? null,
    title: topic?.title ?? "",
    description: topic?.description ?? "",
    majorId: topic?.major_id ?? reference.value.majors[0]?.id ?? null,
    capacity: topic?.capacity ?? 1,
  });
  dialog.value = true;
}
async function save() {
  if (!(await formRef.value.validate().catch(() => false))) return;
  saving.value = true;
  try {
    await topicApi.saveTopic(form.id, form);
    dialog.value = false;
    ElMessage.success(form.id ? "课题已修改并重新提交审核" : "课题已提交，等待院系审核");
    await refresh();
  } finally {
    saving.value = false;
  }
}

/* ---------------- 审核 ---------------- */
const reviewing = ref(null);
const reviewSaving = ref(false);
const reviewSummary = computed(() => {
  const t = reviewing.value;
  if (!t) return [];
  return [
    { label: "课题名称", value: t.title },
    { label: "指导教师", value: t.teacher_name },
    { label: "面向专业", value: t.major_name },
    { label: "名额", value: `${t.capacity} 人` },
    { label: "提交时间", value: dateText(t.submitted_at) },
    { label: "修改次数", value: `${t.revision_count} 次` },
  ];
});
async function doReview({ approved, comment }) {
  reviewSaving.value = true;
  try {
    await topicApi.reviewTopic(reviewing.value.id, { approved, comment });
    ElMessage.success(approved ? "课题已审核通过，学生可见" : "课题已退回，教师将收到修改通知");
    reviewing.value = null;
    await refresh();
  } finally {
    reviewSaving.value = false;
  }
}

/* ---------------- 详情 ---------------- */
const detail = ref(null);
const percent = (t) => Math.min(100, Math.round((t.selected_count / t.capacity) * 100));
</script>

<template>
  <div>
    <PageHeader :title="title" :description="description">
      <el-button
        v-if="mode === 'mine'"
        v-permission="'topic:write'"
        type="primary"
        :icon="Plus"
        @click="openForm()"
      >
        申报新课题
      </el-button>
    </PageHeader>

    <!-- 学生：选题状态横幅 + 课题卡片 -->
    <template v-if="isStudent && mode === 'library'">
      <el-alert
        v-if="mySelection"
        :type="mySelection.status === '已通过' ? 'success' : 'warning'"
        :closable="false"
        show-icon
        class="gd-topic__banner"
      >
        <template #title>
          <template v-if="mySelection.status === '已通过'">
            已确定课题「{{ mySelection.title }}」，指导教师：{{ mySelection.teacher_name }}
          </template>
          <template v-else>
            已申请「{{ mySelection.title }}」，等待 {{ mySelection.teacher_name }} 老师确认
          </template>
        </template>
      </el-alert>

      <section class="panel gd-topic__filters">
        <el-input
          v-model="keyword"
          placeholder="搜索课题名称、指导教师或研究内容"
          clearable
          style="width: 320px"
        />
        <el-select v-model="majorFilter" placeholder="全部专业" clearable style="width: 200px">
          <el-option v-for="m in reference.majors" :key="m.id" :label="m.name" :value="m.id" />
        </el-select>
        <el-checkbox v-model="onlyAvailable">只看有余量</el-checkbox>
        <span class="gd-topic__count">共 {{ cardRows.length }} 个课题</span>
      </section>

      <el-skeleton :loading="loading && !topics.length" animated :rows="6">
        <template #default>
          <el-empty v-if="!cardRows.length" description="暂无符合条件的课题" />
          <div class="gd-topic__grid">
            <article v-for="t in cardRows" :key="t.id" class="gd-topic__card">
              <div class="gd-topic__card-head">
                <el-tag size="small" effect="plain">{{ t.major_name }}</el-tag>
                <span class="gd-topic__quota" :class="{ full: t.selected_count >= t.capacity }">
                  {{ t.selected_count >= t.capacity ? "已满" : `余 ${t.capacity - t.selected_count}` }}
                </span>
              </div>
              <h3 @click="detail = t">{{ t.title }}</h3>
              <p class="gd-topic__desc">{{ t.description }}</p>
              <div class="gd-topic__meta">
                <span
                  ><el-icon><User /></el-icon>{{ t.teacher_name }}</span
                >
                <span
                  ><el-icon><School /></el-icon>名额 {{ t.selected_count }}/{{ t.capacity }}</span
                >
              </div>
              <el-progress
                :percentage="percent(t)"
                :show-text="false"
                :stroke-width="5"
                :color="t.selected_count >= t.capacity ? '#d9634f' : '#175c4b'"
              />
              <div class="gd-topic__actions">
                <el-button link type="primary" @click="detail = t">查看详情</el-button>
                <el-tooltip :disabled="!applyDisabledReason(t)" :content="applyDisabledReason(t)">
                  <span>
                    <el-button
                      type="primary"
                      size="small"
                      :disabled="!!applyDisabledReason(t)"
                      @click="apply(t).catch(() => {})"
                    >
                      {{ applyDisabledReason(t) === "已申请" ? "已申请" : "申请选题" }}
                    </el-button>
                  </span>
                </el-tooltip>
              </div>
            </article>
          </div>
        </template>
      </el-skeleton>
    </template>

    <!-- 教师 / 管理员：表格 -->
    <section v-else class="panel">
      <el-alert
        v-if="mode === 'audit'"
        type="info"
        :closable="false"
        show-icon
        title="审核要点：选题是否符合专业培养目标、工作量是否适中、研究内容是否明确可行。"
        class="gd-topic__tip"
      />
      <DataTable
        :data="rows"
        :loading="loading"
        :status-field="mode === 'audit' ? '' : 'status'"
        placeholder="搜索课题名称或指导教师"
      >
        <el-table-column prop="title" label="课题名称" min-width="240" show-overflow-tooltip>
          <template #default="{ row }">
            <el-button link type="primary" class="gd-topic__title-link" @click="detail = row">
              {{ row.title }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column v-if="mode !== 'mine'" prop="teacher_name" label="指导教师" width="100" />
        <el-table-column prop="major_name" label="面向专业" min-width="140" />
        <el-table-column label="名额使用" width="150">
          <template #default="{ row }">
            <div class="gd-topic__mini-quota">
              <el-progress :percentage="percent(row)" :show-text="false" :stroke-width="6" />
              <span>{{ row.selected_count }}/{{ row.capacity }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }"><StatusTag :status="row.status" /></template>
        </el-table-column>
        <el-table-column label="提交时间" width="170">
          <template #default="{ row }">{{ dateText(row.submitted_at) }}</template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="150">
          <template #default="{ row }">
            <el-button link type="primary" :icon="View" @click="detail = row">详情</el-button>
            <el-button
              v-if="mode === 'audit' && row.status === '待审核'"
              v-permission="'topic:write'"
              link
              type="warning"
              @click="reviewing = row"
            >
              审核
            </el-button>
            <el-button
              v-if="mode === 'mine' && row.status === '已退回'"
              v-permission="'topic:write'"
              link
              type="warning"
              @click="openForm(row)"
            >
              修改重提
            </el-button>
          </template>
        </el-table-column>
      </DataTable>
    </section>

    <!-- 申报对话框 -->
    <el-dialog
      v-model="dialog"
      :title="form.id ? '修改课题并重新提交' : '申报新课题'"
      width="640px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="课题名称" prop="title">
          <el-input
            v-model="form.title"
            maxlength="60"
            show-word-limit
            placeholder="如：基于 Spring Boot 的 XX 系统设计与实现"
          />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="14">
            <el-form-item label="面向专业" prop="majorId">
              <el-select v-model="form.majorId" style="width: 100%">
                <el-option v-for="m in reference.majors" :key="m.id" :label="m.name" :value="m.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="10">
            <el-form-item label="可选名额">
              <el-input-number v-model="form.capacity" :min="1" :max="20" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="研究内容与要求" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="6"
            maxlength="2000"
            show-word-limit
            placeholder="说明研究背景、主要内容、技术要求与预期成果"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">提交审核</el-button>
      </template>
    </el-dialog>

    <ReviewDialog
      :model-value="!!reviewing"
      title="课题审核"
      :summary="reviewSummary"
      :loading="reviewSaving"
      @update:model-value="reviewing = null"
      @submit="doReview"
    />

    <!-- 详情抽屉 -->
    <el-drawer :model-value="!!detail" title="课题详情" size="480px" @close="detail = null">
      <template v-if="detail">
        <h2 class="gd-topic__detail-title">{{ detail.title }}</h2>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="指导教师">{{ detail.teacher_name }}</el-descriptions-item>
          <el-descriptions-item label="面向专业">{{ detail.major_name }}</el-descriptions-item>
          <el-descriptions-item label="名额"
            >{{ detail.selected_count }}/{{ detail.capacity }}</el-descriptions-item
          >
          <el-descriptions-item label="状态"><StatusTag :status="detail.status" /></el-descriptions-item>
          <el-descriptions-item label="提交时间" :span="2">{{
            dateText(detail.submitted_at)
          }}</el-descriptions-item>
        </el-descriptions>
        <h4>研究内容与要求</h4>
        <p class="pre-wrap">{{ detail.description }}</p>
        <template v-if="detail.review_comment">
          <h4>审核意见</h4>
          <p class="pre-wrap">{{ detail.review_comment }}</p>
        </template>
      </template>
    </el-drawer>
  </div>
</template>

<style scoped>
.gd-topic__banner,
.gd-topic__tip {
  margin-bottom: 16px;
}
.gd-topic__filters {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}
.gd-topic__count {
  margin-left: auto;
  color: #8a9a93;
  font-size: 12px;
}
.gd-topic__grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}
.gd-topic__card {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 18px 20px;
  border: 1px solid #e3eae6;
  border-radius: 12px;
  background: #fff;
  transition:
    transform 0.18s,
    box-shadow 0.18s,
    border-color 0.18s;
}
.gd-topic__card:hover {
  border-color: #b9cfc6;
  box-shadow: 0 10px 24px rgb(18 62 52 / 8%);
  transform: translateY(-2px);
}
.gd-topic__card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.gd-topic__quota {
  color: #175c4b;
  font-size: 12px;
  font-weight: 600;
}
.gd-topic__quota.full {
  color: #d9634f;
}
.gd-topic__card h3 {
  margin: 0;
  color: #1f3a32;
  font-size: 16px;
  line-height: 1.45;
  cursor: pointer;
}
.gd-topic__card h3:hover {
  color: #175c4b;
}
.gd-topic__desc {
  display: -webkit-box;
  overflow: hidden;
  min-height: 60px;
  margin: 0;
  color: #6c7f77;
  font-size: 13px;
  line-height: 1.6;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
}
.gd-topic__meta {
  display: flex;
  gap: 16px;
  color: #5b7067;
  font-size: 12px;
}
.gd-topic__meta span {
  display: inline-flex;
  gap: 4px;
  align-items: center;
}
.gd-topic__actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 6px;
  border-top: 1px dashed #e6ece9;
}
.gd-topic__title-link {
  max-width: 100%;
  overflow: hidden;
  font-weight: 600;
  text-overflow: ellipsis;
}
.gd-topic__mini-quota {
  display: flex;
  gap: 8px;
  align-items: center;
}
.gd-topic__mini-quota .el-progress {
  flex: 1;
}
.gd-topic__mini-quota span {
  color: #5b7067;
  font-size: 12px;
}
.gd-topic__detail-title {
  margin: 0 0 16px;
  color: #1f3a32;
  font-size: 18px;
}
</style>
