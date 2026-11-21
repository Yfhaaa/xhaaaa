# 毕业设计全过程管理系统

面向学生、指导教师和院系管理员，覆盖课题申报、双向选题、任务书、开题、中检、论文、指导审核、答辩评分和归档。

## 环境要求

- JDK 11 或更高版本，配置 `JAVA_HOME` 或把 JDK 的 `bin` 加入 PATH。需要完整 JDK（包含 javac）。
- Node.js 18+（包含 npm）。
- Maven 3.6.3+，或者直接使用仓库自带的 Maven Wrapper（自动下载 Maven 3.9.11）。
- Windows 一键启动使用 PowerShell 5.1+；其他系统可按手工步骤运行。
- MySQL 8 可选。默认使用持久化 H2，无需另装数据库。
- 首次构建需要联网。默认 Maven 使用阿里云镜像，npm 使用 npmmirror；启动脚本会清除当前进程代理变量，不修改系统代理。

## H2 一键运行

在仓库根目录双击 `start.bat`，或在 PowerShell 执行：

```powershell
.\start.bat
# 自动验收时可不打开浏览器
.\start.bat -NoBrowser
```

脚本检查 JDK/Node 版本，优先使用 PATH 中的 Maven，其次使用 MAVEN_HOME，最后使用 `backend/mvnw.cmd`。
它会执行后端 package（含测试）、首次 npm ci、前端 build，然后在后台启动服务。

- 系统入口：<http://127.0.0.1:5173>
- 后端健康检查：<http://127.0.0.1:8080/api/health>
- 前端 `/api` 自动代理到后端。关闭启动窗口后服务继续运行。
- `stop.bat` 根据 PID、启动时间和程序路径停止本项目进程。修改代码后先停止再启动。
- 重复启动会复用本脚本记录且健康的服务；端口被其他进程占用时会提示错误。
- 日志、PID、临时文件、npm/Wrapper 缓存均在忽略入库的 `run/` 中。

本机设置可复制 `scripts/local.env.example.ps1` 为 `scripts/local.env.ps1` 后填写。
支持 JAVA_HOME、MAVEN_HOME、GDPM_MAVEN_SETTINGS、镜像，以及 GDPM_BACKEND_PORT/GDPM_FRONTEND_PORT。
数据库密码仅通过进程环境提供，不写入该文件。`local.env.ps1` 已加入 `.gitignore`。

Wrapper 的 Maven 下载地址与 Wrapper JAR 地址可在 `backend/.mvn/wrapper/maven-wrapper.properties`
中分别配置 `distributionUrl`、`wrapperUrl`；更换版本时同时更新校验值。
默认 Maven 仓库镜像见 `scripts/maven-settings.xml`，npm 镜像见 `frontend/.npmrc`。

## 手工构建与运行

在仓库根目录打开终端：

```powershell
cd backend
.\mvnw.cmd -s ../scripts/maven-settings.xml package
# 已安装 Maven 时也可使用 mvn -s ../scripts/maven-settings.xml package
java -Dfile.encoding=UTF-8 -jar target/gdpm-system.jar
```

另开终端，从仓库根目录执行：

```powershell
cd frontend
npm.cmd ci
npm.cmd run build
npm.cmd run dev -- --host 127.0.0.1 --port 5173 --strictPort
```

Linux/macOS 在 backend 目录使用 `sh mvnw -s ../scripts/maven-settings.xml package`，前端使用 `npm`。
后端产物为 `backend/target/gdpm-system.jar`，前端产物为 `frontend/dist/`。
H2 数据保存在 `backend/data/`，材料保存在 `backend/uploads/`，备份时需一起保存。

## MySQL 8 运行

1. 启动 MySQL 8，使用具有建库权限的账号。在 MySQL 客户端中以 UTF-8 执行 `sql/init.sql`，
   创建 utf8mb4 的 gdpm 数据库、26 张业务表与演示数据。该脚本用于首次初始化，不应向已有业务库重复导入。
2. 先执行 `stop.bat`。在启动终端通过环境变量提供数据库凭据并选择 profile：

```powershell
$env:SPRING_PROFILES_ACTIVE='mysql'
# 由你的终端或安全环境提供，不提交到文件或仓库
$env:GDPM_DB_USERNAME='<数据库账号>'
$env:GDPM_DB_PASSWORD='<数据库密码>'
# 可选，默认连接本机 3306 的 gdpm
$env:DB_URL='jdbc:mysql://127.0.0.1:3306/gdpm?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai'
.\start.bat
```

凭据仅从环境变量读取。切换回 H2 时，停止服务并清除 `SPRING_PROFILES_ACTIVE`，或设为 `dev`。
也可在完成构建后用 `java -jar target/gdpm-system.jar --spring.profiles.active=mysql` 启动后端。

## 演示账号

所有演示密码均为 **123456**，初始数据如下：

| 账号 | 角色 | 初始演示状态 |
| --- | --- | --- |
| admin | 院系管理员 | 全院系管理，有待审核课题 |
| teacher1 | 指导教师：程者军 | 指导 student2、student4 |
| teacher2 | 指导教师：王老师 | 指导 student3 |
| student1 | 学生：刘冉 | 尚未选题，可演示完整业务 |
| student2 | 学生：李明 | 已选题、任务书通过、开题退回 |
| student3 | 学生：王悦 | 开题已通过，中检待审核 |
| student4 | 学生：陈晨 | 材料齐全、成绩已发布、已归档 |

密码使用 BCrypt 加盐哈希。随机令牌仅保存 SHA-256 摘要，12 小时过期；退出、停用或管理员维护用户后失效。
菜单按角色权限生成，服务端再次执行 Shiro 权限与数据范围校验。

## 技术栈

- 后端：Java 11、Spring Boot 2.7.18、MyBatis-Plus 3.5.5、Apache Shiro 2.0.4、REST API。
- 前端：Vue 3、Vite 6、Element Plus、Axios、ECharts、Vue Router、Pinia；依赖由 package-lock.json 锁定。
- 数据：H2（MySQL 模式）或 MySQL 8；26 个表实体及 BaseMapper，模块 Service/ServiceImpl，跨表统计放在 Mapper XML。

## 功能与业务规则

| 模块 | 主要操作 |
| --- | --- |
| 工作台 | 待办、个人或指导范围内学生进度、九节点完成比例、截止提醒、ECharts 图表与通知 |
| 系统管理 | 用户新增/编辑/启停/重置密码；三角色权限配置；权限目录；专业、班级、届次维护；节点截止时间；操作日志 |
| 课题与双选 | 教师申报、管理员通过/退回、教师修改重提、学生按专业申请、教师确认、申请状态与反馈 |
| 过程材料 | 教师下达任务书；学生上传开题、中检、初稿、定稿；教师审核；退回重提与所有历史版本下载 |
| 指导与审核 | 指导时间/内容记录、材料待办入口、审核人/时间/结果/意见留痕 |
| 答辩与成绩 | 管理员创建答辩组并安排学生；教师录入三项分数与权重；管理员发布成绩 |
| 统计归档 | 按专业/班级/教师统计，可按届次筛选；缺失和未通过材料清单；CSV 导出；归档快照查询 |

- 教师只处理自己的课题和本人指导的学生；学生只能查询自己的过程数据。学生可浏览通过审核的课题，申请时服务端校验专业、名额与截止时间。
- student_assignment 以 student_id 为主键，确保一名学生只能确定一个课题；确认时锁定学生与课题行，并在事务内检查名额。
- 状态统一为待提交、待审核、已通过、已退回、已完成。每次材料提交保留提交时间、版本、修改次数、审核人与审核意见，审核另存 review_record。
- 任务书由教师下达后直接通过；其余材料必须按开题 → 中检 → 初稿 → 定稿顺序提交，前一阶段通过后才能进入下一阶段。
- 材料退回后允许生成新版本，旧文件和旧审核意见保留；通过的阶段不重复提交。
- 上传支持 PDF、DOC、DOCX、TXT、ZIP，限20MB，校验扩展名及基础文件特征；以随机文件名保存到 backend/uploads。下载必须经过鉴权接口，文件目录不作为静态资源公开。
- 分数范围0至100，权重0至1，最多两位小数，三项权重之和必须等于1；按 BigDecimal 计算总成绩，四舍五入保留两位小数。未发布成绩对学生隐藏，发布后不能改分。
- 全部五类材料通过且成绩已发布后，管理员才能归档；归档保存材料版本、文件编号与成绩快照，并阻止重复归档和材料变更。
- 操作日志记录关键业务操作与登录退出。通知由管理员发布，所有登录用户可读。

## 接口概览

所有受保护接口使用 `Authorization: Bearer <token>`，JSON 请求使用 Content-Type: application/json；文件上传使用 multipart/form-data 的 file 字段。错误以 HTTP 400/401/403/404/409/413 和 message 返回。

业务 JSON 响应统一为 `{"code":200,"message":"操作成功","data":...}`；错误的 code 与实际 HTTP 状态一致，data 为 null。前端在 `api/http.js` 统一解包 data。健康探针 `/api/health` 保留启动脚本原协议，文件下载保留二进制响应。

所有列表接口支持可选 `?page=1&size=20`，返回 `data.records / total / page / size`，size 限 1 至 200。未传分页参数时，data 保留原完整列表，保持现有页面调用方式。分页在服务层数据范围过滤后执行，不允许请求参数扩大范围。统计、看板和下拉字典为复合对象，不对对象本身分页。

Shiro 使用自定义 `TokenRealm`、原生过滤链和 `TokenFilter`；控制器通过 `@RequiresRoles` / `@RequiresPermissions` 约束操作。服务层再次校验学生归属和指导关系。角色权限每次请求重新加载，撤权立即生效。关键变更由 `@OperLog` 切面统一写入 `sys_log`，日志不记录密码、令牌或完整请求体。

`sys_user_role` 和 `sys_role_permission` 保留原数据库复合主键。MyBatis-Plus 以其中一列作为实体标识，关联维护始终使用明确的条件构造器，禁止以单字段主键更新整条关联。

| 接口 | 用途 |
| --- | --- |
| POST /api/auth/login；GET /api/auth/me；POST /api/auth/logout | 登录、身份权限、退出 |
| GET /api/dashboard、/api/students、/api/reference、/api/workflow | 看板、数据范围、下拉数据、截止时间 |
| GET/POST /api/topics；PUT /api/topics/{id} | 课题列表、申报、退回后修改 |
| POST /api/topics/{id}/review、/api/topics/{id}/apply | 课题审核、申请选题 |
| GET /api/selections；POST /api/selections/{id}/review | 双选查询与确认 |
| POST /api/files；GET /api/files/{id} | 上传与受控下载 |
| GET /api/materials；POST /api/materials/{stage} | 版本列表与提交 |
| POST /api/materials/{stage}/{id}/review；GET /api/reviews | 材料审核与意见留痕 |
| GET/POST /api/guidance | 指导记录 |
| GET/POST /api/defense/groups、/api/defenses、/api/scores | 答辩组、学生安排、成绩录入 |
| POST /api/scores/{id}/publish | 管理员发布成绩 |
| GET /api/statistics；GET/POST /api/archives | 统计筛选、归档查询和创建 |
| GET/POST /api/notices；DELETE /api/notices/{id} | 通知管理 |
| /api/system/users、roles、permissions、base/{type}、logs | 系统维护与审计 |

stage 为 task / proposal / midterm / draft / final；基础数据 type 为 majors / classes / cohorts。具体请求字段与可执行示例见 `scripts/selftest.py`。

## 可重复验证

Maven package 包含 10 项数据库、权限、令牌、分页、AOP 日志和统计集成测试。
以下辅助检查需要 Python 3 与 curl；在仓库根目录执行：

```powershell
# 独立 H2 内存数据库和 18081 端口，不修改演示数据
python -X utf8 scripts/selftest.py
# 已运行的前端路由、构建资源与代理检查
python -X utf8 scripts/frontend_smoke.py
```

自测先运行统一响应、分页和范围隔离检查，再执行原 215 项 curl 全链路断言，覆盖全部演示账号。
请求/响应临时文件在结束时清理，仅保留脱敏断言结果与后端日志于 `run/selftest-h2/`。

真实 MySQL 回归要求一个**尚不存在 gdpm 库的独立 MySQL 8 测试实例**，会创建、修改演示业务数据。
先从环境提供上述两项数据库凭据，确保 mysql 客户端在 PATH（或设置 GDPM_MYSQL_CLIENT）：

```powershell
python -X utf8 scripts/init_mysql.py
python -X utf8 scripts/selftest.py --profile mysql
```

MySQL 自测使用 18082 端口，读取同一份初始化 SQL，不自动删除数据库。已有 gdpm 库时初始化工具会停止，避免覆盖数据。

浏览器验收可使用已安装的 Chrome 和 Playwright，检查 1024、1366、1920 三个宽度的页面、标签和固定操作列：

```powershell
npm.cmd install --prefix run/browser-tools --registry=https://registry.npmmirror.com --proxy=null --https-proxy=null --save-exact playwright@1.65.1
node scripts/browser_check.mjs
```

备用端口验证时可设置 GDPM_FRONTEND_URL（前端检查地址）或 GDPM_TEST_BASE（API 检查地址）。
验收方法、实测结果见 [测试报告](docs/TEST_REPORT.md)。

## 目录结构

```text
.
├─backend/
│ ├─src/main/java/edu/gengdan/gdpm/
│ │ ├─entity/、mapper/            26 表实体、BaseMapper 与统计 Mapper
│ │ ├─service/、service/impl/     模块契约、事务与数据范围校验
│ │ ├─controller/、dto/、vo/      REST 接口及响应契约
│ │ └─auth/、config/、common/     Shiro、日志切面、异常与通用定义
│ ├─src/main/resources/          dev/mysql 配置、Mapper XML、共用结构和演示数据
│ ├─src/test/                    10 项集成测试
│ ├─.mvn/wrapper/、mvnw、mvnw.cmd Maven Wrapper
│ └─pom.xml
├─frontend/
│ ├─src/views/                   系统、课题、材料、指导、答辩、统计页面
│ ├─src/api/                     各业务模块 API 与 HTTP 客户端
│ ├─src/components/、stores/     公共组件与身份状态
│ └─package.json、package-lock.json
├─scripts/                       启停、镜像配置、自测与本机设置示例
├─sql/init.sql                   MySQL 8 首次初始化
├─docs/TEST_REPORT.md            测试方法和结果
├─docs/screenshots/              页面截图说明
├─run/                           运行和验收产物（忽略入库）
├─start.bat、stop.bat
└─README.md
```

## 运行维护

后端日志为 `run/backend.out.log`、`run/backend.err.log`，前端日志为 `run/frontend.out.log`、`run/frontend.err.log`。
系统启动和自测均不清理正式 H2 数据。重置演示环境前应停止服务并备份数据库和材料。
