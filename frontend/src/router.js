import { createRouter, createWebHistory } from "vue-router";
import { useAuth } from "./stores/auth";
import { pages } from "./navigation";
const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: "/login", component: () => import("./views/system/Login.vue") },
    ...pages.map((page) => ({
      path: page.path,
      component: page.component,
      meta: {
        title: page.title,
        group: page.group,
        permission: page.permission,
        roles: page.roles,
        fullscreen: page.path === "/board",
      },
    })),
    {
      path: "/students/:id",
      component: () => import("./views/statistics/Dossier.vue"),
      meta: {
        title: "学生全过程档案",
        group: "统计归档",
        permission: "material:read",
        roles: ["ADMIN", "TEACHER"],
      },
    },
    { path: "/materials", redirect: "/materials/proposal" },
    { path: "/system", redirect: "/system/users" },
    { path: "/:pathMatch(.*)*", redirect: "/" },
  ],
});
router.beforeEach(async (to) => {
  if (to.path === "/login") return true;
  if (!sessionStorage.getItem("gdpm-token")) return "/login";
  const auth = useAuth();
  try {
    if (!auth.user) await auth.load();
    if (
      (to.meta.permission && !auth.can(to.meta.permission)) ||
      (to.meta.roles?.length && !to.meta.roles.includes(auth.role))
    )
      return to.path === "/" ? "/login" : "/";
  } catch {
    return "/login";
  }
});
export default router;
