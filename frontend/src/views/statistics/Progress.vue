<script setup>
import { confirmAction } from "../../utils/confirm";
import StatisticsFilters from "../../components/StatisticsFilters.vue";
import { ref, reactive, onMounted } from "vue";
import { get, post, download } from "../../api/advanced";
import { ElMessage } from "element-plus";
import { useAuth } from "../../stores/auth";
const auth = useAuth();
const data = ref({ groups: [], students: [] });
const reference = ref({ classes: [] });
const loading = ref(true);
const classId = ref(null);
const filters = reactive({ groupBy: "major" });
async function refresh() {
  loading.value = true;
  try {
    [data.value, reference.value] = await Promise.all([get("/statistics", filters), get("/reference")]);
  } finally {
    loading.value = false;
  }
}
async function archive(row) {
  if (!(await confirmAction("检查全部材料及成绩并归档 " + row.name + "？", "归档项目"))) return;
  await post("/archives", { studentId: row.student_id });
  ElMessage.success("已归档");
  await refresh();
}
onMounted(refresh);
</script>
<template>
  <div>
    <PageHeader title="进度统计">
      <el-button @click="download('/exports/statistics', '进度统计.xlsx', filters)"
        >导出Excel统计报表</el-button
      ></PageHeader
    >
    <section class="panel">
      <StatisticsFilters :model-value="filters" :reference="reference" @change="refresh" />
      <el-radio-group v-model="filters.groupBy" @change="refresh">
        <el-radio-button value="major">按专业</el-radio-button
        ><el-radio-button value="class">按班级</el-radio-button>
        <el-radio-button value="teacher">按教师</el-radio-button></el-radio-group
      ><DataTable :data="data.groups" :loading="loading">
        <el-table-column prop="name" label="分组" min-width="180" /><el-table-column
          prop="student_count"
          label="学生数" />
        <el-table-column prop="average_progress" label="平均进度%" /><el-table-column
          prop="average_score"
          label="平均成绩" />
        <el-table-column prop="archive_count" label="已归档数"
      /></DataTable>
    </section>
    <section class="panel">
      <h2>学生档案入口</h2>
      <DataTable :data="data.students" :loading="loading"
        ><el-table-column label="学生" width="100"
          ><template #default="{ row }"
            ><router-link :to="'/students/' + row.student_id">
              <el-button link type="primary">{{ row.name }}</el-button></router-link
            ></template
          ></el-table-column
        >
        <el-table-column prop="class_name" label="班级" min-width="150" /><el-table-column
          prop="teacher_name"
          label="指导教师"
          width="110"
        />
        <el-table-column prop="topic_title" label="课题" min-width="220" />
        <el-table-column label="进度" width="160"
          ><template #default="{ row }"><el-progress :percentage="row.progress" /></template
        ></el-table-column>
      </DataTable>
    </section>
  </div>
</template>
