package com.example.polyquiz


import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier

import androidx.compose.ui.tooling.preview.Preview

import com.example.polyquiz.auth.domain.AuthViewModel
import com.example.polyquiz.shop.domain.ShopViewModel
import com.example.polyquiz.ui.features.camera.CameraViewModel
import com.example.polyquiz.ui.theme.PolyQuizTheme
import com.example.polyquiz.ui.theme.Theme

import com.example.vanillaprototype.socket.SocketHandler

import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        SocketHandler.setSocket()
        val context  = applicationContext
        val authViewModel: AuthViewModel by viewModels()
        val cameraViewModel: CameraViewModel by viewModels()
        val shopViewModel: ShopViewModel by viewModels()
        shopViewModel.listenForMoneyEvents(context)
        setContent {
            val currentTheme by authViewModel.theme.collectAsState()
            val setTheme: (Theme) -> Unit = { selectedTheme ->
                authViewModel.setTheme(selectedTheme)
            }
            PolyQuizTheme(currentTheme) {
                val snackbarHostState = remember {
                    SnackbarHostState()
                }
                val scope = rememberCoroutineScope()

                ObserveAsEvents(flow = SnackbarController.events, snackbarHostState) { event ->
                    scope.launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        val result = snackbarHostState.showSnackbar(
                            message = event.message.asString(context = this@MainActivity),
                            actionLabel = event.action?.name,
                            duration = SnackbarDuration.Short,
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            event.action?.action?.invoke()
                        }
                    }
                }

                Scaffold(
                    snackbarHost = {
                        SnackbarHost(
                            hostState = snackbarHostState
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                )
                { innerPadding ->
                    Navigation(
                        modifier = Modifier.padding(innerPadding),
                        authViewModel = authViewModel,
                        cameraViewModel = cameraViewModel,
                        context = applicationContext,
                        currentTheme = currentTheme,
                        onThemeUpdated = setTheme,
                        shopViewModel = shopViewModel
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PolyQuizTheme(currentTheme = Theme.DARK) {
        // Can be used to preview composable
    }
}
