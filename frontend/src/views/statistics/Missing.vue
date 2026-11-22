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
    <PageHeader title="缺失材料">
      <el-button @click="download('/exports/missing', '缺失材料.xlsx', filters)"
        >导出缺失材料清单</el-button
      ></PageHeader
    >
    <section class="panel">
      <StatisticsFilters :model-value="filters" :reference="reference" @change="refresh" />
      <DataTable :loading="loading" :data="data.students.filter((s) => s.missing_materials.length)"
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
        <el-table-column label="缺失 / 未通过材料" min-width="260"
          ><template #default="{ row }"> {{ row.missing_materials.join("、") }}</template></el-table-column
        ></DataTable
      >
    </section>
  </div>
</template>
