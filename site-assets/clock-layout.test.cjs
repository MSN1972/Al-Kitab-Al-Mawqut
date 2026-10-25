const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const html = fs.readFileSync(path.join(__dirname,'../index.html'),'utf8');
const css = fs.readFileSync(path.join(__dirname,'../style.css'),'utf8');
const js = fs.readFileSync(path.join(__dirname,'../main.js'),'utf8');

test('clock and both dates are grouped in one panel',()=>{
 const panel=html.match(/<div class="clock-panel"[\s\S]*?<\/div>\s*<\/div>\s*<\/div>/);
 assert.ok(panel, 'unified clock panel');
 assert.match(panel[0],/id="clock"/);
 assert.match(panel[0],/id="gregorian"/);
 assert.match(panel[0],/id="hijri"/);
 assert.doesNotMatch(html,/class="hijri-panel"/);
});
test('Hijri and Gregorian use the exact same gold visual class',()=>{
 assert.match(html,/id="gregorian" class="date-text"/);
 assert.match(html,/id="hijri" class="date-text"/);
 assert.match(css,/\.dates-stack\{/);
 assert.match(css,/\.date-text\{/);
});
test('both dates update on each Riyadh-time clock tick',()=>{
 assert.match(js,/dom\.gregorian\.textContent=fmtGregorian\.format/);
 assert.match(js,/dom\.hijri\.textContent=fmtHijri/);
 assert.match(js,/timeZone:core\.TIMEZONE/);
});
test('same TV app identity preserves upgrade and saved settings',()=>{
 const manifest=JSON.parse(fs.readFileSync(path.join(__dirname,'../appinfo.json'),'utf8'));
 assert.equal(manifest.id,'com.alkitabmawqut.tv');
 assert.equal(manifest.version,'0.3.0');
 assert.match(js,/alkitab-webos-settings-v1/);
});
