package com.example.polyquiz.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.polyquiz.R

@Composable
fun MenuButton(
    modifier: Modifier,
    navigateToHome: () -> Unit,
    navigateToCreate: () -> Unit,
    navigateToUserEdit: () -> Unit,
    navigateToFriendsPage: () -> Unit,
    navigateToJoinRoom: () -> Unit,
    signOut: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = modifier.padding(horizontal = 26.dp)
    ) {
        ElevatedButton(
            onClick = {
                expanded = !expanded
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceBright,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            shape = RoundedCornerShape(3.dp)
        ) {
            Row(horizontalArrangement = Arrangement.Center) {
                Icon(Icons.Filled.Menu, contentDescription = "Menu")
                Text(text = "Menu")
            }

        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.home_page)) },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Home,
                        contentDescription = stringResource(R.string.home_page)
                    )
                },
                onClick = { navigateToHome() }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.join_match)) },
                leadingIcon = {
                    Icon(
                        Icons.Filled.PlayCircleFilled,
                        contentDescription = stringResource(R.string.join_match)
                    )
                },
                onClick = { navigateToJoinRoom() }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.host_match)) },
                leadingIcon = {
                    Icon(
                        Icons.Filled.AddCircle,
                        contentDescription = stringResource(R.string.host_match)
                    )
                },
                onClick = { navigateToCreate() }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.edit_profile)) },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = stringResource(R.string.edit_profile)
                    )
                },
                onClick = { navigateToUserEdit() }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.friends)) },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Group,
                        contentDescription = stringResource(R.string.friends)
                    )
                },
                onClick = { navigateToFriendsPage() }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.logout_action)) },
                leadingIcon = {
                    Icon(
                        Icons.AutoMirrored.Filled.Logout,
                        contentDescription = stringResource(R.string.logout_action)
                    )
                },
                onClick = { signOut() }
            )
        }
    }

}
