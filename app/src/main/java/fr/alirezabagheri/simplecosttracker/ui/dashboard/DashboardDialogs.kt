package fr.alirezabagheri.simplecosttracker.ui.dashboard

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import fr.alirezabagheri.simplecosttracker.R
import fr.alirezabagheri.simplecosttracker.data.*

@Composable
fun ConfirmationDialog(itemToDelete: Any?, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.confirm_deletion)) },
        text = {
            val message = when (itemToDelete) {
                is Period -> stringResource(id = R.string.confirm_deletion_period_message)
                else -> stringResource(id = R.string.confirm_deletion_message)
            }
            Text(message)
        },
        confirmButton = { Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text(stringResource(id = R.string.delete)) } },
        dismissButton = { Button(onClick = onDismiss) { Text(stringResource(id = R.string.cancel)) } }
    )
}