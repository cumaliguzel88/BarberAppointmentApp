package com.cumaliguzel.barberappointment.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cumaliguzel.barberappointment.R
import java.time.LocalDate

@Composable
fun EmptyAppointmentsView(selectedDate: LocalDate) {
    val isToday = selectedDate.isEqual(LocalDate.now())
    val isFutureDate = selectedDate.isAfter(LocalDate.now())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(R.drawable.baerber_appoinment_no_yet),
            contentDescription = null,
            modifier = Modifier.size(220.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = when {
                isToday -> "Bugün için randevu yok"
                isFutureDate -> "${selectedDate.dayOfMonth} ${selectedDate.month.toString().lowercase().replaceFirstChar { it.uppercase() }} için randevu yok"
                else -> "Geçmiş tarih için randevu yok"
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = when {
                isToday || isFutureDate -> "Yeni bir randevu eklemek için + butonuna basabilirsiniz"
                else -> "Geçmiş tarihler için yeni randevu eklenemez"
            },
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
    }
}
