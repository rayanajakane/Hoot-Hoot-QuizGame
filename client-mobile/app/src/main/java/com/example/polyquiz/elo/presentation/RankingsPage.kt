package com.example.polyquiz.elo.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.polyquiz.R
import com.example.polyquiz.auth.domain.AuthViewModel
import com.example.polyquiz.chat.presentation.ChatComponent
import com.example.polyquiz.constants.FriendsDisplayText
import com.example.polyquiz.elo.domain.EloService
import kotlin.math.roundToInt

@Composable
fun RankingsPage(
    modifier: Modifier,
    navigateToHome: () -> Unit,
    authViewModel: AuthViewModel,
) {
    val rankings by EloService._rankings.observeAsState(emptyList())
    val currentRating by EloService.currentRating.observeAsState(0)

    fun quitRankingsPage() {
        EloService.stopListeningForEloEvents()
        navigateToHome()
    }
    LaunchedEffect(Unit) {
        EloService.listenForEloEvents()
        EloService.getElo(authViewModel.getUserId())
        EloService.getRankings()
    }

    Row() {
    ChatComponent(modifier = modifier, authViewModel = authViewModel)



    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Votre elo: $currentRating")

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = stringResource(R.string.rank), modifier = Modifier.weight(1f))
            Text(text = stringResource(R.string.username), modifier = Modifier.weight(2f))
            Text(text = stringResource(R.string.elo), modifier = Modifier.weight(1f))
            Button(onClick = {quitRankingsPage()}) {
                Text(text = stringResource(R.string.home_page))
            }
        }
        HorizontalDivider()

        LazyColumn {
            itemsIndexed(rankings) { index, ranking ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "${index + 1}", modifier = Modifier.weight(1f))
                    Text(text = ranking.username, modifier = Modifier.weight(2f))
                    Text(text = ranking.rating.roundToInt().toString(), modifier = Modifier.weight(1f))
                }
            }
        }


    }


    }
}
