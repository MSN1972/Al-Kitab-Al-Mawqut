from pathlib import Path
import json

w=Path('webos')
def patch(path, before, after):
 p=w/path
 s=p.read_text(encoding='utf-8')
 assert s.count(before)==1, f"Unexpected {path}: {before[:75]}"
 p.write_text(s.replace(before,after),encoding='utf-8')

patch('prayer-core.js',
"""  function screenAt(now, events) {
    const e = events.find(item => now >= item.start && now < item.end);
    if (e) return {mode:'quiet', event:e};
    const future = events.find(item => item.start > now);
    return {mode: 'display', next: future || null, remainingSec: future ? Math.max(0,Math.ceil((future.start-now)/1000)) : null};
  }""",
"""  function screenAt(now, events, adhkarMinutes=0) {
    const ordered = Array.isArray(events) ? events : [];
    const praying = ordered.find(e => now >= e.start && now < e.end);
    if(praying) return {mode:'quiet',event:praying};
    const duration = Number.isFinite(Number(adhkarMinutes)) ? Math.max(0,Math.min(15,Math.round(Number(adhkarMinutes)))) : 0;
    if(duration>0){
      const finished = ordered.filter(e=>now>=e.end && now<e.end+duration*60000).sort((a,b)=>b.end-a.end);
      if(finished.length)return {mode:'adhkar',event:finished[0],until:finished[0].end+duration*60000};
    }
    const next=ordered.find(e=>e.start>now);
    return {mode:'display',next:next||null,remainingSec:next?Math.max(0,Math.ceil((next.start-now)/1000)):null};
  }""")

screen='''  <main id="quiet" class="quiet-screen hidden" aria-label="شاشة صلاة هادئة"></main>
  <main id="post-prayer" class="post-prayer-screen hidden" aria-label="أذكار ما بعد الصلاة">
    <div class="post-prayer-shell">
      <header class="post-prayer-header"><img src="assets/brand-icon.png" alt="" aria-hidden="true" class="post-prayer-logo"><div class="post-prayer-heading"><h1>أذكار ما بعد الصلاة</h1><p id="post-prayer-name">بعد الصلاة المفروضة</p></div><span class="post-prayer-subtitle">الكتاب الموقوت</span></header>
      <div class="post-prayer-content">
        <section class="post-prayer-card"><h2>الاستغفار بعد السلام</h2><div class="post-prayer-dhikr">أَسْتَغْفِرُ اللَّهَ <span class="post-prayer-count">٣ مرات</span></div><p class="post-prayer-description">اللَّهُمَّ أَنْتَ السَّلَامُ وَمِنْكَ السَّلَامُ، تَبَارَكْتَ ذَا الْجَلَالِ وَالْإِكْرَامِ</p><small>صحيح مسلم (591)</small></section>
        <section class="post-prayer-card"><h2>التهليل والدعاء</h2><p class="post-prayer-description">لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ، وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ</p><p class="post-prayer-description small-desc">اللَّهُمَّ لَا مَانِعَ لِمَا أَعْطَيْتَ، وَلَا مُعْطِيَ لِمَا مَنَعْتَ، وَلَا يَنْفَعُ ذَا الْجَدِّ مِنْكَ الْجَدُّ</p><small>صحيح البخاري (844)</small></section>
        <section class="post-prayer-card post-prayer-tasbih"><h2>التسبيح والتحميد والتكبير</h2><div class="post-prayer-tasbih-row"><span>سُبْحَانَ اللَّهِ</span><strong>٣٣ مرة</strong><span>الْحَمْدُ لِلَّهِ</span><strong>٣٣ مرة</strong><span>اللَّهُ أَكْبَرُ</span><strong>٣٣ مرة</strong></div><p class="post-prayer-description">تمام المائة: لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ، وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ</p><small>صحيح مسلم (597)</small></section>
      </div>
      <footer class="post-prayer-footer"><span>أذكار ثابتة من صحيحي البخاري ومسلم</span><span>تعود شاشة المواقيت تلقائياً بعد انتهاء مدة العرض</span></footer>
    </div>
  </main>'''
patch('index.html','  <main id="quiet" class="quiet-screen hidden" aria-label="شاشة صلاة هادئة"></main>',screen)
patch('index.html','<p>تصحيح الأذان وفاصل الإقامة ومدة الشاشة الهادئة، لكل صلاة</p>','<p>تصحيح الأذان والإقامة ومدة الصلاة، مع أذكار ما بعد الصلاة</p>')
patch('index.html','<button type="button" id="preview">تجربة الشاشة الهادئة (10 ثوان)</button>','<button type="button" id="preview">تجربة الشاشة الهادئة (10 ثوان)</button><button type="button" id="preview-adhkar">تجربة شاشة الأذكار (10 ثوان)</button>')

css='''
/* Post-prayer screen is static and has no animation during or after prayer. */
.post-prayer-screen{position:fixed;inset:0;z-index:105;background:radial-gradient(ellipse at top,#15314b 0%,#081728 64%,#05101d 100%);color:#ffe3ac;direction:rtl;overflow:hidden}
.post-prayer-shell{display:flex;flex-direction:column;gap:1.5vh;height:100vh;padding:2.2vh 3vw 2.1vh}
.post-prayer-header{display:flex;align-items:center;gap:1.35vw;flex:none;border-bottom:2px solid rgba(244,205,136,.7);padding-bottom:1.1vh}
.post-prayer-logo{width:clamp(55px,5.2vw,106px);height:clamp(55px,5.2vw,106px);object-fit:contain;border-radius:17%}
.post-prayer-heading{flex:1;text-align:right}.post-prayer-heading h1{font-size:clamp(36px,3.25vw,68px);line-height:1.2;margin:0;color:#f9d78e;text-shadow:0 0 12px #b8873663}
.post-prayer-heading p{font-size:clamp(20px,1.6vw,34px);color:#f6f5eb;margin:.25vh 0}
.post-prayer-subtitle{font-size:clamp(20px,1.6vw,32px);white-space:nowrap;color:#eac580}
.post-prayer-content{display:grid;grid-template-columns:1fr 1fr;grid-template-rows:minmax(0,1fr) minmax(0,1fr);gap:1.45vh 1.6vw;flex:1;min-height:0}
.post-prayer-card{border:2px solid rgba(226,187,108,.76);border-radius:20px;background:rgba(6,26,48,.93);padding:1.1vh 1.7vw;display:flex;flex-direction:column;align-items:center;justify-content:space-evenly;text-align:center;gap:.35vh;min-height:0}
.post-prayer-card h2{margin:0;color:#f7cb74;font-size:clamp(23px,1.95vw,42px);line-height:1.2}
.post-prayer-card small{color:#d3e0ec;font-size:clamp(15px,1.1vw,24px)}
.post-prayer-dhikr{font-size:clamp(25px,2.45vw,54px);font-weight:700;line-height:1.4}
.post-prayer-count{font-size:.65em;color:#081b32;background:linear-gradient(120deg,#dbb76e,#ffedb2);padding:.25vh .8vw;border-radius:999px;white-space:nowrap}
.post-prayer-description{font-size:clamp(20px,1.75vw,36px);color:#fff5e0;font-weight:600;line-height:1.58;margin:.25vh 0}
.post-prayer-description.small-desc{font-size:clamp(18px,1.6vw,33px)}
.post-prayer-tasbih{grid-column:1 / -1}
.post-prayer-tasbih-row{display:flex;align-items:center;justify-content:center;gap:1.4vw;font-size:clamp(24px,2.0vw,45px);font-weight:700;white-space:nowrap}
.post-prayer-tasbih-row strong{color:#092039;background:linear-gradient(110deg,#e0b86c,#fff0b9);border-radius:12px;padding:.2vh .6vw;font-size:.84em}
.post-prayer-tasbih .post-prayer-description{font-size:clamp(19px,1.6vw,35px)}
.post-prayer-footer{display:flex;justify-content:space-between;gap:1vw;align-items:center;flex:none;color:#d2dfeb;font-size:clamp(15px,1.1vw,25px);min-height:3vh}
@media(max-width:1100px){.post-prayer-shell{gap:1vh;padding:1.4vh 2vw}.post-prayer-card{padding:.5vh 1vw}.post-prayer-description{font-size:clamp(15px,1.9vw,30px)}}
'''
p=w/'style.css';p.write_text(p.read_text(encoding='utf-8')+css,encoding='utf-8')

patch('main.js'," display: $('display'), quiet: $('quiet'), settings: $('settings'),"," display: $('display'), quiet: $('quiet'), adhkar: $('post-prayer'), adhkarPrayer: $('post-prayer-name'), settings: $('settings'),")
patch('main.js',"let tickerSeconds = Math.max(35, Math.min(120, Number(options.tickerSeconds) || 60));","let tickerSeconds = Math.max(35, Math.min(120, Number(options.tickerSeconds) || 60));\nlet adhkarMinutes = Number.isFinite(Number(options.adhkarMinutes)) ? Math.max(0,Math.min(15,Math.round(Number(options.adhkarMinutes)))) : 4;")
patch('main.js',"lastMode = '', demoUntil = 0;","lastMode = '', demoUntil = 0, demoAdhkarUntil = 0;")
patch('main.js',"function save() { localStorage.setItem(KEY, JSON.stringify({offsets,durations,corrections,tickerSeconds})); }","function save() { localStorage.setItem(KEY, JSON.stringify({offsets,durations,corrections,tickerSeconds,adhkarMinutes})); }")
patch('main.js'," dom.grid.appendChild(tickerRow);\n}",""" dom.grid.appendChild(tickerRow);
 const row=document.createElement('article');row.className='setting-card ticker-setting';
 const title2=document.createElement('h2');title2.textContent='أذكار ما بعد الصلاة';row.appendChild(title2);
 const desc2=document.createElement('div');desc2.className='setting-label';desc2.textContent='مدة عرض الأذكار بعد انتهاء الصلاة بالدقائق (0 لإيقافها)';row.appendChild(desc2);
 const group=document.createElement('div');group.className='control';
 const minus2=document.createElement('button');minus2.textContent='−';minus2.type='button';
 const value=document.createElement('span');value.className='setting-value';value.textContent=adhkarMinutes;
 const plus2=document.createElement('button');plus2.textContent='+';plus2.type='button';
 function change(delta){adhkarMinutes=Math.max(0,Math.min(15,adhkarMinutes+delta));value.textContent=adhkarMinutes;save();tick();}
 minus2.onclick=()=>change(-1);plus2.onclick=()=>change(1);group.append(minus2,value,plus2);row.appendChild(group);dom.grid.appendChild(row);
}""")
patch('main.js',"$('preview').onclick=()=>{hideSettings();demoUntil=Date.now()+10000;tick();};","$('preview').onclick=()=>{hideSettings();demoAdhkarUntil=0;demoUntil=Date.now()+10000;tick();};\n$('preview-adhkar').onclick=()=>{hideSettings();demoUntil=0;demoAdhkarUntil=Date.now()+10000;tick();};")
patch('main.js',""" const state=core.screenAt(now,events);
 const quiet=state.mode==='quiet'||now<demoUntil;
 dom.quiet.classList.toggle('hidden',!quiet);
 dom.display.classList.toggle('hidden',quiet);
 if (quiet) {lastMode='quiet';return;}""",""" const state=core.screenAt(now,events,adhkarMinutes);
 const quiet=state.mode==='quiet'||now<demoUntil;
 const postPrayer=!quiet && (state.mode==='adhkar'||now<demoAdhkarUntil);
 dom.quiet.classList.toggle('hidden',!quiet);
 dom.adhkar.classList.toggle('hidden',!postPrayer);
 dom.display.classList.toggle('hidden',quiet||postPrayer);
 if (quiet) {lastMode='quiet';return;}
 if(postPrayer){dom.adhkarPrayer.textContent=state.mode==='adhkar'?'بعد صلاة '+core.ARABIC[state.event.index]:'نموذج أذكار ما بعد الصلاة';lastMode='adhkar';return;}""")
for name in ['tests/clock-layout.test.cjs','tests/release-quality.test.cjs']:
 p=w/name;s=p.read_text(encoding='utf-8');assert "'0.4.0'" in s;p.write_text(s.replace("'0.4.0'","'0.5.0'"),encoding='utf-8')
manifest=w/'appinfo.json';metadata=json.loads(manifest.read_text(encoding='utf-8'));assert metadata['version']=='0.4.0';metadata['version']='0.5.0';manifest.write_text(json.dumps(metadata,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print('Successfully applied LG v0.5.0 post-prayer feature')
