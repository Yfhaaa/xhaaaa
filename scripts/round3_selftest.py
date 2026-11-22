"""第三轮新增接口的 curl 回归补充，复用主自测的请求与脱敏证据。"""
import io, zipfile

def run(request, verify, login, tokens, sample, out):
    for who in ['admin','teacher1','teacher2','student2','student4']:
        request('新增消息查询 '+who,'GET','/messages',who)
    request('消息分页','GET','/messages?page=1&size=2','student2')
    messages=request('读取未读消息','GET','/messages?unread=true','student2')
    if messages:
        request('不能标记他人消息','POST',f'/messages/{messages[0]["id"]}/read','student4',expected=403)
    request('一键已读','POST','/messages/read-all','student2')
    verify('未读归零',request('未读复核','GET','/messages?unread=true','student2')==[])
    dossier=request('本人九节点档案','GET','/students/4/dossier','student1')
    verify('九节点与开题两版本',len(dossier['timeline'])==9 and len(next(n for n in dossier['timeline'] if n['code']=='proposal')['versions'])==2)
    request('档案跨学生隔离','GET','/students/4/dossier','student2',expected=403)
    request('档案跨教师隔离','GET','/students/4/dossier','teacher2',expected=403)
    board=request('真实数据大屏','GET','/statistics/board','admin')
    verify('大屏六类指标齐全',all(k in board for k in ['stages','groups','teacherLoad','scoreDistribution','trend','warnings']) and len(board['trend'])==30)
    request('大屏管理员权限','GET','/statistics/board','teacher1',expected=403)
    for kind in ['scores','statistics','missing']:
        value=request('EasyExcel导出 '+kind,'GET','/exports/'+kind,'admin')
        verify('有效xlsx '+kind,b'xl/workbook.xml' in value and zipfile.is_zipfile(io.BytesIO(value)))
    template=request('用户模板下载','GET','/system/users/template','admin')
    verify('模板为xlsx',zipfile.is_zipfile(io.BytesIO(template)))
    request('学生不能下载用户模板','GET','/system/users/template','student1',expected=403)
    request('拒绝非Excel导入','POST','/system/users/import','admin',upload=sample,expected=400)
    # Generate real import fixtures with the Python standard library's XML zip support.
    def excel(rows, path):
        from xml.sax.saxutils import escape
        xml='<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetData>'
        for index,row in enumerate(rows,1):
            xml+=f'<row r="{index}">'+''.join(f'<c r="{chr(65+i)}{index}" t="inlineStr"><is><t>{escape(str(v))}</t></is></c>' for i,v in enumerate(row))+'</row>'
        xml+='</sheetData></worksheet>'
        with zipfile.ZipFile(path,'w') as z:
            z.writestr('[Content_Types].xml','<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/><Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/><Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/></Types>')
            z.writestr('_rels/.rels','<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/></Relationships>')
            z.writestr('xl/workbook.xml','<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"><sheets><sheet name="用户" sheetId="1" r:id="rId1"/></sheets></workbook>')
            z.writestr('xl/_rels/workbook.xml.rels','<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/></Relationships>')
            z.writestr('xl/worksheets/sheet1.xml',xml)
    fixture=out/'import.xlsx'
    excel([['账号','姓名','初始密码','角色编号','专业编号','班级编号','届次'],
           ['import_student','导入学生','Import1234','3','1','1','2026'],
           ['bad_student','错误班级','Import1234','3','1','2','2026']],fixture)
    imported=request('用户逐行导入结果','POST','/system/users/import','admin',upload=fixture)
    verify('一行成功一行失败且行号正确',imported['success']==1 and imported['failed']==1 and [r['row'] for r in imported['rows']]==[2,3])
    fixture.unlink()
    raw=request('单学生完整ZIP','GET','/archives/download?studentId=4','student1')
    with zipfile.ZipFile(io.BytesIO(raw)) as z:
        verify('ZIP含五材料成绩单清单',len([n for n in z.namelist() if '/'+n.split('/')[-1] and '-v' in n])==5 and any('成绩单.xlsx' in n for n in z.namelist()) and any('归档清单.txt' in n for n in z.namelist()))
    request('跨学生ZIP被拒','GET','/archives/download?studentId=4','student2',expected=403)
    request('未完成项目ZIP被拒','GET','/archives/download?studentId=5','admin',expected=400)
    raw=request('班级批量ZIP','GET','/archives/download?classId=1','admin')
    verify('班级包含两个已完成学生',b'student-4/' in raw and b'student-7/' in raw)
    # Three-level topic change on an unfinished student, preserving the original demo assignment elsewhere.
    target=request('新教师申报可变更课题','POST','/topics','teacher2',dict(title='课题变更验证',description='跨指导教师变更',majorId=1,capacity=1))['id']
    request('审核可变更课题','POST',f'/topics/{target}/review','admin',dict(approved=True,comment='同意'))
    change=request('学生申请课题变更','POST','/topic-changes','student2',dict(newTopicId=target,reason='研究方向调整'))['id']
    request('禁止重复在途变更','POST','/topic-changes','student2',dict(newTopicId=target,reason='重复'),400)
    request('管理员禁止跳级','POST',f'/topic-changes/{change}/approve','admin',dict(approved=True,comment='跳级'),403)
    for who in ['teacher1','teacher2','admin']:
        request('三级审批 '+who,'POST',f'/topic-changes/{change}/approve',who,dict(approved=True,comment='同意变更'))
    assignment=request('新教师指导关系','GET','/students','teacher2')
    verify('变更后分配与新教师一致',any(s['student_id']==5 and s['topic_id']==target and s['teacher_id']==3 for s in assignment))
    dossier=request('新教师继承完整历史档案','GET','/students/5/dossier','teacher2')
    verify('变更后选题节点和原指导记录完整',dossier['timeline'][0]['status']=='已通过' and len(dossier['guidance'])>=1)
    request('原教师失去档案访问范围','GET','/students/5/dossier','teacher1',expected=403)
    changed=request('变更留痕','GET','/topic-changes','admin')
    verify('三级审核历史完整',len(next(c for c in changed if c['id']==change)['history'])==3)
    request('已结束变更不能再审批','POST',f'/topic-changes/{change}/approve','admin',dict(approved=True,comment='重复'),400)
    # Competing three-level changes must share the same capacity lock as double selection.
    old=request('创建变更竞争原课题','POST','/topics','teacher1',dict(title='变更并发原课题',description='测试旧分配',majorId=1,capacity=2))['id']
    dest=request('创建变更竞争目标','POST','/topics','teacher2',dict(title='变更并发目标课题',description='测试名额上限',majorId=1,capacity=1))['id']
    for tid in [old,dest]: request('审核变更竞争课题','POST',f'/topics/{tid}/review','admin',dict(approved=True,comment='同意'))
    changes=[]
    for who in ['change_student_a','change_student_b']:
        request('创建变更竞争学生','POST','/system/users','admin',dict(username=who,name=who,password='123456',roleId=3,majorId=1,classId=1,cohort='2026',enabled=1))
        login(who)
        sid=request('竞争学生原选题','POST',f'/topics/{old}/apply',who)['id']
        request('确认竞争学生原课题','POST',f'/selections/{sid}/review','teacher1',dict(approved=True,comment='同意'))
        cid=request('并发变更申请','POST','/topic-changes',who,dict(newTopicId=dest,reason='研究方向调整'))['id']
        for teacher in ['teacher1','teacher2']:
            request('并发变更教师审批','POST',f'/topic-changes/{cid}/approve',teacher,dict(approved=True,comment='同意'))
        changes.append(cid)
    import concurrent.futures
    def final_approve(cid):
        try:
            request('并发终审','POST',f'/topic-changes/{cid}/approve','admin',dict(approved=True,comment='同意'),record=False)
            return 200
        except AssertionError as error:
            if ': 400 ' in str(error): return 400
            raise
    with concurrent.futures.ThreadPoolExecutor(max_workers=2) as pool:
        statuses=list(pool.map(final_approve,changes))
    verify('变更并发名额只成功一人',sorted(statuses)==[200,400])
    topics=request('核查变更并发名额','GET','/topics','admin')
    verify('变更目标实际分配人数为1',next(t for t in topics if t['id']==dest)['selected_count']==1)
    # Password updates are restricted to this isolated selftest database.
    request('旧密码错误被拒','POST','/profile/password','student2',dict(oldPassword='wrong',newPassword='Changed1234'),400)
    request('弱密码被拒','POST','/profile/password','student2',dict(oldPassword='123456',newPassword='12345678'),400)
    request('个人资料格式校验','PUT','/profile','student2',dict(name='李明',email='bad',phone=''),400)
    request('更新本人资料','PUT','/profile','student2',dict(name='李明',email='student2@example.edu',phone='13800000000'))
    me=request('核验个人资料','GET','/auth/me','student2')
    verify('资料已保存且密码不外泄',me['email']=='student2@example.edu' and 'password' not in me)
    request('修改本人密码','POST','/profile/password','student2',dict(oldPassword='123456',newPassword='Changed1234'))
    request('换密后旧令牌失效','GET','/auth/me','student2',expected=401)
    request('新密码可重新登录','POST','/auth/login',data=dict(username='student2',password='Changed1234'))
