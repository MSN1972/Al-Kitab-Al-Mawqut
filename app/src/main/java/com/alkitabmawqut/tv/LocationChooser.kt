package com.alkitabmawqut.tv

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.time.ZoneId

@Composable
fun LocationChooser(
    active: PrayerLocation,
    onCancel: () -> Unit,
    onApply: (PrayerLocation) -> Unit
) {
    var city by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<PrayerLocation>>(emptyList()) }
    var selected by remember { mutableStateOf<PrayerLocation?>(null) }
    var zoneInput by remember { mutableStateOf("") }
    var methodId by remember { mutableIntStateOf(-1) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("اكتب اسم المدينة والبلد (البلد اختياري)") }
    val scope = rememberCoroutineScope()
    val gold = Color(0xFFF3D38A)
    val white = Color(0xFFFFF7E6)
    BackHandler(onBack = onCancel)
    Column(
        Modifier.fillMaxSize().background(Color(0xFF071B31)).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text("الموقع ومواقيت الصلاة", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = gold)
            Button(onClick = onCancel) { Text("رجوع") }
        }
        Text("الموقع الحالي: " + active.label() + " — " + active.timezone,
            color = white, fontSize = 17.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(city, {city=it}, Modifier.weight(1f), label={Text("المدينة")}, singleLine=true)
            OutlinedTextField(country, {country=it}, Modifier.weight(1f), label={Text("البلد (اختياري)")}, singleLine=true)
            Button(onClick = {
                scope.launch {
                    busy = true; selected = null; results = emptyList()
                    message = "جاري البحث عن المدينة..."
                    try {
                        results = CityGeocoder.search(city,country)
                        message = if (results.isEmpty()) "لا توجد نتائج، جرّب التهجئة الإنجليزية"
                                  else "اختر المدينة الصحيحة"
                    } catch(e: Exception) {
                        message = e.message ?: "تعذّر الاتصال؛ تحقق من الإنترنت"
                    } finally { busy = false }
                }
            }, enabled = !busy && city.trim().length >= 2) { Text("بحث") }
        }
        Text(message, color = gold, fontSize = 16.sp)
        if (selected == null) {
            Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)) {
                results.forEach { item ->
                    Button(
                        onClick = {
                            selected=item;zoneInput=item.timezone;methodId=item.method
                        }, modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(item.label() + (if(item.region.isBlank()) "" else " — " + item.region)
                            + " | " + item.timezone, fontSize = 18.sp)
                    }
                }
            }
        } else {
            val choice = selected!!
            Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("المدينة المختارة: " + choice.label(), color=gold, fontSize=24.sp)
                Text("الإحداثيات: " + choice.latitude + "، " + choice.longitude,
                    color=white, fontSize=15.sp)
                OutlinedTextField(zoneInput, {zoneInput=it}, Modifier.fillMaxWidth(),
                    label={Text("المنطقة الزمنية (مثال: Asia/Riyadh)")}, singleLine=true)
                Button(onClick = {
                    val idx=CalculationOptions.choices.indexOfFirst { it.first == methodId }
                    methodId=CalculationOptions.choices[(idx+1).coerceAtLeast(0)%CalculationOptions.choices.size].first
                }) {
                    Text("طريقة الحساب: " + CalculationOptions.title(methodId) + " (تغيير)")
                }
                Text("تحقق من مواقيت الجهة الرسمية أو المسجد المحلي، فقد تختلف طرق الحساب.",
                    color=white, fontSize=15.sp)
                Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                    Button(onClick = {
                        try {
                            val zone=ZoneId.of(zoneInput.trim()).id
                            onApply(choice.copy(timezone=zone,method=methodId))
                        } catch (_:Exception) {
                            message="منطقة زمنية غير صحيحة؛ مثال: Europe/London"
                        }
                    }) { Text("اعتماد الموقع", fontSize=19.sp) }
                    Button(onClick={selected=null;message="اختر مدينة أخرى"}) { Text("اختيار آخر") }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween,
            verticalAlignment=Alignment.CenterVertically) {
            Button(onClick={onApply(PrayerLocation.MAKKAH)}) { Text("العودة إلى مكة — أم القرى") }
            Text("تُحفظ المواقيت لكل موقع بعد الاتصال الأول بالإنترنت.",
                fontSize=14.sp,color=white)
        }
    }
}
