import { createApp } from "vue";
import { createPinia } from "pinia";
import ElementPlus from "element-plus";
import zhCn from "element-plus/es/locale/lang/zh-cn";
import "element-plus/dist/index.css";
import "./style.css";
import "./theme.css";
import App from "./App.vue";
import router from "./router";
import { watchEffect } from "vue";
import { useAuth } from "./stores/auth";
import DataTable from "./components/DataTable.vue";
import PageHeader from "./components/PageHeader.vue";
import StatusTag from "./components/StatusTag.vue";
import FileUploader from "./components/FileUploader.vue";
import StudentPicker from "./components/StudentPicker.vue";
import ReviewDialog from "./components/ReviewDialog.vue";
const app = createApp(App);
app.use(createPinia()).use(router).use(ElementPlus, { locale: zhCn });
for (const [name, component] of Object.entries({
  DataTable,
  PageHeader,
  StatusTag,
  FileUploader,
  StudentPicker,
  ReviewDialog,
}))
  app.component(name, component);
app.directive("permission", {
  mounted(el, binding) {
    el._permissionStop = watchEffect(() => {
      const values = Array.isArray(binding.value) ? binding.value : [binding.value];
      el.style.display = values.every((value) => useAuth().can(value)) ? "" : "none";
    });
  },
  unmounted(el) {
    el._permissionStop?.();
  },
});
app.mount("#app");
