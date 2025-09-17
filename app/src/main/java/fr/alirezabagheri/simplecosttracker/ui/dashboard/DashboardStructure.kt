package fr.alirezabagheri.simplecosttracker.ui.dashboard

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import fr.alirezabagheri.simplecosttracker.Screen
import fr.alirezabagheri.simplecosttracker.data.Period
import fr.alirezabagheri.simplecosttracker.util.NumberFormatter
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopSection(
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
    Spacer(modifier = Modifier.height(16.dp))
    activePeriod?.let { period ->
        val dateFormatter = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Start: ${period.startDate?.let { dateFormatter.format(it) } ?: "N/A"}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "End: ${period.endDate?.let { dateFormatter.format(it) } ?: "N/A"}", style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = { onDeleteClick(period) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Period", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
}

@Composable
fun NotesSection(notes: String, onNotesChange: (String) -> Unit, onSaveClick: () -> Unit) {
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
fun FinalSummarySection(totalRemaining: Double) {
    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total Remaining", style = MaterialTheme.typography.titleMedium)
            Text(NumberFormatter.format(totalRemaining), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DashboardDrawerContent(
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