package fr.alirezabagheri.simplecosttracker.data

import com.google.firebase.firestore.DocumentId

data class Budget(
    @DocumentId val id: String = "",
    val periodId: String = "",
    val category: String = "",
    val allocatedAmount: Double = 0.0
)