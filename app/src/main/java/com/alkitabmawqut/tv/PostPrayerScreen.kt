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

private data class DhikrPage(
    val title: String,
    val text: String,
    val source: String,
    val textSize: TextUnit
)

// The source text and its reference are kept together; all three pages are static
// and do not contain animations or audio.
private val postPrayerPages = listOf(
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
fun PostPrayerScreen(prayerName: String, pageIndex: Int) {
    val safeIndex = pageIndex.coerceIn(0, postPrayerPages.lastIndex)
    val page = postPrayerPages[safeIndex]
    val shape = RoundedCornerShape(24.dp)

    Box(
        modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(navy, Color(0xFF102B43), navy)))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 38.dp, vertical = 19.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "أذكار ما بعد الصلاة",
                        color = goldBright, fontWeight = FontWeight.Bold, fontSize = 32.sp
                    )
                    Text("مما صح عن النبي ﷺ", color = cream, fontSize = 18.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("الكتاب الموقوت", color = gold, fontWeight = FontWeight.Bold, fontSize = 25.sp)
                    Text("بعد صلاة $prayerName", color = cream, fontSize = 20.sp)
                }
            }

            Box(Modifier.fillMaxWidth().height(1.dp).background(gold.copy(alpha = .72f)))

            Column(
                modifier = Modifier.fillMaxWidth().weight(1f)
                    .border(2.dp, gold, shape)
                    .background(
                        Brush.verticalGradient(listOf(Color(0xFF133550), navyCard)),
                        shape
                    )
                    .padding(horizontal = 40.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    page.title,
                    color = goldBright,
                    fontSize = 31.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    page.text,
                    color = cream,
                    fontSize = page.textSize,
                    lineHeight = (page.textSize.value + 12f).sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
                Text(
                    page.source,
                    color = gold,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "الذكر ${safeIndex + 1} من ${postPrayerPages.size}",
                    color = goldBright,
                    fontSize = 19.sp
                )
                Spacer(Modifier.width(20.dp))
                repeat(postPrayerPages.size) { position ->
                    Box(
                        Modifier.padding(horizontal = 4.dp)
                            .width(if (position == safeIndex) 36.dp else 15.dp)
                            .height(6.dp)
                            .background(
                                if (position == safeIndex) goldBright else Color(0xFF466078),
                                RoundedCornerShape(6.dp)
                            )
                    )
                }
            }
            Text(
                "تُعرض الأذكار تباعاً بعد الصلاة، ثم تعود المواقيت تلقائياً",
                color = cream.copy(alpha = .80f),
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
