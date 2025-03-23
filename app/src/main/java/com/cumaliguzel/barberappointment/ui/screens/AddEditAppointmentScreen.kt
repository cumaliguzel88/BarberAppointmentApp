package com.cumaliguzel.barberappointment.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cumaliguzel.barberappointment.data.Appointment
import com.cumaliguzel.barberappointment.util.CurrencyFormatter
import com.cumaliguzel.barberappointment.viewmodel.AppointmentViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.cumaliguzel.barberappointment.R
import com.cumaliguzel.barberappointment.ui.components.ExistingAppointmentCard
import com.cumaliguzel.barberappointment.ui.components.IOSStyleTimePicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAppointmentScreen(
    appointmentId: Int = -1,
    onNavigateBack: () -> Unit,
    viewModel: AppointmentViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var operation by remember { mutableStateOf("") }
    var showOperationMenu by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf(LocalTime.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showOperationError by remember { mutableStateOf(false) }
    var showTimeConflictDialog by remember { mutableStateOf(false) }
    var conflictingAppointment by remember { mutableStateOf<Appointment?>(null) }
    val operationPrices by viewModel.operationPrices.collectAsState()
    val operations = operationPrices.keys.toList()
    val appointments by viewModel.getAppointmentsByDate(selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
        .collectAsState(initial = emptyList())

    // Load existing appointment data if editing
    LaunchedEffect(appointmentId) {
        if (appointmentId != -1) {
            viewModel.getAppointmentById(appointmentId)?.let { appointment ->
                name = appointment.name
                operation = appointment.operation
                selectedDate = LocalDate.parse(appointment.date)
                selectedTime = LocalTime.parse(appointment.time)
            }
        }
    }

    // Collect appointments for selected date
    val dateAppointments by viewModel.getAppointmentsByDate(
        selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
    ).collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.primary),
                title = { Text(if (appointmentId == -1) stringResource(R.string.new_appointment) else stringResource(R.string.edit_appointment), color = MaterialTheme.colorScheme.tertiary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.tertiary)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(text = stringResource(R.string.customer_name)) },
                modifier = Modifier.fillMaxWidth()
            )

            Box {
                OutlinedTextField(
                    value = operation,
                    onValueChange = {},
                    label = { Text(text = stringResource(R.string.select_service)) },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = showOperationError,
                    supportingText = if (showOperationError) {
                        { Text("Please select an operation") }
                    } else null,
                    trailingIcon = {
                        IconButton(onClick = { 
                            if (operations.isNotEmpty()) {
                                showOperationMenu = true 
                            }
                        }) {
                            Icon(Icons.Default.ArrowDropDown, "Select operation")
                        }
                    }
                )

                if (operations.isEmpty()) {
                    Text(
                        text = "No operations found. Please add one in the Pricing screen.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                DropdownMenu(
                    expanded = showOperationMenu,
                    onDismissRequest = { showOperationMenu = false }
                ) {
                    operations.forEach { op ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(op)
                                    Text(
                                        text = CurrencyFormatter.formatPrice(operationPrices[op] ?: 0.0),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            onClick = {
                                operation = op
                                showOperationMenu = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                onValueChange = {},
                label = { Text(text = stringResource(R.string.select_date)) },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, "Select date")
                    }
                },
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = selectedTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                onValueChange = {},
                label = { Text(text = stringResource(R.string.select_time)) },
                trailingIcon = {
                    IconButton(onClick = { showTimePicker = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_access_time_24),
                            contentDescription = "Select time"
                        )
                    }
                },
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )


            Button(
                onClick = {
                    if (operation.isBlank()) {
                        showOperationError = true
                        return@Button
                    }

                    // Check for time conflicts
                    val newAppointmentStart = selectedTime
                    val newAppointmentEnd = selectedTime.plusMinutes(29) // Assuming 30-minute slots
                    
                    val conflict = appointments.find { existing ->
                        // Skip checking against the current appointment when editing
                        if (existing.id == appointmentId) return@find false
                        
                        val existingStart = LocalTime.parse(existing.time)
                        val existingEnd = existingStart.plusMinutes(29)
                        
                        // Check if times overlap
                        !(newAppointmentEnd.isBefore(existingStart) || newAppointmentStart.isAfter(existingEnd))
                    }

                    if (conflict != null) {
                        conflictingAppointment = conflict
                        showTimeConflictDialog = true
                        return@Button
                    }

                    val appointment = Appointment(
                        id = if (appointmentId == -1) 0 else appointmentId,
                        name = name,
                        operation = operation,
                        date = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                        time = selectedTime.format(DateTimeFormatter.ISO_LOCAL_TIME),
                        price = operationPrices[operation] ?: 0.0,
                        status = "Pending"
                    )
                    
                    if (appointmentId == -1) {
                        viewModel.addAppointment(appointment)
                    } else {
                        viewModel.updateAppointment(appointment)
                    }
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && operation.isNotBlank()
            ) {
                Text(if (appointmentId == -1) stringResource(R.string.save) else stringResource(R.string.cancel))
            }

            // Add a divider and title for existing appointments
            if (dateAppointments.isNotEmpty()) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = String.format(
                        stringResource(R.string.existing_appointments_for_date),
                        selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                // Existing appointments list
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(dateAppointments) { appointment ->
                        ExistingAppointmentCard(appointment = appointment)
                    }
                }
            }
        }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = selectedDate
                    .atStartOfDay()
                    .toInstant(java.time.ZoneOffset.UTC)
                    .toEpochMilli()
            )
            
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                selectedDate = java.time.Instant
                                    .ofEpochMilli(millis)
                                    .atZone(java.time.ZoneOffset.UTC)
                                    .toLocalDate()
                            }
                            showDatePicker = false
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        if (showTimePicker) {
            IOSStyleTimePicker(
                initialTime = selectedTime,
                onTimeSelected = { time ->
                    selectedTime = time
                    showTimePicker = false
                }
            )
        }


        if (showTimeConflictDialog) {
            AlertDialog(
                onDismissRequest = { showTimeConflictDialog = false },
                title = { 
                    Text(
                        text = stringResource(R.string.time_conflict),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = stringResource(R.string.time_coflinct_description))
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Show conflicting appointment details
                        conflictingAppointment?.let { conflict ->
                            Text(
                                text = "Existing Appointment Details:",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(" ${stringResource(R.string.clint)}  ${conflict.name}")
                            Text("Service: ${conflict.operation}")
                            Text(
                                "${stringResource(R.string.service)} ${LocalTime.parse(conflict.time).format(DateTimeFormatter.ofPattern("HH:mm"))} - " +
                                "${LocalTime.parse(conflict.time).plusMinutes(30).format(DateTimeFormatter.ofPattern("HH:mm"))}"
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { showTimeConflictDialog = false }
                    ) {
                        Text("OK")
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            )
        }
    }
}
