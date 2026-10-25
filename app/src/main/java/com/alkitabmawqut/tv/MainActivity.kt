package com.alkitabmawqut.tv

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.*
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val zone = ZoneId.of("Asia/Riyadh")
private val names = listOf("الفجر", "الظهر", "العصر", "المغرب", "العشاء")
private val ink = Color(0xFF071C36)
private val gold = Color(0xFFF0D08A)
private val white = Color(0xFFFFF8EA)
private val hadiths = listOf(
 "«صلاة الجماعة تفضل صلاة الفذ بسبع وعشرين درجة» — صحيح البخاري (645)",
 "«أقرب ما يكون العبد من ربه وهو ساجد فأكثروا الدعاء» — صحيح مسلم (482)",
 "«من صلى البردين دخل الجنة» — صحيح البخاري (574)، صحيح مسلم (635)"
)
private fun hhmm(min: Int): String = "%02d:%02d".format(Locale.US, (min / 60) % 24, min % 60)
private fun countdown(seconds: Long?): String {
 if (seconds == null) return "--:--"
 val n = seconds.coerceAtLeast(0)
 return if (n >= 3600) "%02d:%02d:%02d".format(Locale.US, n / 3600, n % 3600 / 60, n % 60)
 else "%02d:%02d".format(Locale.US, n / 60, n % 60)
}

class MainActivity: ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
  @Suppress("DEPRECATION")
  window.decorView.systemUiVisibility = 5894 or 1024 or 512
  setContent { PrayerTV(this) }
 }
}

@Composable
private fun PrayerTV(activity: ComponentActivity) {
 val prefs = remember { activity.getSharedPreferences("prayer_options", 0) }
 val repository = remember { PrayerRepository(activity) }
 var now by remember { mutableStateOf(ZonedDateTime.now(zone)) }
 var yesterday by remember { mutableStateOf<List<Int>?>(null) }
 var today by remember { mutableStateOf<List<Int>?>(null) }
 var tomorrow by remember { mutableStateOf<List<Int>?>(null) }
 var settings by remember { mutableStateOf(false) }
 var info by remember { mutableStateOf("جاري تحميل المواقيت") }
 var hadithNumber by remember { mutableIntStateOf(0) }
 var adhkarMinutes by remember { mutableIntStateOf(prefs.getInt("adhkar_minutes", 4).coerceIn(0,15)) }
 var adhkarPreviewUntil by remember { mutableLongStateOf(0L) }
 val offsets = remember { mutableStateListOf(*List(5) { prefs.getInt("offset_$it", 15) }.toTypedArray()) }
 val quietDurations = remember { mutableStateListOf(*List(5) { prefs.getInt("duration_$it", if (it == 2 || it == 3) 5 else 7) }.toTypedArray()) }
 val picture = remember {
  try { activity.assets.open("kaaba_background.jpg").use { BitmapFactory.decodeStream(it)?.asImageBitmap() } }
  catch (_: Exception) { null }
 }
 LaunchedEffect(Unit) {
  while (true) { now = ZonedDateTime.now(zone); delay(1000) }
 }
 LaunchedEffect(Unit) {
  while (true) { delay(24000); hadithNumber = (hadithNumber + 1) % hadiths.size }
 }
 LaunchedEffect(now.toLocalDate(), now.hour / 6) {
  val d = now.toLocalDate()
  yesterday = repository.getDay(d.minusDays(1))
  today = repository.getDay(d)
  tomorrow = repository.getDay(d.plusDays(1))
  val months = listOf(YearMonth.from(d), YearMonth.from(d.plusDays(1))).distinct()
  var synced = true
  for (m in months) if (!repository.syncMonth(m)) synced = false
  yesterday = repository.getDay(d.minusDays(1))
  today = repository.getDay(d)
  tomorrow = repository.getDay(d.plusDays(1))
  info = if (today == null) "المواقيت غير متاحة، تحقق من الإنترنت" else if (synced) "المواقيت محفوظة" else "المواقيت من الذاكرة"
 }
 val events = buildList {
  yesterday?.let { addAll(PrayerEngine.events(now.toLocalDate().minusDays(1), it, offsets, quietDurations)) }
  today?.let { addAll(PrayerEngine.events(now.toLocalDate(), it, offsets, quietDurations)) }
  tomorrow?.let { addAll(PrayerEngine.events(now.toLocalDate().plusDays(1), it, offsets, quietDurations)) }
 }
 val screen = PrayerEngine.calculate(now, events, adhkarMinutes)
 if (screen.quiet) {
  Box(Modifier.fillMaxSize().background(Color(0xFF101A25)))
  return
 }
 if (screen.postPrayerIndex != null || now.toInstant().toEpochMilli() < adhkarPreviewUntil) {
  PostPrayerScreen(screen.postPrayerIndex?.let { names[it] } ?: "الصلاة المفروضة", PostPrayerPresentation.pageAt(screen.postPrayerElapsedSeconds ?: 0L, adhkarMinutes.toLong() * 60L))
  return
 }
 BackHandler(enabled = settings) { settings = false }
 Box(Modifier.fillMaxSize().background(ink)) {
  if (picture != null) Image(bitmap = picture, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
  Box(Modifier.fillMaxSize().background(Color(0x9906162E)))
  Column(Modifier.fillMaxSize().padding(horizontal = 30.dp, vertical = 16.dp), verticalArrangement = Arrangement.SpaceBetween) {
   Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
    Column {
     Text("الكتاب الموقوت", color = gold, fontSize = 35.sp, fontWeight = FontWeight.Bold)
     Text("مكة المكرمة  •  أم القرى", color = white, fontSize = 18.sp)
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
     Text(now.format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.US)), color = Color.White, fontSize = 56.sp, fontWeight = FontWeight.Bold)
     Text("${now.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("ar"))}، ${now.dayOfMonth} ${now.month.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("ar"))} ${now.year}", color = white, fontSize = 18.sp)
    }
    Column(horizontalAlignment = Alignment.End) {
     val hijri = remember(now.toLocalDate()) {
      try { HijrahDate.from(now.toLocalDate()).format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("ar"))) }
      catch (_: Exception) { "" }
     }
     Text(hijri, color = gold, fontSize = 21.sp)
     Text("التاريخ الهجري", color = white, fontSize = 16.sp)
    }
   }
   Column(Modifier.align(Alignment.End).background(Color(0xF7FFF7E8), RoundedCornerShape(24.dp)).padding(horizontal = 38.dp, vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
    Text(if (screen.nextEvent == null) "لا تتوفر المواقيت" else "المتبقي على الإقامة", color = ink, fontSize = 26.sp, fontWeight = FontWeight.Bold)
    Text(countdown(screen.secondsUntilIqama), color = ink, fontSize = 60.sp, fontWeight = FontWeight.Bold)
    Text(screen.nextEvent?.let { "صلاة ${names[it.prayerIndex]}" } ?: info, color = ink, fontSize = 23.sp)
   }
   Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
    names.forEachIndexed { index, label ->
     val selected = screen.nextEvent?.prayerIndex == index
     val color = if (selected) ink else Color.White
     Column(Modifier.weight(1f).background(if (selected) Color(0xFFF3D18D) else Color(0xDD0B2646), RoundedCornerShape(12.dp)).padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
      Text(label, color = color, fontSize = 23.sp, fontWeight = FontWeight.Bold)
      Text(today?.get(index)?.let(::hhmm) ?: "--:--", color = color, fontSize = 27.sp)
      Text(today?.get(index)?.let { "الإقامة ${hhmm(it + offsets[index])}" } ?: "غير متاح", color = color, fontSize = 15.sp)
     }
    }
   }
   Row(Modifier.fillMaxWidth().background(Color(0xED05172D), RoundedCornerShape(12.dp)).padding(11.dp), verticalAlignment = Alignment.CenterVertically) {
    Text(hadiths[hadithNumber], color = gold, modifier = Modifier.weight(1f).basicMarquee(iterations = Int.MAX_VALUE), fontSize = 22.sp, maxLines = 1, textAlign = TextAlign.Right)
    Spacer(Modifier.width(14.dp))
    Button(onClick = { settings = true }) { Text("الإعدادات", fontSize = 18.sp) }
   }
  }
  if (settings) {
   Box(Modifier.fillMaxSize().background(Color(0xF207182D)), contentAlignment = Alignment.Center) {
    Column(Modifier.fillMaxWidth(.92f).background(ink, RoundedCornerShape(20.dp)).padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
     Text("أوقات الإقامة ومدة الصلاة", color = gold, fontSize = 28.sp, fontWeight = FontWeight.Bold)
     Text("استخدم ريموت التلفزيون لضبط الدقائق، وتحفظ التغييرات تلقائياً.", color = white, fontSize = 17.sp)
     names.forEachIndexed { i, label ->
      Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
       Text(label, color = white, fontSize = 22.sp, modifier = Modifier.width(100.dp))
       Text("الإقامة بعد الأذان", color = white, fontSize = 16.sp)
       Counter(offsets[i]) { step -> offsets[i] = (offsets[i] + step).coerceIn(0, 120); prefs.edit().putInt("offset_$i", offsets[i]).apply() }
       Text("مدة الصلاة", color = white, fontSize = 16.sp)
       Counter(quietDurations[i]) { step -> quietDurations[i] = (quietDurations[i] + step).coerceIn(1, 30); prefs.edit().putInt("duration_$i", quietDurations[i]).apply() }
      }
     }
     Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
      Text("مدة أذكار ما بعد الصلاة (0 للإيقاف)", color = white, fontSize = 17.sp)
      Counter(adhkarMinutes) { step ->
       adhkarMinutes = (adhkarMinutes + step).coerceIn(0,15)
       prefs.edit().putInt("adhkar_minutes", adhkarMinutes).apply()
      }
      Button(onClick = { settings = false; adhkarPreviewUntil = now.toInstant().toEpochMilli() + 10000L }) {
       Text("تجربة الأذكار")
      }
     }
     Button(onClick = { settings = false }, modifier = Modifier.align(Alignment.End)) { Text("حفظ وإغلاق") }
    }
   }
  }
 }
}

@Composable
private fun Counter(value: Int, onStep: (Int) -> Unit) {
 Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
  Button(onClick = { onStep(-1) }, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)) { Text("−", fontSize = 22.sp) }
  Text("$value", color = white, modifier = Modifier.width(32.dp), fontSize = 20.sp, textAlign = TextAlign.Center)
  Button(onClick = { onStep(1) }, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)) { Text("+", fontSize = 22.sp) }
 }
}
