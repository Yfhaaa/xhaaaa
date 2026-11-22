/** Run only against a disposable demo instance: creates one pending selection. */
import { chromium, request } from '../run/browser-tools/node_modules/playwright/index.mjs';
import fs from 'node:fs/promises';
const base=process.env.GDPM_TEST_FRONTEND_URL;
if(!base)throw Error('Set GDPM_TEST_FRONTEND_URL to a disposable demo instance');
const out='run/browser-pending';
await fs.mkdir(out,{recursive:true});
const api=await request.newContext();
const login=await api.post(base+'/api/auth/login',{data:{username:'student1',password:'123456'}});
const token=(await login.json()).data.token;
const headers={Authorization:'Bearer '+token};
const existing=(await (await api.get(base+'/api/selections',{headers})).json()).data;
if(!existing.some(r=>r.topic_id===1 && r.status==='待审核')) {
  const applied=await api.post(base+'/api/topics/1/apply',{headers});
  if(applied.status()!==200)throw Error('Fresh demo instance required for pending selection');
}
await api.post(base+'/api/auth/logout',{headers:{Authorization:'Bearer '+token}});
await api.dispose();
const browser=await chromium.launch({channel:'chrome',headless:true});
const checks=[];
try {
 for(const width of [1024,1366,1920]) {
  for(const who of ['teacher1','teacher2']) {
   const context=await browser.newContext({viewport:{width,height:1000}});
   const page=await context.newPage();
   await page.goto(base+'/login');
   await page.getByPlaceholder('请输入学号或教师账号').fill(who);
   await page.getByPlaceholder('请输入登录密码').fill('123456');
   await page.getByRole('button',{name:'进入系统 →'}).click();
   await page.waitForURL(base+'/');
   await page.goto(base+(who==='teacher1'?'/selections':'/materials/midterm'));
   await page.waitForLoadState('networkidle');
   await page.waitForTimeout(300);
   const expected=who==='teacher1'?['确认','退回']:['下载','详情','通过','退回'];
   const row=page.locator('.el-table__row').filter({hasText:who==='teacher1'?'刘冉':'王悦'});
   await row.waitFor({state:'visible'});
   const cell=row.locator('td.el-table-fixed-column--right');
   for(const name of expected){
    const button=cell.getByRole('button',{name,exact:true});
    await button.click({trial:true});
    const ok=await button.evaluate(el=>{
      const r=el.getBoundingClientRect(),c=el.closest('td').getBoundingClientRect();
      return r.left>=c.left && r.right<=c.right && r.right<=innerWidth;
    });
    checks.push({width,who,button:name,passed:ok});
    if(!ok)throw Error('Clipped pending action');
   }
   await page.screenshot({path:out+'/'+width+'-'+who+'.png',fullPage:true});
   // Verify that the actual review prompt can be opened within the viewport.
   await cell.getByRole('button',{name:who==='teacher1'?'确认':'通过',exact:true}).click();
   const dialog=page.getByRole('dialog');
   await dialog.waitFor({state:'visible'});
   const b=await dialog.boundingBox();
   checks.push({width,who,dialog:true,passed:b.x>=0 && b.x+b.width<=width});
   await dialog.getByRole('button',{name:'取消',exact:true}).click();
   await page.getByText('退出',{exact:true}).click();
   await context.close();
  }
 }
}finally{
 await browser.close();
 const result={total:checks.length,passed:checks.filter(c=>c.passed).length,checks};
 await fs.writeFile(out+'/results.json',JSON.stringify(result,null,2));
 console.log(JSON.stringify({total:result.total,passed:result.passed}));
 if(result.total!==result.passed)process.exitCode=1;
}
