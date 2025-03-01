package com.example.polyquiz.match.presentation

import android.util.Log
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.polyquiz.match.domain.MatchRoomService
import com.example.polyquiz.match.domain.TimeService

@Composable
fun TimerComponent(
    modifier: Modifier = Modifier,
    timeService: TimeService,

) {
    val timer = remember { mutableIntStateOf(timeService.time) }
    val progress = timeService.computeTimerProgress() / 100f

    LaunchedEffect(timeService.time) {
        timer.value = timeService.time
    }

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
        Text(text = "${timer.value} s", style = MaterialTheme.typography.bodyLarge)
    }
}
