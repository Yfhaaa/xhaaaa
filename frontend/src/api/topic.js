import http from "./http";
export const listTopics = (params) => http.get("/topics", { params });
export const listSelections = (params) => http.get("/selections", { params });
export const saveTopic = (id, body) => (id ? http.put(`/topics/${id}`, body) : http.post("/topics", body));
export const applyTopic = (id) => http.post(`/topics/${id}/apply`);
export const reviewTopic = (id, body) => http.post(`/topics/${id}/review`, body);
export const reviewSelection = (id, body) => http.post(`/selections/${id}/review`, body);
