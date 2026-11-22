<script setup>
/**
 * 用户管理：维护学生、指导教师与管理员账号；支持按角色/专业/届次筛选、启停账号、
 * 重置密码，以及按模板从 Excel 批量导入（逐行校验并反馈结果）。
 */
import { computed, onMounted, reactive, ref, watch } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Download, Plus, Upload, UploadFilled } from "@element-plus/icons-vue";
import { download, post } from "../../api/advanced";
import { getReference, listRoles, listUsers, saveSystemRecord } from "../../api/system";
import { useAuth } from "../../stores/auth";

const auth = useAuth();

const loading = ref(false);
const users = ref([]);
const roles = ref([]);
const reference = ref({ majors: [], classes: [], cohorts: [] });

async function refresh() {
  loading.value = true;
  try {
    const [userRows, roleRows, referenceData] = await Promise.all([listUsers(), listRoles(), getReference()]);
    users.value = userRows;
    roles.value = roleRows;
    reference.value = referenceData;
  } finally {
    loading.value = false;
  }
}
onMounted(refresh);

/* ---------------- 筛选 ---------------- */
const roleFilter = ref("ALL");
const majorFilter = ref(null);
const cohortFilter = ref(null);
const roleTabs = computed(() => [
  { code: "ALL", name: "全部", count: users.value.length },
  ...roles.value.map((r) => ({
    code: r.code,
    name: r.name,
    count: users.value.filter((u) => u.role_code === r.code).length,
  })),
]);
const rows = computed(() =>
  users.value.filter(
    (u) =>
      (roleFilter.value === "ALL" || u.role_code === roleFilter.value) &&
      (!majorFilter.value || u.major_id === majorFilter.value) &&
      (!cohortFilter.value || u.cohort === cohortFilter.value),
  ),
);

/* ---------------- 新增 / 编辑 ---------------- */
const dialog = reactive({ visible: false, id: null });
const saving = ref(false);
const formRef = ref();
const form = reactive({
  username: "",
  name: "",
  password: "",
  roleId: null,
  majorId: null,
  classId: null,
  cohort: "",
  enabled: 1,
});
const classOptions = computed(() => reference.value.classes.filter((c) => c.major_id === form.majorId));
watch(
  () => form.majorId,
  () => {
    if (!classOptions.value.some((c) => c.id === form.classId))
      form.classId = classOptions.value[0]?.id ?? null;
  },
);
const rules = computed(() => ({
  username: [
    { required: true, message: "请填写账号（学号或工号）", trigger: "blur" },
    { pattern: /^[A-Za-z0-9_]{3,30}$/, message: "3–30 位字母、数字或下划线", trigger: "blur" },
  ],
  name: [{ required: true, message: "请填写姓名", trigger: "blur" }],
  roleId: [{ required: true, message: "请选择角色", trigger: "change" }],
  majorId: [{ required: true, message: "请选择专业", trigger: "change" }],
  classId: [{ required: true, message: "请选择班级", trigger: "change" }],
  cohort: [{ required: true, message: "请选择届次", trigger: "change" }],
  password: dialog.id
    ? [{ min: 6, message: "密码至少 6 位", trigger: "blur" }]
    : [
        { required: true, message: "请设置初始密码", trigger: "blur" },
        { min: 6, message: "密码至少 6 位", trigger: "blur" },
      ],
}));

function open(row) {
  Object.assign(dialog, { visible: true, id: row?.id ?? null });
  Object.assign(form, {
    username: row?.username ?? "",
    name: row?.name ?? "",
    password: "",
    roleId: row?.role_id ?? roles.value.find((r) => r.code === "STUDENT")?.id ?? null,
    majorId: row?.major_id ?? reference.value.majors[0]?.id ?? null,
    classId: row?.class_id ?? null,
    cohort: row?.cohort ?? reference.value.cohorts[0]?.name ?? "",
    enabled: row?.enabled ?? 1,
  });
}
async function save() {
  if (!(await formRef.value.validate().catch(() => false))) return;
  saving.value = true;
  try {
    await saveSystemRecord("user", dialog.id, { ...form }, null);
    ElMessage.success(dialog.id ? "用户信息已更新" : "用户已创建");
    dialog.visible = false;
    await refresh();
  } finally {
    saving.value = false;
  }
}

/** 启用 / 停用：停用后该账号无法登录，已登录会话随即失效 */
async function toggleEnabled(row, value) {
  const action = value ? "启用" : "停用";
  try {
    await ElMessageBox.confirm(`确认${action}账号「${row.name}（${row.username}）」？`, `${action}账号`, {
      type: value ? "info" : "warning",
    });
  } catch {
    return;
  }
  await saveSystemRecord(
    "user",
    row.id,
    {
      username: row.username,
      name: row.name,
      password: "",
      roleId: row.role_id,
      majorId: row.major_id,
      classId: row.class_id,
      cohort: row.cohort,
      enabled: value ? 1 : 0,
    },
    null,
  );
  ElMessage.success(`已${action}`);
  await refresh();
}

/* ---------------- 批量导入 ---------------- */
const importDialog = ref(false);
const importing = ref(false);
const importResult = ref(null);
async function importUsers({ file }) {
  importing.value = true;
  try {
    const body = new FormData();
    body.append("file", file);
    importResult.value = await post("/system/users/import", body);
    if (importResult.value.success) await refresh();
  } finally {
    importing.value = false;
  }
}
function openImport() {
  importResult.value = null;
  importDialog.value = true;
}
const importStep = computed(() => (importResult.value ? 2 : 1));
</script>

<template>
  <div>
    <PageHeader title="用户管理" description="账号即学号 / 工号；停用的账号无法登录">
      <el-button v-permission="'system:manage'" :icon="Upload" @click="openImport">批量导入</el-button>
      <el-button v-permission="'system:manage'" type="primary" :icon="Plus" @click="open()"
        >新增用户</el-button
      >
    </PageHeader>

    <section class="panel">
      <el-tabs v-model="roleFilter" class="gd-users__tabs">
        <el-tab-pane v-for="t in roleTabs" :key="t.code" :name="t.code">
          <template #label>
            {{ t.name }} <span class="gd-users__count">{{ t.count }}</span>
          </template>
        </el-tab-pane>
      </el-tabs>
      <DataTable :data="rows" :loading="loading" placeholder="搜索姓名、账号或班级">
        <template #filters>
          <el-select v-model="majorFilter" placeholder="全部专业" clearable style="width: 180px">
            <el-option v-for="m in reference.majors" :key="m.id" :label="m.name" :value="m.id" />
          </el-select>
          <el-select v-model="cohortFilter" placeholder="全部届次" clearable style="width: 120px">
            <el-option v-for="c in reference.cohorts" :key="c.id" :label="`${c.name}届`" :value="c.name" />
          </el-select>
        </template>
        <el-table-column label="用户" min-width="170">
          <template #default="{ row }">
            <div class="gd-users__user">
              <el-avatar :size="32" :class="`role-${row.role_code}`">{{ row.name.slice(0, 1) }}</el-avatar>
              <div>
                <strong>{{ row.name }}</strong>
                <small>{{ row.username }}</small>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="角色" width="110">
          <template #default="{ row }">
            <el-tag
              :type="{ ADMIN: 'danger', TEACHER: 'warning', STUDENT: 'success' }[row.role_code]"
              effect="light"
            >
              {{ row.role_name }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="major_name" label="专业" min-width="160" />
        <el-table-column prop="class_name" label="班级" min-width="120" />
        <el-table-column label="届次" width="90">
          <template #default="{ row }">{{ row.cohort }}届</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              :model-value="!!row.enabled"
              inline-prompt
              active-text="启用"
              inactive-text="停用"
              :disabled="row.id === auth.user?.id"
              style="--el-switch-on-color: #175c4b"
              @change="(v) => toggleEnabled(row, v)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="90">
          <template #default="{ row }">
            <el-button v-permission="'system:manage'" link type="primary" @click="open(row)">编辑</el-button>
          </template>
        </el-table-column>
      </DataTable>
    </section>

    <!-- 新增 / 编辑 -->
    <el-dialog
      v-model="dialog.visible"
      :title="dialog.id ? '编辑用户' : '新增用户'"
      width="620px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="账号（学号 / 工号）" prop="username">
              <el-input v-model="form.username" :disabled="!!dialog.id" maxlength="30" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" maxlength="40" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="角色" prop="roleId">
              <el-select v-model="form.roleId" style="width: 100%">
                <el-option v-for="r in roles" :key="r.id" :value="r.id" :label="r.name" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="dialog.id ? '重置密码（留空则不修改）' : '初始密码'" prop="password">
              <el-input v-model="form.password" type="password" show-password autocomplete="new-password" />
            </el-form-item>
          </el-col>
          <el-col :span="10">
            <el-form-item label="专业" prop="majorId">
              <el-select v-model="form.majorId" style="width: 100%">
                <el-option v-for="m in reference.majors" :key="m.id" :value="m.id" :label="m.name" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="班级" prop="classId">
              <el-select v-model="form.classId" style="width: 100%">
                <el-option v-for="c in classOptions" :key="c.id" :value="c.id" :label="c.name" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="届次" prop="cohort">
              <el-select v-model="form.cohort" style="width: 100%">
                <el-option v-for="c in reference.cohorts" :key="c.id" :value="c.name" :label="c.name" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <!-- 批量导入 -->
    <el-dialog v-model="importDialog" title="批量导入用户" width="640px" destroy-on-close>
      <el-steps :active="importStep" finish-status="success" simple class="gd-users__steps">
        <el-step title="下载模板" />
        <el-step title="上传文件" />
        <el-step title="查看结果" />
      </el-steps>
      <template v-if="!importResult">
        <el-alert type="info" :closable="false" show-icon class="gd-users__tip">
          <template #title>
            请先下载模板，按列填写账号、姓名、角色、专业、班级、届次与初始密码；
            专业、班级、届次须与系统基础数据一致。
          </template>
        </el-alert>
        <el-button :icon="Download" @click="download('/system/users/template', '用户导入模板.xlsx')">
          下载导入模板
        </el-button>
        <el-upload
          drag
          accept=".xlsx"
          :show-file-list="false"
          :http-request="importUsers"
          :disabled="importing"
          class="gd-users__upload"
        >
          <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
          <div class="el-upload__text">
            {{ importing ? "正在导入…" : "将填写好的 .xlsx 拖到此处，或点击选择" }}
          </div>
        </el-upload>
      </template>
      <template v-else>
        <el-result
          :icon="importResult.failed ? 'warning' : 'success'"
          :title="`成功导入 ${importResult.success} 行，失败 ${importResult.failed} 行`"
          :sub-title="importResult.failed ? '失败行未写入，请按提示修改后重新导入' : '全部数据已写入'"
        />
        <el-table v-if="importResult.rows?.length" :data="importResult.rows" max-height="260" size="small">
          <el-table-column prop="row" label="行号" width="80" />
          <el-table-column prop="message" label="结果" />
        </el-table>
      </template>
      <template #footer>
        <el-button v-if="importResult" @click="importResult = null">继续导入</el-button>
        <el-button type="primary" @click="importDialog = false">完成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.gd-users__tabs {
  margin-top: -6px;
}
.gd-users__count {
  margin-left: 4px;
  padding: 0 6px;
  border-radius: 8px;
  background: #eef3f1;
  color: #5b7067;
  font-size: 11px;
}
.gd-users__user {
  display: flex;
  gap: 10px;
  align-items: center;
}
.gd-users__user strong {
  display: block;
  color: #1f3a32;
  font-size: 14px;
}
.gd-users__user small {
  color: #8a9a93;
  font-size: 12px;
}
.gd-users__user .el-avatar {
  font-weight: 600;
}
.role-STUDENT {
  background: #e3eee9;
  color: #175c4b;
}
.role-TEACHER {
  background: #f6eedb;
  color: #a0743a;
}
.role-ADMIN {
  background: #f7e4e1;
  color: #b8554a;
}
.gd-users__steps {
  margin-bottom: 18px;
}
.gd-users__tip {
  margin-bottom: 14px;
}
.gd-users__upload {
  margin-top: 14px;
}
</style>
