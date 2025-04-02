package com.example.polyquiz.pages.presentation

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.fragment.app.DialogFragment
import com.example.polyquiz.R
import com.example.polyquiz.constants.VotingData
import com.example.polyquiz.match.domain.MatchRoomService
import com.example.polyquiz.match.domain.Player
import androidx.compose.foundation.layout.*
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

@Composable
fun VotingDialogComponent(
    players: List<Player>,
    matchRoomService: MatchRoomService,
    onVote: (VotingData) -> Unit,
    onClose: () -> Unit
) {
    var selectedPlayer by remember { mutableStateOf<String?>(null) }
    var voteCounts by remember { mutableStateOf(VotingData("", 0, mutableListOf())) }
    var totalVotes by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = { onClose() },
        title = { Text("Votez pour le tricheur") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                players.forEach { player ->
                    if (player.username != matchRoomService.retrieveUsername()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(8.dp)
                        ) {
                            RadioButton(
                                selected = selectedPlayer == player.username,
                                onClick = { selectedPlayer = player.username }
                            )
                            Text(text = player.username)
                            Text(text = " - Votes: ${voteCounts.numberOfVotes}", modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedPlayer?.let {
                        voteCounts.username = it
                        voteCounts.numberOfVotes += 1;
                        voteCounts.usersWhoVoted.add(matchRoomService.retrieveUsername())
                        totalVotes++
                        onVote(voteCounts)
                    }
                    onClose()
                }
            ) {
                Text("Soumettre")
            }
        },
        dismissButton = {
            TextButton(onClick = { onClose() }) {
                Text("Annuler")
            }
        }
    )


}
