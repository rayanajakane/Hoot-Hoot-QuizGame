package com.example.polyquiz.match.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.polyquiz.match.domain.Choice

@Composable
fun MultipleChoiceArea(
    choices: List<Choice>,
    modifier: Modifier = Modifier
) {
    val selectedStates = remember { mutableStateListOf<Boolean>().apply { addAll(List(choices.size) { false }) } }
    val rows = (choices.size + 1) / 2

    Column(modifier = modifier) {
        for (rowIndex in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val firstIndex = rowIndex * 2
                if (firstIndex < choices.size) {
                    Button(
                        onClick = {
                            selectedStates[firstIndex] = !selectedStates[firstIndex]
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedStates[firstIndex])
                                Color(0xFFA9A9A9)
                            else
                                Color(0xFFD3D3D3)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "${firstIndex + 1}. ${choices[firstIndex].text}",
                            color = if (!selectedStates[firstIndex]) Color.Black else Color.Unspecified
                        )
                    }
                }
                if (firstIndex + 1 < choices.size) {
                    Button(
                        onClick = {
                            selectedStates[firstIndex + 1] = !selectedStates[firstIndex + 1]
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedStates[firstIndex + 1])
                                Color(0xFFA9A9A9)
                            else
                                Color(0xFFD3D3D3)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "${firstIndex + 2}. ${choices[firstIndex + 1].text}",
                            color = if (!selectedStates[firstIndex + 1]) Color.Black else Color.Unspecified
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }}    }

