package fr.alirezabagheri.simplecosttracker.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.alirezabagheri.simplecosttracker.data.Budget
import fr.alirezabagheri.simplecosttracker.data.FirestoreService
import fr.alirezabagheri.simplecosttracker.data.Income
import fr.alirezabagheri.simplecosttracker.data.Period
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel : ViewModel() {

    private val _periods = MutableStateFlow<List<Period>>(emptyList())
    val periods: StateFlow<List<Period>> = _periods.asStateFlow()

    private val _activePeriod = MutableStateFlow<Period?>(null)
    val activePeriod: StateFlow<Period?> = _activePeriod.asStateFlow()

    val activePeriodIncomes: StateFlow<List<Income>> = _activePeriod.flatMapLatest { period ->
        if (period != null) FirestoreService.getIncomesFlow(period.id)
        else MutableStateFlow(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalIncomes: StateFlow<Double> = activePeriodIncomes.map { it.sumOf(Income::amount) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val activePeriodBudgets: StateFlow<List<Budget>> = _activePeriod.flatMapLatest { period ->
        if (period != null) FirestoreService.getBudgetsFlow(period.id)
        else MutableStateFlow(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalBudgets: StateFlow<Double> = activePeriodBudgets.map { it.sumOf(Budget::allocatedAmount) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val remainingAmount: StateFlow<Double> = combine(totalIncomes, totalBudgets) { incomes, budgets ->
        incomes - budgets
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)


    init {
        viewModelScope.launch {
            FirestoreService.getPeriodsFlow().collect { periodList ->
                _periods.value = periodList
                if (_activePeriod.value == null || periodList.none { it.id == _activePeriod.value?.id }) {
                    _activePeriod.value = periodList.firstOrNull()
                }
            }
        }
    }

    fun setActivePeriod(period: Period) {
        _activePeriod.value = period
    }

    fun deleteIncome(incomeId: String) {
        FirestoreService.deleteIncome(incomeId)
    }

    fun deleteBudget(budgetId: String) {
        FirestoreService.deleteBudget(budgetId)
    }

    fun deleteActivePeriod() {
        viewModelScope.launch {
            _activePeriod.value?.id?.let {
                FirestoreService.deletePeriodAndAssociatedData(it)
                _activePeriod.value = _periods.value.firstOrNull()
            }
        }
    }
}