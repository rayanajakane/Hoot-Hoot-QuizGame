package com.example.polyquiz.match.presentation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.example.polyquiz.R
import com.example.polyquiz.chat.presentation.ChatComponent
import com.example.polyquiz.constants.AnswerCorrectness
import com.example.polyquiz.match.domain.MatchRoomService.players
import com.example.polyquiz.match.domain.MatchRoomService.voteOnCheater
import com.example.polyquiz.pages.presentation.VotingDialogComponent

@Composable
fun QuestionArea(
    matchRoomService: MatchRoomService,
    timeService: TimeService,
    matchContextService: MatchContextService,
    answerService: AnswerService,
    authViewModel: AuthViewModel,
    navigateToHome: () -> Unit,
    navigateToResultsPage: () -> Unit,
    modifier: Modifier
) {
    var room by remember { mutableStateOf(matchRoomService.getRoomCode()) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var context by remember { mutableStateOf(matchContextService.getContext()) }
    var showVotingDialogState by remember { mutableStateOf(false) }
    val question by matchRoomService::currentQuestion
    val score by answerService::playerScore

    LaunchedEffect(Unit, MatchRoomService.hasBeenKickedOut, MatchRoomService.isTimeToNavigateToResults) {
        answerService.resetStateForNewQuestion()
        timeService.listenToTimerEvents()
        answerService.listenToAnswerEvents()
        matchRoomService.isQuitting = false
        answerService.playerScore = 0
        context = matchContextService.getContext()

        when (MatchRoomService.hasBeenKickedOut) {
            true -> {
                MatchRoomService.hasBeenKickedOut = false
                navigateToHome()
            }

            else -> Unit
        }
        when (MatchRoomService.isTimeToNavigateToResults) {
            true -> {
                navigateToResultsPage();
            }
            else -> Unit
        }
    }
    context = matchContextService.getContext()

    fun routeToResultsPage() {
        matchRoomService.routeToResultsPage()
    }

    Row(modifier = Modifier.statusBarsPadding()
        .fillMaxSize()
        .pointerInput(Unit) {
            detectTapGestures(onTap = {
                focusManager.clearFocus()
                keyboardController?.hide()
            })
        }) {
        ChatComponent(modifier = Modifier, authViewModel = authViewModel)
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
                size = 90.dp,
                fontSize = 22.sp,
                stroke = 8.dp
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
                        if (matchRoomService.isCooldown) stringResource(R.string.match_prepare) else question?.text
                            ?: ""

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
            if (context != MatchContext.HOSTVIEW) {
                Text(
                    text = "SCORE : $score",
                    style = MaterialTheme.typography.titleMedium
                )

                if (answerService.showFeedback && context === MatchContext.PLAYERVIEW && !matchRoomService.isCooldown) {
                    val (feedbackText, feedbackColor) = when (answerService.answerCorrectness) {
                        AnswerCorrectness.WRONG -> stringResource(R.string.wrong_answer) to Color(
                            0xFFe91b0c
                        )

                        AnswerCorrectness.OK -> {
                            stringResource(R.string.partial_answer) + " points \uD83C\uDD97" to Color(
                                0xFFf6c811
                            )
                        }

                        AnswerCorrectness.GOOD -> {
                            stringResource(R.string.good_answer) + "${question?.points} points \uD83C\uDD97" to Color(
                                0xFF4caf50
                            )
                        }

                    }

                    Text(
                        text = feedbackText,
                        style = MaterialTheme.typography.titleMedium,
                        color = feedbackColor
                    )


                    if (answerService.bonusPoints > 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.bonus_message, answerService.bonusPoints),
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
                        LongAnswerArea(
                            answerService,
                            context,
                            modifier = Modifier.fillMaxWidth(0.8f)
                        )
                    }

                    QuestionType.ESTIMATED_ANSWER.value -> {
                        EstimatedAnswerArea(
                            answerService,
                            matchRoomService,
                            context,
                            modifier = Modifier.fillMaxWidth(0.8f)
                        )

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
                                    userId = matchRoomService.userId,
                                    roomCode = matchRoomService.getRoomCode()
                                )
                            )
                        }
                    ) {
                        Text(stringResource(R.string.submit))
                    }
                }
            }

        }

        if (MatchRoomService.isMatchStarted) {
            PlayersListComponent(
                matchRoomService = matchRoomService,
                context = matchContextService,
                players = matchRoomService.players,
                modifier = Modifier
                    .width(250.dp)
                    .fillMaxHeight(),
                extraContent = {
                    Column {
                        Spacer(modifier = Modifier.height(16.dp))
                        if (context == MatchContext.HOSTVIEW && !matchRoomService.isCooldown) {
                            Spacer(modifier = Modifier.height(16.dp))
                            if (answerService.isNextQuestionButtonEnabled) {
                                Button(onClick = { matchRoomService.goToNextQuestion() }) {
                                    Text(stringResource(R.string.next_question))
                                }
                            } else if(answerService.isEndGame) {
                                Button(
                                    onClick = {
                                        routeToResultsPage();
                                        navigateToResultsPage()
                                    },
                                    modifier = Modifier.fillMaxWidth(0.8f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(stringResource(R.string.show_final))
                                }
                            }
                        }

                        Button(
                            onClick = {
                                matchRoomService.isQuitting = true
                                matchRoomService.disconnectFromRoom()
                                navigateToHome()
                            }
                        ) {
                            Text(stringResource(R.string.leave))
                        }
                    }
                }
            )
        }
        if (MatchRoomService.showVotingDialogState) {
            VotingDialogComponent(
                players = players,
                matchRoomService = MatchRoomService,
                onVote = { voteData ->
                    matchRoomService.sendBackVotesResult(voteData)
                    showVotingDialogState = false

                },
                onClose = {
                    showVotingDialogState = false
                }
            )
        }


        if (context == MatchContext.HOSTVIEW && !matchRoomService.isCooldown && matchRoomService.isCheaterMode) {
            if (answerService.isEndGame) {
                Button(
                    onClick = { voteOnCheater() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Votez pour le tricheur", color = Color.White)
                }
            }
        }
    }


}

