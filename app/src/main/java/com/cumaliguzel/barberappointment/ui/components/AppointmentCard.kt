package com.cumaliguzel.barberappointment.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cumaliguzel.barberappointment.R
import com.cumaliguzel.barberappointment.data.Appointment
import com.cumaliguzel.barberappointment.util.CurrencyFormatter
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun AppointmentCards(
    appointment: Appointment,
    onStatusChange: (Boolean) -> Unit
) {
    var isCheckboxEnabled by remember {
        mutableStateOf(appointment.status != "Completed")
    }

    LaunchedEffect(appointment.status) {
        isCheckboxEnabled = appointment.status != "Completed"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (appointment.status == "Completed") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = appointment.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = "✅ ${appointment.operation}  ➡\uFE0F  ${CurrencyFormatter.formatPriceWithSpace(appointment.price)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                Checkbox(
                    checked = appointment.status == "Completed",
                    onCheckedChange = { isChecked ->
                        if (isCheckboxEnabled) {
                            onStatusChange(isChecked)
                        }
                    },
                    enabled = isCheckboxEnabled
                )
            }
            Text(
                text = " ${stringResource(R.string.time)} ${LocalTime.parse(appointment.time).format(DateTimeFormatter.ofPattern("HH:mm"))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}