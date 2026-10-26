/* Shared location, time zone and method support for LG webOS. */
(function(root){
 'use strict';
 const MAKKAH={id:0,city:'مكة المكرمة',country:'السعودية',region:'مكة المكرمة',
   latitude:21.4225,longitude:39.8262,timezone:'Asia/Riyadh',method:4};
 const methods=[[-1,'تلقائي حسب الموقع'],[4,'أم القرى'],[3,'رابطة العالم الإسلامي'],
   [2,'أمريكا الشمالية'],[5,'الهيئة المصرية'],[1,'كراتشي'],[13,'تركيا'],[17,'ماليزيا'],[23,'الأوقاف الأردنية']];
 function validZone(zone){try{new Intl.DateTimeFormat('en-US',{timeZone:zone}).format(new Date());return true}catch(_){return false}}
 function normalize(value){
   if(!value||typeof value!=='object')return Object.assign({},MAKKAH);
   const lat=Number(value.latitude),lon=Number(value.longitude),method=Number(value.method);
   if(!Number.isFinite(lat)||Math.abs(lat)>90||!Number.isFinite(lon)||Math.abs(lon)>180
       ||!validZone(value.timezone)||!methods.some(m=>m[0]===method)
       ||!String(value.city||'').trim()||!String(value.country||'').trim())return Object.assign({},MAKKAH);
   return {id:String(value.id||'0'),city:String(value.city),country:String(value.country),
     region:String(value.region||''),latitude:lat,longitude:lon,timezone:value.timezone,method};
 }
 function key(location){return [location.id,location.method,location.timezone].join('_').replace(/[^a-zA-Z0-9_-]/g,'_')}
 async function search(city,country){
   city=String(city||'').trim();country=String(country||'').trim();
   if(city.length<2)throw Error('اكتب حرفين على الأقل من اسم المدينة');
   const query=city+(country?', '+country:'');
   const ctl=new AbortController(),timer=setTimeout(()=>ctl.abort(),10000);
   try{
     const r=await fetch('https://geocoding-api.open-meteo.com/v1/search?name='+encodeURIComponent(query)+'&count=12&language=ar&format=json',
       {signal:ctl.signal});
     if(!r.ok)throw Error('تعذّر الاتصال بدليل المدن');
     const data=await r.json();
     return (Array.isArray(data.results)?data.results:[]).filter(x=>
       x.name&&x.country&&validZone(x.timezone)&&Number.isFinite(x.latitude)&&Number.isFinite(x.longitude))
       .map(x=>({id:String(x.id),city:String(x.name),country:String(x.country),
         region:String(x.admin1||''),latitude:x.latitude,longitude:x.longitude,
         timezone:x.timezone,method:x.country_code==='SA'?4:-1}));
   }finally{clearTimeout(timer)}
 }
 const api={MAKKAH,methods,validZone,normalize,key,search};
 if(typeof module!=='undefined'&&module.exports)module.exports=api;
 root.PrayerLocationLayer=api;
})(typeof window!=='undefined'?window:globalThis);
