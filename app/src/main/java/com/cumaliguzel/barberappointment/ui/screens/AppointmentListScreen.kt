package com.cumaliguzel.barberappointment.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cumaliguzel.barberappointment.R
import com.cumaliguzel.barberappointment.data.Appointment
import com.cumaliguzel.barberappointment.viewmodel.AppointmentViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentListScreen(
    onEditAppointment: (Int) -> Unit,
    onAddAppointment: () -> Unit,
    viewModel: AppointmentViewModel = viewModel()
) {
    val appointments by viewModel.appointments.collectAsState(initial = emptyList())
    var selectedDate by rememberSaveable { mutableStateOf(LocalDate.now()) }

    Scaffold(
        topBar = {
            TopAppBar(

                title = { Text(stringResource(R.string.appointments_title), color = MaterialTheme.colorScheme.tertiary) },
                colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.primary),
                actions = {
                    IconButton(onClick = onAddAppointment) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(R.string.add_appointment),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                val dates = generateSequence(LocalDate.now()) { it.plusDays(1) }
                    .take(30)
                    .toList()

                items(dates) { date ->
                    DateCard(
                        date = date,
                        isSelected = date == selectedDate,
                        onClick = { selectedDate = date }
                    )
                }
            }

            val filteredAppointments = appointments.filter { appointment ->
                appointment.date == selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
            }.sortedBy { it.time }

            if (filteredAppointments.isEmpty()) {
                Column(Modifier.fillMaxSize().padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Image(painterResource(R.drawable.baerber_appoinment_no_yet), contentDescription = "", modifier = Modifier.size(300.dp))
                    Spacer(Modifier.padding(vertical = 10.dp))
                   Text(
                       text = stringResource(R.string.no_appointments),
                       color = MaterialTheme.colorScheme.tertiary,
                       textAlign = TextAlign.Center,
                       style = MaterialTheme.typography.bodyLarge,
                       fontWeight = FontWeight.Bold
                       )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredAppointments) { appointment ->
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
private fun DateCard(
    date: LocalDate,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
               MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.secondary
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) 
                    MaterialTheme.colorScheme.tertiary
                else MaterialTheme.colorScheme.tertiary
            )
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.titleLarge,
                color = if (isSelected) 
                    MaterialTheme.colorScheme.tertiary
                else MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
private fun AppointmentCard(
    appointment: Appointment,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    viewModel: AppointmentViewModel = viewModel()
) {
    var isCompleted by remember { mutableStateOf(appointment.status == "Completed") }
    var isCheckboxEnabled by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val currentDate = LocalDate.now()
    val appointmentDate = LocalDate.parse(appointment.date)

    // Delete Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(text = stringResource(R.string.delete_appointment)) },
            text = { Text(text = stringResource(R.string.delete_confirmation)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text(text = stringResource(R.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(text = stringResource(R.string.no))
                }
            }
        )
    }

    // Checkbox'ın durumunu kontrol et
    LaunchedEffect(appointment.status, appointmentDate, currentDate) {
        isCompleted = appointment.status == "Completed"
        isCheckboxEnabled = !isCompleted && appointmentDate.isEqual(currentDate)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted)
                MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.secondary
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "\uD83D\uDC88 ${appointment.name}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.padding(vertical = 5.dp))
                    Text(
                        text = "➡\uFE0F ${appointment.operation}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                   Spacer(modifier = Modifier.padding(vertical = 5.dp))
                    Text(
                        text = "\uD83D\uDD51  ${LocalTime.parse(appointment.time).format(DateTimeFormatter.ofPattern("HH:mm"))} - ${
                            LocalTime.parse(appointment.time).plusMinutes(30).format(DateTimeFormatter.ofPattern("HH:mm"))
                        }",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Düzenle",
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Sil",
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    Checkbox(
                        checked = isCompleted,
                        onCheckedChange = { checked ->
                            if (isCheckboxEnabled && checked) {
                                isCompleted = true
                                isCheckboxEnabled = false
                                viewModel.updateAppointmentStatus(
                                    appointment = appointment,
                                    newStatus = "Completed"
                                )
                            }
                        },
                        enabled = isCheckboxEnabled,
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary,
                            uncheckedColor = MaterialTheme.colorScheme.tertiary,
                            disabledCheckedColor = MaterialTheme.colorScheme.primary,
                            disabledUncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    }
} 