package com.cumaliguzel.barberappointment.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cumaliguzel.barberappointment.R
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*


@Composable
fun WeeklyStatsPieChart(
    weeklyData: List<Pair<LocalDate, Int>>,
    modifier: Modifier = Modifier
) {
    if (weeklyData.isEmpty()) {
        return
    }

    // Toplam randevu sayısını hesapla
    val totalCount = weeklyData.sumOf { it.second }
    if (totalCount == 0) {
        // Veri yoksa, daha bilgilendirici bir boş durum göster
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = stringResource(R.string.pie_chart_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text =  stringResource(R.string.pie_chart_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    // Animasyon için
    val animateFloat = remember { Animatable(0f) }
    LaunchedEffect(weeklyData) {
        animateFloat.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )
    }

    // Pasta grafiği için renkler
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.tertiaryContainer,
        MaterialTheme.colorScheme.surfaceTint
    )

    // İç daire rengi önceden alınıyor - 129. satırdaki hata için çözüm
    val innerCircleColor = MaterialTheme.colorScheme.surfaceVariant

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.pie_chart_weekly),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Pasta grafiği çizimi
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val diameter = size.minDimension
                    val radius = diameter / 2
                    val innerRadius = radius * 0.6f // İç daire boyutu
                    val centerX = size.width / 2
                    val centerY = size.height / 2
                    
                    var startAngle = -90f // Grafiği üstten başlat
                    
                    weeklyData.forEachIndexed { index, (date, count) ->
                        if (count > 0) {
                            val percentage = count.toFloat() / totalCount.toFloat()
                            val sweepAngle = 360f * percentage * animateFloat.value
                            
                            // Pasta dilimini çiz
                            drawArc(
                                color = colors[index % colors.size],
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = true,
                                topLeft = Offset(centerX - radius, centerY - radius),
                                size = Size(diameter, diameter)
                            )
                            
                            startAngle += sweepAngle
                        }
                    }
                    
                    // İç daireyi çiz (ortada boşluk oluşturmak için)
                    // ⚠️ Burada düzeltme yapıldı - Compose context dışında MaterialTheme kullanılamaz
                    drawCircle(
                        color = innerCircleColor,  // Dışarıda tanımlanan rengi kullan
                        radius = innerRadius,
                        center = Offset(centerX, centerY)
                    )
                }
                
                // Ortadaki toplam sayı
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$totalCount",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.pie_chart_appoinment),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Açıklama bölümü
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                weeklyData.forEachIndexed { index, (date, count) ->
                    if (count > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Renk göstergesi
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(
                                        color = colors[index % colors.size],
                                        shape = CircleShape
                                    )
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            // Gün ve sayı bilgisi
                            Text(
                                text = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            
                            Text(
                                text = "$count randevu",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
} 