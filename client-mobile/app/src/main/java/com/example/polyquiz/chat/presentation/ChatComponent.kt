package com.example.polyquiz.chat.presentation

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.window.PopupPositionProvider
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.polyquiz.R
import com.example.polyquiz.auth.domain.AuthViewModel
import com.example.polyquiz.auth.domain.UserIdName
import com.example.polyquiz.chat.domain.ChatChannel
import com.example.polyquiz.chat.domain.ChatEmoji
import com.example.polyquiz.chat.domain.ChatService
import com.example.polyquiz.chat.domain.Message
import com.example.polyquiz.constants.PresetAvatar
import com.example.polyquiz.constants.SIZE_CONSTANTS
import com.example.polyquiz.match.domain.MatchContextService
import com.example.polyquiz.match.domain.MatchRoomService
import com.example.polyquiz.shop.domain.WallpaperService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ChatComponent(modifier: Modifier, authViewModel: AuthViewModel) {
    val username by authViewModel.username.collectAsState()
    val userId by remember { mutableStateOf(authViewModel.getUserId()) }
    val avatarURL by authViewModel.avatarURL.collectAsState()
    val roomCode by MatchRoomService.matchRoomCode.collectAsState()
    val currentWallpaper by WallpaperService.currentWallpaper.collectAsState()
    var selectedChat by remember {
        mutableStateOf(if (roomCode.isNotEmpty()) "Match" else "General")
    }

    LaunchedEffect(selectedChat) {
        if (selectedChat == "Match") {
            ChatService.channel = ChatChannel.ROOM.value
        } else {
            ChatService.channel = ChatChannel.GENERAL.value
        }
    }

    LaunchedEffect(roomCode) {
        if (roomCode.isNotEmpty()) {
            ChatService.channel = ChatChannel.ROOM.value
            selectedChat = "Match"
        } else {
            ChatService.channel = ChatChannel.GENERAL.value
            selectedChat = "General"
        }
    }

    val listState = rememberLazyListState()
    val messages = when (selectedChat) {
        "Match" -> ChatService.matchRoomMessages.observeAsState().value
        else -> ChatService.generalMessages.observeAsState().value
    }
    var newMessageText by remember { mutableStateOf("") }

    LaunchedEffect(messages?.size) {
        messages?.let { list ->
            if (list.isNotEmpty()) {
                listState.scrollToItem(list.size - 1)
            }
        }
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(0.dp),
        modifier = Modifier
            .size(width = 300.dp, height = 1000.dp)
            .fillMaxHeight()
            .imePadding()
            .statusBarsPadding()
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            TruncatedText(
                text = username,
                fontSize = 30.sp,
                maxChars = 20,
                FontWeight(800),
                Modifier.padding(20.dp, 20.dp, 20.dp, 0.dp)
            )
            ChatSelectionMenu(selectedChat) { newChat -> selectedChat = newChat }
            Spacer(modifier = Modifier.height(8.dp))
            // REFERENCE: https://youtu.be/P3xQdINdrWY
            // To handle the situation where there would be no message to display.
            messages?.let {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    state = listState,
                    modifier = Modifier
                        .weight(1f),
                ) {
                    itemsIndexed(it) { _: Int, message: Message ->
                        MessageContainer(message, userId, username)
                    }
                }
            } ?: LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .padding(20.dp, 20.dp, 20.dp, 0.dp)
            ) {

            }
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp, 10.dp, 0.dp, 70.dp),
                value = newMessageText,
                onValueChange = {
                    if (it.length <= SIZE_CONSTANTS.MAX_INPUT_LENGTH) newMessageText = it
                },
                label = { Text(text = stringResource(R.string.message_label)) },
                singleLine = true,
                shape = RoundedCornerShape(0.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    if (selectedChat == "General") {
                        ChatService.sendMessage(
                            newMessageText,
                            userId,
                            username,
                            avatarURL,
                            null
                        )
                    } else {
                        ChatService.sendMessage(
                            newMessageText,
                            userId,
                            username,
                            avatarURL,
                            MatchRoomService.getRoomCode()
                        )
                    }
                    newMessageText = ""
                }),
                trailingIcon = {
                    val image = Icons.AutoMirrored.Filled.Send;
                    IconButton(onClick = {
                        if (selectedChat == "General") {
                            ChatService.sendMessage(
                                newMessageText,
                                userId,
                                username,
                                avatarURL,
                                null
                            )
                        } else {
                            ChatService.sendMessage(
                                newMessageText,
                                userId,
                                username,
                                avatarURL,
                                MatchRoomService.getRoomCode()
                            )
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
fun MessageContainer(message: Message, currentUserId: String, username: String) {
    val containerWidth = 225.dp
    val containerAlignment: Alignment.Horizontal
    val containerCorner: RoundedCornerShape
    val containerColor: Color

    if (message.authorId != currentUserId) {
        containerColor = MaterialTheme.colorScheme.surfaceBright
        containerAlignment = Alignment.Start
        containerCorner = RoundedCornerShape(
            topStart = 10.dp,
            topEnd = 10.dp,
            bottomEnd = 10.dp,
            bottomStart = 0.dp
        )
    } else {
        containerColor = MaterialTheme.colorScheme.primary
        containerAlignment = Alignment.End
        containerCorner = RoundedCornerShape(
            topStart = 10.dp,
            topEnd = 10.dp,
            bottomEnd = 0.dp,
            bottomStart = 10.dp
        )
    }
    Row(modifier = Modifier.fillMaxSize()) {
        // Avatar box if not author
        if (message.authorId != currentUserId) {
            Box(modifier = Modifier.align(Alignment.Bottom).padding(bottom = 20.dp, start = 10.dp)) {
                AvatarImage(message.photoUrl)
            }
        }
        // Message box
        Box(modifier = Modifier.weight(1f)) {
            Column(modifier = Modifier.align(Alignment.Center)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.width(containerWidth)
                ) {
                    TruncatedText(
                        message.authorUsername,
                        fontSize = 16.sp,
                        maxChars = 11,
                        FontWeight(600),
                        Modifier
                    )
                    Text(
                        text = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).format(message.date)
                            .toString()
                    )
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = containerColor),
                    shape = containerCorner,
                    modifier = Modifier.width(containerWidth)
                ) {
                    Text(text = message.text, modifier = Modifier.padding(10.dp))
                }
                ReactionsRow(
                    message,
                    currentUserId,
                    username,
                    MatchRoomService.getRoomCode()
                )
            }
        }
        // Avatar box if author
        if (message.authorId == currentUserId) {
            Box(modifier = Modifier.align(Alignment.Bottom).padding(bottom = 20.dp, end = 10.dp)) {
                AvatarImage(message.photoUrl)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatSelectionMenu(selectedChat: String, onChatSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("General", "Match")
    var matchContext by remember { mutableStateOf(MatchContextService.context) }

    LaunchedEffect(Unit, MatchContextService.context) {
        matchContext = MatchContextService.context
    }

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
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { selectionOption ->
                val isSelected = selectedChat == selectionOption
                val roomCode by MatchRoomService.matchRoomCode.collectAsState()
                MatchRoomService.getRoomCode().isNotEmpty()

                // Enable Match only if the room is available, but still show it if selected
                val isOptionDisabled =
                    selectionOption == "Match" && roomCode.isEmpty() && !isSelected
                if (!isOptionDisabled) {
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = selectionOption,
                                    color = if (isOptionDisabled) Color.Gray else LocalContentColor.current
                                )
                                if (isSelected) {
                                    Spacer(Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = LocalContentColor.current
                                    )
                                }
                            }
                        },
                        onClick = {

                            onChatSelected(selectionOption)
                            expanded = false

                            ChatService.channel = selectionOption
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }

            }
        }
    }
}


@Composable
fun AvatarImage(photoUrl: String?) {
    AsyncImage(
        model = photoUrl,
        contentDescription = "Avatar",
        modifier = Modifier
            .size(50.dp)
            .clip(CircleShape),
        contentScale = ContentScale.Crop,
        placeholder = rememberAsyncImagePainter(model = PresetAvatar.DEFAULT.value)
    )
}

@Composable
fun ReactionsRow(
    message: Message,
    userId: String,
    username: String,
    roomCode: String?,
) {
    Row(
        modifier = Modifier.padding(top = 1.dp),
        horizontalArrangement = Arrangement.Absolute.Left,
    ) {
        ReactionButton("👍", message.userLikes.size, users = message.userLikes) {
            ChatService.reactToMessage(
                message.id,
                ChatEmoji.LIKE,
                userId,
                username,
                if (roomCode.isNullOrEmpty()) null else roomCode
            )
        }
        ReactionButton("❤️", message.userLoves.size, users = message.userLoves) {
            ChatService.reactToMessage(
                message.id,
                ChatEmoji.LOVE,
                userId,
                username,
                if (roomCode.isNullOrEmpty()) null else roomCode
            )
        }
        ReactionButton("👎", message.userDislikes.size, users = message.userDislikes) {
            ChatService.reactToMessage(
                message.id,
                ChatEmoji.DISLIKE,
                userId,
                username,
                if (roomCode.isNullOrEmpty()) null else roomCode
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReactionButton(emoji: String, count: Int, users: List<UserIdName>, onClick: () -> Unit) {
    var isClicked by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val viewConfig = LocalViewConfiguration.current

    var usernameList = users.map { it.name }
    LaunchedEffect(users) {
        usernameList = users.map { it.name }
    }

    LaunchedEffect(interactionSource) {
        var isLongClick = false

        interactionSource.interactions.collectLatest { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    isLongClick = false
                    delay(viewConfig.longPressTimeoutMillis)
                    isLongClick = true
                    // TODO users name list
                }

                is PressInteraction.Release -> {
                    if (!isLongClick) {
                        // TODO
                        Log.d("Emoji", "Not long click")
                        isClicked = !isClicked
                        onClick()
                    }
                }
            }
        }
    }

    TooltipBox(
        tooltip = { PlainTooltip { Text(usernameList.joinToString()) } },
        positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
        state = rememberTooltipState()
    ) {
        Button(
            onClick = {
//            isClicked = !isClicked
//            onClick()
            },
            interactionSource = interactionSource,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isClicked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            modifier = Modifier
                .heightIn(min = 32.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onLongPress = {
                        // TODO : Show list of users
                        Log.d("Emoji", "Users, $users")
                    })
                }
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = emoji,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isClicked) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.offset(y = (-4).dp)
                )
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isClicked) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }

}

@Composable
fun TruncatedText(
    text: String,
    fontSize: TextUnit,
    maxChars: Int,
    fontWeight: FontWeight,
    modifier: Modifier,
    color : Color = MaterialTheme.colorScheme.onSurface,
    style: TextStyle = TextStyle()

) {
    val truncatedText = if (text.length > maxChars) {
        text.take(maxChars) + "..."
    } else {
        text
    }

    Text(
        text = truncatedText,
        fontSize = fontSize,
        fontWeight = fontWeight,
        modifier = modifier,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        color = color,
        style = style
    )
}
