import { Injectable } from '@angular/core';
import { FirebaseError } from '@angular/fire/app';
import { Auth, createUserWithEmailAndPassword, onAuthStateChanged, signInWithEmailAndPassword, signOut, updateProfile } from '@angular/fire/auth';
import { Router } from '@angular/router';
import { NotificationService } from '@app/services/notification/notification.service';
import { SocketHandlerService } from '@app/services/socket-handler/socket-handler.service';
import { ChatEvents } from '@common/events/chat.events';
import { TranslocoService } from '@jsverse/transloco';
import { User } from 'firebase/auth';

@Injectable({
    providedIn: 'root',
})
export class AuthenticationService {
    private currentUser: User | null;

    constructor(
        private readonly router: Router,
        private readonly socketHandler: SocketHandlerService,
        private readonly notificationService: NotificationService,
        private readonly translocoService: TranslocoService,
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
        const displayName: string = this.currentUser?.displayName ?? '';
        return displayName;
    }

    signUp(username: string, password: string) {
        const formattedUsername = username.trim();
        createUserWithEmailAndPassword(this.auth, `${formattedUsername}@polyQuiz.com`, password)
            .then((userCredential) => {
                updateProfile(userCredential.user, { displayName: formattedUsername }).then(() => {
                    this.notificationService.displaySuccessMessage(this.translocoService.translate('auth.dialog-feedback.sign-up'));
                    this.currentUser = userCredential.user;
                    this.router.navigateByUrl('/chat');
                });
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
                this.notificationService.displaySuccessMessage(this.translocoService.translate('auth.dialog-feedback.sign-in'));
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
                this.notificationService.displaySuccessMessage(this.translocoService.translate('auth.dialog-feedback.sign-out'));
            })
            .catch((error) => {
                this.notificationService.displayErrorMessage(error.message);
            });
        this.socketHandler.socket.removeListener(ChatEvents.NewMessage);
    }

    private handleAuthErrorMessage(error: FirebaseError): string {
        switch (error.code) {
            case 'auth/email-already-in-use': {
                return this.translocoService.translate('auth.error.user-already-exists');
            }
            case 'auth/weak-password': {
                return this.translocoService.translate('auth.error.password-too-short');
            }
            case 'auth/invalid-credential': {
                return this.translocoService.translate('auth.error.invalid-username-password');
            }
            default: {
                return this.translocoService.translate('auth.error.other-error');
            }
        }
    }
}
