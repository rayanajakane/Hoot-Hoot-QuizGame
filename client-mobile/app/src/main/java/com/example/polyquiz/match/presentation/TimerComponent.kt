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
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun TimerComponent(
    modifier: Modifier = Modifier,
    timeService: TimeService,
    size: Dp,
    fontSize: TextUnit,
    stroke: Dp
) {
    val timer = remember { mutableIntStateOf(timeService.time) }
    val progress = timeService.computeTimerProgress() / 100f

    LaunchedEffect(timeService.time) {
        timer.value = timeService.time
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(size),
            strokeWidth = stroke,
        )

        Text(
            text = "${timer.value}",
            fontWeight = FontWeight.Bold,
            fontSize = fontSize
        )
    }
}
