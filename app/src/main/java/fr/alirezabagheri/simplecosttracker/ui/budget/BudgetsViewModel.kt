package fr.alirezabagheri.simplecosttracker.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.alirezabagheri.simplecosttracker.data.Budget
import fr.alirezabagheri.simplecosttracker.data.CostTrackerRepository
import fr.alirezabagheri.simplecosttracker.util.NumberFormatter
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BudgetsViewModel(private val periodId: String) : ViewModel() {

    private val repository = CostTrackerRepository()
    private val _budgets = MutableStateFlow<List<Budget>>(emptyList())
    val budgets: StateFlow<List<Budget>> = _budgets.asStateFlow()

    val totalBudgets: StateFlow<Double> = _budgets.map { budgetList ->
        budgetList.sumOf { it.allocatedAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val category = MutableStateFlow("")
    val amount = MutableStateFlow("")

    init {
        viewModelScope.launch {
            repository.getBudgetsFlow(periodId).collect {
                _budgets.value = it
            }
        }
    }

    fun addBudget() {
        val cat = category.value
        val amountValue = NumberFormatter.parse(amount.value)

        if (cat.isNotBlank() && amountValue != null && amountValue > 0) {
            repository.addBudget(cat, amountValue, periodId)
            category.value = ""
            amount.value = ""
        }
    }
}

class BudgetsViewModelFactory(private val periodId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BudgetsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BudgetsViewModel(periodId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}