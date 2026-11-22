<script setup>
import { reactive, ref } from "vue";
import { useAuth } from "../../stores/auth";
import http from "../../api/http";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
const auth = useAuth();
const router = useRouter();
const profileRef = ref();
const passwordRef = ref();
const saving = ref(false);
const form = reactive({ name: auth.user.name, email: auth.user.email || "", phone: auth.user.phone || "" });
const password = reactive({ oldPassword: "", newPassword: "", confirmPassword: "" });
async function saveProfile() {
  if (!(await profileRef.value.validate().catch(() => false))) return;
  saving.value = true;
  try {
    await http.put("/profile", form);
    await auth.load();
    ElMessage.success("资料已更新");
  } finally {
    saving.value = false;
  }
}
async function savePassword() {
  if (!(await passwordRef.value.validate().catch(() => false))) return;
  saving.value = true;
  try {
    await http.post("/profile/password", password);
    sessionStorage.removeItem("gdpm-token");
    auth.user = null;
    ElMessage.success("密码已修改，请重新登录");
    router.push("/login");
  } finally {
    saving.value = false;
  }
}
const strong = (_, value, callback) =>
  /^(?=.*[A-Za-z])(?=.*[0-9]).{8,32}$/.test(value)
    ? callback()
    : callback(new Error("需8至32位，包含字母和数字"));
const confirm = (_, value, callback) =>
  value === password.newPassword ? callback() : callback(new Error("两次密码不一致"));
</script>
<template>
  <div>
    <PageHeader title="个人中心" description="维护个人资料与账号安全" />
    <div class="split equal">
      <section class="panel">
        <h2>
          <el-avatar class="avatar">{{ auth.user?.name?.slice(0, 1) }}</el-avatar> {{ auth.user?.username }}
        </h2>
        <p class="subtle">{{ auth.user?.cohort }}届 · {{ auth.user?.roles.join(" / ") }}</p>
        <el-form ref="profileRef" :model="form" label-position="top"
          ><el-form-item label="姓名" prop="name" :rules="[{ required: true, message: '请填写姓名' }]"
            ><el-input v-model="form.name" maxlength="80"
          /></el-form-item>
          <el-form-item label="邮箱" prop="email" :rules="[{ type: 'email', message: '邮箱格式不正确' }]">
            <el-input v-model="form.email" maxlength="120" /></el-form-item
          ><el-form-item label="联系电话"> <el-input v-model="form.phone" maxlength="30" /></el-form-item>
          <el-button type="primary" :loading="saving" @click="saveProfile">保存资料</el-button></el-form
        >
      </section>
      <section class="panel">
        <h2>修改密码</h2>
        <p class="subtle">密码需8至32位，包含字母和数字；修改后请重新登录。</p>
        <el-form ref="passwordRef" :model="password" label-position="top"
          ><el-form-item
            label="旧密码"
            prop="oldPassword"
            :rules="[{ required: true, message: '请输入旧密码' }]"
            ><el-input v-model="password.oldPassword" type="password" show-password
          /></el-form-item>
          <el-form-item label="新密码" prop="newPassword" :rules="[{ validator: strong }]">
            <el-input v-model="password.newPassword" type="password" show-password
          /></el-form-item>
          <el-form-item label="确认新密码" prop="confirmPassword" :rules="[{ validator: confirm }]">
            <el-input v-model="password.confirmPassword" type="password" show-password
          /></el-form-item>
          <el-button type="primary" :loading="saving" @click="savePassword">修改密码</el-button></el-form
        >
      </section>
    </div>
  </div>
</template>
