<script setup>
/**
 * 流程节点：配置毕业设计九个业务节点的截止时间与论文查重阈值。
 * 截止时间用于学生端倒计时、首页提醒与每日截止前 3 天的站内消息。
 */
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { listWorkflow, saveSystemRecord } from "../../api/system";
import { dateText } from "../../utils/format";

const loading = ref(false);
const steps = ref([]);

async function refresh() {
  loading.value = true;
  try {
    steps.value = (await listWorkflow()).sort((a, b) => a.sort_order - b.sort_order);
  } finally {
    loading.value = false;
  }
}
onMounted(refresh);

/** 当前进行中的节点：第一个截止时间未到的节点 */
const activeIndex = computed(() => {
  const index = steps.value.findIndex((s) => new Date(s.deadline) > new Date());
  return index < 0 ? steps.value.length : index;
});
function daysLeft(step) {
  return Math.ceil((new Date(step.deadline) - Date.now()) / 86400000);
}
function stepState(index) {
  if (index < activeIndex.value) return { text: "已截止", type: "info" };
  if (index === activeIndex.value) return { text: "进行中", type: "success" };
  return { text: "未开始", type: "" };
}

/* ---------------- 编辑 ---------------- */
const dialog = reactive({ visible: false, step: null });
const saving = ref(false);
const form = reactive({ deadline: "", plagiarismThreshold: 30 });

function open(step) {
  dialog.step = step;
  dialog.visible = true;
  form.deadline = new Date(step.deadline).toLocaleString("sv-SE").replace(" ", "T");
  form.plagiarismThreshold = Number(step.plagiarism_threshold ?? 30);
}
/** 截止时间须在前一节点之后、后一节点之前，保证流程顺序 */
const orderWarning = computed(() => {
  if (!dialog.step || !form.deadline) return "";
  const index = steps.value.findIndex((s) => s.id === dialog.step.id);
  const prev = steps.value[index - 1];
  const next = steps.value[index + 1];
  const value = new Date(form.deadline);
  if (prev && value <= new Date(prev.deadline)) return `需晚于上一节点「${prev.name}」的截止时间`;
  if (next && value >= new Date(next.deadline)) return `需早于下一节点「${next.name}」的截止时间`;
  return "";
});
async function save() {
  if (orderWarning.value) {
    ElMessage.warning(orderWarning.value);
    return;
  }
  saving.value = true;
  try {
    await saveSystemRecord(
      "deadline",
      dialog.step.id,
      { name: dialog.step.name, deadline: form.deadline, plagiarismThreshold: form.plagiarismThreshold },
      null,
    );
    ElMessage.success(`「${dialog.step.name}」已更新`);
    dialog.visible = false;
    await refresh();
  } finally {
    saving.value = false;
  }
}
</script>

<template>
  <div>
    <PageHeader title="流程节点" description="设置各阶段截止时间，系统据此提醒学生与教师并统计逾期" />

    <section v-loading="loading" class="panel gd-flow__overview">
      <el-steps :active="activeIndex" finish-status="success" align-center>
        <el-step
          v-for="s in steps"
          :key="s.id"
          :title="s.name"
          :description="dateText(s.deadline).slice(0, 10)"
        />
      </el-steps>
    </section>

    <section class="panel">
      <el-table :data="steps" header-cell-class-name="gd-th">
        <el-table-column prop="sort_order" label="顺序" width="70" align="center" />
        <el-table-column prop="name" label="流程节点" min-width="130">
          <template #default="{ row }"
            ><b>{{ row.name }}</b></template
          >
        </el-table-column>
        <el-table-column prop="code" label="节点标识" width="120">
          <template #default="{ row }"
            ><code class="gd-code">{{ row.code }}</code></template
          >
        </el-table-column>
        <el-table-column label="截止时间" min-width="180">
          <template #default="{ row }">{{ dateText(row.deadline) }}</template>
        </el-table-column>
        <el-table-column label="剩余" width="110">
          <template #default="{ row }">
            <span v-if="daysLeft(row) >= 0" :class="{ warn: daysLeft(row) <= 7 }" class="gd-flow__days">
              {{ daysLeft(row) }} 天
            </span>
            <span v-else class="subtle">—</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ $index }">
            <el-tag :type="stepState($index).type" effect="light" round>{{ stepState($index).text }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="查重阈值" width="100">
          <template #default="{ row }">
            <span v-if="row.code === 'final'">{{ row.plagiarism_threshold }}%</span>
            <span v-else class="subtle">—</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="100">
          <template #default="{ row }">
            <el-button v-permission="'system:manage'" link type="primary" @click="open(row)">配置</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog
      v-model="dialog.visible"
      :title="`配置节点 · ${dialog.step?.name}`"
      width="480px"
      destroy-on-close
    >
      <el-form label-position="top">
        <el-form-item label="截止时间">
          <el-date-picker
            v-model="form.deadline"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            format="YYYY-MM-DD HH:mm"
            style="width: 100%"
          />
          <div v-if="orderWarning" class="gd-flow__warn">{{ orderWarning }}</div>
        </el-form-item>
        <el-form-item v-if="dialog.step?.code === 'final'" label="论文定稿查重阈值（%）">
          <el-slider v-model="form.plagiarismThreshold" :min="5" :max="50" :step="1" show-input />
          <div class="subtle">重复率超过该值的论文定稿不能审核通过</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" :disabled="!!orderWarning" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.gd-flow__overview {
  padding: 28px 16px 20px;
}
.gd-flow__overview :deep(.el-step__title) {
  font-size: 13px;
}
.gd-flow__days {
  color: #175c4b;
  font-weight: 600;
}
.gd-flow__days.warn {
  color: #c4862c;
}
.gd-flow__warn {
  margin-top: 6px;
  color: #d9634f;
  font-size: 12px;
}
.gd-code {
  padding: 2px 6px;
  border-radius: 4px;
  background: #eef3f1;
  color: #4f665d;
  font-size: 12px;
}
:deep(.gd-th) {
  background: #f5f8f7 !important;
  color: #4f665d;
}
</style>
