package com.example.polyquiz.friends.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.polyquiz.R
import com.example.polyquiz.auth.domain.UserIdName
import com.example.polyquiz.constants.PresetAvatar
import com.example.polyquiz.ui.theme.BrightRed
import com.example.polyquiz.ui.theme.LightGray
import com.example.polyquiz.ui.theme.AndroidGreen
import com.example.polyquiz.ui.theme.HotPink

@Composable
fun FriendsListItem(
    modifier: Modifier = Modifier,
    user: UserIdName,
    isFriend: Boolean,
    isRequestPending: Boolean,
    isRequestSent: Boolean,
    isEligible: Boolean,
    onSendRequest: (String) -> Unit,
    onCancelRequest: (String) -> Unit,
    onAcceptRequest: (String) -> Unit,
    onRejectRequest: (String) -> Unit,
    onRemoveFriend: (String) -> Unit,
    onDonate: (String) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val painter = rememberAsyncImagePainter(
                model = user.photoUrl ?: PresetAvatar.DEFAULT.value
            )
            Image(
                painter = painter,
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.FillBounds
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = user.name)
            Spacer(modifier = Modifier.width(8.dp))
        }
        when {
            isFriend -> {
                Row {
                    Button(
                        onClick = { },
                        enabled = false,
                        shape = RoundedCornerShape(3.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LightGray)
                    ) {
                        Text(text = "✅ ${stringResource(R.string.friends)}")
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Button(
                        onClick = { onRemoveFriend(user.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = BrightRed, contentColor = Color.White),
                        shape = RoundedCornerShape(3.dp),
                    ) {
                        Text(text = stringResource(R.string.remove_friend))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Button(
                        onClick = { onDonate(user.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = HotPink),
                        shape = RoundedCornerShape(3.dp),
                    ) {
                        Text(text = stringResource(R.string.donate_money))
                    }
                }
            }

            isRequestPending -> {
                Row {
                    Button(
                        onClick = { onAcceptRequest(user.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = HotPink),
                        shape = RoundedCornerShape(3.dp),
                    ) {
                        Text(text = stringResource(R.string.accept))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Button(
                        onClick = { onRejectRequest(user.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = BrightRed),
                        shape = RoundedCornerShape(3.dp),
                    ) {
                        Text(text = stringResource(R.string.decline))
                    }
                }
            }

            isRequestSent -> {
                Row {
                    Button(
                        onClick = { },
                        enabled = false,
                        colors = ButtonDefaults.buttonColors(containerColor = LightGray),
                        shape = RoundedCornerShape(3.dp),
                    ) {
                        Text(text = stringResource(R.string.request_sent))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Button(
                        onClick = { onCancelRequest(user.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = HotPink),
                        shape = RoundedCornerShape(3.dp),
                    ) {
                        Text(text = stringResource(R.string.cancel))
                    }
                }
            }

            else -> {
                Button(onClick = { onSendRequest(user.id) }, shape = RoundedCornerShape(3.dp)) {
                    Text(text = stringResource(R.string.add_friend))
                }
            }
        }
    }
}
