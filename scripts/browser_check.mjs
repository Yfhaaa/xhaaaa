/** Browser regression: real Chromium, three widths, every role page and tab. */
import { chromium } from '../run/browser-tools/node_modules/playwright/index.mjs';
import fs from 'node:fs/promises';
import path from 'node:path';
const base = process.env.GDPM_FRONTEND_URL || 'http://127.0.0.1:5173';
const out = path.resolve('run/browser-check');
await fs.mkdir(out, {recursive:true});
const browser = await chromium.launch({channel:'chrome',headless:true});
const results = [], errors = [];
const pages = ['/', '/topics', '/materials', '/guidance', '/defense', '/statistics', '/notices'];
async function inspect(page, label, screenshot=false) {
  await page.locator('.el-loading-mask:visible').waitFor({state:'hidden'}).catch(()=>{});
  await page.waitForTimeout(200);
  const evidence = await page.evaluate(() => {
    const issues=[];
    const width=document.documentElement.clientWidth;
    if (document.documentElement.scrollWidth>width+1) issues.push('page overflow '+document.documentElement.scrollWidth+'/'+width);
    for (const table of document.querySelectorAll('.el-table')) {
      if (!table.getBoundingClientRect().height) continue;
      const rect=table.getBoundingClientRect();
      if (rect.right>width+1) issues.push('table outside viewport');
      for (const header of table.querySelectorAll('th')) {
        if (header.textContent.trim()==='操作' && !header.classList.contains('el-table-fixed-column--right')) {
          // The system audit log has a data column named 操作, rather than action buttons.
          if (!table.querySelector('td button')) continue;
          issues.push('action header not pinned');
        }
      }
      for (const cell of table.querySelectorAll('td.el-table-fixed-column--right')) {
        const c=cell.getBoundingClientRect();
        if (c.left<rect.left-1 || c.right>rect.right+1) issues.push('pinned cell outside table');
        for (const button of cell.querySelectorAll('button')) {
          const b=button.getBoundingClientRect();
          if (!b.width || !b.height) continue;
          if (b.left<c.left-1 || b.right>c.right+1) issues.push('clipped action '+button.textContent.trim());
          if (b.top>=0 && b.bottom<=innerHeight) {
            const hit=document.elementFromPoint(b.left+b.width/2,b.top+b.height/2);
            if (hit && !button.contains(hit)) issues.push('occluded action '+button.textContent.trim());
          }
        }
      }
    }
    return {width,scrollWidth:document.documentElement.scrollWidth,issues};
  });
  results.push({name:label,...evidence,passed:evidence.issues.length===0});
  if (screenshot || evidence.issues.length) {
    await page.screenshot({path:path.join(out,label.replace(/[^a-zA-Z0-9_-]/g,'_')+'.png'),fullPage:true});
  }
  if (evidence.issues.length) console.log('FAIL',label,JSON.stringify(evidence.issues));
}
try {
  for (const width of [1024,1366,1920]) {
    for (const who of ['admin','teacher1','student2','student4']) {
      const context=await browser.newContext({viewport:{width,height:1000}});
      const page=await context.newPage();
      page.on('pageerror',e=>errors.push({width,who,message:e.message}));
      page.on('response',r=>{if(r.url().includes('/api/') && r.status()>=400)errors.push({width,who,status:r.status(),path:new URL(r.url()).pathname});});
      await page.goto(base+'/login');
      await inspect(page,`${width}-${who}-login`);
      await page.getByPlaceholder('请输入学号或教师账号').fill(who);
      await page.getByPlaceholder('请输入登录密码').fill('123456');
      await page.getByRole('button',{name:'进入系统 →'}).click();
      await page.waitForURL(base+'/');
      for (const route of [...pages,...(who==='admin'?['/system']:[])]) {
        await page.goto(base+route);
        await page.waitForLoadState('networkidle');
        const label=`${width}-${who}-${route.slice(1)||'dashboard'}`;
        await inspect(page,label,width===1024 && who==='admin' || route==='/topics');
        const tabs=page.getByRole('tab');
        for (let i=0;i<await tabs.count();i++) {
          await tabs.nth(i).click();
          await inspect(page,label+'-tab'+i,width===1024 && route==='/topics');
          if ((await tabs.nth(i).innerText()).includes('专业 / 班级')) {
            const baseRadios=page.locator('.el-radio-button:visible');
            for(let j=0;j<await baseRadios.count();j++) {
              await baseRadios.nth(j).click();
              await inspect(page,label+'-base'+j);
            }
          }
          // Verify action cells at both horizontal scroll endpoints.
          await page.locator('.el-table .el-scrollbar__wrap:visible').evaluateAll(elements=>elements.forEach(el=>{el.scrollLeft=el.scrollWidth;}));
          await inspect(page,label+'-tab'+i+'-scroll');
        }
        const radios=page.locator('.el-radio-button:visible');
        for (let i=0;i<await radios.count();i++) {
          await radios.nth(i).click();
          await inspect(page,label+'-radio'+i);
        }
      }
      await page.getByText('退出',{exact:true}).click();
      await page.waitForURL(base+'/login');
      await context.close();
      console.log('Checked',width,who);
    }
  }
} finally {
  await browser.close();
  const summary={total:results.length,passed:results.filter(r=>r.passed).length,errors,checks:results};
  await fs.writeFile(path.join(out,'results.json'),JSON.stringify(summary,null,2));
  console.log(JSON.stringify({total:summary.total,passed:summary.passed,errors}));
  if(summary.total!==summary.passed || errors.length)process.exitCode=1;
}
