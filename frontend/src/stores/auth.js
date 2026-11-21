import { defineStore } from "pinia";
import { ref, computed } from "vue";
import * as authApi from "../api/auth";
export const useAuth = defineStore("auth", () => {
  const user = ref(null);
  const role = computed(() => user.value?.roles?.[0] || "");
  const can = (permission) => user.value?.permissions?.includes(permission);
  async function load() {
    user.value = await authApi.currentUser();
  }
  async function login(form) {
    const data = await authApi.login(form);
    sessionStorage.setItem("gdpm-token", data.token);
    await load();
  }
  async function logout() {
    try {
      await authApi.logout();
    } finally {
      sessionStorage.removeItem("gdpm-token");
      user.value = null;
    }
  }
  return { user, role, can, load, login, logout };
});
