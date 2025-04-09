package com.example.polyquiz.auth.presentation

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.AutoFixNormal
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.polyquiz.R
import com.example.polyquiz.auth.domain.AuthViewModel
import com.example.polyquiz.auth.domain.HistoryService
import com.example.polyquiz.auth.domain.UsernameSuggestionService
import com.example.polyquiz.auth.domain.UsernameSuggestionService.showUsernameDialog
import com.example.polyquiz.chat.presentation.ChatComponent
import com.example.polyquiz.constants.MatchStats
import com.example.polyquiz.constants.PremiumAvatar
import com.example.polyquiz.constants.PresetAvatar
import com.example.polyquiz.constants.SIZE_CONSTANTS
import com.example.polyquiz.constants.UserHistoryInfo
import com.example.polyquiz.constants.Wallpaper
import com.example.polyquiz.core.ThemeService
import com.example.polyquiz.ui.features.camera.CameraViewModel
import com.example.polyquiz.core.TranslationService
import com.example.polyquiz.shop.domain.PremiumAvatarService
import com.example.polyquiz.shop.domain.WallpaperService
import com.example.polyquiz.ui.MenuButton
import com.example.polyquiz.ui.theme.Theme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserEditPage(
    modifier: Modifier,
    context: Context,
    authViewModel: AuthViewModel,
    cameraViewModel: CameraViewModel,
    currentTheme: Theme,
    onThemeUpdated: (Theme) -> Unit,
    navigateToHome: () -> Unit,
    navigateToCreate: () -> Unit,
    navigateToUserEdit: () -> Unit,
    navigateToFriendsPage: () -> Unit,
    navigateToJoinRoom: () -> Unit,
    navigateToLogin: () -> Unit,
    navigateToCamera: () -> Unit,
    navigateToRankingsPage: () -> Unit,
    navigateToShopPage: () -> Unit
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
        Theme.DARK to stringResource(R.string.dark_theme),
        Theme.LUIGI to stringResource(R.string.luigi_theme),
        Theme.MARIO to stringResource(R.string.mario_theme),
        Theme.SONIC to stringResource(R.string.sonic_theme),
        Theme.PIKACHU to stringResource(R.string.pikachu_theme)
    )
    val availableLangs =
        mapOf("en" to stringResource(R.string.english), "fr" to stringResource(R.string.french))
    var userHistory by remember {
        mutableStateOf(
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
        )
    }
    val historyData = userHistory
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
    var initialLang by remember { mutableStateOf(Locale.getDefault().language) }

    val openDeleteDialog = remember { mutableStateOf(false) }

    val onClickAvatar: (String) -> Unit = { url ->
        cameraViewModel.setPresetAvatar(authViewModel, url)
        Log.d("Save UserProfile", "Initial URL : $initialAvatarURL, new url: $url")
    }
    val onClickTheme: (Theme) -> Unit = { selectedTheme ->
        theme = selectedTheme
        Log.d("Theme changer", "Selected $theme")
    }
    val purchasedAvatars by PremiumAvatarService.purchasedAvatars.collectAsState()
    val purchasedWallpapers by WallpaperService.purchasedWallpapers.collectAsState()
    val currentWallpaper by WallpaperService.currentWallpaper.collectAsState()
    val purchasedThemes by ThemeService.purchasedThemes.collectAsState()
    val availablePremiumThemes = remember(purchasedThemes) {
        ThemeService.getPremiumThemes().filter { theme ->
            purchasedThemes.contains(ThemeService.themeToString(theme))
        }
    }

    LaunchedEffect(Unit) {
        WallpaperService.initialize(authViewModel)
        PremiumAvatarService.initialize(authViewModel)
        ThemeService.loadPurchasedThemes(authViewModel)
    }

    val scope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        onDispose {
            authViewModel.setProfileUpdated(false)
            authViewModel.resetUsername()
            cameraViewModel.resetCapturedPhotoState()
        }
    }
    fun deleteUser() {
        authViewModel.resetSignUpFields()
        authViewModel.deleteUser()
        navigateToLogin()
    }

    @Composable
    fun openUsernameDialog() {
        if (showUsernameDialog) {
            UsernameSuggestionDialog(
                onDismiss = { showUsernameDialog = false },
                onUsernameSelected = { newUsername ->
                    username = newUsername
                    showUsernameDialog = false
                    authViewModel.updateUsername(
                        newUsername,
                        context
                    )
                }

            )
        }
    }

    fun saveUserProfile() {
        // To hide the keyboard in case it's open
        keyboardController?.hide()

        // Equivalent to invalid form
        if (usernameError.isNotEmpty()) {
            Log.e("Update profile", "Username is invalid")
            return
        }

        val isSameUsername = (username.lowercase() == authViewModel.getUsername().lowercase())


        // Update avatar image
        val capturedImage = cameraViewModel.state.value.capturedImage
        if (!isPresetAvatar && capturedImage != null) {
            cameraViewModel.saveCapturedImage(
                capturedImage,
                authViewModel.getUserId(),
                authViewModel
            ) { newAvatarUrl ->
                if (newAvatarUrl != null) {
                    Log.d("Update profile", "Camera Avatar : $newAvatarUrl")
                    initialAvatarURL = newAvatarUrl
                    authViewModel.updateAvatar(newAvatarUrl)
                } else {
                    Log.e("Update profile", "Failed to save image. URL was null")
                }
            }
        } else if (initialAvatarURL != authViewModel.getAvatarURL()) {
            val newAvatarUrl = authViewModel.getAvatarURL()
            initialAvatarURL = newAvatarUrl
            authViewModel.updateAvatar(newAvatarUrl)
            Log.d("Update profile", "Saved preset avatar : $newAvatarUrl")

        } else {
            Log.d("Update profile", "Avatar has not changed")
        }


        if (!isSameUsername) {
            Log.d("Update profile", "Updating username to : $username")
            authViewModel.updateUsername(username) { updateUsernameTask ->
                if (updateUsernameTask) {
                    authViewModel.emitUpdates()
                } else {
                    Log.e("Update profile", "An error has occured when updating username")
                }
            }
        }

        // Change app theme
        if (currentTheme != theme) {
            onThemeUpdated(theme)
            ThemeService.saveThemeToDB(theme, authViewModel.getUserConfigsDatabaseRef())
        } else {
            Log.d("Update profile", "Theme was not changed")
        }

        // Change app language
        if (initialLang != currentLang) {
            translationService.setLanguage(currentLang)
            translationService.saveLanguageToDB(
                currentLang,
                authViewModel.getUserConfigsDatabaseRef()
            )
        } else {
            Log.d("Update profile", "Lang was not changed")
        }

        authViewModel.setProfileUpdated(true)
    }

    // Delete dialog
    when {
        openDeleteDialog.value -> {
            DeleteDialog(
                onDismissRequest = { openDeleteDialog.value = false },
                onConfirmation = { deleteUser() },
                dialogTitle = stringResource(R.string.warning),
                dialogText = stringResource(R.string.dialog_warning),
                icon = Icons.Default.Warning,
                contentDescription = stringResource(R.string.delete_user)
            )
        }
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
            .statusBarsPadding()
    ) {
        ChatComponent(modifier = modifier, authViewModel = authViewModel)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        stringResource(R.string.edit_profile),
                        fontSize = 35.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    MenuButton(
                        modifier = Modifier,
                        navigateToHome,
                        navigateToCreate,
                        navigateToUserEdit,
                        navigateToFriendsPage,
                        navigateToJoinRoom,
                        navigateToRankingsPage,
                        navigateToShopPage,
                        signOut = {
                            authViewModel.signOut()
                            navigateToLogin()
                        }
                    )
                }

                ElevatedCard(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(32.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .padding(
                                start = 40.dp, top = 24.dp, end = 40.dp, bottom = 16.dp
                            )
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
                                    Log.d(
                                        "UserEditPage",
                                        "Showing avatar from url : $avatarURL"
                                    )
                                } else {
                                    AvatarPlaceholder(128.dp, PresetAvatar.DEFAULT.value)
                                }
                            }
                            Button(
                                onClick = {
                                    cameraViewModel.setCameraContent(false)
                                    navigateToCamera()
                                }, shape = RoundedCornerShape(3.dp)
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
                            if (purchasedAvatars.isNotEmpty()) {
                                Text(stringResource(R.string.avatar_items))
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    purchasedAvatars.forEach { avatarId ->
                                        val premiumAvatar = PremiumAvatar.entries.find { it.name == avatarId }
                                        if (premiumAvatar != null) {
                                            ClickableAvatarPlaceholder(
                                                32.dp,
                                                premiumAvatar.value,
                                                onClickAvatar
                                            )
                                        }
                                    }
                                }
                            }
                            // Add wallpapers section
                            Text(stringResource(R.string.wallpaper_items))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                // No background option
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(width = 80.dp, height = 50.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color.LightGray)
                                        .border(
                                            width = if (currentWallpaper == Wallpaper.None.value) 2.dp else 0.dp,
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .clickable {
                                            scope.launch {
                                                WallpaperService.setWallpaper(authViewModel, Wallpaper.None.value)
                                            }
                                        }
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.Block,
                                            contentDescription = "No background",
                                            tint = Color.Gray
                                        )
                                        Text(
                                            text = stringResource(R.string.no_background),
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }

                                // Purchased wallpapers
                                purchasedWallpapers.forEach { wallpaperId ->
                                    val wallpaper = Wallpaper.values().find { it.name == wallpaperId }
                                    if (wallpaper != null && wallpaper != Wallpaper.None) {
                                        Box(
                                            modifier = Modifier
                                                .size(width = 80.dp, height = 50.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .border(
                                                    width = if (currentWallpaper == wallpaper.value) 2.dp else 0.dp,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    shape = RoundedCornerShape(4.dp)
                                                )
                                                .clickable {
                                                    scope.launch {
                                                        WallpaperService.setWallpaper(authViewModel, wallpaper.value)
                                                    }
                                                }
                                        ) {
                                            AsyncImage(
                                                model = wallpaper.value,
                                                contentDescription = "Wallpaper",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                                }
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
                                    if (it.length <= SIZE_CONSTANTS.MAX_USERNAME_LENGTH) {
                                        username = it
                                        authViewModel.updateUsername(
                                            it,
                                            context
                                        )
                                    }
                                },
                                isError = usernameError.isNotEmpty(),
                                singleLine = true,
                                label = { Text(stringResource(R.string.username)) },
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = {
                                    if (UsernameSuggestionService.showUsernameDialog) {
                                        UsernameSuggestionDialog(
                                            onDismiss = {
                                                UsernameSuggestionService.showUsernameDialog = false
                                            },
                                            onUsernameSelected = { selectedUsername ->
                                                username = selectedUsername;
                                                authViewModel.updateUsername(
                                                    selectedUsername,
                                                    context,
                                                )
                                                UsernameSuggestionService.showUsernameDialog = false
                                            }
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            UsernameSuggestionService.showUsernameDialog =
                                                !UsernameSuggestionService.showUsernameDialog
                                        },

                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                    )
                                    {
                                        Icon(
                                            imageVector = Icons.Default.AutoFixNormal,
                                            contentDescription = null
                                        )
                                    }

                                }
                            )
                            if (usernameError.isNotEmpty()) {
                                Text(text = usernameError, color = Color.Red)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            ThemeDropdown(context, availableThemes, availablePremiumThemes, currentTheme, onClickTheme)
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
                                        ExposedDropdownMenuDefaults.TrailingIcon(
                                            expanded = expandedLang
                                        )
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
                                                textFieldStateLang.setTextAndPlaceCursorAtEnd(
                                                    language
                                                )
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
                                modifier = Modifier.width(200.dp),
                                shape = RoundedCornerShape(3.dp)
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
                    onClick = {
                        openDeleteDialog.value = !openDeleteDialog.value
                    },
                    shape = RoundedCornerShape(3.dp),
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
                        .padding(8.dp),

                    ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier
                                .padding(
                                    start = 20.dp,
                                    end = 150.dp
                                )
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,

                            ) {
                            Text(text = stringResource(R.string.matches_played))
                            Text(text = historyData.stats.nMatchesPlayed.toString())
                        }
                        Row(
                            modifier = Modifier
                                .padding(
                                    start = 20.dp,
                                    end = 150.dp
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
                                    end = 150.dp
                                )
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = stringResource(R.string.avg_good_answers_percentage))
                            Text(text = "${historyData.stats.averageGoodAnswersPercentage} %")
                        }
                        Row(
                            modifier = Modifier
                                .padding(
                                    start = 20.dp,
                                    end = 150.dp
                                )
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = stringResource(R.string.avg_time))
                            Text(text = "${historyData.stats.averageTime} s")
                        }
                    }
                }
                Text(
                    text = stringResource(R.string.matches_year),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                IntensityGrid(historyData.intensityGrid)
                ElevatedCard(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp, end = 26.dp)
                        .height(40.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.start),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = stringResource(R.string.end),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = stringResource(R.string.result),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = stringResource(R.string.gave_up),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                historyData.match.forEach { item ->
                    ElevatedCard(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp, end = 26.dp)
                            .height(40.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formatDateTime(item.start),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = formatDateTime(item.end),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = if (item.hasWon) stringResource(R.string.victory)
                                else stringResource(R.string.defeat),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = if (item.hasGivenUp) "✔" else "-",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                }
                Text(
                    text = stringResource(R.string.auth_history),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                ElevatedCard(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp)
                        .height(40.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.date),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = stringResource(R.string.action),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                historyData.auth.forEach { item ->
                    ElevatedCard(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp)
                            .height(40.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formatDateTime(item.date),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = if (item.isLogin) stringResource(R.string.sign_in) else stringResource(
                                    R.string.sign_out
                                ),
                                modifier = Modifier.weight(1f)
                            )
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
    premiumThemes: List<Theme>,
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
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expandedTheme
                )
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors()
        )
        ExposedDropdownMenu(
            expanded = expandedTheme,
            onDismissRequest = { expandedTheme = false }
        ) {
            themes.keys.forEach { theme ->
                if (theme == Theme.LIGHT || theme == Theme.DARK) {
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
                            textFieldStateTheme.setTextAndPlaceCursorAtEnd(
                                theme.displayName.asString(
                                    context
                                )
                            )
                            expandedTheme = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
            if (premiumThemes.isNotEmpty()) {
                DropdownMenuText(
                    text = stringResource(R.string.premium_themes),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.outline
                    )
                )

                premiumThemes.forEach { theme ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    theme.displayName.asString(context),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("👑", fontSize = 14.sp)
                            }
                        },
                        onClick = {
                            onClick(theme)
                            selectedTheme = theme
                            expandedTheme = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
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
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "placeholder avatar",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun ClickableAvatarPlaceholder(
    avatarSize: Dp,
    imageUrl: String,
    onClickAvatar: (String) -> Unit
) {
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

fun formatDateTime(timestamp: Date): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(timestamp)
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
    icon: ImageVector,
    contentDescription: String
) {
    AlertDialog(
        icon = {
            Icon(icon, contentDescription, tint = MaterialTheme.colorScheme.error)
        },
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Text(text = dialogText)
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                },
                shape = RoundedCornerShape(3.dp)
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                },
                shape = RoundedCornerShape(3.dp)
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenuText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium
) {
    Text(
        text = text,
        modifier = modifier.padding(
            vertical = 8.dp
        ),
        style = style
    )
}

