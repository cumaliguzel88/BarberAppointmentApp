package com.cumaliguzel.barberappointment.ui.components


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                Text(text = "OK", style = MaterialTheme.typography.titleMedium)
            }
        },
        text = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center,
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
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 32.sp),
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

