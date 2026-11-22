<script setup>
/** 专业与班级：左侧维护专业，右侧维护所选专业下的班级；被业务数据引用的记录不可删除。 */
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Delete, Edit, Plus } from "@element-plus/icons-vue";
import { getReference, removeBase, saveSystemRecord } from "../../api/system";

const loading = ref(false);
const majors = ref([]);
const classes = ref([]);
const activeMajorId = ref(null);

async function refresh() {
  loading.value = true;
  try {
    const reference = await getReference();
    majors.value = reference.majors;
    classes.value = reference.classes;
    if (!majors.value.some((m) => m.id === activeMajorId.value))
      activeMajorId.value = majors.value[0]?.id ?? null;
  } finally {
    loading.value = false;
  }
}
onMounted(refresh);

const activeMajor = computed(() => majors.value.find((m) => m.id === activeMajorId.value));
const majorClasses = computed(() => classes.value.filter((c) => c.major_id === activeMajorId.value));
const classCount = (majorId) => classes.value.filter((c) => c.major_id === majorId).length;

/* ---------------- 新增 / 编辑 ---------------- */
const dialog = reactive({ visible: false, type: "majors", id: null });
const saving = ref(false);
const formRef = ref();
const form = reactive({ name: "", majorId: null });
const rules = { name: [{ required: true, message: "请填写名称", trigger: "blur" }] };

function open(type, row) {
  Object.assign(dialog, { visible: true, type, id: row?.id ?? null });
  Object.assign(form, { name: row?.name ?? "", majorId: row?.major_id ?? activeMajorId.value });
}
async function save() {
  if (!(await formRef.value.validate().catch(() => false))) return;
  saving.value = true;
  try {
    await saveSystemRecord("base", dialog.id, { ...form }, dialog.type);
    ElMessage.success("已保存");
    dialog.visible = false;
    await refresh();
  } finally {
    saving.value = false;
  }
}
async function remove(type, row) {
  await ElMessageBox.confirm(`确认删除「${row.name}」？已被学生或课题引用的数据无法删除。`, "删除确认", {
    type: "warning",
    confirmButtonText: "删除",
    cancelButtonText: "取消",
  });
  await removeBase(type, row.id);
  ElMessage.success("已删除");
  await refresh();
}
</script>

<template>
  <div>
    <PageHeader title="专业班级" description="维护院系专业与行政班级，作为用户、课题与统计的基础数据" />
    <div v-loading="loading" class="gd-base">
      <section class="panel gd-base__majors">
        <header>
          <h3>专业</h3>
          <el-button v-permission="'system:manage'" type="primary" link :icon="Plus" @click="open('majors')">
            新增
          </el-button>
        </header>
        <ul>
          <li
            v-for="m in majors"
            :key="m.id"
            :class="{ active: m.id === activeMajorId }"
            @click="activeMajorId = m.id"
          >
            <span class="gd-base__name">{{ m.name }}</span>
            <el-tag size="small" round effect="plain">{{ classCount(m.id) }} 个班</el-tag>
            <span class="gd-base__ops" @click.stop>
              <el-icon v-permission="'system:manage'" @click="open('majors', m)"><Edit /></el-icon>
              <el-icon
                v-permission="'system:manage'"
                class="danger"
                @click="remove('majors', m).catch(() => {})"
              >
                <Delete />
              </el-icon>
            </span>
          </li>
        </ul>
        <el-empty v-if="!majors.length" description="暂无专业" :image-size="60" />
      </section>

      <section class="panel gd-base__classes">
        <header>
          <h3>{{ activeMajor?.name || "—" }} · 班级</h3>
          <el-button
            v-permission="'system:manage'"
            type="primary"
            :icon="Plus"
            :disabled="!activeMajorId"
            @click="open('classes')"
          >
            新增班级
          </el-button>
        </header>
        <el-table :data="majorClasses" stripe header-cell-class-name="gd-th">
          <el-table-column type="index" label="序号" width="70" />
          <el-table-column prop="name" label="班级名称" min-width="200" />
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button v-permission="'system:manage'" link type="primary" @click="open('classes', row)"
                >编辑</el-button
              >
              <el-button
                v-permission="'system:manage'"
                link
                type="danger"
                @click="remove('classes', row).catch(() => {})"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
          <template #empty><el-empty description="该专业暂无班级" :image-size="70" /></template>
        </el-table>
      </section>
    </div>

    <el-dialog
      v-model="dialog.visible"
      :title="`${dialog.id ? '编辑' : '新增'}${dialog.type === 'majors' ? '专业' : '班级'}`"
      width="460px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item v-if="dialog.type === 'classes'" label="所属专业">
          <el-select v-model="form.majorId" style="width: 100%">
            <el-option v-for="m in majors" :key="m.id" :value="m.id" :label="m.name" />
          </el-select>
        </el-form-item>
        <el-form-item :label="dialog.type === 'majors' ? '专业名称' : '班级名称'" prop="name">
          <el-input
            v-model="form.name"
            maxlength="50"
            :placeholder="dialog.type === 'majors' ? '如：软件工程' : '如：计科22-1班'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.gd-base {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 16px;
  align-items: start;
}
.gd-base header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.gd-base h3 {
  margin: 0;
  color: #1f3a32;
  font-size: 15px;
}
.gd-base__majors ul {
  margin: 0;
  padding: 0;
  list-style: none;
}
.gd-base__majors li {
  display: flex;
  gap: 8px;
  align-items: center;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.15s;
}
.gd-base__majors li:hover {
  background: #f3f7f5;
}
.gd-base__majors li.active {
  background: #e7f1ed;
  color: #175c4b;
  font-weight: 600;
}
.gd-base__name {
  flex: 1;
}
.gd-base__ops {
  display: none;
  gap: 8px;
  color: #7d9088;
}
.gd-base__majors li:hover .gd-base__ops {
  display: inline-flex;
}
.gd-base__ops .danger:hover {
  color: #d9634f;
}
.gd-base__classes :deep(.gd-th) {
  background: #f5f8f7 !important;
  color: #4f665d;
}
@media (max-width: 1100px) {
  .gd-base {
    grid-template-columns: 1fr;
  }
}
</style>
