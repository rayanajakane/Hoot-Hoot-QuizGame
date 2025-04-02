package com.example.polyquiz.auth.presentation

import androidx.benchmark.perfetto.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.polyquiz.auth.domain.UsernameSuggestionService
import androidx.compose.material3.Button
import androidx.compose.material3.RadioButton
import java.lang.reflect.Modifier

@Composable
fun UsernameSuggestionDialog(
    onDismiss: () -> Unit,
    onUsernameSelected: (String) -> Unit
) {

    var selectedUsername by remember { mutableStateOf("") }
    var usernameSuggestions by remember { mutableStateOf<List<String>>(emptyList()) }


    LaunchedEffect(Unit) {

        UsernameSuggestionService.getUsernameSuggestions()
        usernameSuggestions = UsernameSuggestionService.usernames
    }

    fun regenerate(){
        UsernameSuggestionService.getUsernameSuggestions();
        selectedUsername = "";
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Choose a Username") },
        text = {
            Column {
                if (usernameSuggestions.isNotEmpty()) {
                    usernameSuggestions.forEach { username ->
                        Row {
                            RadioButton(
                                selected = selectedUsername == username,
                                onClick = { selectedUsername = username }
                            )
                            Text(text = username)
                        }
                    }
                }


                //Spacer(modifier = Modifier.height(16.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedUsername.isNotBlank()) {
                        onUsernameSelected(selectedUsername)
                    }
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
