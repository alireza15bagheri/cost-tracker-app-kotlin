package fr.alirezabagheri.simplecosttracker.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.dataObjects
import kotlinx.coroutines.flow.Flow
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
}