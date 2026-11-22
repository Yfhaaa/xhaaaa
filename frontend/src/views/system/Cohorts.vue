<script setup>
/** 届次管理：维护毕业届次（如 2026），用于用户归属与统计筛选。 */
import { onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Plus } from "@element-plus/icons-vue";
import { getReference, removeBase, saveSystemRecord } from "../../api/system";

const loading = ref(false);
const cohorts = ref([]);

async function refresh() {
  loading.value = true;
  try {
    cohorts.value = (await getReference()).cohorts;
  } finally {
    loading.value = false;
  }
}
onMounted(refresh);

const dialog = reactive({ visible: false, id: null });
const saving = ref(false);
const formRef = ref();
const form = reactive({ name: "" });
const rules = {
  name: [
    { required: true, message: "请填写届次", trigger: "blur" },
    { pattern: /^20\d{2}$/, message: "届次为四位年份，如 2026", trigger: "blur" },
  ],
};
function open(row) {
  Object.assign(dialog, { visible: true, id: row?.id ?? null });
  form.name = row?.name ?? String(new Date().getFullYear() + 1);
}
async function save() {
  if (!(await formRef.value.validate().catch(() => false))) return;
  saving.value = true;
  try {
    await saveSystemRecord("base", dialog.id, { ...form }, "cohorts");
    ElMessage.success("已保存");
    dialog.visible = false;
    await refresh();
  } finally {
    saving.value = false;
  }
}
async function remove(row) {
  await ElMessageBox.confirm(`确认删除「${row.name}届」？已有用户归属的届次无法删除。`, "删除确认", {
    type: "warning",
  });
  await removeBase("cohorts", row.id);
  ElMessage.success("已删除");
  await refresh();
}
</script>

<template>
  <div>
    <PageHeader title="届次管理" description="按毕业年份组织学生与统计口径">
      <el-button v-permission="'system:manage'" type="primary" :icon="Plus" @click="open()"
        >新增届次</el-button
      >
    </PageHeader>
    <section class="panel">
      <DataTable :data="cohorts" :loading="loading" placeholder="搜索届次" :searchable="cohorts.length > 10">
        <el-table-column type="index" label="序号" width="80" />
        <el-table-column label="届次" min-width="200">
          <template #default="{ row }"
            ><b>{{ row.name }}</b> 届</template
          >
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="160">
          <template #default="{ row }">
            <el-button v-permission="'system:manage'" link type="primary" @click="open(row)">编辑</el-button>
            <el-button v-permission="'system:manage'" link type="danger" @click="remove(row).catch(() => {})">
              删除
            </el-button>
          </template>
        </el-table-column>
      </DataTable>
    </section>

    <el-dialog
      v-model="dialog.visible"
      :title="dialog.id ? '编辑届次' : '新增届次'"
      width="420px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="届次（毕业年份）" prop="name">
          <el-input v-model="form.name" maxlength="4" placeholder="如：2026" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
