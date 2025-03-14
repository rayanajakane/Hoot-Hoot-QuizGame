package com.example.polyquiz.constants

import kotlinx.serialization.Serializable

object Route {
    @Serializable
    object Login

    @Serializable
    object Signup

    @Serializable
    object Chat

    @Serializable
    object ForgotPassword

    @Serializable
    object ForgotPasswordFeedbackPage

    @Serializable
    object Home

    @Serializable
    object MatchRoom

    @Serializable
    object MatchCreation

    @Serializable
    object WaitPage

    @Serializable
    object ResultsPage

    @Serializable
    object UserEditPage

    @Serializable
    object FriendsSearchScreen

	@Serializable
    object MainCameraScreen

    @Serializable
    object JoinMatchPage
}
