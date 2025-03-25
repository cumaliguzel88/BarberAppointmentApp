package com.cumaliguzel.barberappointment.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cumaliguzel.barberappointment.R
import com.cumaliguzel.barberappointment.ui.components.AppointmentCard
import com.cumaliguzel.barberappointment.ui.components.DateCard
import com.cumaliguzel.barberappointment.viewmodel.AppointmentViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentListScreen(
    onEditAppointment: (Int) -> Unit,
    onAddAppointment: () -> Unit,
    viewModel: AppointmentViewModel = viewModel()
) {
    val appointments by viewModel.appointments.collectAsState(initial = emptyList())
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    
    // Seçili tarihe göre randevuları filtrele
    val filteredAppointments = remember(appointments, selectedDate) {
        appointments.filter { appointment ->
            val appointmentDate = LocalDate.parse(appointment.date)
            appointmentDate.isEqual(selectedDate)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = stringResource(R.string.appointments_title),
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddAppointment,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.tertiary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_appointment)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tarih seçim kartları
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 5.dp, vertical = 5.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    // Tarih seçimi başlığı
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tarih Seçin",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Tarih kartları
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        val dates = generateSequence(LocalDate.now()) { it.plusDays(1) }
                            .take(14)
                            .toList()
                        
                        items(dates) { date ->
                            DateCard(
                                date = date,
                                isSelected = date == selectedDate,
                                onClick = { selectedDate = date }
                            )
                        }
                    }
                }
            }
            
            // Randevu listesi veya boş durum
            if (filteredAppointments.isEmpty()) {
                // Boş durum gösterimi
                EmptyAppointmentsView(selectedDate)
            } else {
                // Randevu listesi
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(
                        items = filteredAppointments,
                        key = { it.id }
                    ) { appointment ->
                        AppointmentCard(
                            appointment = appointment,
                            onEdit = { onEditAppointment(appointment.id) },
                            onDelete = { viewModel.deleteAppointment(appointment) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyAppointmentsView(selectedDate: LocalDate) {
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
