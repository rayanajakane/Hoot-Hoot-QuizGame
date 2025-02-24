package com.example.polyquiz.classicmode.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.polyquiz.classicmode.domain.TimeService
import com.example.polyquiz.classicmode.domain.TimeService.joinRoom
import com.example.polyquiz.classicmode.domain.TimeService.startTimer

@Composable
fun TimerComponent(
    modifier: Modifier = Modifier,
    timeService: TimeService,
) {
    val timer by remember { mutableIntStateOf(timeService.time) }
    val progress = timeService.computeTimerProgress() / 100f


    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(90.dp),
            strokeWidth = 8.dp,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "$timer s", style = MaterialTheme.typography.bodyLarge)
    }
//    Button(
//        onClick = {
//            startTimer("3095", 60)
//        },
//        colors = ButtonDefaults.buttonColors(
//            containerColor = MaterialTheme.colorScheme.surfaceBright,
//            contentColor = MaterialTheme.colorScheme.onSurface
//        )
//    ) {
//        Text(text = "Start Timer with 1234 and 25")
//    }
//    Button(
//        onClick = {
//            joinRoom("6450", "sami")
//        },
//        colors = ButtonDefaults.buttonColors(
//            containerColor = MaterialTheme.colorScheme.surfaceBright,
//            contentColor = MaterialTheme.colorScheme.onSurface
//        )
//    ) {
//        Text(text = "JoinRoom")
//    }
}
