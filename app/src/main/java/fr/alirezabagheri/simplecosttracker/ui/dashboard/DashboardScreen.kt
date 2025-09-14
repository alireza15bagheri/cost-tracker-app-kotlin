package fr.alirezabagheri.simplecosttracker.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import fr.alirezabagheri.simplecosttracker.Screen
import fr.alirezabagheri.simplecosttracker.data.Budget
import fr.alirezabagheri.simplecosttracker.data.Income
import fr.alirezabagheri.simplecosttracker.data.Period
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
    val periods by viewModel.periods.collectAsState()
    val activePeriod by viewModel.activePeriod.collectAsState()
    val incomes by viewModel.activePeriodIncomes.collectAsState()
    val totalIncomes by viewModel.totalIncomes.collectAsState()
    val budgets by viewModel.activePeriodBudgets.collectAsState()
    val totalBudgets by viewModel.totalBudgets.collectAsState()
    val remainingAmount by viewModel.remainingAmount.collectAsState()
    var isDropdownExpanded by remember { mutableStateOf(false) }
    val username = auth.currentUser?.email?.split("@")?.get(0) ?: "User"

    var showDeleteDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<Any?>(null) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column { // Top items
                        Spacer(Modifier.height(12.dp))
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.DateRange, contentDescription = "Manage Periods") },
                            label = { Text("Manage Periods") },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate(Screen.PeriodsScreen.route)
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        val isDataManagementEnabled = activePeriod != null
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Filled.TrendingUp, contentDescription = "Manage Incomes") },
                            label = { Text("Manage Incomes") },
                            selected = false,
                            onClick = {
                                if (isDataManagementEnabled) {
                                    activePeriod?.let {
                                        scope.launch { drawerState.close() }
                                        navController.navigate(Screen.IncomesScreen.createRoute(it.id))
                                    }
                                }
                            },
                            colors = if (isDataManagementEnabled) NavigationDrawerItemDefaults.colors() else NavigationDrawerItemDefaults.colors(
                                unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        )
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Manage Budgets") },
                            label = { Text("Manage Budgets") },
                            selected = false,
                            onClick = {
                                if (isDataManagementEnabled) {
                                    activePeriod?.let {
                                        scope.launch { drawerState.close() }
                                        navController.navigate(Screen.BudgetsScreen.createRoute(it.id))
                                    }
                                }
                            },
                            colors = if (isDataManagementEnabled) NavigationDrawerItemDefaults.colors() else NavigationDrawerItemDefaults.colors(
                                unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        )
                    }
                    Column { // Bottom items
                        NavigationDrawerItem(
                            icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Sign Out") },
                            label = { Text("Sign Out") },
                            selected = false,
                            onClick = {
                                auth.signOut()
                                navController.navigate(Screen.LoginScreen.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        inclusive = true
                                    }
                                }
                            }
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Dashboard") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(imageVector = Icons.Filled.Menu, contentDescription = "Menu")
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
                // Welcome Text Block, Period Selector, and Dates remain the same...

                LazyColumn(modifier = Modifier.weight(1f)) {
                    // Incomes Section
                    item {
                        Text(text = "Incomes", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 8.dp))
                    }
                    items(incomes) { income ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(income.description, style = MaterialTheme.typography.bodyLarge)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(NumberFormatter.format(income.amount), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                    IconButton(onClick = { itemToDelete = income; showDeleteDialog = true }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Income")
                                    }
                                }
                            }
                        }
                    }
                    item {
                        if (incomes.isNotEmpty()) {
                            Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("Total Income", style = MaterialTheme.typography.titleMedium)
                                    Text(NumberFormatter.format(totalIncomes), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Budgets Section
                    item {
                        Text(text = "Budgets", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 8.dp))
                    }
                    items(budgets) { budget ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(budget.category, style = MaterialTheme.typography.bodyLarge)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(NumberFormatter.format(budget.allocatedAmount), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                    IconButton(onClick = { itemToDelete = budget; showDeleteDialog = true }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Budget")
                                    }
                                }
                            }
                        }
                    }
                    item {
                        if (budgets.isNotEmpty()) {
                            Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("Total Budget", style = MaterialTheme.typography.titleMedium)
                                    Text(NumberFormatter.format(totalBudgets), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Final Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Remaining", style = MaterialTheme.typography.titleMedium)
                        Text(NumberFormatter.format(remainingAmount), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
                    is Income -> Text("Are you sure you want to delete this income? This action cannot be undone.")
                    is Budget -> Text("Are you sure you want to delete this budget? This action cannot be undone.")
                    is Period -> Text("Are you sure you want to delete this period and all its associated data? This action cannot be undone.")
                    else -> Text("Are you sure you want to delete this item?")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        when (val item = itemToDelete) {
                            is Income -> viewModel.deleteIncome(item.id)
                            is Budget -> viewModel.deleteBudget(item.id)
                            is Period -> viewModel.deleteActivePeriod()
                        }
                        showDeleteDialog = false
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false; itemToDelete = null }) { Text("Cancel") }
            }
        )
    }
}