#!/usr/bin/env python3
"""LG actual v0.7 remembrance layout; apply after v0.6.1 patch."""
from pathlib import Path
import json
p=Path("webos")
hfile=p/"index.html"; cfile=p/"style.css"; manifest=p/"appinfo.json"
h=hfile.read_text("utf8")
begin=h.index('  <main id="post-prayer"')
end=h.index('  <section id="settings"',begin)
rows=[
("الاستغفار","أَسْتَغْفِرُ اللَّهَ، أَسْتَغْفِرُ اللَّهَ، أَسْتَغْفِرُ اللَّهَ","ثلاث مرات · مسلم ٥٩١",""),
("ذكر السلام","اللَّهُمَّ أَنْتَ السَّلَامُ وَمِنْكَ السَّلَامُ، تَبَارَكْتَ يَا ذَا الْجَلَالِ وَالْإِكْرَامِ","مسلم ٥٩٢ — رواية ابن نمير",""),
("التهليل عشر مرات بعد الفجر والمغرب","لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ، يُحْيِي وَيُمِيتُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ","عشر مرات بعد الفجر والمغرب",""),
("شهادة التوحيد كاملة","لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَلَا نَعْبُدُ إِلَّا إِيَّاهُ، لَهُ النِّعْمَةُ وَلَهُ الْفَضْلُ وَلَهُ الثَّنَاءُ الْحَسَنُ، لَا إِلَهَ إِلَّا اللَّهُ مُخْلِصِينَ لَهُ الدِّينَ وَلَوْ كَرِهَ الْكَافِرُونَ","صحيح مسلم ٥٩٤","adhkar-line-long"),
("اللهم لا مانع لما أعطيت","اللَّهُمَّ لَا مَانِعَ لِمَا أَعْطَيْتَ، وَلَا مُعْطِيَ لِمَا مَنَعْتَ، وَلَا يَنْفَعُ ذَا الْجَدِّ مِنْكَ الْجَدُّ","صحيح البخاري ٨٤٤",""),
("التسبيح والتحميد والتكبير","سُبْحَانَ اللَّهِ ٣٣، الْحَمْدُ لِلَّهِ ٣٣، اللَّهُ أَكْبَرُ ٣٣","صحيح مسلم ٥٩٧","adhkar-line-tasbih"),
("سور الإخلاص والفلق والناس","سُورَةُ الْإِخْلَاصِ، سُورَةُ الْفَلَقِ، سُورَةُ النَّاسِ","مرة بعد كل صلاة، وثلاث مرات بعد الفجر والمغرب",""),
("آية الكرسي","آيَةُ الْكُرْسِيِّ","مرة بعد كل صلاة",""),
]
new='''  <main id="post-prayer" class="post-prayer-screen hidden adhkar-v07" aria-label="أذكار ما بعد الصلاة">
    <div class="adhkar-photo" aria-hidden="true"></div>
    <div class="adhkar-photo-shade" aria-hidden="true"></div>
    <div class="adhkar-side-brand" aria-hidden="true"><img src="assets/brand-icon.png" alt=""><span>الكتاب الموقوت</span></div>
    <img class="phone-symbol phone-symbol-adhkar" src="assets/phone-silent.svg" alt="" aria-hidden="true">
    <div class="adhkar-lines-layout">
      <header class="adhkar-lines-header"><h1>أذكار ما بعد الصلاة</h1><p id="post-prayer-name">بعد الصلاة المفروضة</p></header>
      <div class="adhkar-lines" aria-label="أذكار ما بعد الصلوات">
'''
for title,phrase,hint,kind in rows:
    new+=f'        <section class="adhkar-line {kind}" aria-label="{title}"><p>{phrase}</p>'
    if kind=="adhkar-line-tasbih":
        new+='<span class="adhkar-subline">تمام المائة: لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ</span>'
    new+=f'<span class="adhkar-hint">{hint}</span></section>\n'
new+='''      </div>
    </div>
  </main>
'''
h=h[:begin]+new+h[end:];hfile.write_text(h,"utf8")
css=r'''
/* v0.7: real TV HTML remembrance lines, translucent navy with photographic side. */
.adhkar-v07{position:fixed;inset:0;z-index:105;display:block;overflow:hidden;background:#062039;color:#fff;direction:rtl;font-family:Tahoma,Arial,sans-serif}
.adhkar-v07 .adhkar-photo{position:absolute;inset:0;background:url('assets/kaaba-4k-cinematic.jpg') center center/cover no-repeat;filter:saturate(1.12) contrast(1.1) brightness(.98);z-index:0}
.adhkar-v07 .adhkar-photo-shade{position:absolute;inset:0;z-index:1;background:linear-gradient(to right,#030d1e3d 0%,#05162614 28%,#071d307a 35%,#05162638 100%)}
.adhkar-v07 .adhkar-lines-layout{position:absolute;z-index:3;right:2.6%;top:1.2%;width:69%;height:97.6%;display:flex;flex-direction:column;gap:1vh;min-height:0}
.adhkar-v07 .adhkar-lines-header{flex:0 0 auto;min-height:10.4vh;display:flex;align-items:center;justify-content:space-between;gap:1vw;padding:0 1.5vw;background:linear-gradient(90deg,#031527c2,#061c31d6);border:2px solid #dab065;border-radius:22px;box-shadow:0 0 20px #0007}
.adhkar-v07 .adhkar-lines-header h1{font-size:clamp(36px,3.15vw,74px);font-weight:800;margin:0;color:#ffe1a0;line-height:1.1;text-shadow:0 2px 7px #000b}
.adhkar-v07 .adhkar-lines-header p{font-size:clamp(16px,1.13vw,25px);white-space:nowrap;color:#ffe7ad;margin:0}
.adhkar-v07 .adhkar-lines{flex:1;min-height:0;display:flex;flex-direction:column;justify-content:space-between;gap:.75vh}
.adhkar-v07 .adhkar-line{position:relative;min-height:0;flex:1 1 0;display:flex;flex-direction:column;align-items:center;justify-content:center;gap:.12vh;padding:.55vh 1vw;direction:rtl;text-align:center;border:2px solid #e7bd6ee3;border-radius:15px;background:linear-gradient(100deg,#061a2ccc,#092336b3);box-shadow:inset 0 0 9px #ffe6b115,0 2px 8px #0005}
.adhkar-v07 .adhkar-line-long{flex:1.8 1 0}.adhkar-v07 .adhkar-line-tasbih{flex:1.35 1 0}
.adhkar-v07 .adhkar-line p{color:#fff8eb;font-weight:800;line-height:1.36;font-size:clamp(18px,1.42vw,30px);margin:0;max-width:100%;text-shadow:0 1px 4px #000a}
.adhkar-v07 .adhkar-line-long p{font-size:clamp(17px,1.29vw,27px);line-height:1.42}
.adhkar-v07 .adhkar-line .adhkar-hint{display:block;color:#f7d293;font-size:clamp(12px,.82vw,18px);line-height:1.15;font-weight:700}
.adhkar-v07 .adhkar-line .adhkar-subline{display:block;color:#fff1d3;font-size:clamp(14px,1vw,23px);line-height:1.3;font-weight:700}
.adhkar-v07 .adhkar-side-brand{position:absolute;left:2%;top:3%;z-index:4;direction:rtl;display:flex;align-items:center;gap:.75vw;color:#ffdf9e;font-size:clamp(19px,1.3vw,29px);font-weight:700;padding:.45vw .7vw;background:#061b33db;border:1.5px solid #dfb975;border-radius:15px}
.adhkar-v07 .adhkar-side-brand img{width:clamp(40px,3.7vw,80px);height:clamp(40px,3.7vw,80px)}
.adhkar-v07 .phone-symbol-adhkar{position:absolute;z-index:5;left:10.5%;bottom:11%;width:clamp(64px,9vw,176px);height:clamp(64px,9vw,176px);opacity:.99;filter:drop-shadow(0 2px 4px #0009)}
.adhkar-v07.hidden{display:none!important}
@media(max-width:1100px){.adhkar-v07 .adhkar-lines-layout{width:72%;right:1.5%;height:98.5%;top:.7%}.adhkar-v07 .adhkar-lines-header{min-height:10vh}.adhkar-v07 .adhkar-line{padding:.2vh .5vw;gap:0}.adhkar-v07 .adhkar-line p{font-size:clamp(13px,1.55vw,19px);line-height:1.28}.adhkar-v07 .adhkar-line-long p{font-size:clamp(13px,1.3vw,17px);line-height:1.26}.adhkar-v07 .adhkar-line .adhkar-hint{font-size:clamp(10px,.97vw,14px)}.adhkar-v07 .adhkar-line .adhkar-subline{font-size:clamp(11px,1.05vw,15px)}}
'''
cfile.write_text(cfile.read_text("utf8")+"\n"+css,"utf8")
m=json.loads(manifest.read_text("utf8"))
assert m["id"]=="com.alkitabmawqut.tv" and m["version"]=="0.6.1"
m["version"]="0.7.0"
manifest.write_text(json.dumps(m,ensure_ascii=False,indent=2)+"\n","utf8")
# Update earlier tests to the intended layout and new version without removing prayer-core assertions.
tests=p/"tests"
for filename in ["clock-layout.test.cjs","mobile-symbol-v061.test.cjs","release-quality.test.cjs"]:
    f=tests/filename
    txt=f.read_text("utf8").replace("'0.6.1'","'0.7.0'")
    if filename=="mobile-symbol-v061.test.cjs":
        txt=txt.replace("صحيح مسلم \\\\(592\\\\) — رواية ابن نمير","مسلم ٥٩٢ — رواية ابن نمير")
    f.write_text(txt,"utf8")
f=tests/"post-prayer.test.cjs";s=f.read_text("utf8")
s=s.replace('id="post-prayer" class="post-prayer-screen hidden"','id="post-prayer" class="post-prayer-screen hidden adhkar-v07"')
s=s.replace("صحيح مسلم \\\\(591\\\\)","مسلم ٥٩١").replace("صحيح البخاري \\\\(844\\\\)","صحيح البخاري ٨٤٤").replace("صحيح مسلم \\\\(597\\\\)","صحيح مسلم ٥٩٧")
f.write_text(s,"utf8")
f=tests/"visual-v06.test.cjs";s=f.read_text("utf8");i=s.index("test('remembrance page");j=s.index("test('enhanced CC0",i)
s=s[:i]+"""test('approved eight unnumbered live text lines replace old 4-card screen',()=>{
 const section=html.slice(html.indexOf('id="post-prayer"'),html.indexOf('id="settings"'));
 assert.equal((section.match(/<section class="adhkar-line/g)||[]).length,8);
 assert.doesNotMatch(section,/class="post-prayer-card/);
 assert.match(section,/مُخْلِصِينَ لَهُ الدِّينَ/);
 assert.match(section,/مرة بعد كل صلاة، وثلاث مرات بعد الفجر والمغرب/);
 assert.match(css,/\\.adhkar-v07 \\.adhkar-lines\\{/);
});
"""+s[j:]
f.write_text(s,"utf8")
print("Applied eight line screen; appinfo version",m["version"])