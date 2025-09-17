package fr.alirezabagheri.simplecosttracker.ui.dashboard

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import fr.alirezabagheri.simplecosttracker.R
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
        Text(text = stringResource(id = R.string.welcome), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Text(text = username, style = MaterialTheme.typography.headlineSmall)
    }
    Spacer(modifier = Modifier.height(24.dp))
    ExposedDropdownMenuBox(expanded = isDropdownExpanded, onExpandedChange = onExpandedChange) {
        OutlinedTextField(
            value = activePeriod?.name ?: stringResource(id = R.string.no_period_selected),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(id = R.string.active_period)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
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
                val startDate = period.startDate?.let { dateFormatter.format(it) } ?: stringResource(id = R.string.na)
                val endDate = period.endDate?.let { dateFormatter.format(it) } ?: stringResource(id = R.string.na)
                Text(text = stringResource(id = R.string.start_date, startDate), style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = stringResource(id = R.string.end_date, endDate), style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = { onDeleteClick(period) }) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(id = R.string.delete_period), tint = MaterialTheme.colorScheme.error)
            }
        }
    }
    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
}

@Composable
fun NotesSection(notes: String, onNotesChange: (String) -> Unit, onSaveClick: () -> Unit) {
    val context = LocalContext.current
    Text(text = stringResource(id = R.string.notes), style = MaterialTheme.typography.titleLarge, modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = notes,
        onValueChange = onNotesChange,
        modifier = Modifier.fillMaxWidth().height(150.dp),
        label = { Text(stringResource(id = R.string.notes_placeholder)) }
    )
    Spacer(modifier = Modifier.height(8.dp))
    Button(
        onClick = {
            onSaveClick()
            Toast.makeText(context, context.getString(R.string.notes_saved), Toast.LENGTH_SHORT).show()
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(id = R.string.save_notes))
    }
    Spacer(modifier = Modifier.height(24.dp))
}

@Composable
fun FinalSummarySection(totalRemaining: Double) {
    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(id = R.string.total_remaining), style = MaterialTheme.typography.titleMedium)
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
                NavigationDrawerItem(icon = { Icon(Icons.Default.DateRange, contentDescription = null) }, label = { Text(stringResource(id = R.string.manage_periods)) }, selected = false, onClick = { onCloseDrawer(); navController.navigate(Screen.PeriodsScreen.route) })
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                val isDataManagementEnabled = activePeriod != null
                val colors = if (isDataManagementEnabled) NavigationDrawerItemDefaults.colors() else NavigationDrawerItemDefaults.colors(unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f), unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f))
                val onNavigate: (String) -> Unit = { route ->
                    if (isDataManagementEnabled) {
                        onCloseDrawer()
                        navController.navigate(route)
                    }
                }

                NavigationDrawerItem(icon = { Icon(Icons.Default.TrendingUp, contentDescription = null) }, label = { Text(stringResource(id = R.string.manage_incomes)) }, selected = false, onClick = { activePeriod?.id?.let { onNavigate(Screen.IncomesScreen.createRoute(it)) } }, colors = colors)
                NavigationDrawerItem(icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null) }, label = { Text(stringResource(id = R.string.manage_budgets)) }, selected = false, onClick = { activePeriod?.id?.let { onNavigate(Screen.BudgetsScreen.createRoute(it)) } }, colors = colors)
                NavigationDrawerItem(icon = { Icon(Icons.Default.Home, contentDescription = null) }, label = { Text(stringResource(id = R.string.daily_spending)) }, selected = false, onClick = { activePeriod?.id?.let { onNavigate(Screen.DailySpendingsScreen.createRoute(it)) } }, colors = colors)
                NavigationDrawerItem(icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) }, label = { Text(stringResource(id = R.string.misc_costs)) }, selected = false, onClick = { activePeriod?.id?.let { onNavigate(Screen.MiscCostsScreen.createRoute(it)) } }, colors = colors)
            }
            Column {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                NavigationDrawerItem(icon = { Icon(Icons.Default.Lock, contentDescription = null) }, label = { Text(stringResource(id = R.string.change_password)) }, selected = false, onClick = { onCloseDrawer(); navController.navigate(Screen.ChangePasswordScreen.route) })
                NavigationDrawerItem(icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null) }, label = { Text(stringResource(id = R.string.sign_out)) }, selected = false, onClick = { onCloseDrawer(); auth.signOut(); navController.navigate(Screen.LoginScreen.route) { popUpTo(navController.graph.startDestinationId) { inclusive = true } } })
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}