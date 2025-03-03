package com.example.polyquiz

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonDefaults.shape
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.polyquiz.Game
import com.example.polyquiz.GameService
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight


@Composable
fun GameList(modifier: Modifier) {
    val gameService = GameService()
    var games by remember { mutableStateOf<List<Game>>(emptyList()) }
    var selectedGame by remember { mutableStateOf<Game?>(null) }

    LaunchedEffect(Unit) {
        gameService.getGames(
            onSuccess = { fetchedGames ->
                val gson = Gson()
                val json = gson.toJson(fetchedGames)
                val listType = object : TypeToken<List<Game>>() {}.type
                games = gson.fromJson(json, listType)
            },
            onError = { errorMessage -> println("Error: $errorMessage") }
        )

    }


    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Liste des jeux", modifier = Modifier.padding(8.dp), fontWeight = FontWeight.Bold)
            if (games.isEmpty()) {
                Text("Aucun jeu disponible", modifier = Modifier.padding(8.dp), fontWeight = FontWeight.Bold)
            } else {
                games.forEach { game ->
                    ElevatedButton(
                        onClick = {selectedGame = game },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(1.dp),
                        shape = RoundedCornerShape(16.dp)
                    )
                    {
                        Text(text = game.title)
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
        ) {
            if (selectedGame != null) {
                Text("Détails du jeu:  ${selectedGame!!.title}", modifier = Modifier.padding(8.dp), fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "Description: ",
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${selectedGame!!.description}",
                    )
                }

                Row(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "Durée: ",
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${selectedGame!!.duration} minutes",
                        fontWeight = FontWeight.Normal
                    )
                }

                Text("Questions :", modifier = Modifier.padding(8.dp), fontWeight = FontWeight.Bold)
                selectedGame!!.questions?.forEachIndexed { index, question ->
                    Text("${index + 1}. ${question.text}", modifier = Modifier.padding(8.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                Text("Sélectionner un jeu dans la liste des jeux", modifier = Modifier.padding(8.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
