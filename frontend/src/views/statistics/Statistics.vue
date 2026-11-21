<script setup>
import * as statisticsApi from "../../api/statistics";
import { getReference } from "../../api/system";
import { ref, reactive, computed, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { stageNames, dateText, exportCsv } from "../../utils/format";
import { useAuth } from "../../stores/auth";
import ChartPanel from "../../components/ChartPanel.vue";
import StatusTag from "../../components/StatusTag.vue";
const auth = useAuth(),
  loading = ref(false),
  data = ref({ groups: [], students: [] }),
  reference = ref({ majors: [], classes: [], teachers: [], cohorts: [] }),
  archives = ref([]),
  manifest = ref(null);
const filters = reactive({ groupBy: "major", majorId: "", classId: "", teacherId: "", cohort: "" });
async function refresh() {
  loading.value = true;
  try {
    [data.value, archives.value, reference.value] = await Promise.all([
      statisticsApi.getStatistics(filters),
      statisticsApi.listArchives(),
      getReference(),
    ]);
  } finally {
    loading.value = false;
  }
}
const option = computed(() => ({
  color: ["#32775d", "#d4b675"],
  tooltip: { trigger: "axis" },
  legend: { bottom: 0, itemWidth: 10, itemHeight: 10, textStyle: { color: "#87998d" } },
  grid: { left: 20, right: 20, top: 15, bottom: 55, containLabel: true },
  xAxis: {
    type: "category",
    data: data.value.groups.map((r) => r.name),
    axisLine: { lineStyle: { color: "#e2e9e2" } },
    axisTick: { show: false },
    axisLabel: { color: "#8a9a8e" },
  },
  yAxis: {
    type: "value",
    max: 100,
    splitLine: { lineStyle: { color: "#eff3ee" } },
    axisLabel: { color: "#93a196" },
  },
  series: [
    {
      name: "平均进度 %",
      type: "bar",
      barWidth: 24,
      data: data.value.groups.map((r) => r.average_progress),
      itemStyle: { borderRadius: [4, 4, 0, 0] },
    },
    {
      name: "平均成绩",
      type: "bar",
      barWidth: 24,
      data: data.value.groups.map((r) => r.average_score),
      itemStyle: { borderRadius: [4, 4, 0, 0] },
    },
  ],
}));
async function archive(row) {
  try {
    await ElMessageBox.confirm(
      "系统将检查全部材料与已发布成绩，并创建归档快照。确认归档 " + row.name + "？",
      "归档项目",
      { type: "info" },
    );
    await statisticsApi.createArchive({ studentId: row.student_id });
    ElMessage.success("归档成功");
    await refresh();
  } catch {}
}
function exportData() {
  exportCsv(
    "毕业设计进度统计.csv",
    [
      { key: "name", label: "学生" },
      { key: "major_name", label: "专业" },
      { key: "class_name", label: "班级" },
      { key: "teacher_name", label: "教师" },
      { key: "cohort", label: "届次" },
      { key: "topic_title", label: "课题" },
      { key: "progress", label: "进度%" },
      { key: "total_score", label: "成绩" },
      { key: "missing_materials", label: "缺失或未通过材料" },
    ],
    data.value.students,
  );
}
onMounted(refresh);
</script>
<template>
  <div v-loading="loading">
    <div class="page-heading">
      <div>
        <h1>统计与归档</h1>
        <p>以过程数据发现问题，以完整材料沉淀成果</p>
      </div>
      <el-button @click="exportData">导出当前统计 CSV</el-button>
    </div>
    <section class="panel">
      <div class="toolbar">
        <div class="actions">
          <el-select
            v-model="filters.groupBy"
            style="width: 140px"
            @change="refresh"
          >
            <el-option
              label="按专业统计"
              value="major"
            />
            <el-option
              label="按班级统计"
              value="class"
            />
            <el-option
              label="按教师统计"
              value="teacher"
            />
          </el-select>
          <el-select
            v-model="filters.majorId"
            placeholder="全部专业"
            clearable
            style="width: 170px"
            @change="refresh"
          >
            <el-option
              v-for="m in reference.majors"
              :key="m.id"
              :label="m.name"
              :value="m.id"
            />
          </el-select>
          <el-select
            v-model="filters.classId"
            placeholder="全部班级"
            clearable
            style="width: 150px"
            @change="refresh"
          >
            <el-option
              v-for="c in reference.classes"
              :key="c.id"
              :label="c.name"
              :value="c.id"
            />
          </el-select>
          <el-select
            v-if="auth.role === 'ADMIN'"
            v-model="filters.teacherId"
            placeholder="全部教师"
            clearable
            style="width: 140px"
            @change="refresh"
          >
            <el-option
              v-for="t in reference.teachers"
              :key="t.id"
              :label="t.name"
              :value="t.id"
            />
          </el-select>
          <el-select
            v-model="filters.cohort"
            placeholder="全部届次"
            clearable
            style="width: 120px"
            @change="refresh"
          >
            <el-option
              v-for="c in reference.cohorts"
              :key="c.id"
              :label="c.name + '届'"
              :value="c.name"
            />
          </el-select>
        </div>
      </div>
      <ChartPanel :option="option" />
      <el-table :data="data.groups">
        <el-table-column
          prop="name"
          label="统计分组"
          min-width="200"
        />
        <el-table-column
          prop="student_count"
          label="学生数"
          min-width="140"
        />
        <el-table-column
          prop="average_progress"
          label="平均进度 %"
          min-width="140"
        />
        <el-table-column
          prop="average_score"
          label="平均成绩"
          min-width="140"
        />
        <el-table-column
          prop="archive_count"
          label="归档数"
          min-width="140"
        />
      </el-table>
    </section>
    <section class="panel">
      <el-tabs>
        <el-tab-pane label="进度与缺失材料">
          <el-table :data="data.students">
            <el-table-column
              prop="name"
              label="学生"
              width="80"
              fixed
            />
            <el-table-column
              prop="teacher_name"
              label="指导教师"
              width="100"
            />
            <el-table-column
              label="进度"
              width="145"
            >
              <template #default="{ row }">
                <el-progress
                  :percentage="row.progress"
                  :stroke-width="5"
                />
              </template>
            </el-table-column>
            <el-table-column
              v-for="(label, key) in stageNames"
              :key="key"
              :label="label"
              width="105"
            >
              <template #default="{ row }">
                <StatusTag :status="row.materials[key].status" />
              </template>
            </el-table-column>
            <el-table-column
              label="归档"
              width="100"
            >
              <template #default="{ row }">
                <el-tag
                  v-if="row.archived"
                  type="success"
                >
                  已归档
                </el-tag>
                <el-button
                  v-else-if="auth.role === 'ADMIN'"
                  link
                  type="primary"
                  @click="archive(row)"
                >
                  检查并归档
                </el-button>
                <span
                  v-else
                  class="subtle"
                >
                  待归档
                </span>
              </template>
            </el-table-column>
          </el-table>
          <div
            class="subtle"
            style="margin-top: 14px"
          >
            待提交、待审核、已退回均列入未完成材料。
            进度依据选题、五类材料、答辩、成绩与归档九个节点计算。
          </div>
        </el-tab-pane>
        <el-tab-pane label="归档查询">
          <el-table :data="archives">
            <el-table-column
              prop="student_name"
              label="学生"
              width="100"
            />
            <el-table-column
              prop="topic_title"
              label="课题"
              min-width="220"
            />
            <el-table-column
              prop="cohort"
              label="届次"
              width="90"
            />
            <el-table-column
              label="归档时间"
              width="180"
            >
              <template #default="{ row }">{{ dateText(row.created_at) }}</template>
            </el-table-column>
            <el-table-column
              label="操作"
              fixed="right"
              width="130"
            >
              <template #default="{ row }">
                <el-button
                  link
                  type="primary"
                  @click="manifest = row"
                >
                  查看归档清单
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </section>
    <el-dialog
      :model-value="!!manifest"
      @update:model-value="
        (v) => {
          if (!v) manifest = null;
        }
      "
      title="归档快照"
      width="700px"
    >
      <p>{{ manifest?.student_name }} · {{ manifest?.topic_title }}</p>
      <pre class="pre-wrap">{{ manifest?.manifest }}</pre>
      <router-link to="/materials">
        <el-button
          type="primary"
          plain
          @click="manifest = null"
        >
          查看与下载过程材料
        </el-button>
      </router-link>
    </el-dialog>
  </div>
</template>
