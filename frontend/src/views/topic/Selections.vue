<script setup>
/**
 * 双选申请：学生在课题库提交申请后，指导教师在此确认或退回；
 * 确认即建立唯一指导关系并占用课题名额（后端加行锁防止并发超额）。
 */
import { computed, onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import * as topicApi from "../../api/topic";
import { useAuth } from "../../stores/auth";
import { dateText } from "../../utils/format";
import ReviewDialog from "../../components/ReviewDialog.vue";
import StatusTag from "../../components/StatusTag.vue";

const auth = useAuth();
const isTeacher = computed(() => auth.role === "TEACHER");
const isStudent = computed(() => auth.role === "STUDENT");

const loading = ref(false);
const selections = ref([]);
const topics = ref([]);

async function refresh() {
  loading.value = true;
  try {
    const [selectionRows, topicRows] = await Promise.all([topicApi.listSelections(), topicApi.listTopics()]);
    selections.value = selectionRows;
    topics.value = topicRows;
  } finally {
    loading.value = false;
  }
}
onMounted(refresh);

const pendingCount = computed(() => selections.value.filter((s) => s.status === "待审核").length);
const topicOf = (row) => topics.value.find((t) => t.id === row.topic_id);

/* ---------------- 确认 / 退回 ---------------- */
const reviewing = ref(null);
const saving = ref(false);
const summary = computed(() => {
  const r = reviewing.value;
  if (!r) return [];
  const topic = topicOf(r);
  return [
    { label: "学生", value: r.student_name },
    { label: "申请时间", value: dateText(r.submitted_at) },
    { label: "课题", value: r.title },
    { label: "名额", value: topic ? `已选 ${topic.selected_count} / ${topic.capacity}` : "—" },
  ];
});
async function doReview({ approved, comment }) {
  saving.value = true;
  try {
    await topicApi.reviewSelection(reviewing.value.id, { approved, comment });
    ElMessage.success(approved ? "已确认，师生指导关系建立" : "已退回，学生可重新选择课题");
    reviewing.value = null;
    await refresh();
  } finally {
    saving.value = false;
  }
}
</script>

<template>
  <div>
    <PageHeader
      title="双选申请"
      :description="
        isStudent ? '查看本人选题申请的确认进度' : '学生申请 → 指导教师确认，确认后即确定唯一指导关系'
      "
    >
      <el-tag v-if="isTeacher && pendingCount" type="warning" effect="light" round size="large">
        {{ pendingCount }} 条申请待确认
      </el-tag>
      <router-link v-if="isStudent" to="/topics">
        <el-button type="primary">去课题库选题</el-button>
      </router-link>
    </PageHeader>

    <section class="panel">
      <DataTable :data="selections" :loading="loading" status-field="status" placeholder="搜索学生或课题">
        <el-table-column v-if="!isStudent" label="学生" width="110">
          <template #default="{ row }">
            <router-link :to="`/students/${row.student_id}`" class="gd-link">{{ row.student_name }}</router-link>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="申请课题" min-width="240" show-overflow-tooltip />
        <el-table-column v-if="!isTeacher" prop="teacher_name" label="指导教师" width="100" />
        <el-table-column label="名额" width="90" align="center">
          <template #default="{ row }">
            <span v-if="topicOf(row)">{{ topicOf(row).selected_count }}/{{ topicOf(row).capacity }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }"><StatusTag :status="row.status" /></template>
        </el-table-column>
        <el-table-column label="确认意见" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <span :class="{ subtle: !row.review_comment }">{{ row.review_comment || "等待教师确认" }}</span>
          </template>
        </el-table-column>
        <el-table-column label="申请时间" width="170">
          <template #default="{ row }">{{ dateText(row.submitted_at) }}</template>
        </el-table-column>
        <el-table-column v-if="isTeacher" label="操作" fixed="right" width="100">
          <template #default="{ row }">
            <el-button
              v-if="row.status === '待审核'"
              v-permission="'topic:write'"
              link
              type="warning"
              @click="reviewing = row"
            >
              处理申请
            </el-button>
            <span v-else class="subtle">已处理</span>
          </template>
        </el-table-column>
      </DataTable>
    </section>

    <ReviewDialog
      :model-value="!!reviewing"
      title="处理选题申请"
      :summary="summary"
      :loading="saving"
      @update:model-value="reviewing = null"
      @submit="doReview"
    />
  </div>
</template>

<style scoped>
.gd-link {
  color: #175c4b;
  font-weight: 600;
}
</style>
