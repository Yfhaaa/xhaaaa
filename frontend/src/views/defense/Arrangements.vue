<script setup>
/**
 * 答辩安排：管理员将评阅同意答辩的学生批量安排到答辩组；
 * 教师查看本人指导学生安排；学生查看本人答辩时间地点与倒计时。
 */
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { Calendar, Location, Plus } from "@element-plus/icons-vue";
import * as defenseApi from "../../api/defense";
import { get } from "../../api/advanced";
import { listStudents } from "../../api/system";
import { useAuth } from "../../stores/auth";
import { dateText } from "../../utils/format";
import StatusTag from "../../components/StatusTag.vue";

const auth = useAuth();
const isStudent = computed(() => auth.role === "STUDENT");
const isAdmin = computed(() => auth.role === "ADMIN");

const loading = ref(false);
const defenses = ref([]);
const groups = ref([]);
const students = ref([]);
const reviews = ref([]);

async function refresh() {
  loading.value = true;
  try {
    const tasks = [defenseApi.listDefenses(), defenseApi.listGroups()];
    if (isAdmin.value)
      tasks.push(
        listStudents(),
        get("/thesis-reviews").catch(() => []),
      );
    const [defenseRows, groupRows, studentRows = [], reviewRows = []] = await Promise.all(tasks);
    defenses.value = defenseRows;
    groups.value = groupRows;
    students.value = studentRows;
    reviews.value = reviewRows;
  } finally {
    loading.value = false;
  }
}
onMounted(refresh);

const myDefense = computed(() => defenses.value.find((d) => d.student_id === auth.user?.id) || null);
const countdown = computed(() => {
  if (!myDefense.value) return null;
  return Math.ceil((new Date(myDefense.value.defense_at) - Date.now()) / 86400000);
});

/* ---------------- 批量安排 ---------------- */
const dialog = ref(false);
const saving = ref(false);
const formRef = ref();
const form = reactive({ studentIds: [], groupId: null });
const rules = {
  studentIds: [{ required: true, type: "array", min: 1, message: "请至少选择 1 名学生", trigger: "change" }],
  groupId: [{ required: true, message: "请选择答辩组", trigger: "change" }],
};
/** 可安排：评阅已同意答辩、且尚未安排的学生 */
const arrangeable = computed(() => {
  const arranged = new Set(defenses.value.map((d) => d.student_id));
  const agreed = new Set(
    reviews.value
      .filter((r) => r.status === "已评阅" && Number(r.agree_defense) === 1)
      .map((r) => r.student_id),
  );
  return students.value
    .filter((s) => !arranged.has(s.student_id))
    .map((s) => ({ ...s, ready: agreed.has(s.student_id) }));
});
function open() {
  Object.assign(form, { studentIds: [], groupId: groups.value[0]?.id ?? null });
  dialog.value = true;
}
async function save() {
  if (!(await formRef.value.validate().catch(() => false))) return;
  saving.value = true;
  const failed = [];
  try {
    for (const studentId of form.studentIds) {
      try {
        await defenseApi.saveDefense("arrange", { studentId, groupId: form.groupId });
      } catch {
        failed.push(students.value.find((s) => s.student_id === studentId)?.name || studentId);
      }
    }
    const okCount = form.studentIds.length - failed.length;
    if (okCount) ElMessage.success(`已安排 ${okCount} 名学生，学生将收到答辩通知`);
    if (failed.length) ElMessage.warning(`以下学生未能安排：${failed.join("、")}`);
    dialog.value = false;
    await refresh();
  } finally {
    saving.value = false;
  }
}
</script>

<template>
  <div>
    <PageHeader title="答辩安排" description="评阅同意答辩的学生方可安排答辩，安排后自动通知学生">
      <el-button v-if="isAdmin" v-permission="'defense:write'" type="primary" :icon="Plus" @click="open">
        安排答辩
      </el-button>
    </PageHeader>

    <!-- 学生 -->
    <template v-if="isStudent">
      <section v-if="myDefense" class="gd-defense__ticket">
        <div class="gd-defense__ticket-main">
          <span class="gd-defense__eyebrow">DEFENSE NOTICE · 答辩通知</span>
          <h2>{{ myDefense.topic_title }}</h2>
          <ul>
            <li>
              <el-icon><Calendar /></el-icon>{{ dateText(myDefense.defense_at) }}
            </li>
            <li>
              <el-icon><Location /></el-icon>{{ myDefense.location }} · {{ myDefense.group_name }}
            </li>
          </ul>
        </div>
        <div class="gd-defense__ticket-side">
          <template v-if="countdown > 0">
            <strong>{{ countdown }}</strong>
            <span>天后答辩</span>
          </template>
          <template v-else>
            <StatusTag :status="myDefense.status" size="large" />
          </template>
        </div>
      </section>
      <section v-else class="panel">
        <el-empty description="暂未安排答辩；论文评阅同意答辩后，院系将统一安排" />
      </section>
    </template>

    <!-- 教师 / 管理员 -->
    <section v-else class="panel">
      <DataTable
        :data="defenses"
        :loading="loading"
        status-field="status"
        placeholder="搜索学生、课题或答辩组"
      >
        <el-table-column label="学生" width="110">
          <template #default="{ row }">
            <router-link :to="`/students/${row.student_id}`" class="gd-link">{{
              row.student_name
            }}</router-link>
          </template>
        </el-table-column>
        <el-table-column prop="topic_title" label="课题" min-width="220" show-overflow-tooltip />
        <el-table-column prop="group_name" label="答辩组" min-width="160" />
        <el-table-column prop="location" label="地点" width="130" />
        <el-table-column label="答辩时间" width="170">
          <template #default="{ row }">{{ dateText(row.defense_at) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }"><StatusTag :status="row.status" /></template>
        </el-table-column>
      </DataTable>
    </section>

    <el-dialog v-model="dialog" title="安排答辩" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="答辩组" prop="groupId">
          <el-select v-model="form.groupId" style="width: 100%">
            <el-option
              v-for="g in groups"
              :key="g.id"
              :value="g.id"
              :label="`${g.name} · ${g.location} · ${dateText(g.defense_at)}`"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="学生（可多选）" prop="studentIds">
          <el-select
            v-model="form.studentIds"
            multiple
            filterable
            placeholder="选择待安排学生"
            style="width: 100%"
          >
            <el-option
              v-for="s in arrangeable"
              :key="s.student_id"
              :value="s.student_id"
              :label="s.name"
              :disabled="!s.ready"
            >
              <span>{{ s.name }}</span>
              <span class="gd-defense__opt">{{ s.ready ? s.topic_title : "评阅未同意答辩" }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-alert
          type="info"
          :closable="false"
          show-icon
          title="仅论文评阅已完成且同意答辩的学生可被安排；不可选的学生请先在“评阅分配”中完成评阅。"
        />
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">确认安排</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.gd-link {
  color: #175c4b;
  font-weight: 600;
}
.gd-defense__ticket {
  display: grid;
  grid-template-columns: 1fr 200px;
  overflow: hidden;
  border-radius: 14px;
  background: linear-gradient(120deg, #175c4b, #10352c);
  color: #d5e4de;
  box-shadow: 0 12px 30px rgb(18 62 52 / 18%);
}
.gd-defense__ticket-main {
  padding: 28px 32px;
}
.gd-defense__eyebrow {
  color: #d8be7b;
  font-size: 12px;
  letter-spacing: 2px;
}
.gd-defense__ticket h2 {
  margin: 10px 0 16px;
  color: #fff;
  font-size: 22px;
}
.gd-defense__ticket ul {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin: 0;
  padding: 0;
  list-style: none;
}
.gd-defense__ticket li {
  display: flex;
  gap: 8px;
  align-items: center;
}
.gd-defense__ticket-side {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-left: 2px dashed rgb(255 255 255 / 18%);
  background: rgb(0 0 0 / 12%);
}
.gd-defense__ticket-side strong {
  color: #d8be7b;
  font:
    700 54px/1 Georgia,
    serif;
}
.gd-defense__opt {
  float: right;
  margin-left: 16px;
  color: #8a9a93;
  font-size: 12px;
}
</style>
