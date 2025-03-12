package com.example.polyquiz.match.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.polyquiz.match.domain.MatchRoomService
import com.example.polyquiz.match.domain.Player

@Composable
fun ResultsPage(
    matchRoomService: MatchRoomService,
    players: List<Player>,
    modifier: Modifier = Modifier,
    extraContent: @Composable () -> Unit = {}
) {
    val username = matchRoomService.getUsername()

    var sortBy by remember { mutableStateOf("score") }
    var sortOrder by remember { mutableStateOf("descending") }

//    val sortedPlayers = remember(players, sortBy, sortOrder) {
//        players.sortedWith(
//            when (sortBy) {
//                "name" -> compareBy { it.username }
//                "score" -> compareBy { it.score as Comparable<*> }
//                "state" -> compareBy { it.state }
//                else -> compareBy<Player> { it.score as Comparable<*> }
//            }.let { comparator ->
//                if (sortOrder == "descending") comparator.reversed() else comparator
//            }
//        )
//    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(8.dp)
            .background(Color.White)
    ) {
        Text(text = "Résultats", fontSize = 24.sp, modifier = Modifier.padding(8.dp))

        if (username == "Organisateur") {
            SortOptions(sortBy, sortOrder, onSortChange = { sortBy = it }, onOrderChange = { sortOrder = it })
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(players) { player ->
                PlayerCard(player)
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
fun PlayerCard(player: Player) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color.White),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = player.username,
            fontSize = 16.sp,
            color = Color.Black
        )
        Column(horizontalAlignment = Alignment.End) {
            Text(text = "${player.score} pts", fontSize = 14.sp)
            Text(text = "(${player.bonusCount}✨)", fontSize = 12.sp)
        }
    }
}

