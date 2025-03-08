package com.example.polyquiz.match.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.polyquiz.match.domain.AnswerService
import com.example.polyquiz.match.domain.MatchRoomService
import com.example.polyquiz.constants.MatchContext

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
    val isDisabled = answerService.showFeedback

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        if (matchContext != MatchContext.HOSTVIEW) {

            Text(
                text = "Choisissez une valeur estimée",
                fontSize = 18.sp,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = answer,
                onValueChange = {
                    val newValue = it.toFloatOrNull()
                    if (!isDisabled && newValue != null && newValue in lowerBound..upperBound) {
                        answer = newValue.toInt().toString()
                        answerService.currentLongAnswer = answer
                        answerService.updateLongAnswer()
                    }
                },
                label = { Text("Réponse") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                singleLine = true,
                enabled = !isDisabled
            )

            Slider(
                value = answer.toFloatOrNull() ?: lowerBound,
                onValueChange = {
                    if (!isDisabled) {
                        val newValue = it.toInt().toString()
                        answer = newValue
                        answerService.currentLongAnswer = newValue
                        answerService.updateLongAnswer()
                    }
                },
                valueRange = lowerBound..upperBound,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isDisabled
            )
        }
    }
}
