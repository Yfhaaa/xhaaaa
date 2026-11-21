<script setup>
import * as notificationApi from "../../api/notification";
import { ref, reactive, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { dateText } from "../../utils/format";
import { useAuth } from "../../stores/auth";
const auth = useAuth(),
  rows = ref([]),
  dialog = ref(false),
  saving = ref(false),
  formRef = ref();
const form = reactive({ title: "", content: "" });
async function refresh() {
  rows.value = await notificationApi.listNotices();
}
async function save() {
  if (!(await formRef.value.validate().catch(() => false))) return;
  saving.value = true;
  try {
    await notificationApi.createNotice(form);
    dialog.value = false;
    ElMessage.success("通知已发布");
    await refresh();
  } finally {
    saving.value = false;
  }
}
async function remove(row) {
  try {
    await ElMessageBox.confirm("确认删除该通知？", "删除通知");
    await notificationApi.removeNotice(row.id);
    await refresh();
  } catch {}
}
function open() {
  Object.assign(form, { title: "", content: "" });
  dialog.value = true;
}
onMounted(refresh);
</script>
<template>
  <div>
    <div class="page-heading">
      <div>
        <h1>通知公告</h1>
        <p>同步毕业设计安排与重要事项</p>
      </div>
      <el-button
        v-if="auth.role === 'ADMIN'"
        type="primary"
        @click="open"
      >
        ＋ 发布通知
      </el-button>
    </div>
    <article
      v-for="row in rows"
      :key="row.id"
      class="panel notice-card"
    >
      <div class="panel-title">
        <h2>{{ row.title }}</h2>
        <el-button
          v-if="auth.role === 'ADMIN'"
          link
          type="danger"
          @click="remove(row)"
        >
          删除
        </el-button>
      </div>
      <p class="pre-wrap">{{ row.content }}</p>
      <span class="subtle">{{ row.publisher_name }} · {{ dateText(row.created_at) }}</span>
    </article>
    <el-empty
      v-if="!rows.length"
      description="暂无通知"
    />
    <el-dialog
      v-model="dialog"
      title="发布通知"
      width="560px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        label-position="top"
      >
        <el-form-item
          label="通知标题"
          prop="title"
          :rules="[{ required: true, message: '请填写通知标题' }]"
        >
          <el-input
            v-model="form.title"
            maxlength="200"
          />
        </el-form-item>
        <el-form-item
          label="通知内容"
          prop="content"
          :rules="[{ required: true, message: '请填写通知内容' }]"
        >
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="7"
            maxlength="10000"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button
          type="primary"
          :loading="saving"
          @click="save"
        >
          发布通知
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>
