package fr.alirezabagheri.simplecosttracker.data

import kotlinx.coroutines.flow.Flow
import java.util.Date

class CostTrackerRepository {

    fun addPeriod(name: String, startDate: Date, endDate: Date) =
        FirestoreService.addPeriod(name, startDate, endDate)

    fun getPeriodsFlow(): Flow<List<Period>> = FirestoreService.getPeriodsFlow()

    suspend fun deletePeriodAndAssociatedData(periodId: String) =
        FirestoreService.deletePeriodAndAssociatedData(periodId)

    fun updatePeriodSpendingLimit(periodId: String, limit: Double) =
        FirestoreService.updatePeriodSpendingLimit(periodId, limit)

    fun updatePeriodNotes(periodId: String, notes: String) =
        FirestoreService.updatePeriodNotes(periodId, notes)

    fun addIncome(description: String, amount: Double, periodId: String) =
        FirestoreService.addIncome(description, amount, periodId)

    fun getIncomesFlow(periodId: String): Flow<List<Income>> =
        FirestoreService.getIncomesFlow(periodId)

    fun deleteIncome(incomeId: String) = FirestoreService.deleteIncome(incomeId)

    fun addBudget(category: String, amount: Double, periodId: String) =
        FirestoreService.addBudget(category, amount, periodId)

    fun getBudgetsFlow(periodId: String): Flow<List<Budget>> =
        FirestoreService.getBudgetsFlow(periodId)

    fun deleteBudget(budgetId: String) = FirestoreService.deleteBudget(budgetId)

    fun addOrUpdateDailySpending(spendingId: String?, date: Date, spent: Double, periodId: String) =
        FirestoreService.addOrUpdateDailySpending(spendingId, date, spent, periodId)

    fun getDailySpendingsFlow(periodId: String): Flow<List<DailySpending>> =
        FirestoreService.getDailySpendingsFlow(periodId)

    fun deleteDailySpending(spendingId: String) = FirestoreService.deleteDailySpending(spendingId)

    fun addMiscCost(description: String, amount: Double, periodId: String) =
        FirestoreService.addMiscCost(description, amount, periodId)

    fun getMiscCostsFlow(periodId: String): Flow<List<MiscCost>> =
        FirestoreService.getMiscCostsFlow(periodId)

    fun deleteMiscCost(miscCostId: String) = FirestoreService.deleteMiscCost(miscCostId)
}