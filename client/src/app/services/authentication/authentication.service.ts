/* eslint-disable no-console */
import { Injectable } from '@angular/core';
import { FirebaseError } from '@angular/fire/app';
import {
    Auth,
    createUserWithEmailAndPassword,
    onAuthStateChanged,
    setPersistence,
    signInWithEmailAndPassword,
    signOut,
    updateProfile,
} from '@angular/fire/auth';
import { Router } from '@angular/router';
import { NotificationService } from '@app/services/notification/notification.service';
import { SocketHandlerService } from '@app/services/socket-handler/socket-handler.service';
import { ChatEvents } from '@common/events/chat.events';
import { TranslocoService } from '@jsverse/transloco';
import { browserSessionPersistence, User } from 'firebase/auth';
import { DataSnapshot, get, getDatabase, onDisconnect, ref, set, update } from 'firebase/database';
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
        setPersistence(this.auth, browserSessionPersistence); // TODO: Add this in mobile client, this is very important to prevent bugs

        onAuthStateChanged(this.auth, (user) => {
            if (user) {
                this.currentUser = user;
            } else {
                this.currentUser = null;
                this.router.navigateByUrl('/login');
            }
        });
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
            .catch((error: any) => {
                this.currentUser = null;
                console.log(error);
                return Promise.resolve(false);
            });
    }

    get userDisplayName(): string {
        const displayName: string = this.currentUser?.displayName ?? '';
        return displayName;
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
                    onDisconnect(userRef).update({
                        isOnline: false,
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
                this.router.navigateByUrl('/chat');
                this.notificationService.displaySuccessMessage(this.translocoService.translate('auth.dialog-feedback.sign-in'));
            })
            .catch((error) => {
                const errorMessage = this.handleAuthErrorMessage(error);
                this.notificationService.displayErrorMessage(errorMessage);
                signOut(this.auth);
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
                return this.translocoService.translate('auth.error.other-error');
            }
        }
    }
}
