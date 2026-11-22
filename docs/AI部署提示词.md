# AI 自动部署提示词

把下面整段发给 DeepSeek（或其他能执行终端命令的 AI 编程助手，如 Trae、Cursor、Cline），
它会读取本仓库的部署文档，在当前电脑或服务器上自行完成安装、下载、启动和验证。

---

```text
你是一名运维工程师，请在【当前这台机器】上部署开源项目“毕业设计全过程管理系统（GDPM）”，并确保可以通过浏览器访问。

项目仓库：https://github.com/winfaaa/xhaaaa
部署文档：https://github.com/winfaaa/xhaaaa/blob/main/DEPLOY.md
（若无法访问 github.com，可读取原始文件：https://raw.githubusercontent.com/winfaaa/xhaaaa/main/DEPLOY.md）

要求：
1. 先完整阅读 DEPLOY.md，严格按其中“方式一：下载发布包直接运行”部署；只有发布包下载失败时，才改用“方式二：从源码构建”。
2. 动手前先检测环境：操作系统类型与版本、是否有 java 且版本 ≥ 11、8080 端口是否被占用、能否访问 github.com。把检测结果告诉我。
3. 缺少 Java 时，按 DEPLOY.md 1.1 节对应系统的命令安装 JDK 17；需要管理员权限的命令执行前先告诉我。
4. 默认使用内置 H2 数据库，不要安装 MySQL；除非我明确说要用 MySQL，那时再按 DEPLOY.md 第 3 节操作，数据库密码向我索取，只通过环境变量传入，不要写进任何文件。
5. 如果 8080 被占用，改用 SERVER_PORT=9090（或其他空闲端口），并在后续所有地址中使用新端口。
6. 若是 Linux 服务器且需要长期运行，按 DEPLOY.md 第 4 节配置 systemd 开机自启；Windows 本机试用则直接前台运行即可。
7. 每一步执行后检查输出，出错时先对照 DEPLOY.md 第 7 节“常见问题”自行排查修复，再继续；不要跳过失败的步骤。
8. 部署完成后必须逐项验证并把结果贴给我：
   - curl http://127.0.0.1:端口/api/health 返回 {"status":"UP","application":"gdpm"}
   - 用 admin / 123456 调用 POST /api/auth/login 返回 code 200
   - 浏览器访问 http://127.0.0.1:端口 能看到登录页
9. 最后用中文告诉我：访问地址（本机地址 + 服务器公网/局域网地址）、演示账号、jar 和数据目录所在位置、
   如何停止 / 重启服务，以及提醒我在“系统管理 → 用户管理”中修改默认密码。

如果你不能直接执行命令，请根据我告诉你的操作系统，按上述步骤逐条给出可以复制运行的命令，
每次只给一步，等我把执行结果发给你后再给下一步。
```

---

## 只需要命令清单时

```text
请阅读 https://github.com/winfaaa/xhaaaa/blob/main/DEPLOY.md ，
按“方式一”给出在【Ubuntu 22.04 / Windows 11（改成你的系统）】上部署本系统的完整命令清单，
包括安装 Java、下载 jar、后台运行和验证命令，每条命令附一句中文说明。
```
