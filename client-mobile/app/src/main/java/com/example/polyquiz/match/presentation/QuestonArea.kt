package com.example.polyquiz.match.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.polyquiz.auth.domain.AuthViewModel
import com.example.polyquiz.match.domain.MatchRoomService
import com.example.polyquiz.match.domain.TimeService

//@Preview(showBackground = true)
//@Composable
//fun PreviewQuizArea(){
//    QuestionArea()
//}
@Composable
fun QuestionArea(
    modifier: Modifier = Modifier,
    navigateToHome: () -> Unit,
    timeService: TimeService = TimeService,
    authViewModel: AuthViewModel,
    matchRoomService: MatchRoomService
) {
    var selectedOptions by remember { mutableStateOf(setOf<String>()) }
    val authState by authViewModel.authState.observeAsState()
    var room by remember { mutableStateOf("") }

    TimerComponent(
        modifier = Modifier.fillMaxWidth(),
        timeService = timeService,
    )
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Quel est le meilleur cours ?",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.background(Color.Blue, shape = RoundedCornerShape(8.dp)).padding(16.dp),
            color = Color.White
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("SCORE : 0", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChoiceButton("1. LOG3900", selectedOptions, onSelect = { selectedOptions = toggleSelection(selectedOptions, it) }, modifier = Modifier.weight(1f).height(80.dp))
                ChoiceButton("2. INF1040", selectedOptions, onSelect = { selectedOptions = toggleSelection(selectedOptions, it) }, modifier = Modifier.weight(1f).height(80.dp))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChoiceButton("3. LOG1810", selectedOptions, onSelect = { selectedOptions = toggleSelection(selectedOptions, it) }, modifier = Modifier.weight(1f).height(80.dp))
                ChoiceButton("4. INF2205", selectedOptions, onSelect = { selectedOptions = toggleSelection(selectedOptions, it) }, modifier = Modifier.weight(1f).height(80.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { navigateToHome() },
            modifier = Modifier.fillMaxWidth(0.5f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Page d'accueil")
        }

        TextField(
            value = room,
            onValueChange = { room = it },
            label = { Text("Room ID") },
            modifier = Modifier.fillMaxWidth(0.8f).padding(8.dp)
        )

        Button(
            onClick = { matchRoomService.joinRoom(room, "sami") },
        modifier = Modifier.fillMaxWidth(0.5f),
        shape = RoundedCornerShape(8.dp)
        ) {
        Text("join")
        }
        Button(
            onClick = { timeService.handleTimer() },
            modifier = Modifier.fillMaxWidth(0.5f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("allumer minuterie")
        }
    }
}

@Composable
fun ChoiceButton(text: String, selectedOptions: Set<String>, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = { onSelect(text) },
        modifier = modifier.padding(4.dp).height(80.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selectedOptions.contains(text)) Color.Gray else Color.LightGray
        )
    ) {
        Text(text, fontSize = 18.sp)
    }
}

fun toggleSelection(selectedOptions: Set<String>, option: String): Set<String> {
    return if (selectedOptions.contains(option)) {
        selectedOptions - option
    } else {
        selectedOptions + option
    }
}
