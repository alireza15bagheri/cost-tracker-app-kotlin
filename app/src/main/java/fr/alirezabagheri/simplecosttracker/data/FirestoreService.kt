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
        val period = Period(
            userId = userId,
            name = name,
            startDate = startDate,
            endDate = endDate
        )
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

        // 1. Find and mark all incomes for deletion
        val incomesToDelete = db.collection("incomes").whereEqualTo("periodId", periodId).get().await()
        incomesToDelete.documents.forEach { batch.delete(it.reference) }

        // 2. Find and mark all budgets for deletion
        val budgetsToDelete = db.collection("budgets").whereEqualTo("periodId", periodId).get().await()
        budgetsToDelete.documents.forEach { batch.delete(it.reference) }

        // 3. Commit all deletions
        batch.commit().await()

        // 4. Delete the period itself
        db.collection("periods").document(periodId).delete().await()
    }

    // Income-related functions
    fun addIncome(description: String, amount: Double, periodId: String) {
        val income = Income(
            periodId = periodId,
            description = description,
            amount = amount,
            date = Date()
        )
        db.collection("incomes").add(income)
    }

    fun getIncomesFlow(periodId: String): Flow<List<Income>> {
        return db.collection("incomes")
            .whereEqualTo("periodId", periodId)
            .orderBy("date", Query.Direction.DESCENDING)
            .dataObjects<Income>()
    }

    fun deleteIncome(incomeId: String) {
        db.collection("incomes").document(incomeId).delete()
    }

    // Budget-related functions
    fun addBudget(category: String, amount: Double, periodId: String) {
        val budget = Budget(
            periodId = periodId,
            category = category,
            allocatedAmount = amount
        )
        db.collection("budgets").add(budget)
    }

    fun getBudgetsFlow(periodId: String): Flow<List<Budget>> {
        return db.collection("budgets")
            .whereEqualTo("periodId", periodId)
            .dataObjects<Budget>()
    }

    fun deleteBudget(budgetId: String) {
        db.collection("budgets").document(budgetId).delete()
    }
}