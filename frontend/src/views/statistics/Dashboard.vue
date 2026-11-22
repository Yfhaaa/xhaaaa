<script setup>
import * as statisticsApi from "../../api/statistics";
import { ref, computed, onMounted } from "vue";
import { dateText } from "../../utils/format";
import { useAuth } from "../../stores/auth";
import ChartPanel from "../../components/ChartPanel.vue";
import { User, Bell, TrendCharts, FolderChecked } from "@element-plus/icons-vue";
const auth = useAuth();
const data = ref(null);
const loading = ref(true);
onMounted(async () => {
  try {
    data.value = await statisticsApi.getDashboard();
  } finally {
    loading.value = false;
  }
});
const option = computed(() => ({
  color: ["#2b7159", "#d9bc78"],
  tooltip: { trigger: "axis" },
  grid: { left: 20, right: 20, top: 25, bottom: 30, containLabel: true },
  xAxis: {
    type: "category",
    data: data.value?.progress.map((r) => r.name) || [],
    axisLine: { lineStyle: { color: "#dfe7e0" } },
    axisTick: { show: false },
    axisLabel: { color: "#8c9a90" },
  },
  yAxis: {
    type: "value",
    max: 100,
    splitLine: { lineStyle: { color: "#eff3ee" } },
    axisLabel: { color: "#9aaba0" },
  },
  series: [
    {
      name: "完成进度 %",
      type: "bar",
      barWidth: 22,
      itemStyle: { borderRadius: [4, 4, 0, 0] },
      data: data.value?.progress.map((r) => r.progress) || [],
    },
  ],
}));
const date = new Date().toLocaleDateString("zh-CN", {
  year: "numeric",
  month: "long",
  day: "numeric",
  weekday: "long",
});
const deadlines = computed(() =>
  (data.value?.deadlines || []).map((d) => ({
    ...d,
    days: Math.ceil((new Date(d.deadline) - Date.now()) / 86400000),
  })),
);
</script>
<template>
  <div v-loading="loading">
    <div class="page-heading">
      <div>
        <h1>工作台</h1>
        <p>{{ date }} · 每一步进展，都值得认真记录</p>
      </div>
      <el-tag effect="plain" type="success"> 2026届毕业设计 </el-tag>
    </div>
    <section class="hero">
      <div>
        <div class="eyebrow">YOUR GRADUATION JOURNEY</div>
        <h2>{{ auth.user?.name }}，欢迎回到工作空间</h2>
        <p>当前有 {{ data?.todoCount || 0 }} 项待办，继续推进毕业设计工作。</p>
      </div>
      <div class="hero-badge">
        {{ data?.averageProgress || 0 }}
        <small>平均完成进度 %</small>
      </div>
    </section>
    <template v-if="data">
      <div class="metric-grid">
        <div class="metric">
          <el-icon class="metric-icon"><User /></el-icon>
          <small>{{ auth.role === "STUDENT" ? "我的项目" : "管理学生" }}</small>
          <strong>{{ data.studentCount }}</strong>
          <span>当前权限范围内</span>
        </div>
        <div class="metric">
          <el-icon class="metric-icon"><Bell /></el-icon>
          <small>待办事项</small>
          <strong>{{ data.todoCount }}</strong>
          <span>待处理与待完善</span>
        </div>
        <div class="metric">
          <el-icon class="metric-icon"><TrendCharts /></el-icon>
          <small>平均进度</small>
          <strong>
            {{ data.averageProgress }}
            <small>%</small>
          </strong>
          <span>覆盖九个关键节点</span>
        </div>
        <div class="metric">
          <el-icon class="metric-icon"><FolderChecked /></el-icon>
          <small>完成归档</small>
          <strong>{{ data.archiveCount }}</strong>
          <span>材料完整且成绩发布</span>
        </div>
      </div>
      <div class="split">
        <section class="panel">
          <div class="panel-title">
            <h2>过程进度概览</h2>
            <router-link to="/statistics" class="subtle"> 查看统计 ↗ </router-link>
          </div>
          <ChartPanel :option="option" />
        </section>
        <section class="panel">
          <div class="panel-title">
            <h2>我的待办</h2>
            <span class="subtle">{{ data.todoCount }} 项</span>
          </div>
          <router-link v-for="(todo, i) in data.todos.slice(0, 5)" :key="i" :to="todo.path" class="todo-row">
            <span>
              <i class="todo-dot"></i>
              {{ todo.title }}
            </span>
            <span class="subtle">↗</span>
          </router-link>
          <el-empty v-if="!data.todos.length" description="当前暂无待办" :image-size="70" />
        </section>
      </div>
      <div class="split">
        <section class="panel">
          <div class="panel-title">
            <h2>学生流程跟踪</h2>
            <span class="subtle">选题 → 材料 → 答辩 → 归档</span>
          </div>
          <DataTable :loading="loading" :data="data.progress">
            <el-table-column prop="name" label="学生" width="85" />
            <el-table-column prop="topic_title" label="课题" min-width="160" show-overflow-tooltip />
            <el-table-column label="进度" min-width="150">
              <template #default="{ row }">
                <el-progress :percentage="row.progress" :stroke-width="6" />
              </template>
            </el-table-column>
            <el-table-column label="当前状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.archived ? 'success' : 'info'">
                  {{ row.archived ? "已归档" : row.topic_id ? "进行中" : "待选题" }}
                </el-tag>
              </template>
            </el-table-column>
          </DataTable>
        </section>
        <section class="panel">
          <div class="panel-title">
            <h2>节点截止提醒</h2>
            <span class="subtle">流程日历</span>
          </div>
          <div v-for="d in deadlines" :key="d.id" class="date-row">
            <span>
              {{ d.name }}
              <el-tag v-if="d.days <= 7" :type="d.days < 0 ? 'danger' : 'warning'" size="small">
                {{ d.days < 0 ? "已截止" : "即将截止" }}
              </el-tag>
            </span>
            <span>{{ dateText(d.deadline).split(" ")[0] }}</span>
          </div>
        </section>
      </div>
      <section class="panel">
        <div class="panel-title">
          <h2>通知公告</h2>
          <router-link to="/notices" class="subtle"> 全部通知 ↗ </router-link>
        </div>
        <div v-for="n in data.notices.slice(0, 2)" :key="n.id" class="todo-row">
          <router-link to="/notices">{{ n.title }}</router-link>
          <span class="subtle">{{ dateText(n.created_at) }}</span>
        </div>
      </section>
    </template>
  </div>
</template>
