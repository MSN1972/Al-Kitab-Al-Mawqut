const {test}=require('node:test');
const assert=require('node:assert/strict');
const fs=require('node:fs');
const p=require('../prayer-core.js');
const day='2026-10-25', times=[300,720,930,1080,1170];
const events=p.getEvents({[day]:times},[15,15,5,5,15],[7,7,5,5,7]);
const clock=(min,seconds=0)=>p.epochInRiyadh(day,min)+seconds*1000;
test('prayer screen always wins over adhkar',()=>{assert.equal(p.screenAt(clock(315),events,4).mode,'quiet');assert.equal(p.screenAt(clock(321,59),events,4).mode,'quiet');});
test('post-prayer only after quiet ends',()=>{assert.equal(p.screenAt(clock(314),events,4).mode,'display');assert.equal(p.screenAt(clock(322),events,4).mode,'adhkar');});
test('automatic return after configurable duration',()=>{assert.equal(p.screenAt(clock(325,59),events,4).mode,'adhkar');assert.equal(p.screenAt(clock(326),events,4).mode,'display');});
test('disabled mode with zero minutes',()=>assert.equal(p.screenAt(clock(322),events,0).mode,'display'));
test('Isha after midnight works across date boundary',()=>{
 const e=p.getEvents({[day]:[300,720,930,1080,1438]},[15,15,5,5,6],[7,7,5,5,7]);
 const midnight=p.epochInRiyadh(p.dayAdd(day,1),0);
 assert.equal(p.screenAt(midnight+5*60000,e,4).mode,'quiet');
 assert.equal(p.screenAt(midnight+11*60000,e,4).mode,'adhkar');
 assert.equal(p.screenAt(midnight+15*60000,e,4).mode,'display');
});
test('source texts in distinct, static post-prayer screen',()=>{
 const html=fs.readFileSync('webos/index.html','utf8');
 assert.match(html,/id="post-prayer" class="post-prayer-screen hidden"/);
 assert.match(html,/صحيح مسلم \(591\)/);
 assert.match(html,/صحيح البخاري \(844\)/);
 assert.match(html,/صحيح مسلم \(597\)/);
 assert.match(html,/id="preview-adhkar"/);
});
test('release identity and saved settings retained',()=>{
 const manifest=JSON.parse(fs.readFileSync('webos/appinfo.json','utf8'));
 const js=fs.readFileSync('webos/main.js','utf8');
 assert.equal(manifest.version,'0.5.0');
 assert.equal(manifest.id,'com.alkitabmawqut.tv');
 assert.match(js,/alkitab-webos-settings-v1/);
 assert.match(js,/adhkarMinutes/);
});
