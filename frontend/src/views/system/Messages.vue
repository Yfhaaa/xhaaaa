<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { get, post } from "../../api/advanced";
const rows = ref([]);
const unread = ref(false);
const loading = ref(true);
const router = useRouter();
async function refresh() {
  loading.value = true;
  try {
    rows.value = await get("/messages", { unread: unread.value });
  } finally {
    loading.value = false;
  }
}
async function read(row) {
  await post(`/messages/${row.id}/read`);
  await refresh();
  window.dispatchEvent(new Event("gdpm-message-read"));
}
async function allRead() {
  await post("/messages/read-all");
  await refresh();
  window.dispatchEvent(new Event("gdpm-message-read"));
}
onMounted(refresh);
</script>
<template>
  <div>
    <PageHeader title="消息中心" description="业务提醒与审核结果，集中查阅">
      <el-button type="primary" @click="allRead">一键已读</el-button></PageHeader
    >
    <section class="panel">
      <el-radio-group v-model="unread" @change="refresh"
        ><el-radio-button :value="false">全部</el-radio-button>
        <el-radio-button :value="true">未读</el-radio-button></el-radio-group
      ><DataTable :data="rows" :loading="loading">
        <el-table-column label="状态" width="90"
          ><template #default="{ row }"
            ><el-tag :type="row.read_flag ? 'info' : 'warning'">
              {{ row.read_flag ? "已读" : "未读" }}</el-tag
            ></template
          ></el-table-column
        >
        <el-table-column prop="title" label="消息标题" min-width="200" /><el-table-column
          prop="content"
          label="内容"
          min-width="250"
        />
        <el-table-column prop="created_at" label="时间" min-width="180" />
        <el-table-column label="操作" fixed="right" width="150"
          ><template #default="{ row }">
            <el-button v-if="!row.read_flag" link @click="read(row)">已读</el-button>
            <el-button
              link
              type="primary"
              @click="
                read(row);
                router.push(row.path);
              "
              >查看</el-button
            ></template
          ></el-table-column
        >
      </DataTable>
    </section>
  </div>
</template>
