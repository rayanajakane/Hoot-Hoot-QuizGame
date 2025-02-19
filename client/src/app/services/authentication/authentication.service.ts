/* eslint-disable no-console */
/* eslint-disable @typescript-eslint/no-explicit-any */
import { Injectable } from '@angular/core';
import { FirebaseError } from '@angular/fire/app';
import { Auth, createUserWithEmailAndPassword, onAuthStateChanged, signInWithEmailAndPassword, signOut, updateProfile } from '@angular/fire/auth';
import { Router } from '@angular/router';
import { SessionAlreadyExistsError } from '@app/services/authentication/session-exists';
import { ChatService } from '@app/services/chat/chat.service';
import { NotificationService } from '@app/services/notification/notification.service';
import { SocketHandlerService } from '@app/services/socket-handler/socket-handler.service';
import { ChatEvents } from '@common/events/chat.events';
import { TranslocoService } from '@jsverse/transloco';
import { browserSessionPersistence, setPersistence, User } from 'firebase/auth';
import { DataSnapshot, get, getDatabase, onDisconnect, ref, set, update } from 'firebase/database';

@Injectable({
    providedIn: 'root',
})
export class AuthenticationService {
    private currentUser: User | null;
    private database = getDatabase();

    // eslint-disable-next-line max-params
    constructor(
        private readonly router: Router,
        private readonly socketHandler: SocketHandlerService,
        private readonly notificationService: NotificationService,
        private readonly translocoService: TranslocoService,
        private readonly chatService: ChatService,
        private auth: Auth,
    ) {
        setPersistence(this.auth, browserSessionPersistence);
        onAuthStateChanged(this.auth, (user) => {
            if (user) {
                this.currentUser = user;
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

    isUserAuthenticated(): boolean {
        return !!this.currentUser;
    }

    async ensureUserSession(uid: string) {
        const userRef = this.getUserDatabaseRef(uid);
        return get(userRef)
            .then(async (databaseSnapshot: DataSnapshot) => {
                if (!databaseSnapshot.exists()) {
                    // Session does not exist
                    update(userRef, {
                        isOnline: true,
                    });
                    return Promise.resolve(true);
                }

                const isUserOnline = databaseSnapshot.val().isOnline;
                if (isUserOnline) {
                    return Promise.resolve(false);
                } else {
                    return Promise.resolve(true);
                }
            })
            .catch(async (error: unknown) => {
                this.currentUser = null;
                console.log(error);
                return Promise.resolve(false);
            });
    }

    getUserDatabaseRef(uid: string | null) {
        return uid ? ref(this.database, `users/${uid}`) : ref(this.database, 'users/');
    }

    signUp(email: string, username: string, password: string) {
        const formattedUsername = username.trim();
        const formattedEmail = email.trim();
        createUserWithEmailAndPassword(this.auth, `${formattedEmail}`, password)
            .then((userCredential) => {
                updateProfile(userCredential.user, { displayName: formattedUsername }).then(() => {
                    const userRef = this.getUserDatabaseRef(userCredential.user.uid);

                    set(userRef, {
                        isOnline: true,
                    });
                    onDisconnect(userRef).update({
                        isOnline: false,
                    });
                    this.connectToSocket();
                    this.currentUser = userCredential.user;
                    this.router.navigateByUrl('/home');
                    this.notificationService.displaySuccessMessage(this.translocoService.translate('auth.dialog-feedback.sign-up'));
                });
            })
            .catch((error) => {
                const errorMessage = this.handleAuthErrorMessage(error);
                this.notificationService.displayErrorMessage(errorMessage);
            });
    }

    signIn(email: string, password: string) {
        const formattedEmail = email.trim();
        signInWithEmailAndPassword(this.auth, `${formattedEmail}`, password)
            .then(async (userCredential) => {
                const userRef = this.getUserDatabaseRef(userCredential.user.uid);
                const isAbleToSignIn = await this.ensureUserSession(userCredential.user.uid);
                if (!isAbleToSignIn) throw new SessionAlreadyExistsError();
                update(userRef, {
                    isOnline: true,
                });
                onDisconnect(userRef).update({
                    isOnline: false,
                });
                this.connectToSocket();
                this.router.navigateByUrl('/home');
                this.notificationService.displaySuccessMessage(this.translocoService.translate('auth.dialog-feedback.sign-in'));
            })
            .catch((error) => {
                const errorMessage = this.handleAuthErrorMessage(error);
                this.notificationService.displayErrorMessage(errorMessage);
                signOut(this.auth);
            });
    }

    connectToSocket() {
        if (!this.socketHandler.isSocketAlive()) {
            this.socketHandler.connect();
            this.chatService.handleReceivedMessages();
        }
    }

    disconnectSocket() {
        this.socketHandler.disconnect();
        this.socketHandler.socket.removeListener(ChatEvents.NewMessage);
        this.chatService.clearMessages();
    }

    signOut() {
        const user = this.auth.currentUser;
        if (!user) {
            return;
        }
        const userRef = this.getUserDatabaseRef(user.uid);
        update(userRef, { isOnline: false });
        signOut(this.auth)
            .then(() => {
                this.notificationService.displaySuccessMessage(this.translocoService.translate('auth.dialog-feedback.sign-out'));
                this.disconnectSocket();
                this.currentUser = null;
                this.router.navigateByUrl('/login');
            })
            .catch((error) => {
                this.notificationService.displayErrorMessage(error.message);
            });
    }

    private handleAuthErrorMessage(error: FirebaseError): string {
        switch (error.code) {
            case 'SessionAlreadyExists': {
                return "L'utilisateur est déjà connecté !";
            }
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
                console.log(error);
                return this.translocoService.translate('auth.error.other-error');
            }
        }
    }
}
