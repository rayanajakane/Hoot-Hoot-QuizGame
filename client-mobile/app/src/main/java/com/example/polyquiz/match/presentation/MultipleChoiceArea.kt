package com.example.polyquiz.match.presentation

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.polyquiz.constants.MatchContext
import com.example.polyquiz.constants.UserInfo
import com.example.polyquiz.match.domain.AnswerService
import com.example.polyquiz.match.domain.Choice
import com.example.polyquiz.match.domain.MatchRoomService
import com.example.polyquiz.ui.theme.AndroidGreen
import com.example.polyquiz.ui.theme.BrightRed
import com.example.polyquiz.ui.theme.DarkGrey
import com.example.polyquiz.ui.theme.LightGray

@Composable
fun MultipleChoiceArea(
    choices: List<Choice>,
    answerService: AnswerService,
    matchRoomService: MatchRoomService,
    matchContext: MatchContext,
    modifier: Modifier = Modifier
) {

    val selectedStates =
        remember { mutableStateListOf<Boolean>().apply { addAll(List(choices.size) { false }) } }
    val rows = (choices.size + 1) / 2

    if (matchContext == MatchContext.PLAYERVIEW || matchContext == MatchContext.CHEATERVIEW) {
        val selectedStates =
            remember { mutableStateListOf<Boolean>().apply { addAll(List(choices.size) { false }) } }
        val rows = (choices.size + 1) / 2

        Column(modifier = modifier) {
            for (rowIndex in 0 until rows) {
                val firstIndex = rowIndex * 2

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ChoiceButton(
                        firstIndex,
                        choices,
                        selectedStates,
                        answerService,
                        matchRoomService,
                        matchContext,
                        Modifier.weight(1f)
                    )

                    if (firstIndex + 1 < choices.size) {
                        ChoiceButton(
                            firstIndex + 1,
                            choices,
                            selectedStates,
                            answerService,
                            matchRoomService,
                            matchContext,
                            Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }

    if (matchContext === MatchContext.HOSTVIEW) {
        answerService.isSelectionEnabled = false
        Column(modifier = modifier) {
            for (rowIndex in 0 until rows) {
                val firstIndex = rowIndex * 2

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {

                    ChoiceButton(
                        firstIndex,
                        choices,
                        selectedStates,
                        answerService,
                        matchRoomService,
                        matchContext,
                        Modifier.weight(1f)
                    )

                    if (firstIndex + 1 < choices.size) {
                        ChoiceButton(
                            firstIndex + 1,
                            choices,
                            selectedStates,
                            answerService,
                            matchRoomService,
                            matchContext,
                            Modifier.weight(1f)

                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun ChoiceButton(
    index: Int,
    choices: List<Choice>,
    selectedStates: MutableList<Boolean>,
    answerService: AnswerService,
    matchRoomService: MatchRoomService,
    matchContext: MatchContext,
    modifier: Modifier = Modifier
) {
    val showFeedback by answerService::showFeedback;
    val feedback by answerService::feedback;
    if (index < choices.size) {
        val choice = choices[index]
        val buttonColor = when {
            showFeedback && feedback.correctAnswer.orEmpty().contains(choice.text) -> AndroidGreen
            matchRoomService.currentAnswers.orEmpty()
                .contains(choice.text) && matchContext === MatchContext.HOSTVIEW -> AndroidGreen

            showFeedback && selectedStates[index] && !feedback.correctAnswer.orEmpty()
                .contains(choice.text) -> BrightRed

            selectedStates[index] && !showFeedback -> DarkGrey
            else -> LightGray
        }
        Button(
            onClick = {
                if (answerService.isSelectionEnabled) {
                    selectedStates[index] = !selectedStates[index]
                    val userInfo = UserInfo(
                        userId = matchRoomService.userId,
                        roomCode = matchRoomService.getRoomCode()
                    )
                    if (selectedStates[index]) {
                        answerService.selectChoice(choice.text, userInfo)
                    } else {
                        answerService.deselectChoice(choice.text, userInfo)
                    }
                }
            },
            shape = RoundedCornerShape(3.dp),
            enabled = answerService.isSelectionEnabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonColor,
                disabledContainerColor = buttonColor,
                disabledContentColor = Color.Black
            ),
            modifier = modifier
                .height(100.dp)
                .padding(8.dp)
        )

        {
            Text(
                text = "${index + 1}. ${choice.text}",
                color = if (!selectedStates[index]) Color.Black else Color.Unspecified
            )

            Box(modifier = Modifier.fillMaxSize()) {
                if (matchContext == MatchContext.CHEATERVIEW &&
                    matchRoomService.currentAnswers.orEmpty().contains(choice.text)
                ) {
                    Icon(
                        tint = AndroidGreen,
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (matchContext == MatchContext.HOSTVIEW &&
                    matchRoomService.currentAnswers.orEmpty().contains(choice.text)
                ) {
                    Icon(
                        tint = AndroidGreen,
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}
