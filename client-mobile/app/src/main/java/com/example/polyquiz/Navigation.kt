package com.example.polyquiz

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.polyquiz.auth.domain.AuthViewModel
import com.example.polyquiz.auth.presentation.LoginPage
import com.example.polyquiz.auth.presentation.SignupPage
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
                navigateToHome = {
                    navController.navigate(Route.Home)
                },
                navigateToSignup = {
                    navController.navigate(Route.Signup)
                },
                authViewModel = authViewModel
            )
        }
        composable<Route.Signup> {
            SignupPage(
                modifier = modifier,
                navigateToLogin = {
                    navController.navigate(Route.Login)
                },
                navigateToChat = {
                    navController.navigate(Route.Home)
                },
                authViewModel = authViewModel
            )
        }
        composable<Route.Home> {
            HomePage(modifier,
                navigateToLogin = {
                    navController.navigate(Route.Login)
                },
                navigateToCreate = {
                    navController.navigate(Route.MatchCreation)
                },
                authViewModel = authViewModel
            )
        }
        composable<Route.MatchCreation> {
            MatchCreationPage(modifier,
                navigateToLogin = {
                    navController.navigate(Route.Login)
                },
                navigateToHome = {
                    navController.navigate(Route.Home)
                },
                authViewModel = authViewModel
            )
        }
    }
}
