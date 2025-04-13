package com.example.polyquiz.elo.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.polyquiz.auth.domain.UserIdName
import com.example.polyquiz.constants.PresetAvatar
import com.example.polyquiz.match.domain.Player

@Composable
fun EloItem(
    modifier: Modifier = Modifier,
    user: Player,
    rank: Int,
    elo: Int,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val painter = rememberAsyncImagePainter(
                model = user.photoUrl ?: PresetAvatar.DEFAULT.value
            )
            Text(text = rank.toString())
            Spacer(modifier = Modifier.width(12.dp))
            Image(
                painter = painter,
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = user.username)
        }

        Spacer(modifier = Modifier.width(24.dp)) // Creates more gap between name section and elo

        Text(
            text = elo.toString(),
            modifier = Modifier
                .wrapContentWidth(Alignment.End)
        )
    }
}
