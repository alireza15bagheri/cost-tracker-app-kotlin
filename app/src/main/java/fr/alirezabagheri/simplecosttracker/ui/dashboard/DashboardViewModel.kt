package fr.alirezabagheri.simplecosttracker.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.alirezabagheri.simplecosttracker.data.FirestoreService
import fr.alirezabagheri.simplecosttracker.data.Period
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {

    private val _periods = MutableStateFlow<List<Period>>(emptyList())
    val periods: StateFlow<List<Period>> = _periods.asStateFlow()

    private val _activePeriod = MutableStateFlow<Period?>(null)
    val activePeriod: StateFlow<Period?> = _activePeriod.asStateFlow()

    init {
        viewModelScope.launch {
            FirestoreService.getPeriodsFlow().collect { periodList ->
                _periods.value = periodList
                // Set the most recent period as active by default
                if (activePeriod.value == null) {
                    _activePeriod.value = periodList.firstOrNull()
                }
            }
        }
    }

    fun setActivePeriod(period: Period) {
        _activePeriod.value = period
    }
}