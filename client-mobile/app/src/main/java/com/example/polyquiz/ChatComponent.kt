package com.example.polyquiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.polyquiz.constants.DisplayChatText
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale

@Composable
fun ChatComponent() {
    val username = "TODO"
    val messages = listOf(
        Message("Coucou!", "TODO", Date.from(Instant.now())),
        Message("Allo!", "Autre utilisateur", Date.from(Instant.now()))
    )
    var newMessageText by remember{ mutableStateOf(TextFieldValue("")) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(0.dp),
        modifier = Modifier.size(width = 300.dp, height = 1000.dp).fillMaxHeight()
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceAround
        ) {
            Text(text = username, fontSize = 30.sp, modifier = Modifier.padding(20.dp, 20.dp, 20.dp, 0.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f).padding(20.dp, 20.dp, 20.dp, 0.dp)
            ) {
                itemsIndexed(messages) { _: Int, message: Message ->
                    MessageContainer(message, username)
                }
            }
            // TODO: Add button icon
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = newMessageText,
                onValueChange = { newText -> newMessageText = newText },
                label = { Text(text = DisplayChatText.MESSAGE_LABEL.value) },
                singleLine = true,
                shape = RoundedCornerShape(0.dp),
                keyboardActions = KeyboardActions(onDone = {
                    // TODO: Send message
                    newMessageText = newMessageText.copy("")
                }),
                trailingIcon = {
                    val image = Icons.AutoMirrored.Filled.Send;
                    IconButton(onClick = {
                        // TODO: Send message
                        newMessageText = newMessageText.copy("")
                    }) {
                        Icon(imageVector = image, "send")
                    }
                }
            )
        }
    }
}

@Composable
fun MessageContainer(message: Message, username: String) {
    val containerWidth = 225.dp
    val containerAlignment: Alignment.Horizontal
    val containerCorner: RoundedCornerShape
    val containerColor: Color
    if (message.author != username) {
        containerColor = MaterialTheme.colorScheme.surfaceBright
        containerAlignment = Alignment.Start
        containerCorner = RoundedCornerShape(topStart=10.dp, topEnd=10.dp, bottomEnd=10.dp, bottomStart=0.dp)
    } else {
        containerColor = MaterialTheme.colorScheme.primary
        containerAlignment = Alignment.End
        containerCorner = RoundedCornerShape(topStart=10.dp, topEnd=10.dp, bottomEnd=0.dp, bottomStart=10.dp)
    }
    Column(
        horizontalAlignment = containerAlignment,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.width(containerWidth)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.width(containerWidth)
            ) {
                Text(text = message.author)
                Text(text = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).format(message.date).toString())
            }
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = containerColor
                ),
                shape = containerCorner,
                modifier = Modifier.width(containerWidth)
            ) {
                // TODO: Change background color based on author (if same as current user or not)
                Text(text = message.text, modifier = Modifier.padding(10.dp))
            }
        }
    }
}
