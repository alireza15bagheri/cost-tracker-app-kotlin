package fr.alirezabagheri.simplecosttracker.ui.dashboard

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import fr.alirezabagheri.simplecosttracker.data.*

@Composable
fun ConfirmationDialog(itemToDelete: Any?, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Deletion") },
        text = {
            val text = when (itemToDelete) {
                is Income -> "Are you sure you want to delete this income?"
                is Budget -> "Are you sure you want to delete this budget?"
                is DailySpending -> "Are you sure you want to delete this spending entry?"
                is MiscCost -> "Are you sure you want to delete this cost?"
                is Period -> "Are you sure you want to delete this period and all its associated data?"
                else -> "Are you sure you want to delete this item?"
            } + "\nThis action cannot be undone."
            Text(text)
        },
        confirmButton = { Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Delete") } },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } }
    )
}