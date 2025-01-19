export const enum DisplayAuthenticationText {
    Username = 'Username',
    Password = 'Password',
    LoginAction = 'Sign in',
    SignupAction = 'Sign up',
    ReturnToLogin = 'Return to login',
    Logout = 'Log out',
}

export const enum AuthErrorText {
    EmptyUsernamePassword = 'Username and/or password are empty',
    UserAlreadyExists = 'User already exists',
    PasswordTooShort = 'Password is too short (minimum: 6 characters).',
    InvalidUsernamePassword = 'The username and/or the password are invalid.',
    OtherError = 'An unknown error occured',
}

export const enum AuthFeedbackText {
    SignIn = 'Signed in successfully !',
    SignUp = 'Signed up successfully !',
    SignOut = 'Signed out successfully !',
}
