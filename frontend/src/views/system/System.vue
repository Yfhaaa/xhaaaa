<script setup>
import * as systemApi from "../../api/system";
import { getReference } from "../../api/system";
import { ref, reactive, computed, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { dateText } from "../../utils/format";
import { useAuth } from "../../stores/auth";
const auth = useAuth(),
  loading = ref(false),
  saving = ref(false),
  users = ref([]),
  roles = ref([]),
  permissions = ref([]),
  workflow = ref([]),
  logs = ref([]),
  reference = ref({ majors: [], classes: [], cohorts: [] }),
  baseType = ref("majors"),
  dialog = ref(""),
  form = reactive({}),
  formRef = ref(),
  search = ref("");
const filtered = computed(() =>
  users.value.filter((u) => (u.name + u.username).includes(search.value)),
);
const classes = computed(() => reference.value.classes.filter((c) => c.major_id === form.majorId));
const baseNames = { majors: "专业", classes: "班级", cohorts: "届次" };
async function refresh() {
  loading.value = true;
  try {
    [users.value, roles.value, permissions.value, workflow.value, logs.value, reference.value] =
      await Promise.all([
        systemApi.listUsers(),
        systemApi.listRoles(),
        systemApi.listPermissions(),
        systemApi.listWorkflow(),
        systemApi.listLogs(),
        getReference(),
      ]);
  } finally {
    loading.value = false;
  }
}
function open(type, row) {
  for (const key of Object.keys(form)) delete form[key];
  Object.assign(form, { id: row?.id || null });
  if (type === "user")
    Object.assign(form, {
      username: row?.username || "",
      name: row?.name || "",
      password: "",
      roleId: row?.role_id || 3,
      majorId: row?.major_id || reference.value.majors[0]?.id,
      classId: row?.class_id || reference.value.classes[0]?.id,
      cohort: row?.cohort || reference.value.cohorts[0]?.name,
      enabled: row?.enabled ?? 1,
    });
  if (type === "role")
    Object.assign(form, { name: row.name, permissionIds: [...row.permission_ids] });
  if (type === "permission") Object.assign(form, { code: row?.code || "", name: row?.name || "" });
  if (type === "base")
    Object.assign(form, {
      name: row?.name || "",
      majorId: row?.major_id || reference.value.majors[0]?.id,
    });
  if (type === "deadline")
    Object.assign(form, {
      name: row.name,
      deadline: new Date(row.deadline).toLocaleString("sv-SE").replace(" ", "T"),
    });
  dialog.value = type;
}
async function save() {
  if (!(await formRef.value.validate().catch(() => false))) return;
  saving.value = true;
  try {
    await systemApi.saveSystemRecord(dialog.value, form.id, form, baseType.value);
    dialog.value = "";
    ElMessage.success("保存成功");
    await auth.load();
    await refresh();
  } finally {
    saving.value = false;
  }
}
async function removeBase(row) {
  try {
    await ElMessageBox.confirm(
      "确认删除 " + row.name + "？被业务引用的数据无法删除。",
      "删除基础数据",
      {
        type: "warning",
      },
    );
    await systemApi.removeBase(baseType.value, row.id);
    await refresh();
  } catch {}
}
const required = (label) => [{ required: true, message: "请填写或选择" + label, trigger: "blur" }];
onMounted(refresh);
</script>
<template>
  <div v-loading="loading">
    <div class="page-heading">
      <div>
        <h1>系统管理</h1>
        <p>维护基础数据、角色权限与毕业设计流程节点</p>
      </div>
      <el-button @click="refresh">刷新数据</el-button>
    </div>
    <section class="panel">
      <el-tabs>
        <el-tab-pane label="用户管理">
          <div class="toolbar">
            <el-input
              v-model="search"
              placeholder="搜索姓名或账号"
              clearable
            />
            <el-button
              type="primary"
              @click="open('user')"
            >
              ＋ 新增用户
            </el-button>
          </div>
          <el-table :data="filtered">
            <el-table-column
              prop="username"
              label="账号"
              width="115"
            />
            <el-table-column
              prop="name"
              label="姓名"
              width="100"
            />
            <el-table-column
              prop="role_name"
              label="角色"
              width="110"
            />
            <el-table-column
              prop="major_name"
              label="专业"
              min-width="160"
            />
            <el-table-column
              prop="class_name"
              label="班级"
              min-width="130"
            />
            <el-table-column
              prop="cohort"
              label="届次"
              width="80"
            />
            <el-table-column
              label="状态"
              width="80"
            >
              <template #default="{ row }">
                <el-tag :type="row.enabled ? 'success' : 'info'">
                  {{ row.enabled ? "启用" : "停用" }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column
              label="操作"
              fixed="right"
              width="100"
            >
              <template #default="{ row }">
                <el-button
                  link
                  type="primary"
                  @click="open('user', row)"
                >
                  编辑
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="角色权限">
          <div class="note-box">
            系统提供学生、指导教师和院系管理员三类角色；
            每类角色的接口权限可单独配置，修改后即时生效。
          </div>
          <el-table :data="roles">
            <el-table-column
              prop="name"
              label="角色"
              min-width="140"
            />
            <el-table-column
              prop="code"
              label="标识"
              min-width="140"
            />
            <el-table-column
              label="权限数"
              min-width="140"
            >
              <template #default="{ row }">{{ row.permission_ids.length }}</template>
            </el-table-column>
            <el-table-column
              label="操作"
              fixed="right"
              min-width="130"
            >
              <template #default="{ row }">
                <el-button
                  link
                  type="primary"
                  @click="open('role', row)"
                >
                  配置权限
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="权限目录">
          <div class="toolbar">
            <span class="subtle">资源权限与接口校验对应</span>
            <el-button
              type="primary"
              @click="open('permission')"
            >
              新增权限项
            </el-button>
          </div>
          <el-table :data="permissions">
            <el-table-column
              prop="id"
              label="编号"
              width="100"
            />
            <el-table-column
              prop="code"
              label="权限标识"
              min-width="140"
            />
            <el-table-column
              prop="name"
              label="权限名称"
              min-width="140"
            />
            <el-table-column
              label="操作"
              fixed="right"
              width="120"
            >
              <template #default="{ row }">
                <el-button
                  link
                  type="primary"
                  @click="open('permission', row)"
                >
                  编辑名称
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="专业 / 班级 / 届次">
          <div class="toolbar">
            <el-radio-group v-model="baseType">
              <el-radio-button
                v-for="(label, key) in baseNames"
                :key="key"
                :value="key"
              >
                {{ label }}
              </el-radio-button>
            </el-radio-group>
            <el-button
              type="primary"
              @click="open('base')"
            >
              新增{{ baseNames[baseType] }}
            </el-button>
          </div>
          <el-table :data="reference[baseType]">
            <el-table-column
              prop="id"
              label="编号"
              width="100"
            />
            <el-table-column
              prop="name"
              :label="baseNames[baseType] + '名称'"
              min-width="140"
            />
            <el-table-column
              v-if="baseType === 'classes'"
              label="所属专业"
              min-width="140"
            >
              <template #default="{ row }">
                {{ reference.majors.find((m) => m.id === row.major_id)?.name }}
              </template>
            </el-table-column>
            <el-table-column
              label="操作"
              fixed="right"
              width="160"
            >
              <template #default="{ row }">
                <el-button
                  link
                  type="primary"
                  @click="open('base', row)"
                >
                  编辑
                </el-button>
                <el-button
                  link
                  type="danger"
                  @click="removeBase(row)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="流程节点">
          <el-table :data="workflow">
            <el-table-column
              prop="sort_order"
              label="顺序"
              width="100"
            />
            <el-table-column
              prop="name"
              label="流程节点"
              min-width="140"
            />
            <el-table-column
              prop="code"
              label="节点标识"
              min-width="140"
            />
            <el-table-column
              label="截止时间"
              min-width="200"
            >
              <template #default="{ row }">{{ dateText(row.deadline) }}</template>
            </el-table-column>
            <el-table-column
              label="操作"
              fixed="right"
              width="120"
            >
              <template #default="{ row }">
                <el-button
                  link
                  type="primary"
                  @click="open('deadline', row)"
                >
                  配置截止时间
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="操作日志">
          <div
            class="subtle"
            style="margin-bottom: 14px"
          >
            展示最近1000条业务操作与登录记录
          </div>
          <el-table :data="logs">
            <el-table-column
              prop="user_name"
              label="操作人"
              width="120"
            />
            <el-table-column
              prop="action"
              label="操作"
              width="160"
            />
            <el-table-column
              prop="detail"
              label="业务标识与详情"
              min-width="220"
            />
            <el-table-column
              label="操作时间"
              width="200"
            >
              <template #default="{ row }">{{ dateText(row.created_at) }}</template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </section>
    <el-dialog
      :model-value="!!dialog"
      @update:model-value="
        (v) => {
          if (!v) dialog = '';
        }
      "
      :title="
        {
          user: '维护用户',
          role: '配置角色权限',
          permission: '维护权限项',
          base: '维护基础数据',
          deadline: '配置节点截止时间',
        }[dialog]
      "
      width="580px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        label-position="top"
      >
        <template v-if="dialog === 'user'">
          <el-form-item
            label="账号"
            prop="username"
            :rules="required('账号')"
          >
            <el-input
              v-model="form.username"
              :disabled="!!form.id"
              maxlength="60"
            />
          </el-form-item>
          <div class="split equal">
            <el-form-item
              label="姓名"
              prop="name"
              :rules="required('姓名')"
            >
              <el-input
                v-model="form.name"
                maxlength="80"
              />
            </el-form-item>
            <el-form-item
              label="角色"
              prop="roleId"
              :rules="required('角色')"
            >
              <el-select v-model="form.roleId">
                <el-option
                  v-for="r in roles"
                  :key="r.id"
                  :value="r.id"
                  :label="r.name"
                />
              </el-select>
            </el-form-item>
          </div>
          <el-form-item
            :label="form.id ? '重置密码（留空保留原密码）' : '初始密码'"
            prop="password"
            :rules="form.id ? [] : required('密码')"
          >
            <el-input
              v-model="form.password"
              type="password"
              show-password
              autocomplete="new-password"
            />
          </el-form-item>
          <div class="split equal">
            <el-form-item
              label="专业"
              prop="majorId"
              :rules="required('专业')"
            >
              <el-select
                v-model="form.majorId"
                @change="form.classId = classes[0]?.id"
              >
                <el-option
                  v-for="m in reference.majors"
                  :key="m.id"
                  :value="m.id"
                  :label="m.name"
                />
              </el-select>
            </el-form-item>
            <el-form-item
              label="班级"
              prop="classId"
              :rules="required('班级')"
            >
              <el-select v-model="form.classId">
                <el-option
                  v-for="c in classes"
                  :key="c.id"
                  :value="c.id"
                  :label="c.name"
                />
              </el-select>
            </el-form-item>
          </div>
          <el-form-item
            label="届次"
            prop="cohort"
            :rules="required('届次')"
          >
            <el-select v-model="form.cohort">
              <el-option
                v-for="c in reference.cohorts"
                :key="c.id"
                :value="c.name"
                :label="c.name + '届'"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="启用账号">
            <el-switch
              v-model="form.enabled"
              :active-value="1"
              :inactive-value="0"
            />
          </el-form-item>
        </template>
        <template v-else-if="dialog === 'role'">
          <el-form-item
            label="角色名称"
            prop="name"
            :rules="required('角色名称')"
          >
            <el-input
              v-model="form.name"
              maxlength="60"
            />
          </el-form-item>
          <el-form-item label="授权权限">
            <el-checkbox-group v-model="form.permissionIds">
              <el-checkbox
                v-for="p in permissions"
                :key="p.id"
                :value="p.id"
                style="display: flex; margin: 0 0 10px"
              >
                {{ p.name }}
                <span class="subtle">· {{ p.code }}</span>
              </el-checkbox>
            </el-checkbox-group>
          </el-form-item>
        </template>
        <template v-else-if="dialog === 'permission'">
          <el-form-item
            label="权限标识（资源:操作）"
            prop="code"
            :rules="required('权限标识')"
          >
            <el-input
              v-model="form.code"
              :disabled="!!form.id"
              maxlength="80"
            />
          </el-form-item>
          <el-form-item
            label="权限名称"
            prop="name"
            :rules="required('权限名称')"
          >
            <el-input
              v-model="form.name"
              maxlength="80"
            />
          </el-form-item>
        </template>
        <template v-else-if="dialog === 'base'">
          <el-form-item
            :label="baseNames[baseType] + '名称'"
            prop="name"
            :rules="required('名称')"
          >
            <el-input
              v-model="form.name"
              maxlength="100"
            />
          </el-form-item>
          <el-form-item
            v-if="baseType === 'classes'"
            label="所属专业"
            prop="majorId"
            :rules="required('所属专业')"
          >
            <el-select v-model="form.majorId">
              <el-option
                v-for="m in reference.majors"
                :key="m.id"
                :label="m.name"
                :value="m.id"
              />
            </el-select>
          </el-form-item>
        </template>
        <template v-else-if="dialog === 'deadline'">
          <div class="note-box">{{ form.name }}：提交接口会校验此截止时间。</div>
          <el-form-item
            label="截止时间"
            prop="deadline"
            :rules="required('截止时间')"
          >
            <el-date-picker
              v-model="form.deadline"
              type="datetime"
              value-format="YYYY-MM-DDTHH:mm:ss"
            />
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="dialog = ''">取消</el-button>
        <el-button
          type="primary"
          :loading="saving"
          @click="save"
        >
          保存设置
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>
