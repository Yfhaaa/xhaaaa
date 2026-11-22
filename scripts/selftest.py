"""Repeatable curl end-to-end checks against an isolated H2 database; never changes demo data."""
import argparse, concurrent.futures, json, os, pathlib, shutil, subprocess, sys, time, uuid
sys.stdout.reconfigure(encoding='utf-8',errors='backslashreplace')
ROOT=pathlib.Path(__file__).resolve().parent.parent
parser=argparse.ArgumentParser(description='curl 全链路回归，mysql 模式要求已初始化的全新 gdpm 库')
parser.add_argument('--profile', choices=['h2','mysql'], default='h2')
parser.add_argument('--port', type=int)
options=parser.parse_args()
port=options.port or (18082 if options.profile=='mysql' else 18081)
os.chdir(ROOT)
OUT=ROOT/('run/selftest-'+options.profile)
OUT.mkdir(parents=True,exist_ok=True)
BASE=f'http://127.0.0.1:{port}/api'
CURL=shutil.which('curl.exe') or shutil.which('curl')
if not CURL: raise SystemExit('请先安装 curl 并加入 PATH')
tokens={}
results=[]
env=os.environ.copy()
for key in list(env):
    if key.lower() in ('http_proxy','https_proxy','all_proxy'): env.pop(key,None)
def request(label, method, path, who=None, data=None, expected=200, upload=None, record=True):
    nonce=uuid.uuid4().hex
    response=OUT/(nonce+'.response')
    args=[CURL,'--noproxy','*','-sS','--max-time','30','-X',method,BASE+path,'-o',str(response.relative_to(ROOT)),'-w','%{http_code}']
    if who: args+=['-H','Authorization: Bearer '+tokens[who]]
    if data is not None:
        body=OUT/(nonce+'.request')
        body.write_text(json.dumps(data,ensure_ascii=False),encoding='utf-8')
        args+=['-H','Content-Type: application/json','--data-binary','@'+str(body.relative_to(ROOT))]
    if upload: args+=['-F','file=@'+str(upload.relative_to(ROOT))]
    p=subprocess.run(args,capture_output=True,env=env)
    status=int(p.stdout.decode() or '0')
    raw=response.read_bytes() if response.exists() else b''
    try: value=json.loads(raw)
    except (ValueError,UnicodeError): value=raw
    if isinstance(value,dict) and value.get("code")==200 and "data" in value:
        value=value["data"]
    passed=p.returncode==0 and status==expected
    if record:
        results.append(dict(name=label,method=method,path=path,status=status,expected=expected,passed=passed))
        print(('PASS' if passed else 'FAIL'),label,status,flush=True)
    if not passed: raise AssertionError(label+': '+str(status)+' '+str(value))
    return value
def verify(label, condition):
    results.append(dict(name=label,passed=bool(condition)))
    print(('PASS' if condition else 'FAIL'),label,flush=True)
    if not condition: raise AssertionError(label)
def login(who):
    value=request('登录 '+who,'POST','/auth/login',data=dict(username=who,password='123456'))
    tokens[who]=value['token']
def review(path,who,approved=True,comment='符合要求，同意通过',expected=200):
    return request('审核 '+path,'POST',path,who,dict(approved=approved,comment=comment),expected)
sample=OUT/'sample.txt';sample.write_text('毕业设计测试材料\n需求分析、技术路线、实施进度与测试计划。',encoding='utf-8')
bad=OUT/'bad.exe';bad.write_bytes(b'MZ-invalid')
pdf=OUT/'fake.pdf';pdf.write_bytes(b'not-a-pdf')
java=pathlib.Path(env['JAVA_HOME'])/'bin'/('java.exe' if os.name=='nt' else 'java') if env.get('JAVA_HOME') else shutil.which('java')
if not java: raise SystemExit('请配置 JAVA_HOME 或把 JDK 11+ 加入 PATH')
log=(OUT/'backend.log').open('w',encoding='utf-8')
args=[str(java),'-Dfile.encoding=UTF-8','-Djava.io.tmpdir=run/tmp','-jar',
      'backend/target/gdpm-system-v3.jar',f'--server.port={port}',
      '--app.upload-dir=./'+OUT.relative_to(ROOT).as_posix()+'/uploads']
if options.profile=='mysql':
    if not all(env.get(k) for k in ['GDPM_DB_USERNAME','GDPM_DB_PASSWORD']):
        raise SystemExit('mysql 模式需要进程环境 GDPM_DB_USERNAME、GDPM_DB_PASSWORD')
    client=env.get('GDPM_MYSQL_CLIENT') or shutil.which('mysql')
    if not client:
        matches=sorted(pathlib.Path(env.get('ProgramFiles','C:/Program Files')).glob('MySQL/*/bin/mysql.exe'))
        client=str(matches[-1]) if matches else None
    if not client: raise SystemExit('MySQL客户端不可用')
    mysql_name='gdpm_r3_'+uuid.uuid4().hex[:12]
    mysql_env=env.copy();mysql_env['MYSQL_PWD']=env['GDPM_DB_PASSWORD']
    mysql_args=[client,'--protocol=TCP','--host=127.0.0.1','--port=3306',
                '--user='+env['GDPM_DB_USERNAME'],'--default-character-set=utf8mb4']
    def mysql_sql(sql):
        result=subprocess.run(mysql_args,input=sql.encode('utf-8'),capture_output=True,env=mysql_env)
        if result.returncode: raise RuntimeError('??MySQL客户端不可用??????????????')
    mysql_sql('CREATE DATABASE '+mysql_name+' CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;')
    init_sql=(ROOT/'sql/init.sql').read_text(encoding='utf-8-sig')
    mysql_sql('USE '+mysql_name+';'+init_sql[init_sql.index('CREATE TABLE'):])
    env['DB_URL']='jdbc:mysql://127.0.0.1:3306/'+mysql_name+'?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai'
    args+=['--spring.profiles.active=mysql']
else:
    args+=['--spring.profiles.active=dev',
           '--spring.datasource.url=jdbc:h2:mem:curltest;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1']
(ROOT/'run/tmp').mkdir(parents=True,exist_ok=True)
process=subprocess.Popen(args,cwd=ROOT,env=env,stdout=log,stderr=subprocess.STDOUT,creationflags=getattr(subprocess,"CREATE_NO_WINDOW",0))
failed=None
try:
    for _ in range(90):
        if process.poll() is not None: raise RuntimeError('后端启动失败，查看 run/selftest-'+options.profile+'/backend.log')
        try:
            health=request('ready','GET','/health',record=False)
            ready=request('ready login','POST','/auth/login',data=dict(username='admin',password='123456'),record=False)
            break
        except Exception: time.sleep(1)
    else: raise RuntimeError('后端启动超时')
    smoke_env=env.copy()
    smoke_env['GDPM_TEST_BASE']=BASE
    smoke_env['GDPM_TEST_OUT']=str(OUT/'pagination-scope')
    subprocess.run([sys.executable,'scripts/api_smoke.py'],env=smoke_env,cwd=ROOT,check=True)
    request('健康检查','GET','/health')
    import urllib.request
    with urllib.request.urlopen(BASE.replace('/api','')+'/v3/api-docs') as response:
        spec=json.load(response)
    verify('Swagger含新旧业务接口', '/api/topic-changes' in spec['paths'] and '/api/materials/{stage}' in spec['paths'])
    request('匿名访问拦截','GET','/topics',expected=401)
    request('错误密码拦截','POST','/auth/login',data=dict(username='admin',password='wrong'),expected=401)
    for who in ['admin','teacher1','teacher2','student1','student2','student3','student4']: login(who)
    me=request('当前身份与RBAC','GET','/auth/me','student1')
    verify('学生身份不含系统管理权限',me['roles']==['STUDENT'] and 'system:manage' not in me['permissions'])
    request('学生访问管理用户被拒','GET','/system/users','student1',expected=403)
    request('教师访问操作日志被拒','GET','/system/logs','teacher1',expected=403)
    request('学生申报课题被拒','POST','/topics','student1',dict(title='越权',description='越权',majorId=1,capacity=1),403)
    topic=request('教师发布课题','POST','/topics','teacher1',dict(title='全过程管理系统自动化验证课题',description='覆盖课题申报、双选、过程材料、答辩及归档。',majorId=1,capacity=1))['id']
    request('未审批课题不能申请','POST',f'/topics/{topic}/apply','student1',expected=400)
    review(f'/topics/{topic}/review','teacher2',expected=403)
    review(f'/topics/{topic}/review','admin')
    review(f'/topics/{topic}/review','admin',expected=400)
    selection=request('学生申请选题','POST',f'/topics/{topic}/apply','student1')['id']
    request('重复申请被拒','POST',f'/topics/{topic}/apply','student1',expected=400)
    review(f'/selections/{selection}/review','teacher2',expected=403)
    review(f'/selections/{selection}/review','teacher1')
    request('一个学生不能确认多个课题','POST','/topics/1/apply','student1',expected=400)
    own=request('学生仅查询自己','GET','/students','student1')
    verify('学生数据范围为自己',len(own)==1 and own[0]['student_id']==4)
    own=request('教师仅查询本人指导学生','GET','/students','teacher1')
    verify('教师不可见其他教师学生',all(row['teacher_id']==2 for row in own))
    request('拒绝可执行文件上传','POST','/files','student1',expected=400,upload=bad)
    request('拒绝伪造PDF','POST','/files','student1',expected=400,upload=pdf)
    fid=request('教师上传任务书','POST','/files','teacher1',upload=sample)['id']
    request('跨教师下达任务书被拒','POST','/materials/task','teacher2',dict(studentId=4,title='task',fileId=fid),403)
    request('教师下达任务书','POST','/materials/task','teacher1',dict(studentId=4,title='毕业设计任务书',content='完成设计、实现与测试',fileId=fid))
    studentFile=request('学生上传开题材料','POST','/files','student1',upload=sample)['id']
    request('不能跳过开题提交中检','POST','/materials/midterm','student1',dict(title='中检',fileId=studentFile),400)
    request('不能引用他人上传的文件','POST','/materials/proposal','student1',dict(title='开题',fileId=fid),403)
    proposal=request('学生提交开题报告v1','POST','/materials/proposal','student1',dict(title='开题报告v1',content='研究背景与技术路线',fileId=studentFile))['id']
    review(f'/materials/proposal/{proposal}/review','teacher2',expected=403)
    request('学生不能审核自己的材料','POST',f'/materials/proposal/{proposal}/review','student1',dict(approved=True,comment='自己通过'),403)
    review(f'/materials/proposal/{proposal}/review','teacher1',False,'请补充技术路线与实施计划')
    second=request('开题退回后重新提交v2','POST','/materials/proposal','student1',dict(title='开题报告v2',content='已补充技术路线与实施计划',fileId=studentFile))
    verify('退回重提版本递增',second['version']==2)
    review(f'/materials/proposal/{second["id"]}/review','teacher1')
    review(f'/materials/proposal/{second["id"]}/review','teacher1',expected=400)
    allMaterials=request('材料历史版本查询','GET','/materials','student1')
    verify('开题两个版本与退回意见完整保留',len([m for m in allMaterials if m['stage']=='proposal'])==2 and any(m['review_comment']=='请补充技术路线与实施计划' for m in allMaterials))
    request('本人文件可下载','GET',f'/files/{studentFile}','student1')
    request('指导教师文件可下载','GET',f'/files/{studentFile}','teacher1')
    request('其他学生不能下载','GET',f'/files/{studentFile}','student2',expected=403)
    request('其他教师不能下载','GET',f'/files/{studentFile}','teacher2',expected=403)
    for stage in ['midterm','draft','final']:
        mid=request('提交 '+stage,'POST','/materials/'+stage,'student1',dict(title=stage+' 测试材料',fileId=studentFile))['id']
        if stage == 'final':
            request('未登记查重不能通过定稿','POST',f'/materials/final/{mid}/review','teacher1',dict(approved=True,comment='通过'),400)
            request('登记超标查重','POST','/plagiarism-checks','teacher1',dict(studentId=4,platform='测试平台',rate=35,fileId=fid))
            request('超标查重阻止定稿','POST',f'/materials/final/{mid}/review','teacher1',dict(approved=True,comment='通过'),400)
            request('登记合格查重','POST','/plagiarism-checks','teacher1',dict(studentId=4,platform='测试平台复查',rate=20,fileId=fid))
        review(f'/materials/{stage}/{mid}/review','teacher1')
    request('记录教师指导','POST','/guidance','teacher1',dict(studentId=4,content='已完成全过程功能实现，补充测试记录。',guidanceAt='2026-11-22T10:00:00'))
    request('跨教师记录指导被拒','POST','/guidance','teacher2',dict(studentId=4,content='越权',guidanceAt='2026-11-22T10:00:00'),403)
    group=request('管理员创建答辩组','POST','/defense/groups','admin',dict(name='测试答辩组',teacherIds=[2,3],location='A302',defenseAt='2027-06-01T09:00:00'))['id']
    request('未评阅不能安排答辩','POST','/defenses','admin',dict(studentId=4,groupId=group),400)
    request('指导教师不能自评','POST','/thesis-reviews/assign','admin',dict(studentId=4,reviewerId=2),400)
    request('分配评阅教师','POST','/thesis-reviews/assign','admin',dict(studentId=4,reviewerId=3))
    assigned=request('评阅教师任务','GET','/thesis-reviews','teacher2')
    rid=next(r['id'] for r in assigned if r['student_id']==4)
    request('非评阅教师不能评阅','POST',f'/thesis-reviews/{rid}/review','teacher1',dict(score=80,approved=True,comment='同意'),403)
    request('独立教师完成论文评阅','POST',f'/thesis-reviews/{rid}/review','teacher2',dict(score=80,approved=True,comment='同意答辩'))
    request('管理员安排答辩','POST','/defenses','admin',dict(studentId=4,groupId=group))
    scoreBody=dict(studentId=4,guidanceScore=90,reviewScore=80,defenseScore=95,guidanceWeight=0.3,reviewWeight=0.3,defenseWeight=0.4)
    request('错误权重被拒','POST','/scores','teacher1',{**scoreBody,'defenseWeight':0.5},400)
    request('成绩精度超过两位被拒','POST','/scores','teacher1',{**scoreBody,'reviewScore':80.123},400)
    request('越权评分被拒','POST','/scores','teacher2',scoreBody,403)
    score=request('教师录入加权成绩','POST','/scores','teacher1',scoreBody)
    verify('加权总成绩为89.00',score['totalScore']==89)
    value=request('未发布成绩对学生隐藏','GET','/scores','student1')
    verify('成绩发布前不可见',len(value)==0)
    request('未发布成绩不能归档','POST','/archives','admin',dict(studentId=4),400)
    request('学生不能发布成绩','POST',f'/scores/{score["id"]}/publish','student1',expected=403)
    request('管理员发布成绩','POST',f'/scores/{score["id"]}/publish','admin')
    request('已发布成绩不能修改','POST','/scores','teacher1',scoreBody,400)
    value=request('学生查询已发布成绩','GET','/scores','student1')
    verify('成绩发布后可见且正确',len(value)==1 and value[0]['total_score']==89)
    request('管理员完整归档','POST','/archives','admin',dict(studentId=4))
    request('重复归档被拒','POST','/archives','admin',dict(studentId=4),400)
    dash=request('学生仪表盘流程完成','GET','/dashboard','student1')
    verify('完整闭环进度100%',dash['progress'][0]['progress']==100 and dash['archiveCount']==1)
    for who in ['admin','teacher1','teacher2','student1','student2','student3','student4']:
        for path in ['/dashboard','/topics','/selections','/materials','/guidance','/reviews','/defense/groups','/defenses','/scores','/statistics','/archives','/workflow','/notices','/reference']:
            request(who+' 页面接口 '+path,'GET',path,who)
    for dimension in ['major','class','teacher']: request('统计维度 '+dimension,'GET','/statistics?groupBy='+dimension,'admin')
    request('统计筛选','GET','/statistics?groupBy=teacher&majorId=1&classId=1&teacherId=2&cohort=2026','admin')
    notice=request('管理员发布通知','POST','/notices','admin',dict(title='自动化验证通知',content='系统全流程测试通过'))['id']
    request('管理员删除测试通知','DELETE',f'/notices/{notice}','admin')
    for endpoint in ['users','roles','permissions','logs']: request('系统管理 '+endpoint,'GET','/system/'+endpoint,'admin')
    major=request('创建专业','POST','/system/base/majors','admin',dict(name='测试专业'))['id']
    request('更新专业','PUT',f'/system/base/majors/{major}','admin',dict(name='测试专业更新'))
    request('删除未引用专业','DELETE',f'/system/base/majors/{major}','admin')
    permission=request('创建权限项','POST','/system/permissions','admin',dict(code='test:read',name='测试权限'))['id']
    request('编辑权限项名称','PUT',f'/system/permissions/{permission}','admin',dict(name='测试权限更新'))
    role=request('读取教师角色','GET','/system/roles','admin')[1]
    request('维护角色权限','PUT','/system/roles/2','admin',dict(name=role['name'],permissionIds=role['permission_ids']))
    request('不能移除管理员管理权限','PUT','/system/roles/1','admin',dict(name='管理员',permissionIds=[1]),400)
    request('配置过期截止时间','PUT','/workflow/2','admin',dict(deadline='2020-01-01T00:00:00'))
    raceTopic=request('创建名额竞争课题','POST','/topics','teacher1',dict(title='名额竞争测试',description='并发验证名额上限',majorId=1,capacity=1))['id']
    review(f'/topics/{raceTopic}/review','admin')
    ids=[]
    for who in ['race_student_a','race_student_b']:
        sid=request('创建并发测试学生 '+who,'POST','/system/users','admin',dict(username=who,name=who,password='123456',roleId=3,majorId=1,classId=1,cohort='2026',enabled=1))['id']
        ids.append(sid);login(who)
        request('截止后选题被拒','POST',f'/topics/{raceTopic}/apply',who,expected=400)
    request('恢复选题截止时间','PUT','/workflow/2','admin',dict(deadline='2027-02-28T23:59:59'))
    selections=[request('竞争学生申请','POST',f'/topics/{raceTopic}/apply',who)['id'] for who in ['race_student_a','race_student_b']]
    def confirm(sid):
        try:
            request('并发确认','POST',f'/selections/{sid}/review','teacher1',dict(approved=True,comment='同意指导'),record=False)
            return 200
        except AssertionError as error:
            if ': 400 ' in str(error): return 400
            raise
    with concurrent.futures.ThreadPoolExecutor(max_workers=2) as pool: status=list(pool.map(confirm,selections))
    verify('并发确认名额1仅成功1人',sorted(status)==[200,400])
    current=request('验证竞争课题人数','GET','/topics','admin')
    verify('数据库实际选定人数未超额',next(t for t in current if t['id']==raceTopic)['selected_count']==1)
    user=request('读取测试用户','GET','/system/users','admin')
    u=next(x for x in user if x['id']==ids[0])
    request('停用测试账号','PUT',f'/system/users/{u["id"]}','admin',dict(name=u['name'],roleId=3,majorId=1,classId=1,cohort='2026',enabled=0))
    request('停用后既有令牌失效','GET','/auth/me','race_student_a',expected=401)
    request('停用账号不能登录','POST','/auth/login',data=dict(username='race_student_a',password='123456'),expected=401)
    from round3_selftest import run
    run(request, verify, login, tokens, sample, OUT)
    request('退出登录','POST','/auth/logout','student1')
    request('退出后令牌不可复用','GET','/auth/me','student1',expected=401)
except Exception as error:
    failed=str(error);print('FAILED:',failed,flush=True)
finally:
    process.terminate()
    try: process.wait(timeout=20)
    except subprocess.TimeoutExpired: process.kill();process.wait()
    log.close()
    if options.profile=='mysql' and 'mysql_name' in globals():
        mysql_sql('DROP DATABASE '+mysql_name+';')
    summary=dict(profile=options.profile,concurrentStatuses=locals().get('status'),time=time.strftime('%Y-%m-%d %H:%M:%S'),passed=sum(r['passed'] for r in results),total=len(results),error=failed,checks=results)
    (OUT/'results.json').write_text(json.dumps(summary,ensure_ascii=False,indent=2),encoding='utf-8')
    # Request files contain demo credentials, response files can contain tokens; retain only redacted results and server log.
    for pattern in ['*.request','*.response']:
        for path in OUT.glob(pattern): path.unlink()
    print(json.dumps({k:v for k,v in summary.items() if k!='checks'},ensure_ascii=False),flush=True)
sys.exit(1 if failed else 0)
