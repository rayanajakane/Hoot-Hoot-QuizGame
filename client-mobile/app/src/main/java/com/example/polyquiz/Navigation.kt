package com.example.polyquiz

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.polyquiz.auth.AuthViewModel
import com.example.polyquiz.constants.Route

// References: https://youtu.be/AIC_OFQ1r3k  and  https://youtu.be/lv1raAvwcgI
@Composable
fun Navigation(modifier: Modifier, authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Route.Login
    ) {
        composable<Route.Login> {
            LoginPage(
                modifier = modifier,
                navigateToChat = {
                    navController.navigate(Route.Chat)
                },
                navigateToSignup = {
                    navController.navigate(Route.Signup)
                }
            )
        }
        composable<Route.Signup> {
            SignupPage(
                modifier = modifier,
                navigateToLogin = {
                    navController.navigate(Route.Login)
                },
                navigateToChat = {
                    navController.navigate(Route.Chat)
                },
                authViewModel = authViewModel
            )
        }
        composable<Route.Chat> {
            ChatPage(modifier,
                navigateToLogin = {
                    navController.navigate(Route.Login)
                })
        }
    }
}
