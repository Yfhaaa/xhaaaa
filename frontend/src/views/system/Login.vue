<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { useAuth } from "../../stores/auth";
const router = useRouter(),
  auth = useAuth(),
  loading = ref(false),
  formRef = ref();
const form = reactive({ username: "", password: "" });
const rules = {
  username: [{ required: true, message: "请输入账号", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }],
};
async function login() {
  if (!(await formRef.value.validate().catch(() => false))) return;
  loading.value = true;
  try {
    await auth.login(form);
    router.push("/");
  } catch {
  } finally {
    loading.value = false;
  }
}
function demo(username) {
  form.username = username;
  form.password = "123456";
}
</script>
<template>
  <div class="login-shell">
    <section class="login-brand">
      <div class="brand">
        <div class="brand-mark">G</div>
        <div>
          <strong>北京工业大学耿丹学院</strong>
          <small>GENGDAN INSTITUTE</small>
        </div>
      </div>
      <div class="login-title">
        <small>GRADUATION DESIGN · 2026</small>
        <h1>
          从课题出发
          <br />
          让成长有迹可循。
        </h1>
        <p>
          连接学生、指导教师与教学管理
          <br />
          让毕业设计的每一步，清晰、协同、有序。
        </p>
      </div>
      <div class="login-foot">全过程管理 · 协同育人 · 质量可追溯</div>
    </section>
    <section class="login-form-area">
      <div class="login-card">
        <div class="row-caption">WELCOME TO GDPM</div>
        <h2>登录工作空间</h2>
        <p>毕业设计全过程管理系统</p>
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          @submit.prevent="login"
        >
          <el-form-item
            label="账号"
            prop="username"
          >
            <el-input
              v-model="form.username"
              placeholder="请输入学号或教师账号"
              autocomplete="username"
            />
          </el-form-item>
          <el-form-item
            label="密码"
            prop="password"
          >
            <el-input
              v-model="form.password"
              type="password"
              show-password
              placeholder="请输入登录密码"
              autocomplete="current-password"
            />
          </el-form-item>
          <el-button
            type="primary"
            native-type="submit"
            class="full-width"
            :loading="loading"
          >
            进入系统 →
          </el-button>
        </el-form>
        <div class="demo-accounts">
          演示空间 · 点击填入账号，密码均为 123456
          <div>
            <el-button
              text
              @click="demo('admin')"
            >
              管理员
            </el-button>
            <el-button
              text
              @click="demo('teacher1')"
            >
              指导教师
            </el-button>
            <el-button
              text
              @click="demo('student1')"
            >
              学生
            </el-button>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>
