package fr.alirezabagheri.simplecosttracker.ui.income

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.alirezabagheri.simplecosttracker.data.FirestoreService
import fr.alirezabagheri.simplecosttracker.data.Income
import fr.alirezabagheri.simplecosttracker.util.NumberFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class IncomesViewModel(private val periodId: String) : ViewModel() {

    private val _incomes = MutableStateFlow<List<Income>>(emptyList())
    val incomes: StateFlow<List<Income>> = _incomes.asStateFlow()

    val totalIncomes: StateFlow<Double> = _incomes.map { incomeList ->
        incomeList.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val description = MutableStateFlow("")
    val amount = MutableStateFlow("")

    init {
        viewModelScope.launch {
            FirestoreService.getIncomesFlow(periodId).collect {
                _incomes.value = it
            }
        }
    }

    fun addIncome() {
        val desc = description.value
        val amountValue = NumberFormatter.parse(amount.value) // Use parser

        if (desc.isNotBlank() && amountValue != null && amountValue > 0) {
            FirestoreService.addIncome(desc, amountValue, periodId)
            description.value = ""
            amount.value = ""
        }
    }
}

class IncomesViewModelFactory(private val periodId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IncomesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return IncomesViewModel(periodId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}