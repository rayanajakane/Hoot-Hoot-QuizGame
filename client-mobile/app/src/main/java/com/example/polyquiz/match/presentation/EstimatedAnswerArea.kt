package com.example.polyquiz.match.presentation

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.polyquiz.R
import com.example.polyquiz.constants.EstimatedQuestionFeedback
import com.example.polyquiz.match.domain.AnswerService
import com.example.polyquiz.match.domain.MatchRoomService
import com.example.polyquiz.constants.MatchContext
import com.example.polyquiz.ui.theme.AndroidGreen
import com.example.polyquiz.ui.theme.BrightRed

@Composable
fun EstimatedAnswerArea(
    answerService: AnswerService,
    matchRoomService: MatchRoomService,
    matchContext: MatchContext,
    modifier: Modifier = Modifier
) {
    val estimatedParams = matchRoomService.currentQuestion?.estimatedParameters
    val lowerBound = estimatedParams?.lowerBound?.toFloat() ?: 0f
    val upperBound = estimatedParams?.upperBound?.toFloat() ?: 100f

    var answer by remember { mutableStateOf(answerService.currentLongAnswer) }
    var sliderValue by remember { mutableStateOf(answer.toFloatOrNull() ?: lowerBound) }
    var isInputCleared by remember { mutableStateOf(answer.isEmpty()) }
    var isOutOfBounds by remember { mutableStateOf(false) }
    val isDisabled = answerService.showFeedback
    val isDisableHostView = matchContext == MatchContext.HOSTVIEW

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {

        if (matchContext === MatchContext.HOSTVIEW) {
            OutlinedTextField(
                value = MatchRoomService.currentAnswers[0],
                onValueChange = {},
                label = { Text(stringResource(R.string.answer)) },
                modifier = Modifier
                    .width(180.dp)
                    .padding(vertical = 8.dp)
                    .align(Alignment.CenterHorizontally),
                singleLine = true,
                enabled = false,
                isError = isOutOfBounds
            )
            Slider(
                value = MatchRoomService.currentAnswers[0].toFloat(),
                onValueChange = {
                    sliderValue = it
                    isInputCleared = false
                    val newValue = it.toInt().toString()
                    answer = newValue
                    answerService.currentLongAnswer = newValue
                    answerService.updateLongAnswer()
                },
                valueRange = lowerBound..upperBound,
                modifier = Modifier.weight(1f),
                enabled = false
            )
        }
        if (matchContext != MatchContext.HOSTVIEW) {
            Text(
                text = if (isDisabled ) {
                    stringResource(
                        R.string.good_answer_qre,
                        answerService.feedback.correctAnswer!![0], estimatedParams!!.margin
                    )
                } else {
                    stringResource(R.string.choose_estimated, estimatedParams!!.margin)
                },
                fontSize = 18.sp,
                color = if (isDisabled) AndroidGreen else MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )


            OutlinedTextField(
                value = answer,
                onValueChange = { newText ->
                    if (newText.matches(Regex("-?\\d*"))) {
                        answer = newText
                        isInputCleared = newText.isEmpty()

                        val newValue = newText.toFloatOrNull()
                        if (newValue != null) {
                            if (newValue in lowerBound..upperBound) {
                                isOutOfBounds = false
                                sliderValue = newValue
                                answerService.currentLongAnswer = newValue.toInt().toString()
                                answerService.updateLongAnswer()
                            } else {
                                isOutOfBounds = true
                                answerService.currentLongAnswer = ""
                                answerService.updateLongAnswer()
                            }
                        } else if (newText.isEmpty() || newText == "-") {
                            answerService.currentLongAnswer = ""
                            answerService.updateLongAnswer()
                            sliderValue = lowerBound
                            isOutOfBounds = false
                        }
                    }
                },
                label = { Text(stringResource(R.string.answer)) },
                modifier = Modifier
                    .width(180.dp)
                    .padding(vertical = 8.dp)
                    .align(Alignment.CenterHorizontally),
                singleLine = true,
                enabled = !isDisabled,
                isError = isOutOfBounds
            )
            if (matchContext === MatchContext.CHEATERVIEW) {
                Text(
                    text = stringResource(R.string.cheater_mode_answer_hint) + matchRoomService.currentAnswers[0],
                    fontSize = 16.sp,
                    modifier = Modifier
                        .width(180.dp)
                        .padding(vertical = 8.dp)
                        .align(Alignment.CenterHorizontally),
                )
            }


            if (isOutOfBounds) {
                Text(
                    text = stringResource(R.string.out_of_bounds, lowerBound.toInt(), upperBound.toInt()),
                    // TODO : Check hardcoded value
                    color = BrightRed,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = lowerBound.toInt().toString(),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Slider(
                    value = if (isInputCleared) lowerBound else sliderValue,
                    onValueChange = {
                        sliderValue = it
                        isInputCleared = false
                        val newValue = it.toInt().toString()
                        answer = newValue
                        answerService.currentLongAnswer = newValue
                        answerService.updateLongAnswer()
                    },
                    valueRange = lowerBound..upperBound,
                    modifier = Modifier.weight(1f),
                    enabled = !isDisabled
                )
                Text(
                    text = upperBound.toInt().toString(),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}
