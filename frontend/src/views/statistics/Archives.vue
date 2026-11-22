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
    <PageHeader title="归档管理" description="最终材料、成绩单与清单，一键完整打包">
      <div v-if="auth.role === 'ADMIN'" class="actions">
        <el-select v-model="classId" placeholder="选择班级" style="width: 180px">
          <el-option v-for="c in reference.classes" :key="c.id" :value="c.id" :label="c.name"
        /></el-select>
        <el-button :disabled="!classId" @click="download('/archives/download', '班级归档.zip', { classId })"
          >按班级打包</el-button
        >
      </div>
    </PageHeader>
    <section class="panel">
      <StatisticsFilters :model-value="filters" :reference="reference" @change="refresh" />
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
        <el-table-column label="归档" width="110"
          ><template #default="{ row }">{{ row.archived ? "已归档" : "待归档" }}</template></el-table-column
        >
        <el-table-column label="操作" fixed="right" width="180"
          ><template #default="{ row }">
            <el-button
              v-if="!row.archived && auth.role === 'ADMIN'"
              v-permission="'archive:write'"
              link
              @click="archive(row)"
              >检查并归档</el-button
            >
            <el-button
              v-if="row.archived"
              link
              type="primary"
              @click="download('/archives/download', '学生归档.zip', { studentId: row.student_id })"
              >下载ZIP</el-button
            >
          </template></el-table-column
        ></DataTable
      >
    </section>
  </div>
</template>
