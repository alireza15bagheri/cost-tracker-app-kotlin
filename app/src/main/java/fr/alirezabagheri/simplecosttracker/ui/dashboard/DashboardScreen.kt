package fr.alirezabagheri.simplecosttracker.ui.dashboard

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import fr.alirezabagheri.simplecosttracker.R
import fr.alirezabagheri.simplecosttracker.data.*
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
    val context = LocalContext.current
    var backPressedTime by remember { mutableStateOf(0L) }

    val onDeleteClick = { item: Any ->
        itemToDelete = item
        showDeleteDialog = true
    }

    // Intercepts the back button press on this screen
    BackHandler(enabled = true) {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            (context as? Activity)?.finish()
        } else {
            Toast.makeText(context, context.getString(R.string.press_back_again_to_exit), Toast.LENGTH_SHORT).show()
        }
        backPressedTime = System.currentTimeMillis()
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
                    title = { Text(stringResource(id = R.string.dashboard)) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(imageVector = Icons.Filled.Menu, contentDescription = stringResource(id = R.string.menu))
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
                budgetsSection(
                    budgets = uiState.budgets,
                    totalBudgets = uiState.totalBudgets,
                    onDeleteClick = onDeleteClick,
                    onTogglePaidStatus = viewModel::toggleBudgetPaidStatus
                )
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