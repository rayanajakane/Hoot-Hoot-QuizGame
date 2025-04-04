package com.example.polyquiz.auth.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFrom
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.polyquiz.R
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

    fun regenerate() {
        UsernameSuggestionService.getUsernameSuggestions();
        usernameSuggestions = UsernameSuggestionService.usernames;
        selectedUsername = "";
    }

    AlertDialog(
        shape = RoundedCornerShape(3.dp),
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.suggest_names)) },
        text = {
            Column {
                if (usernameSuggestions.isNotEmpty()) {
                    usernameSuggestions.forEach { username ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = selectedUsername == username,
                                onClick = { selectedUsername = username },
                            )
                            Text(text = username)
                        }
                    }
                    Button(onClick = { regenerate() }, shape = RoundedCornerShape(3.dp)) {
                        Text(stringResource(R.string.regen))
                    }
                }
            }
        },


        confirmButton = {
            Button(
                onClick = {
                    if (selectedUsername.isNotBlank()) {
                        onUsernameSelected(selectedUsername)
                    }
                },
                shape = RoundedCornerShape(3.dp)
            ) {
                Text(stringResource(R.string.confirm))
            }
        },

        dismissButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(3.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceBright,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(stringResource(R.string.cancel))
            }
        },


        )

}
