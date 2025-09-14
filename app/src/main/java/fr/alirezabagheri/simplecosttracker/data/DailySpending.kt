package fr.alirezabagheri.simplecosttracker.data

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class DailySpending(
    @DocumentId val id: String = "",
    val periodId: String = "",
    val date: Date = Date(),
    val spent: Double = 0.0
)