package fr.alirezabagheri.simplecosttracker.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.alirezabagheri.simplecosttracker.data.*
import fr.alirezabagheri.simplecosttracker.ui.common.DeletableCard
import fr.alirezabagheri.simplecosttracker.ui.common.TotalCard
import fr.alirezabagheri.simplecosttracker.util.NumberFormatter
import java.text.SimpleDateFormat
import java.util.Locale

fun LazyListScope.incomesSection(incomes: List<Income>, totalIncomes: Double, onDeleteClick: (Income) -> Unit) {
    item { Text(text = "Incomes", style = MaterialTheme.typography.titleLarge, modifier = Modifier.fillMaxWidth()) }
    items(incomes) { income ->
        DeletableCard(
            description = income.description,
            amount = income.amount,
            onDeleteClick = { onDeleteClick(income) }
        )
    }
    item {
        if (incomes.isNotEmpty()) {
            TotalCard(label = "Total Income", total = totalIncomes, color = MaterialTheme.colorScheme.secondaryContainer)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

fun LazyListScope.budgetsSection(budgets: List<Budget>, totalBudgets: Double, onDeleteClick: (Budget) -> Unit) {
    item { Text(text = "Budgets", style = MaterialTheme.typography.titleLarge, modifier = Modifier.fillMaxWidth()) }
    items(budgets) { budget ->
        DeletableCard(
            description = budget.category,
            amount = budget.allocatedAmount,
            onDeleteClick = { onDeleteClick(budget) }
        )
    }
    item {
        if (budgets.isNotEmpty()) {
            TotalCard(label = "Total Budget", total = totalBudgets, color = MaterialTheme.colorScheme.tertiaryContainer)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

fun LazyListScope.dailySpendingsSection(spendings: List<DailySpending>, totalSpending: Double, onDeleteClick: (DailySpending) -> Unit) {
    item {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Daily House Spendings", style = MaterialTheme.typography.titleLarge)
            Text(
                text = "Total Planned: ${NumberFormatter.format(totalSpending)}",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
    items(spendings) { spending ->
        DeletableCard(
            description = SimpleDateFormat("EEE, dd MMM", Locale.getDefault()).format(spending.date),
            amount = spending.spent,
            onDeleteClick = { onDeleteClick(spending) }
        )
    }
    item { Spacer(modifier = Modifier.height(24.dp)) }
}

fun LazyListScope.miscCostsSection(miscCosts: List<MiscCost>, totalMiscCosts: Double, onDeleteClick: (MiscCost) -> Unit) {
    item { Text(text = "Miscellaneous Costs", style = MaterialTheme.typography.titleLarge, modifier = Modifier.fillMaxWidth()) }
    items(miscCosts) { cost ->
        DeletableCard(
            description = cost.description,
            amount = cost.amount,
            onDeleteClick = { onDeleteClick(cost) }
        )
    }
    item {
        if (miscCosts.isNotEmpty()) {
            TotalCard(label = "Total Misc. Costs", total = totalMiscCosts, color = MaterialTheme.colorScheme.secondaryContainer)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}