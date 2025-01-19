export const enum DisplayAuthenticationText {
    Username = "Nom d'utilisateur",
    Password = 'Mot de passe',
    LoginTitle = 'Connexion',
    SignupTitle = 'Nouveau compte',
    LoginAction = 'Se connecter',
    SignupAction = "S'inscrire",
    ReturnToLogin = 'Retourner à la page de connexion',
    Logout = 'Se déconnecter',
}

export const enum AuthErrorText {
    EmptyUsernamePassword = "Le nom d'utilisateur et/ou le mot de passe sont vides",
    UserAlreadyExists = 'Cet utilisateur existe déjà.',
    PasswordTooShort = 'Le mot de passe est trop court (minimum: 6 caractères).',
    InvalidUsernamePassword = "Le nom d'utilisateur et/ou le mot de passe sont invalides.",
    OtherError = 'Une erreur inconnue est survenue',
}

export const enum AuthFeedbackText {
    SignIn = 'Connexion réussie !',
    SignUp = 'Inscription réussie !',
    SignOut = 'Déconnexion réussie !',
}
