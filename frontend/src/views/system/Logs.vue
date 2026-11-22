<script setup>
/** 操作日志：审计登录与关键业务操作，可按操作类型、操作人与时间范围筛选。 */
import { computed, onMounted, ref } from "vue";
import { Refresh } from "@element-plus/icons-vue";
import { listLogs } from "../../api/system";
import { dateText } from "../../utils/format";

const loading = ref(false);
const logs = ref([]);
const action = ref("");
const range = ref(null);

async function refresh() {
  loading.value = true;
  try {
    logs.value = await listLogs();
  } finally {
    loading.value = false;
  }
}
onMounted(refresh);

const actions = computed(() => [...new Set(logs.value.map((l) => l.action))]);
const filtered = computed(() =>
  logs.value.filter((l) => {
    if (action.value && l.action !== action.value) return false;
    if (range.value) {
      const t = new Date(l.created_at);
      const [start, end] = range.value;
      if (t < start || t > new Date(end.getTime() + 86400000)) return false;
    }
    return true;
  }),
);
const tagType = (name) => {
  if (name.includes("登录") || name.includes("退出")) return "info";
  if (name.includes("删除") || name.includes("退回")) return "danger";
  if (name.includes("审核") || name.includes("发布")) return "success";
  return "";
};
</script>

<template>
  <div>
    <PageHeader title="操作日志" description="记录最近 1000 条登录与关键业务操作，用于审计追溯">
      <el-button :icon="Refresh" @click="refresh">刷新</el-button>
    </PageHeader>
    <section class="panel">
      <DataTable :data="filtered" :loading="loading" placeholder="搜索操作人或详情" :page-size="20">
        <template #filters>
          <el-select v-model="action" placeholder="全部操作" clearable style="width: 150px">
            <el-option v-for="a in actions" :key="a" :label="a" :value="a" />
          </el-select>
          <el-date-picker
            v-model="range"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 260px"
          />
        </template>
        <el-table-column label="操作时间" width="180">
          <template #default="{ row }">{{ dateText(row.created_at) }}</template>
        </el-table-column>
        <el-table-column prop="user_name" label="操作人" width="130" />
        <el-table-column label="操作类型" width="140">
          <template #default="{ row }">
            <el-tag :type="tagType(row.action)" effect="light">{{ row.action }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="detail" label="详情" min-width="260" show-overflow-tooltip />
      </DataTable>
    </section>
  </div>
</template>
