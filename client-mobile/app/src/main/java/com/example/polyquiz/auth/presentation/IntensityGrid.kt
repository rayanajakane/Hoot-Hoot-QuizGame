package com.example.polyquiz.auth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.polyquiz.constants.IntensityGridItem
import java.util.Locale

@Composable
fun IntensityGrid(intensityGrid: List<IntensityGridItem>) {
    if (intensityGrid.isEmpty()) return

    val chunkedData = intensityGrid.chunked(7)
    val scrollState = rememberScrollState()

    val calendar = java.util.Calendar.getInstance()
    calendar.set(java.util.Calendar.DAY_OF_YEAR, 1)
    val startDate = calendar.time

    BoxWithConstraints(Modifier.fillMaxWidth().padding(end = 26.dp)) {
        val totalColumns = chunkedData.size
        val spacing = 2.dp
        val totalSpacing = spacing * (totalColumns - 1)
        val columnWidth = (maxWidth - totalSpacing) / totalColumns
        val monthShort = java.text.DateFormatSymbols.getInstance(Locale.getDefault()).shortMonths

        val columnMonths = List(totalColumns) { index ->
            val colCal = java.util.Calendar.getInstance()
            colCal.time = startDate
            colCal.add(java.util.Calendar.DAY_OF_YEAR, index * 7)
            colCal.get(java.util.Calendar.MONTH)
        }

        Column {
            Row(
                modifier = Modifier
                    .horizontalScroll(scrollState)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                var prevMonth = -1
                columnMonths.forEach { currentMonth ->
                    if (currentMonth != prevMonth) {
                        Text(
                            text = monthShort[currentMonth].replaceFirstChar { it.uppercaseChar() },
                            fontSize = 12.sp,
                            maxLines = 1,
                            softWrap = false,
                        )
                        prevMonth = currentMonth
                    } else {
                        Spacer(Modifier.width(columnWidth-3.dp))
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .horizontalScroll(scrollState)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                chunkedData.forEach { columnData ->
                    Column {
                        columnData.forEach { level ->
                            Box(
                                modifier = Modifier
                                    .size(columnWidth)
                                    .background(
                                        when (level.intensity.toInt()) {
                                            0 -> Color(0xFFEBEDF0).copy(alpha = 0.6f)
                                            1 -> Color(0xFFC6E48B)
                                            2 -> Color(0xFF7BC96F)
                                            3 -> Color(0xFF196127)
                                            else -> Color(0xFFEBEDF0).copy(alpha = 0.6f)
                                        }
                                    )
                            )
                            Spacer(Modifier.height(2.dp))
                        }
                    }
                }
            }
        }
    }

    Spacer(Modifier.height(8.dp))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Text("-")
        Box(Modifier.size(20.dp).background(Color(0xFFEBEDF0).copy(alpha = 0.6f)))
        Box(Modifier.size(20.dp).background(Color(0xFFC6E48B)))
        Box(Modifier.size(20.dp).background(Color(0xFF7BC96F)))
        Box(Modifier.size(20.dp).background(Color(0xFF196127)))
        Text("+")
    }
}
