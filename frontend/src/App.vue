<script setup>
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useAuth } from "./stores/auth";
import {
  House,
  Collection,
  Document,
  ChatDotRound,
  Calendar,
  DataAnalysis,
  Setting,
  Bell,
  SwitchButton,
} from "@element-plus/icons-vue";
const route = useRoute(),
  router = useRouter(),
  auth = useAuth();
const entries = [
  ["/", "工作台", House, "dashboard:read"],
  ["/topics", "课题与双选", Collection, "topic:read"],
  ["/materials", "过程材料", Document, "material:read"],
  ["/guidance", "指导与审核", ChatDotRound, "guidance:read"],
  ["/defense", "答辩与成绩", Calendar, "defense:read"],
  ["/statistics", "统计与归档", DataAnalysis, "stats:read"],
  ["/notices", "通知公告", Bell, null],
  ["/system", "系统管理", Setting, "system:manage"],
];
const menu = computed(() => entries.filter((e) => !e[3] || auth.can(e[3])));
const roleNames = { ADMIN: "院系管理员", TEACHER: "指导教师", STUDENT: "学生" };
async function logout() {
  await auth.logout();
  router.push("/login");
}
</script>
<template>
  <router-view v-if="route.path === '/login'" />
  <div v-else class="app-shell">
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-mark">G</div>
        <div><strong>毕业设计管理</strong><small>GENGDAN · GDPM</small></div>
      </div>
      <div class="side-label">工作空间</div>
      <nav>
        <router-link v-for="item in menu" :key="item[0]" :to="item[0]" :class="{ active: route.path === item[0] }"
          ><el-icon><component :is="item[2]" /></el-icon>{{ item[1] }}</router-link
        >
      </nav>
      <div class="sidebar-footer">
        <span class="live-dot"></span>全过程协同 · 2026届
        <div>北京工业大学耿丹学院</div>
      </div>
    </aside>
    <div class="main-shell">
      <header class="topbar">
        <div class="breadcrumb">
          毕业设计 <span>/</span> <strong>{{ route.meta.title }}</strong>
        </div>
        <div class="user-area">
          <span class="role-chip">{{ roleNames[auth.role] }}</span><span>{{ auth.user?.name }}</span
          ><el-button text :icon="SwitchButton" @click="logout">退出</el-button>
        </div>
      </header>
      <main class="page"><router-view :key="route.path" /></main>
      <footer class="page-footer">GDPM · 让每一步过程清晰可见</footer>
    </div>
  </div>
</template>
