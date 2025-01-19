package com.example.polyquiz.constants

enum class DisplayAuthenticationText (val value: String) {
    USERNAME("Nom d'utilisateur"),
    PASSWORD("Mot de passe"),
    LOGIN_TITLE("Connexion"),
    SIGNUP_TITLE("Nouveau compte"),
    LOGIN_ACTION("Se connecter"),
    SIGNUP_ACTION("S'inscrire"),
    RETURN_TO_LOGIN("Retourner à la page de connexion"),
    LOGOUT("Se déconnecter")
}

enum class DisplayChatText (val value: String) {
    MESSAGE_LABEL("Message"),
    DISABLED_LABLE("Désactivé")
}

enum class AuthErrorText (val value: String) {
    EMPTY_USERNAME_PASSWORD("Le nom d'utilisateur et/ou le mot de passe sont vides"),
    USER_ALREADY_EXISTS( "Cet utilisateur existe déjà."),
    PASSWORD_TOO_SHORT("Le mot de passe est trop court (minimum: 6 caractères)."),
    INVALID_USERNAME_PASSWORD("Le nom d'utilisateur et/ou le mot de passe sont invalides."),
    OTHER_ERROR("Erreur")
}
