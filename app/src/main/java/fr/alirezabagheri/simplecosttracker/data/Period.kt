package fr.alirezabagheri.simplecosttracker.data

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Period(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val startDate: Date? = null,
    val endDate: Date? = null,
    val dailySpendingLimit: Double = 0.0,
    val notes: String = "",
    @ServerTimestamp val createdAt: Date? = null
)