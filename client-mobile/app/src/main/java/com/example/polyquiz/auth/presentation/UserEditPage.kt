package com.example.polyquiz.auth.presentation

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import android.content.Context
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.polyquiz.R
import com.example.polyquiz.auth.domain.AuthViewModel
import com.example.polyquiz.chat.presentation.ChatComponent
import com.example.polyquiz.constants.PresetAvatar
import com.example.polyquiz.core.storage.ImageStorage
import com.example.polyquiz.ui.features.camera.CameraViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.example.polyquiz.core.TranslationService
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun UserEditPage(
    modifier: Modifier,
    navigateToHome: () -> Unit,
    navigateToCamera: () -> Unit,
    authViewModel: AuthViewModel,
    context: Context,
    cameraViewModel: CameraViewModel
) {

    val focusManager = LocalFocusManager.current
    val translationService = TranslationService

    var currentLang by remember { mutableStateOf(Locale.getDefault().language) }

    val email by authViewModel.email.collectAsState()
    var username by remember { mutableStateOf(authViewModel.getUsername()) }
    val usernameError by authViewModel.usernameError.collectAsState()

    var expandedTheme by remember { mutableStateOf(false) }
    var expandedLang by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    val availableLangs =
        mapOf("en" to stringResource(R.string.english), "fr" to stringResource(R.string.french))

    val themes = listOf("light theme", "dark theme")
    val textFieldStateLang = rememberTextFieldState(currentLang)
    val textFieldStateTheme = rememberTextFieldState(themes[0])

    // TODO : Cleanup function
    DisposableEffect(Unit) {
        onDispose {
            authViewModel.resetUsername()
            cameraViewModel.resetCapturedPhotoState()
        }
    }
    val avatarURL by authViewModel.avatarURL.collectAsState()
    val isPresetAvatar by cameraViewModel.isPresetAvatar.collectAsState()
    val temporaryAvatar by cameraViewModel.temporaryAvatar.collectAsState()
    val avatarToShow = temporaryAvatar ?: avatarURL

    val onClickAvatar: (String) -> Unit = { url ->
        cameraViewModel.setPresetAvatar(authViewModel, url)
    }

    fun saveUserProfile() {
        // TODO : save themes

        // To hide the keyboard in case it's open
        keyboardController?.hide()

        // Change app language
        translationService.setLanguage(currentLang)
        translationService.saveLanguageToDB(currentLang, authViewModel.getUserConfigsDatabaseRef())

        // Change username
        if (authViewModel.getUsername() != username) {
            authViewModel.changeUsername(
                username, authViewModel.getUsername()
            )
        } else {
            Log.e("caca", "CACA")
        }

        // Save avatar image + url
        val capturedImage = cameraViewModel.state.value.capturedImage
        if (!isPresetAvatar && capturedImage != null) {
            Log.d("UserEditPage", "Saving new stuff")
            cameraViewModel.saveCapturedImage(
                capturedImage,
                authViewModel.getUserId(),
                authViewModel
            ) { newAvatarUrl ->
                if (newAvatarUrl != null) {
                    authViewModel.updateUserProfile(newAvatarUrl)
                } else {
                    Log.e("Save user profile", "Failed to save image. URL was null")
                }
            }


        } else {
            Log.d("UserEditPage", "Setting preset avatar instead")
            val newAvatarUrl = authViewModel.getAvatarURL()
            authViewModel.updateUserProfile(newAvatarUrl)

            ImageStorage.deleteAvatar(authViewModel.getUserId())
        }
    }

    Button(
        onClick = {
            navigateToHome()
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text(text = stringResource(R.string.home_page))
    }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                })
            }
    ) {
        ChatComponent(modifier = modifier, authViewModel = authViewModel)
        Box(
            contentAlignment = Alignment.Center, modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            Column() {
                Button(
                    onClick = {
                        navigateToHome()
                    }, colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(imageVector = Icons.Default.Home, contentDescription = "Home")
                    Text(text = stringResource(R.string.home_page))
                }
                Text(
                    stringResource(R.string.edit_profile),
                    fontSize = 35.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                ElevatedCard(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(32.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .padding(
                                start = 40.dp, top = 24.dp, end = 40.dp, bottom = 16.dp
                            )
                    ) {
                        // Avatar stuff column
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // TODO : Fix clickable avatar
                            if (avatarToShow is Bitmap) {
                                TemporaryAvatar(128.dp, avatarToShow)
                            } else {
                                if (avatarURL != null) {
                                    AvatarPlaceholder(128.dp, avatarURL!!)
                                } else {
                                    AvatarPlaceholder(128.dp, PresetAvatar.DEFAULT.value)
                                }
                            }

                            Button(
                                onClick = {
                                    navigateToCamera()
                                },
                            ) { Text(stringResource(R.string.upload_avatar)) }
                            Text(stringResource(R.string.preset_avatars))
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                ClickableAvatarPlaceholder(
                                    32.dp,
                                    PresetAvatar.A.value,
                                    onClickAvatar
                                )
                                ClickableAvatarPlaceholder(
                                    32.dp,
                                    PresetAvatar.B.value,
                                    onClickAvatar
                                )
                                ClickableAvatarPlaceholder(
                                    32.dp,
                                    PresetAvatar.C.value,
                                    onClickAvatar
                                )
                                ClickableAvatarPlaceholder(
                                    32.dp,
                                    PresetAvatar.D.value,
                                    onClickAvatar
                                )
                                ClickableAvatarPlaceholder(
                                    32.dp,
                                    PresetAvatar.DEFAULT.value,
                                    onClickAvatar
                                )
                            }

                        }
                        // Form stuff column
                        Column() {
                            TextField(
                                value = email,
                                onValueChange = {
                                    //TODO
                                },
                                singleLine = true,
                                enabled = false,
                                label = { Text(stringResource(R.string.email)) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextField(
                                value = username,
                                onValueChange = {
                                    username = it
                                    authViewModel.updateUsername(it, context)
                                },
                                isError = usernameError.isNotEmpty(),
                                singleLine = true,
                                label = { Text(stringResource(R.string.username)) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (usernameError.isNotEmpty()) {
                                Text(text = usernameError, color = Color.Red)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            // REF : https://composables.com/material3/exposeddropdownmenubox
                            // Visual themes menu
                            ExposedDropdownMenuBox(
                                expanded = expandedTheme,
                                onExpandedChange = { expandedTheme = it },
                            ) {
                                TextField(
                                    value = "",
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    label = { Text(stringResource(R.string.visual_themes)) },
                                    onValueChange = {
                                        // TODO
                                    },
                                    readOnly = true,
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(
                                            expanded = expandedTheme
                                        )
                                    },
                                    colors = ExposedDropdownMenuDefaults.textFieldColors(),

                                    )
                                ExposedDropdownMenu(
                                    expanded = expandedTheme,
                                    onDismissRequest = { expandedTheme = false }) {
                                    themes.forEach { theme ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    theme,
                                                    style = MaterialTheme.typography.bodyLarge
                                                )
                                            },
                                            onClick = {
                                                textFieldStateTheme.setTextAndPlaceCursorAtEnd(theme)
                                                expandedTheme = false
                                            },
                                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            // Languages
                            // REF : https://github.com/android/user-interface-samples/blob/main/PerAppLanguages/compose_app/app/src/main/java/com/example/perapplanguages/MainActivity.kt
                            ExposedDropdownMenuBox(
                                expanded = expandedLang,
                                onExpandedChange = { expandedLang = it },
                            ) {
                                TextField(
                                    value = availableLangs[currentLang].toString(),
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    label = { Text(stringResource(R.string.language)) },
                                    onValueChange = {
                                        // TODO
                                    },
                                    readOnly = true,
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(
                                            expanded = expandedLang
                                        )
                                    },
                                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedLang,
                                    onDismissRequest = { expandedLang = false }) {
                                    availableLangs.keys.forEach { language ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    availableLangs[language].toString(),
                                                    style = MaterialTheme.typography.bodyLarge
                                                )
                                            },
                                            onClick = {
                                                expandedLang = false
                                                textFieldStateLang.setTextAndPlaceCursorAtEnd(
                                                    language
                                                )
                                                currentLang = language

                                            },
                                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    saveUserProfile()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.width(200.dp)
                            ) {
                                Text(text = stringResource(R.string.save))
                            }

                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    stringResource(R.string.danger_zone), fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Button(
                    onClick = {
                        // TODO
                    },
                    // TODO : Change color
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                ) {
                    Text(
                        text = stringResource(R.string.delete_user)
                    )
                }
            }
        }

    }
}

@Composable
fun TemporaryAvatar(avatarSize: Dp, bitmap: Bitmap?) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(avatarSize)
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            )
    ) {
        // TODO: Add image
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                // TODO : Add translation once merged
                contentDescription = "placeholder avatar",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
    }
}


@Composable()
fun ClickableAvatarPlaceholder(avatarSize: Dp, imageUrl: String, onClickAvatar: (String) -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(avatarSize)
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            )
            .clickable {
                onClickAvatar(imageUrl)
            }
    ) {
        AsyncImage(
            model = imageUrl,
            contentScale = ContentScale.Crop,
            contentDescription = stringResource(R.string.preset_avatars),
            modifier = Modifier.clip(CircleShape)
        )
    }
}
