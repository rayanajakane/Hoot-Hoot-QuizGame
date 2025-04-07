package com.example.polyquiz.match.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.res.stringResource
import com.example.polyquiz.R
import com.example.polyquiz.match.domain.MatchRoomService
import com.example.polyquiz.match.domain.PartyConfig

@Composable
fun PartyConfigDialog(
    initialPartyConfig: PartyConfig,
    onConfirm: (PartyConfig) -> Unit,
    onCancel: () -> Unit
) {
    var partyConfig by remember { mutableStateOf(initialPartyConfig.copy()) }
    var feeText by remember { mutableStateOf(partyConfig.entryFeeAmount?.toString() ?: "") }

    val feeValue = feeText.toFloatOrNull() ?: -1f
    val isValid = if (partyConfig.isEntryFeeRequired) feeValue >= 0f else true

    AlertDialog(
        onDismissRequest = { },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(R.string.match_config))
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.between_friends))
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(
                        checked = partyConfig.isFriendsOnly,
                        onCheckedChange = { partyConfig = partyConfig.copy(isFriendsOnly = it) }
                    )
                }
                if (partyConfig.isFriendsOnly) {
                    Text(
                        text = stringResource(R.string.only_friends),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.with_fee))
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(
                        checked = partyConfig.isEntryFeeRequired,
                        onCheckedChange = { partyConfig = partyConfig.copy(isEntryFeeRequired = it) }
                    )
                }
                if (partyConfig.isEntryFeeRequired) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = feeText,
                        onValueChange = { feeText = it },
                        label = { Text(stringResource(R.string.entry_fee)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    Text(
                        text = "Les joueurs devront payer ce montant pour rejoindre la partie.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Jouer en Mode Tricheur")
                    Spacer(modifier = Modifier.weight(1f))

                    Switch(
                        checked = partyConfig.isCheaterMode,
                        onCheckedChange = { partyConfig = partyConfig.copy(isCheaterMode = it) },

                    )
                    MatchRoomService.isCheaterMode = partyConfig.isCheaterMode
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(partyConfig.copy(entryFeeAmount = feeValue))
                },
                enabled = isValid,
                shape = RoundedCornerShape(3.dp)
            ) {
                Text(text = stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}
