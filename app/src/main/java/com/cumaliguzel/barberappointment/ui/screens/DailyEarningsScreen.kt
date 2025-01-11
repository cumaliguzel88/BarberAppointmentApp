import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cumaliguzel.barberappointment.data.Appointment
import com.cumaliguzel.barberappointment.data.CompletedAppointment
import java.time.*
import java.time.format.DateTimeFormatter
import com.cumaliguzel.barberappointment.viewmodel.AppointmentViewModel
import com.cumaliguzel.barberappointment.util.CurrencyFormatter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.cumaliguzel.barberappointment.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyEarningsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AppointmentViewModel = viewModel()
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    )

    // Collect appointments and completed appointments
    val activeAppointments by viewModel.getAppointmentsByDate(selectedDate.toString()).collectAsState(initial = emptyList())
    val completedAppointments by viewModel.getCompletedAppointmentsByDate(selectedDate.toString()).collectAsState(initial = emptyList())

    // Calculate unique appointments and earnings using Set
    val completedAppointmentIds = completedAppointments.map { it.originalAppointmentId }.toSet()
    val uniqueActiveAppointments = activeAppointments.filter { appointment ->
        !completedAppointmentIds.contains(appointment.id) && appointment.status != "Completed"
    }
    
    val dailyEarnings = completedAppointments.sumOf { it.price }
    val totalAppointments = completedAppointments.size
    val pendingAppointments = uniqueActiveAppointments.filter { it.status == "Pending" }
    val pendingEarnings = pendingAppointments.sumOf { it.price }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.primary),
                title = { Text(text = stringResource(R.string.daily_earnings), color = MaterialTheme.colorScheme.tertiary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = MaterialTheme.colorScheme.tertiary)
                    }
                },
                actions = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, "Select Date", tint = MaterialTheme.colorScheme.tertiary)
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
            // Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.total_earnings),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.completed_appointments),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            Text(
                                text = "✂\uFE0F ${stringResource(R.string.appointment_earnings)} $totalAppointments",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = stringResource(R.string.daily_earnings),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            Text(
                                text = CurrencyFormatter.formatPriceWithSpace(dailyEarnings),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (pendingAppointments.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(
                                R.string.pending_appointments
                            ) + ": ${pendingAppointments.size} (${CurrencyFormatter.formatPriceWithSpace(pendingEarnings)}) \uD83D\uDCB0",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }

            // Date Picker Dialog
            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                selectedDate = Instant.ofEpochMilli(millis)
                                    .atZone(ZoneOffset.UTC)
                                    .toLocalDate()
                            }
                            showDatePicker = false
                        }) {
                            Text(text = stringResource(R.string.ok))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text(text = stringResource(R.string.cancel_alert))
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            // Appointments List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Show completed appointments first (guaranteed unique by DB constraints)
                items(completedAppointments) { appointment ->
                    CompletedAppointmentCard(appointment = appointment)
                }
                
                // Show remaining active appointments (filtered to prevent duplicates)
                items(uniqueActiveAppointments) { appointment ->
                    AppointmentCard(
                        appointment = appointment,
                        onStatusChange = { isCompleted ->
                            if (isCompleted && !completedAppointmentIds.contains(appointment.id)) {
                                viewModel.updateAppointmentStatus(
                                    appointment,
                                    "Completed"
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CompletedAppointmentCard(
    appointment: CompletedAppointment
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
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
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Completed",
                    tint = MaterialTheme.colorScheme.onSecondary
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = " ${stringResource(R.string.time)} ${LocalTime.parse(appointment.time).format(DateTimeFormatter.ofPattern("HH:mm"))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    text = stringResource(R.string.completed),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }
        }
    }
}


@Composable
private fun AppointmentCard(
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
            containerColor = if (appointment.status == "Completed") Color(0xFF90EE90) else MaterialTheme.colorScheme.surface
        )
    ) {
    }
}
