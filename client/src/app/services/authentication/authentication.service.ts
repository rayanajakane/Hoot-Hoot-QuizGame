/* eslint-disable no-console */
/* eslint-disable @typescript-eslint/no-explicit-any */
import { Injectable } from '@angular/core';
import { FirebaseError } from '@angular/fire/app';
import {
    Auth,
    createUserWithEmailAndPassword,
    onAuthStateChanged,
    sendPasswordResetEmail,
    signInWithEmailAndPassword,
    signOut,
    updateProfile,
} from '@angular/fire/auth';
import { Router } from '@angular/router';
import { AuthError } from '@app/services/authentication/auth-error';
import { ChatService } from '@app/services/chat/chat.service';
import { MatchRoomService } from '@app/services/match-room/match-room.service';
import { NotificationService } from '@app/services/notification/notification.service';
import { SocketHandlerService } from '@app/services/socket-handler/socket-handler.service';
import { ChatEvents } from '@common/events/chat.events';
import { TranslocoService } from '@jsverse/transloco';
import { browserSessionPersistence, setPersistence, User, UserCredential } from 'firebase/auth';
import { DataSnapshot, get, getDatabase, onDisconnect, ref, remove, set, update } from 'firebase/database';

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
        private matchRoomService: MatchRoomService,
        private auth: Auth,
    ) {
        setPersistence(this.auth, browserSessionPersistence);
        onAuthStateChanged(this.auth, (user) => {
            if (user) {
                this.setUser(user);
            } else {
                this.currentUser = null;
                this.router.navigateByUrl('/login');
            }
        });
    }

    get userEmail(): string {
        return this.currentUser?.email ?? '';
    }

    get userDisplayName(): string {
        return this.currentUser?.displayName ?? '';
    }

    get userAvatarUrl(): string {
        return this.currentUser?.photoURL ?? '';
    }

    getUsernameDatabaseRef(username: string) {
        return ref(this.database, `usernames/${username}`);
    }

    async checkUsername(username: string): Promise<boolean> {
        const usernameRef = this.getUsernameDatabaseRef(username);
        return get(usernameRef).then(async (databaseSnapshot: DataSnapshot) => {
            if (databaseSnapshot.exists()) {
                return Promise.reject(new AuthError('UsernameAlreadyExists', 'UsernameAlreadyExistsError'));
            } else {
                // Username will be saved in realtime database later when sign up succeeds.
                return Promise.resolve(false);
            }
        });
    }

    setUser(user: User | null) {
        this.currentUser = user;
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
                this.setUser(null);
                console.log(error);
                return Promise.resolve(false);
            });
    }

    getUserDatabaseRef(uid: string | null) {
        return uid ? ref(this.database, `users/${uid}`) : ref(this.database, 'users/');
    }

    async completeUserProfileCreation(userCredential: UserCredential, username: string, avatarUrl: string) {
        updateProfile(userCredential.user, { displayName: username, photoURL: avatarUrl }).then(() => {
            const userRef = this.getUserDatabaseRef(userCredential.user.uid);

            set(userRef, {
                isOnline: true,
            });
            onDisconnect(userRef).update({
                isOnline: false,
            });

            const usernameRef = this.getUsernameDatabaseRef(username.toLowerCase());
            set(usernameRef, username.toLowerCase());

            this.connectToSocket();
            this.setUser(userCredential.user);
            this.router.navigateByUrl('/home');
            this.notificationService.displaySuccessMessage(this.translocoService.translate('auth.dialog-feedback.sign-up'));
        });
    }

    async editUserProfile(username: string, avatarUrl: string) {
        let isValidUsername: boolean = this.userDisplayName.toLowerCase() === username.toLowerCase();
        let isValidAvatarUrl: boolean = this.userAvatarUrl === avatarUrl;
        if (!isValidUsername) {
            isValidUsername = await this.editUsername(username);
        }
        if (!isValidAvatarUrl) {
            isValidAvatarUrl = this.editAvatarUrl(avatarUrl);
        }
        if (isValidUsername && isValidAvatarUrl) {
            this.notificationService.displaySuccessMessage(this.translocoService.translate('auth.dialog-feedback.edited'));
        }
    }

    editAvatarUrl(avatarUrl: string) {
        if (!this.currentUser) return false;
        updateProfile(this.currentUser, { photoURL: avatarUrl })
            .then(() => {
                return true;
            })
            .catch((error: any) => {
                console.log(error);
                return false;
            });
        return true;
    }

    async editUsername(username: string) {
        if (!this.currentUser) return false;
        const formattedUsername = username.trim();
        const oldUsername = this.userDisplayName;
        try {
            await this.checkUsername(formattedUsername.toLowerCase());
            updateProfile(this.currentUser, { displayName: formattedUsername })
                .then(() => {
                    const usernameRef = this.getUsernameDatabaseRef(username.toLowerCase());
                    set(usernameRef, username.toLowerCase());

                    const oldUsernameRef = this.getUsernameDatabaseRef(oldUsername.toLowerCase());
                    remove(oldUsernameRef);

                    return true;
                })
                .catch((error: any) => {
                    const errorMessage = this.handleAuthErrorMessage(error);
                    this.notificationService.displayErrorMessage(errorMessage);
                    return false;
                });
        } catch (error: any) {
            const errorMessage = this.handleAuthErrorMessage(error);
            this.notificationService.displayErrorMessage(errorMessage);
            return false;
        }
        return true;
    }

    async signUp(email: string, username: string, password: string, avatarUrl: string) {
        const formattedUsername = username.trim();
        const formattedEmail = email.trim();

        try {
            await this.checkUsername(formattedUsername.toLowerCase());
            createUserWithEmailAndPassword(this.auth, `${formattedEmail}`, password)
                .then((userCredential) => {
                    this.completeUserProfileCreation(userCredential, formattedUsername, avatarUrl);
                })
                .catch((error) => {
                    const errorMessage = this.handleAuthErrorMessage(error);
                    this.notificationService.displayErrorMessage(errorMessage);
                });
        } catch (error: any) {
            const errorMessage = this.handleAuthErrorMessage(error);
            this.notificationService.displayErrorMessage(errorMessage);
        }
    }

    signIn(email: string, password: string) {
        const formattedEmail = email.trim();
        signInWithEmailAndPassword(this.auth, `${formattedEmail}`, password)
            .then(async (userCredential) => {
                const userRef = this.getUserDatabaseRef(userCredential.user.uid);
                const isAbleToSignIn = await this.ensureUserSession(userCredential.user.uid);
                if (!isAbleToSignIn) throw new AuthError('SessionAlreadyExists', 'SessionAlreadyExistsError');
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
            this.chatService.handleRoomMessages();
        }
    }

    disconnectSocket() {
        this.matchRoomService.disconnectFromRoom();
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
                this.setUser(null);
                this.router.navigateByUrl('/login');
            })
            .catch((error) => {
                this.notificationService.displayErrorMessage(error.message);
            });
    }

    public deleteUser() {
        const user = this.auth.currentUser;
        if (!user) {
            return;
        }
        // Delete user from Realtime database (TODO: Delete profile picture too?)
        const userRef = this.getUserDatabaseRef(user.uid);
        remove(userRef);
        if (user.displayName) {
            const usernameRef = this.getUsernameDatabaseRef(user.displayName.toLowerCase());
            remove(usernameRef);
        }
        this.disconnectSocket();
        user.delete();
        this.setUser(null);
        this.router.navigateByUrl('/login');
        this.notificationService.displaySuccessMessage(this.translocoService.translate('auth.dialog-feedback.delete'));
    }

    sendResetPasswordEmail(email: string) {
        // TODO: Check if we need to use Firebase Admin SDK to getUserByEmail() from server (to only send emails to user that already have an account)
        sendPasswordResetEmail(this.auth, email)
            .then(() => {
                this.router.navigateByUrl('reset-password-email-sent');
            })
            .catch((error) => {
                console.log(error);
                this.notificationService.displayErrorMessage(this.translocoService.translate('auth.error.invalid-email'));
            });
    }

    private handleAuthErrorMessage(error: FirebaseError): string {
        switch (error.code) {
            case 'SessionAlreadyExists': {
                return this.translocoService.translate('auth.error.user-already-connected');
            }
            case 'UsernameAlreadyExists': {
                return this.translocoService.translate('auth.error.username-already-exists');
            }
            case 'auth/email-already-in-use': {
                return this.translocoService.translate('auth.error.email-already-in-use');
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
