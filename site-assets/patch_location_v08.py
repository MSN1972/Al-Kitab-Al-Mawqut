#!/usr/bin/env python3
"""Apply global country/city/timezone/method selection after v0.7 LG patch."""
import json,re
from pathlib import Path
W=Path('webos')
def edit(file,old,new,expected=1):
 p=W/file;s=p.read_text('utf-8')
 assert s.count(old)==expected, f"{file}: expected {expected} matches for {old[:70]}, got {s.count(old)}"
 p.write_text(s.replace(old,new),'utf-8')
def add_js_core():
 p=W/'prayer-core.js';s=p.read_text('utf-8')
 assert 'function getEvents(daySchedules, offsets, durations) {' in s
 zone_functions=r"""
  // Convert wall clock schedules to real instants including DST for supported IANA zones.
  function partsForZone(epochMillis, timezone=TIMEZONE) {
    const fmt=new Intl.DateTimeFormat('en-GB',{timeZone:timezone,year:'numeric',month:'2-digit',
      day:'2-digit',hour:'2-digit',minute:'2-digit',second:'2-digit',hourCycle:'h23'});
    const o={};for(const part of fmt.formatToParts(new Date(epochMillis)))o[part.type]=part.value;
    return {date:o.year+'-'+o.month+'-'+o.day,hhmm:o.hour+':'+o.minute,seconds:Number(o.second)};
  }
  function epochInZone(ymd, minutes, timezone=TIMEZONE) {
    const expected=Date.parse(ymd+'T00:00:00Z')+minutes*60000;
    let instant=expected;
    for(let i=0;i<4;i++){
      const p=partsForZone(instant,timezone);
      const displayAsUtc=Date.parse(p.date+'T'+p.hhmm+':'+pad(p.seconds)+'Z');
      instant+=expected-displayAsUtc;
    }
    return instant;
  }
"""
 pos=s.index('  function validatedSchedule(')
 s=s[:pos]+zone_functions+s[pos:]
 s=s.replace('function getEvents(daySchedules, offsets, durations) {',
             'function getEvents(daySchedules, offsets, durations, timezone=TIMEZONE) {')
 s=s.replace('const start = epochInRiyadh(date, mins[i] + delay);',
             'const start = epochInZone(date, mins[i] + delay, timezone);')
 assert s.count('epochInZone(date, mins[i] + delay, timezone)')==1
 old='partsForRiyadh, validatedSchedule, getEvents'
 assert s.count(old)==1
 s=s.replace(old,'partsForRiyadh, partsForZone, epochInZone, validatedSchedule, getEvents')
 p.write_text(s,'utf-8')
add_js_core()
(W/'location-layer.js').write_text((Path('site-assets')/'location-layer-webos.js').read_text('utf-8'),'utf-8')
html=W/'index.html';h=html.read_text('utf-8')
assert "https://api.aladhan.com" in h
h=h.replace("connect-src 'self' https://api.aladhan.com;", "connect-src 'self' https://api.aladhan.com https://geocoding-api.open-meteo.com;")
# Header on all versions retains a brand subtitle.
assert 'مكة المكرمة - توقيت أم القرى' in h
h=h.replace('مكة المكرمة - توقيت أم القرى','<span id="brand-location">مكة المكرمة - توقيت أم القرى</span>',1)
assert '<section id="settings"' in h
location_dialog="""<section id="location-dialog" class="location-dialog hidden" role="dialog" aria-label="اختيار البلد والمدينة والمنطقة الزمنية" aria-modal="true">
  <div class="location-shell">
    <h1>البلد والمدينة والمنطقة الزمنية</h1>
    <p id="location-current">الموقع الحالي: مكة المكرمة</p>
    <div class="location-row"><label>المدينة <input id="city-query" type="text" placeholder="مثال: دمشق" /></label>
      <label>البلد (اختياري) <input id="country-query" type="text" placeholder="مثال: سوريا" /></label>
      <button id="location-search" type="button">بحث</button></div>
    <p id="location-message">ابحث عن المدينة، ثم اختر نتيجة واحدة</p>
    <div id="location-results" class="location-results"></div>
    <div id="location-selection" class="location-selection hidden">
      <p id="location-chosen"></p><label>المنطقة الزمنية IANA <input id="location-zone" type="text" placeholder="Asia/Riyadh" /></label>
      <label>طريقة حساب الصلاة <select id="location-method"></select></label>
      <button id="location-apply" type="button">اعتماد الموقع</button></div>
    <div class="location-footer"><button id="location-makkah" type="button">العودة إلى مكة - أم القرى</button>
      <button id="location-close" type="button">إغلاق</button></div>
    <small>تُحفظ المواقيت بشكل مستقل لكل مدينة وطريقة حساب. ينبغي مطابقتها لتقويم المسجد المحلي.</small>
  </div>
</section>
"""
h=h.replace('<section id="settings"',location_dialog+'  <section id="settings"',1)
assert 'id="settings-grid"' in h
h=h.replace('<div class="settings-grid" id="settings-grid"></div>',
  '<div class="location-setting"><span>البلد والمدينة والمنطقة الزمنية</span><button type="button" id="location-open">تغيير الموقع</button></div><div class="settings-grid" id="settings-grid"></div>',1)
assert '<script src="main.js"></script>' in h
h=h.replace('<script src="main.js"></script>','<script src="location-layer.js"></script><script src="main.js"></script>',1)
html.write_text(h,'utf-8')

p=W/'main.js';s=p.read_text('utf-8')
assert s.count('const core = window.PrayerCore;')==1
s=s.replace('const core = window.PrayerCore;','const core = window.PrayerCore;\nconst locationLayer = window.PrayerLocationLayer;')
assert 'const options = safelyParse(localStorage.getItem(KEY), {});' in s
s=s.replace('const options = safelyParse(localStorage.getItem(KEY), {});',
'''const options = safelyParse(localStorage.getItem(KEY), {});
let location = locationLayer.normalize(options.location);
function cacheKey(){return location.id==0&&location.method===4 ? CACHE : CACHE+':'+locationLayer.key(location);}
function refreshFormatters(){
 fmtClock=new Intl.DateTimeFormat('en-GB',{timeZone:location.timezone,hour:'2-digit',minute:'2-digit',second:'2-digit',hour12:false});
 fmtGregorian=new Intl.DateTimeFormat('ar-SA-u-ca-gregory',{timeZone:location.timezone,weekday:'long',day:'numeric',month:'long',year:'numeric'});
 fmtHijri=null;
 try{
   const f=new Intl.DateTimeFormat('ar-SA-u-ca-islamic-umalqura',{timeZone:location.timezone,day:'numeric',month:'long',year:'numeric'});
   if(f.resolvedOptions().calendar==='islamic-umalqura')fmtHijri=f;
 }catch(_){}
}''',1)
# Reassignable formatters are used after changes.
assert s.count('const fmtClock =')==1 and s.count('const fmtGregorian =')==1
s=s.replace('const fmtClock =','let fmtClock =',1).replace('const fmtGregorian =','let fmtGregorian =',1)
s=s.replace('timeZone:core.TIMEZONE','timeZone:location.timezone')
assert s.count('let days = safelyParse(localStorage.getItem(CACHE), {});')==1
s=s.replace('let days = safelyParse(localStorage.getItem(CACHE), {});',
            'let days = safelyParse(localStorage.getItem(cacheKey()), {});')
save_rx=r'function save\(\) \{ localStorage.setItem\(KEY, JSON.stringify\(\{([^}]+)\}\)\); \}'
m=re.search(save_rx,s);assert m and 'location' not in m.group(1)
s=s[:m.start()]+m.group(0).replace(m.group(1),m.group(1)+',location')+s[m.end():]
assert 'const url=' in s and 'latitude=21.4225&longitude=39.8262' in s
s=s.replace('async function fetchMonth(ym) {','async function fetchMonth(ym,selectedLocation=location) {',1)
s=s.replace("const url='https://api.aladhan.com/v1/calendar/'+year+'/'+Number(month)+'?latitude=21.4225&longitude=39.8262&method=4&timezonestring=Asia%2FRiyadh';",
 """const url='https://api.aladhan.com/v1/calendar/'+year+'/'+Number(month)
   +'?latitude='+selectedLocation.latitude+'&longitude='+selectedLocation.longitude
   +'&timezonestring='+encodeURIComponent(selectedLocation.timezone)
   +(selectedLocation.method===-1?'':'&method='+selectedLocation.method);""",1)
s=s.replace('const date=core.partsForRiyadh(Date.now()).date;',
            'const date=core.partsForZone(Date.now(),location.timezone).date;',1)
s=s.replace('const now=Date.now(), p=core.partsForRiyadh(now);',
            'const now=Date.now(), p=core.partsForZone(now,location.timezone);',1)
s=s.replace('core.getEvents(available,offsets,durations)','core.getEvents(available,offsets,durations,location.timezone)')
s=s.replace('localStorage.setItem(CACHE,JSON.stringify(days));','localStorage.setItem(cacheKey(),JSON.stringify(days));')
s=s.replace('const fetched=await fetchMonth(ym);','const fetched=await fetchMonth(ym,requestLocation);')
s=s.replace('syncBusy=true;lastSyncAttempt=Date.now();','syncBusy=true;lastSyncAttempt=Date.now();\n const requestLocation=location;',1)
s=s.replace('if (!fetched) {success=false;continue;}','if (requestLocation!==location||!fetched) {success=false;continue;}',1)
assert 'function tick()' in s
s=s.replace('function tick() {',"""function tick() {
 const brandLocation=$('brand-location');
 if(brandLocation)brandLocation.textContent=location.city+'، '+location.country+' — '+(location.method===4?'أم القرى':'طريقة الحساب: '+locationLayer.methods.find(x=>x[0]===location.method)[1]);""",1)
# Trigger location refresh after applying preference; never overwrite another location cache.
controls=r'''
let selectedCity=null;
const dlg=$('location-dialog');
const methodInput=$('location-method');
for(const pair of locationLayer.methods){
 const option=document.createElement('option');option.value=String(pair[0]);option.textContent=pair[1];methodInput.appendChild(option);
}
function closeLocation(){dlg.classList.add('hidden');$('settings-open').focus();}
$('location-open').onclick=()=>{
 dom.settings.classList.add('hidden');
 $('location-current').textContent='الموقع الحالي: '+location.city+'، '+location.country+' — '+location.timezone;
 dlg.classList.remove('hidden');
 $('city-query').focus();
};
$('location-close').onclick=closeLocation;
function chooseCity(city){
 selectedCity=city;
 $('location-chosen').textContent='الموقع المختار: '+city.city+'، '+city.country+(city.region?' — '+city.region:'');
 $('location-zone').value=city.timezone;
 methodInput.value=String(city.method);
 $('location-selection').classList.remove('hidden');
 $('location-message').textContent='تحقق من المدينة والمنطقة الزمنية وطريقة الحساب ثم اضغط اعتماد';
}
$('location-search').onclick=async()=>{
 $('location-message').textContent='جاري البحث...';
 $('location-results').textContent='';$('location-selection').classList.add('hidden');selectedCity=null;
 try{
  const results=await locationLayer.search($('city-query').value,$('country-query').value);
  $('location-message').textContent=results.length?'اختر المدينة الصحيحة':'لا نتائج؛ جرّب تهجئة أخرى';
  for(const item of results){
   const button=document.createElement('button');
   button.type='button';button.textContent=item.city+'، '+item.country+(item.region?' — '+item.region:'')+' | '+item.timezone;
   button.onclick=()=>chooseCity(item);
   $('location-results').appendChild(button);
  }
 }catch(error){$('location-message').textContent=error.message||'تعذر الاتصال';}
};
function setLocation(loc) {
 location=locationLayer.normalize(loc);
 days=safelyParse(localStorage.getItem(cacheKey()),{});
 lastClockDate='';lastMode='';lastSyncAttempt=0;
 refreshFormatters();save();closeLocation();
 tick();void synchronize();
}
$('location-apply').onclick=()=>{
 if(!selectedCity)return;
 const tz=$('location-zone').value.trim();
 if(!locationLayer.validZone(tz)){$('location-message').textContent='المنطقة الزمنية غير صحيحة، مثال Europe/London';return;}
 setLocation(Object.assign({},selectedCity,{timezone:tz,method:Number(methodInput.value)}));
};
$('location-makkah').onclick=()=>setLocation(locationLayer.MAKKAH);
'''
assert s.rstrip().endswith('})();'), 'Unexpected main.js ending'
point=s.rfind('})();')
s=s[:point]+controls+'\n'+s[point:]
p.write_text(s,'utf-8')

c=W/'style.css';c.write_text(c.read_text('utf-8')+r"""
/* v0.8 city/country picker, TV-remote-friendly large focus surfaces */
.location-setting{display:flex;justify-content:space-between;align-items:center;gap:1vw;margin:1vh 0;padding:1vh 1.3vw;border:1px solid #d7b76d;background:#081d32;color:#ffe3a6;font-size:clamp(18px,1.7vw,32px);border-radius:12px}
.location-setting button,.location-dialog button{font:inherit;padding:.7vh 1.3vw;min-height:44px;border:1.5px solid #d8b66b;border-radius:10px;color:#ffdfa1;background:#173452;cursor:pointer}
.location-dialog{position:fixed;inset:0;z-index:250;background:#051425f5;color:#fff4d9;direction:rtl;overflow:hidden}
.location-dialog.hidden{display:none!important}
.location-shell{height:100%;display:flex;flex-direction:column;gap:1vh;padding:2.5vh 4vw;box-sizing:border-box}
.location-shell h1{margin:0;color:#ffda8d;font-size:clamp(30px,3vw,60px)}
.location-shell p{margin:.3vh 0;font-size:clamp(16px,1.4vw,27px)}
.location-row,.location-footer,.location-selection{display:flex;align-items:center;gap:1.4vw;flex-wrap:wrap}
.location-dialog label{display:flex;flex:1;flex-direction:column;gap:.4vh;color:#ffdfa1;font-size:clamp(16px,1.1vw,24px)}
.location-dialog input,.location-dialog select{font:inherit;font-size:clamp(17px,1.45vw,29px);color:#102741;background:#fff8e8;border:2px solid #c49d51;border-radius:10px;min-height:44px;padding:.7vh 1vw}
.location-results{flex:1;overflow:auto;min-height:12vh;display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:.7vh .8vw;align-content:start}
.location-results button{text-align:right;min-height:50px;font-size:clamp(14px,1.15vw,24px)}
.location-selection{background:#14304b;border:1px solid #d7b66b;padding:1vh;border-radius:12px}
.location-selection.hidden{display:none!important}
.location-shell small{font-size:clamp(13px,.95vw,21px)}
.location-dialog button:focus,.location-dialog input:focus,.location-dialog select:focus{outline:3px solid #fff29b;outline-offset:2px}
""",'utf-8')
info=W/'appinfo.json';meta=json.loads(info.read_text('utf-8'))
assert meta['version']=='0.7.0' and meta['id']=='com.alkitabmawqut.tv'
meta['version']='0.8.0'
info.write_text(json.dumps(meta,ensure_ascii=False,indent=2)+'\n','utf-8')
for f in (W/'tests').glob('*.test.cjs'):
 txt=f.read_text('utf-8')
 txt=txt.replace("'0.7.0'","'0.8.0'").replace('"0.7.0"','"0.8.0"')
 f.write_text(txt,'utf-8')
print('Global city chooser, dynamic timezone, city cache and per-country prayer method integrated on LG.')
