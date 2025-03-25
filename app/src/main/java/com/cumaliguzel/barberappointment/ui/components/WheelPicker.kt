package com.cumaliguzel.barberappointment.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

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
