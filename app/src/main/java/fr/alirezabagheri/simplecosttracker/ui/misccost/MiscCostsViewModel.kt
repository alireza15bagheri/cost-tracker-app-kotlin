package fr.alirezabagheri.simplecosttracker.ui.misccost

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.alirezabagheri.simplecosttracker.data.FirestoreService
import fr.alirezabagheri.simplecosttracker.data.MiscCost
import fr.alirezabagheri.simplecosttracker.util.NumberFormatter
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MiscCostsViewModel(private val periodId: String) : ViewModel() {

    private val _miscCosts = MutableStateFlow<List<MiscCost>>(emptyList())
    val miscCosts: StateFlow<List<MiscCost>> = _miscCosts.asStateFlow()

    val totalMiscCosts: StateFlow<Double> = _miscCosts.map { costList ->
        costList.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val description = MutableStateFlow("")
    val amount = MutableStateFlow("")

    init {
        viewModelScope.launch {
            FirestoreService.getMiscCostsFlow(periodId).collect {
                _miscCosts.value = it
            }
        }
    }

    fun addMiscCost() {
        val desc = description.value
        val amountValue = NumberFormatter.parse(amount.value)

        if (desc.isNotBlank() && amountValue != null && amountValue > 0) {
            FirestoreService.addMiscCost(desc, amountValue, periodId)
            description.value = ""
            amount.value = ""
        }
    }
}

class MiscCostsViewModelFactory(private val periodId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MiscCostsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MiscCostsViewModel(periodId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}