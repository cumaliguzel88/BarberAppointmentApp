import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cumaliguzel.barberappointment.viewmodel.AppointmentViewModel
import androidx.compose.ui.Alignment
import com.cumaliguzel.barberappointment.ui.components.CustomSnackbar
import kotlinx.coroutines.delay
import com.cumaliguzel.barberappointment.util.CurrencyFormatter
import androidx.compose.ui.res.stringResource
import com.cumaliguzel.barberappointment.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperationPricesScreen(
    onNavigateBack: () -> Unit,
    viewModel: AppointmentViewModel = viewModel()
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var operationToDelete by remember { mutableStateOf("") }
    var selectedOperations by remember { mutableStateOf(setOf<String>()) }
    var newOperationName by remember { mutableStateOf("") }
    var newOperationPrice by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showSuccessMessage by remember { mutableStateOf(false) }
    
    val existingPrices by viewModel.operationPrices.collectAsState()
    val operations by remember(existingPrices) { 
        mutableStateOf(existingPrices.keys.toList().sortedWith(compareBy { 
            when(it) {
                "\uD83D\uDC87\uD83C\uDFFB\u200D♂\uFE0F Saç Traşı" -> 0
                "\uD83E\uDDD4\uD83C\uDFFB\u200D♂\uFE0F Sakal Traşı" -> 1
                "\uD83D\uDC88 Saç & Sakal Traş" -> 2
                "\uD83E\uDDD2 Çocuk Traş" -> 3
                "\uD83D\uDC86\uD83C\uDFFB\u200D♂\uFE0F Cilt Bakımı" -> 4
                else -> 5

            }
        }))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.primary),
                title = { Text(text = stringResource(R.string.pricing_title), color = MaterialTheme.colorScheme.tertiary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = MaterialTheme.colorScheme.tertiary)
                    }
                },
                actions = {
                    if (selectedOperations.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                if (selectedOperations.size == 1) {
                                    operationToDelete = selectedOperations.first()
                                    showDeleteDialog = true
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Operation",
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            items(operations) { operation ->
                var showEditDialog by remember { mutableStateOf(false) }
                var editedPrice by remember { mutableStateOf("") }
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "\uD83D\uDC88 $operation",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "\uD83D\uDCB0 ${CurrencyFormatter.formatPriceWithSpace(existingPrices[operation] ?: 0.0)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { showEditDialog = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = stringResource(R.string.edit_prices),
                                        tint = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                                
                                Checkbox(
                                    checked = selectedOperations.contains(operation),
                                    onCheckedChange = { isChecked ->
                                        selectedOperations = if (isChecked) {
                                            setOf(operation)
                                        } else {
                                            emptySet()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                if (showEditDialog) {
                    AlertDialog(
                        onDismissRequest = { 
                            showEditDialog = false
                            editedPrice = ""
                        },
                        title = { Text(stringResource(R.string.edit_prices)) },
                        text = {
                            Column {
                                Text(operation)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = editedPrice,
                                    onValueChange = { editedPrice = it },
                                    label = { Text(stringResource(R.string.price)) },
                                    suffix = { Text("TL") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    editedPrice.toDoubleOrNull()?.let { newPrice ->
                                        viewModel.updateOperationPrice(operation, newPrice)
                                        showSuccessMessage = true
                                    }
                                    showEditDialog = false
                                    editedPrice = ""
                                }
                            ) {
                                Text(stringResource(R.string.save))
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { 
                                    showEditDialog = false
                                    editedPrice = ""
                                }
                            ) {
                                Text(stringResource(R.string.cancel))
                            }
                        }
                    )
                }
            }
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = {
                    showAddDialog = false
                    showError = false
                    newOperationName = ""
                    newOperationPrice = ""
                },
                title = { Text(text = stringResource(R.string.add_new_opt)) },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = newOperationName,
                            onValueChange = { newOperationName = it },
                            label = { Text(text = stringResource(R.string.service_name)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = newOperationPrice,
                            onValueChange = { newOperationPrice = it },
                            label = { Text(stringResource(R.string.price)) },
                            suffix = { Text("TL") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                            singleLine = true
                        )
                        if (showError) {
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            when {
                                newOperationName.isBlank() -> {
                                    showError = true
                                    errorMessage = "Please enter an operation name"
                                }
                                newOperationPrice.isBlank() || newOperationPrice.toDoubleOrNull() == null -> {
                                    showError = true
                                    errorMessage = "Please enter a valid price"
                                }
                                newOperationPrice.toDouble() <= 0 -> {
                                    showError = true
                                    errorMessage = "Price must be greater than 0 TL"
                                }
                                else -> {
                                    viewModel.saveOperationPrices(mapOf(newOperationName to newOperationPrice.toDouble()))
                                    showAddDialog = false
                                    newOperationName = ""
                                    newOperationPrice = ""
                                    showError = false
                                    showSuccessMessage = true
                                }
                            }
                        }
                    ) {
                        Text(text = stringResource(R.string.add_new_service))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showAddDialog = false
                            showError = false
                            newOperationName = ""
                            newOperationPrice = ""
                        }
                    ) {
                        Text(text = stringResource(R.string.cancel))
                    }
                }
            )
        }

        if (showSuccessMessage) {
            LaunchedEffect(Unit) {
                delay(2000)
                showSuccessMessage = false
            }
            CustomSnackbar(
                message = "Price updated successfully",
                onDismiss = { showSuccessMessage = false }
            )
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showDeleteDialog = false 
                    operationToDelete = ""
                },
                title = { Text("Operasyon Silme") },
                text = { Text("Bu operasyonu silmek istediğinizden emin misiniz?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteOperation(operationToDelete)
                            selectedOperations = emptySet()
                            showDeleteDialog = false
                            operationToDelete = ""
                        }
                    ) {
                        Text("Evet")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { 
                            showDeleteDialog = false
                            operationToDelete = ""
                        }
                    ) {
                        Text("Hayır")
                    }
                }
            )
        }
    }
} 