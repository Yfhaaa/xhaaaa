<script setup>
import * as topicApi from "../../api/topic";
import { getReference } from "../../api/system";
import { ref, reactive, computed, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { dateText } from "../../utils/format";
import { useAuth } from "../../stores/auth";
import StatusTag from "../../components/StatusTag.vue";
const auth = useAuth(),
  topics = ref([]),
  selections = ref([]),
  reference = ref({ majors: [] }),
  loading = ref(false),
  saving = ref(false),
  dialog = ref(false),
  formRef = ref(),
  search = ref(""),
  active = ref("topics");
const form = reactive({ id: null, title: "", description: "", majorId: 1, capacity: 1 });
const rules = {
  title: [{ required: true, message: "请输入课题名称" }],
  description: [{ required: true, message: "请填写研究内容" }],
  majorId: [{ required: true, message: "请选择专业" }],
};
const filtered = computed(() =>
  topics.value.filter((r) => (r.title + r.teacher_name).includes(search.value)),
);
async function refresh() {
  loading.value = true;
  try {
    [topics.value, selections.value, reference.value] = await Promise.all([
      topicApi.listTopics(),
      topicApi.listSelections(),
      getReference(),
    ]);
  } finally {
    loading.value = false;
  }
}
function open(row) {
  Object.assign(form, {
    id: row?.id || null,
    title: row?.title || "",
    description: row?.description || "",
    majorId: row?.major_id || reference.value.majors[0]?.id,
    capacity: row?.capacity || 1,
  });
  dialog.value = true;
}
async function save() {
  if (!(await formRef.value.validate().catch(() => false))) return;
  saving.value = true;
  try {
    await topicApi.saveTopic(form.id, form);
    dialog.value = false;
    ElMessage.success("课题已提交审核");
    await refresh();
  } finally {
    saving.value = false;
  }
}
async function review(row, approved, selection = false) {
  try {
    const { value } = await ElMessageBox.prompt(
      approved ? "请填写通过意见" : "请填写退回原因",
      selection ? "双选确认" : "课题审核",
      {
        inputType: "textarea",
        inputValidator: (v) => !!v?.trim() || "审核意见不能为空",
        confirmButtonText: approved ? "确认通过" : "确认退回",
        cancelButtonText: "取消",
      },
    );
    await (selection ? topicApi.reviewSelection : topicApi.reviewTopic)(row.id, {
      approved,
      comment: value,
    });
    ElMessage.success("已处理");
    await refresh();
  } catch (e) {
    if (e !== "cancel" && e !== "close") console.debug("审核未完成");
  }
}
async function apply(row) {
  try {
    await ElMessageBox.confirm(
      "确认申请「" + row.title + "」？教师确认后将确定师生关系。",
      "提交双选申请",
      {
        type: "info",
      },
    );
    await topicApi.applyTopic(row.id);
    ElMessage.success("申请成功，等待教师确认");
    await refresh();
  } catch {}
}
onMounted(refresh);
</script>
<template>
  <div v-loading="loading">
    <div class="page-heading">
      <div>
        <h1>课题与双选</h1>
        <p>从研究兴趣出发，建立清晰的师生指导关系</p>
      </div>
      <el-button
        v-if="auth.role === 'TEACHER' && auth.can('topic:write')"
        type="primary"
        @click="open()"
      >
        ＋ 申报课题
      </el-button>
    </div>
    <section class="panel">
      <el-tabs v-model="active">
        <el-tab-pane
          label="课题库"
          name="topics"
        >
          <div class="toolbar">
            <el-input
              v-model="search"
              placeholder="搜索课题或指导教师"
              clearable
            />
            <span class="subtle">共 {{ filtered.length }} 个课题</span>
          </div>
          <el-table :data="filtered">
            <el-table-column
              prop="title"
              label="课题名称"
              min-width="220"
              show-overflow-tooltip
            />
            <el-table-column
              prop="teacher_name"
              label="指导教师"
              width="100"
            />
            <el-table-column
              prop="major_name"
              label="专业"
              min-width="150"
            />
            <el-table-column
              label="已选 / 名额"
              width="100"
            >
              <template #default="{ row }">{{ row.selected_count }} / {{ row.capacity }}</template>
            </el-table-column>
            <el-table-column
              label="状态"
              width="100"
            >
              <template #default="{ row }"><StatusTag :status="row.status" /></template>
            </el-table-column>
            <el-table-column
              label="操作"
              fixed="right"
              min-width="180"
            >
              <template #default="{ row }">
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
                  <h3>{{ row.title }}</h3>
                  <p class="pre-wrap">{{ row.description }}</p>
                  <p v-if="row.review_comment">审核意见：{{ row.review_comment }}</p>
                  <p class="subtle">
                    提交：{{ dateText(row.submitted_at) }} · 修改 {{ row.revision_count }} 次
                  </p>
                </el-popover>
                <template v-if="auth.role === 'ADMIN' && row.status === '待审核'">
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
                <el-button
                  v-if="auth.role === 'TEACHER' && row.status === '已退回'"
                  link
                  type="primary"
                  @click="open(row)"
                >
                  修改重提
                </el-button>
                <el-button
                  v-if="auth.role === 'STUDENT'"
                  link
                  type="primary"
                  :disabled="row.selected_count >= row.capacity"
                  @click="apply(row)"
                >
                  申请选题
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane
          :label="auth.role === 'STUDENT' ? '我的申请' : '双选申请'"
          name="selections"
        >
          <el-table :data="selections">
            <el-table-column
              prop="student_name"
              label="学生"
              width="90"
            />
            <el-table-column
              prop="title"
              label="课题"
              min-width="220"
            />
            <el-table-column
              prop="teacher_name"
              label="教师"
              width="100"
            />
            <el-table-column
              label="状态"
              width="100"
            >
              <template #default="{ row }"><StatusTag :status="row.status" /></template>
            </el-table-column>
            <el-table-column
              prop="review_comment"
              label="审核意见"
              min-width="170"
            />
            <el-table-column
              label="申请时间"
              min-width="160"
            >
              <template #default="{ row }">{{ dateText(row.submitted_at) }}</template>
            </el-table-column>
            <el-table-column
              v-if="auth.role === 'TEACHER'"
              label="操作"
              fixed="right"
              width="130"
            >
              <template #default="{ row }">
                <template v-if="row.status === '待审核'">
                  <el-button
                    link
                    type="primary"
                    @click="review(row, true, true)"
                  >
                    确认
                  </el-button>
                  <el-button
                    link
                    type="danger"
                    @click="review(row, false, true)"
                  >
                    退回
                  </el-button>
                </template>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </section>
    <div class="note-box">
      教师申报 → 管理员审核 → 学生申请 →
      教师确认。每名学生只确定一个课题；
      课题名额与专业在提交和确认时均由服务端校验。
    </div>
    <el-dialog
      v-model="dialog"
      :title="form.id ? '修改课题并重新提交' : '申报新课题'"
      width="600px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
      >
        <el-form-item
          label="课题名称"
          prop="title"
        >
          <el-input
            v-model="form.title"
            maxlength="200"
          />
        </el-form-item>
        <el-form-item
          label="面向专业"
          prop="majorId"
        >
          <el-select v-model="form.majorId">
            <el-option
              v-for="m in reference.majors"
              :key="m.id"
              :label="m.name"
              :value="m.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="课题名额">
          <el-input-number
            v-model="form.capacity"
            :min="1"
            :max="20"
          />
        </el-form-item>
        <el-form-item
          label="研究内容与要求"
          prop="description"
        >
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="5"
            maxlength="10000"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button
          type="primary"
          :loading="saving"
          @click="save"
        >
          提交审核
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>
