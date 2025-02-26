package com.example.polyquiz.match.presentation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp

@Composable
fun LongAnswerArea(
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = "",
        onValueChange = { },
        label = { Text("Réponse") },
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(8.dp),
        maxLines = 5
    )
}
