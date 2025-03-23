package com.example.polyquiz.money.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun DonationDialog(
    onDismiss: () -> Unit,
    onDonate: (Int) -> Unit
) {
    var donationAmount by remember { mutableStateOf("") }
    val amountValid = donationAmount.toIntOrNull()?.let { it > 0 } ?: false

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Enter Donation Amount") },
        text = {
            Column {
                OutlinedTextField(
                    value = donationAmount,
                    onValueChange = { donationAmount = it },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    donationAmount.toIntOrNull()?.let { onDonate(it) }
                },
                enabled = amountValid
            ) {
                Text("Donate")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
