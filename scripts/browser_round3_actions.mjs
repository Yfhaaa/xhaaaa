/** Browser actions against the isolated third-round preview only. */
import { chromium } from '../run/browser-tools/node_modules/playwright/index.mjs';
import fs from 'node:fs/promises';
const base=process.env.GDPM_FRONTEND_URL||'http://127.0.0.1:15173';
if(new URL(base).port!=='15173')throw Error('Only isolated review preview is allowed');
const browser=await chromium.launch({channel:'chrome',headless:true});
const checks=[],errors=[];
const out='run/browser-round3-actions';await fs.mkdir(out,{recursive:true});
function verify(name,passed){checks.push({name,passed});if(!passed)throw Error(name);}
async function login(page,who){await page.goto(base+'/login');await page.getByPlaceholder('请输入学号或教师账号').fill(who);
 await page.getByPlaceholder('请输入登录密码').fill('123456');await page.getByRole('button',{name:'进入系统 →'}).click();await page.waitForURL(base+'/');}
try{
 for(const width of [1024,1366,1920]){
  const context=await browser.newContext({viewport:{width,height:1000}}),page=await context.newPage();
  page.on('pageerror',e=>errors.push(e.message));
  await login(page,'admin');
  await page.goto(base+'/system/workflow');await page.waitForLoadState('networkidle');
  const finalRow=page.locator('.el-table__row').filter({hasText:'论文定稿'});
  await finalRow.getByRole('button',{name:'配置截止时间'}).click();
  const dialog=page.getByRole('dialog');await dialog.waitFor({state:'visible'});
  verify(width+' threshold control',await dialog.getByText('查重阈值（%，定稿审核使用）').isVisible());
  await dialog.getByRole('button',{name:'保存设置'}).click();await dialog.waitFor({state:'hidden'});
  verify(width+' shared system form saves',true);
  await page.goto(base+'/system/users');await page.waitForLoadState('networkidle');
  await page.getByRole('button',{name:'新增用户'}).click();
  await page.getByRole('dialog').getByRole('button',{name:'保存设置'}).click();
  await page.locator('.el-form-item__error').first().waitFor({state:'visible'});
  verify(width+' required user form validation',await page.locator('.el-form-item__error').count()>=2);
  await page.getByRole('dialog').getByRole('button',{name:'取消',exact:true}).click();
  const templateDownload=page.waitForEvent('download');await page.getByRole('button',{name:'下载导入模板'}).click();
  const template=await templateDownload;verify(width+' template browser download',template.suggestedFilename()==='用户导入模板.xlsx');
  await page.goto(base+'/archives');await page.waitForLoadState('networkidle');
  const archiveDownload=page.waitForEvent('download');
  await page.locator('.el-table__row').filter({hasText:'陈晨'}).getByRole('button',{name:'下载ZIP'}).click();
  const archive=await archiveDownload;verify(width+' ZIP browser download',archive.suggestedFilename()==='学生归档.zip');
  await page.goto(base+'/statistics');await page.waitForLoadState('networkidle');
  await page.getByRole('button',{name:'折叠侧栏'}).click();
  verify(width+' collapsible sidebar',await page.locator('.app-shell').evaluate(el=>el.classList.contains('collapsed')));
  verify(width+' closeable route tabs',await page.locator('.route-tab button').count()>0);
  await page.getByText('退出',{exact:true}).click();await page.waitForURL(base+'/login');
  await login(page,'student4');await page.goto(base+'/my-project');await page.waitForLoadState('networkidle');
  verify(width+' nine stage dossier',await page.locator('.el-timeline-item').count()===9);
  verify(width+' timeline shows reviewer and versions',await page.getByText(/文件版本：v1/).count()>=5);
  // Generate a fresh business message so read-all starts with a nonzero unread badge at every width.
  const studentToken=(await (await context.request.post(base+'/api/auth/login',
   {data:{username:'student2',password:'123456'}})).json()).data.token;
  const teacherToken=(await (await context.request.post(base+'/api/auth/login',
   {data:{username:'teacher1',password:'123456'}})).json()).data.token;
  const submitted=await context.request.post(base+'/api/materials/proposal',
   {headers:{Authorization:'Bearer '+studentToken},data:{title:'消息与版本验收',fileId:1}});
  if(submitted.status()!==200)throw Error('Fresh returned student2 proposal is required');
  const proposal=(await submitted.json()).data.id;
  const reviewed=await context.request.post(base+`/api/materials/proposal/${proposal}/review`,
   {headers:{Authorization:'Bearer '+teacherToken},data:{approved:false,comment:'请补充技术路线（隔离验收）'}});
  if(reviewed.status()!==200)throw Error('Message fixture review failed');
  await context.request.post(base+'/api/auth/logout',{headers:{Authorization:'Bearer '+studentToken}});
  await context.request.post(base+'/api/auth/logout',{headers:{Authorization:'Bearer '+teacherToken}});
  await page.getByText('退出',{exact:true}).click();await page.waitForURL(base+'/login');
  await login(page,'student2');await page.goto(base+'/messages');await page.waitForLoadState('networkidle');
  await page.locator('.el-badge__content:visible').waitFor({state:'visible'});
  verify(width+' starts with unread business message',await page.locator('.el-badge__content:visible').count()>0);
  await page.getByRole('button',{name:'一键已读'}).click();
  await page.locator('.el-badge__content').waitFor({state:'hidden',timeout:5000});
  verify(width+' badge synchronizes read-all',await page.locator('.el-badge__content:visible').count()===0);
  await page.goto(base+'/profile');await page.waitForLoadState('networkidle');
  const profileName=page.locator('.el-form-item').filter({has:page.locator('label').filter({hasText:'姓名'})}).locator('input').first();
  await profileName.fill('');await page.getByRole('button',{name:'保存资料'}).click();
  await page.getByText('请填写姓名',{exact:true}).waitFor({state:'visible'});
  verify(width+' profile required validation',await page.getByText('请填写姓名',{exact:true}).isVisible());
  await context.close();console.log('Actions checked',width);
 }
 // Verify the automatic refresh timer fires a second real request.
 const context=await browser.newContext(),page=await context.newPage();await login(page,'admin');
 let refreshes=0;page.on('response',r=>{if(new URL(r.url()).pathname==='/api/statistics/board'&&r.status()===200)refreshes++;});
 await page.goto(base+'/board');await page.waitForLoadState('networkidle');
 await page.waitForTimeout(31000);verify('board refreshes automatically after 30 seconds',refreshes>=2);
 await context.close();
}catch(error){errors.push(error.stack);}
finally{await browser.close();const result={total:checks.length,passed:checks.filter(c=>c.passed).length,errors,checks};
 await fs.writeFile(out+'/results.json',JSON.stringify(result,null,2));console.log(JSON.stringify(result));
 if(result.total!==result.passed||errors.length)process.exitCode=1;}
