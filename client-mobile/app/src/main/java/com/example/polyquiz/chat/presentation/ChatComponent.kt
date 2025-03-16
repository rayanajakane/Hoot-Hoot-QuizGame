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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.example.polyquiz.R
import com.example.polyquiz.auth.domain.AuthViewModel
import com.example.polyquiz.chat.domain.ChatService
import com.example.polyquiz.chat.domain.Message
import com.example.polyquiz.constants.MatchContext
import com.example.polyquiz.constants.PresetAvatar
import com.example.polyquiz.constants.SIZE_CONSTANTS
import com.example.polyquiz.match.domain.MatchRoomService
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ChatComponent(modifier: Modifier, authViewModel: AuthViewModel) {
    val username by remember { mutableStateOf(authViewModel.getUsername() )}
    val userId by remember { mutableStateOf(authViewModel.getUserId() )}
    var selectedChat by remember { mutableStateOf("General") }
    val messages = when (selectedChat) {
        "Match" -> ChatService.matchRoomMessages.observeAsState().value
        else -> ChatService.generalMessages.observeAsState().value
    }
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
            ChatSelectionMenu(selectedChat) { newChat -> selectedChat = newChat }
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
                label = { Text(text = stringResource(R.string.message_label)) },
                singleLine = true,
                shape = RoundedCornerShape(0.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    // TODO: Change to actual user avatar
                    if (selectedChat == "General") {
                        ChatService.sendMessage(newMessageText, userId, username, PresetAvatar.DEFAULT.value, null)
                    }
                    else {
                        ChatService.sendMessage(newMessageText, userId, username, PresetAvatar.DEFAULT.value, MatchRoomService.getRoomCode())
                    }
                    newMessageText = ""
                }),
                trailingIcon = {
                    val image = Icons.AutoMirrored.Filled.Send;
                    IconButton(onClick = {
                        // TODO: Change to actual user avatar
                        if (selectedChat == "General") {
                            ChatService.sendMessage(newMessageText, userId, username, PresetAvatar.DEFAULT.value, null)
                        }
                        else {
                            ChatService.sendMessage(newMessageText, userId, username, PresetAvatar.DEFAULT.value, MatchRoomService.getRoomCode())
                        }
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatSelectionMenu(selectedChat: String, onChatSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
//    var selectedOptionText by remember { mutableStateOf("General") }
    val options = listOf("General", "Match")
    var matchContext by remember { mutableStateOf(MatchContextService.getContext()) }

    LaunchedEffect(Unit, MatchContextService.getContext()) {
        matchContext = MatchContextService.getContext()
    }

    val isMatchDisabled by remember(matchContext) {
        derivedStateOf { matchContext == MatchContext.PLAYERVIEW }
    }

    val isMatchNull by remember(matchContext) {
        derivedStateOf { matchContext == MatchContext.Null }
    }
//    val isMatchDisabled = MatchContextService.getContext() == MatchContext.PLAYERVIEW
//    val isMatchNull = MatchContextService.getContext() == MatchContext.Null

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        TextField(
            readOnly = true,
            value = selectedChat,
            onValueChange = { },
            label = { Text("Option") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { selectionOption ->
                val isOptionDisabled = (isMatchNull || selectedChat == "Match")//(isMatchDisabled && selectedChat == "Match") || (isMatchNull && selectedChat == "Match")
                DropdownMenuItem(
                    text = {
                        Text(
                            text = selectionOption,
                            color = if (isOptionDisabled) Color.Gray else LocalContentColor.current
                        )
                    },
                    onClick = {
                        if (!isOptionDisabled) {
                            onChatSelected(selectionOption)
                            expanded = false
                        }
                    },
                    enabled = !isOptionDisabled,
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

