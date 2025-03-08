package com.example.polyquiz.match.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        if (matchContext != MatchContext.HOSTVIEW) {
            if (isDisabled) {
                Text(
                    text = "Réponse correcte : ${answerService.feedback.correctAnswer?.get(0)}",
                    fontSize = 18.sp,
                    color = AndroidGreen,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }
            if (!isDisabled) {
                Text(
                    text = "Choisissez une valeur estimée",
                    fontSize = 18.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

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
                            }
                        } else if (newText.isEmpty()) {
                            answerService.currentLongAnswer = ""
                            answerService.updateLongAnswer()
                            sliderValue = lowerBound
                            isOutOfBounds = false
                        }
                    }
                },
                label = { Text("Réponse") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                singleLine = true,
                enabled = !isDisabled,
                isError = isOutOfBounds
            )

            if (isOutOfBounds) {
                Text(
                    text = "Valeur hors limites ! Doit être entre ${lowerBound.toInt()} et ${upperBound.toInt()}",
                    color = BrightRed,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

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
                modifier = Modifier.fillMaxWidth(),
                enabled = !isDisabled
            )
        }
    }
}
