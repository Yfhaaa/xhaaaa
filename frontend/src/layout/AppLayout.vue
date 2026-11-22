<script setup>
/**
 * 系统主布局：侧边导航（按角色与权限生成二级菜单）、顶栏（面包屑、消息、用户菜单）、
 * 多页签导航与内容区。登录页和数据大屏不使用本布局。
 */
import { computed, onBeforeUnmount, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessageBox } from "element-plus";
import {
  Bell,
  Collection,
  DataAnalysis,
  Expand,
  Files,
  Fold,
  HomeFilled,
  Medal,
  Notebook,
  Notification,
  Reading,
  Setting,
  SwitchButton,
  User,
  ChatDotRound,
  Close,
} from "@element-plus/icons-vue";
import { useAuth } from "../stores/auth";
import { pages } from "../navigation";
import { get, post } from "../api/advanced";

const route = useRoute();
const router = useRouter();
const auth = useAuth();

/** 菜单分组图标与排序 */
const GROUP_META = {
  工作台: { icon: HomeFilled, order: 1 },
  我的毕设: { icon: Notebook, order: 2 },
  通知公告: { icon: Notification, order: 9 },
  课题管理: { icon: Collection, order: 3 },
  过程管理: { icon: Files, order: 4 },
  指导审核: { icon: Reading, order: 5 },
  评阅答辩: { icon: Medal, order: 6 },
  统计归档: { icon: DataAnalysis, order: 7 },
  系统管理: { icon: Setting, order: 8 },
};
const ROLE_NAMES = { ADMIN: "院系管理员", TEACHER: "指导教师", STUDENT: "学生" };
const SINGLE_GROUPS = ["工作台", "我的毕设", "通知公告"];

const collapsed = ref(localStorage.getItem("gdpm-collapsed") === "true");
const messages = ref([]);
const tabs = ref([]);

/** 当前用户可见的菜单：过滤角色与权限码后按分组聚合 */
const menu = computed(() => {
  const groups = new Map();
  for (const page of pages) {
    if (page.group === "个人空间") continue;
    if (page.permission && !auth.can(page.permission)) continue;
    if (page.roles.length && !page.roles.includes(auth.role)) continue;
    if (!groups.has(page.group)) groups.set(page.group, []);
    groups.get(page.group).push(page);
  }
  return [...groups.entries()]
    .map(([name, items]) => ({ name, items, ...(GROUP_META[name] || { icon: Files, order: 99 }) }))
    .sort((a, b) => a.order - b.order);
});

const unreadCount = computed(() => messages.value.filter((m) => !m.read_flag).length);

/** 面包屑：一级菜单（工作台等）只显示一级，避免“工作台 / 工作台”重复 */
const crumbs = computed(() => {
  const { group, title } = route.meta;
  if (!title) return [];
  if (!group || group === title || SINGLE_GROUPS.includes(group) || group === "个人空间") {
    return [title];
  }
  return [group, title];
});

function toggleCollapse() {
  collapsed.value = !collapsed.value;
  localStorage.setItem("gdpm-collapsed", String(collapsed.value));
}

/* ---------------- 多页签 ---------------- */
function addTab() {
  if (!route.meta.title || route.meta.fullscreen) return;
  const existing = tabs.value.find((t) => t.path === route.path);
  if (existing) existing.title = route.meta.title;
  else tabs.value.push({ path: route.path, title: route.meta.title });
}
function closeTab(path) {
  if (path === "/") return;
  const index = tabs.value.findIndex((t) => t.path === path);
  if (index < 0) return;
  tabs.value.splice(index, 1);
  if (route.path === path) router.push(tabs.value[Math.max(index - 1, 0)]?.path || "/");
}
function handleTabCommand(command, tab) {
  if (command === "refresh") {
    router.replace({ path: tab.path, query: { _t: Date.now() } });
  } else if (command === "close") {
    closeTab(tab.path);
  } else if (command === "others") {
    tabs.value = tabs.value.filter((t) => t.path === "/" || t.path === tab.path);
    if (route.path !== tab.path) router.push(tab.path);
  } else if (command === "all") {
    tabs.value = tabs.value.filter((t) => t.path === "/");
    router.push("/");
  }
}

/* ---------------- 站内消息 ---------------- */
let messageRequest = 0;
async function refreshMessages() {
  const requestId = ++messageRequest;
  const userId = auth.user?.id;
  if (!userId || route.path === "/login") {
    messages.value = [];
    return;
  }
  try {
    const rows = await get("/messages");
    if (requestId === messageRequest && userId === auth.user?.id) messages.value = rows;
  } catch {
    /* 消息拉取失败不打断页面操作 */
  }
}
async function openMessage(message) {
  if (!message.read_flag) await post(`/messages/${message.id}/read`);
  await refreshMessages();
  if (message.path) router.push(message.path);
}
async function readAll() {
  await post("/messages/read-all");
  await refreshMessages();
}

/* ---------------- 用户菜单 ---------------- */
async function handleUserCommand(command) {
  if (command === "profile") router.push("/profile");
  else if (command === "messages") router.push("/messages");
  else if (command === "logout") {
    await ElMessageBox.confirm("确定退出当前账号吗？", "退出登录", {
      type: "warning",
      confirmButtonText: "退出",
      cancelButtonText: "取消",
    });
    messageRequest++;
    messages.value = [];
    tabs.value = [];
    await auth.logout();
    router.push("/login");
  }
}

function formatTime(value) {
  if (!value) return "";
  const date = new Date(value);
  const diff = (Date.now() - date.getTime()) / 60000;
  if (diff < 1) return "刚刚";
  if (diff < 60) return `${Math.floor(diff)} 分钟前`;
  if (diff < 1440) return `${Math.floor(diff / 60)} 小时前`;
  return date.toLocaleDateString("zh-CN");
}

watch(
  () => route.fullPath,
  () => {
    if (route.path === "/login") return;
    if (!tabs.value.length) tabs.value.push({ path: "/", title: "工作台" });
    addTab();
    refreshMessages();
  },
  { immediate: true },
);
window.addEventListener("gdpm-message-read", refreshMessages);
const timer = setInterval(refreshMessages, 30000);
onBeforeUnmount(() => {
  clearInterval(timer);
  window.removeEventListener("gdpm-message-read", refreshMessages);
});
</script>

<template>
  <div class="gd-shell" :class="{ 'is-collapsed': collapsed }">
    <!-- 侧边导航 -->
    <aside class="gd-aside">
      <router-link to="/" class="gd-brand">
        <span class="gd-brand__mark">G</span>
        <span v-show="!collapsed" class="gd-brand__text">
          <strong>毕业设计管理</strong>
          <small>GENGDAN · GDPM</small>
        </span>
      </router-link>

      <el-scrollbar class="gd-aside__scroll">
        <el-menu
          :default-active="route.path"
          :collapse="collapsed"
          :collapse-transition="false"
          :default-openeds="[route.meta.group]"
          router
          unique-opened
          class="gd-menu"
        >
          <template v-for="group in menu" :key="group.name">
            <el-menu-item
              v-if="SINGLE_GROUPS.includes(group.name) && group.items.length === 1"
              :index="group.items[0].path"
            >
              <el-icon><component :is="group.icon" /></el-icon>
              <template #title>{{ group.name }}</template>
            </el-menu-item>
            <el-sub-menu v-else :index="group.name">
              <template #title>
                <el-icon><component :is="group.icon" /></el-icon>
                <span>{{ group.name }}</span>
              </template>
              <el-menu-item v-for="item in group.items" :key="item.path" :index="item.path">
                {{ item.title }}
              </el-menu-item>
            </el-sub-menu>
          </template>
        </el-menu>
      </el-scrollbar>

      <div v-show="!collapsed" class="gd-aside__footer">
        <span class="gd-live-dot" />全过程协同 · 2026 届
        <div>北京工业大学耿丹学院</div>
      </div>
    </aside>

    <div class="gd-main">
      <!-- 顶栏 -->
      <header class="gd-header">
        <div class="gd-header__left">
          <el-tooltip :content="collapsed ? '展开菜单' : '收起菜单'" placement="bottom">
            <el-button text class="gd-icon-btn" @click="toggleCollapse">
              <el-icon :size="18"><component :is="collapsed ? Expand : Fold" /></el-icon>
            </el-button>
          </el-tooltip>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">毕业设计</el-breadcrumb-item>
            <el-breadcrumb-item v-for="(c, i) in crumbs" :key="c + i">{{ c }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="gd-header__right">
          <el-popover placement="bottom-end" :width="380" trigger="click" popper-class="gd-msg-popper">
            <template #reference>
              <el-badge :value="unreadCount" :hidden="!unreadCount" :max="99" class="gd-bell">
                <el-button text class="gd-icon-btn" aria-label="消息通知">
                  <el-icon :size="18"><Bell /></el-icon>
                </el-button>
              </el-badge>
            </template>
            <div class="gd-msg__head">
              <strong>站内消息</strong>
              <el-button v-if="unreadCount" link type="primary" @click="readAll">全部已读</el-button>
            </div>
            <el-empty v-if="!messages.length" description="暂无消息" :image-size="56" />
            <div class="gd-msg__list">
              <div
                v-for="m in messages.slice(0, 6)"
                :key="m.id"
                class="gd-msg__item"
                :class="{ unread: !m.read_flag }"
                @click="openMessage(m)"
              >
                <el-icon class="gd-msg__icon"><ChatDotRound /></el-icon>
                <div class="gd-msg__body">
                  <div class="gd-msg__title">{{ m.title }}</div>
                  <div class="gd-msg__content">{{ m.content }}</div>
                  <div class="gd-msg__time">{{ formatTime(m.created_at) }}</div>
                </div>
              </div>
            </div>
            <div class="gd-msg__foot">
              <el-button link type="primary" @click="router.push('/messages')">进入消息中心 →</el-button>
            </div>
          </el-popover>

          <span class="gd-role">{{ ROLE_NAMES[auth.role] }}</span>

          <el-dropdown trigger="click" @command="handleUserCommand">
            <div class="gd-user">
              <el-avatar :size="32" class="gd-user__avatar">{{ auth.user?.name?.slice(0, 1) }}</el-avatar>
              <span class="gd-user__name">{{ auth.user?.name }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile" :icon="User">个人中心</el-dropdown-item>
                <el-dropdown-item command="messages" :icon="Bell">消息中心</el-dropdown-item>
                <el-dropdown-item command="logout" :icon="SwitchButton" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- 多页签 -->
      <nav class="gd-tabs">
        <el-scrollbar>
          <div class="gd-tabs__inner">
            <el-dropdown
              v-for="tab in tabs"
              :key="tab.path"
              trigger="contextmenu"
              @command="(cmd) => handleTabCommand(cmd, tab)"
            >
              <router-link :to="tab.path" class="gd-tab" :class="{ active: route.path === tab.path }">
                {{ tab.title }}
                <el-icon
                  v-if="tab.path !== '/'"
                  class="gd-tab__close"
                  @click.prevent.stop="closeTab(tab.path)"
                >
                  <Close />
                </el-icon>
              </router-link>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="refresh">刷新页面</el-dropdown-item>
                  <el-dropdown-item command="close" :disabled="tab.path === '/'">关闭</el-dropdown-item>
                  <el-dropdown-item command="others">关闭其他</el-dropdown-item>
                  <el-dropdown-item command="all">关闭全部</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-scrollbar>
      </nav>

      <main class="gd-content">
        <router-view v-slot="{ Component }">
          <transition name="gd-fade" mode="out-in">
            <component :is="Component" :key="route.fullPath" />
          </transition>
        </router-view>
      </main>
      <footer class="gd-footer">北京工业大学耿丹学院 · 毕业设计全过程管理系统 GDPM v1.0</footer>
    </div>
  </div>
</template>

<style scoped>
.gd-shell {
  --aside-w: 232px;
  --aside-bg: #10352c;
  --aside-bg-2: #0c2a23;
  --aside-text: #a9c3b8;
  --aside-active: #e9f3ef;
  --gold: #d8be7b;
  display: flex;
  min-height: 100vh;
  background: #f3f6f5;
}
.gd-shell.is-collapsed {
  --aside-w: 68px;
}

/* 侧栏 */
.gd-aside {
  position: fixed;
  inset: 0 auto 0 0;
  z-index: 20;
  display: flex;
  flex-direction: column;
  width: var(--aside-w);
  background: linear-gradient(180deg, var(--aside-bg) 0%, var(--aside-bg-2) 100%);
  transition: width 0.2s ease;
}
.gd-brand {
  display: flex;
  gap: 12px;
  align-items: center;
  height: 64px;
  padding: 0 16px;
  border-bottom: 1px solid rgb(255 255 255 / 6%);
}
.gd-brand__mark {
  display: grid;
  flex-shrink: 0;
  place-items: center;
  width: 36px;
  height: 36px;
  border-radius: 10px 3px 10px 3px;
  background: linear-gradient(135deg, #e8d29a, var(--gold));
  color: #123e34;
  font:
    700 20px/1 Georgia,
    serif;
  box-shadow: 0 4px 12px rgb(216 190 123 / 25%);
}
.gd-brand__text {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  white-space: nowrap;
}
.gd-brand__text strong {
  color: #fff;
  font-size: 16px;
  letter-spacing: 1px;
}
.gd-brand__text small {
  margin-top: 3px;
  color: #7fa494;
  font-size: 10px;
  letter-spacing: 2px;
}
.gd-aside__scroll {
  flex: 1;
  padding: 10px 0;
}

/* Element Plus 菜单深色定制 */
.gd-menu {
  --el-menu-bg-color: transparent;
  --el-menu-hover-bg-color: rgb(255 255 255 / 6%);
  --el-menu-text-color: var(--aside-text);
  --el-menu-active-color: #fff;
  --el-menu-item-height: 44px;
  --el-menu-sub-item-height: 40px;
  --el-menu-base-level-padding: 18px;
  border-right: none;
}
.gd-menu:not(.el-menu--collapse) {
  width: var(--aside-w);
}
.gd-menu :deep(.el-sub-menu .el-menu) {
  background: rgb(0 0 0 / 14%);
}
.gd-menu :deep(.el-menu-item),
.gd-menu :deep(.el-sub-menu__title) {
  margin: 2px 10px;
  border-radius: 8px;
  font-size: 14px;
}
.gd-menu :deep(.el-sub-menu .el-menu-item) {
  padding-left: 52px !important;
  font-size: 13px;
}
.gd-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, rgb(216 190 123 / 22%), rgb(216 190 123 / 4%));
  color: #fff;
  font-weight: 600;
}
.gd-menu :deep(.el-menu-item.is-active)::before {
  position: absolute;
  top: 10px;
  bottom: 10px;
  left: 0;
  width: 3px;
  border-radius: 3px;
  background: var(--gold);
  content: "";
}
.gd-menu :deep(.el-sub-menu.is-active > .el-sub-menu__title) {
  color: #fff;
}
.gd-menu.el-menu--collapse :deep(.el-menu-item),
.gd-menu.el-menu--collapse :deep(.el-sub-menu__title) {
  margin: 2px 8px;
  padding: 0 !important;
  justify-content: center;
}
.gd-aside__footer {
  padding: 14px 20px 18px;
  border-top: 1px solid rgb(255 255 255 / 6%);
  color: #7fa494;
  font-size: 12px;
}
.gd-aside__footer div {
  margin-top: 4px;
  color: #5d8475;
}
.gd-live-dot {
  display: inline-block;
  width: 7px;
  height: 7px;
  margin-right: 7px;
  border-radius: 50%;
  background: var(--gold);
  box-shadow: 0 0 0 3px rgb(216 190 123 / 20%);
}

/* 主区域 */
.gd-main {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-width: 0;
  margin-left: var(--aside-w);
  transition: margin-left 0.2s ease;
}
.gd-header {
  position: sticky;
  top: 0;
  z-index: 15;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  padding: 0 24px 0 12px;
  border-bottom: 1px solid #e6ece9;
  background: rgb(255 255 255 / 92%);
  backdrop-filter: blur(8px);
}
.gd-header__left,
.gd-header__right {
  display: flex;
  gap: 14px;
  align-items: center;
}
.gd-icon-btn {
  width: 36px;
  height: 36px;
  padding: 0;
  color: #50675e;
}
.gd-bell :deep(.el-badge__content) {
  top: 6px;
  right: 10px;
}
.gd-role {
  padding: 3px 10px;
  border-radius: 20px;
  background: #e8f1ed;
  color: #175c4b;
  font-size: 12px;
  font-weight: 600;
}
.gd-user {
  display: flex;
  gap: 8px;
  align-items: center;
  padding: 4px 8px 4px 4px;
  border-radius: 20px;
  cursor: pointer;
  transition: background 0.15s;
}
.gd-user:hover {
  background: #f0f4f2;
}
.gd-user__avatar {
  background: linear-gradient(135deg, #1d6a56, #123e34);
  color: var(--gold);
  font-weight: 700;
}
.gd-user__name {
  color: #273d37;
  font-size: 14px;
  font-weight: 600;
}

/* 页签 */
.gd-tabs {
  position: sticky;
  top: 64px;
  z-index: 14;
  height: 40px;
  border-bottom: 1px solid #e6ece9;
  background: #fff;
}
.gd-tabs__inner {
  display: flex;
  gap: 6px;
  align-items: center;
  height: 40px;
  padding: 0 16px;
  white-space: nowrap;
}
.gd-tab {
  display: inline-flex;
  gap: 6px;
  align-items: center;
  height: 28px;
  padding: 0 12px;
  border: 1px solid #e1e8e4;
  border-radius: 6px;
  color: #5b7067;
  font-size: 12px;
  transition: all 0.15s;
}
.gd-tab:hover {
  border-color: #b9cfc6;
  color: #175c4b;
}
.gd-tab.active {
  border-color: #175c4b;
  background: #175c4b;
  color: #fff;
}
.gd-tab.active::before {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--gold);
  content: "";
}
.gd-tab__close {
  border-radius: 50%;
  font-size: 11px;
}
.gd-tab__close:hover {
  background: rgb(0 0 0 / 15%);
}

/* 内容 */
.gd-content {
  flex: 1;
  min-width: 0;
  padding: 24px;
}
.gd-footer {
  padding: 16px 24px 22px;
  color: #9caaa2;
  font-size: 12px;
  text-align: center;
}
.gd-fade-enter-active,
.gd-fade-leave-active {
  transition:
    opacity 0.18s ease,
    transform 0.18s ease;
}
.gd-fade-enter-from {
  opacity: 0;
  transform: translateY(6px);
}
.gd-fade-leave-to {
  opacity: 0;
}
@media (prefers-reduced-motion: reduce) {
  .gd-fade-enter-active,
  .gd-fade-leave-active {
    transition: none;
  }
}

/* 消息弹层 */
.gd-msg__head,
.gd-msg__foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 2px 4px 10px;
}
.gd-msg__foot {
  justify-content: center;
  padding: 10px 0 0;
  border-top: 1px solid #eef2f0;
}
.gd-msg__list {
  max-height: 340px;
  overflow-y: auto;
}
.gd-msg__item {
  display: flex;
  gap: 10px;
  padding: 10px 8px;
  border-radius: 8px;
  cursor: pointer;
}
.gd-msg__item:hover {
  background: #f4f7f6;
}
.gd-msg__icon {
  flex-shrink: 0;
  margin-top: 2px;
  color: #9caaa2;
}
.gd-msg__item.unread .gd-msg__icon {
  color: #175c4b;
}
.gd-msg__item.unread .gd-msg__title::after {
  display: inline-block;
  width: 6px;
  height: 6px;
  margin-left: 6px;
  border-radius: 50%;
  background: #e05a4f;
  vertical-align: middle;
  content: "";
}
.gd-msg__body {
  min-width: 0;
}
.gd-msg__title {
  color: #273d37;
  font-size: 13px;
  font-weight: 600;
}
.gd-msg__content {
  display: -webkit-box;
  overflow: hidden;
  margin-top: 3px;
  color: #6c7f77;
  font-size: 12px;
  line-height: 1.5;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}
.gd-msg__time {
  margin-top: 4px;
  color: #a6b3ad;
  font-size: 11px;
}

@media (max-width: 1100px) {
  .gd-user__name,
  .gd-role {
    display: none;
  }
}
</style>
