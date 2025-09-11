package fr.alirezabagheri.simplecosttracker.ui.period

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodsScreen(
    navController: NavController,
    viewModel: PeriodsViewModel = viewModel()
) {
    val periods by viewModel.periods.collectAsState()
    val periodName by viewModel.periodName.collectAsState()
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Periods") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = periodName,
                onValueChange = { viewModel.periodName.value = it },
                label = { Text("Period Name (e.g., September)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                Button(onClick = { showStartDatePicker = true }) {
                    Text(text = startDate?.let { SimpleDateFormat("dd MMM yyyy", Locale.US).format(it) } ?: "Start Date")
                }
                Button(onClick = { showEndDatePicker = true }) {
                    Text(text = endDate?.let { SimpleDateFormat("dd MMM yyyy", Locale.US).format(it) } ?: "End Date")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { viewModel.addPeriod() }, modifier = Modifier.fillMaxWidth()) {
                Text("Create Period")
            }
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            LazyColumn {
                items(periods) { period ->
                    Text("${period.name} (${SimpleDateFormat("dd MMM", Locale.US).format(period.startDate!!)} - ${SimpleDateFormat("dd MMM", Locale.US).format(period.endDate!!)})")
                }
            }
        }
    }

    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                Button(onClick = { showStartDatePicker = false }) { Text("OK") }
            }) {
            DatePicker(onDateSelected = { viewModel.startDate.value = it })
        }
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                Button(onClick = { showEndDatePicker = false }) { Text("OK") }
            }) {
            DatePicker(onDateSelected = { viewModel.endDate.value = it })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePicker(onDateSelected: (Date) -> Unit) {
    val calendar = Calendar.getInstance()
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = calendar.timeInMillis
    )
    LaunchedEffect(datePickerState.selectedDateMillis) {
        datePickerState.selectedDateMillis?.let {
            calendar.timeInMillis = it
            onDateSelected(calendar.time)
        }
    }
    DatePicker(state = datePickerState)
}