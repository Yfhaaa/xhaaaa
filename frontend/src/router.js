import { createRouter, createWebHistory } from "vue-router";
import { useAuth } from "./stores/auth";
const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: "/login", component: () => import("./views/system/Login.vue") },
    {
      path: "/",
      component: () => import("./views/statistics/Dashboard.vue"),
      meta: { title: "工作台", permission: "dashboard:read" },
    },
    {
      path: "/topics",
      component: () => import("./views/topic/Topics.vue"),
      meta: { title: "课题与双选", permission: "topic:read" },
    },
    {
      path: "/materials",
      component: () => import("./views/material/Materials.vue"),
      meta: { title: "过程材料", permission: "material:read" },
    },
    {
      path: "/guidance",
      component: () => import("./views/guidance/Guidance.vue"),
      meta: { title: "指导与审核", permission: "guidance:read" },
    },
    {
      path: "/defense",
      component: () => import("./views/defense/Defense.vue"),
      meta: { title: "答辩与成绩", permission: "defense:read" },
    },
    {
      path: "/statistics",
      component: () => import("./views/statistics/Statistics.vue"),
      meta: { title: "统计与归档", permission: "stats:read" },
    },
    { path: "/notices", component: () => import("./views/system/Notices.vue"), meta: { title: "通知公告" } },
    {
      path: "/system",
      component: () => import("./views/system/System.vue"),
      meta: { title: "系统管理", permission: "system:manage" },
    },
    { path: "/:pathMatch(.*)*", redirect: "/" },
  ],
});
router.beforeEach(async (to) => {
  if (to.path === "/login") return true;
  if (!sessionStorage.getItem("gdpm-token")) return "/login";
  const auth = useAuth();
  try {
    if (!auth.user) await auth.load();
    if (to.meta.permission && !auth.can(to.meta.permission)) return "/login";
  } catch {
    return "/login";
  }
});
export default router;
