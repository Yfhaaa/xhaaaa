<script setup>
import * as defenseApi from "../../api/defense";
import { getReference, listStudents } from "../../api/system";
import { ref, reactive, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { dateText } from "../../utils/format";
import { useAuth } from "../../stores/auth";
import StatusTag from "../../components/StatusTag.vue";
const auth = useAuth(),
  groups = ref([]),
  defenses = ref([]),
  scores = ref([]),
  students = ref([]),
  reference = ref({ teachers: [] }),
  loading = ref(false),
  saving = ref(false),
  dialog = ref(""),
  formRef = ref();
const form = reactive({});
async function refresh() {
  loading.value = true;
  try {
    [groups.value, defenses.value, scores.value, students.value, reference.value] =
      await Promise.all([
        defenseApi.listGroups(),
        defenseApi.listDefenses(),
        defenseApi.listScores(),
        listStudents(),
        getReference(),
      ]);
  } finally {
    loading.value = false;
  }
}
function open(type) {
  for (const key of Object.keys(form)) delete form[key];
  Object.assign(
    form,
    type === "group"
      ? { name: "", teacherIds: [], location: "", defenseAt: "" }
      : type === "arrange"
        ? { studentId: students.value[0]?.student_id, groupId: groups.value[0]?.id }
        : {
            studentId: students.value[0]?.student_id,
            guidanceScore: 85,
            reviewScore: 85,
            defenseScore: 85,
            guidanceWeight: 0.3,
            reviewWeight: 0.3,
            defenseWeight: 0.4,
          },
  );
  dialog.value = type;
}
async function save() {
  if (!(await formRef.value.validate().catch(() => false))) return;
  if (dialog.value === "group" && !form.teacherIds.length) {
    ElMessage.warning("请选择答辩教师");
    return;
  }
  saving.value = true;
  try {
    await defenseApi.saveDefense(dialog.value, form);
    ElMessage.success("保存成功");
    dialog.value = "";
    await refresh();
  } finally {
    saving.value = false;
  }
}
async function publish(row) {
  try {
    await ElMessageBox.confirm("发布后学生可查询成绩，成绩将锁定。确认发布？", "发布成绩", {
      type: "info",
    });
    await defenseApi.publishScore(row.id);
    ElMessage.success("成绩已发布");
    await refresh();
  } catch {}
}
onMounted(refresh);
</script>
<template>
  <div v-loading="loading">
    <div class="page-heading">
      <div>
        <h1>答辩与成绩</h1>
        <p>有序组织答辩，依据权重规范汇总成绩</p>
      </div>
      <div class="actions">
        <template v-if="auth.role === 'ADMIN'">
          <el-button @click="open('group')">新建答辩组</el-button>
          <el-button
            type="primary"
            @click="open('arrange')"
          >
            安排学生
          </el-button>
        </template>
        <el-button
          v-if="auth.role === 'TEACHER' && auth.can('score:write')"
          type="primary"
          @click="open('score')"
        >
          录入 / 修改成绩
        </el-button>
      </div>
    </div>
    <section class="panel">
      <el-tabs>
        <el-tab-pane label="答辩安排">
          <el-table :data="defenses">
            <el-table-column
              prop="student_name"
              label="学生"
              width="90"
            />
            <el-table-column
              prop="topic_title"
              label="课题"
              min-width="200"
            />
            <el-table-column
              prop="group_name"
              label="答辩组"
              min-width="160"
            />
            <el-table-column
              prop="location"
              label="地点"
              width="120"
            />
            <el-table-column
              label="答辩时间"
              width="180"
            >
              <template #default="{ row }">{{ dateText(row.defense_at) }}</template>
            </el-table-column>
            <el-table-column
              label="状态"
              width="100"
            >
              <template #default="{ row }"><StatusTag :status="row.status" /></template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="成绩汇总">
          <div class="note-box">
            总成绩 = 指导分 × 指导权重 + 评阅分 × 评阅权重 + 答辩分 ×
            答辩权重。三项权重之和必须为1；学生仅能查询已经发布的成绩。
          </div>
          <el-table :data="scores">
            <el-table-column
              prop="student_name"
              label="学生"
              width="90"
            />
            <el-table-column
              prop="guidance_score"
              label="指导分"
              width="90"
            />
            <el-table-column
              prop="review_score"
              label="评阅分"
              width="90"
            />
            <el-table-column
              prop="defense_score"
              label="答辩分"
              width="90"
            />
            <el-table-column
              label="权重（指导/评阅/答辩）"
              min-width="170"
            >
              <template #default="{ row }">
                {{ row.guidance_weight }} / {{ row.review_weight }} / {{ row.defense_weight }}
              </template>
            </el-table-column>
            <el-table-column
              prop="total_score"
              label="总成绩"
              width="100"
            />
            <el-table-column
              label="发布状态"
              width="100"
            >
              <template #default="{ row }">
                <el-tag :type="row.published ? 'success' : 'info'">
                  {{ row.published ? "已发布" : "未发布" }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column
              v-if="auth.role === 'ADMIN'"
              label="操作"
              fixed="right"
              width="100"
            >
              <template #default="{ row }">
                <el-button
                  v-if="!row.published"
                  link
                  type="primary"
                  @click="publish(row)"
                >
                  发布成绩
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="答辩组">
          <el-table :data="groups">
            <el-table-column
              prop="name"
              label="答辩组"
              min-width="200"
            />
            <el-table-column
              prop="teacher_ids"
              label="教师编号"
              min-width="120"
            />
            <el-table-column
              prop="location"
              label="地点"
              min-width="150"
            />
            <el-table-column
              label="时间"
              min-width="180"
            >
              <template #default="{ row }">{{ dateText(row.defense_at) }}</template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </section>
    <el-dialog
      :model-value="!!dialog"
      @update:model-value="
        (v) => {
          if (!v) dialog = '';
        }
      "
      :title="{ group: '新建答辩组', arrange: '安排学生答辩', score: '录入 / 修改成绩' }[dialog]"
      width="560px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        label-position="top"
      >
        <template v-if="dialog === 'group'">
          <el-form-item
            label="答辩组名称"
            prop="name"
            :rules="[{ required: true, message: '请填写名称' }]"
          >
            <el-input
              v-model="form.name"
              maxlength="100"
            />
          </el-form-item>
          <el-form-item label="答辩教师">
            <el-select
              v-model="form.teacherIds"
              multiple
            >
              <el-option
                v-for="t in reference.teachers"
                :key="t.id"
                :value="t.id"
                :label="t.name"
              />
            </el-select>
          </el-form-item>
          <el-form-item
            label="答辩地点"
            prop="location"
            :rules="[{ required: true, message: '请填写地点' }]"
          >
            <el-input
              v-model="form.location"
              maxlength="100"
            />
          </el-form-item>
          <el-form-item
            label="答辩时间"
            prop="defenseAt"
            :rules="[{ required: true, message: '请选择时间' }]"
          >
            <el-date-picker
              v-model="form.defenseAt"
              type="datetime"
              value-format="YYYY-MM-DDTHH:mm:ss"
            />
          </el-form-item>
        </template>
        <template v-else>
          <el-form-item
            label="学生"
            prop="studentId"
            :rules="[{ required: true, message: '请选择学生' }]"
          >
            <el-select v-model="form.studentId">
              <el-option
                v-for="s in students"
                :key="s.student_id"
                :value="s.student_id"
                :label="s.name"
              />
            </el-select>
          </el-form-item>
          <el-form-item
            v-if="dialog === 'arrange'"
            label="答辩组"
            prop="groupId"
            :rules="[{ required: true, message: '请选择答辩组' }]"
          >
            <el-select v-model="form.groupId">
              <el-option
                v-for="g in groups"
                :key="g.id"
                :value="g.id"
                :label="g.name + ' · ' + g.location"
              />
            </el-select>
          </el-form-item>
          <template v-if="dialog === 'score'">
            <div class="split equal">
              <div>
                <el-form-item
                  v-for="(label, key) in {
                    guidanceScore: '指导分',
                    reviewScore: '评阅分',
                    defenseScore: '答辩分',
                  }"
                  :key="key"
                  :label="label"
                >
                  <el-input-number
                    v-model="form[key]"
                    :min="0"
                    :max="100"
                    :precision="2"
                  />
                </el-form-item>
              </div>
              <div>
                <el-form-item
                  v-for="(label, key) in {
                    guidanceWeight: '指导权重',
                    reviewWeight: '评阅权重',
                    defenseWeight: '答辩权重',
                  }"
                  :key="key"
                  :label="label"
                >
                  <el-input-number
                    v-model="form[key]"
                    :min="0"
                    :max="1"
                    :precision="2"
                    :step="0.1"
                  />
                </el-form-item>
              </div>
            </div>
          </template>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="dialog = ''">取消</el-button>
        <el-button
          type="primary"
          :loading="saving"
          @click="save"
        >
          确认保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>
