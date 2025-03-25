package com.cumaliguzel.barberappointment.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cumaliguzel.barberappointment.R
import com.cumaliguzel.barberappointment.viewmodel.AppointmentViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.cumaliguzel.barberappointment.ui.components.AppointmentCard
import com.cumaliguzel.barberappointment.ui.components.DateCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentListScreen(
    onEditAppointment: (Int) -> Unit,
    onAddAppointment: () -> Unit,
    viewModel: AppointmentViewModel = viewModel()
) {
    val appointments by viewModel.appointments.collectAsState(initial = emptyList())
    var selectedDate by rememberSaveable { mutableStateOf(LocalDate.now()) }

    // Filtrelenen öğeleri memo ile tut
    val filteredAppointments = remember(appointments, selectedDate) {
        appointments.filter { appointment ->
            val appointmentDate = LocalDate.parse(appointment.date, DateTimeFormatter.ISO_DATE)
            appointmentDate.isEqual(selectedDate)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.appointments_title), color = MaterialTheme.colorScheme.tertiary) },
                colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.primary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddAppointment,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.tertiary
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_appointment)
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
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

            if (filteredAppointments.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 10.dp), 
                    horizontalAlignment = Alignment.CenterHorizontally, 
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.baerber_appoinment_no_yet), 
                        contentDescription = "", 
                        modifier = Modifier.size(300.dp)
                    )
                    Spacer(Modifier.padding(vertical = 10.dp))
                    Text(
                        text = stringResource(R.string.no_appointments),
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    items(
                        items = filteredAppointments,
                        key = { it.id } // Key kullanımı performansı artırır
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
