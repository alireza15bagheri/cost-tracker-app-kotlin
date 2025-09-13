package fr.alirezabagheri.simplecosttracker.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.alirezabagheri.simplecosttracker.data.FirestoreService
import fr.alirezabagheri.simplecosttracker.data.Income
import fr.alirezabagheri.simplecosttracker.data.Period
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel : ViewModel() {

    private val _periods = MutableStateFlow<List<Period>>(emptyList())
    val periods: StateFlow<List<Period>> = _periods.asStateFlow()

    private val _activePeriod = MutableStateFlow<Period?>(null)
    val activePeriod: StateFlow<Period?> = _activePeriod.asStateFlow()

    val activePeriodIncomes: StateFlow<List<Income>> = _activePeriod.flatMapLatest { period ->
        if (period != null) {
            FirestoreService.getIncomesFlow(period.id)
        } else {
            MutableStateFlow(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalIncomes: StateFlow<Double> = activePeriodIncomes.map { incomes ->
        incomes.sumOf { it.amount }
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

    fun deleteActivePeriod() {
        viewModelScope.launch {
            _activePeriod.value?.id?.let {
                FirestoreService.deletePeriodAndAssociatedData(it)
                // After deletion, reset the active period
                _activePeriod.value = _periods.value.firstOrNull()
            }
        }
    }
}