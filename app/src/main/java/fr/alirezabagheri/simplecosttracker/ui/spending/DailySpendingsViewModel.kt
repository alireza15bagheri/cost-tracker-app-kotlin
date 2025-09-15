package fr.alirezabagheri.simplecosttracker.ui.spending

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.alirezabagheri.simplecosttracker.data.CostTrackerRepository
import fr.alirezabagheri.simplecosttracker.data.DailySpending
import fr.alirezabagheri.simplecosttracker.data.Period
import fr.alirezabagheri.simplecosttracker.util.NumberFormatter
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class DailySpendingsViewModel(private val periodId: String) : ViewModel() {

    private val repository = CostTrackerRepository()
    private val _period = MutableStateFlow<Period?>(null)
    val period: StateFlow<Period?> = _period.asStateFlow()

    private val _spendings = MutableStateFlow<List<DailySpending>>(emptyList())

    val calculatedSpendingItems: StateFlow<List<DailySpendingItem>> = combine(_period, _spendings) { period, spendings ->
        calculateSpendings(period, spendings)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.getPeriodsFlow().map { periods -> periods.find { it.id == periodId } }.collect {
                _period.value = it
            }
        }
        viewModelScope.launch {
            repository.getDailySpendingsFlow(periodId).collect {
                _spendings.value = it
            }
        }
    }

    fun updateSpendingLimit(limit: String) {
        val limitValue = NumberFormatter.parse(limit) ?: 0.0
        _period.value?.id?.let {
            repository.updatePeriodSpendingLimit(it, limitValue)
        }
    }

    fun addOrUpdateSpending(item: DailySpendingItem, spent: String) {
        val spentValue = NumberFormatter.parse(spent) ?: 0.0
        repository.addOrUpdateDailySpending(item.id, item.date, spentValue, periodId)
    }

    private fun calculateSpendings(period: Period?, spendings: List<DailySpending>): List<DailySpendingItem> {
        if (period?.startDate == null || period.endDate == null) return emptyList()

        val results = mutableListOf<DailySpendingItem>()
        val spendingsMap = spendings.associateBy {
            Calendar.getInstance().apply { time = it.date }.get(Calendar.DAY_OF_YEAR)
        }

        val cal = Calendar.getInstance().apply { time = period.startDate }
        val endCal = Calendar.getInstance().apply { time = period.endDate }

        var carryover = 0.0

        while (!cal.after(endCal)) {
            val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
            val currentSpending = spendingsMap[dayOfYear]
            val spent = currentSpending?.spent ?: 0.0
            val remaining = period.dailySpendingLimit + carryover - spent

            results.add(
                DailySpendingItem(
                    id = currentSpending?.id,
                    date = cal.time,
                    spent = spent,
                    limit = period.dailySpendingLimit,
                    carryover = carryover,
                    remaining = remaining
                )
            )

            carryover = remaining
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        return results
    }
}

class DailySpendingsViewModelFactory(private val periodId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DailySpendingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DailySpendingsViewModel(periodId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}