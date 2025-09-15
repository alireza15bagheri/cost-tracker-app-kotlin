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
        // This flow listens for changes in the list of all periods
        viewModelScope.launch {
            repository.getPeriodsFlow().collect { periods ->
                _uiState.update { it.copy(periods = periods) }
                // If there's no active period or the active one was deleted, select the first available
                if (_activePeriodId.value == null || periods.none { it.id == _activePeriodId.value }) {
                    setActivePeriod(periods.firstOrNull())
                }
            }
        }

        // This flow listens to the active period ID and fetches all related data when it changes
        viewModelScope.launch {
            _activePeriodId.flatMapLatest { id ->
                if (id == null) {
                    // If no period is selected, flow empty data
                    flowOf(DashboardUiState(periods = _uiState.value.periods))
                } else {
                    // When an ID is available, combine all data sources for that period
                    combine(
                        repository.getPeriodsFlow().map { it.find { p -> p.id == id } },
                        repository.getIncomesFlow(id),
                        repository.getBudgetsFlow(id),
                        repository.getDailySpendingsFlow(id),
                        repository.getMiscCostsFlow(id)
                    ) { period, incomes, budgets, spendings, miscCosts ->
                        // Perform all calculations here
                        val totalIncomes = incomes.sumOf { it.amount }
                        val totalBudgets = budgets.sumOf { it.allocatedAmount }
                        val totalPeriodSpending = if (period?.startDate != null && period.endDate != null) {
                            val diff = period.endDate.time - period.startDate.time
                            val days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) + 1
                            days * period.dailySpendingLimit
                        } else 0.0
                        val totalMiscCosts = miscCosts.sumOf { it.amount }
                        val totalRemaining = totalIncomes - totalBudgets - totalPeriodSpending - totalMiscCosts

                        // Update the single state object
                        _uiState.value.copy(
                            activePeriod = period,
                            incomes = incomes,
                            totalIncomes = totalIncomes,
                            budgets = budgets,
                            totalBudgets = totalBudgets,
                            spendings = spendings,
                            totalPeriodSpending = totalPeriodSpending,
                            miscCosts = miscCosts,
                            totalMiscCosts = totalMiscCosts,
                            totalRemaining = totalRemaining,
                            notesInput = period?.notes ?: ""
                        )
                    }
                }
            }.collect { newState ->
                // Emit the new comprehensive state
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

    fun deleteIncome(incomeId: String) { repository.deleteIncome(incomeId) }
    fun deleteBudget(budgetId: String) { repository.deleteBudget(budgetId) }
    fun deleteDailySpending(spendingId: String) { repository.deleteDailySpending(spendingId) }
    fun deleteMiscCost(miscCostId: String) { repository.deleteMiscCost(miscCostId) }

    fun deleteActivePeriod() {
        viewModelScope.launch {
            _activePeriodId.value?.let { periodId ->
                repository.deletePeriodAndAssociatedData(periodId)
                // The main collector will handle selecting a new period
            }
        }
    }
}