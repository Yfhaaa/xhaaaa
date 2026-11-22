/** All independent pages, all roles, three viewport widths, real Chromium. */
import { chromium } from '../run/browser-tools/node_modules/playwright/index.mjs';
import { pages } from '../frontend/src/navigation.js';
import fs from 'node:fs/promises';
import path from 'node:path';
const base=process.env.GDPM_FRONTEND_URL || 'http://127.0.0.1:15173';
const focus=process.argv.includes('--focus');
const out=path.resolve(focus?'run/browser-round3-focus':'run/browser-round3'); await fs.mkdir(out,{recursive:true});
const shots=path.resolve('run/screenshots-round3'); await fs.mkdir(shots,{recursive:true});
const browser=await chromium.launch({channel:'chrome',headless:true});
const results=[],errors=[];
async function inspect(page,label,screenshot=false) {
 await page.locator('.el-loading-mask:visible').first().waitFor({state:'hidden',timeout:10000}).catch(()=>{});
 await page.waitForTimeout(100);
 const evidence=await page.evaluate(()=>{
  const issues=[],width=document.documentElement.clientWidth;
  if(document.documentElement.scrollWidth>width+1)issues.push('page overflow');
  for(const table of document.querySelectorAll('.el-table')) {
   if(!table.getBoundingClientRect().height)continue;
   const rect=table.getBoundingClientRect();if(rect.right>width+1)issues.push('table outside viewport');
   for(const header of table.querySelectorAll('th')) {
    if(header.textContent.trim()==='操作' && table.querySelector('td button') &&
     !header.classList.contains('el-table-fixed-column--right'))issues.push('action not pinned');
   }
   for(const cell of table.querySelectorAll('td.el-table-fixed-column--right')) {
    const c=cell.getBoundingClientRect();
    for(const button of cell.querySelectorAll('button')) {
     const b=button.getBoundingClientRect();if(!b.width || !b.height)continue;
     if(b.left<c.left-1 || b.right>c.right+1)issues.push('clipped action '+button.textContent.trim());
    }
   }
  }
  for(const dialog of document.querySelectorAll('.el-dialog')) {
   const r=dialog.getBoundingClientRect(); if(r.width && (r.left<0||r.right>width+1))issues.push('dialog overflow');
  }
  return {width,scrollWidth:document.documentElement.scrollWidth,issues};
 });
 results.push({name:label,...evidence,passed:!evidence.issues.length});
 if(screenshot||evidence.issues.length)await page.screenshot({path:path.join(screenshot?shots:out,label+'.png'),fullPage:true});
 if(evidence.issues.length)console.log('FAIL',label,JSON.stringify(evidence.issues));
}
try {
 for(const width of [1024,1366,1920]) {
  for(const who of (focus?['admin','teacher2','student4']:['admin','teacher1','teacher2','student2','student4'])) {
   const context=await browser.newContext({viewport:{width,height:1000}}),page=await context.newPage();
   page.on('pageerror',e=>errors.push({width,who,error:e.message}));
   page.on('response',r=>{if(r.url().includes('/api/')&&r.status()>=400)errors.push({width,who,status:r.status(),path:new URL(r.url()).pathname});});
   await page.goto(base+'/login');
   await page.getByPlaceholder('请输入学号或教师账号').fill(who);
   await page.getByPlaceholder('请输入登录密码').fill('123456');
   await page.getByRole('button',{name:'进入系统 →'}).click(); await page.waitForURL(base+'/');
   const identity=await page.evaluate(async()=>{const r=await fetch('/api/auth/me',{headers:{Authorization:'Bearer '+sessionStorage.getItem('gdpm-token')}});return(await r.json()).data;});
   const allowed=pages.filter(p=>(!p.permission||identity.permissions.includes(p.permission))&&(!p.roles.length||p.roles.includes(identity.roles[0])));
   for(const route of (focus ? allowed.filter(p => ['/system/users','/system/roles','/system/classes','/system/cohorts','/system/workflow','/system/logs','/board','/statistics','/missing-materials','/archives','/messages','/profile','/scores','/my-project'].includes(p.path)) : allowed)) {
    await page.goto(base+route.path);await page.waitForLoadState('networkidle');
    if(new URL(page.url()).pathname!==route.path)errors.push({width,who,path:route.path,error:'unexpected redirect'});
    const label=`${width}-${who}-${route.path.slice(1).replaceAll('/','-')||'dashboard'}`;
    await inspect(page,label,who==='admin'||who==='teacher2'&&route.path==='/my-reviews'||who==='student4'&&route.path==='/my-project');
    await page.locator('.el-table .el-scrollbar__wrap').evaluateAll(els=>els.forEach(el=>{el.scrollLeft=el.scrollWidth;}));
    await inspect(page,label+'-scroll');
    // Check visible dialog dimensions for every role and action available on each independent page.
    const buttons=page.locator('main .page-heading button, main .toolbar button').filter({hasText:/申报课题|新建答辩组|安排学生|录入成绩|登记查重|申请变更|分配 \/|记录指导|新增用户/});
    if(await buttons.count()) {
     await buttons.first().click();
     if(await page.locator('.el-dialog:visible').count()) { await inspect(page,label+'-dialog');
      await page.locator('.el-dialog:visible .el-dialog__headerbtn').click(); }
    }
   }
   if(who==='student4') {
    await page.goto(base+'/system/users');await page.waitForLoadState('networkidle');
    results.push({name:`${width}-route-role-guard`,passed:new URL(page.url()).pathname==='/'});
   }
   await page.goto(base+'/profile');await page.reload();await page.waitForLoadState('networkidle');
   results.push({name:`${width}-${who}-refresh-login`,passed:new URL(page.url()).pathname==='/profile'&&await page.locator('h1').innerText()==='个人中心'});
   await page.getByText('退出',{exact:true}).click();await page.waitForURL(base+'/login');
   await context.close();console.log('Checked',width,who,allowed.length);
  }
 }
} catch(e) { errors.push({error:e.stack}); }
finally {
 await browser.close();const summary={total:results.length,passed:results.filter(r=>r.passed).length,errors,checks:results};
 await fs.writeFile(path.join(out,'results.json'),JSON.stringify(summary,null,2));
 console.log(JSON.stringify({total:summary.total,passed:summary.passed,errors}));
 if(summary.total!==summary.passed||errors.length)process.exitCode=1;
}
