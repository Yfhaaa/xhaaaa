import sys
sys.stdout.reconfigure(encoding="utf-8")
"""Read-only structural, formatting, schema parity and delivery checks."""
from pathlib import Path
import hashlib,json,re,struct,zipfile
from datetime import datetime
ROOT=Path(__file__).resolve().parent.parent
PROJECT=ROOT
JAVA=PROJECT/'backend/src/main/java/edu/gengdan/gdpm'
checks=[]
def check(name,ok,details=None):
 row=dict(name=name,passed=bool(ok))
 if details is not None:row['details']=details
 checks.append(row)
 print(('PASS ' if ok else 'FAIL ')+name,flush=True)
 return ok
sources=[f for directory in ['backend/src','frontend/src','sql'] for f in (PROJECT/directory).rglob('*') if f.is_file() and f.suffix in ['.java','.vue','.js','.xml','.sql']]
sources.append(PROJECT/'backend/pom.xml')
long_lines=[];tabs=[];damaged=[]
for p in sources:
 s=p.read_text(encoding='utf-8-sig')
 for i,line in enumerate(s.splitlines(),1):
  if len(line)>120:long_lines.append(f'{p.relative_to(PROJECT)}:{i}:{len(line)}')
  if '\t' in line:tabs.append(f'{p.relative_to(PROJECT)}:{i}')
  if re.search(r'\?{3,}',line):damaged.append(f'{p.relative_to(PROJECT)}:{i}')
check('Java/Vue/JS/XML/SQL 单行不超过120字符',not long_lines,long_lines)
check('源码使用空格缩进',not tabs,tabs)
check('源码无中文编码损坏占位符',not damaged,damaged)
schema=(PROJECT/'backend/src/main/resources/db/schema.sql').read_text(encoding='utf-8-sig')
tables=set(re.findall(r'CREATE TABLE IF NOT EXISTS\s+(\w+)',schema,re.I))
entities={re.search(r'@TableName\("(\w+)"\)',p.read_text(encoding='utf-8')).group(1):p for p in (JAVA/'entity').glob('*.java') if '@TableName' in p.read_text(encoding='utf-8')}
check('26张表全部有独立@TableName实体',len(tables)==26 and set(entities)==tables,sorted(entities))
check('所有实体显式声明@TableId',all('@TableId' in p.read_text(encoding='utf-8') for p in entities.values()))
check('26个实体均有BaseMapper',all((JAVA/'mapper'/(p.stem+'Mapper.java')).exists() and 'extends BaseMapper<' in (JAVA/'mapper'/(p.stem+'Mapper.java')).read_text(encoding='utf-8') for p in entities.values()))
main='\n'.join(p.read_text(encoding='utf-8') for p in JAVA.rglob('*.java'))
check('删除Db及手写JDBC业务访问',not (JAVA/'core/Db.java').exists() and not re.search(r'JdbcTemplate|\bDb\b|java\.sql\.PreparedStatement',main))
xml=(PROJECT/'backend/src/main/resources/mapper/ReportMapper.xml').read_text(encoding='utf-8')
check('跨表统计位于XML且无请求字符串拼接', '<select id="statistics"' in xml and '${' not in xml)
check('实际Shiro Spring过滤链与角色权限注解存在',all(word in main for word in ['ShiroFilterFactoryBean','AuthorizationAttributeSourceAdvisor','extends AuthorizingRealm','extends AccessControlFilter','@RequiresRoles','@RequiresPermissions']))
check('操作日志由AOP统一拦截', '@Around("@annotation(annotation)")' in main and '@OperLog(' in main and 'auth.log(' not in main)
for module in ['User','Role','Permission','BaseData','Topic','Selection','TaskBook','Proposal','Midterm','Thesis','Guidance','Review','Defense','Score','Archive','Statistics','Notification','Log']:
 check(module+'模块有Service契约与实现',(JAVA/'service'/(module+'Service.java')).exists() and (JAVA/'service/impl'/(module+'ServiceImpl.java')).exists())
check('前端六个模块目录完整',all((PROJECT/'frontend/src/views'/name).is_dir() for name in ['system','topic','material','guidance','defense','statistics']))
check('前端API按模块拆分',all((PROJECT/'frontend/src/api'/(name+'.js')).exists() for name in ['auth','system','topic','material','guidance','defense','statistics','notification','http']))
def sql_tokens(sql):
 sql=re.sub(r'--[^\n]*','',sql)
 return [t if t.startswith("'") else t.upper() for t in re.findall(r"'(?:''|[^'])*'|[A-Za-z_][A-Za-z0-9_]*|[0-9]+(?:\.[0-9]+)?|[^\s]",sql)]
init=(PROJECT/'sql/init.sql').read_text(encoding='utf-8-sig')
init=init[re.search(r'CREATE TABLE',init,re.I).start():]
data=(PROJECT/'backend/src/main/resources/db/data.sql').read_text(encoding='utf-8-sig')
check('MySQL初始化SQL与dev结构及演示数据一致',sql_tokens(init)==sql_tokens(schema+'\n'+data))
files=list(JAVA.rglob('*.java'))
statistics=dict(javaSourceFiles=len(files),javaClasses=sum(len(re.findall(r'\bclass\s+\w+',p.read_text(encoding='utf-8'))) for p in files),javaInterfaces=sum(len(re.findall(r'(?<!@)\binterface\s+\w+',p.read_text(encoding='utf-8'))) for p in files),javaEnums=sum(len(re.findall(r'\benum\s+\w+',p.read_text(encoding='utf-8'))) for p in files),javaAnnotations=sum(len(re.findall(r'@interface\s+\w+',p.read_text(encoding='utf-8'))) for p in files),tableEntities=len(entities),mappers=len(list((JAVA/'mapper').glob('*.java'))),baseMappers=sum('extends BaseMapper<' in p.read_text(encoding='utf-8') for p in (JAVA/'mapper').glob('*.java')),controllers=len(list((JAVA/'controller').glob('*Controller.java'))),vuePages=len(list((PROJECT/'frontend/src/views').rglob('*.vue'))),serviceInterfaces=len(list((JAVA/'service').glob('*.java'))))
jar=PROJECT/'backend/target/gdpm-system.jar'
artifacts={}
if jar.exists():
 artifacts['jarSha256']=hashlib.sha256(jar.read_bytes()).hexdigest()
 with zipfile.ZipFile(jar) as z:
  names=z.namelist();cl=z.read('BOOT-INF/classes/edu/gengdan/gdpm/GdpmApplication.class')
  artifacts['javaClassMajor']=struct.unpack('>H',cl[6:8])[0]
  check('产物编译目标为Java11',artifacts['javaClassMajor']==55)
  check('产物不包含原Db或集中控制器',not any(n.endswith('/core/Db.class') or n.endswith('/api/ApiController.class') for n in names))
check('未创建Git仓库',not (PROJECT/'.git').exists())
result=dict(time=datetime.now().isoformat(timespec='seconds'),total=len(checks),passed=sum(c['passed'] for c in checks),statistics=statistics,artifacts=artifacts,checks=checks)
(ROOT/'run/source-checks.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps({k:v for k,v in result.items() if k!='checks'},ensure_ascii=False,indent=2))
raise SystemExit(0 if result['passed']==result['total'] else 1)
