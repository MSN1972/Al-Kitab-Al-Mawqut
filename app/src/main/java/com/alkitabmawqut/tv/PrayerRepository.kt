package com.alkitabmawqut.tv

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class PrayerRepository(context: Context) {
    private val names = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
    private val prefs = context.getSharedPreferences("prayer_calendar", Context.MODE_PRIVATE)
    fun getDay(day: LocalDate): List<Int>? {
        val value = prefs.getString("month_${YearMonth.from(day)}", null) ?: return null
        return try {
            val arr = JSONObject(value).getJSONArray(day.toString())
            if (arr.length() != 5) null else (0..4).map { arr.getInt(it) }
        } catch (_: Exception) { null }
    }
    suspend fun syncMonth(month: YearMonth): Boolean = withContext(Dispatchers.IO) {
        val endpoint = "https://api.aladhan.com/v1/calendar/${month.year}/${month.monthValue}?latitude=21.4225&longitude=39.8262&method=4&timezonestring=Asia%2FRiyadh"
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            connectTimeout = 12000; readTimeout = 12000; requestMethod = "GET"
        }
        try {
            if (connection.responseCode != 200) return@withContext false
            val root = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
            if (root.optInt("code") != 200) return@withContext false
            val calendar = root.getJSONArray("data")
            val saved = JSONObject()
            for (i in 0 until calendar.length()) {
                val item = calendar.getJSONObject(i)
                val rawDate = item.getJSONObject("date").getJSONObject("gregorian").getString("date")
                val day = LocalDate.parse(rawDate, DateTimeFormatter.ofPattern("dd-MM-uuuu"))
                if (YearMonth.from(day) != month) continue
                val timings = item.getJSONObject("timings")
                val mins = JSONArray()
                for (name in names) {
                    val match = Regex("^(\\d{1,2}):(\\d{2})").find(timings.getString(name)) ?: error("Invalid time")
                    val hh = match.groupValues[1].toInt()
                    val mm = match.groupValues[2].toInt()
                    require(hh in 0..23 && mm in 0..59)
                    mins.put(hh * 60 + mm)
                }
                saved.put(day.toString(), mins)
            }
            if (saved.length() != month.lengthOfMonth()) return@withContext false
            prefs.edit().putString("month_$month", saved.toString()).apply()
            true
        } catch (_: Exception) { false } finally { connection.disconnect() }
    }
}
