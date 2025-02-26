package com.example.polyquiz.constants

enum class DisplayAuthenticationText (val value: String) {
    EMAIL("Courriel"),
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
    DISABLED_LABEL("Désactivé")
}

enum class AuthErrorText (val value: String) {
    EMPTY_USERNAME_PASSWORD("❌ Le courriel, le nom d'utilisateur et/ou le mot de passe sont vides"),
    USER_ALREADY_EXISTS( "❌ Cet utilisateur existe déjà."),
    PASSWORD_TOO_SHORT("Le mot de passe est trop court (minimum: 6 caractères)."),
    PASSWORD_TOO_LONG("Le mot de passe est trop long (maximum: 14 caractères)."),
    ALREADY_ONLINE("❌ Cet utilisateur est déjà connecté."),
    INVALID_USERNAME_PASSWORD("❌ Le courriel, le nom d'utilisateur et/ou le mot de passe sont invalides."),
    OTHER_ERROR("❌ Erreur"),
    INVALID_EMAIL("Adresse courriel invalide."),
    SHORT_USERNAME("Nom d'utilisateur trop court. Min 3 caractères"),
    LONG_USERNAME("Nom d'utilisateur trop long. Max 20 caractères."),
    SPECIAL_CHAR_USERNAME("Ne doit pas contenir des charactères spéciaux."),
    PASSWORD_LOWERCASE("Devrait contenir au moins 1 lettre minuscule."),
    PASSWORD_UPPERCASE("Devrait contenir au moins 1 lettre majuscule."),
    PASSWORD_DIGIT("Devrait contenir au moins 1 chiffre."),
    PASSWORD_SPECIAL("Devrait contenir au moins 1 caractère spécial."),
}

enum class AuthFeedbackText (val value: String) {
    SIGN_IN("✅ Connexion réussie !"),
    SIGN_UP("✅ Inscription réussie !"),
    SIGN_OUT("✅ Déconnexion réussie !")
}
