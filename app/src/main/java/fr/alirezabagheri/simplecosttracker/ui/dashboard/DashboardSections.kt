package fr.alirezabagheri.simplecosttracker.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.alirezabagheri.simplecosttracker.R
import fr.alirezabagheri.simplecosttracker.data.*
import fr.alirezabagheri.simplecosttracker.ui.common.DeletableCard
import fr.alirezabagheri.simplecosttracker.ui.common.TotalCard
import fr.alirezabagheri.simplecosttracker.util.NumberFormatter
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleLarge, modifier = Modifier.fillMaxWidth())
}

fun LazyListScope.incomesSection(incomes: List<Income>, totalIncomes: Double, onDeleteClick: (Income) -> Unit) {
    item { SectionTitle(text = stringResource(id = R.string.incomes)) }
    items(incomes) { income ->
        DeletableCard(
            description = income.description,
            amount = income.amount,
            onDeleteClick = { onDeleteClick(income) }
        )
    }
    item {
        if (incomes.isNotEmpty()) {
            TotalCard(label = stringResource(id = R.string.total_income), total = totalIncomes, color = MaterialTheme.colorScheme.secondaryContainer)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

fun LazyListScope.budgetsSection(
    budgets: List<Budget>,
    totalBudgets: Double,
    onDeleteClick: (Budget) -> Unit,
    onTogglePaidStatus: (Budget) -> Unit
) {
    item { SectionTitle(text = stringResource(id = R.string.budgets)) }
    items(budgets) { budget ->
        val backgroundColor = if (budget.isPaid) Color(0xFFC8E6C9) else Color(0xFFFFCDD2)
        val statusText = if (budget.isPaid) stringResource(id = R.string.paid) else stringResource(id = R.string.unpaid)
        val contentColor = if (budget.isPaid) Color(0xFF256029) else Color(0xFFC63939)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable { onTogglePaidStatus(budget) },
            colors = CardDefaults.cardColors(containerColor = backgroundColor, contentColor = contentColor)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = budget.category,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = NumberFormatter.format(budget.allocatedAmount),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { onDeleteClick(budget) },
                            colors = IconButtonDefaults.iconButtonColors(contentColor = contentColor)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(id = R.string.delete))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth(),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
    item {
        if (budgets.isNotEmpty()) {
            TotalCard(label = stringResource(id = R.string.total_budget), total = totalBudgets, color = MaterialTheme.colorScheme.tertiaryContainer)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

fun LazyListScope.dailySpendingsSection(spendings: List<DailySpending>, totalSpending: Double, onDeleteClick: (DailySpending) -> Unit) {
    item {
        Column(modifier = Modifier.fillMaxWidth()) {
            SectionTitle(text = stringResource(id = R.string.daily_house_spendings))
            Text(
                text = stringResource(id = R.string.total_planned, NumberFormatter.format(totalSpending)),
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
    item { SectionTitle(text = stringResource(id = R.string.miscellaneous_costs)) }
    items(miscCosts) { cost ->
        DeletableCard(
            description = cost.description,
            amount = cost.amount,
            onDeleteClick = { onDeleteClick(cost) }
        )
    }
    item {
        if (miscCosts.isNotEmpty()) {
            TotalCard(label = stringResource(id = R.string.total_misc_costs), total = totalMiscCosts, color = MaterialTheme.colorScheme.secondaryContainer)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}