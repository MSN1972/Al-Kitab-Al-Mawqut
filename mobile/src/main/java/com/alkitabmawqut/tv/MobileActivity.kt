package com.alkitabmawqut.tv

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.ZonedDateTime
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val midnight = Color(0xFF071827)
private val navyCard = Color(0xFF133047)
private val gold = Color(0xFFF3D38A)
private val cream = Color(0xFFFFF8EA)
private val shape = RoundedCornerShape(18.dp)
private val prayerNames = listOf("الفجر", "الظهر", "العصر", "المغرب", "العشاء")
private fun mobileTime(minute: Int) = "%02d:%02d".format(Locale.US, (minute / 60) % 24, minute % 60)
private fun remaining(seconds: Long?): String {
    if (seconds == null) return "--:--"
    val v = seconds.coerceAtLeast(0L)
    return "%02d:%02d:%02d".format(Locale.US, v / 3600, (v % 3600) / 60, v % 60)
}

class MobileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Unlike the mosque television, the phone should respect normal sleep/lock.
        setContent {
            MaterialTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    MobilePrayerApp(this)
                }
            }
        }
    }
}

@Composable
private fun MobilePrayerApp(activity: ComponentActivity) {
    val preferences = remember { activity.getSharedPreferences("prayer_options", 0) }
    val repository = remember { PrayerRepository(activity) }
    var location by remember { mutableStateOf(LocationStore.load(preferences)) }
    var now by remember(location.timezone) { mutableStateOf(ZonedDateTime.now(location.zone())) }
    var today by remember { mutableStateOf<List<Int>?>(null) }
    var previous by remember { mutableStateOf<List<Int>?>(null) }
    var following by remember { mutableStateOf<List<Int>?>(null) }
    var dataStatus by remember { mutableStateOf("جاري تحميل المواقيت...") }
    var page by remember { mutableStateOf("home") }
    val offsets = remember {
        mutableStateListOf(*List(5) { preferences.getInt("offset_$it", 15).coerceIn(0,120) }.toTypedArray())
    }
    val quiet = remember { List(5) { 7 } }

    val kaaba = remember {
        try {
            activity.assets.open("kaaba_background.jpg").use {
                BitmapFactory.decodeStream(it)?.asImageBitmap()
            }
        } catch (_: Exception) { null }
    }
    LaunchedEffect(location.timezone) {
        while (true) {
            now = ZonedDateTime.now(location.zone())
            delay(1000)
        }
    }
    LaunchedEffect(location, now.toLocalDate()) {
        val day = now.toLocalDate()
        today = repository.getDay(day, location)
        previous = repository.getDay(day.minusDays(1), location)
        following = repository.getDay(day.plusDays(1), location)
        dataStatus = if (today != null) "المواقيت المخزنة" else "جاري مزامنة المواقيت..."
        val months = listOf(YearMonth.from(day.minusDays(1)), YearMonth.from(day), YearMonth.from(day.plusDays(1))).distinct()
        var synced = true
        for (month in months) if (!repository.syncMonth(month, location)) synced = false
        today = repository.getDay(day, location)
        previous = repository.getDay(day.minusDays(1), location)
        following = repository.getDay(day.plusDays(1), location)
        dataStatus = when {
            today == null -> "لا تتوفر مواقيت محفوظة؛ تحقق من الإنترنت"
            synced -> "المواقيت محدثة"
            else -> "المواقيت من الذاكرة؛ تعذّر التحديث"
        }
    }
    val events = buildList {
        previous?.let { addAll(PrayerEngine.events(now.toLocalDate().minusDays(1), it, offsets, quiet, location.timezone)) }
        today?.let { addAll(PrayerEngine.events(now.toLocalDate(), it, offsets, quiet, location.timezone)) }
        following?.let { addAll(PrayerEngine.events(now.toLocalDate().plusDays(1), it, offsets, quiet, location.timezone)) }
    }
    // The phone remains usable during prayer; only the television turns dark.
    val next = PrayerEngine.calculate(now, events, 0)
    BackHandler(enabled = page != "home") { page = "home" }

    Column(Modifier.fillMaxSize().background(midnight)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("الكتاب الموقوت", color = gold, fontSize = 25.sp, fontWeight = FontWeight.Bold)
            Text(if (page == "home") "نسخة الجوال" else when(page) {
                "adhkar" -> "الأذكار"; else -> "الإعدادات"
            }, color = cream, fontSize = 15.sp)
        }

        when (page) {
            "home" -> MobileHome(now, location, today, next, offsets, kaaba, dataStatus,
                Modifier.weight(1f))
            "adhkar" -> MobileAdhkar(Modifier.weight(1f))
            else -> MobileSettings(location, offsets,
                onLocation = {
                    LocationStore.save(preferences, it)
                    location = it
                    now = ZonedDateTime.now(it.zone())
                    today = null; previous = null; following = null
                    dataStatus = "جاري تحميل مواقيت " + it.city
                    page = "home"
                },
                onOffset = { i, value ->
                    offsets[i] = value
                    preferences.edit().putInt("offset_$i", value).apply()
                },
                modifier = Modifier.weight(1f))
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            listOf(Triple("home", "المواقيت", "⌚"), Triple("adhkar","الأذكار","☪"),
                Triple("settings","الإعدادات","⚙")).forEach { item ->
                Button(
                    onClick = { page = item.first },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (page == item.first) gold else navyCard,
                        contentColor = if (page == item.first) midnight else cream
                    ),
                    contentPadding = PaddingValues(horizontal = 5.dp, vertical = 11.dp)
                ) { Text(item.second, fontSize = 14.sp, maxLines = 1) }
            }
        }
    }
}

@Composable
private fun MobileHome(
    now: ZonedDateTime,
    location: PrayerLocation,
    times: List<Int>?,
    next: ScreenState,
    offsets: List<Int>,
    kaaba: androidx.compose.ui.graphics.ImageBitmap?,
    status: String,
    modifier: Modifier
) {
    Column(modifier.verticalScroll(rememberScrollState()).padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            Modifier.fillMaxWidth().height(202.dp)
                .background(Brush.verticalGradient(listOf(navyCard, midnight)), shape)
        ) {
            if (kaaba != null) {
                Image(bitmap = kaaba, contentDescription = "صورة الكعبة المشرفة",
                    modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            }
            Box(Modifier.fillMaxSize().background(Color(0x99051829), shape))
            Column(
                Modifier.fillMaxSize().padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(location.label(), color = gold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(now.format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.US)),
                    color = cream, fontSize = 48.sp, fontWeight = FontWeight.Bold)
                val civil = now.format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.forLanguageTag("ar")))
                Text(civil, color = cream, fontSize = 16.sp)
                val hijri = remember(now.toLocalDate()) {
                    try { HijrahDate.from(now.toLocalDate()).format(
                        DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("ar"))
                    ) + " هـ" } catch (_:Exception) { "" }
                }
                Text(hijri, color = gold, fontSize = 16.sp)
            }
        }
        Column(
            Modifier.fillMaxWidth().border(1.dp, gold, shape)
                .background(navyCard, shape).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("المتبقي على الإقامة", color = gold, fontSize = 18.sp)
            Text(remaining(next.secondsUntilIqama), color = cream,
                fontWeight = FontWeight.Bold, fontSize = 36.sp)
            Text(next.nextEvent?.let { "صلاة " + prayerNames[it.prayerIndex] }
                ?: "لا تتوفر بيانات الإقامة", color = cream, fontSize = 18.sp)
        }
        Text("مواقيت اليوم", color = gold, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        prayerNames.forEachIndexed { i, title ->
            val minutes = times?.getOrNull(i)
            Row(
                Modifier.fillMaxWidth()
                    .background(if (next.nextEvent?.date == now.toLocalDate() &&
                        next.nextEvent?.prayerIndex == i) Color(0xFF324154) else navyCard, shape)
                    .padding(horizontal = 18.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(title, fontSize = 21.sp, color = gold, fontWeight = FontWeight.SemiBold)
                Column(horizontalAlignment = Alignment.End) {
                    Text(minutes?.let(::mobileTime) ?: "--:--",
                        fontSize = 21.sp, color = cream, fontWeight = FontWeight.Bold)
                    Text(minutes?.let { "الإقامة: " + mobileTime(it + offsets[i]) }
                        ?: "لا تتوفر المواقيت", fontSize = 13.sp, color = cream)
                }
            }
        }
        Text("طريقة الحساب: " + CalculationOptions.title(location.method),
            color = cream, fontSize = 13.sp)
        Text(status + " • يُنصح بمطابقة المواقيت مع التقويم المحلي",
            color = gold, fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun MobileAdhkar(modifier: Modifier) {
    Column(modifier.verticalScroll(rememberScrollState()).padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("أذكار ما بعد الصلاة", color = gold, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("مما صح عن النبي ﷺ — تُعرض الأذكار الثلاثة هنا دون انتقال تلقائي",
            color = cream, fontSize = 14.sp)
        postPrayerPages.forEach { dhikr ->
            Column(
                Modifier.fillMaxWidth().border(1.dp, gold, shape)
                    .background(navyCard, shape).padding(17.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(dhikr.title, color = gold, fontSize = 21.sp, fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center)
                Text(dhikr.text, color = cream, fontSize = 19.sp, lineHeight = 29.sp,
                    textAlign = TextAlign.Center)
                Text(dhikr.source, color = gold, fontSize = 14.sp)
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun MobileSettings(
    location: PrayerLocation,
    offsets: SnapshotStateList<Int>,
    onLocation: (PrayerLocation) -> Unit,
    onOffset: (Int, Int) -> Unit,
    modifier: Modifier
) {
    var city by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var searching by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf<List<PrayerLocation>>(emptyList()) }
    var chosen by remember { mutableStateOf<PrayerLocation?>(null) }
    var chosenMethod by remember { mutableIntStateOf(-1) }
    var message by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    Column(modifier.verticalScroll(rememberScrollState()).padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("إعدادات المواقيت", color = gold, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("الموقع الحالي: " + location.label(), color = cream, fontSize = 17.sp)
        Text("التوقيت: " + location.timezone + " — " + CalculationOptions.title(location.method),
            color = cream, fontSize = 14.sp)
        Button(onClick = { onLocation(PrayerLocation.MAKKAH) },
            colors = ButtonDefaults.buttonColors(containerColor = gold, contentColor = midnight)) {
            Text("العودة إلى مكة المكرمة — أم القرى")
        }
        OutlinedTextField(value = city, onValueChange = { city = it },
            label = { Text("المدينة — مثال: Damascus") },
            modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = country, onValueChange = { country = it },
            label = { Text("البلد (اختياري)") },
            modifier = Modifier.fillMaxWidth(), singleLine = true)
        Button(onClick = {
            scope.launch {
                searching = true
                chosen = null
                results = emptyList()
                message = "جاري البحث..."
                try {
                    results = CityGeocoder.search(city, country)
                    message = if (results.isEmpty()) "لا توجد نتائج، جرّب اسم المدينة بالإنجليزية"
                        else "اختر المدينة الصحيحة"
                } catch (_: Exception) {
                    message = "تعذّر البحث؛ تحقق من الإنترنت"
                } finally { searching = false }
            }
        }, enabled = !searching && city.trim().length >= 2) { Text("البحث عن المدينة") }
        if (message.isNotEmpty()) Text(message, color = gold, fontSize = 14.sp)
        if (chosen == null) {
            results.take(8).forEach { item ->
                OutlinedButton(onClick = {
                    chosen = item
                    chosenMethod = item.method
                }, modifier = Modifier.fillMaxWidth()) {
                    Text(item.label() + " — " + item.timezone, fontSize = 15.sp, color = gold)
                }
            }
        } else {
            Text("المدينة المختارة: " + chosen!!.label(), color = gold, fontSize = 18.sp)
            OutlinedButton(onClick = {
                val index = CalculationOptions.choices.indexOfFirst { it.first == chosenMethod }
                chosenMethod = CalculationOptions.choices[(index + 1).coerceAtLeast(0) %
                    CalculationOptions.choices.size].first
            }, modifier = Modifier.fillMaxWidth()) {
                Text("طريقة الحساب: " + CalculationOptions.title(chosenMethod), color = gold)
            }
            Button(onClick = { onLocation(chosen!!.copy(method = chosenMethod)) },
                colors = ButtonDefaults.buttonColors(containerColor = gold, contentColor = midnight)) {
                Text("اعتماد المدينة وطريقة الحساب")
            }
        }
        Text("ضبط الإقامة (بالدقائق بعد الأذان)", color = gold,
            fontSize = 18.sp, fontWeight = FontWeight.Bold)
        prayerNames.forEachIndexed { i, name ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text(name, color = cream, fontSize = 17.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(onClick = { onOffset(i, (offsets[i] - 1).coerceAtLeast(0)) },
                        contentPadding = PaddingValues(horizontal = 12.dp)) { Text("−") }
                    Text("  " + offsets[i] + "  ", color = gold, fontSize = 17.sp)
                    Button(onClick = { onOffset(i, (offsets[i] + 1).coerceAtMost(120)) },
                        contentPadding = PaddingValues(horizontal = 12.dp)) { Text("+") }
                }
            }
        }
        Text("تنبيه: اختلاف طريقة الحساب يؤدي إلى اختلاف المواقيت، وموعد الإقامة قابل للضبط محلياً.",
            color = cream, fontSize = 13.sp)
        Spacer(Modifier.height(8.dp))
    }
}
