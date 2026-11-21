import sys
sys.stdout.reconfigure(encoding="utf-8")
"""Additional real-curl response, pagination and cross-role checks against the running demo."""
from pathlib import Path
from datetime import datetime
import json,os,shutil,subprocess,uuid
ROOT=Path(__file__).resolve().parent.parent
OUT=Path(os.environ.get('GDPM_TEST_OUT',str(ROOT/'run/api-smoke')))
OUT.mkdir(parents=True,exist_ok=True)
os.chdir(ROOT)
BASE=os.environ.get('GDPM_TEST_BASE','http://127.0.0.1:8080/api')
env={k:v for k,v in os.environ.items() if k.lower() not in ['http_proxy','https_proxy','all_proxy']}
checks=[];tokens={};error=None

def verify(name,condition):
 checks.append(dict(name=name,passed=bool(condition)))
 print(('PASS ' if condition else 'FAIL ')+name,flush=True)
 if not condition:raise AssertionError(name)

def request(name,path,who=None,method='GET',data=None,expected=200):
 nonce=uuid.uuid4().hex;response=OUT/(nonce+'.response');body=OUT/(nonce+'.request')
 args=[(shutil.which('curl.exe') or shutil.which('curl')),'--noproxy','*','-sS','--max-time','30','-X',method,BASE+path,'-o',str(response.relative_to(ROOT)),'-w','%{http_code}']
 if who:args+=['-H','Authorization: Bearer '+tokens[who]]
 if data is not None:
  body.write_text(json.dumps(data,ensure_ascii=False),encoding='utf-8')
  args+=['-H','Content-Type: application/json','--data-binary','@'+str(body.relative_to(ROOT))]
 result=subprocess.run(args,env=env,capture_output=True)
 status=int(result.stdout.decode() or '0');value=json.loads(response.read_bytes())
 verify(name+' HTTP',result.returncode==0 and status==expected)
 verify(name+' Result结构',isinstance(value,dict) and value.get('code')==expected and 'message' in value and 'data' in value)
 return value.get('data')
try:
 request('匿名请求','/students',expected=401)
 for who in ['admin','teacher1','teacher2','student2','student4']:
  tokens[who]=request('登录 '+who,'/auth/login',method='POST',data=dict(username=who,password='123456'))['token']
 for who,allowed in [('teacher1',{5,7}),('teacher2',{6}),('student2',{5}),('student4',{7})]:
  page=request(who+' 伪造参数分页','/students?page=1&size=1&studentId=6&teacherId=3',who)
  verify(who+' 分页total保持范围',page['total']==len(allowed) and len(page['records'])==1 and page['records'][0]['student_id'] in allowed)
  tail=request(who+' 尾页','/students?page=2147483647&size=200',who)
  verify(who+' 极大页码不溢出',tail['records']==[] and tail['total']==len(allowed))
  for path in ['/materials','/guidance','/reviews','/defenses','/scores','/archives']:
   rows=request(who+' 范围 '+path,path,who)
   verify(who+' 行数据归属 '+path,all(row.get('student_id') in allowed or row.get('student_id') is None for row in rows))
 for path in ['/system/users','/system/roles','/system/permissions','/system/logs']:
  request('学生管理越权 '+path,path,'student2',expected=403)
 request('教师日志越权','/system/logs','teacher1',expected=403)
 request('跨教师文件下载','/files/1','teacher2',expected=403)
 request('同学文件下载','/files/1','student4',expected=403)
 for suffix in ['page=0&size=20','page=1&size=0','page=1&size=201','page=x&size=20']:
  request('非法分页 '+suffix,'/students?'+suffix,'admin',expected=400)
 request('拒绝统计维度注入','/statistics?groupBy=major%20OR%201=1','admin',expected=400)
 request('拒绝统计编号注入','/statistics?teacherId=2%20OR%201=1','admin',expected=400)
 page=request('管理员统一分页','/topics?page=1&size=1','admin')
 verify('分页字段与记录数',set(page)=={'records','total','page','size'} and page['page']==1 and page['size']==1 and len(page['records'])==1)
 logs=request('日志落库','/system/logs','admin')
 verify('AOP登录记录存在',any(row['action']=='登录' and row['detail']=='AuthServiceImpl.login' for row in logs))
 verify('日志不泄露密码与令牌',all('123456' not in str(row) and all(token not in str(row) for token in tokens.values()) for row in logs))
except Exception as exc:error=str(exc)
finally:
 for who in list(tokens):
  try:request('退出 '+who,'/auth/logout',who,method='POST')
  except Exception as exc:error=error or str(exc)
 for pattern in ['*.response','*.request']:
  for p in OUT.glob(pattern):p.unlink()
 result=dict(time=datetime.now().isoformat(timespec='seconds'),total=len(checks),passed=sum(c['passed'] for c in checks),error=error,checks=checks)
 (OUT/'results.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8')
 print(json.dumps({k:v for k,v in result.items() if k!='checks'},ensure_ascii=False),flush=True)
raise SystemExit(1 if error else 0)
