from pathlib import Path
import json

root=Path("webos")
htmlfile=root/"index.html"
cssfile=root/"style.css"
manifest=root/"appinfo.json"

html=htmlfile.read_text(encoding="utf-8")
old='''      <div class="clock-panel"><div id="clock" class="clock">--:--:--</div><div id="gregorian" class="date-text">—</div></div>
      <div class="hijri-panel"><div id="hijri" class="hijri-date">—</div><small>التاريخ الهجري</small></div>'''
new='''      <div class="clock-panel" aria-label="الساعة والتاريخ الميلادي والهجري">
        <div id="clock" class="clock">--:--:--</div>
        <div class="dates-stack">
          <div id="gregorian" class="date-text" aria-label="التاريخ الميلادي">—</div>
          <div id="hijri" class="date-text" aria-label="التاريخ الهجري">—</div>
        </div>
      </div>'''
if html.count(old)!=1: raise ValueError("Expected old date markup exactly once")
html=html.replace(old,new)
if 'class="hijri-panel"' in html: raise ValueError("Separate Hijri panel still present")
htmlfile.write_text(html, encoding="utf-8")

css=cssfile.read_text(encoding="utf-8")
replacements=[
 ('grid-template-columns:1fr 1.4fr 1fr','grid-template-columns:minmax(0,1fr) minmax(0,1.48fr) minmax(0,1fr)'),
 ('padding:1vw}.brand strong','padding:1vw;grid-column:1;grid-row:1}.brand strong'),
 ('box-shadow:0 3px 20px #0007;padding:.7vh 2vw 1.3vh}','box-shadow:0 3px 20px #0007;padding:.5vh 2vw 1.15vh;grid-column:2;grid-row:1;min-width:0}'),
 ('.date-text{font-size:clamp(16px,1.55vw,32px);color:var(--gold)}.hijri-panel{text-align:left;padding:1.1vw}.hijri-date{font-size:clamp(22px,2vw,43px);color:var(--gold);font-weight:bold}.hijri-panel small{font-size:clamp(12px,1.2vw,25px)}',
  '.dates-stack{display:flex;flex-direction:column;align-items:center;justify-content:center;gap:.35vh;padding-top:.35vh}\n.date-text{font-size:clamp(18px,1.55vw,33px);font-weight:700;line-height:1.42;color:#f9d98f;text-shadow:0 1px 4px rgba(0,0,0,.72);white-space:nowrap;direction:rtl}')
]
for before,after in replacements:
    if css.count(before)!=1: raise ValueError(f"Expected CSS segment exactly once: {before[:30]}")
    css=css.replace(before,after)
if '.hijri-panel' in css: raise ValueError("Separate Hijri styling still present")
cssfile.write_text(css,encoding="utf-8")

data=json.loads(manifest.read_text(encoding="utf-8"))
if data["id"]!="com.alkitabmawqut.tv" or data["version"]!="0.2.0": raise ValueError("Unexpected base app identity/version")
data["version"]="0.3.0"
manifest.write_text(json.dumps(data,ensure_ascii=False,indent=2)+"\n",encoding="utf-8")
print("Updated one-panel Hijri/Gregorian design to v0.3.0; existing icon and settings preserved.")
