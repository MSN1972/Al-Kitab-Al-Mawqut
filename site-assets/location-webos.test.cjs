const test=require('node:test');
const assert=require('node:assert/strict');
const fs=require('node:fs');
const loc=require('../location-layer.js');
const core=require('../prayer-core.js');
test('Makkah default remains method four and Riyadh clock',()=>{
 assert.equal(loc.MAKKAH.method,4);
 assert.equal(loc.MAKKAH.timezone,'Asia/Riyadh');
 const p=core.partsForZone(Date.parse('2026-10-26T09:00:00Z'),'Asia/Riyadh');
 assert.equal(p.hhmm,'12:00');
});
test('timezone conversion respects DST in London and does not reuse Makkah time',()=>{
 const summer=core.epochInZone('2026-07-01',12*60,'Europe/London');
 const winter=core.epochInZone('2026-01-01',12*60,'Europe/London');
 assert.equal(new Date(summer).toISOString(),'2026-07-01T11:00:00.000Z');
 assert.equal(new Date(winter).toISOString(),'2026-01-01T12:00:00.000Z');
});
test('two cities are isolated by location cache key and invalid zones rejected',()=>{
 const a=loc.normalize(loc.MAKKAH);
 assert.notEqual(loc.key(a),loc.key({...a,id:54321}));
 assert.equal(loc.validZone('NoSuch/Zone'),false);
});
test('LG DOM and integration do not use fixed Makkah coords for all cities',()=>{
 const html=fs.readFileSync('webos/index.html','utf8');
 const main=fs.readFileSync('webos/main.js','utf8');
 assert.match(html,/id="location-dialog"/);
 assert.match(html,/geocoding-api\.open-meteo\.com/);
 assert.match(main,/selectedLocation\.latitude/);
 assert.match(main,/core\.partsForZone\(now,location\.timezone\)/);
 assert.doesNotMatch(main,/latitude=21\.4225&longitude=39\.8262/);
});
