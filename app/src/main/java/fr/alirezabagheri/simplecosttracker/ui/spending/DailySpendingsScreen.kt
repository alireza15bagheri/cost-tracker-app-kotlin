package fr.alirezabagheri.simplecosttracker.ui.spending

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import fr.alirezabagheri.simplecosttracker.R
import fr.alirezabagheri.simplecosttracker.util.NumberFormatter
import fr.alirezabagheri.simplecosttracker.util.NumberVisualTransformation
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailySpendingsScreen(
    navController: NavController,
    periodId: String
) {
    val viewModel: DailySpendingsViewModel = viewModel(factory = DailySpendingsViewModelFactory(periodId))
    val period by viewModel.period.collectAsState()
    val spendingItems by viewModel.calculatedSpendingItems.collectAsState()

    var limitInput by remember(period) { mutableStateOf(NumberFormatter.format(period?.dailySpendingLimit ?: 0.0)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.daily_spending)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.back))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = limitInput,
                    onValueChange = { limitInput = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text(stringResource(id = R.string.daily_spending_limit)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = NumberVisualTransformation()
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { viewModel.updateSpendingLimit(limitInput) }) {
                    Text(stringResource(id = R.string.set))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn {
                items(spendingItems) { item ->
                    SpendingRow(item = item, onSpentChange = { newAmount ->
                        viewModel.addOrUpdateSpending(item, newAmount)
                    })
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun SpendingRow(item: DailySpendingItem, onSpentChange: (String) -> Unit) {
    var spentInput by remember(item.spent) { mutableStateOf(if (item.spent == 0.0) "" else NumberFormatter.format(item.spent)) }
    val dateFormatter = SimpleDateFormat("EEE, dd MMM", Locale.getDefault())

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1.5f)) {
            Text(dateFormatter.format(item.date), fontWeight = FontWeight.Bold)
            Text(stringResource(id = R.string.limit, NumberFormatter.format(item.limit)), fontSize = 12.sp)
            Text(stringResource(id = R.string.carryover, NumberFormatter.format(item.carryover)), fontSize = 12.sp)
            Text(stringResource(id = R.string.remaining, NumberFormatter.format(item.remaining)), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        OutlinedTextField(
            value = spentInput,
            onValueChange = {
                spentInput = it.filter { char -> char.isDigit() || char == '.' }
                onSpentChange(spentInput)
            },
            label = { Text(stringResource(id = R.string.spent)) },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = NumberVisualTransformation()
        )
    }
}