<script setup>
/**
 * 角色权限（RBAC）：左侧选择角色，右侧按业务模块勾选权限码并保存；
 * 权限码与后端 @RequiresPermissions 注解一一对应，修改后对已登录用户即时生效。
 */
import { computed, onMounted, reactive, ref, watch } from "vue";
import { ElMessage } from "element-plus";
import { Lock, Plus } from "@element-plus/icons-vue";
import { listPermissions, listRoles, saveSystemRecord } from "../../api/system";
import { useAuth } from "../../stores/auth";

const auth = useAuth();
const loading = ref(false);
const roles = ref([]);
const permissions = ref([]);
const activeRoleId = ref(null);
const checked = ref([]);
const tab = ref("roles");

/** 权限码前缀 → 业务模块 */
const MODULES = [
  { key: "dashboard", name: "工作台" },
  { key: "topic", name: "课题与双选" },
  { key: "material", name: "过程材料" },
  { key: "guidance", name: "指导审核" },
  { key: "defense", name: "评阅答辩" },
  { key: "score", name: "评阅答辩" },
  { key: "stats", name: "统计分析" },
  { key: "archive", name: "统计分析" },
  { key: "system", name: "系统管理" },
];
const ROLE_DESC = {
  ADMIN: "统筹流程配置、课题审核、答辩安排、成绩发布与归档",
  TEACHER: "申报课题、确认双选、指导与审核材料、评阅评分",
  STUDENT: "选题申请、提交各阶段材料、查看反馈与成绩",
};

async function refresh() {
  loading.value = true;
  try {
    const [roleRows, permissionRows] = await Promise.all([listRoles(), listPermissions()]);
    roles.value = roleRows;
    permissions.value = permissionRows;
    if (!roles.value.some((r) => r.id === activeRoleId.value))
      activeRoleId.value = roles.value[0]?.id ?? null;
  } finally {
    loading.value = false;
  }
}
onMounted(refresh);

const activeRole = computed(() => roles.value.find((r) => r.id === activeRoleId.value));
watch(activeRole, (role) => {
  checked.value = role ? [...role.permission_ids] : [];
});

const groups = computed(() => {
  const map = new Map();
  for (const p of permissions.value) {
    const prefix = p.code.split(":")[0];
    const name = MODULES.find((m) => m.key === prefix)?.name || "其他";
    if (!map.has(name)) map.set(name, []);
    map.get(name).push(p);
  }
  return [...map.entries()].map(([name, items]) => ({ name, items }));
});
const dirty = computed(() => {
  const origin = [...(activeRole.value?.permission_ids || [])].sort().join();
  return origin !== [...checked.value].sort().join();
});
/** 管理员不能移除自身角色的系统管理权限，避免把自己锁在系统外 */
const isLockedPermission = (p) => activeRole.value?.code === auth.role && p.code === "system:manage";

function toggleGroup(group, value) {
  const ids = group.items.filter((p) => !isLockedPermission(p)).map((p) => p.id);
  checked.value = value
    ? [...new Set([...checked.value, ...ids])]
    : checked.value.filter((id) => !ids.includes(id));
}
const groupState = (group) => {
  const count = group.items.filter((p) => checked.value.includes(p.id)).length;
  return { all: count === group.items.length, some: count > 0 && count < group.items.length };
};

const saving = ref(false);
async function saveRole() {
  saving.value = true;
  try {
    await saveSystemRecord(
      "role",
      activeRole.value.id,
      { name: activeRole.value.name, permissionIds: checked.value },
      null,
    );
    ElMessage.success(`「${activeRole.value.name}」权限已更新，即时生效`);
    await auth.load();
    await refresh();
  } finally {
    saving.value = false;
  }
}

/* ---------------- 权限项目录 ---------------- */
const dialog = reactive({ visible: false, id: null });
const formRef = ref();
const form = reactive({ code: "", name: "" });
const rules = {
  code: [
    { required: true, message: "请填写权限标识", trigger: "blur" },
    { pattern: /^[a-z]+:[a-z]+$/, message: "格式为 模块:动作，如 topic:read", trigger: "blur" },
  ],
  name: [{ required: true, message: "请填写权限名称", trigger: "blur" }],
};
function openPermission(row) {
  Object.assign(dialog, { visible: true, id: row?.id ?? null });
  Object.assign(form, { code: row?.code ?? "", name: row?.name ?? "" });
}
async function savePermission() {
  if (!(await formRef.value.validate().catch(() => false))) return;
  saving.value = true;
  try {
    await saveSystemRecord("permission", dialog.id, { ...form }, null);
    ElMessage.success("权限项已保存");
    dialog.visible = false;
    await refresh();
  } finally {
    saving.value = false;
  }
}
</script>

<template>
  <div>
    <PageHeader
      title="角色权限"
      description="基于 RBAC 模型：用户 → 角色 → 权限，菜单与接口按权限码双重校验"
    />
    <el-tabs v-model="tab" class="gd-rbac__tabs">
      <el-tab-pane label="角色授权" name="roles">
        <div v-loading="loading" class="gd-rbac">
          <aside class="gd-rbac__roles">
            <div
              v-for="r in roles"
              :key="r.id"
              class="gd-rbac__role"
              :class="{ active: r.id === activeRoleId }"
              @click="activeRoleId = r.id"
            >
              <div class="gd-rbac__role-head">
                <strong>{{ r.name }}</strong>
                <code>{{ r.code }}</code>
              </div>
              <p>{{ ROLE_DESC[r.code] || "自定义角色" }}</p>
              <span>{{ r.permission_ids.length }} / {{ permissions.length }} 项权限</span>
            </div>
          </aside>

          <section v-if="activeRole" class="panel gd-rbac__perms">
            <header>
              <h3>{{ activeRole.name }} · 权限配置</h3>
              <div>
                <el-button :disabled="!dirty" @click="checked = [...activeRole.permission_ids]"
                  >撤销</el-button
                >
                <el-button
                  v-permission="'system:manage'"
                  type="primary"
                  :disabled="!dirty"
                  :loading="saving"
                  @click="saveRole"
                >
                  保存授权
                </el-button>
              </div>
            </header>
            <div v-for="g in groups" :key="g.name" class="gd-rbac__group">
              <el-checkbox
                :model-value="groupState(g).all"
                :indeterminate="groupState(g).some"
                class="gd-rbac__group-title"
                @change="(v) => toggleGroup(g, v)"
              >
                {{ g.name }}
              </el-checkbox>
              <el-checkbox-group v-model="checked" class="gd-rbac__items">
                <el-checkbox
                  v-for="p in g.items"
                  :key="p.id"
                  :value="p.id"
                  :disabled="isLockedPermission(p)"
                  border
                >
                  {{ p.name }}
                  <code>{{ p.code }}</code>
                  <el-icon v-if="isLockedPermission(p)" title="不能移除当前登录角色的系统管理权限"
                    ><Lock
                  /></el-icon>
                </el-checkbox>
              </el-checkbox-group>
            </div>
          </section>
        </div>
      </el-tab-pane>

      <el-tab-pane label="权限项目录" name="permissions">
        <section class="panel">
          <DataTable :data="permissions" :loading="loading" placeholder="搜索权限标识或名称">
            <template #actions>
              <el-button v-permission="'system:manage'" type="primary" :icon="Plus" @click="openPermission()">
                新增权限项
              </el-button>
            </template>
            <el-table-column prop="id" label="编号" width="80" />
            <el-table-column label="权限标识" min-width="180">
              <template #default="{ row }"
                ><code class="gd-code">{{ row.code }}</code></template
              >
            </el-table-column>
            <el-table-column prop="name" label="权限名称" min-width="180" />
            <el-table-column label="已授权角色" min-width="200">
              <template #default="{ row }">
                <el-tag
                  v-for="r in roles.filter((x) => x.permission_ids.includes(row.id))"
                  :key="r.id"
                  size="small"
                  effect="plain"
                  class="gd-rbac__role-tag"
                >
                  {{ r.name }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" fixed="right" width="100">
              <template #default="{ row }">
                <el-button v-permission="'system:manage'" link type="primary" @click="openPermission(row)">
                  编辑
                </el-button>
              </template>
            </el-table-column>
          </DataTable>
        </section>
      </el-tab-pane>
    </el-tabs>

    <el-dialog
      v-model="dialog.visible"
      :title="dialog.id ? '编辑权限项' : '新增权限项'"
      width="460px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="权限标识" prop="code">
          <el-input v-model="form.code" :disabled="!!dialog.id" placeholder="如：topic:read" />
        </el-form-item>
        <el-form-item label="权限名称" prop="name">
          <el-input v-model="form.name" maxlength="50" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="savePermission">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.gd-rbac {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 16px;
  align-items: start;
}
.gd-rbac__roles {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.gd-rbac__role {
  padding: 16px 18px;
  border: 1px solid #e3eae6;
  border-radius: 12px;
  background: #fff;
  cursor: pointer;
  transition: all 0.15s;
}
.gd-rbac__role:hover {
  border-color: #b9cfc6;
}
.gd-rbac__role.active {
  border-color: #175c4b;
  background: linear-gradient(135deg, #f0f7f4, #fff);
  box-shadow: 0 6px 16px rgb(18 62 52 / 10%);
}
.gd-rbac__role-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.gd-rbac__role-head strong {
  color: #1f3a32;
  font-size: 15px;
}
.gd-rbac__role p {
  margin: 8px 0;
  color: #6c7f77;
  font-size: 12px;
  line-height: 1.6;
}
.gd-rbac__role span {
  color: #175c4b;
  font-size: 12px;
  font-weight: 600;
}
code,
.gd-code {
  padding: 1px 6px;
  border-radius: 4px;
  background: #eef3f1;
  color: #4f665d;
  font-family: Consolas, monospace;
  font-size: 11px;
}
.gd-rbac__perms header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.gd-rbac__perms h3 {
  margin: 0;
  color: #1f3a32;
  font-size: 16px;
}
.gd-rbac__group {
  padding: 14px 0;
  border-bottom: 1px dashed #e6ece9;
}
.gd-rbac__group:last-child {
  border-bottom: none;
}
.gd-rbac__group-title {
  margin-bottom: 10px;
  font-weight: 600;
}
.gd-rbac__items {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  padding-left: 24px;
}
.gd-rbac__items .el-checkbox {
  margin-right: 0;
}
.gd-rbac__items code {
  margin-left: 6px;
}
.gd-rbac__role-tag {
  margin-right: 4px;
}
@media (max-width: 1100px) {
  .gd-rbac {
    grid-template-columns: 1fr;
  }
}
</style>
