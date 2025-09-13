package fr.alirezabagheri.simplecosttracker.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
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
import fr.alirezabagheri.simplecosttracker.data.Income
import fr.alirezabagheri.simplecosttracker.data.Period
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
                        val isIncomeEnabled = activePeriod != null
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Filled.TrendingUp, contentDescription = "Manage Incomes") },
                            label = { Text("Manage Incomes") },
                            selected = false,
                            onClick = {
                                if (isIncomeEnabled) {
                                    activePeriod?.let {
                                        scope.launch { drawerState.close() }
                                        navController.navigate(Screen.IncomesScreen.createRoute(it.id))
                                    }
                                }
                            },
                            colors = if (isIncomeEnabled) NavigationDrawerItemDefaults.colors() else NavigationDrawerItemDefaults.colors(
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
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Welcome", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    Text(text = username, style = MaterialTheme.typography.headlineSmall)
                }
                Spacer(modifier = Modifier.height(24.dp))
                ExposedDropdownMenuBox(expanded = isDropdownExpanded, onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }) {
                    OutlinedTextField(value = activePeriod?.name ?: "No Period Selected", onValueChange = {}, readOnly = true, label = { Text("Active Period") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) }, modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth())
                    ExposedDropdownMenu(expanded = isDropdownExpanded, onDismissRequest = { isDropdownExpanded = false }) {
                        periods.forEach { period ->
                            DropdownMenuItem(text = { Text(period.name) }, onClick = {
                                viewModel.setActivePeriod(period)
                                isDropdownExpanded = false
                            })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                activePeriod?.let { period ->
                    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Start: ${period.startDate?.let { dateFormatter.format(it) } ?: "N/A"}", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "End: ${period.endDate?.let { dateFormatter.format(it) } ?: "N/A"}", style = MaterialTheme.typography.bodyMedium)
                        IconButton(onClick = {
                            itemToDelete = period
                            showDeleteDialog = true
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Period", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                // Incomes List with Title and Total
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    // Title for the incomes section
                    item {
                        Text(
                            text = "Incomes",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    // List of income items
                    items(incomes) { income ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(income.description, style = MaterialTheme.typography.bodyLarge)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("%.2f".format(income.amount), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                    IconButton(onClick = {
                                        itemToDelete = income
                                        showDeleteDialog = true
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Income")
                                    }
                                }
                            }
                        }
                    }
                    // Total incomes display, placed as the last item in the list
                    item {
                        if (incomes.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Total Income", style = MaterialTheme.typography.titleMedium)
                                    Text("%.2f".format(totalIncomes), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                itemToDelete = null
            },
            title = { Text("Confirm Deletion") },
            text = {
                when (itemToDelete) {
                    is Income -> Text("Are you sure you want to delete this income? This action cannot be undone.")
                    is Period -> Text("Are you sure you want to delete this period and all its associated data (incomes, budgets, etc.)? This action cannot be undone.")
                    else -> Text("Are you sure you want to delete this item?")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        when (val item = itemToDelete) {
                            is Income -> viewModel.deleteIncome(item.id)
                            is Period -> viewModel.deleteActivePeriod()
                        }
                        showDeleteDialog = false
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(onClick = {
                    showDeleteDialog = false
                    itemToDelete = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}