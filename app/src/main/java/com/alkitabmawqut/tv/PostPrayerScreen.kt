package com.alkitabmawqut.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val navy = Color(0xFF0A1C30)
private val panel = Color(0xFF14304A)
private val gold = Color(0xFFF0D08A)
private val white = Color(0xFFFFF8EA)

@Composable
fun PostPrayerScreen(prayerName: String) {
 Column(Modifier.fillMaxSize().background(navy).padding(26.dp), verticalArrangement=Arrangement.spacedBy(13.dp)) {
  Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween) {
   Text("أذكار ما بعد الصلاة", color=gold, fontWeight=FontWeight.Bold, fontSize=36.sp)
   Text("بعد صلاة $prayerName", color=white, fontSize=24.sp)
  }
  Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement=Arrangement.spacedBy(14.dp)) {
   PostDhikrCard("الاستغفار بعد السلام",
    "أَسْتَغْفِرُ اللَّهَ × ٣\n\nاللَّهُمَّ أَنْتَ السَّلَامُ وَمِنْكَ السَّلَامُ، تَبَارَكْتَ ذَا الْجَلَالِ وَالْإِكْرَامِ",
    "صحيح مسلم (591)",Modifier.weight(1f).fillMaxHeight())
   PostDhikrCard("التهليل والدعاء",
    "لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ، وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ\n\nاللَّهُمَّ لَا مَانِعَ لِمَا أَعْطَيْتَ، وَلَا مُعْطِيَ لِمَا مَنَعْتَ، وَلَا يَنْفَعُ ذَا الْجَدِّ مِنْكَ الْجَدُّ",
    "صحيح البخاري (844)",Modifier.weight(1f).fillMaxHeight())
  }
  PostDhikrCard("التسبيح والتحميد والتكبير",
   "سُبْحَانَ اللَّهِ × ٣٣     الْحَمْدُ لِلَّهِ × ٣٣     اللَّهُ أَكْبَرُ × ٣٣\n\nتمام المائة: لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ، وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
   "صحيح مسلم (597)",Modifier.fillMaxWidth().weight(.85f))
  Text("من الصحيحين — العودة إلى المواقيت تلقائياً",color=white,fontSize=15.sp,modifier=Modifier.fillMaxWidth(),textAlign=TextAlign.Center)
 }
}

@Composable
private fun PostDhikrCard(title: String, content: String, source: String, modifier: Modifier) {
 Column(modifier.background(panel,RoundedCornerShape(20.dp)).padding(16.dp),
  horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.SpaceBetween) {
  Text(title,color=gold,fontSize=23.sp,fontWeight=FontWeight.Bold,textAlign=TextAlign.Center)
  Text(content,color=white,fontSize=19.sp,lineHeight=27.sp,textAlign=TextAlign.Center)
  Text(source,color=gold,fontSize=15.sp)
 }
}
