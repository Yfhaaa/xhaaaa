import http from "./http";
export const listStudents = (params) => http.get("/students", { params });
export const getReference = () => http.get("/reference");
export const listUsers = (params) => http.get("/system/users", { params });
export const listRoles = (params) => http.get("/system/roles", { params });
export const listPermissions = (params) => http.get("/system/permissions", { params });
export const listWorkflow = (params) => http.get("/workflow", { params });
export const listLogs = (params) => http.get("/system/logs", { params });
export const removeBase = (type, id) => http.delete(`/system/base/${type}/${id}`);
export function saveSystemRecord(kind, id, body, baseType) {
  const paths = {
    user: "/system/users",
    role: "/system/roles",
    permission: "/system/permissions",
    base: `/system/base/${baseType}`,
    deadline: "/workflow",
  };
  const path = paths[kind] + (id ? `/${id}` : "");
  return id ? http.put(path, body) : http.post(path, body);
}
