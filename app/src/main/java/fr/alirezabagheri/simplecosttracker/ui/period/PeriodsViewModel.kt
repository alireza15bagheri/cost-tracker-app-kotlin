package fr.alirezabagheri.simplecosttracker.ui.period

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.alirezabagheri.simplecosttracker.data.FirestoreService
import fr.alirezabagheri.simplecosttracker.data.Period
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

class PeriodsViewModel : ViewModel() {

    // State for the list of existing periods
    private val _periods = MutableStateFlow<List<Period>>(emptyList())
    val periods: StateFlow<List<Period>> = _periods.asStateFlow()

    // State for the input fields
    val periodName = MutableStateFlow("")
    val startDate = MutableStateFlow<Date?>(null)
    val endDate = MutableStateFlow<Date?>(null)

    init {
        viewModelScope.launch {
            FirestoreService.getPeriodsFlow().collect {
                _periods.value = it
            }
        }
    }

    fun addPeriod() {
        val name = periodName.value
        val start = startDate.value
        val end = endDate.value

        if (name.isNotBlank() && start != null && end != null) {
            FirestoreService.addPeriod(name, start, end)
            // Reset fields
            periodName.value = ""
            startDate.value = null
            endDate.value = null
        }
    }
}