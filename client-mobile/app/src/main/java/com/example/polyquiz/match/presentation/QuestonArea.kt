package com.example.polyquiz.match.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import com.example.polyquiz.auth.domain.AuthViewModel
import com.example.polyquiz.constants.MatchContext
import com.example.polyquiz.constants.QuestionType
import com.example.polyquiz.match.domain.Choice
import com.example.polyquiz.match.domain.MatchContextService
import com.example.polyquiz.match.domain.MatchRoomService
import com.example.polyquiz.match.domain.TimeService

@Composable
fun QuestionArea(
    matchContextService: MatchContextService,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    navigateToHome: () -> Unit
) {
    val question = MatchRoomService.currentQuestion
    val questionText = question?.text ?: "Question inconnue"
    val questionPoints = question?.points ?: 0
    var room by remember { mutableStateOf("") }
    val username by remember { mutableStateOf(authViewModel.getUsername() )}
    val questionType = question?.type ?: QuestionType.MULTIPLE_CHOICE.value

    val score = 12
//    val timeRemaining = timeService.time

    val currentContext = matchContextService.getContext()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        TimerComponent(
            modifier = Modifier.fillMaxWidth(),
            timeService = TimeService,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(120.dp)
                .background(
                    color = Color(0xFF5469D4),
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = questionText,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$questionPoints points",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "SCORE : $score",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        when (questionType) {
            QuestionType.MULTIPLE_CHOICE.value -> {
                val mockChoices = listOf(
                    Choice(text = "LOG3900"),
                    Choice(text = "INF1040"),
                    Choice(text = "LOG1810"),
                    Choice(text = "INF2205")
                )
                MultipleChoiceArea(choices = mockChoices, modifier = Modifier.fillMaxWidth(0.8f))
            }

            QuestionType.LONG_ANSWER.value -> {
                LongAnswerArea(modifier = Modifier.fillMaxWidth(0.8f))
            }
        }

        if (currentContext == MatchContext.HOSTVIEW) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { }) {
                Text("QUESTION SUIVANTE")
            }
        }
        TextField(
            value = room,
            onValueChange = { room = it },
            label = { Text("Room ID") },
            modifier = Modifier.fillMaxWidth(0.8f).padding(8.dp)
        )
        Button(
            onClick = { navigateToHome() },
            modifier = Modifier.fillMaxWidth(0.5f),
            shape = RoundedCornerShape(8.dp)
        )
        {
            Text("Page d'accueil")
        }

        Button(
            onClick = { MatchRoomService.joinRoom(room, username); TimeService.handleTimer() },
            modifier = Modifier.fillMaxWidth(0.5f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("join")
        }
    }

}
