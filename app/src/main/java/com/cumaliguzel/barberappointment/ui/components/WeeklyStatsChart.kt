package com.cumaliguzel.barberappointment.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun WeeklyStatsChart(weeklyStats: List<Pair<LocalDate, Int>>) {
    val maxCount = weeklyStats.maxOfOrNull { it.second }?.let { max -> maxOf(max, 1) } ?: 1
    val locale = Locale.getDefault()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Grafik alanı
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val barSpacing = width / weeklyStats.size // Her çubuk için eşit alan

                    // Izgara çizgileri - Tema renklerine uygun hale getirildi
                    val strokeWidth = 1.dp.toPx()
                    drawLine(
                        color = Color.White,
                        start = Offset(0f, height),
                        end = Offset(width, height),
                        strokeWidth = strokeWidth
                    )

                    // Her bir veri için bar çiz - Kırmızı yerine tema rengi kullanıldı
                    weeklyStats.forEachIndexed { index, (_, count) ->
                        val barHeight = (count.toFloat() / maxCount) * height
                        val barCenter = barSpacing * index + (barSpacing / 2) // Çubuğun merkezi
                        val barWidth = barSpacing * 0.6f // Çubuk genişliği

                        drawLine(
                            color = Color.Red, // ColorGreen veya MaterialTheme.colorScheme.primary de kullanılabilir
                            start = Offset(barCenter, height),
                            end = Offset(barCenter, height - barHeight),
                            strokeWidth = barWidth
                        )
                    }
                }
            }

            // Gün isimleri
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weeklyStats.forEach { (date, _) ->
                    Text(
                        text = date.format(DateTimeFormatter.ofPattern("E", locale)),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        fontSize = 10.sp,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}