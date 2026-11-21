import http from "./http";
export const listNotices = (params) => http.get("/notices", { params });
export const createNotice = (body) => http.post("/notices", body);
export const removeNotice = (id) => http.delete(`/notices/${id}`);
