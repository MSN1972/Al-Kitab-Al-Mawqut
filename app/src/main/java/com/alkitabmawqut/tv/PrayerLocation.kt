package com.alkitabmawqut.tv

import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.time.ZoneId

data class PrayerLocation(
    val id: Long,
    val city: String,
    val country: String,
    val countryCode: String,
    val region: String,
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val method: Int
) {
    fun zone() = ZoneId.of(timezone)
    fun cacheKey() = id.toString() + "_" + method
    fun label() = city + "، " + country
    fun encode(): String = JSONObject().apply {
        put("id",id);put("city",city);put("country",country)
        put("countryCode",countryCode);put("region",region)
        put("latitude",latitude);put("longitude",longitude)
        put("timezone",timezone);put("method",method)
    }.toString()
    companion object {
        val MAKKAH = PrayerLocation(0L,"مكة المكرمة","السعودية","SA","مكة المكرمة",
            21.4225,39.8262,"Asia/Riyadh",4)
        fun decode(raw: String): PrayerLocation? = try {
            val o=JSONObject(raw)
            val tz=o.getString("timezone")
            ZoneId.of(tz)
            val lat=o.getDouble("latitude");val lon=o.getDouble("longitude")
            val result=PrayerLocation(o.getLong("id"),o.getString("city"),o.getString("country"),
                o.optString("countryCode"),o.optString("region"),lat,lon,tz,o.getInt("method"))
            result.takeIf {
                it.city.isNotBlank() && it.country.isNotBlank()
                && lat.isFinite() && lon.isFinite() && lat in -90.0..90.0 && lon in -180.0..180.0
                && it.method in CalculationOptions.ids
            }
        } catch (_: Exception) { null }
    }
}
object CalculationOptions {
    val choices=listOf(
        -1 to "تلقائي حسب موقع المدينة",
        4 to "تقويم أم القرى - مكة",
        3 to "رابطة العالم الإسلامي",
        2 to "أمريكا الشمالية",
        5 to "هيئة المساحة المصرية",
        1 to "جامعة كراتشي",
        8 to "منطقة الخليج",
        13 to "الشؤون الدينية التركية",
        17 to "جاكيم - ماليزيا",
        23 to "الأوقاف الأردنية"
    )
    val ids=choices.map { it.first }.toSet()
    fun title(id:Int)=choices.firstOrNull{it.first==id}?.second ?: choices.first().second
}
object LocationStore {
    private const val KEY="selected_prayer_location_v1"
    fun load(prefs: SharedPreferences): PrayerLocation =
        prefs.getString(KEY,null)?.let(PrayerLocation::decode) ?: PrayerLocation.MAKKAH
    fun save(prefs: SharedPreferences, value: PrayerLocation) {
        prefs.edit().putString(KEY,value.encode()).apply()
    }
}
object CityGeocoder {
    suspend fun search(city:String, country:String):List<PrayerLocation> = withContext(Dispatchers.IO) {
        val term=city.trim()
        require(term.length>=2) {"اكتب حرفين على الأقل من اسم المدينة"}
        val searchTerm=if(country.isBlank())term else term+", "+country.trim()
        val url=URL("https://geocoding-api.open-meteo.com/v1/search?name="+
            URLEncoder.encode(searchTerm,"UTF-8")+"&count=12&language=ar&format=json")
        val connection=(url.openConnection() as HttpURLConnection).apply {
            connectTimeout=10000;readTimeout=10000;requestMethod="GET"
        }
        try {
            if(connection.responseCode!=200) error("تعذّر الاتصال بخدمة المدن")
            val rows=JSONObject(connection.inputStream.bufferedReader().use{it.readText()})
                .optJSONArray("results") ?: return@withContext emptyList()
            buildList {
                for(i in 0 until rows.length()) {
                    val v=rows.optJSONObject(i) ?: continue
                    try {
                        val tz=v.getString("timezone");ZoneId.of(tz)
                        val lat=v.getDouble("latitude");val lon=v.getDouble("longitude")
                        if(!lat.isFinite()||!lon.isFinite()||lat !in -90.0..90.0||lon !in -180.0..180.0) continue
                        val cc=v.optString("country_code")
                        add(PrayerLocation(v.getLong("id"),v.getString("name"),v.getString("country"),
                            cc,v.optString("admin1"),lat,lon,tz,if(cc=="SA")4 else -1))
                    }catch(_:Exception){}
                }
            }
        }finally {connection.disconnect()}
    }
}
