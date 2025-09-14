package fr.alirezabagheri.simplecosttracker.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.dataObjects
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.util.Date

object FirestoreService {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Period-related functions
    fun addPeriod(name: String, startDate: Date, endDate: Date) {
        val userId = auth.currentUser?.uid ?: return
        val period = Period(userId = userId, name = name, startDate = startDate, endDate = endDate)
        db.collection("periods").add(period)
    }

    fun getPeriodsFlow(): Flow<List<Period>> {
        val userId = auth.currentUser?.uid ?: return kotlinx.coroutines.flow.flowOf(emptyList())
        return db.collection("periods")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .dataObjects<Period>()
    }

    suspend fun deletePeriodAndAssociatedData(periodId: String) {
        val batch = db.batch()
        val incomesToDelete = db.collection("incomes").whereEqualTo("periodId", periodId).get().await()
        incomesToDelete.documents.forEach { batch.delete(it.reference) }
        val budgetsToDelete = db.collection("budgets").whereEqualTo("periodId", periodId).get().await()
        budgetsToDelete.documents.forEach { batch.delete(it.reference) }
        val spendingsToDelete = db.collection("dailySpendings").whereEqualTo("periodId", periodId).get().await()
        spendingsToDelete.documents.forEach { batch.delete(it.reference) }
        val miscCostsToDelete = db.collection("miscCosts").whereEqualTo("periodId", periodId).get().await()
        miscCostsToDelete.documents.forEach { batch.delete(it.reference) }
        batch.commit().await()
        db.collection("periods").document(periodId).delete().await()
    }

    fun updatePeriodSpendingLimit(periodId: String, limit: Double) {
        db.collection("periods").document(periodId).update("dailySpendingLimit", limit)
    }

    fun updatePeriodNotes(periodId: String, notes: String) {
        db.collection("periods").document(periodId).update("notes", notes)
    }

    // Income-related functions
    fun addIncome(description: String, amount: Double, periodId: String) {
        val income = Income(periodId = periodId, description = description, amount = amount, date = Date())
        db.collection("incomes").add(income)
    }

    fun getIncomesFlow(periodId: String): Flow<List<Income>> {
        return db.collection("incomes").whereEqualTo("periodId", periodId).orderBy("date", Query.Direction.DESCENDING).dataObjects<Income>()
    }

    fun deleteIncome(incomeId: String) {
        db.collection("incomes").document(incomeId).delete()
    }

    // Budget-related functions
    fun addBudget(category: String, amount: Double, periodId: String) {
        val budget = Budget(periodId = periodId, category = category, allocatedAmount = amount)
        db.collection("budgets").add(budget)
    }

    fun getBudgetsFlow(periodId: String): Flow<List<Budget>> {
        return db.collection("budgets").whereEqualTo("periodId", periodId).dataObjects<Budget>()
    }

    fun deleteBudget(budgetId: String) {
        db.collection("budgets").document(budgetId).delete()
    }

    // Daily Spending-related functions
    fun addOrUpdateDailySpending(spendingId: String?, date: Date, spent: Double, periodId: String) {
        val spending = DailySpending(periodId = periodId, date = date, spent = spent)
        if (spendingId != null) {
            db.collection("dailySpendings").document(spendingId).set(spending)
        } else {
            db.collection("dailySpendings").add(spending)
        }
    }

    fun getDailySpendingsFlow(periodId: String): Flow<List<DailySpending>> {
        return db.collection("dailySpendings").whereEqualTo("periodId", periodId).orderBy("date").dataObjects<DailySpending>()
    }

    fun deleteDailySpending(spendingId: String) {
        db.collection("dailySpendings").document(spendingId).delete()
    }

    // Misc Cost-related functions
    fun addMiscCost(description: String, amount: Double, periodId: String) {
        val miscCost = MiscCost(periodId = periodId, description = description, amount = amount, date = Date())
        db.collection("miscCosts").add(miscCost)
    }

    fun getMiscCostsFlow(periodId: String): Flow<List<MiscCost>> {
        return db.collection("miscCosts").whereEqualTo("periodId", periodId).orderBy("date", Query.Direction.DESCENDING).dataObjects<MiscCost>()
    }

    fun deleteMiscCost(miscCostId: String) {
        db.collection("miscCosts").document(miscCostId).delete()
    }
}