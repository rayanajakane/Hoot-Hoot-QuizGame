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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.polyquiz.R
import com.example.polyquiz.constants.PresetAvatar
import com.example.polyquiz.match.domain.MatchContextService
import com.example.polyquiz.match.domain.MatchRoomService
import com.example.polyquiz.match.domain.Player

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
    withAvatar: Boolean = false
) {
    Card(
        shape = RoundedCornerShape(3.dp),
        colors = CardColors(
            containerColor = MaterialTheme.colorScheme.surfaceBright,
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceBright,
            disabledContentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
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
            if(!player.isPlaying) {
                Text(
                    text = player.username,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = TextStyle(textDecoration = TextDecoration.LineThrough)
                )
            } else {
                Text(
                    text = player.username,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text(text = "${player.score} pts", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "(${player.bonusCount}✨)", fontSize = 12.sp)
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

