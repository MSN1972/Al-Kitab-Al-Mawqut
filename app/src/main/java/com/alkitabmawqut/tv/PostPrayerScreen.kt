package com.alkitabmawqut.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val navy = Color(0xFF071827)
private val navyCard = Color(0xFF0D2941)
private val gold = Color(0xFFF3D38A)
private val goldBright = Color(0xFFFFE9B4)
private val cream = Color(0xFFFFFAED)

internal data class DhikrPage(
    val title: String,
    val text: String,
    val source: String,
    val textSize: TextUnit
)

// The source text and its reference are kept together; all three pages are static
// and do not contain animations or audio.
internal val postPrayerPages = listOf(
    DhikrPage(
        "الاستغفار بعد السلام",
        "أَسْتَغْفِرُ اللَّهَ  × ٣\n\nاللَّهُمَّ أَنْتَ السَّلَامُ وَمِنْكَ السَّلَامُ، تَبَارَكْتَ ذَا الْجَلَالِ وَالْإِكْرَامِ",
        "صحيح مسلم (591)",
        32.sp
    ),
    DhikrPage(
        "التهليل والدعاء",
        "لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ، وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ\n\nاللَّهُمَّ لَا مَانِعَ لِمَا أَعْطَيْتَ، وَلَا مُعْطِيَ لِمَا مَنَعْتَ، وَلَا يَنْفَعُ ذَا الْجَدِّ مِنْكَ الْجَدُّ",
        "صحيح البخاري (844)",
        27.sp
    ),
    DhikrPage(
        "التسبيح والتحميد والتكبير",
        "سُبْحَانَ اللَّهِ  × ٣٣\nالْحَمْدُ لِلَّهِ  × ٣٣\nاللَّهُ أَكْبَرُ  × ٣٣\n\nتمام المائة: لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ، وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
        "صحيح مسلم (597)",
        27.sp
    )
)

@Composable
fun PostPrayerScreen(prayerName: String, @Suppress("UNUSED_PARAMETER") pageIndex: Int) {
    // All three adhkar are always displayed, with no automatic page transitions.
    val compactLabel = if (prayerName == "الصلاة المفروضة" || prayerName == "عرض تجريبي")
        "عرض تجريبي للأذكار" else "بعد صلاة $prayerName"
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(navy, Color(0xFF102B43), navy))
        )
    ) {
        val compact = maxHeight < 520.dp
        val gap = if (compact) 7.dp else 12.dp
        Column(
            modifier = Modifier.fillMaxSize().padding(
                horizontal = if (compact) 13.dp else 27.dp,
                vertical = if (compact) 9.dp else 17.dp
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(gap)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("أذكار ما بعد الصلاة", color = goldBright,
                        fontWeight = FontWeight.Bold, fontSize = if (compact) 26.sp else 32.sp)
                    Text("مما صح عن النبي ﷺ", color = cream,
                        fontSize = if (compact) 14.sp else 18.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("الكتاب الموقوت", color = gold,
                        fontWeight = FontWeight.Bold, fontSize = if (compact) 20.sp else 25.sp)
                    Text(compactLabel, color = cream,
                        fontSize = if (compact) 14.sp else 18.sp)
                }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(gold.copy(alpha = .72f)))
            Row(
                Modifier.fillMaxWidth().weight(1.25f),
                horizontalArrangement = Arrangement.spacedBy(gap)
            ) {
                DhikrCard(postPrayerPages[0], Modifier.weight(1f).fillMaxHeight(), compact, false)
                DhikrCard(postPrayerPages[1], Modifier.weight(1f).fillMaxHeight(), compact, false)
            }
            DhikrCard(postPrayerPages[2], Modifier.fillMaxWidth().weight(.92f), compact, true)
            Text("تظهر الأذكار معاً بعد الصلاة، ثم تعود المواقيت تلقائياً",
                color = cream.copy(alpha = .82f),
                fontSize = if (compact) 12.sp else 15.sp,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun DhikrCard(page: DhikrPage, modifier: Modifier, compact: Boolean, wide: Boolean) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier.border(1.5.dp, gold, shape)
            .background(Brush.verticalGradient(listOf(Color(0xFF153651), navyCard)), shape)
            .padding(horizontal = if (compact) 12.dp else 18.dp,
                vertical = if (compact) 5.dp else 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(page.title, color = goldBright, fontWeight = FontWeight.Bold,
            fontSize = if (compact) 19.sp else 25.sp, textAlign = TextAlign.Center)
        val fontSize = if (compact) (if (wide) 19.sp else 17.sp) else (if (wide) 24.sp else 21.sp)
        Text(page.text, color = cream, fontWeight = FontWeight.Medium,
            fontSize = fontSize, lineHeight = (fontSize.value + if (compact) 5f else 8f).sp,
            textAlign = TextAlign.Center)
        Text(page.source, color = gold, fontWeight = FontWeight.Bold,
            fontSize = if (compact) 13.sp else 17.sp, textAlign = TextAlign.Center)
    }
}
