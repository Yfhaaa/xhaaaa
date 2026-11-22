<script setup>
/** 根组件：登录页与数据大屏全屏显示，其余页面套用主布局。 */
import { useRoute } from "vue-router";
import AppLayout from "./layout/AppLayout.vue";

const route = useRoute();
</script>

<template>
  <router-view v-if="route.path === '/login' || route.meta.fullscreen" />
  <AppLayout v-else />
</template>
