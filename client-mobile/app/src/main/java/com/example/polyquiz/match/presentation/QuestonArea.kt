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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.polyquiz.match.domain.AnswerService
import com.example.polyquiz.match.domain.MatchContextService
import com.example.polyquiz.match.domain.MatchRoomService
import com.example.polyquiz.match.domain.TimeService
import com.example.polyquiz.constants.MatchStatus
import com.example.polyquiz.constants.UserInfo
import androidx.compose.foundation.layout.*
import com.example.polyquiz.constants.AnswerCorrectness
import com.example.polyquiz.constants.AnswerFeedback
import com.example.polyquiz.constants.BonusFeedback
import com.example.polyquiz.constants.MatchButtonActions
import com.example.polyquiz.ui.theme.AndroidGreen
import com.example.polyquiz.ui.theme.BrightRed
import com.example.polyquiz.ui.theme.GoldenYellow

@Composable
fun QuestionArea(
    matchRoomService: MatchRoomService,
    timeService: TimeService,
    matchContextService: MatchContextService,
    answerService: AnswerService,
    authViewModel: AuthViewModel,
    navigateToHome: () -> Unit,
    modifier: Modifier
) {

    val username by remember { mutableStateOf(authViewModel.getUsername()) }
    var context = MatchContext.PLAYERVIEW
    val question by matchRoomService::currentQuestion
    val score by answerService::playerScore

    LaunchedEffect(Unit, MatchRoomService.hasBeenKickedOut) {
        answerService.resetStateForNewQuestion()
        timeService.listenToTimerEvents()
        answerService.listenToAnswerEvents()
        matchRoomService.isQuitting = false
        answerService.playerScore = 0
//        context = matchContextService.getContext()

        when (MatchRoomService.hasBeenKickedOut) {
            true -> {
                MatchRoomService.hasBeenKickedOut = false
                navigateToHome()
            }
            else -> Unit
        }
    }


    Row(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            TimerComponent(
                modifier = Modifier.fillMaxWidth(),
                timeService = timeService,
            )

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
                    val questionText =
                        if (matchRoomService.isCooldown) MatchStatus.PREPARE.value else question?.text ?: ""

                    Text(
                        text = questionText,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    if (!matchRoomService.isCooldown) {
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
                        AnswerCorrectness.WRONG -> AnswerFeedback.WRONG.value to BrightRed
                        AnswerCorrectness.OK -> AnswerFeedback.OK.withPoints((question?.points ?: 0) / 2) to GoldenYellow
                        AnswerCorrectness.GOOD -> AnswerFeedback.GOOD.withPoints(question?.points ?: 0) to AndroidGreen
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
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = BonusFeedback.BONUS.withPoints(answerService.bonusPoints),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Green
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

            }

            if (!matchRoomService.isCooldown) {
                when (question?.type) {
                    QuestionType.MULTIPLE_CHOICE.value -> {
                        MultipleChoiceArea(
                            choices = question?.choices ?: emptyList(),
                            answerService = answerService,
                            matchRoomService = matchRoomService,
                            matchContext = context,
                            modifier = Modifier.fillMaxWidth(0.8f)
                        )
                    }

                    QuestionType.LONG_ANSWER.value -> {
                        LongAnswerArea(answerService, context, modifier = Modifier.fillMaxWidth(0.8f))
                    }
                }
            }



            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize(Alignment.Center)
            ) {
                if (answerService.isSelectionEnabled && context === MatchContext.PLAYERVIEW) {
                    Button(
                        onClick = {
                            answerService.submitAnswer(
                                UserInfo(
                                    username = matchRoomService.getUsername(),
                                    roomCode = matchRoomService.getRoomCode()
                                )
                            )
                        }
                    ) {
                        Text(MatchButtonActions.LEAVE_MATCH.value)
                    }
                }
            }

        }
            PlayersListComponent(
                matchRoomService = matchRoomService,
                context = context,
                players = matchRoomService.players,
                modifier = Modifier.width(250.dp).fillMaxHeight(),
                extraContent = {
                    Spacer(modifier = Modifier.height(16.dp))
                    if (context == MatchContext.HOSTVIEW &&
                        question?.type != null &&
                        (
                            (!answerService.isSelectionEnabled && question?.type == QuestionType.MULTIPLE_CHOICE.value) ||
                                (answerService.isGradingComplete && question?.type == QuestionType.LONG_ANSWER.value)
                            )
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { matchRoomService.goToNextQuestion() }) {
                            Text(MatchButtonActions.NEXT_QUESTION.value)
                        }
                    }

                    Button(
                        onClick = {
                            matchRoomService.isQuitting = true
                            matchRoomService.disconnectFromRoom()
                            navigateToHome()
                        }
                    ) {
                        Text(MatchButtonActions.LEAVE_MATCH.value)
                    }
                }
            )
        }


    }

