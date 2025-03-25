package com.cumaliguzel.barberappointment.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun DateCard(
    date: LocalDate,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val today = LocalDate.now()
    val isToday = date.isEqual(today)
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(vertical = 4.dp, horizontal = 4.dp)
            .width(65.dp)
    ) {
        // Gün adı (Pts, Salı, vb.)
        Text(
            text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.tertiary,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Tarih kutusu
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    when {
                        isSelected -> MaterialTheme.colorScheme.primary
                        isToday -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.secondary
                    }
                )
                .clickable(onClick = onClick)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Ayın günü (1, 2, 3, ...)
                Text(
                    text = date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                )
                
                // Küçük ay gösterimi
                Text(
                    text = date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()).substring(0, 3),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontSize = 10.sp
                )
            }
        }
        
        // Bugün işareti
        if (isToday) {
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}