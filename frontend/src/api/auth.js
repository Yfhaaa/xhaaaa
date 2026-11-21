import http from "./http";
export const login = (body) => http.post("/auth/login", body);
export const currentUser = () => http.get("/auth/me");
export const logout = () => http.post("/auth/logout");
