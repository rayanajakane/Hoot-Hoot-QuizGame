/* eslint-disable no-console */
import { Injectable } from '@angular/core';
import { FirebaseError } from '@angular/fire/app';
import { Auth, createUserWithEmailAndPassword, onAuthStateChanged, signInWithEmailAndPassword, signOut, updateProfile } from '@angular/fire/auth';
import { Router } from '@angular/router';
import { NotificationService } from '@app/services/notification/notification.service';
import { SocketHandlerService } from '@app/services/socket-handler/socket-handler.service';
import { ChatEvents } from '@common/events/chat.events';
import { DataSnapshot, get, getDatabase, onDisconnect, ref, set, update } from 'firebase/database';
import { TranslocoService } from '@jsverse/transloco';
import { User } from 'firebase/auth';
import { SessionAlreadyExistsError } from './session-exists';

@Injectable({
    providedIn: 'root',
})
export class AuthenticationService {
    private currentUser: User | null;
    private database = getDatabase();

    constructor(
        private readonly router: Router,
        private readonly socketHandler: SocketHandlerService,
        private readonly notificationService: NotificationService,
        private readonly translocoService: TranslocoService,
        private auth: Auth,
    ) {
        onAuthStateChanged(this.auth, async (user) => {
            if (user) {
                const userRef = this.getUserDatabaseRef(user.uid);
                this.currentUser = user;
                return this.ensureUserSession(user.uid).then(() => {
                    onDisconnect(userRef)
                        .update({
                            isOnline: false,
                        })
                        .then(async () => {
                            update(userRef, { isOnline: true });
                        })
                        .catch((error) => {
                            console.log(error.message);
                        });
                });
            } else {
                // TODO : How to resolve correctly?
                this.currentUser = null;
                this.router.navigateByUrl('/login');
                return Promise.resolve();
            }
        });
    }

    get userDisplayName(): string {
        const displayName: string = this.currentUser?.displayName ?? '';
        return displayName;
    }

    async ensureUserSession(uid: string) {
        const userRef = this.getUserDatabaseRef(uid);
        return get(userRef)
            .then(async (databaseSnapshot: DataSnapshot) => {
                if (!databaseSnapshot.exists()) {
                    // Session does not exist
                    return update(userRef, {
                        isOnline: true,
                    });
                }

                const isUserOnline = databaseSnapshot.val().isOnline;
                if (isUserOnline) {
                    // TODO : Reject reason
                    throw new SessionAlreadyExistsError();
                    // return Promise.reject(SessionAlreadyExistsError);
                } else {
                    this.router.navigateByUrl('/chat');
                    return Promise.resolve();
                }
            })
            .catch((error) => {
                this.currentUser = null;
                console.log(error);
            });
    }

    getUserDatabaseRef(uid: string) {
        return ref(this.database, `users/${uid}`);
    }

    signUp(username: string, password: string) {
        const formattedUsername = username.trim();
        createUserWithEmailAndPassword(this.auth, `${formattedUsername}@polyQuiz.com`, password)
            .then((userCredential) => {
                updateProfile(userCredential.user, { displayName: formattedUsername }).then(() => {
                    const userRef = this.getUserDatabaseRef(userCredential.user.uid);
                    set(userRef, {
                        isOnline: true,
                    });
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
        const user = this.auth.currentUser;
        if (!user) {
            return;
        }
        const userRef = this.getUserDatabaseRef(user.uid);
        update(userRef, { isOnline: false });
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
