package fr.alirezabagheri.simplecosttracker.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.alirezabagheri.simplecosttracker.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

data class DashboardUiState(
    val periods: List<Period> = emptyList(),
    val activePeriod: Period? = null,
    val incomes: List<Income> = emptyList(),
    val totalIncomes: Double = 0.0,
    val budgets: List<Budget> = emptyList(),
    val totalBudgets: Double = 0.0,
    val spendings: List<DailySpending> = emptyList(),
    val totalPeriodSpending: Double = 0.0,
    val miscCosts: List<MiscCost> = emptyList(),
    val totalMiscCosts: Double = 0.0,
    val totalRemaining: Double = 0.0,
    val notesInput: String = "",
    val isDropdownExpanded: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel : ViewModel() {

    private val repository = CostTrackerRepository()
    private val _activePeriodId = MutableStateFlow<String?>(null)
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Combine the full list of periods and the selected period ID to derive the UI state
            repository.getPeriodsFlow().combine(_activePeriodId) { periods, activeId ->
                // Determine the currently active ID. If none is selected or the selected one is invalid,
                // default to the first period in the list.
                val currentActiveId = if (activeId == null || periods.none { it.id == activeId }) {
                    periods.firstOrNull()?.id
                } else {
                    activeId
                }
                // Pass the full list and the definitive active ID to the next step
                Pair(periods, currentActiveId)
            }.flatMapLatest { (periods, activeId) ->
                // If there's an active ID, fetch all related data. Otherwise, flow a default state.
                if (activeId == null) {
                    flowOf(DashboardUiState(periods = periods))
                } else {
                    // When an ID is available, combine all data sources for that period
                    combine(
                        repository.getIncomesFlow(activeId),
                        repository.getBudgetsFlow(activeId),
                        repository.getDailySpendingsFlow(activeId),
                        repository.getMiscCostsFlow(activeId)
                    ) { incomes, budgets, spendings, miscCosts ->
                        // Perform all calculations here
                        val activePeriod = periods.find { it.id == activeId }
                        val totalIncomes = incomes.sumOf { it.amount }
                        val totalBudgets = budgets.sumOf { it.allocatedAmount }
                        val totalPeriodSpending = if (activePeriod?.startDate != null && activePeriod.endDate != null) {
                            val diff = activePeriod.endDate.time - activePeriod.startDate.time
                            val days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) + 1
                            days * activePeriod.dailySpendingLimit
                        } else 0.0
                        val totalMiscCosts = miscCosts.sumOf { it.amount }
                        val totalRemaining = totalIncomes - totalBudgets - totalPeriodSpending - totalMiscCosts

                        // Create the new, complete state object from scratch
                        DashboardUiState(
                            periods = periods,
                            activePeriod = activePeriod,
                            incomes = incomes,
                            totalIncomes = totalIncomes,
                            budgets = budgets,
                            totalBudgets = totalBudgets,
                            spendings = spendings,
                            totalPeriodSpending = totalPeriodSpending,
                            miscCosts = miscCosts,
                            totalMiscCosts = totalMiscCosts,
                            totalRemaining = totalRemaining,
                            notesInput = activePeriod?.notes ?: ""
                        )
                    }
                }
            }.collect { newState ->
                // This is now the single point where the UI state is updated
                _uiState.value = newState
            }
        }
    }

    // UI Event Handlers
    fun onDropdownExpandedChange(isExpanded: Boolean) {
        _uiState.update { it.copy(isDropdownExpanded = isExpanded) }
    }

    fun onNotesChange(newNotes: String) {
        _uiState.update { it.copy(notesInput = newNotes) }
    }

    fun saveNotes() {
        uiState.value.activePeriod?.id?.let {
            repository.updatePeriodNotes(it, uiState.value.notesInput)
        }
    }

    fun setActivePeriod(period: Period?) {
        _activePeriodId.value = period?.id
    }

    fun toggleBudgetPaidStatus(budget: Budget) {
        repository.updateBudgetPaidStatus(budget.id, !budget.isPaid)
    }

    fun deleteIncome(incomeId: String) { repository.deleteIncome(incomeId) }
    fun deleteBudget(budgetId: String) { repository.deleteBudget(budgetId) }
    fun deleteDailySpending(spendingId: String) { repository.deleteDailySpending(spendingId) }
    fun deleteMiscCost(miscCostId: String) { repository.deleteMiscCost(miscCostId) }

    fun deleteActivePeriod() {
        viewModelScope.launch {
            _activePeriodId.value?.let { periodId ->
                repository.deletePeriodAndAssociatedData(periodId)
                // The main collector will automatically handle selecting a new period
            }
        }
    }
}