import http from "./http";
export const listGroups = (params) => http.get("/defense/groups", { params });
export const listDefenses = (params) => http.get("/defenses", { params });
export const listScores = (params) => http.get("/scores", { params });
export const publishScore = (id) => http.post(`/scores/${id}/publish`);
export function saveDefense(kind, body) {
  const paths = { group: "/defense/groups", arrange: "/defenses", score: "/scores" };
  return http.post(paths[kind], body);
}
