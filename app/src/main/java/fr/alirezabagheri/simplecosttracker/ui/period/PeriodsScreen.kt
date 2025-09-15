package fr.alirezabagheri.simplecosttracker.ui.period

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodsScreen(
    navController: NavController,
    viewModel: PeriodsViewModel = viewModel()
) {
    val periods by viewModel.periods.collectAsState()
    val periodName by viewModel.periodName.collectAsState()

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Periods") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                    val date = viewModel.startDatePickerState.selectedDateMillis?.let { Date(it) }
                    Text(text = date?.let { SimpleDateFormat("dd MMM yyyy", Locale.US).format(it) } ?: "Start Date")
                }
                Button(onClick = { showEndDatePicker = true }) {
                    val date = viewModel.endDatePickerState.selectedDateMillis?.let { Date(it) }
                    Text(text = date?.let { SimpleDateFormat("dd MMM yyyy", Locale.US).format(it) } ?: "End Date")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { viewModel.addPeriod() }, modifier = Modifier.fillMaxWidth()) {
                Text("Create Period")
            }
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(periods) { period ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = period.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            val startDateText = period.startDate?.let { SimpleDateFormat("dd MMM", Locale.US).format(it) } ?: ""
                            val endDateText = period.endDate?.let { SimpleDateFormat("dd MMM", Locale.US).format(it) } ?: ""
                            Text(
                                text = "$startDateText - $endDateText",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
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
            DatePicker(state = viewModel.startDatePickerState)
        }
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                Button(onClick = { showEndDatePicker = false }) { Text("OK") }
            }) {
            DatePicker(state = viewModel.endDatePickerState)
        }
    }
}