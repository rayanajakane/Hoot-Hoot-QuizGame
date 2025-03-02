package com.example.polyquiz

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import com.example.polyquiz.auth.domain.AuthViewModel
import com.example.polyquiz.match.domain.JoinMatchService
import com.example.polyquiz.match.domain.MatchRoomService
import com.example.polyquiz.match.domain.TimeService

@Composable
fun JoinGameDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onJoin: (String) -> Unit,
    authViewModel: AuthViewModel,
    navigateToMatchRoom: () -> Unit


) {
    var room by remember { mutableStateOf("") }
    val username by remember { mutableStateOf(authViewModel.getUsername() )}

    fun submitCode(matchRoomCode: String) {
        JoinMatchService.matchRoomCode = "";
        JoinMatchService.validateMatchRoomCode(
            matchRoomCode,
            onSuccess = {
                JoinMatchService.matchRoomCode = matchRoomCode
                JoinMatchService.validateUsername(username)
            },
            onError = { errorMessage ->
                println("Error: $errorMessage")
                JoinMatchService.matchRoomCode = ""
            }
        )
    }



    if (isOpen) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Joindre une partie") },
            text = {
                Column {
                    Text("Code d'accès :")
                    BasicTextField(
                        value = room,
                        onValueChange = { room = it },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { }
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (room.isNotBlank()) {
                        submitCode(room)
                        TimeService.handleTimer()
                        navigateToMatchRoom()
                        room = ""
                        onJoin(room)

                    }
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    room = ""
                    onDismiss()
                }) {
                    Text("Annuler", color = Color.Gray)
                }
            }
        )
    }
}
