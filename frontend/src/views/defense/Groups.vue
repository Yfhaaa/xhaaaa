<script setup>
/** 答辩分组：院系管理员创建答辩组（教师、地点、时间），各角色查看分组与已安排人数。 */
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { Calendar, Location, Plus, UserFilled } from "@element-plus/icons-vue";
import * as defenseApi from "../../api/defense";
import { getReference } from "../../api/system";
import { useAuth } from "../../stores/auth";
import { dateText } from "../../utils/format";

const auth = useAuth();
const loading = ref(false);
const groups = ref([]);
const defenses = ref([]);
const teachers = ref([]);

async function refresh() {
  loading.value = true;
  try {
    const [groupRows, defenseRows, reference] = await Promise.all([
      defenseApi.listGroups(),
      defenseApi.listDefenses(),
      getReference(),
    ]);
    groups.value = groupRows;
    defenses.value = defenseRows;
    teachers.value = reference.teachers || [];
  } finally {
    loading.value = false;
  }
}
onMounted(refresh);

const teacherName = (id) => teachers.value.find((t) => t.id === Number(id))?.name || `教师${id}`;
const cards = computed(() =>
  groups.value.map((g) => {
    const members = defenses.value.filter((d) => d.group_id === g.id);
    const days = Math.ceil((new Date(g.defense_at) - Date.now()) / 86400000);
    return {
      ...g,
      teacherNames: String(g.teacher_ids || "")
        .split(",")
        .filter(Boolean)
        .map(teacherName),
      members,
      days,
    };
  }),
);

/* ---------------- 新建答辩组 ---------------- */
const dialog = ref(false);
const saving = ref(false);
const formRef = ref();
const form = reactive({ name: "", teacherIds: [], location: "", defenseAt: "" });
const rules = {
  name: [{ required: true, message: "请填写答辩组名称", trigger: "blur" }],
  teacherIds: [
    { required: true, type: "array", min: 2, message: "答辩组至少需要 2 名教师", trigger: "change" },
  ],
  location: [{ required: true, message: "请填写答辩地点", trigger: "blur" }],
  defenseAt: [{ required: true, message: "请选择答辩时间", trigger: "change" }],
};
function open() {
  Object.assign(form, {
    name: `第${groups.value.length + 1}答辩组`,
    teacherIds: [],
    location: "",
    defenseAt: "",
  });
  dialog.value = true;
}
async function save() {
  if (!(await formRef.value.validate().catch(() => false))) return;
  saving.value = true;
  try {
    await defenseApi.saveDefense("group", { ...form });
    ElMessage.success("答辩组已创建");
    dialog.value = false;
    await refresh();
  } finally {
    saving.value = false;
  }
}
const disabledDate = (date) => date.getTime() < Date.now() - 86400000;
</script>

<template>
  <div>
    <PageHeader title="答辩分组" description="按专业与时间组织答辩小组，每组不少于 2 名教师">
      <el-button
        v-if="auth.role === 'ADMIN'"
        v-permission="'defense:write'"
        type="primary"
        :icon="Plus"
        @click="open"
      >
        新建答辩组
      </el-button>
    </PageHeader>

    <el-skeleton :loading="loading && !groups.length" animated :rows="5">
      <template #default>
        <section v-if="!cards.length" class="panel">
          <el-empty description="尚未创建答辩组" />
        </section>
        <div class="gd-groups">
          <article v-for="g in cards" :key="g.id" class="gd-group">
            <header>
              <h3>{{ g.name }}</h3>
              <el-tag v-if="g.days > 0" type="success" effect="light" round>{{ g.days }} 天后</el-tag>
              <el-tag v-else-if="g.days === 0" type="warning" effect="light" round>今天</el-tag>
              <el-tag v-else type="info" effect="light" round>已结束</el-tag>
            </header>
            <ul class="gd-group__info">
              <li>
                <el-icon><Calendar /></el-icon>{{ dateText(g.defense_at) }}
              </li>
              <li>
                <el-icon><Location /></el-icon>{{ g.location }}
              </li>
              <li>
                <el-icon><UserFilled /></el-icon>{{ g.teacherNames.join("、") }}
              </li>
            </ul>
            <div class="gd-group__members">
              <div class="gd-group__members-head">
                已安排学生 <b>{{ g.members.length }}</b> 人
              </div>
              <div v-if="g.members.length" class="gd-group__avatars">
                <el-tooltip
                  v-for="m in g.members"
                  :key="m.id"
                  :content="`${m.student_name} · ${m.topic_title}`"
                >
                  <el-avatar :size="30">{{ m.student_name.slice(0, 1) }}</el-avatar>
                </el-tooltip>
              </div>
              <span v-else class="subtle">暂未安排学生</span>
            </div>
          </article>
        </div>
      </template>
    </el-skeleton>

    <el-dialog v-model="dialog" title="新建答辩组" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="答辩组名称" prop="name">
          <el-input v-model="form.name" maxlength="50" />
        </el-form-item>
        <el-form-item label="答辩教师" prop="teacherIds">
          <el-select
            v-model="form.teacherIds"
            multiple
            filterable
            placeholder="至少选择 2 名"
            style="width: 100%"
          >
            <el-option v-for="t in teachers" :key="t.id" :value="t.id" :label="t.name" />
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="答辩地点" prop="location">
              <el-input v-model="form.location" maxlength="50" placeholder="如：教学楼 A302" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="答辩时间" prop="defenseAt">
              <el-date-picker
                v-model="form.defenseAt"
                type="datetime"
                value-format="YYYY-MM-DDTHH:mm:ss"
                format="YYYY-MM-DD HH:mm"
                :disabled-date="disabledDate"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.gd-groups {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}
.gd-group {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 20px;
  border: 1px solid #e3eae6;
  border-top: 4px solid #175c4b;
  border-radius: 12px;
  background: #fff;
}
.gd-group header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.gd-group h3 {
  margin: 0;
  color: #1f3a32;
  font-size: 16px;
}
.gd-group__info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin: 0;
  padding: 0;
  color: #4f665d;
  font-size: 13px;
  list-style: none;
}
.gd-group__info li {
  display: flex;
  gap: 8px;
  align-items: center;
}
.gd-group__info .el-icon {
  color: #a0874a;
}
.gd-group__members {
  padding-top: 12px;
  border-top: 1px dashed #e6ece9;
}
.gd-group__members-head {
  margin-bottom: 8px;
  color: #7d9088;
  font-size: 12px;
}
.gd-group__members-head b {
  color: #175c4b;
}
.gd-group__avatars {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.gd-group__avatars .el-avatar {
  background: #e3eee9;
  color: #175c4b;
  font-size: 13px;
  font-weight: 600;
}
</style>
