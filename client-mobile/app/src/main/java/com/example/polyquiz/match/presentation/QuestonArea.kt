package com.example.polyquiz.match.presentation

import android.util.Log
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.polyquiz.auth.domain.AuthViewModel
import com.example.polyquiz.constants.AnswerCorrectness
import com.example.polyquiz.constants.MatchContext
import com.example.polyquiz.constants.QuestionType
import com.example.polyquiz.match.domain.AnswerService
import com.example.polyquiz.match.domain.MatchContextService
import com.example.polyquiz.match.domain.MatchRoomService
import com.example.polyquiz.match.domain.TimeService
import androidx.compose.runtime.snapshotFlow
import com.example.polyquiz.constants.MatchStatus
import com.example.polyquiz.match.domain.Question


@Composable
fun QuestionArea(
    matchRoomService: MatchRoomService,
    timeService: TimeService,
    matchContextService: MatchContextService,
    answerService : AnswerService,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    navigateToHome: () -> Unit
) {

    fun goToNextQuestion(){
        matchRoomService.goToNextQuestion();
        answerService.isNextQuestionButtonEnabled = false;
    }

    fun resetStateForNewQuestion(){
        answerService.resetStateForNewQuestion();
    }

    fun listenToGameEvents(){
        timeService.listenToTimerEvents();
        answerService.listenToAnswerEvents();
    }


    var room by remember { mutableStateOf(matchRoomService.getRoomCode()) }
    val username by remember { mutableStateOf(authViewModel.getUsername() )}
    var context by remember { mutableStateOf(matchContextService.getContext()) }
    val question by matchRoomService::currentQuestion


//    val answerOptions by remember { derivedStateOf { AnswerCorrectness } }

    val score by answerService::playerScore;

    LaunchedEffect(Unit) {
        resetStateForNewQuestion()
        listenToGameEvents()
        matchRoomService.isQuitting = false
        answerService.playerScore = 0
        context = matchContextService.getContext()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        TimerComponent(
            modifier = Modifier.fillMaxWidth(),
            timeService = timeService,
        )

//        Spacer(modifier = Modifier.height(24.dp))

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
                val questionText = if (matchRoomService.isCooldown) MatchStatus.PREPARE.value else question?.text ?: "xx"

                Text(
                    text = questionText,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))
                if (!matchRoomService.isCooldown){
                    Text(
                        text = "${question?.points} points",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }

            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        if (context != MatchContext.HOSTVIEW){
            Text(
                text = "SCORE : $score",
                style = MaterialTheme.typography.titleMedium
            )

            if (answerService.showFeedback && context === MatchContext.PLAYERVIEW && !matchRoomService.isCooldown) {
                val (feedbackText, feedbackColor) = when (answerService.answerCorrectness) {
                    AnswerCorrectness.WRONG.ordinal -> "\uD83D\uDE14 Mauvaise Réponse \uD83D\uDE14" to Color.Red
                    AnswerCorrectness.OK.ordinal -> {
                        "\uD83C\uDD97 Réponse partielle! Vous avez obtenu ${(question?.points ?: 0) / 2} points \uD83C\uDD97" to Color.Yellow
                    }
                    AnswerCorrectness.GOOD.ordinal -> {
                        "\uD83C\uDD97 Réponse correcte! Vous avez obtenu ${question?.points} points \uD83C\uDD97" to Color.Green
                    }
                    else -> null to null
                }

                if (feedbackText != null && feedbackColor != null) {
                    Text(
                        text = feedbackText,
                        style = MaterialTheme.typography.titleMedium,
                        color = feedbackColor
                    )
                }


            if (answerService.bonusPoints > 0){
                    Text(
                        text = "✨ Vous avez obtenu un bonus de ${answerService.bonusPoints} points!✨",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Green
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (!matchRoomService.isCooldown){
            when (question?.type) {
                QuestionType.MULTIPLE_CHOICE.value -> {
                    MultipleChoiceArea(choices = question?.choices ?: emptyList(), answerService, matchRoomService, context, modifier = Modifier.fillMaxWidth(0.8f))
                }

                QuestionType.LONG_ANSWER.value -> {
                    LongAnswerArea(modifier = Modifier.fillMaxWidth(0.8f))
                }
            }
        }


        if (context == MatchContext.HOSTVIEW) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { goToNextQuestion();}) {
                Text("QUESTION SUIVANTE")
            }
        }
        //TODO: REMOVE WHEN DONE
        if ( question == null){
            TextField(
                value = room,
                onValueChange = { room = it },
                label = { Text("Room ID") },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(8.dp)
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
                onClick = { matchRoomService.connect();matchRoomService.joinRoom(room, username); timeService.handleTimer() },
                modifier = Modifier.fillMaxWidth(0.5f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("join")
            }
        }
        }
}
