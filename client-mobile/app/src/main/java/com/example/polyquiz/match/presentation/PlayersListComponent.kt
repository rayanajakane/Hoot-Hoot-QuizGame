package com.example.polyquiz.match.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.polyquiz.R
import com.example.polyquiz.constants.MatchContext
import com.example.polyquiz.constants.PresetAvatar
import com.example.polyquiz.match.domain.AnswerService
import com.example.polyquiz.match.domain.MatchContextService
import com.example.polyquiz.match.domain.MatchRoomService
import com.example.polyquiz.match.domain.Player
import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment

@Composable
fun PlayersListComponent(
    matchRoomService: MatchRoomService,
    context: MatchContextService,
    players: List<Player>,
    modifier: Modifier = Modifier,
    extraContent: @Composable () -> Unit = {}
) {
    val userId = matchRoomService.userId
    var sortBy by remember { mutableStateOf("score") }
    var sortOrder by remember { mutableStateOf("descending") }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(250.dp)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(8.dp)
    ) {
        Text(
            text = stringResource(R.string.players),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(8.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(players) { player ->
                PlayerCard(Modifier, player, player.photoUrl, context)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            extraContent()
        }
    }
}

@Composable
fun PlayerCard(
    modifier: Modifier,
    player: Player,
    url: String,
    context: MatchContextService,
    withAvatar: Boolean = false,
    inResultsPage: Boolean = false,
) {
    Card(
        shape = RoundedCornerShape(4.dp),
        colors = CardColors(
            containerColor = MaterialTheme.colorScheme.surfaceBright,
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceBright,
            disabledContentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp,
        ),
        modifier = modifier,

        ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.fillMaxWidth(0.4f), verticalAlignment = Alignment.CenterVertically) {
                if (withAvatar) {
                    AsyncImage(
                        model = url,
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        placeholder = rememberAsyncImagePainter(model = PresetAvatar.DEFAULT.value)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }


                    if (!player.isPlaying) {
                        Text(
                            text = player.username,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = TextStyle(textDecoration = TextDecoration.LineThrough)
                        )
                    } else {
                        TruncatedText(
                            text = player.username,
                            fontSize = 16.sp,
                            maxChars = 20,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier,
                        )
                    }
                    if (MatchRoomService.isCheaterMode && context.getContext() !== MatchContext.HOSTVIEW && MatchRoomService.isTimeToNavigateToResults && inResultsPage) {
                        if (player.id == MatchRoomService.cheaterPlayer.id) {
                            TruncatedText(
                                text = " \uD83D\uDE08",
                                fontSize = 16.sp,
                                maxChars = 20,
                                fontWeight = FontWeight.Normal,
                                modifier = Modifier,
                            )
                        }
                    }

                    if (MatchRoomService.isCheaterMode && context.getContext() === MatchContext.HOSTVIEW) {
                        if (player.id == MatchRoomService.cheaterPlayer.id) {
                            TruncatedText(
                                text = " \uD83D\uDE08",
                                fontSize = 16.sp,
                                maxChars = 20,
                                fontWeight = FontWeight.Normal,
                                modifier = Modifier,
                            )
                        }

                }

            }


            if (MatchRoomService.isCheaterMode && MatchRoomService.isTimeToNavigateToResults && inResultsPage) {
                Row(modifier = Modifier.fillMaxWidth(0.6f)) {

                    if (MatchRoomService.votesResults[player.username] == null) {
                        Text(
                            text = " Votes: ${0}",
                            fontSize = 14.sp
                        )
                    } else {
                        Text(
                            text = " Votes: ${MatchRoomService.votesResults[player.username]}",
                            fontSize = 14.sp
                        )
                    }
                }


            }
            Row(horizontalArrangement = Arrangement.End) {
                Text(text = "${player.score} pts", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(20.dp))
                Text(text = "${player.bonusCount} bonus✨", fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun ButtonGroup(
    options: List<Pair<String, String>>,
    selected: String,
    onSelected: (String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (value, label) ->
            Button(
                onClick = { onSelected(value) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selected == value) Color.Gray else Color.LightGray
                )
            ) {
                Text(text = label)
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
    modifier: Modifier
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
        overflow = TextOverflow.Ellipsis
    )
}

