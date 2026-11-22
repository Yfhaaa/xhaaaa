import http from "./http";
export const get = (path, params) => http.get(path, { params });
export const post = (path, body) => http.post(path, body);
export async function download(path, filename, params) {
  const blob = await http.get(path, { params, responseType: "blob" });
  const url = URL.createObjectURL(blob),
    link = document.createElement("a");
  link.href = url;
  link.download = filename;
  link.click();
  setTimeout(() => URL.revokeObjectURL(url), 1000);
}
