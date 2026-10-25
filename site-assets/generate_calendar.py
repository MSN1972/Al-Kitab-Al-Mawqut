#!/usr/bin/env python3
"""Generate checked daily Makkah timetables for static LG TV browser deployment.
Source: AlAdhan method 4 (Umm al-Qura), not an official Saudi timetable."""
import json, urllib.request, calendar, time
from datetime import datetime, timedelta, timezone
from pathlib import Path

TZ = timezone(timedelta(hours=3))
today = datetime.now(TZ).date()
months = sorted({(d.year, d.month) for d in (today.replace(day=1) - timedelta(days=1), today, (today.replace(day=28) + timedelta(days=5)))})
names = ("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
all_months = {}
for yr, mo in months:
    url = f"https://api.aladhan.com/v1/calendar/{yr}/{mo}?latitude=21.4225&longitude=39.8262&method=4&timezonestring=Asia%2FRiyadh"
    error = None
    for retry in range(4):
        try:
            req = urllib.request.Request(url, headers={"User-Agent": "AlKitabMawqut-TV-Build/0.2"})
            with urllib.request.urlopen(req, timeout=25) as response:
                payload = json.load(response)
            if payload.get("code") != 200:
                raise ValueError("Calendar returned invalid code")
            data = payload["data"]
            dates = {}
            for entry in data:
                date_text = entry["date"]["gregorian"]["date"]
                dd, mm, yyyy = map(int, date_text.split("-"))
                if (yyyy, mm) != (yr, mo): continue
                clock = []
                for name in names:
                    clock_str = entry["timings"][name].split(" ")[0]
                    hh, minute = map(int, clock_str.split(":"))
                    assert 0 <= hh <= 23 and 0 <= minute <= 59, "Bad time"
                    clock.append(hh * 60 + minute)
                assert all(a < b for a, b in zip(clock, clock[1:])), "Prayer order invalid"
                dates[f"{yyyy:04d}-{mm:02d}-{dd:02d}"] = clock
            assert len(dates) == calendar.monthrange(yr, mo)[1], "Incomplete calendar"
            all_months[f"{yr:04d}-{mo:02d}"] = {"calendar": dates, "fetchedAt": int(time.time()*1000)}
            break
        except Exception as exc:
            error = exc
            time.sleep(2 + 2*retry)
    else:
        raise RuntimeError(f"Could not obtain safe valid calendar for {yr}-{mo:02d}: {error}")
root = Path("webos")
root.mkdir(exist_ok=True)
js = "/* Generated from AlAdhan method 4, Makkah. Validate with local mosque. */\n" + \
     "(function(){try{localStorage.setItem('alkitab-webos-calendar-v1',JSON.stringify(" + \
     json.dumps(all_months, ensure_ascii=False, separators=(",",":")) + \
     "));}catch(e){}})();\n"
(root / "calendar-boot.js").write_text(js, encoding="utf-8")
print("Validated static prayer data:", ", ".join(all_months), "days:", sum(len(v["calendar"]) for v in all_months.values()))
