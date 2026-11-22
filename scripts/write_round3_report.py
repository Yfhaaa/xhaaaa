from pathlib import Path
import json, hashlib, xml.etree.ElementTree as ET
from datetime import datetime
R=Path(__file__).resolve().parent.parent

def read(name):
    p=R/name
    return json.loads(p.read_text(encoding='utf-8-sig')) if p.exists() else {'total':0,'passed':0,'error':'尚未生成'}
def count(name):
    r=read(name)
    return f"{r.get('passed',0)}/{r.get('total',0)}"
files=list((R/'backend/target/surefire-reports').glob('TEST-*.xml'))
unit=[ET.parse(p).getroot() for p in files]
total=sum(int(x.get('tests',0)) for x in unit)
failed=sum(int(x.get('failures',0))+int(x.get('errors',0)) for x in unit)
paths={
 'Swagger入口与资源':'run/swagger-check.json',
 'H2全链路':'run/selftest-h2/results.json', 'MySQL全链路':'run/selftest-mysql/results.json',
 'H2范围与分页':'run/selftest-h2/pagination-scope/results.json',
 'MySQL范围与分页':'run/selftest-mysql/pagination-scope/results.json',
 '浏览器完整检查':'run/browser-round3/results.json','浏览器调整复查':'run/browser-round3-focus/results.json',
 '待处理按钮':'run/browser-pending/results.json','实际操作与定时刷新':'run/browser-round3-actions/results.json',
 '前端资源与代理':'run/frontend-smoke/results.json', '源码结构':'run/source-checks.json'}
rows='\n'.join(f'| {name} | {count(path)} | `{path}` |' for name,path in paths.items())
shots=list((R/'docs/screenshots/round3').glob('*.png'))
jar=R/'backend/target/gdpm-system-v3.jar'
sha=hashlib.sha256(jar.read_bytes()).hexdigest()
report=f'''# 第3轮测试报告

更新时间：{datetime.now():%Y-%m-%d %H:%M:%S}。验收依据：过程性材料/QC_返工意见_03_高级化.md。

## 最终结果

后端 Maven package 成功，Spring Boot 集成测试 **{total}项，失败/错误{failed}项**；前端 npm run build 成功。
两套 curl 使用同一业务脚本，并在完整流程之前执行原151项响应、分页和越权断言。

| 检查 | 通过/总数 | 原始证据 |
| --- | --- | --- |
{rows}

浏览器检查使用真实 Chrome 和现有 run/browser-tools/Playwright，覆盖1024、1366、1920三个宽度。
完整检查覆盖管理员、两名教师、开题退回学生、已归档学生；调整复查覆盖公共表单、筛选、消息、档案和大屏。
最终截图共{len(shots)}张，见 [截图索引](screenshots/README.md)。

## 后端覆盖

原 DatabaseContractTest 的10项用例保留，包括实体默认值、唯一分配、BCrypt、Shiro注解独立拦截、
既有令牌撤权、分页/伪造参数、AOP脱敏日志、服务层跨教师隔离、注销令牌、九节点统计一致性。

新增 AdvancedContractTest 的11项真实 Spring Boot 集成测试覆盖：

- 三级变更顺序、重复申请、跳级、拒绝、师生关系、审批历史；变更后的新教师继承历史指导记录，原教师失去范围。
- 指导教师回避、随机分配、评阅人权限、不同意答辩拦截；成绩强制使用独立评阅分。
- 定稿查重缺失/超标拦截、版本绑定、阈值配置和跨教师登记拦截。
- 私人消息隔离、每日截止提醒与重复执行去重、已读状态。
- 个人资料白名单、联系方式校验、旧密码和强度、换密撤销全部会话。
- 九节点档案范围、管理员大屏及30天数据；真实ZIP内容、EasyExcel模板/导出和逐行导入错误。
- 全部OpenAPI操作的中文摘要；旧库补列重复执行且保留原用户。

curl进一步验证完整业务闭环、版本历史、文件权限、权重精度、发布锁定、单人/班级ZIP、
真实xlsx导入、消息已读、资料和密码、选题并发与课题变更并发。
两类名额竞争均为一个200、一个400，数据库实际分配不超过名额。

## 前端覆盖

所有独立路由分别检查查询列表、空状态、右侧固定操作列、表格内滚动两端、页面横向溢出和页面/API错误。
检查登录刷新、角色路由守卫、弹窗边界、折叠侧栏、可关闭页签。
实际操作检查共享系统表单保存、必填校验、模板和ZIP浏览器下载、九节点与可见版本、消息已读角标、
个人资料校验，并等待30秒确认大屏自动再次请求接口。待审核双选与材料按钮另有24项点击和弹窗检查。
视觉复核修正漏斗窄屏标签，最新页面截图保留深绿学院风，大屏为独立深色页。

## 数据与安全边界

- H2自测为独立内存库，MySQL为随机命名的gdpm_r3_测试库，直接执行同份sql/init.sql的表结构及数据部分。
  MySQL回归结束仅删除本次创建的测试库，正式gdpm不受影响。
- 生产初始化SQL与schema.sql/data.sql通过令牌级一致性检查；30个实体均有独立Mapper。
  幂等升级只创建缺表/缺列，已有业务和演示账号保留。
- 数据库账号/密码只由GDPM_DB_USERNAME / GDPM_DB_PASSWORD环境变量提供，无凭据落盘。
  业务日志不序列化请求体；自测请求/响应临时文件结束后清理。
- 控制器Shiro注解、服务层身份/数据范围、事务与学生/课题行锁共同约束业务，按钮隐藏不替代后端鉴权。
- 未执行git commit/push。

## 环境限制与运行入口

本轮执行项目stop.bat时，原8080/5173进程返回无权停止。未绕过操作系统权限，也未覆盖原启动记录。
本轮打包使用独立gdpm-system-v3.jar，并已在 **http://127.0.0.1:15173** 启动新版，后端18080，
Swagger为 **http://127.0.0.1:18080/swagger-ui/index.html**。
独立预览的数据库、附件、日志和PID记录位于run/preview-v3；stop-review.ps1已实际停止本轮实例，
随后可通过start-review.ps1重启。原端口切换需由其启动用户停止旧进程后运行start.bat。
这项环境限制单列为未完成的运行切换，功能开发和备用端口验证已完成。

Vite仍提示共享依赖块较大，构建成功。未把大块提示记作验证失败。

## 复现

按README准备Java/Node/Maven及数据库环境变量，运行package、npm run build、selftest.py（H2/MySQL）、
verify_source.py、frontend_smoke.py、browser_round3.mjs、browser_round3.mjs --focus、
browser_pending_check.mjs、browser_round3_actions.mjs。浏览器写入演示数据的脚本仅使用隔离预览。

后端产物SHA-256：`{sha}`。
'''
(R/'docs/TEST_REPORT.md').write_text(report,encoding='utf-8')
index='''# 第3轮截图索引

截图由真实Chrome自动生成，覆盖1024/1366/1920。文件名为“宽度-账号-路由.png”。
原第1/2轮截图保留；本轮最新页面在round3/目录。

| 主要页面 | 1024 | 1366 | 1920 |
| --- | --- | --- | --- |
'''
major=[('工作台','admin-dashboard'),('课题库','admin-topics'),('课题审核','admin-topics-audit'),
 ('双选申请','admin-selections'),('课题变更','admin-topic-changes'),('任务书','admin-materials-task'),
 ('开题报告','admin-materials-proposal'),('中期检查','admin-materials-midterm'),('论文提交','admin-materials-thesis'),
 ('查重记录','admin-plagiarism'),('评阅分配','admin-review-assignments'),('我的评阅','teacher2-my-reviews'),
 ('答辩分组','admin-defense-groups'),('答辩安排','admin-defense'),('成绩管理','admin-scores'),
 ('进度统计','admin-statistics'),('成绩分析','admin-score-analysis'),('缺失材料','admin-missing-materials'),
 ('归档管理','admin-archives'),('数据大屏','admin-board'),('个人消息','admin-messages'),
 ('个人中心','admin-profile'),('全过程档案','student4-my-project'),('用户管理','admin-system-users'),
 ('角色权限','admin-system-roles'),('专业班级','admin-system-classes'),('届次管理','admin-system-cohorts'),
 ('流程节点','admin-system-workflow'),('通知公告','admin-notices'),('操作日志','admin-system-logs')]
for title,slug in major:
    index+='| '+title+' | '+' | '.join(f'[查看](round3/{width}-{slug}.png)' for width in [1024,1366,1920])+' |\n'
index+=f'\n本轮共{len(shots)}张PNG。全部脚本与布局证据见docs/TEST_REPORT.md。\n'
(R/'docs/screenshots/README.md').write_text(index,encoding='utf-8')
result=f'''# 第3轮结果：高级化升级

完成日期：{datetime.now():%Y-%m-%d}。仓库：H:\\毕设系统_刘冉\\gdpm-system。
未执行git commit或push；既有账号、数据、接口和分层保留。

## 逐项对照

| 项目 | 实现与验证 |
| --- | --- |
| A1 课题变更 | topic_change表、学生申请、原教师→新教师→管理员三级审批、拒绝留痕、终审事务调整关系/名额；并发竞争与新教师历史档案继承已验证。 |
| A2 论文评阅 | thesis_review表、手工/批量随机分配、回避指导教师、指定评阅人意见/分数/答辩资格、不同意则禁止答辩；成绩取独立评阅分。 |
| A3 查重记录 | plagiarism_check表、平台/重复率/附件、定稿版本绑定、默认30%和节点阈值配置、缺失/超标拦截；前后端及演示记录齐备。 |
| A4 消息中心 | sys_message表，业务事件站内信、每天08:00三天内截止提醒、去重、收件人隔离、铃铛/最近消息/未读角标、单条/一键已读；换账号异步消息竞态已处理。 |
| A5 个人中心 | 姓名/邮箱/电话维护，旧密码校验、8–32位字母数字强度、BCrypt和会话失效，首字头像；表单和越权校验已验证。 |
| A6 全过程档案 | 九节点el-timeline、当前状态/提交/审核人/意见/修改/版本下载、指导记录和成绩构成；学生本人入口及教师管理员列表入口，变更后历史可继承。 |
| A7 归档打包 | 五阶段最终版附件+成绩单.xlsx+归档清单+批次清单，单人和班级ZIP；完整性/范围和浏览器实际下载通过。 |
| A8 Excel | EasyExcel用户模板及逐行校验结果、独立有效行提交，成绩/统计/缺失材料xlsx导出；真实xlsx/混合正确错误行和下载已验证。 |
| A9 数据大屏 | 独立全屏深色页，六类ECharts/预警模块，30秒轮询、浏览器全屏；数据、窄屏标签和自动刷新已验证。 |
| A10 接口文档 | springdoc-openapi/Swagger UI，25个Controller与每个操作中文Tag/Operation，Bearer输入；OpenAPI契约测试与README地址齐备。 |
| B 前端重构 | 35个独立页面、按角色二级菜单，公共列表/搜索/表单/上传/审批/时间线组件，分页/骨架/空状态/fixed操作/确认/校验，路由守卫与v-permission，面包屑/可关闭页签/折叠/刷新登录，深绿主题与三宽度截图。 |
| C 质量 | Maven package与npm build通过；Spring Boot {total}/{total}；双库完整回归及原151项各自通过；浏览器完整/复查/操作/待审核、源码45项和SQL同步完成；README、测试报告与截图已更新。 |

## 实测结果

| 验证 | 结果 |
| --- | --- |
| Spring Boot集成测试 | {total}项，失败/错误{failed}项 |
'''+ '\n'.join(f'| {name} | {count(path)} |' for name,path in paths.items())+f'''

新版可审阅：**http://127.0.0.1:15173**；Swagger：**http://127.0.0.1:18080/swagger-ui/index.html**。
完整证据见[TEST_REPORT.md](../gdpm-system/docs/TEST_REPORT.md)与[截图索引](../gdpm-system/docs/screenshots/README.md)。

## 未完成项（环境限制）

- **原8080/5173运行进程切换到新版**：已按要求执行stop.bat，系统提示当前用户无权停止。
  原运行记录保留，未强制绕过权限。新版已在18080/15173使用独立JAR与独立数据完成验证并运行。
  需要旧进程的启动用户停止原实例后，start.bat即可在默认端口启动新版。
- 功能开发与列出的备用环境验证没有未完成项。

## 交付说明

后端产物backend/target/gdpm-system-v3.jar，前端frontend/dist/；30张表的sql/init.sql与schema.sql/data.sql同步。
所有自测使用隔离库，数据库凭据未写入文件。新增可复用自测与预览脚本保留，一次性生成脚本已整理。
'''
(R.parent/'过程性材料/第3轮结果.md').write_text(result,encoding='utf-8')
summary={name:read(path) for name,path in paths.items()}
summary={name:{k:v for k,v in data.items() if k not in ['checks']} for name,data in summary.items()}
summary.update(time=datetime.now().isoformat(timespec='seconds'),springTests=total,springFailures=failed,
 jarSha256=sha,screenshotCount=len(shots),preview='http://127.0.0.1:15173')
(R/'run/round3-delivery.json').write_text(json.dumps(summary,ensure_ascii=False,indent=2),encoding='utf-8')
print('Reports and screenshot index updated')
