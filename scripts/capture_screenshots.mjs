// 生成 README 用的系统截图（1440×900），输出到 docs/screenshots/。
// 用法：先启动后端与前端，再执行  node scripts/capture_screenshots.mjs
// 依赖：run/browser-tools 下安装的 playwright；本机 Chrome。
import { createRequire } from "node:module";
import path from "node:path";
import { fileURLToPath } from "node:url";

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const require = createRequire(path.join(root, "run/browser-tools/package.json"));
const { chromium } = require("playwright");

const base = process.env.GDPM_FRONTEND_URL || "http://127.0.0.1:5173";
const out = path.join(root, "docs/screenshots");

const shots = [
  { file: "01-login.png", who: null, path: "/login" },
  { file: "02-dashboard-admin.png", who: "admin", path: "/" },
  { file: "03-topic-cards-student.png", who: "student1", path: "/topics" },
  { file: "04-selection-teacher.png", who: "teacher1", path: "/selections" },
  { file: "05-proposal-student.png", who: "student2", path: "/materials/proposal" },
  { file: "06-material-review-teacher.png", who: "teacher1", path: "/materials/proposal" },
  { file: "07-dossier.png", who: "admin", path: "/students/7" },
  { file: "08-scores-admin.png", who: "admin", path: "/scores" },
  { file: "09-defense-groups.png", who: "admin", path: "/defense/groups" },
  { file: "10-workflow.png", who: "admin", path: "/system/workflow" },
  { file: "11-rbac.png", who: "admin", path: "/system/roles" },
  { file: "12-board.png", who: "admin", path: "/board" },
];

const browser = await chromium.launch({ channel: "chrome" });
const context = await browser.newContext({ viewport: { width: 1440, height: 900 }, locale: "zh-CN" });
const page = await context.newPage();
await page.emulateMedia({ reducedMotion: "reduce" });

for (const shot of shots) {
  await page.goto(`${base}/login`);
  await page.evaluate(() => sessionStorage.clear());
  if (shot.who) {
    await page.evaluate(async (username) => {
      const res = await fetch("/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password: "123456" }),
      });
      sessionStorage.setItem("gdpm-token", (await res.json()).data.token);
    }, shot.who);
  }
  await page.goto(base + shot.path);
  await page.waitForLoadState("networkidle");
  await page.waitForTimeout(1500);
  await page.screenshot({ path: path.join(out, shot.file) });
  console.log("saved", shot.file);
}
await browser.close();
