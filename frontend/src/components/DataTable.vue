<script setup>
/**
 * 通用数据表格：关键词检索 + 状态快捷筛选 + 前端分页 + 骨架屏与空状态。
 *
 * 用法：
 *   <DataTable :data="rows" :loading="loading" placeholder="搜索课题" status-field="status">
 *     <template #actions><el-button type="primary">新增</el-button></template>
 *     <el-table-column ... />
 *   </DataTable>
 */
import { computed, ref, watch } from "vue";
import { Search } from "@element-plus/icons-vue";

defineOptions({ inheritAttrs: false });

const props = defineProps({
  data: { type: Array, default: () => [] },
  loading: Boolean,
  /** 搜索框占位文字 */
  placeholder: { type: String, default: "输入关键词检索" },
  /** 按该字段生成状态快捷筛选（如 status），为空则不显示 */
  statusField: { type: String, default: "" },
  /** 是否显示搜索框 */
  searchable: { type: Boolean, default: true },
  pageSize: { type: Number, default: 10 },
});

const keyword = ref("");
const status = ref("全部");
const page = ref(1);
const size = ref(props.pageSize);

/** 状态选项及数量，按数据中出现的顺序排列 */
const statusOptions = computed(() => {
  if (!props.statusField) return [];
  const counts = new Map();
  for (const row of props.data) {
    const value = row[props.statusField] || "待提交";
    counts.set(value, (counts.get(value) || 0) + 1);
  }
  return [
    { label: "全部", count: props.data.length },
    ...[...counts].map(([label, count]) => ({ label, count })),
  ];
});

const rows = computed(() => {
  const text = keyword.value.trim().toLowerCase();
  return props.data.filter((row) => {
    if (status.value !== "全部" && (row[props.statusField] || "待提交") !== status.value) return false;
    if (!text) return true;
    return Object.values(row).some(
      (v) => v != null && typeof v !== "object" && String(v).toLowerCase().includes(text),
    );
  });
});

const visible = computed(() => rows.value.slice((page.value - 1) * size.value, page.value * size.value));

watch([keyword, status, size, () => props.data], () => {
  page.value = 1;
});
watch(
  () => props.data,
  () => {
    if (!statusOptions.value.some((o) => o.label === status.value)) status.value = "全部";
  },
);
</script>

<template>
  <div class="gd-table">
    <div class="gd-table__toolbar">
      <div class="gd-table__filters">
        <el-input
          v-if="searchable"
          v-model="keyword"
          :placeholder="placeholder"
          :prefix-icon="Search"
          clearable
          class="gd-table__search"
        />
        <div v-if="statusOptions.length > 2" class="gd-table__status">
          <button
            v-for="o in statusOptions"
            :key="o.label"
            type="button"
            :class="{ active: status === o.label }"
            @click="status = o.label"
          >
            {{ o.label }}<span>{{ o.count }}</span>
          </button>
        </div>
        <slot name="filters" />
      </div>
      <div class="gd-table__actions">
        <span class="gd-table__count"
          >共 <b>{{ rows.length }}</b> 条</span
        >
        <slot name="actions" />
      </div>
    </div>

    <el-skeleton :loading="loading && !data.length" animated :rows="6">
      <template #default>
        <el-table
          v-loading="loading && data.length > 0"
          v-bind="$attrs"
          :data="visible"
          stripe
          class="gd-table__table"
          header-cell-class-name="gd-table__th"
        >
          <slot />
          <template #empty>
            <el-empty description="暂无符合条件的记录" :image-size="90" />
          </template>
        </el-table>
        <div v-if="rows.length > 10" class="gd-table__pager">
          <el-pagination
            v-model:current-page="page"
            v-model:page-size="size"
            :page-sizes="[10, 20, 50, 100]"
            :total="rows.length"
            background
            layout="total, sizes, prev, pager, next, jumper"
          />
        </div>
      </template>
    </el-skeleton>
  </div>
</template>

<style scoped>
.gd-table__toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}
.gd-table__filters,
.gd-table__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}
.gd-table__search {
  width: 260px;
}
.gd-table__status {
  display: inline-flex;
  padding: 3px;
  border-radius: 8px;
  background: #f0f4f2;
}
.gd-table__status button {
  display: inline-flex;
  gap: 5px;
  align-items: center;
  height: 26px;
  padding: 0 11px;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: #5b7067;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.15s;
}
.gd-table__status button span {
  min-width: 16px;
  padding: 0 4px;
  border-radius: 8px;
  background: rgb(0 0 0 / 6%);
  font-size: 11px;
  text-align: center;
}
.gd-table__status button.active {
  background: #fff;
  color: #175c4b;
  font-weight: 600;
  box-shadow: 0 1px 3px rgb(18 62 52 / 12%);
}
.gd-table__status button.active span {
  background: #175c4b;
  color: #fff;
}
.gd-table__count {
  color: #8a9a93;
  font-size: 12px;
}
.gd-table__count b {
  color: #175c4b;
}
.gd-table__table {
  --el-table-row-hover-bg-color: #f2f7f5;
  border-radius: 8px;
}
.gd-table__table :deep(.gd-table__th) {
  background: #f5f8f7 !important;
  color: #4f665d;
  font-weight: 600;
}
.gd-table__pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
