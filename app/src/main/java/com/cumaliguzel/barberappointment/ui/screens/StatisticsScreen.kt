package com.cumaliguzel.barberappointment.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cumaliguzel.barberappointment.R
import com.cumaliguzel.barberappointment.viewmodel.AppointmentViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: AppointmentViewModel = viewModel()
) {
    val todayCompletedAppointments by viewModel.getTodayCompletedAppointmentsCount().collectAsState(initial = 0)
    val weeklyCompletedAppointments by viewModel.getWeeklyCompletedAppointmentsByDay().collectAsState(initial = emptyList())
    val selectedDate by viewModel.selectedDate.collectAsState()
    
    val locale = Locale.getDefault()
    val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.statistics_title), color = MaterialTheme.colorScheme.tertiary) },
                colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.primary)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Bugünün istatistikleri
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.today_stats),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedDate.format(dateFormatter),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            StatCircle(count = todayCompletedAppointments)
                        }
                    }
                }
            }
            
            // Haftalık istatistikler başlığı
            item {
                Text(
                    text = stringResource(R.string.weekly_stats),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // Grafik gösterimi
            item {
                WeeklyStatsChart(weeklyCompletedAppointments)
            }
            
            // Haftalık istatistikler liste olarak
            items(weeklyCompletedAppointments) { (date, count) ->
                WeeklyStatItem(date = date, count = count)
            }
        }
    }
}

@Composable
fun StatCircle(count: Int) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(60.dp)
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(50)
            )
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary
            )
        )
    }
}

@Composable
fun WeeklyStatItem(date: LocalDate, count: Int) {
    val locale = Locale.getDefault()
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE", locale)
    val formattedDay = date.format(dateFormatter)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = formattedDay,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        
        // Progress bar
        Row(
            modifier = Modifier.weight(2f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .height(24.dp)
                    .fillMaxWidth(count.toFloat() / (weeklyMax(count) + 1))
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(4.dp)
                    ),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 8.dp),
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

private fun weeklyMax(currentValue: Int): Int {
    // En az 5 olsun ki grafik çok küçük olmasın
    return maxOf(currentValue, 5)
}

@Composable
fun WeeklyStatsChart(weeklyStats: List<Pair<LocalDate, Int>>) {
    val maxCount = weeklyStats.maxOfOrNull { it.second }?.let { max -> maxOf(max, 1) } ?: 1
    val locale = Locale.getDefault()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                    
                    // Izgara çizgileri
                    val strokeWidth = 1.dp.toPx()
                    drawLine(
                        color = Color.LightGray,
                        start = Offset(0f, height),
                        end = Offset(width, height),
                        strokeWidth = strokeWidth
                    )
                    
                    // Her bir veri için bar çiz
                    weeklyStats.forEachIndexed { index, (_, count) ->
                        val barHeight = (count.toFloat() / maxCount) * height
                        val barCenter = barSpacing * index + (barSpacing / 2) // Çubuğun merkezi
                        val barWidth = barSpacing * 0.6f // Çubuk genişliği
                        
                        drawLine(
                            color = Color.Red,
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
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
} 