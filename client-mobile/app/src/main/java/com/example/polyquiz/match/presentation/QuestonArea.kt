package com.example.polyquiz.match.presentation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.polyquiz.match.domain.Player
import com.example.polyquiz.match.domain.TimeService

@Composable
fun QuestionArea(
    matchRoomService: MatchRoomService,
    timeService: TimeService,
    matchContextService: MatchContextService,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel
) {
    val question = matchRoomService.currentQuestion
    val questionText = question?.text ?: "Question inconnue"
    val questionPoints = question?.points ?: 0
    val questionType = question?.type ?: QuestionType.MULTIPLE_CHOICE.value

    val score = 12
    val timeRemaining = timeService.time
    val timerProgress = (timeService.computeTimerProgress() / 100f).coerceIn(0f, 1f)

    val currentContext = matchContextService.getContext()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier.size(100.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = timerProgress,
                strokeWidth = 8.dp,
                modifier = Modifier.size(100.dp)
            )
            Text(
                text = timeRemaining.toString(),
                style = MaterialTheme.typography.titleMedium
            )
        }

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
            Button(onClick = {  }) {
                Text("QUESTION SUIVANTE")
            }
        }
    }
}
