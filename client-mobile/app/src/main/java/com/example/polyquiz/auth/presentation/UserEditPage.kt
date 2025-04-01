package com.example.polyquiz.auth.presentation

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.polyquiz.R
import com.example.polyquiz.auth.domain.AuthViewModel
import com.example.polyquiz.auth.domain.HistoryService
import com.example.polyquiz.chat.presentation.ChatComponent
import com.example.polyquiz.constants.MatchStats
import com.example.polyquiz.constants.PresetAvatar
import com.example.polyquiz.constants.UserHistoryInfo
import com.example.polyquiz.core.ThemeService
import com.example.polyquiz.ui.features.camera.CameraViewModel
import com.example.polyquiz.core.TranslationService
import com.example.polyquiz.ui.theme.Theme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserEditPage(
    modifier: Modifier,
    navigateToHome: () -> Unit,
    navigateToCamera: () -> Unit,
    navigateToLogin: () -> Unit,
    authViewModel: AuthViewModel,
    context: android.content.Context,
    cameraViewModel: CameraViewModel,
    currentTheme: Theme,
    onThemeUpdated: (Theme) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val translationService = TranslationService
    var currentLang by remember { mutableStateOf(Locale.getDefault().language) }
    var theme by remember { mutableStateOf(currentTheme) }
    val email by authViewModel.email.collectAsState()
    var username by remember { mutableStateOf(authViewModel.getUsername()) }
    val usernameError by authViewModel.usernameError.collectAsState()
    var expandedLang by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val availableThemes = mapOf(
        Theme.LIGHT to stringResource(R.string.light_theme),
        Theme.DARK to stringResource(R.string.dark_theme)
    )
    val availableLangs = mapOf("en" to stringResource(R.string.english), "fr" to stringResource(R.string.french))
    var userHistory by remember { mutableStateOf(
        UserHistoryInfo(
            auth = listOf(),
            match = listOf(),
            stats = MatchStats(
                nMatchesPlayed = 0,
                nMatchesWon = 0,
                averageGoodAnswersPercentage = 0,
                averageTime = 0
            ),
            intensityGrid = listOf()
        )
    ) }
    LaunchedEffect(authViewModel.getUserId()) {
        authViewModel.getUserId().let { uid ->
            HistoryService.getHistoryById(uid, onSuccess = { history ->
                userHistory = history
            }, onError = { error ->
                Log.e("UserEditPage", "Error loading history: $error")
            })
        }
    }
    val textFieldStateLang = rememberTextFieldState(currentLang)
    val avatarURL by authViewModel.avatarURL.collectAsState()
    val isPresetAvatar by cameraViewModel.isPresetAvatar.collectAsState()
    val temporaryAvatar by cameraViewModel.temporaryAvatar.collectAsState()
    val avatarToShow = temporaryAvatar ?: avatarURL
    var initialAvatarURL by remember { mutableStateOf(authViewModel.getAvatarURL()) }
    var initialUsername by remember { mutableStateOf(authViewModel.getUsername()) }
    val initialLang by remember { mutableStateOf(Locale.getDefault().language) }
    val onClickAvatar: (String) -> Unit = { url ->
        cameraViewModel.setPresetAvatar(authViewModel, url)
        Log.d("Save UserProfile", "Initial URL : $initialAvatarURL, new url: $url")
    }
    val onClickTheme: (Theme) -> Unit = { selectedTheme ->
        theme = selectedTheme
        Log.d("Theme changer", "Selected $theme")
    }
    DisposableEffect(Unit) {
        onDispose {
            authViewModel.setProfileUpdated(false)
            authViewModel.resetUsername()
            cameraViewModel.resetCapturedPhotoState()
        }
    }
    fun deleteUser() {
        authViewModel.deleteUser()
        authViewModel.resetSignUpFields()
        navigateToLogin()
    }
    fun saveUserProfile(): Boolean {
        var usernameUpdate: String = ""
        var avatarURLUpdate: String = ""
        var isUpdated = false
        keyboardController?.hide()
        if (initialUsername != username) {
            usernameUpdate = username
            initialUsername = username
            isUpdated = true
        } else {
            Log.d("Save UserProfile", "Username has not changed.")
        }
        val capturedImage = cameraViewModel.state.value.capturedImage
        if (!isPresetAvatar && capturedImage != null) {
            Log.d("UserEditPage", "Saving new stuff")
            cameraViewModel.saveCapturedImage(
                capturedImage,
                authViewModel.getUserId(),
                authViewModel
            ) { newAvatarUrl ->
                if (newAvatarUrl != null) {
                    avatarURLUpdate = newAvatarUrl
                    initialAvatarURL = newAvatarUrl
                    isUpdated = true
                } else {
                    Log.e("Save UserProfile", "Failed to save image. URL was null")
                }
            }
        } else if (initialAvatarURL != authViewModel.getAvatarURL()) {
            Log.d("Save UserProfile", "Using preset avatar")
            val newAvatarUrl = authViewModel.getAvatarURL()
            avatarURLUpdate = newAvatarUrl
            initialAvatarURL = newAvatarUrl
            isUpdated = true
        } else {
            Log.d("Save UserProfile", "Avatar has not changed")
        }
        authViewModel.updateUserProfile(avatarURLUpdate, usernameUpdate)
        if (currentTheme != theme) {
            isUpdated = true
            onThemeUpdated(theme)
            ThemeService.saveThemeToDB(theme, authViewModel.getUserConfigsDatabaseRef())
        } else {
            Log.d("Save UserProfile", "Theme was not changed")
        }
        if (initialLang != currentLang) {
            isUpdated = true
            translationService.setLanguage(currentLang)
            translationService.saveLanguageToDB(currentLang, authViewModel.getUserConfigsDatabaseRef())
        } else {
            Log.d("Save UserProfile", "Lang was not changed")
        }
        if (isUpdated) {
            authViewModel.setProfileUpdated(isUpdated)
        }
        return isUpdated
    }
    val historyData = userHistory
    Button(
        onClick = { navigateToHome() },
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text(text = stringResource(R.string.home_page))
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(26.dp),
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
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Button(
                    onClick = { navigateToHome() },
                    colors = ButtonDefaults.buttonColors(
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
                ElevatedCard(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(32.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .padding(start = 40.dp, top = 24.dp, end = 40.dp, bottom = 16.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (avatarToShow is Bitmap) {
                                TemporaryAvatar(128.dp, avatarToShow)
                                Log.d("UserEditPage", "Showing temp avatar")
                            } else {
                                if (avatarURL.isNotEmpty()) {
                                    AvatarPlaceholder(128.dp, avatarURL)
                                    Log.d("UserEditPage", "Showing avatar from url : $avatarURL")
                                } else {
                                    AvatarPlaceholder(128.dp, PresetAvatar.DEFAULT.value)
                                }
                            }
                            Button(
                                onClick = { navigateToCamera() }
                            ) { Text(stringResource(R.string.upload_avatar)) }
                            Text(stringResource(R.string.preset_avatars))
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                ClickableAvatarPlaceholder(32.dp, PresetAvatar.A.value, onClickAvatar)
                                ClickableAvatarPlaceholder(32.dp, PresetAvatar.B.value, onClickAvatar)
                                ClickableAvatarPlaceholder(32.dp, PresetAvatar.C.value, onClickAvatar)
                                ClickableAvatarPlaceholder(32.dp, PresetAvatar.D.value, onClickAvatar)
                                ClickableAvatarPlaceholder(32.dp, PresetAvatar.DEFAULT.value, onClickAvatar)
                            }
                        }
                        Column {
                            TextField(
                                value = email,
                                onValueChange = { },
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
                            ThemeDropdown(context, availableThemes, currentTheme, onClickTheme)
                            Spacer(modifier = Modifier.height(8.dp))
                            ExposedDropdownMenuBox(
                                expanded = expandedLang,
                                onExpandedChange = { expandedLang = it }
                            ) {
                                TextField(
                                    value = availableLangs[currentLang].toString(),
                                    modifier = Modifier
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                        .fillMaxWidth(),
                                    label = { Text(stringResource(R.string.language)) },
                                    onValueChange = { },
                                    readOnly = true,
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLang)
                                    },
                                    colors = ExposedDropdownMenuDefaults.textFieldColors()
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedLang,
                                    onDismissRequest = { expandedLang = false }
                                ) {
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
                                                textFieldStateLang.setTextAndPlaceCursorAtEnd(language)
                                                currentLang = language
                                            },
                                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { saveUserProfile() },
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
                    stringResource(R.string.danger_zone),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Button(
                    onClick = { deleteUser() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text(text = stringResource(R.string.delete_user))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.statistics),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
                ElevatedCard(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    ),
                    modifier = Modifier
                        .width(700.dp)
                        .fillMaxWidth()
                        .padding(8.dp) ,

                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier
                                .padding(
                                    start = 20.dp,
                                    end = 250.dp
                                )
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = stringResource(R.string.matches_played))
                            Text(text = historyData.stats.nMatchesPlayed.toString())
                        }
                        Row(
                            modifier = Modifier
                                .padding(
                                    start = 20.dp,
                                    end = 250.dp
                                )
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = stringResource(R.string.matches_won))
                            Text(text = historyData.stats.nMatchesWon.toString())
                        }
                        Row(
                            modifier = Modifier
                                .padding(
                                    start = 20.dp,
                                    end = 250.dp
                                )
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = stringResource(R.string.avg_good_answers_percentage) )
                            Text(text = "${historyData.stats.averageGoodAnswersPercentage} %")
                        }
                        Row(
                            modifier = Modifier
                                .padding(
                                    start = 20.dp,
                                    end = 250.dp
                                )
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = stringResource(R.string.avg_time))
                            Text(text = "${historyData.stats.averageTime} s")
                        }
                    }
                }
                Log.d("UserEditPage", "intensityGrid = ${userHistory.intensityGrid}")
                Text(
                    text = stringResource(R.string.matches_year),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                IntensityGrid(historyData.intensityGrid)
                Text(
                    text = stringResource(R.string.match_history),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                if (historyData.match.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_items_to_display),
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.padding(8.dp)
                    )
                } else {
                    ElevatedCard(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFEBEDF0)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .height(40.dp)

                    ) {
                        Row(
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = stringResource(R.string.start))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = stringResource(R.string.end))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = stringResource(R.string.result))
                            Text(text = stringResource(R.string.gave_up))
                        }
                    }
                    historyData.match.forEach { item ->
                        ElevatedCard(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .height(40.dp)

                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically

                            ) {
                                Text(text = formatDateTime(item.start))
                                Text(text = formatDateTime(item.end))
                                Text(text = if (item.hasWon) stringResource(R.string.victory) else stringResource(R.string.defeat))
                                Text(text = if (item.hasGivenUp) "✔" else "-")
                            }
                        }
                    }
                }
                Text(
                    text = stringResource(R.string.auth_history),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                ElevatedCard(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFEBEDF0)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .height(40.dp)

                ) {
                    Row(
                        modifier = Modifier
                            .padding(
                                start = 20.dp,
                                end = 250.dp
                            )
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically

                    ) {
                        Text(text = stringResource(R.string.date))
                        Text(text = stringResource(R.string.action))
                    }
                }
                historyData.auth.forEach { item ->
                    ElevatedCard(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .height(40.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(
                                start = 20.dp,
                                end = 250.dp
                            )
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = formatDateTime(item.date))
                            Text(text = if (item.isLogin) stringResource(R.string.sign_in) else stringResource(R.string.sign_out))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeDropdown(
    context: android.content.Context,
    themes: Map<Theme, String>,
    currentTheme: Theme,
    onClick: (Theme) -> Unit
) {
    var expandedTheme by remember { mutableStateOf(false) }
    var selectedTheme by remember { mutableStateOf(currentTheme) }
    val textFieldStateTheme = rememberTextFieldState(currentTheme.toString())
    ExposedDropdownMenuBox(
        expanded = expandedTheme,
        onExpandedChange = { expandedTheme = it }
    ) {
        TextField(
            value = themes[selectedTheme].toString(),
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            label = { Text(stringResource(R.string.visual_themes)) },
            onValueChange = { },
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTheme)
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors()
        )
        ExposedDropdownMenu(
            expanded = expandedTheme,
            onDismissRequest = { expandedTheme = false }
        ) {
            themes.keys.forEach { theme ->
                DropdownMenuItem(
                    text = {
                        Text(
                            theme.displayName.asString(context),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    onClick = {
                        onClick(theme)
                        selectedTheme = theme
                        textFieldStateTheme.setTextAndPlaceCursorAtEnd(theme.displayName.asString(context))
                        expandedTheme = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
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
            .border(width = 2.dp, color = MaterialTheme.colorScheme.primary, shape = CircleShape)
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "placeholder avatar",
                modifier = Modifier.fillMaxSize().clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun ClickableAvatarPlaceholder(avatarSize: Dp, imageUrl: String, onClickAvatar: (String) -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(avatarSize)
            .border(width = 2.dp, color = MaterialTheme.colorScheme.primary, shape = CircleShape)
            .clickable { onClickAvatar(imageUrl) }
    ) {
        AsyncImage(
            model = imageUrl,
            contentScale = ContentScale.Crop,
            contentDescription = stringResource(R.string.preset_avatars),
            modifier = Modifier.clip(CircleShape)
        )
    }
}

fun formatDateTime(timestamp: Date): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(timestamp)
}

