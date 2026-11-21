import http from "./http";
export const getDashboard = () => http.get("/dashboard");
export const getStatistics = (params) => http.get("/statistics", { params });
export const listArchives = (params) => http.get("/archives", { params });
export const createArchive = (body) => http.post("/archives", body);
