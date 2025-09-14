package fr.alirezabagheri.simplecosttracker.ui.spending

import java.util.Date

data class DailySpendingItem(
    val id: String?,
    val date: Date,
    val spent: Double,
    val limit: Double,
    val carryover: Double,
    val remaining: Double
)