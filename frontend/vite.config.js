import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
const apiTarget = process.env.GDPM_API_TARGET || "http://127.0.0.1:8080";
export default defineConfig({
  plugins: [vue()],
  server: { proxy: { "/api": { target: apiTarget, changeOrigin: true } } },
  preview: { proxy: { "/api": { target: apiTarget, changeOrigin: true } } },
  build: {
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (id.includes("echarts") || id.includes("zrender")) return "charts";
          if (id.includes("element-plus") || id.includes("@element-plus")) return "element";
          if (id.includes("node_modules")) return "vendor";
        },
      },
    },
  },
});
