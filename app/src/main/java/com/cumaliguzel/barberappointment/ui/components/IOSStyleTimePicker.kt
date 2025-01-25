package com.cumaliguzel.barberappointment.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import java.time.LocalTime

@Composable
fun IOSStyleTimePicker(
    initialTime: LocalTime = LocalTime.now(),
    onTimeSelected: (LocalTime) -> Unit
) {
    var selectedHour by remember { mutableStateOf(initialTime.hour) }
    var selectedMinute by remember { mutableStateOf(initialTime.minute) }

    AlertDialog(
        onDismissRequest = { },
        confirmButton = {
            TextButton(onClick = { onTimeSelected(LocalTime.of(selectedHour, selectedMinute)) }) {
                Text("Tamam", style = MaterialTheme.typography.titleMedium)
            }
        },
        text = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {

                WheelPicker(
                    range = 0..23,
                    selectedValue = selectedHour,
                    onValueChange = { selectedHour = it },
                    label = "Saat"
                )

                Text(
                    text = ":",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                WheelPicker(
                    range = 0..59,
                    selectedValue = selectedMinute,
                    onValueChange = { selectedMinute = it },
                    label = "Dakika"
                )
            }
        }
    )
}

@Composable
fun WheelPicker(
    range: IntRange,
    selectedValue: Int,
    onValueChange: (Int) -> Unit,
    label: String
) {
    LazyColumn(
        modifier = Modifier
            .height(150.dp)
            .width(80.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(range.toList()) { value ->
            Text(
                text = value.toString().padStart(2, '0'),
                style = if (value == selectedValue) {
                    MaterialTheme.typography.headlineSmall.copy(color = MaterialTheme.colorScheme.primary)
                } else {
                    MaterialTheme.typography.bodyLarge
                },
                modifier = Modifier
                    .clickable { onValueChange(value) }
                    .padding(8.dp)
            )
        }
    }
}

