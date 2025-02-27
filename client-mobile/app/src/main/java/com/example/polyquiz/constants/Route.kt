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
    object ResetPasswordEmailSent
}
