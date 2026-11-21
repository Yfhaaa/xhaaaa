export const stageNames = {
  task: "任务书",
  proposal: "开题报告",
  midterm: "中期检查",
  draft: "论文初稿",
  final: "论文定稿",
};
export const dateText = (value) => (value ? new Date(value).toLocaleString("zh-CN", { hour12: false }) : "—");
export function exportCsv(filename, columns, rows) {
  const escape = (v) =>
    '"' +
    String(v ?? "")
      .replaceAll('"', '""')
      .replace(/^[=+@-]/, "'$&") +
    '"';
  const text =
    "\uFEFF" +
    [
      columns.map((c) => escape(c.label)).join(","),
      ...rows.map((r) => columns.map((c) => escape(r[c.key])).join(",")),
    ].join("\r\n");
  const url = URL.createObjectURL(new Blob([text], { type: "text/csv;charset=utf-8" }));
  const a = document.createElement("a");
  a.href = url;
  a.download = filename;
  a.click();
  setTimeout(() => URL.revokeObjectURL(url), 1000);
}
