package com.example.polyquiz.friends.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
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
                    .clip(CircleShape)
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
                        colors = ButtonDefaults.buttonColors(containerColor = LightGray)
                    ) {
                        Text(text = "Friends")
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Button(
                        onClick = { onRemoveFriend(user.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = BrightRed)
                    ) {
                        Text(text = "Remove Friend")
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Button(
                        onClick = { onDonate(user.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = HotPink)
                    ) {
                        Text(text = "Donate Money")
                    }
                }
            }
            isRequestPending -> {
                Row {
                    Button(
                        onClick = { onAcceptRequest(user.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = HotPink)
                    ) {
                        Text(text = "Accept")
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Button(
                        onClick = { onRejectRequest(user.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = BrightRed)
                    ) {
                        Text(text = "Decline")
                    }
                }
            }
            isRequestSent -> {
                Row {
                    Button(
                        onClick = { },
                        enabled = false,
                        colors = ButtonDefaults.buttonColors(containerColor = LightGray)
                    ) {
                        Text(text = "Request Sent")
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Button(
                        onClick = { onCancelRequest(user.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = HotPink)
                    ) {
                        Text(text = "Cancel")
                    }
                }
            }
            else -> {
                Button(onClick = { onSendRequest(user.id) }) {
                    Text(text = "Add Friend")
                }
            }
        }
    }
}
