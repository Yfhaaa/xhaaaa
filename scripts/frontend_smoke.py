"""Read-only SPA/assets checks plus login through the actual frontend reverse proxy."""
import hashlib,json,os,pathlib,re,shutil,subprocess,sys,tempfile
sys.stdout.reconfigure(encoding='utf-8',errors='backslashreplace')
PROJECT_DIR=pathlib.Path(__file__).resolve().parent.parent
os.chdir(PROJECT_DIR)
out=PROJECT_DIR/'run/frontend-smoke';out.mkdir(parents=True,exist_ok=True)
checks=[]
curl=shutil.which('curl.exe')
env=os.environ.copy()
for key in list(env):
    if key.lower() in ('http_proxy','https_proxy','all_proxy'):env.pop(key,None)
def call(label,path,method='GET',data=None,token=None,status=200):
    args=[curl,'--noproxy','*','-sS','--max-time','20','-X',method,os.environ.get('GDPM_FRONTEND_URL','http://127.0.0.1:5173')+path,'-o','run/frontend-smoke/body.tmp','-w','%{http_code}']
    if token:args+=['-H','Authorization: Bearer '+token]
    if data is not None:
        (out/'request.tmp').write_text(json.dumps(data,ensure_ascii=False),encoding='utf-8')
        args+=['-H','Content-Type: application/json','--data-binary','@run/frontend-smoke/request.tmp']
    p=subprocess.run(args,env=env,capture_output=True)
    actual=int(p.stdout.decode() or '0');ok=p.returncode==0 and actual==status
    checks.append(dict(name=label,path=path,status=actual,expected=status,passed=ok))
    print('PASS' if ok else 'FAIL',label,actual,flush=True)
    if not ok:raise AssertionError(label)
    body=(out/'body.tmp').read_bytes()
    if path.startswith('/api/') and status==200:
        value=json.loads(body)
        if isinstance(value,dict) and value.get('code')==200 and 'data' in value:
            return json.dumps(value['data'],ensure_ascii=False).encode('utf-8')
    return body
error=None
try:
    for path in ['/','/login','/topics','/materials','/system']:
        body=call('SPA路由 '+path,path)
        assert b'id="app"' in body
    for asset in sorted((PROJECT_DIR/'frontend/dist/assets').iterdir()):
        body=call('静态资源 '+asset.name,'/assets/'+asset.name)
        assert hashlib.sha256(body).digest()==hashlib.sha256(asset.read_bytes()).digest()
    value=json.loads(call('前端代理健康检查','/api/health'))
    assert value['status']=='UP'
    for who,role,count in [('admin','ADMIN',4),('teacher1','TEACHER',2),('student1','STUDENT',1)]:
        token=json.loads(call('前端代理登录 '+who,'/api/auth/login','POST',dict(username=who,password='123456')))['token']
        me=json.loads(call('前端代理RBAC '+who,'/api/auth/me',token=token));assert role in me['roles']
        dash=json.loads(call('前端代理看板 '+who,'/api/dashboard',token=token));assert dash['studentCount']==count
        if who=='student1':call('前端代理越权拦截','/api/system/users',token=token,status=403)
        call('前端代理退出 '+who,'/api/auth/logout','POST',token=token)
except Exception as e:error=str(e)
finally:
    for p in out.glob('*.tmp'):p.unlink()
    result=dict(total=len(checks),passed=sum(r['passed'] for r in checks),error=error,checks=checks)
    (out/'results.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8')
    print(json.dumps({k:v for k,v in result.items() if k!='checks'},ensure_ascii=False),flush=True)
sys.exit(1 if error else 0)
