package fr.alirezabagheri.simplecosttracker.ui.dashboard

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
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
    val uiState by viewModel.uiState.collectAsState()
    val username = auth.currentUser?.email?.split("@")?.get(0) ?: "User"
    var showDeleteDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<Any?>(null) }

    val onDeleteClick = { item: Any ->
        itemToDelete = item
        showDeleteDialog = true
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DashboardDrawerContent(
                navController = navController,
                auth = auth,
                activePeriod = uiState.activePeriod,
                onCloseDrawer = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Dashboard") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(imageVector = Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    TopSection(
                        username = username,
                        activePeriod = uiState.activePeriod,
                        periods = uiState.periods,
                        isDropdownExpanded = uiState.isDropdownExpanded,
                        onExpandedChange = viewModel::onDropdownExpandedChange,
                        onPeriodSelected = viewModel::setActivePeriod,
                        onDeleteClick = onDeleteClick
                    )
                }

                incomesSection(incomes = uiState.incomes, totalIncomes = uiState.totalIncomes, onDeleteClick = onDeleteClick)
                budgetsSection(budgets = uiState.budgets, totalBudgets = uiState.totalBudgets, onDeleteClick = onDeleteClick)
                dailySpendingsSection(spendings = uiState.spendings, totalSpending = uiState.totalPeriodSpending, onDeleteClick = onDeleteClick)
                miscCostsSection(miscCosts = uiState.miscCosts, totalMiscCosts = uiState.totalMiscCosts, onDeleteClick = onDeleteClick)
                item { NotesSection(notes = uiState.notesInput, onNotesChange = viewModel::onNotesChange, onSaveClick = viewModel::saveNotes) }
                item { FinalSummarySection(totalRemaining = uiState.totalRemaining) }
            }
        }
    }

    if (showDeleteDialog) {
        ConfirmationDialog(
            itemToDelete = itemToDelete,
            onDismiss = { showDeleteDialog = false; itemToDelete = null },
            onConfirm = {
                when (val item = itemToDelete) {
                    is Income -> viewModel.deleteIncome(item.id)
                    is Budget -> viewModel.deleteBudget(item.id)
                    is DailySpending -> viewModel.deleteDailySpending(item.id)
                    is MiscCost -> viewModel.deleteMiscCost(item.id)
                    is Period -> viewModel.deleteActivePeriod()
                }
                showDeleteDialog = false
                itemToDelete = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopSection(
    username: String,
    activePeriod: Period?,
    periods: List<Period>,
    isDropdownExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onPeriodSelected: (Period?) -> Unit,
    onDeleteClick: (Period) -> Unit
) {
    Spacer(modifier = Modifier.height(16.dp))
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Welcome", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Text(text = username, style = MaterialTheme.typography.headlineSmall)
    }
    Spacer(modifier = Modifier.height(24.dp))
    ExposedDropdownMenuBox(expanded = isDropdownExpanded, onExpandedChange = onExpandedChange) {
        OutlinedTextField(
            value = activePeriod?.name ?: "No Period Selected",
            onValueChange = {},
            readOnly = true,
            label = { Text("Active Period") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = isDropdownExpanded, onDismissRequest = { onExpandedChange(false) }) {
            periods.forEach { period ->
                DropdownMenuItem(text = { Text(period.name) }, onClick = { onPeriodSelected(period); onExpandedChange(false) })
            }
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
    activePeriod?.let { period ->
        val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Start: ${period.startDate?.let { dateFormatter.format(it) } ?: "N/A"}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "End: ${period.endDate?.let { dateFormatter.format(it) } ?: "N/A"}", style = MaterialTheme.typography.bodyMedium)
            IconButton(onClick = { onDeleteClick(period) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Period", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
}

private fun LazyListScope.incomesSection(incomes: List<Income>, totalIncomes: Double, onDeleteClick: (Income) -> Unit) {
    item { Text(text = "Incomes", style = MaterialTheme.typography.titleLarge, modifier = Modifier.fillMaxWidth()) }
    items(incomes) { income ->
        DeletableCard(
            description = income.description,
            amount = income.amount,
            onDeleteClick = { onDeleteClick(income) }
        )
    }
    item {
        if (incomes.isNotEmpty()) {
            TotalCard(label = "Total Income", total = totalIncomes, color = MaterialTheme.colorScheme.secondaryContainer)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

private fun LazyListScope.budgetsSection(budgets: List<Budget>, totalBudgets: Double, onDeleteClick: (Budget) -> Unit) {
    item { Text(text = "Budgets", style = MaterialTheme.typography.titleLarge, modifier = Modifier.fillMaxWidth()) }
    items(budgets) { budget ->
        DeletableCard(
            description = budget.category,
            amount = budget.allocatedAmount,
            onDeleteClick = { onDeleteClick(budget) }
        )
    }
    item {
        if (budgets.isNotEmpty()) {
            TotalCard(label = "Total Budget", total = totalBudgets, color = MaterialTheme.colorScheme.tertiaryContainer)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

private fun LazyListScope.dailySpendingsSection(spendings: List<DailySpending>, totalSpending: Double, onDeleteClick: (DailySpending) -> Unit) {
    item {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Daily House Spendings", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.weight(1f))
            Text(text = "(${NumberFormatter.format(totalSpending)})", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
    items(spendings) { spending ->
        DeletableCard(
            description = SimpleDateFormat("EEE, dd MMM", Locale.getDefault()).format(spending.date),
            amount = spending.spent,
            onDeleteClick = { onDeleteClick(spending) }
        )
    }
    item { Spacer(modifier = Modifier.height(24.dp)) }
}

private fun LazyListScope.miscCostsSection(miscCosts: List<MiscCost>, totalMiscCosts: Double, onDeleteClick: (MiscCost) -> Unit) {
    item { Text(text = "Miscellaneous Costs", style = MaterialTheme.typography.titleLarge, modifier = Modifier.fillMaxWidth()) }
    items(miscCosts) { cost ->
        DeletableCard(
            description = cost.description,
            amount = cost.amount,
            onDeleteClick = { onDeleteClick(cost) }
        )
    }
    item {
        if (miscCosts.isNotEmpty()) {
            TotalCard(label = "Total Misc. Costs", total = totalMiscCosts, color = MaterialTheme.colorScheme.secondaryContainer)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun NotesSection(notes: String, onNotesChange: (String) -> Unit, onSaveClick: () -> Unit) {
    val context = LocalContext.current
    Text(text = "Notes", style = MaterialTheme.typography.titleLarge, modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = notes,
        onValueChange = onNotesChange,
        modifier = Modifier.fillMaxWidth().height(150.dp),
        label = { Text("Write any notes for this period...") }
    )
    Spacer(modifier = Modifier.height(8.dp))
    Button(
        onClick = {
            onSaveClick()
            Toast.makeText(context, "Notes saved!", Toast.LENGTH_SHORT).show()
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Save Notes")
    }
    Spacer(modifier = Modifier.height(24.dp))
}

@Composable
private fun FinalSummarySection(totalRemaining: Double) {
    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total Remaining", style = MaterialTheme.typography.titleMedium)
            Text(NumberFormatter.format(totalRemaining), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DashboardDrawerContent(
    navController: NavController,
    auth: FirebaseAuth,
    activePeriod: Period?,
    onCloseDrawer: () -> Unit
) {
    ModalDrawerSheet {
        Column(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween) {
            Column {
                Spacer(Modifier.height(12.dp))
                NavigationDrawerItem(icon = { Icon(Icons.Default.DateRange, contentDescription = "Manage Periods") }, label = { Text("Manage Periods") }, selected = false, onClick = { onCloseDrawer(); navController.navigate(Screen.PeriodsScreen.route) })
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                val isDataManagementEnabled = activePeriod != null
                val colors = if (isDataManagementEnabled) NavigationDrawerItemDefaults.colors() else NavigationDrawerItemDefaults.colors(unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f), unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f))
                val onNavigate: (String) -> Unit = { route ->
                    if (isDataManagementEnabled) {
                        onCloseDrawer()
                        navController.navigate(route)
                    }
                }

                NavigationDrawerItem(icon = { Icon(Icons.Filled.TrendingUp, contentDescription = "Manage Incomes") }, label = { Text("Manage Incomes") }, selected = false, onClick = { activePeriod?.id?.let { onNavigate(Screen.IncomesScreen.createRoute(it)) } }, colors = colors)
                NavigationDrawerItem(icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Manage Budgets") }, label = { Text("Manage Budgets") }, selected = false, onClick = { activePeriod?.id?.let { onNavigate(Screen.BudgetsScreen.createRoute(it)) } }, colors = colors)
                NavigationDrawerItem(icon = { Icon(Icons.Default.Home, contentDescription = "Daily Spendings") }, label = { Text("Daily Spendings") }, selected = false, onClick = { activePeriod?.id?.let { onNavigate(Screen.DailySpendingsScreen.createRoute(it)) } }, colors = colors)
                NavigationDrawerItem(icon = { Icon(Icons.Default.List, contentDescription = "Misc. Costs") }, label = { Text("Misc. Costs") }, selected = false, onClick = { activePeriod?.id?.let { onNavigate(Screen.MiscCostsScreen.createRoute(it)) } }, colors = colors)
            }
            Column {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                NavigationDrawerItem(icon = { Icon(Icons.Default.Lock, contentDescription = "Change Password") }, label = { Text("Change Password") }, selected = false, onClick = { onCloseDrawer(); navController.navigate(Screen.ChangePasswordScreen.route) })
                NavigationDrawerItem(icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Sign Out") }, label = { Text("Sign Out") }, selected = false, onClick = { onCloseDrawer(); auth.signOut(); navController.navigate(Screen.LoginScreen.route) { popUpTo(navController.graph.startDestinationId) { inclusive = true } } })
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun ConfirmationDialog(itemToDelete: Any?, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Deletion") },
        text = {
            val text = when (itemToDelete) {
                is Income -> "Are you sure you want to delete this income?"
                is Budget -> "Are you sure you want to delete this budget?"
                is DailySpending -> "Are you sure you want to delete this spending entry?"
                is MiscCost -> "Are you sure you want to delete this cost?"
                is Period -> "Are you sure you want to delete this period and all its associated data?"
                else -> "Are you sure you want to delete this item?"
            } + "\nThis action cannot be undone."
            Text(text)
        },
        confirmButton = { Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Delete") } },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun DeletableCard(description: String, amount: Double, onDeleteClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(description, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(NumberFormatter.format(amount), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                IconButton(onClick = onDeleteClick) { Icon(Icons.Default.Delete, contentDescription = "Delete") }
            }
        }
    }
}

@Composable
private fun TotalCard(label: String, total: Double, color: androidx.compose.ui.graphics.Color) {
    Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), colors = CardDefaults.cardColors(containerColor = color)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.titleMedium)
            Text(NumberFormatter.format(total), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}