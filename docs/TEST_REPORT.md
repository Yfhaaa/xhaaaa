# 测试报告

测试日期：2026-11-22。Java 11、Node.js 22、MySQL 8.0.43、真实 Chrome 无头浏览器。

## 结果

| 验证项 | 方法 | 结果 |
| --- | --- | --- |
| Maven package | JDK 11 编译、Spring Boot 打包及集成测试 | 成功；10 项，0 失败、0 错误 |
| Maven Wrapper | 独立副本不使用外部项目工具，下载 Maven 后 package | 成功；10 项集成测试通过 |
| npm run build | Vite 生产构建；独立副本另从 npm ci 开始 | 成功 |
| H2 全链路 | 独立内存 H2、18081 端口、真实 curl | 215/215 |
| H2 统一响应、分页和越权 | 全链路之前执行新增检查 | 151/151 |
| MySQL 全链路 | mysql profile、真实 MySQL 8、18082 端口、同一组 curl 断言 | 215/215 |
| MySQL 统一响应、分页和越权 | 同上，使用真实 MySQL 数据 | 151/151 |
| 前端资源与代理 | 页面路由、全部构建资源哈希、代理登录和权限拦截 | 38/38 |
| 浏览器布局 | 1024、1366、1920；管理员、教师、不同状态学生；全部页面及标签 | 480/480，0 页面脚本/API 错误 |
| 待处理操作 | 独立副本中待确认双选、待审核材料的按钮和实际弹窗 | 24/24 |
| 源码和结构 | 行长、空格缩进、26 表实体与 Mapper、分层、SQL 一致性、Java 版本 | 34/34 |
| 启停 | 仓库根目录 start.bat、stop.bat、重启、重复启动复用 PID | 通过；独立副本也启动和停止成功 |

## 业务和权限验证

保留原 215 项 curl 业务断言，覆盖 7 个演示账号、课题申报审核、双向选题、任务书、
开题退回后重提、材料历史版本、中检、初稿、定稿、指导、答辩、加权成绩、成绩发布和完整归档。
同时验证匿名请求、学生与跨教师越权、文件访问、截止时间、分数精度、名额竞争、停用和退出后的令牌失效。

新增 151 项检查验证统一 Result 结构、分页 total 与范围、伪造参数、非法页码、管理接口越权、
文件隔离、统计参数校验与 AOP 日志。两套数据库都先检查初始演示数据，再执行完整业务闭环。

真实 MySQL 初始化创建 utf8mb4 的 gdpm 库并执行 sql/init.sql，26 张业务表导入成功。
初始化 SQL 与 H2 的结构、演示数据通过令牌级一致性检查，无需修改 MySQL 8 语法。
完整回归后直接查询 MySQL，确认新增测试用户与课题已持久化，名额竞争课题实际仅分配 1 名学生。
数据库账号与密码仅从 GDPM_DB_USERNAME、GDPM_DB_PASSWORD 读取，不写入配置、报告或日志。

MySQL 首次回归发现默认可重复读隔离下，确认事务在等待课题行锁后仍可能使用此前建立的快照。
选题申请与确认现使用 READ COMMITTED，并保留学生、课题、申请行锁和事务内名额校验。
修复后重新打包并完整重跑 H2、MySQL 两套回归：并发确认均恰好一个 200、一个 400，数据库人数为 1。

## 页面验证

逐页检查登录、工作台、课题双选、五个材料阶段、指导审核、答辩成绩、统计归档、通知、系统管理。
系统管理还检查用户、角色权限、权限目录、专业/班级/届次、流程节点和操作日志。
在三个宽度下检查 document 的 scrollWidth、固定列位置、按钮边界和点击遮挡，并检查表格内部滚动两端。
宽表格通过表格内部滚动查看；页面本身无横向滚动，操作列固定在右侧。

待处理数据仅在独立验收副本中补充，另验证双选“确认/退回”、材料“下载/详情/通过/退回”
均可点击且未被遮挡，实际审核弹窗位于视口内。部分截图见 screenshots/。

## 启动验证范围

仓库根目录实际执行 start.bat -NoBrowser，完成 package、前端 build、后台服务启动及代理健康检查。
本次已有旧实例占用 8080/5173、旧 JAR 和默认 H2 文件；旧 stop 脚本返回“访问被拒绝”。
因此验收服务使用 18080/15173 和 run/ 下独立 H2 文件，本机设置保存在忽略入库的 local.env.ps1。
仓库脚本默认端口仍为 8080/5173。独立副本使用默认 H2 相对路径，在另外两个端口完成 Wrapper 一键启动，随后停止。
正式演示库未被自动化回归重置。默认端口上的旧实例需要由其启动用户停止。

依赖通过阿里云和 npmmirror 获取，未依赖本地代理，未执行 git init。
构建保留 Vite 大块体积提示，不影响产物和运行。

## 复现与证据

从仓库根目录执行；构建与环境设置详见 README。

```powershell
python -X utf8 scripts/selftest.py
# 需要已导入演示数据且未运行过全链路的独立 gdpm 测试库
python -X utf8 scripts/selftest.py --profile mysql
python -X utf8 scripts/frontend_smoke.py
python -X utf8 scripts/verify_source.py
node scripts/browser_check.mjs
```

备用端口可通过 GDPM_FRONTEND_URL 指定前端验收地址。
初始化工具 scripts/init_mysql.py 在 gdpm 已存在时主动停止，避免覆盖数据库。
待处理浏览器检查 scripts/browser_pending_check.mjs 仅用于独立演示副本，需显式设置 GDPM_TEST_FRONTEND_URL。

本机完整证据位于忽略入库的 run/：start-validation.log、standalone-start.log、
selftest-h2/results.json、selftest-mysql/results.json、各自 pagination-scope/results.json、
frontend-smoke/results.json、browser-check/results.json、browser-pending/results.json、
source-checks.json 和 lifecycle-checks.json。curl 请求/响应临时文件已清理。

后端可执行产物：backend/target/gdpm-system.jar（Java class major 55，即 Java 11）。
SHA-256：`e709784befc73b8d6e09c6d44e998cf26cf6799cf21d9e36a1a782c04fd9db92`。
