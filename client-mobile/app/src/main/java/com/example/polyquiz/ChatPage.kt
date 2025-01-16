package com.example.polyquiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.polyquiz.constants.DisplayAuthenticationText

@Composable
fun ChatPage(modifier: Modifier) {
    Row (
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxSize()
    ){
        ChatComponent()
        ElevatedButton(
            onClick = {

            },
            modifier = Modifier.padding(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceBright,
                contentColor = MaterialTheme.colorScheme.onSurface)
        ) {
            Text(text = DisplayAuthenticationText.LOGOUT.value)
        }
    }
}
