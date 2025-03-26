package com.example.polyquiz.match.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import com.example.polyquiz.match.domain.PartyConfig

@Composable
fun PartyConfigDialog(
    initialPartyConfig: PartyConfig,
    onConfirm: (PartyConfig) -> Unit,
    onCancel: () -> Unit
) {
    var partyConfig by remember { mutableStateOf(initialPartyConfig.copy()) }

    AlertDialog(
        onDismissRequest = {  },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Configuration de la partie")
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Partie entre amis uniquement")
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(
                        checked = partyConfig.isFriendsOnly,
                        onCheckedChange = { partyConfig = partyConfig.copy(isFriendsOnly = it) }
                    )
                }
                if (partyConfig.isFriendsOnly) {
                    Text(
                        text = "Seuls vos amis pourront rejoindre cette partie.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Partie payante")
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(
                        checked = partyConfig.isEntryFeeRequired,
                        onCheckedChange = { partyConfig = partyConfig.copy(isEntryFeeRequired = it) }
                    )
                }
                if (partyConfig.isEntryFeeRequired) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = partyConfig.entryFeeAmount.toString(),
                        onValueChange = { value ->
                            val intValue = value.toIntOrNull() ?: 0
                            partyConfig = partyConfig.copy(entryFeeAmount = intValue)
                        },
                        label = { Text("Frais d'entrée") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Les joueurs devront payer ce montant pour rejoindre la partie.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(partyConfig) }) {
                Text(text = "Confirmer")
            }
        },
        dismissButton = {
            TextButton(onClick = { onCancel() }) {
                Text(text = "Annuler")
            }
        }
    )
}
