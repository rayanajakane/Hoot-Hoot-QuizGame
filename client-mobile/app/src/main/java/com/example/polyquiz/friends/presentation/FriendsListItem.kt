package com.example.polyquiz.friends.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.example.polyquiz.auth.domain.UserIdName
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.polyquiz.constants.FriendsDisplayText
import com.example.polyquiz.ui.theme.AndroidGreen
import com.example.polyquiz.ui.theme.BrightRed
import com.example.polyquiz.ui.theme.HotPink
import com.example.polyquiz.ui.theme.LightGray
import com.example.polyquiz.constants.PresetAvatar

@Composable
fun FriendsListItem(
    user: UserIdName,
    isFriend: Boolean,
    isRequestPending: Boolean,
    isRequestSent: Boolean,
    isEligible: Boolean,
    onSendRequest: (String) -> Unit,
    onCancelRequest: (String) -> Unit,
    onAcceptRequest: (String) -> Unit,
    onRejectRequest: (String) -> Unit,
    onRemoveFriend: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            val painter = rememberAsyncImagePainter(
                ImageRequest.Builder(LocalContext.current)
                    .data(data = user.photoUrl ?: PresetAvatar.DEFAULT.value).apply(block = fun ImageRequest.Builder.() {
                        crossfade(true)
                    }).build()
            )
            Image(
                painter = painter,
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = user.name, style = MaterialTheme.typography.displaySmall)
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (user.isOnline == true) AndroidGreen else LightGray)
            )
        }
        when {
            isFriend -> {
                Row{
                    Button(onClick = {},
                        enabled = false,
                        colors = ButtonDefaults.buttonColors(containerColor = LightGray))
                    {
                        Text(FriendsDisplayText.FRIENDS.value)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Button(onClick = { onRemoveFriend(user.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = BrightRed)
                        ) {
                        Text(FriendsDisplayText.REMOVE_FRIEND.value)
                    }
                }

            }
            isRequestPending -> {
                Row {
                    Button(onClick = { onAcceptRequest(user.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = HotPink)
                    ) {
                        Text(FriendsDisplayText.ACCEPT_REQUEST.value)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Button(onClick = { onRejectRequest(user.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = BrightRed)
                    ) {
                        Text(FriendsDisplayText.REJECT_REQUEST.value)
                    }
                }
            }
            isRequestSent -> {
                Row {
                    Button(onClick = {},
                        enabled = false,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                    ) {
                        Text(FriendsDisplayText.REQUEST_SENT.value)

                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Button(onClick = { onCancelRequest(user.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = HotPink)
                    ) {
                        Text(FriendsDisplayText.CANCEL_REQUEST.value)
                    }
                }
            }
            isEligible -> {
                Button(onClick = { onSendRequest(user.id) }) {
                    Text(FriendsDisplayText.ADD_FRIEND.value)
                }
            }
        }
    }
}
