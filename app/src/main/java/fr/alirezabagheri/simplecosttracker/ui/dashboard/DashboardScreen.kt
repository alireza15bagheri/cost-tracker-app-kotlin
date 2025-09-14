package fr.alirezabagheri.simplecosttracker.ui.dashboard

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import fr.alirezabagheri.simplecosttracker.Screen
import fr.alirezabagheri.simplecosttracker.data.*
import fr.alirezabagheri.simplecosttracker.util.NumberFormatter
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    auth: FirebaseAuth,
    viewModel: DashboardViewModel = viewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val periods by viewModel.periods.collectAsState()
    val activePeriod by viewModel.activePeriod.collectAsState()
    val incomes by viewModel.activePeriodIncomes.collectAsState()
    val totalIncomes by viewModel.totalIncomes.collectAsState()
    val budgets by viewModel.activePeriodBudgets.collectAsState()
    val totalBudgets by viewModel.totalBudgets.collectAsState()
    val spendings by viewModel.activePeriodSpendings.collectAsState()
    val totalPeriodSpending by viewModel.totalPeriodSpending.collectAsState()
    val miscCosts by viewModel.activePeriodMiscCosts.collectAsState()
    val totalMiscCosts by viewModel.totalMiscCosts.collectAsState()
    val totalRemaining by viewModel.totalRemaining.collectAsState()
    val notesInput by viewModel.notesInput.collectAsState()

    var isDropdownExpanded by remember { mutableStateOf(false) }
    val username = auth.currentUser?.email?.split("@")?.get(0) ?: "User"
    var showDeleteDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<Any?>(null) }

    LaunchedEffect(activePeriod) {
        viewModel.setNotesInput(activePeriod?.notes ?: "")
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Spacer(Modifier.height(12.dp))
                        NavigationDrawerItem(icon = { Icon(Icons.Default.DateRange, contentDescription = "Manage Periods") }, label = { Text("Manage Periods") }, selected = false, onClick = { scope.launch { drawerState.close() }; navController.navigate(Screen.PeriodsScreen.route) })
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        val isDataManagementEnabled = activePeriod != null
                        NavigationDrawerItem(icon = { Icon(Icons.Filled.TrendingUp, contentDescription = "Manage Incomes") }, label = { Text("Manage Incomes") }, selected = false, onClick = { if (isDataManagementEnabled) { activePeriod?.let { scope.launch { drawerState.close() }; navController.navigate(Screen.IncomesScreen.createRoute(it.id)) } } }, colors = if (isDataManagementEnabled) NavigationDrawerItemDefaults.colors() else NavigationDrawerItemDefaults.colors(unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f), unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)))
                        NavigationDrawerItem(icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Manage Budgets") }, label = { Text("Manage Budgets") }, selected = false, onClick = { if (isDataManagementEnabled) { activePeriod?.let { scope.launch { drawerState.close() }; navController.navigate(Screen.BudgetsScreen.createRoute(it.id)) } } }, colors = if (isDataManagementEnabled) NavigationDrawerItemDefaults.colors() else NavigationDrawerItemDefaults.colors(unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f), unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)))
                        NavigationDrawerItem(icon = { Icon(Icons.Default.Home, contentDescription = "Daily Spendings") }, label = { Text("Daily Spendings") }, selected = false, onClick = { if (isDataManagementEnabled) { activePeriod?.let { scope.launch { drawerState.close() }; navController.navigate(Screen.DailySpendingsScreen.createRoute(it.id)) } } }, colors = if (isDataManagementEnabled) NavigationDrawerItemDefaults.colors() else NavigationDrawerItemDefaults.colors(unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f), unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)))
                        NavigationDrawerItem(icon = { Icon(Icons.Default.List, contentDescription = "Misc. Costs") }, label = { Text("Misc. Costs") }, selected = false, onClick = { if (isDataManagementEnabled) { activePeriod?.let { scope.launch { drawerState.close() }; navController.navigate(Screen.MiscCostsScreen.createRoute(it.id)) } } }, colors = if (isDataManagementEnabled) NavigationDrawerItemDefaults.colors() else NavigationDrawerItemDefaults.colors(unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f), unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)))
                    }
                    Column {
                        NavigationDrawerItem(icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Sign Out") }, label = { Text("Sign Out") }, selected = false, onClick = { auth.signOut(); navController.navigate(Screen.LoginScreen.route) { popUpTo(navController.graph.startDestinationId) { inclusive = true } } })
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = { TopAppBar(title = { Text("Dashboard") }, navigationIcon = { IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(imageVector = Icons.Filled.Menu, contentDescription = "Menu") } }) }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top section
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Welcome", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        Text(text = username, style = MaterialTheme.typography.headlineSmall)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    ExposedDropdownMenuBox(expanded = isDropdownExpanded, onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }) {
                        OutlinedTextField(value = activePeriod?.name ?: "No Period Selected", onValueChange = {}, readOnly = true, label = { Text("Active Period") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) }, modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth())
                        ExposedDropdownMenu(expanded = isDropdownExpanded, onDismissRequest = { isDropdownExpanded = false }) {
                            periods.forEach { period ->
                                DropdownMenuItem(text = { Text(period.name) }, onClick = { viewModel.setActivePeriod(period); isDropdownExpanded = false })
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    activePeriod?.let { period ->
                        val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Start: ${period.startDate?.let { dateFormatter.format(it) } ?: "N/A"}", style = MaterialTheme.typography.bodyMedium)
                            Text(text = "End: ${period.endDate?.let { dateFormatter.format(it) } ?: "N/A"}", style = MaterialTheme.typography.bodyMedium)
                            IconButton(onClick = { itemToDelete = period; showDeleteDialog = true }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Period", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                }

                // Incomes Section
                item { Text(text = "Incomes", style = MaterialTheme.typography.titleLarge, modifier = Modifier.fillMaxWidth()) }
                items(incomes) { income ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(income.description, style = MaterialTheme.typography.bodyLarge)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(NumberFormatter.format(income.amount), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                IconButton(onClick = { itemToDelete = income; showDeleteDialog = true }) { Icon(Icons.Default.Delete, contentDescription = "Delete Income") }
                            }
                        }
                    }
                }
                item {
                    if (incomes.isNotEmpty()) {
                        Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Total Income", style = MaterialTheme.typography.titleMedium); Text(NumberFormatter.format(totalIncomes), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Budgets Section
                item { Text(text = "Budgets", style = MaterialTheme.typography.titleLarge, modifier = Modifier.fillMaxWidth()) }
                items(budgets) { budget ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(budget.category, style = MaterialTheme.typography.bodyLarge)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(NumberFormatter.format(budget.allocatedAmount), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                IconButton(onClick = { itemToDelete = budget; showDeleteDialog = true }) { Icon(Icons.Default.Delete, contentDescription = "Delete Budget") }
                            }
                        }
                    }
                }
                item {
                    if (budgets.isNotEmpty()) {
                        Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Total Budget", style = MaterialTheme.typography.titleMedium); Text(NumberFormatter.format(totalBudgets), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Daily Spendings Section
                item {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Daily House Spendings", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.weight(1f))
                        Text(text = "(${NumberFormatter.format(totalPeriodSpending)})", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(spendings) { spending ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(SimpleDateFormat("EEE, dd MMM", Locale.getDefault()).format(spending.date), style = MaterialTheme.typography.bodyLarge)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(NumberFormatter.format(spending.spent), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                IconButton(onClick = { itemToDelete = spending; showDeleteDialog = true }) { Icon(Icons.Default.Delete, contentDescription = "Delete Spending") }
                            }
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Miscellaneous Costs Section
                item {
                    Text(text = "Miscellaneous Costs", style = MaterialTheme.typography.titleLarge, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(miscCosts) { cost ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(cost.description, style = MaterialTheme.typography.bodyLarge)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(NumberFormatter.format(cost.amount), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                IconButton(onClick = { itemToDelete = cost; showDeleteDialog = true }) { Icon(Icons.Default.Delete, contentDescription = "Delete Misc Cost") }
                            }
                        }
                    }
                }
                item {
                    if (miscCosts.isNotEmpty()) {
                        Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Total Misc. Costs", style = MaterialTheme.typography.titleMedium)
                                Text(NumberFormatter.format(totalMiscCosts), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Notes Section
                item {
                    Text(text = "Notes", style = MaterialTheme.typography.titleLarge, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = { viewModel.setNotesInput(it) },
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        label = { Text("Write any notes for this period...") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            viewModel.saveNotes()
                            Toast.makeText(context, "Notes saved!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Notes")
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Final Summary Card
                item {
                    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Remaining", style = MaterialTheme.typography.titleMedium)
                            Text(NumberFormatter.format(totalRemaining), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false; itemToDelete = null },
            title = { Text("Confirm Deletion") },
            text = {
                when (itemToDelete) {
                    is Income -> Text("Are you sure you want to delete this income?")
                    is Budget -> Text("Are you sure you want to delete this budget?")
                    is DailySpending -> Text("Are you sure you want to delete this spending entry?")
                    is MiscCost -> Text("Are you sure you want to delete this cost?")
                    is Period -> Text("Are you sure you want to delete this period and all its associated data?")
                    else -> Text("Are you sure you want to delete this item?")
                }
            },
            confirmButton = { Button(onClick = {
                when (val item = itemToDelete) {
                    is Income -> viewModel.deleteIncome(item.id)
                    is Budget -> viewModel.deleteBudget(item.id)
                    is DailySpending -> viewModel.deleteDailySpending(item.id)
                    is MiscCost -> viewModel.deleteMiscCost(item.id)
                    is Period -> viewModel.deleteActivePeriod()
                }
                showDeleteDialog = false; itemToDelete = null
            }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Delete") } },
            dismissButton = { Button(onClick = { showDeleteDialog = false; itemToDelete = null }) { Text("Cancel") } }
        )
    }
}