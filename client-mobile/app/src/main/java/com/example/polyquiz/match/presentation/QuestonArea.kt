package com.example.polyquiz.match.presentation

import android.media.MediaPlayer
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip

import coil.compose.rememberAsyncImagePainter

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.example.polyquiz.constants.UserInfo
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material3.Icon
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.polyquiz.R
import com.example.polyquiz.SnackbarController
import com.example.polyquiz.SnackbarEvent
import com.example.polyquiz.chat.presentation.ChatComponent
import com.example.polyquiz.constants.AnswerCorrectness
import com.example.polyquiz.match.domain.MatchRoomService.isCooldown
import com.example.polyquiz.constants.AnswerEvents
import com.example.polyquiz.match.domain.AnswerService.showFeedback
import kotlinx.coroutines.launch
import com.example.polyquiz.match.domain.MatchRoomService.isCooldown

@Composable
fun QuestionArea(
    matchRoomService: MatchRoomService,
    timeService: TimeService,
    matchContextService: MatchContextService,
    answerService: AnswerService,
    authViewModel: AuthViewModel,
    navigateToHome: () -> Unit,
    navigateToResultsPage: () -> Unit,
    navigateToVotingPage: () -> Unit,
    modifier: Modifier
) {
    var room by remember { mutableStateOf(matchRoomService.getRoomCode()) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var context by remember { mutableStateOf(matchContextService.getContext()) }
    val question by matchRoomService::currentQuestion
    val score by answerService::playerScore
    val scope = rememberCoroutineScope()

    val hasImage = !question?.pictureUrl.isNullOrEmpty()
    val isTimerPaused by TimeService.isTimerPaused.collectAsState()
    val isPanicking by TimeService.isPanicking.collectAsState()

    var isTimerPausedButtonEnabled by remember { mutableStateOf(false) }

    val mediaPlayer = MediaPlayer.create(LocalContext.current, R.raw.panic)

    LaunchedEffect(isPanicking) {
        if(isPanicking) {
            mediaPlayer.start()
            scope.launch {
                SnackbarController.sendEvent(
                    event = SnackbarEvent(
                        message = StringValue.StringResource(R.string.panic_mode_activated)
                    )
                )
            }
        }
    }

    LaunchedEffect(isTimerPaused) {
        if(!isTimerPaused) {
            scope.launch {
                SnackbarController.sendEvent(
                    event = SnackbarEvent(
                        message = StringValue.StringResource(R.string.timer_start)
                    )
                )
            }
        } else {
            scope.launch {
                SnackbarController.sendEvent(
                    event = SnackbarEvent(
                        message = StringValue.StringResource(R.string.timer_paused)
                    )
                )
            }
        }
    }

    LaunchedEffect (matchRoomService.isCheaterMode){
        if (matchRoomService.isCheaterMode) {
            if (matchRoomService.username == matchRoomService.cheaterPlayer?.username) {
                matchContextService.setContext(MatchContext.CHEATERVIEW);
                scope.launch {
                    SnackbarController.sendEvent(
                        event = SnackbarEvent(
                            message = StringValue.StringResource(R.string.cheater_mode_notify_cheater)
                        )
                    )
                }
            }
            if(matchContextService.getContext() === MatchContext.PLAYERVIEW){
                scope.launch {
                    SnackbarController.sendEvent(
                        event = SnackbarEvent(
                            message = StringValue.StringResource(R.string.cheater_mode_notify_regular_player)
                        )
                    )
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        answerService.resetStateForNewQuestion()
        timeService.listenToTimerEvents()
        answerService.listenToAnswerEvents()
    }

    LaunchedEffect(
        MatchRoomService.hasBeenKickedOut,
        MatchRoomService.isTimeToNavigateToResults,
        MatchRoomService.navigateToVotingPage
    ) {
        matchRoomService.isQuitting = false
        answerService.resetStateForNewQuestion()
        answerService.playerScore = 0
        context = matchContextService.getContext()

        when (MatchRoomService.navigateToVotingPage) {
            true -> {
                navigateToVotingPage();
            }

            else -> Unit
        }

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

    // To avoid memory leaks
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer.release()
        }
    }

    context = matchContextService.getContext()

    fun routeToResultsPage() {
        matchRoomService.routeToResultsPage()
    }

    Row(modifier = Modifier
        .statusBarsPadding()
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
                .background(MaterialTheme.colorScheme.background).navigationBarsPadding(),
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
                    .height(250.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .shadow(4.dp)
                    .background(
                        color = Color(0xFF4054B4)
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (hasImage && !matchRoomService.isCooldown) {
                    val questionText = question?.text ?: ""
                    Text(
                        text = questionText.uppercase(),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                    Image(
                        painter = rememberAsyncImagePainter(model = question?.pictureUrl),
                        contentDescription = "Question Image",
                        modifier = Modifier
                            .heightIn(max = 150.dp)
                            .align(Alignment.CenterEnd)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${question?.points} points",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(top = 16.dp)
                    )


                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val questionText =
                            if (matchRoomService.isCooldown) stringResource(R.string.match_prepare) else question?.text
                                ?: ""

                        Text(
                            text = questionText,
                            style = MaterialTheme.typography.titleLarge,
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
                            stringResource(
                                R.string.partial_answer,
                                (question?.points ?: 0) / 2
                            ) to Color(
                                0xFFf6c811
                            )
                        }

                        AnswerCorrectness.GOOD -> {
                            stringResource(R.string.good_answer, question?.points ?: 0) to Color(
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
                            text = stringResource(
                                R.string.bonus_message,
                                answerService.bonusPoints
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Green
                        )
                    }
                }

                if (answerService.showFeedback && context === MatchContext.CHEATERVIEW && !matchRoomService.isCooldown) {
                    val (feedbackText, feedbackColor) = when (answerService.answerCorrectness) {
                        AnswerCorrectness.WRONG -> stringResource(R.string.wrong_answer) to Color(
                            0xFFe91b0c
                        )

                        AnswerCorrectness.OK -> {
                            stringResource(
                                R.string.partial_answer,
                                (question?.points ?: 0) / 2
                            ) to Color(
                                0xFFf6c811
                            )
                        }

                        AnswerCorrectness.GOOD -> {
                            stringResource(R.string.good_answer, question?.points ?: 0) to Color(
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
                            text = stringResource(
                                R.string.bonus_message,
                                answerService.bonusPoints
                            ),
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
                if (answerService.isSelectionEnabled && (context === MatchContext.PLAYERVIEW || context === MatchContext.CHEATERVIEW)) {
                    Button(
                        onClick = {
                            answerService.submitAnswer(
                                UserInfo(
                                    userId = matchRoomService.userId,
                                    roomCode = matchRoomService.getRoomCode()
                                )
                            )
                        },
                        shape = RoundedCornerShape(3.dp)
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
                    .fillMaxHeight()
                    .navigationBarsPadding(),
                extraContent = {
                    Column {
                        Spacer(modifier = Modifier.height(16.dp))
                        if (context == MatchContext.HOSTVIEW && !matchRoomService.isCooldown) {
                            Spacer(modifier = Modifier.height(16.dp))
                            if (answerService.isEndGame && !matchRoomService.isCheaterMode) {
                                Log.d("Question area", "is end game")
                                Button(
                                    onClick = {
                                        routeToResultsPage();
                                        navigateToResultsPage()
                                    },
                                    modifier = Modifier.fillMaxWidth(0.8f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.BarChart,
                                        contentDescription = stringResource(R.string.show_final)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(stringResource(R.string.show_final))
                                }
                            } else if (!answerService.isEndGame && answerService.isNextQuestionButtonEnabled) {
                                Log.d("Question area", "next question enabled")
                                Button(
                                    onClick = { matchRoomService.goToNextQuestion() },
                                    shape = RoundedCornerShape(3.dp)
                                ) {
                                    Text(stringResource(R.string.next_question))
                                }
                            } else if (!answerService.isNextQuestionButtonEnabled) {
                                Button(
                                    onClick = {
                                        isTimerPausedButtonEnabled = !isTimerPausedButtonEnabled
                                        timeService.pauseTimer(matchRoomService.getRoomCode())
                                    },
                                    shape = RoundedCornerShape(3.dp)
                                ) {
                                    if (isTimerPaused) {
                                        Icon(
                                            Icons.Filled.PlayArrow,
                                            contentDescription = stringResource(R.string.start_timer)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(stringResource(R.string.start_timer))
                                    } else {
                                        Icon(
                                            Icons.Filled.Pause,
                                            contentDescription = stringResource(R.string.pause_timer)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(stringResource(R.string.pause_timer))
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        timeService.triggerPanicTimer(matchRoomService.getRoomCode())
                                    },
                                    enabled = !isPanicking,
                                    shape = RoundedCornerShape(3.dp)
                                ) {

                                    Icon(
                                        Icons.Filled.PriorityHigh,
                                        contentDescription = stringResource(R.string.panic_mode)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(R.string.panic_mode))

                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            else if (answerService.isEndGame && MatchRoomService.isCheaterMode ) {
                                Log.d("Voting Area", "next question enabled")
                                Button(
                                    onClick = { MatchRoomService.voteOnCheater() },
                                    shape = RoundedCornerShape(3.dp)
                                ) {
                                    Text(stringResource(R.string.cheater_mode_go_to_vote_button))
                                }
                            }
                        }

                        Button(
                            onClick = {
                                matchRoomService.isQuitting = true
                                matchRoomService.disconnectFromRoom()
                                navigateToHome()
                            },
                            shape = RoundedCornerShape(3.dp),
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Logout,
                                contentDescription = stringResource(R.string.leave)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(stringResource(R.string.leave))
                        }
                    }
                }
            )
        }
    }


}

