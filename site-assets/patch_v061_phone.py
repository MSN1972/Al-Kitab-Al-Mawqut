from pathlib import Path
import json
R=Path('webos')
svg='''<svg xmlns="http://www.w3.org/2000/svg" width="192" height="192" viewBox="0 0 192 192" aria-hidden="true">
<defs><radialGradient id="n" cx="50%" cy="35%" r="80%"><stop stop-color="#153351"/><stop offset="1" stop-color="#061526"/></radialGradient></defs>
<circle cx="96" cy="96" r="89" fill="url(#n)" stroke="#dfb76d" stroke-width="6"/>
<rect x="68" y="36" width="56" height="119" rx="11" fill="#f4f0e7" stroke="#afc0d2" stroke-width="4"/>
<rect x="74" y="48" width="44" height="87" rx="2" fill="#17334d"/>
<circle cx="96" cy="144" r="4" fill="#627d96"/>
<path d="M34 34 158 158" stroke="#70070b" stroke-width="24" stroke-linecap="round"/>
<path d="M34 34 158 158" stroke="#f23a30" stroke-width="16" stroke-linecap="round"/>
<circle cx="96" cy="96" r="75" fill="none" stroke="#b8090f" stroke-width="17"/>
<circle cx="96" cy="96" r="75" fill="none" stroke="#f44035" stroke-width="11"/>
</svg>'''
(R/'assets'/'phone-silent.svg').write_text(svg,encoding='utf-8')
p=R/'index.html';s=p.read_text('utf-8')
pairs=[
('<section class="ticker-bar"><div class="ticker-title">أحاديث صحيحة موثقة</div>', '<section class="ticker-bar"><img class="phone-symbol phone-symbol-main" src="assets/phone-silent.svg" alt="" aria-hidden="true"><div class="ticker-title">أحاديث صحيحة موثقة</div>'),
('<main id="quiet" class="quiet-screen hidden" aria-label="شاشة صلاة هادئة"></main>', '<main id="quiet" class="quiet-screen hidden" aria-label="شاشة صلاة هادئة"><img class="phone-symbol phone-symbol-quiet" src="assets/phone-silent.svg" alt="" aria-hidden="true"></main>'),
('<div class="adhkar-hero-overlay" aria-hidden="true"></div>', '<div class="adhkar-hero-overlay" aria-hidden="true"></div>\n        <img class="phone-symbol phone-symbol-adhkar" src="assets/phone-silent.svg" alt="" aria-hidden="true">'),
('<header><div><h1>إعدادات الكتاب الموقوت</h1><p>تصحيح الأذان والإقامة ومدة الصلاة، مع أذكار ما بعد الصلاة</p></div><button type="button" id="settings-close">إغلاق ✕</button></header>', '<header><div><h1>إعدادات الكتاب الموقوت</h1><p>تصحيح الأذان والإقامة ومدة الصلاة، مع أذكار ما بعد الصلاة</p></div><img class="phone-symbol phone-symbol-settings" src="assets/phone-silent.svg" alt="" aria-hidden="true"><button type="button" id="settings-close">إغلاق ✕</button></header>')
]
for old,new in pairs:
    assert s.count(old)==1,old[:60]
    s=s.replace(old,new)
wrong='تَبَارَكْتَ ذَا الْجَلَالِ وَالْإِكْرَامِ'
right='تَبَارَكْتَ يَا ذَا الْجَلَالِ وَالْإِكْرَامِ'
assert s.count(wrong)==1
s=s.replace(wrong,right)
needle='<section class="post-prayer-card adhkar-salam" aria-label="ذكر السلام بعد الصلاة">'
start=s.index(needle);end=s.index('</section>',start)
section=s[start:end]
assert '<small>صحيح مسلم (591)</small>' in section
s=s[:start]+section.replace('<small>صحيح مسلم (591)</small>','<small>صحيح مسلم (592) — رواية ابن نمير</small>')+s[end:]
p.write_text(s,'utf-8')
p=R/'style.css'
css=p.read_text('utf-8')
css+='''
/* v0.6.1: visible pictogram only; deliberately static and dim during prayer */
.phone-symbol{display:block;flex:none;object-fit:contain;aspect-ratio:1/1;animation:none!important;transition:none!important;filter:none}
.phone-symbol-main{width:clamp(32px,3vw,72px);height:clamp(32px,3vw,72px);opacity:.88}
.phone-symbol-quiet{position:absolute;width:clamp(40px,4.3vw,90px);height:clamp(40px,4.3vw,90px);bottom:2.3vh;right:2vw;opacity:.35}
.phone-symbol-adhkar{position:absolute;z-index:2;width:clamp(48px,4.9vw,103px);height:clamp(48px,4.9vw,103px);bottom:1.8vh;left:2vw;opacity:.91}
.phone-symbol-settings{width:clamp(40px,3.3vw,70px);height:clamp(40px,3.3vw,70px);opacity:.85;margin-inline-start:auto}
@media(max-width:1100px){.phone-symbol-main{width:38px;height:38px}.phone-symbol-quiet{width:45px;height:45px}}
'''
p.write_text(css,'utf-8')
p=R/'post-prayer-adhkar.json';v=json.loads(p.read_text('utf-8'))
assert v[0]['supplement'].count(wrong)==1
v[0]['supplement']=v[0]['supplement'].replace(wrong,right)
v[0]['supplementSource']='صحيح مسلم (592) — رواية ابن نمير'
v[0]['supplementUrl']='https://sunnah.com/muslim:592b'
p.write_text(json.dumps(v,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
(R/'post-prayer-adhkar.js').write_text('/* Verified variant from Sahih Muslim 592. */\nwindow.PostPrayerAdhkar = '+json.dumps(v,ensure_ascii=False,separators=(',',':'))+';\n',encoding='utf-8')
p=R/'appinfo.json';v=json.loads(p.read_text('utf-8'));assert v['version']=='0.6.0';v['version']='0.6.1';p.write_text(json.dumps(v,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
tests=R/'tests'
for name in ('clock-layout.test.cjs','release-quality.test.cjs'):
    x=tests/name;s=x.read_text('utf-8')
    assert "manifest.version,'0.6.0'" in s
    x.write_text(s.replace("manifest.version,'0.6.0'","manifest.version,'0.6.1'"),encoding='utf-8')
x=tests/'post-prayer.test.cjs';s=x.read_text('utf-8')
old="const section=css.slice(css.indexOf('/* Post-prayer remembrance'));"
assert old in s
s=s.replace(old,"const section=css.slice(css.indexOf('/* Post-prayer remembrance'), css.indexOf('/* v0.6.1:'));")
x.write_text(s,encoding='utf-8')
x=tests/'visual-v06.test.cjs';s=x.read_text('utf-8')
old='assert.doesNotMatch(html,/<main id="quiet"[^>]*>\\s*<img/);'
assert old in s
s=s.replace(old,'assert.match(html,/<main id="quiet"[^>]*>\\s*<img class="phone-symbol phone-symbol-quiet"/);\n assert.match(css,/\\.phone-symbol-quiet\\{[^}]*opacity:\\.35/);')
x.write_text(s,encoding='utf-8')
print('Applied silent phone pictogram across main/quiet/adhkar/settings, corrected Muslim 592 variant, and updated existing tests.')