package com.cumaliguzel.barberappointment.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
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

@Composable
fun WheelPicker(
    range: IntRange,
    selectedValue: Int,
    onValueChange: (Int) -> Unit,
    label: String
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val itemHeight = with(LocalDensity.current) { 50.dp.toPx() }

    LaunchedEffect(selectedValue) {
        coroutineScope.launch {
            listState.animateScrollToItem(selectedValue)
        }
    }

    Box(modifier = Modifier.height(180.dp)) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .width(90.dp)
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            itemsIndexed(range.toList()) { index, value ->
                val isSelected = value == selectedValue
                Text(
                    text = value.toString().padStart(2, '0'),
                    fontSize = if (isSelected) 32.sp else 24.sp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(if (isSelected) Color.LightGray.copy(alpha = 0.3f) else Color.Transparent)
                        .clickable {
                            onValueChange(value)
                            coroutineScope.launch {
                                listState.animateScrollToItem(index)
                            }
                        }
                )
            }
        }
    }
}
