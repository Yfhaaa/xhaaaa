import http from "./http";
export const listGuidance = (params) => http.get("/guidance", { params });
export const listReviews = (params) => http.get("/reviews", { params });
export const createGuidance = (body) => http.post("/guidance", body);
