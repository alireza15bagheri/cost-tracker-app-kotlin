package fr.alirezabagheri.simplecosttracker.data

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class Income(
    @DocumentId val id: String = "",
    val periodId: String = "",
    val description: String = "",
    val amount: Double = 0.0,
    val date: Date? = null
)