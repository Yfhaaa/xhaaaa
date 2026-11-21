import http from "./http";
export const listMaterials = (params) => http.get("/materials", { params });
export const submitMaterial = (stage, body) => http.post(`/materials/${stage}`, body);
export const reviewMaterial = (stage, id, body) => http.post(`/materials/${stage}/${id}/review`, body);
export const uploadFile = (body) => http.post("/files", body);
export async function downloadFile(row) {
  const blob = await http.get(`/files/${row.file_id}`, { responseType: "blob" });
  const url = URL.createObjectURL(blob);
  const anchor = document.createElement("a");
  anchor.href = url;
  anchor.download = row.original_name || "过程材料";
  anchor.click();
  setTimeout(() => URL.revokeObjectURL(url), 1000);
}
