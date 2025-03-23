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
import com.example.polyquiz.match.domain.TimeService
import com.example.polyquiz.constants.Route
import com.example.polyquiz.friends.domain.FriendsService
import com.example.polyquiz.match.domain.AnswerService
import com.example.polyquiz.match.domain.MatchContextService
import com.example.polyquiz.match.domain.MatchRoomService
import com.example.polyquiz.match.domain.MatchRoomService.players
import com.example.polyquiz.match.presentation.QuestionArea
import com.example.polyquiz.match.presentation.ResultsPage
import com.example.polyquiz.pages.presentation.JoinMatchPage
import com.example.polyquiz.pages.presentation.MatchCreationPage
import com.example.polyquiz.pages.presentation.WaitPage
import com.example.polyquiz.friends.presentation.FriendsSearchScreen
import com.example.polyquiz.money.domain.MoneyService
import com.example.polyquiz.ui.features.camera.CameraViewModel
import com.example.polyquiz.ui.features.camera.MainCameraScreen
import com.example.polyquiz.ui.theme.Theme

// References: https://youtu.be/AIC_OFQ1r3k  and  https://youtu.be/lv1raAvwcgI
@Composable
fun Navigation(
    modifier: Modifier,
    authViewModel: AuthViewModel,
    cameraViewModel: CameraViewModel,
    context: Context,
    currentTheme: Theme,
    onThemeUpdated: () -> Unit
) {
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
                navigateToCamera = {
                    navController.navigate(Route.MainCameraScreen)
                },
                authViewModel = authViewModel,
                cameraViewModel = cameraViewModel
            )
        }
        composable<Route.MatchRoom> {
            QuestionArea(
                modifier = modifier,
                navigateToHome = {
                    navController.navigate(Route.Home)
                },
                navigateToResultsPage = {
                    navController.navigate(Route.ResultsPage)
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
                navigateToJoinRoom = { navController.navigate(Route.JoinMatchPage) },
                authViewModel = authViewModel,
                navigateToHome = {
                    navController.navigate(Route.Home)
                },
                navigateToUserEdit = {
                    navController.navigate(Route.UserEditPage)
                },
                navigateToWaitPage = {
                    navController.navigate(Route.WaitPage)
                },
                navigateToFriendsPage = {
                    navController.navigate(Route.FriendsSearchScreen)
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
                authViewModel = authViewModel,
                navigateToWaitPage = { navController.navigate(Route.WaitPage) }
            )
        }
        composable<Route.ResultsPage> {
            ResultsPage(
                matchRoomService = MatchRoomService,
                navigateToHome = { navController.navigate(Route.Home) },
                players,
                modifier,
                extraContent = {}
            )
        }

        composable<Route.JoinMatchPage> {
            JoinMatchPage(
                modifier,
                authViewModel,
                navigateToHome = { navController.navigate(Route.Home) },
                navigateToMatchPage = { navController.navigate(Route.MatchRoom) },
                navigateToWaitPage = { navController.navigate(Route.WaitPage) }
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
                modifier = modifier,
                navigateToHome = {
                    navController.navigate(Route.Home)
                },
                navigateToCamera = {
                    navController.navigate(Route.MainCameraScreen)
                },
                navigateToLogin = {
                    navController.navigate(Route.Login)
                },
                authViewModel = authViewModel,
                context = context,
                cameraViewModel = cameraViewModel,
                currentTheme = currentTheme,
                onThemeUpdated = onThemeUpdated
            )
        }
        composable<Route.FriendsSearchScreen> {
            FriendsSearchScreen(
                currentUserID = authViewModel.getUserId(),
//                friendsService = FriendsService(),
//                moneyService = MoneyService(),
                navigateToHome = { navController.navigate(Route.Home) }
            )
        }
        composable<Route.MainCameraScreen> {
            MainCameraScreen(
                authViewModel,
                cameraViewModel,
                navigateToUserEdit = { navController.navigate(Route.UserEditPage) },
                navigateToSignup = { navController.navigate(Route.Signup) }
            )
        }
    }


}
