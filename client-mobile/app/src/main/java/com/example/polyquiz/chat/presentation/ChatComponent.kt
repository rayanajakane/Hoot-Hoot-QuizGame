package com.example.polyquiz.chat.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.example.polyquiz.auth.domain.AuthViewModel
import com.example.polyquiz.chat.domain.ChatService
import com.example.polyquiz.chat.domain.Message
import com.example.polyquiz.constants.SIZE_CONSTANTS
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ChatComponent(modifier: Modifier, authViewModel: AuthViewModel) {
    val username by remember { mutableStateOf(authViewModel.getUsername() )}
    val userId by remember { mutableStateOf(authViewModel.getUserId() )}
    val messages by ChatService.messages.observeAsState()
    var newMessageText by remember{ mutableStateOf("") }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(0.dp),
        modifier = Modifier.size(width = 300.dp, height = 1000.dp).fillMaxHeight().imePadding()
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceAround,
        ) {
            Text(text = username, fontSize = 30.sp, fontWeight = FontWeight(800), modifier = Modifier.padding(20.dp, 20.dp, 20.dp, 0.dp))

            // REFERENCE: https://youtu.be/P3xQdINdrWY
            // To handle the situation where there would be no message to display.
            messages?.let {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f).padding(20.dp, 20.dp, 20.dp, 0.dp),
                ) {
                    itemsIndexed(it) { _: Int, message: Message ->
                        MessageContainer(message, userId)
                    }
                }
            } ?: LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f).padding(20.dp, 20.dp, 20.dp, 0.dp)
            ) {

            }
            TextField(
                modifier = Modifier.fillMaxWidth().padding(0.dp, 10.dp, 0.dp, 70.dp),
                value = newMessageText,
                onValueChange = { if (it.length <= SIZE_CONSTANTS.MAX_INPUT_LENGTH) newMessageText = it },
                label = { Text(text = DisplayChatText.MESSAGE_LABEL.value) },
                singleLine = true,
                shape = RoundedCornerShape(0.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    // TODO: Change to actual user avatar
                    ChatService.sendMessage(newMessageText, userId, username, "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcT18iwsdCCbBfpa50-5BmNa_m_BX087_x1oWQ&s")
                    newMessageText = ""
                }),
                trailingIcon = {
                    val image = Icons.AutoMirrored.Filled.Send;
                    IconButton(onClick = {
                        // TODO: Change to actual user avatar
                        ChatService.sendMessage(newMessageText, userId, username, "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcT18iwsdCCbBfpa50-5BmNa_m_BX087_x1oWQ&s")
                        newMessageText = ""
                    }) {
                        Icon(imageVector = image, "send")
                    }
                }
            )
        }
    }
}

@Composable
fun MessageContainer(message: Message, currentUserId: String) {
    val containerWidth = 225.dp
    val containerAlignment: Alignment.Horizontal
    val containerCorner: RoundedCornerShape
    val containerColor: Color

    if (message.authorId != currentUserId) {
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
                Text(text = message.authorUsername, fontWeight = FontWeight(600))
                Text(text = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).format(message.date).toString())
            }
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = containerColor
                ),
                shape = containerCorner,
                modifier = Modifier.width(containerWidth)
            ) {
                Text(text = message.text, modifier = Modifier.padding(10.dp))
            }
        }
    }
}
