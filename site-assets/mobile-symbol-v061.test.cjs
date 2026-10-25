const {test}=require('node:test');
const assert=require('node:assert/strict');
const fs=require('node:fs'),path=require('node:path');
const r=path.resolve(__dirname,'..');
const h=fs.readFileSync(path.join(r,'index.html'),'utf8');
const css=fs.readFileSync(path.join(r,'style.css'),'utf8');
const svg=fs.readFileSync(path.join(r,'assets/phone-silent.svg'),'utf8');
const data=JSON.parse(fs.readFileSync(path.join(r,'post-prayer-adhkar.json'),'utf8'));
test('mobile-off icon appears in all four TV screens',()=>{
 for(const cls of ['main','quiet','adhkar','settings'])assert.match(h,new RegExp('class="phone-symbol phone-symbol-'+cls+'"'));
 assert.equal((h.match(/src="assets\/phone-silent\.svg"/g)||[]).length,4);
});
test('SVG has no letters or motion; quiet screen is dim',()=>{
 assert.match(svg,/<circle/);assert.match(svg,/stroke="#f44035"/);
 assert.doesNotMatch(svg,/<text\b|<animate\b/);
 assert.match(css,/\.phone-symbol\{[^}]*animation:none!important/);
 assert.match(css,/\.phone-symbol-quiet\{[^}]*opacity:\.35/);
});
test('salam uses the user approved Muslim 592 narrated variant',()=>{
 assert.match(h,/تَبَارَكْتَ يَا ذَا الْجَلَالِ وَالْإِكْرَامِ/);
 assert.match(h,/صحيح مسلم \(592\) — رواية ابن نمير/);
 assert.match(data[0].supplement,/تَبَارَكْتَ يَا ذَا/);
 assert.equal(data[0].supplementSource,'صحيح مسلم (592) — رواية ابن نمير');
});
test('webOS upgrade preserves app id and increments semver',()=>{
 const app=JSON.parse(fs.readFileSync(path.join(r,'appinfo.json'),'utf8'));
 assert.equal(app.id,'com.alkitabmawqut.tv');
 assert.equal(app.version,'0.6.1');
});