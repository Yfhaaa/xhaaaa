import axios from "axios";
import { ElMessage } from "element-plus";
const api = axios.create({ baseURL: "/api", timeout: 30000 });
api.interceptors.request.use((config) => {
  const token = sessionStorage.getItem("gdpm-token");
  if (token) config.headers.Authorization = "Bearer " + token;
  return config;
});
api.interceptors.response.use(
  (r) => (r.config.responseType === "blob" ? r.data : r.data.data),
  (e) => {
    if (e.response?.status === 401 && location.pathname !== "/login") {
      sessionStorage.removeItem("gdpm-token");
      location.assign("/login");
    }
    ElMessage.error(e.response?.data?.message || "请求失败，请检查服务与网络连接");
    return Promise.reject(e);
  },
);
export default api;
