package com.example.polyquiz.shop.presentation

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.polyquiz.R
import com.example.polyquiz.auth.domain.AuthViewModel
import com.example.polyquiz.money.domain.MoneyService
import com.example.polyquiz.shop.domain.ShopItem
import com.example.polyquiz.shop.domain.ShopViewModel
import com.example.polyquiz.ui.MenuButton

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ShopPage(
    authViewModel: AuthViewModel,
    currentUserID: String,
    moneyService: MoneyService,
    navigateToHome: () -> Unit,
    navigateToCreate: () -> Unit,
    navigateToUserEdit: () -> Unit,
    navigateToFriendsPage: () -> Unit,
    navigateToJoinRoom: () -> Unit,
    navigateToRankingsPage: () -> Unit,
) {
    val shopViewModel: ShopViewModel = viewModel()

    // State
    val avatarItems by shopViewModel.avatarItems.collectAsState()
    val themeItems by shopViewModel.themeItems.collectAsState()
    val wallpaperItems by shopViewModel.wallpaperItems.collectAsState()
    val isLoading by shopViewModel.isLoading.collectAsState()
    val currentBalance by moneyService.currentBalance.collectAsState()

    LaunchedEffect(currentUserID) {
        moneyService.getCurrentBalance(currentUserID)
        moneyService.listenForMoneyEvents()
    }

    // Load shop items when screen is shown
    LaunchedEffect(true) {
        shopViewModel.initialize(authViewModel)
    }

    Scaffold { paddingValues ->
        MenuButton(
            modifier = Modifier,
            navigateToHome,
            navigateToCreate,
            navigateToUserEdit,
            navigateToFriendsPage,
            navigateToJoinRoom,
            navigateToRankingsPage,
            signOut = { authViewModel.signOut() }
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            ElevatedButton(
                onClick = { navigateToHome() },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.home_page))
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = "Balance",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$${currentBalance}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Avatars section
                    ShopSection(
                        title = stringResource(R.string.avatar_items),
                        items = avatarItems,
                        onBuyClick = { shopViewModel.buyAvatar(it) }
                    )

                    // Themes section
                    ShopSection(
                        title = stringResource(R.string.theme_items),
                        items = themeItems,
                        onBuyClick = { shopViewModel.buyTheme(it) }
                    )

                    // Wallpapers section
                    ShopSection(
                        title = stringResource(R.string.wallpaper_items),
                        items = wallpaperItems,
                        onBuyClick = { shopViewModel.buyWallpaper(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun ShopSection(
    title: String,
    items: List<ShopItem>,
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
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
        ) {
            items(items) { item ->
                ShopItemCard(
                    item = item,
                    onBuyClick = onBuyClick
                )
            }
        }
    }
}

@Composable
fun ShopItemCard(
    item: ShopItem,
    onBuyClick: (ShopItem) -> Unit
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(item.imageUrl),
                contentDescription = item.id,
                modifier = Modifier
                    .height(100.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$${item.price}",
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { onBuyClick(item) },
                enabled = !item.owned,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (item.owned)
                        stringResource(R.string.owned)
                    else
                        stringResource(R.string.buy)
                )
            }
        }
    }
}
