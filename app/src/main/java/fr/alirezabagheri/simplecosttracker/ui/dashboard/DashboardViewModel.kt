package fr.alirezabagheri.simplecosttracker.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.alirezabagheri.simplecosttracker.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel : ViewModel() {

    private val _periods = MutableStateFlow<List<Period>>(emptyList())
    val periods: StateFlow<List<Period>> = _periods.asStateFlow()

    private val _activePeriodId = MutableStateFlow<String?>(null)

    val activePeriod: StateFlow<Period?> = _activePeriodId.flatMapLatest { id ->
        if (id == null) {
            flowOf(null)
        } else {
            FirestoreService.getPeriodsFlow().map { periods -> periods.find { it.id == id } }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)


    val activePeriodIncomes: StateFlow<List<Income>> = activePeriod.flatMapLatest { p -> if (p != null) FirestoreService.getIncomesFlow(p.id) else flowOf(emptyList()) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val totalIncomes: StateFlow<Double> = activePeriodIncomes.map { it.sumOf(Income::amount) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val activePeriodBudgets: StateFlow<List<Budget>> = activePeriod.flatMapLatest { p -> if (p != null) FirestoreService.getBudgetsFlow(p.id) else flowOf(emptyList()) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val totalBudgets: StateFlow<Double> = activePeriodBudgets.map { it.sumOf(Budget::allocatedAmount) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val activePeriodSpendings: StateFlow<List<DailySpending>> = activePeriod.flatMapLatest { p -> if (p != null) FirestoreService.getDailySpendingsFlow(p.id) else flowOf(emptyList()) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val totalPeriodSpending: StateFlow<Double> = activePeriod.map { period ->
        if (period?.startDate != null && period.endDate != null) {
            val diff = period.endDate.time - period.startDate.time
            val days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) + 1
            days * period.dailySpendingLimit
        } else { 0.0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val activePeriodMiscCosts: StateFlow<List<MiscCost>> = activePeriod.flatMapLatest { p -> if (p != null) FirestoreService.getMiscCostsFlow(p.id) else flowOf(emptyList()) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val totalMiscCosts: StateFlow<Double> = activePeriodMiscCosts.map { it.sumOf(MiscCost::amount) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalRemaining: StateFlow<Double> = combine(totalIncomes, totalBudgets, totalPeriodSpending, totalMiscCosts) { incomes, budgets, spending, miscCosts ->
        incomes - budgets - spending - miscCosts
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val notesInput = MutableStateFlow("")

    init {
        viewModelScope.launch {
            FirestoreService.getPeriodsFlow().collect { periodList ->
                _periods.value = periodList
                if (_activePeriodId.value == null || periodList.none { it.id == _activePeriodId.value }) {
                    _activePeriodId.value = periodList.firstOrNull()?.id
                }
            }
        }
    }

    fun setNotesInput(notes: String) {
        notesInput.value = notes
    }

    fun saveNotes() {
        activePeriod.value?.id?.let {
            FirestoreService.updatePeriodNotes(it, notesInput.value)
        }
    }

    fun setActivePeriod(period: Period) {
        _activePeriodId.value = period.id
    }
    fun deleteIncome(incomeId: String) { FirestoreService.deleteIncome(incomeId) }
    fun deleteBudget(budgetId: String) { FirestoreService.deleteBudget(budgetId) }
    fun deleteDailySpending(spendingId: String) { FirestoreService.deleteDailySpending(spendingId) }
    fun deleteMiscCost(miscCostId: String) { FirestoreService.deleteMiscCost(miscCostId) }
    fun deleteActivePeriod() {
        viewModelScope.launch {
            _activePeriodId.value?.let { periodId ->
                FirestoreService.deletePeriodAndAssociatedData(periodId)
            }
        }
    }
}