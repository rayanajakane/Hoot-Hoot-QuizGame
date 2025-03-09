package com.example.polyquiz

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.polyquiz.auth.domain.AuthViewModel
import com.example.polyquiz.auth.presentation.ForgotPasswordPage
import com.example.polyquiz.auth.presentation.LoginPage
import com.example.polyquiz.auth.presentation.ForgotPasswordFeedbackPage
import com.example.polyquiz.auth.presentation.SignupPage
import com.example.polyquiz.auth.presentation.UserEditPage
import com.example.polyquiz.constants.MatchContext
import com.example.polyquiz.match.domain.TimeService
import com.example.polyquiz.constants.Route
import com.example.polyquiz.match.domain.AnswerService
import com.example.polyquiz.match.domain.MatchContextService
import com.example.polyquiz.match.domain.MatchRoomService
import com.example.polyquiz.match.presentation.QuestionArea
import com.example.polyquiz.pages.presentation.WaitPage
import com.example.polyquiz.utils.LanguageChangeHelper

// References: https://youtu.be/AIC_OFQ1r3k  and  https://youtu.be/lv1raAvwcgI
@Composable
fun Navigation(modifier: Modifier, authViewModel: AuthViewModel, context : Context) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Route.Login
    ) {
        composable<Route.WaitPage> {
            WaitPage(
                modifier = modifier,
                authViewModel = authViewModel,
                navigateToHome = {
                    navController.navigate(Route.Home)
                },
                navigateToMatchRoom = {
                    navController.navigate(Route.MatchRoom)
                }
            )
        }
        composable<Route.Login> {
            LoginPage(
                modifier = modifier,
                navigateToHome = {
                    navController.navigate(Route.Home)
                },
                navigateToSignup = {
                    navController.navigate(Route.Signup)
                },
                navigateToForgotPassword = {
                    navController.navigate(Route.ForgotPassword)
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
        composable<Route.MatchRoom> {
            //val matchContextService = MatchContextService()
            //matchContextService.setContext(MatchContext.PLAYERVIEW)
            QuestionArea(
                modifier = modifier,
                navigateToHome = {
                    navController.navigate(Route.Home)
                },
                authViewModel = authViewModel,
                matchContextService = MatchContextService,
                matchRoomService = MatchRoomService,
                timeService = TimeService,
                answerService = AnswerService
            )
        }
        composable<Route.Home> {
            HomePage(
                modifier,
                navigateToLogin = {
                    navController.navigate(Route.Login)
                },
                navigateToCreate = {
                    navController.navigate(Route.MatchCreation)
                },
                authViewModel = authViewModel,
                navigateToHome = {
                    navController.navigate(Route.Home)
                },
                navigateToUserEdit = {
                    navController.navigate(Route.UserEditPage)
                },
                navigateToWaitPage = {
                    navController.navigate(Route.WaitPage)
                }
            )
        }
        composable<Route.MatchCreation> {
            MatchCreationPage(
                modifier,
                navigateToLogin = {
                    navController.navigate(Route.Login)
                },
                navigateToHome = {
                    navController.navigate(Route.Home)
                },
                authViewModel = authViewModel
            )
        }

        composable<Route.ForgotPassword> {
            ForgotPasswordPage(
                modifier,
                navigateToForgotPasswordFeedback = {
                    navController.navigate(Route.ForgotPasswordFeedbackPage)
                },
                navigateToLogin = {
                    navController.navigate(Route.Login)
                },
                authViewModel = authViewModel
            )
        }

        composable<Route.ForgotPasswordFeedbackPage> {
            ForgotPasswordFeedbackPage(
                modifier,
                navigateToLogin = {
                    navController.navigate(Route.Login)
                },
                authViewModel = authViewModel
            )
        }

        composable<Route.UserEditPage> {
            UserEditPage(
                modifier,
                navigateToHome = {
                    navController.navigate(Route.Home)
                },
                authViewModel = authViewModel,
                context = context,
            )
        }
    }
}
