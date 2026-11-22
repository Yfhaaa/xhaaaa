<script setup>
/**
 * 成绩管理：总成绩 = 指导分 × 指导权重 + 评阅分 × 评阅权重 + 答辩分 × 答辩权重。
 * - 指导教师 / 管理员：录入指导分与答辩分（评阅分由评阅记录自动带入），预览总成绩与等级。
 * - 管理员：发布成绩（发布后锁定），导出成绩表。
 * - 学生：查看本人已发布成绩及构成。
 */
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Download, EditPen, Promotion } from "@element-plus/icons-vue";
import * as defenseApi from "../../api/defense";
import { download, get } from "../../api/advanced";
import { listStudents } from "../../api/system";
import { useAuth } from "../../stores/auth";
import { dateText } from "../../utils/format";

const auth = useAuth();
const isStudent = computed(() => auth.role === "STUDENT");
const isAdmin = computed(() => auth.role === "ADMIN");

const loading = ref(false);
const scores = ref([]);
const defenses = ref([]);
const students = ref([]);
const reviews = ref([]);

async function refresh() {
  loading.value = true;
  try {
    const tasks = [defenseApi.listScores(), defenseApi.listDefenses()];
    if (!isStudent.value)
      tasks.push(
        listStudents(),
        get("/thesis-reviews").catch(() => []),
      );
    const [scoreRows, defenseRows, studentRows = [], reviewRows = []] = await Promise.all(tasks);
    scores.value = scoreRows;
    defenses.value = defenseRows;
    students.value = studentRows;
    reviews.value = reviewRows;
  } finally {
    loading.value = false;
  }
}
onMounted(refresh);

/* ---------------- 等级与统计 ---------------- */
function gradeOf(total) {
  const t = Number(total);
  if (t >= 90) return { label: "优秀", type: "success" };
  if (t >= 80) return { label: "良好", type: "primary" };
  if (t >= 70) return { label: "中等", type: "warning" };
  if (t >= 60) return { label: "及格", type: "info" };
  return { label: "不及格", type: "danger" };
}

const stats = computed(() => {
  const list = scores.value;
  const totals = list.map((s) => Number(s.total_score));
  const avg = totals.length ? totals.reduce((a, b) => a + b, 0) / totals.length : 0;
  const excellent = totals.filter((t) => t >= 90).length;
  return [
    { label: "已录入", value: list.length, unit: "人" },
    { label: "已发布", value: list.filter((s) => s.published).length, unit: "人" },
    { label: "平均成绩", value: avg.toFixed(1), unit: "分" },
    {
      label: "优秀率",
      value: totals.length ? ((excellent / totals.length) * 100).toFixed(0) : 0,
      unit: "%",
    },
  ];
});

/* 学生：本人成绩 */
const myScore = computed(() => scores.value.find((s) => s.student_id === auth.user?.id) || null);
const scoreParts = computed(() => {
  const s = myScore.value;
  if (!s) return [];
  return [
    { label: "指导教师评分", score: s.guidance_score, weight: s.guidance_weight },
    { label: "评阅教师评分", score: s.review_score, weight: s.review_weight },
    { label: "答辩小组评分", score: s.defense_score, weight: s.defense_weight },
  ];
});

/* ---------------- 录入成绩 ---------------- */
const dialog = ref(false);
const saving = ref(false);
const formRef = ref();
const form = reactive({
  studentId: null,
  guidanceScore: 85,
  defenseScore: 85,
  guidanceWeight: 0.3,
  reviewWeight: 0.3,
  defenseWeight: 0.4,
});

/** 只有已安排答辩、成绩未发布的学生可录入 */
const scorableStudents = computed(() => {
  const arranged = new Set(defenses.value.map((d) => d.student_id));
  const published = new Set(scores.value.filter((s) => s.published).map((s) => s.student_id));
  return students.value.filter((s) => arranged.has(s.student_id) && !published.has(s.student_id));
});
const currentReview = computed(() =>
  reviews.value.find((r) => r.student_id === form.studentId && r.status === "已评阅"),
);
const weightSum = computed(() => +(form.guidanceWeight + form.reviewWeight + form.defenseWeight).toFixed(2));
const previewTotal = computed(() => {
  if (!currentReview.value) return null;
  const total =
    form.guidanceScore * form.guidanceWeight +
    Number(currentReview.value.score) * form.reviewWeight +
    form.defenseScore * form.defenseWeight;
  return total.toFixed(2);
});
const rules = {
  studentId: [{ required: true, message: "请选择学生", trigger: "change" }],
};

function openScore(row) {
  const existing = row || null;
  Object.assign(form, {
    studentId: existing?.student_id ?? scorableStudents.value[0]?.student_id ?? null,
    guidanceScore: Number(existing?.guidance_score ?? 85),
    defenseScore: Number(existing?.defense_score ?? 85),
    guidanceWeight: Number(existing?.guidance_weight ?? 0.3),
    reviewWeight: Number(existing?.review_weight ?? 0.3),
    defenseWeight: Number(existing?.defense_weight ?? 0.4),
  });
  dialog.value = true;
}

async function save() {
  if (!(await formRef.value.validate().catch(() => false))) return;
  if (weightSum.value !== 1) {
    ElMessage.warning(`三项权重之和须等于 1，当前为 ${weightSum.value}`);
    return;
  }
  saving.value = true;
  try {
    const result = await defenseApi.saveDefense("score", { ...form });
    ElMessage.success(`成绩已保存，总成绩 ${result.totalScore} 分`);
    dialog.value = false;
    await refresh();
  } finally {
    saving.value = false;
  }
}

/* ---------------- 发布 ---------------- */
const unpublished = computed(() => scores.value.filter((s) => !s.published));

async function publish(rows) {
  const names = rows.map((r) => r.student_name).join("、");
  await ElMessageBox.confirm(
    `将发布 ${rows.length} 名学生的成绩（${names}）。发布后学生可查询，成绩将锁定不可修改。`,
    "发布成绩",
    { type: "warning", confirmButtonText: "确认发布", cancelButtonText: "取消" },
  );
  for (const row of rows) await defenseApi.publishScore(row.id);
  ElMessage.success(`已发布 ${rows.length} 名学生的成绩`);
  await refresh();
}
</script>

<template>
  <div>
    <PageHeader title="成绩管理" description="指导、评阅、答辩三方评分按权重汇总，发布后锁定">
      <template v-if="!isStudent">
        <el-button :icon="Download" @click="download('/exports/scores', '成绩表.xlsx')">导出成绩表</el-button>
        <el-button
          v-if="isAdmin && unpublished.length"
          v-permission="'score:write'"
          :icon="Promotion"
          @click="publish(unpublished).catch(() => {})"
        >
          一键发布（{{ unpublished.length }}）
        </el-button>
        <el-button v-permission="'score:write'" type="primary" :icon="EditPen" @click="openScore()">
          录入成绩
        </el-button>
      </template>
    </PageHeader>

    <!-- 学生视角 -->
    <template v-if="isStudent">
      <el-skeleton :loading="loading && !scores.length" animated :rows="5">
        <template #default>
          <section v-if="myScore" class="panel gd-score__mine">
            <div class="gd-score__total">
              <span>毕业设计总成绩</span>
              <strong>{{ myScore.total_score }}</strong>
              <el-tag :type="gradeOf(myScore.total_score).type" size="large" effect="dark" round>
                {{ gradeOf(myScore.total_score).label }}
              </el-tag>
              <small>发布时间 {{ dateText(myScore.updated_at) }}</small>
            </div>
            <div class="gd-score__parts">
              <div v-for="p in scoreParts" :key="p.label" class="gd-score__part">
                <div class="gd-score__part-head">
                  <span>{{ p.label }}</span>
                  <b>{{ p.score }}</b>
                </div>
                <el-progress
                  :percentage="Number(p.score)"
                  :show-text="false"
                  :stroke-width="8"
                  color="#175c4b"
                />
                <small
                  >权重 {{ (p.weight * 100).toFixed(0) }}% · 折合
                  {{ (p.score * p.weight).toFixed(2) }} 分</small
                >
              </div>
            </div>
          </section>
          <section v-else class="panel">
            <el-empty description="成绩尚未发布，请耐心等待" />
          </section>
        </template>
      </el-skeleton>
    </template>

    <!-- 教师 / 管理员视角 -->
    <template v-else>
      <div class="gd-score__stats">
        <div v-for="s in stats" :key="s.label" class="gd-score__stat">
          <span>{{ s.label }}</span>
          <strong
            >{{ s.value }}<small>{{ s.unit }}</small></strong
          >
        </div>
      </div>
      <section class="panel">
        <DataTable :data="scores" :loading="loading" placeholder="搜索学生姓名">
          <el-table-column label="学生" min-width="110">
            <template #default="{ row }">
              <router-link :to="`/students/${row.student_id}`" class="gd-link">{{
                row.student_name
              }}</router-link>
            </template>
          </el-table-column>
          <el-table-column prop="teacher_name" label="录入教师" width="100" />
          <el-table-column label="指导分" width="90" align="center">
            <template #default="{ row }">{{ row.guidance_score }}</template>
          </el-table-column>
          <el-table-column label="评阅分" width="90" align="center">
            <template #default="{ row }">{{ row.review_score }}</template>
          </el-table-column>
          <el-table-column label="答辩分" width="90" align="center">
            <template #default="{ row }">{{ row.defense_score }}</template>
          </el-table-column>
          <el-table-column label="权重" width="130" align="center">
            <template #default="{ row }">
              <span class="subtle"
                >{{ row.guidance_weight }} / {{ row.review_weight }} / {{ row.defense_weight }}</span
              >
            </template>
          </el-table-column>
          <el-table-column
            label="总成绩"
            width="100"
            align="center"
            sortable
            :sort-by="(r) => Number(r.total_score)"
          >
            <template #default="{ row }"
              ><b class="gd-score__num">{{ row.total_score }}</b></template
            >
          </el-table-column>
          <el-table-column label="等级" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="gradeOf(row.total_score).type" effect="light">{{
                gradeOf(row.total_score).label
              }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="发布状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.published ? 'success' : 'info'" effect="plain" round>
                {{ row.published ? "已发布" : "未发布" }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" fixed="right" width="140">
            <template #default="{ row }">
              <template v-if="!row.published">
                <el-button v-permission="'score:write'" link type="primary" @click="openScore(row)"
                  >修改</el-button
                >
                <el-button
                  v-if="isAdmin"
                  v-permission="'score:write'"
                  link
                  type="success"
                  @click="publish([row]).catch(() => {})"
                >
                  发布
                </el-button>
              </template>
              <span v-else class="subtle">已锁定</span>
            </template>
          </el-table-column>
        </DataTable>
      </section>
    </template>

    <!-- 录入对话框 -->
    <el-dialog v-model="dialog" title="录入 / 修改成绩" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="学生" prop="studentId">
          <el-select
            v-model="form.studentId"
            filterable
            placeholder="仅显示已安排答辩且成绩未发布的学生"
            style="width: 100%"
          >
            <el-option
              v-for="s in scorableStudents"
              :key="s.student_id"
              :value="s.student_id"
              :label="`${s.name} · ${s.topic_title || '未确定课题'}`"
            />
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="指导分">
              <el-input-number
                v-model="form.guidanceScore"
                :min="0"
                :max="100"
                :precision="1"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="评阅分（自动带入）">
              <el-input :model-value="currentReview ? currentReview.score : '保存时自动带入'" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="答辩分">
              <el-input-number
                v-model="form.defenseScore"
                :min="0"
                :max="100"
                :precision="1"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col
            v-for="(label, key) in {
              guidanceWeight: '指导权重',
              reviewWeight: '评阅权重',
              defenseWeight: '答辩权重',
            }"
            :key="key"
            :span="8"
          >
            <el-form-item :label="label">
              <el-input-number
                v-model="form[key]"
                :min="0"
                :max="1"
                :step="0.05"
                :precision="2"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <div class="gd-score__preview" :class="{ invalid: weightSum !== 1 }">
          <span
            >权重合计 <b>{{ weightSum }}</b>{{ weightSum !== 1 ? "（须等于 1）" : "" }}</span
          >
          <span v-if="previewTotal">
            预计总成绩 <b>{{ previewTotal }}</b>
            <el-tag :type="gradeOf(previewTotal).type" size="small">{{ gradeOf(previewTotal).label }}</el-tag>
          </span>
          <span v-else class="subtle">评阅完成后方可计算总成绩</span>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" :disabled="weightSum !== 1" @click="save"
          >保存成绩</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.gd-score__stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  margin-bottom: 16px;
}
.gd-score__stat {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 16px 18px;
  border: 1px solid #e6ece9;
  border-radius: 10px;
  background: #fff;
}
.gd-score__stat span {
  color: #7d9088;
  font-size: 12px;
}
.gd-score__stat strong {
  color: #1f3a32;
  font:
    700 26px/1 Georgia,
    serif;
}
.gd-score__stat small {
  margin-left: 3px;
  color: #8a9a93;
  font: 400 12px sans-serif;
}
.gd-score__num {
  color: #175c4b;
  font-size: 15px;
}
.gd-link {
  color: #175c4b;
  font-weight: 600;
}
.gd-score__mine {
  display: grid;
  grid-template-columns: 260px 1fr;
  gap: 32px;
  align-items: center;
}
.gd-score__total {
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: center;
  padding: 24px;
  border-radius: 14px;
  background: linear-gradient(135deg, #175c4b, #10352c);
  color: #cfe2da;
}
.gd-score__total strong {
  color: #fff;
  font:
    700 56px/1 Georgia,
    serif;
}
.gd-score__total small {
  color: #8fb3a5;
  font-size: 12px;
}
.gd-score__parts {
  display: flex;
  flex-direction: column;
  gap: 18px;
}
.gd-score__part-head {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
  color: #4f665d;
  font-size: 14px;
}
.gd-score__part-head b {
  color: #1f3a32;
  font-size: 18px;
}
.gd-score__part small {
  color: #8a9a93;
  font-size: 12px;
}
.gd-score__preview {
  display: flex;
  justify-content: space-between;
  padding: 12px 16px;
  border-radius: 8px;
  background: #f0f6f3;
  color: #4f665d;
  font-size: 13px;
}
.gd-score__preview b {
  color: #175c4b;
  font-size: 16px;
}
.gd-score__preview.invalid {
  background: #fdf0ee;
}
.gd-score__preview.invalid b {
  color: #d9634f;
}
@media (max-width: 1100px) {
  .gd-score__stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .gd-score__mine {
    grid-template-columns: 1fr;
  }
}
</style>
