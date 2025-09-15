package fr.alirezabagheri.simplecosttracker.ui.period

import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.alirezabagheri.simplecosttracker.data.CostTrackerRepository
import fr.alirezabagheri.simplecosttracker.data.Period
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
class PeriodsViewModel : ViewModel() {

    private val repository = CostTrackerRepository()
    private val _periods = MutableStateFlow<List<Period>>(emptyList())
    val periods: StateFlow<List<Period>> = _periods.asStateFlow()

    val periodName = MutableStateFlow("")

    val startDatePickerState = DatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis(),
        locale = Locale.getDefault()
    )
    val endDatePickerState = DatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis(),
        locale = Locale.getDefault()
    )

    init {
        viewModelScope.launch {
            repository.getPeriodsFlow().collect {
                _periods.value = it
            }
        }
    }

    fun addPeriod() {
        val name = periodName.value
        val start = startDatePickerState.selectedDateMillis?.let { Date(it) }
        val end = endDatePickerState.selectedDateMillis?.let { Date(it) }

        if (name.isNotBlank() && start != null && end != null) {
            repository.addPeriod(name, start, end)
            periodName.value = ""
        }
    }
}