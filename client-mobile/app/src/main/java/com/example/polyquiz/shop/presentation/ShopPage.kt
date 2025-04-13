package com.example.polyquiz.shop.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.polyquiz.R
import com.example.polyquiz.auth.domain.AuthViewModel
import com.example.polyquiz.chat.presentation.ChatComponent
import com.example.polyquiz.shop.domain.ShopItem
import com.example.polyquiz.shop.domain.ShopViewModel
import com.example.polyquiz.ui.MenuButton

@Composable
fun ShopPage(
    modifier: Modifier,
    shopViewModel: ShopViewModel,
    authViewModel: AuthViewModel,
    currentUserID: String,
    navigateToLogin: () -> Unit,
    navigateToHome: () -> Unit,
    navigateToCreate: () -> Unit,
    navigateToUserEdit: () -> Unit,
    navigateToFriendsPage: () -> Unit,
    navigateToJoinRoom: () -> Unit,
    navigateToRankingsPage: () -> Unit,
    navigateToShop: () -> Unit
) {
    val avatarItems by shopViewModel.avatarItems.collectAsState()
    val themeItems by shopViewModel.themeItems.collectAsState()
    val wallpaperItems by shopViewModel.wallpaperItems.collectAsState()
    val isLoading by shopViewModel.isLoading.collectAsState()
    val dataInitialized by shopViewModel.dataInitialized.collectAsState()
    val currentBalance by shopViewModel.currentBalance.collectAsState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    LaunchedEffect(currentUserID) {
        shopViewModel.getCurrentBalance(currentUserID)
        shopViewModel.listenForMoneyEvents(context)
    }

    LaunchedEffect(Unit) {
        shopViewModel.initialize(authViewModel, context)
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

        if (isLoading || !dataInitialized) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return
        }

        Column(modifier = modifier.padding(26.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.buy_goodies),
                    modifier = Modifier.padding(horizontal = 26.dp),
                    style = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold)
                )
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center
                ) {
                    MenuButton(
                        modifier = Modifier,
                        navigateToHome,
                        navigateToCreate,
                        navigateToUserEdit,
                        navigateToFriendsPage,
                        navigateToJoinRoom,
                        navigateToRankingsPage,
                        navigateToShop,
                        signOut = {
                            authViewModel.signOut()
                            navigateToLogin()
                        },
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    BalanceCard(currentBalance)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                ShopSection(
                    title = stringResource(R.string.avatar_items),
                    items = avatarItems,
                    currentBalance = currentBalance,
                    onBuyClick = { shopViewModel.buyAvatar(it) }
                )
                ShopSection(
                    title = stringResource(R.string.theme_items),
                    items = themeItems,
                    currentBalance = currentBalance,
                    onBuyClick = { shopViewModel.buyTheme(it) }
                )
                ShopSection(
                    title = stringResource(R.string.wallpaper_items),
                    items = wallpaperItems,
                    currentBalance = currentBalance,
                    onBuyClick = { shopViewModel.buyWallpaper(it) }
                )
            }
        }
    }
}

@Composable
fun ShopSection(
    title: String,
    items: List<ShopItem>,
    currentBalance: Int,
    onBuyClick: (ShopItem) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            contentPadding = PaddingValues(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
        ) {
            items(items) { item ->
                ShopItemCard(
                    item = item,
                    currentBalance = currentBalance,
                    onBuyClick = onBuyClick
                )
            }
        }
    }
}

@Composable
fun ShopItemCard(
    item: ShopItem,
    currentBalance: Int,
    onBuyClick: (ShopItem) -> Unit
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp),
        ) {
            Image(
                painter = rememberAsyncImagePainter(item.imageUrl),
                contentDescription = item.id,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier
                .height(8.dp)
                .fillMaxWidth())

            Text(
                text = "$${item.price}",
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { onBuyClick(item) },
                enabled = !item.owned && item.price <= currentBalance,
                shape = RoundedCornerShape(0.dp)
            ) {
                Text(
                    text = if (item.owned)
                        stringResource(R.string.owned)
                    else
                        stringResource(R.string.buy)
                )
            }
            if (!item.owned && item.price > currentBalance) {
                Text(
                    text = stringResource(R.string.insufficient_funds),
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                Spacer(modifier = Modifier.height(24.dp))
            }

        }
    }
}

@Composable
fun BalanceCard(currentBalance: Int) {
    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.padding(horizontal = 26.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AccountBalanceWallet,
                contentDescription = stringResource(R.string.balance),
                tint = MaterialTheme.colorScheme.tertiary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$currentBalance$",
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
            )
        }
    }
}
