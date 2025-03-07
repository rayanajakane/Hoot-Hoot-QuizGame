package com.example.polyquiz

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.polyquiz.auth.domain.AuthViewModel
import com.example.polyquiz.auth.presentation.LoginPage
import com.example.polyquiz.auth.presentation.SignupPage
import com.example.polyquiz.constants.MatchContext
import com.example.polyquiz.match.domain.TimeService
import com.example.polyquiz.constants.Route
import com.example.polyquiz.match.domain.AnswerService
import com.example.polyquiz.match.domain.MatchContextService
import com.example.polyquiz.match.domain.MatchRoomService
import com.example.polyquiz.match.domain.Player
import com.example.polyquiz.match.presentation.QuestionArea
import com.example.polyquiz.pages.presentation.MatchCreationPage
import com.example.polyquiz.pages.presentation.WaitPage
import com.example.polyquiz.results.presentation.ResultsPage


// References: https://youtu.be/AIC_OFQ1r3k  and  https://youtu.be/lv1raAvwcgI
@Composable
fun Navigation(modifier: Modifier, authViewModel: AuthViewModel) {
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
                authViewModel = authViewModel,
                navigateToHome = {
                    navController.navigate(Route.Home)
                },
                navigateToWaitPage = {
                    navController.navigate(Route.WaitPage)
                }
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
                authViewModel = authViewModel,
                navigateToWaitPage = {navController.navigate(Route.WaitPage)}
            )
        }
        composable<Route.ResultsPage> {
            ResultsPage(modifier,
                matchRoomService = MatchRoomService,
                //matchContextService = MatchContextService,
            )
        }
    }
}
