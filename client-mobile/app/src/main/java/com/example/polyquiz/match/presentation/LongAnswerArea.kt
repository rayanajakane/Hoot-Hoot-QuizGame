package com.example.polyquiz.match.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.polyquiz.R
import com.example.polyquiz.constants.AnswerCorrectness
import com.example.polyquiz.constants.FREE_ANSWER_MAX_LENGTH
import com.example.polyquiz.constants.MatchContext
import com.example.polyquiz.match.domain.AnswerService
import com.example.polyquiz.match.domain.MatchRoomService
import com.example.polyquiz.constants.GradesInfo
import com.example.polyquiz.constants.GradingFeedback
import com.example.polyquiz.constants.LongAnswerInfo

@Composable
fun LongAnswerArea(
    answerService: AnswerService,
    matchContext: MatchContext,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        if (matchContext != MatchContext.HOSTVIEW) {
                if (!answerService.isSelectionEnabled && !answerService.showFeedback) {
                    Text(
                        text = stringResource(R.string.waiting_grading),
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                var answer by remember { mutableStateOf(answerService.currentLongAnswer) }

                OutlinedTextField(
                    value = answer,
                    onValueChange = { if (it.length <= FREE_ANSWER_MAX_LENGTH) {
                        answer = it;
                        answerService.currentLongAnswer = it
                        answerService.updateLongAnswer()
                    } },
                    label = { Text(stringResource(R.string.answer)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5,
                    enabled = answerService.isSelectionEnabled
                )

                Text(
                    text = "${answer.length} / $FREE_ANSWER_MAX_LENGTH",
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 4.dp)
                )
        } else {
            if (answerService.gradeAnswers) {
                Text(
                    text = stringResource(R.string.grade_players),
                    fontSize = 18.sp,
                    modifier = Modifier.padding(8.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(Color.White)
                        .padding(8.dp)
                ) {
                    items(answerService.playersAnswers) { playerAnswer ->
                        AnswerCard(playerAnswer)
                    }
                }


                Button(
                    onClick = { answerService.sendGrades() },
                    enabled = answerService.isGradingComplete,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = if (answerService.isGradingComplete) stringResource(R.string.submit_grading)
                        else stringResource(R.string.players_to_grade)
                    )
                }
            }
        }
    }
}

@Composable
fun AnswerCard(playerAnswer: LongAnswerInfo) {
    val answerService = AnswerService
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Text(text = playerAnswer.username, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(text = playerAnswer.answer, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                AnswerCorrectness.entries.forEach { option ->
                    Button(
                        onClick = { playerAnswer.score = option.value.toString()
                                answerService.handleGrading()},
                    ) {
                        Text("${option.value}%")
                    }
                }
            }
        }
    }
}

fun getGradeColor(option: AnswerCorrectness): Color {
    return when (option) {
        AnswerCorrectness.WRONG -> Color.Red
        AnswerCorrectness.OK -> Color.Yellow
        AnswerCorrectness.GOOD -> Color.Green
    }
}

