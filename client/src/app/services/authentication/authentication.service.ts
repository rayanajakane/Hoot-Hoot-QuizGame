import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { SocketHandlerService } from '@app/services/socket-handler/socket-handler.service';
import { ChatEvents } from '@common/events/chat.events';
import { NotificationService } from '@app/services/notification/notification.service';
import { User } from 'firebase/auth';
import { Auth, createUserWithEmailAndPassword, onAuthStateChanged, signInWithEmailAndPassword, signOut, updateProfile } from '@angular/fire/auth';
import { AuthErrorText, AuthFeedbackText } from 'src/assets/i18n/auth/fr';
import { FirebaseError } from '@angular/fire/app';

@Injectable({
    providedIn: 'root',
})
export class AuthenticationService {
    private currentUser: User | null;

    constructor(
        private readonly router: Router,
        private readonly socketHandler: SocketHandlerService,
        private readonly notificationService: NotificationService,
        private auth: Auth,
    ) {
        onAuthStateChanged(this.auth, (user) => {
            if (user) {
                this.currentUser = user;
                this.router.navigateByUrl('/chat');
            } else {
                this.currentUser = null;
                this.router.navigateByUrl('/login');
            }
        });
    }

    get userDisplayName(): string {
        const displayName: string = this.currentUser?.displayName ?? 'DisplayNameNotFound';
        return displayName;
    }

    signUp(username: string, password: string) {
        const formattedUsername = username.trim();
        createUserWithEmailAndPassword(this.auth, `${formattedUsername}@polyQuiz.com`, password)
            .then((userCredential) => {
                updateProfile(userCredential.user, { displayName: formattedUsername });
                this.notificationService.displaySuccessMessage(AuthFeedbackText.SignUp);
            })
            .catch((error) => {
                const errorMessage = this.handleAuthErrorMessage(error);
                this.notificationService.displayErrorMessage(errorMessage);
            });
    }

    signIn(username: string, password: string) {
        const formattedUsername = username.trim();
        signInWithEmailAndPassword(this.auth, `${formattedUsername}@polyQuiz.com`, password)
            .then(() => {
                this.notificationService.displaySuccessMessage(AuthFeedbackText.SignIn);
            })
            .catch((error) => {
                const errorMessage = this.handleAuthErrorMessage(error);
                this.notificationService.displayErrorMessage(errorMessage);
            });
    }

    connectToSocket() {
        this.socketHandler.connect();
    }

    disconnectSocket() {
        this.socketHandler.disconnect();
    }

    signOut() {
        signOut(this.auth)
            .then(() => {
                this.notificationService.displaySuccessMessage(AuthFeedbackText.SignOut);
            })
            .catch((error) => {
                this.notificationService.displayErrorMessage(error.message);
            });
        this.socketHandler.socket.removeListener(ChatEvents.NewMessage);
    }

    private handleAuthErrorMessage(error: FirebaseError): string {
        switch (error.code) {
            case 'auth/email-already-in-use': {
                return AuthErrorText.UserAlreadyExists;
            }
            case 'auth/weak-password': {
                return AuthErrorText.PasswordTooShort;
            }
            case 'auth/invalid-credential': {
                return AuthErrorText.InvalidUsernamePassword;
            }
            default: {
                return AuthErrorText.OtherError;
            }
        }
    }
}
