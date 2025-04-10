package com.example.polyquiz

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.example.polyquiz.elo.presentation.RankingsPage
import com.example.polyquiz.core.avatarDrawing.DrawingScreen
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
import com.example.polyquiz.shop.domain.ShopViewModel
import com.example.polyquiz.ui.features.camera.CameraViewModel
import com.example.polyquiz.ui.features.camera.MainCameraScreen
import com.plcoding.drawinginjetpackcompose.DrawingViewModel
import com.example.polyquiz.ui.theme.Theme
import com.example.polyquiz.shop.presentation.ShopPage

// References: https://youtu.be/AIC_OFQ1r3k  and  https://youtu.be/lv1raAvwcgI
@Composable
fun Navigation(
    modifier: Modifier,
    authViewModel: AuthViewModel,
    cameraViewModel: CameraViewModel,
    context: Context,
    currentTheme: Theme,
    onThemeUpdated: (Theme) -> Unit
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
                authViewModel = authViewModel,
                onThemeUpdated = onThemeUpdated
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
                },
                navigateToRankingsPage = {
                    navController.navigate(Route.RankingsPage)
                },
                navigateToShopPage = {
                    navController.navigate(Route.ShopPage)
                },
            )
        }
        composable<Route.MatchCreation> {
            MatchCreationPage(
                modifier,
                authViewModel = authViewModel,
                navigateToHome = {
                    navController.navigate(Route.Home)
                },
                navigateToCreate = {
                    navController.navigate(Route.MatchCreation)
                },
                navigateToUserEdit = {
                    navController.navigate(Route.UserEditPage)
                },
                navigateToWaitPage = {
                    navController.navigate(Route.WaitPage)
                },
                navigateToFriendsPage = {
                    navController.navigate(Route.FriendsSearchScreen)
                },
                navigateToJoinRoom = { navController.navigate(Route.JoinMatchPage) },
                navigateToLogin = { navController.navigate(Route.Login) },
                navigateToRankingsPage = { navController.navigate(Route.RankingsPage) },
                navigateToShopPage = { navController.navigate(Route.ShopPage) }
            )
        }
        composable<Route.ResultsPage> {
            ResultsPage(
                authViewModel,
                matchRoomService = MatchRoomService,
                navigateToHome = { navController.navigate(Route.Home) },
                extraContent = {},
                matchContextService = MatchContextService,
                players = players,
                modifier = modifier
            )
        }

        composable<Route.JoinMatchPage> {
            JoinMatchPage(
                modifier = modifier,
                authViewModel = authViewModel,
                navigateToHome = {
                    navController.navigate(Route.Home)
                },
                navigateToCreate = {
                    navController.navigate(Route.MatchCreation)
                },
                navigateToUserEdit = {
                    navController.navigate(Route.UserEditPage)
                },
                navigateToWaitPage = {
                    navController.navigate(Route.WaitPage)
                },
                navigateToFriendsPage = {
                    navController.navigate(Route.FriendsSearchScreen)
                },
                navigateToJoinRoom = { navController.navigate(Route.JoinMatchPage) },
                navigateToMatchPage = { navController.navigate(Route.MatchRoom) },
                navigateToLogin = { navController.navigate(Route.Login) },
                navigateToRankingsPage = { navController.navigate(Route.RankingsPage) },
                navigateToCamera = { navController.navigate(Route.MainCameraScreen) },
                cameraViewModel = cameraViewModel,
                navigateToShopPage = { navController.navigate(Route.ShopPage) }
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
                context = context,
                authViewModel = authViewModel,
                cameraViewModel = cameraViewModel,
                currentTheme = currentTheme,
                onThemeUpdated = onThemeUpdated,
                navigateToHome = {
                    navController.navigate(Route.Home)
                },
                navigateToCreate = {
                    navController.navigate(Route.MatchCreation)
                },
                navigateToUserEdit = {
                    navController.navigate(Route.UserEditPage)
                },
                navigateToFriendsPage = {
                    navController.navigate(Route.FriendsSearchScreen)
                },
                navigateToJoinRoom = { navController.navigate(Route.JoinMatchPage) },
                navigateToLogin = {
                    navController.navigate(Route.Login)
                },
                navigateToCamera = {
                    navController.navigate(Route.MainCameraScreen)
                },
                navigateToRankingsPage = {
                    navController.navigate(Route.RankingsPage)
                },
                navigateToShopPage = {
                    navController.navigate(Route.ShopPage)
                }
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
                navigateToJoinRoom = {
                    navController.navigate(Route.JoinMatchPage)
                },
                navigateToSignup = { navController.navigate(Route.Signup) }
            )
        }


        composable<Route.RankingsPage> {
            RankingsPage(
                modifier = modifier,
                authViewModel = authViewModel,
                navigateToHome = {
                    navController.navigate(Route.Home)
                },
                navigateToCreate = {
                    navController.navigate(Route.MatchCreation)
                },
                navigateToUserEdit = {
                    navController.navigate(Route.UserEditPage)
                },
                navigateToFriendsPage = {
                    navController.navigate(Route.FriendsSearchScreen)
                },
                navigateToJoinRoom = { navController.navigate(Route.JoinMatchPage) },
                navigateToLogin = { navController.navigate(Route.Login) },
                navigateToRankingsPage = { navController.navigate(Route.RankingsPage) },
                navigateToShop = { navController.navigate(Route.ShopPage) }
            )
        }
        composable<Route.Drawing> {

            val viewModel: DrawingViewModel = viewModel() // If not using Hilt

            DrawingScreen(
                viewModel = viewModel,
                navigateToUserEdit = { navController.navigate(Route.UserEditPage) },
                uid = authViewModel.getUserId(),
                cameraViewModel = cameraViewModel
            )
        }
        composable<Route.ShopPage> {
            val moneyService = remember { MoneyService() }

            ShopPage(
                modifier,
                authViewModel = authViewModel,
                currentUserID = authViewModel.getUserId(),
                moneyService = moneyService,
                navigateToLogin = { navController.navigate(Route.Login) },
                navigateToHome = { navController.navigate(Route.Home) },
                navigateToCreate = { navController.navigate(Route.MatchCreation) },
                navigateToUserEdit = { navController.navigate(Route.UserEditPage) },
                navigateToFriendsPage = { navController.navigate(Route.FriendsSearchScreen) },
                navigateToJoinRoom = { navController.navigate(Route.JoinMatchPage) },
                navigateToRankingsPage = { navController.navigate(Route.RankingsPage) },
                navigateToShop = { navController.navigate(Route.ShopPage) }
            )
        }
    }


}
