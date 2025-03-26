/* eslint-disable @typescript-eslint/no-explicit-any */
import { Injectable } from '@angular/core';
import { FirebaseError } from '@angular/fire/app';
import { Auth, createUserWithEmailAndPassword, onAuthStateChanged, signInWithEmailAndPassword, signOut, updateProfile } from '@angular/fire/auth';
import { Router } from '@angular/router';
import { PresetAvatar } from '@app/constants/image-constants';
import { AuthError } from '@app/services/authentication/auth-error';
import { ChatService } from '@app/services/chat/chat.service';
import { MatchRoomService } from '@app/services/match-room/match-room.service';
import { MoneyService } from '@app/services/money/money.service';
import { NotificationService } from '@app/services/notification/notification.service';
import { SocketHandlerService } from '@app/services/socket-handler/socket-handler.service';
import { ChatEvents } from '@common/events/chat.events';
import { FriendsEvents } from '@common/events/friends.events';
import { TranslocoService } from '@jsverse/transloco';
import { browserSessionPersistence, sendPasswordResetEmail, setPersistence, User, UserCredential } from 'firebase/auth';
import { Database, DataSnapshot, get, getDatabase, onDisconnect, ref, remove, set, update } from 'firebase/database';
import { deleteObject, FirebaseStorage, ref as firebaseStorageRef, getDownloadURL, getStorage, uploadBytes } from 'firebase/storage';
import { BehaviorSubject } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class AuthenticationService {
    currentUser: User | null;
    database: Database = getDatabase(); // Realtime Database
    storage: FirebaseStorage = getStorage(); // Firebase Storage: For images

    authenticatedUser = new BehaviorSubject<User | null>(null);

    // eslint-disable-next-line max-params
    constructor(
        private readonly router: Router,
        private readonly socketHandler: SocketHandlerService,
        private readonly notificationService: NotificationService,
        private readonly translocoService: TranslocoService,
        private readonly chatService: ChatService,
        private matchRoomService: MatchRoomService,
        private readonly moneyService: MoneyService,
        private auth: Auth,
    ) {
        setPersistence(this.auth, browserSessionPersistence);

        onAuthStateChanged(this.auth, (user) => {
            if (user) {
                this.setUser(user);
            } else {
                this.setUser(null);
                this.router.navigateByUrl('/login');
            }
        });
    }

    get userId(): string {
        return this.currentUser?.uid ?? '';
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
        this.authenticatedUser.next(this.currentUser);
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
            .catch(async () => {
                this.setUser(null);
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
                balance: 1000,
            });
            onDisconnect(userRef).update({
                isOnline: false,
            });

            const usernameRef = this.getUsernameDatabaseRef(username.toLowerCase());
            set(usernameRef, username.toLowerCase());

            this.connectToSocket();
            this.socketHandler.send(FriendsEvents.Connect, this.userId);
            this.socketHandler.send(FriendsEvents.UpdateData);
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
            isValidAvatarUrl = await this.editAvatarUrl(avatarUrl);
        }
        if (isValidUsername && isValidAvatarUrl) {
            this.socketHandler.send(FriendsEvents.UpdateData);
            this.notificationService.displaySuccessMessage(this.translocoService.translate('auth.dialog-feedback.edited'));
        }
    }

    async editAvatarUrl(avatarUrl: string) {
        if (!this.currentUser) return false;
        await updateProfile(this.currentUser, { photoURL: avatarUrl })
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

    async signUp(email: string, username: string, password: string, isPresetAvatar: boolean, avatarUrl: string, avatarFile: File | null) {
        const formattedUsername = username.trim();
        const formattedEmail = email.trim();

        try {
            await this.checkUsername(formattedUsername.toLowerCase());
            createUserWithEmailAndPassword(this.auth, `${formattedEmail}`, password)
                .then(async (userCredential) => {
                    if (!isPresetAvatar && avatarFile) {
                        avatarUrl = await this.uploadUserAvatar(userCredential.user.uid, avatarFile);
                    } else if (!isPresetAvatar && !avatarFile) {
                        avatarUrl = PresetAvatar.Default;
                    }
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
                this.setUser(userCredential.user);
                this.socketHandler.send(FriendsEvents.Connect, this.userId);
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
            this.chatService.handleGeneralEmoji();
            this.chatService.handleRoomEmoji();
            this.moneyService.getCurrentBalance(this.userId);
            this.moneyService.listenForMoneyEvents();
        }
    }

    disconnectSocket() {
        this.matchRoomService.disconnectFromRoom();
        this.socketHandler.disconnect();
        this.socketHandler.socket.removeListener(ChatEvents.NewMessage);
        this.chatService.clearMessages();
        this.moneyService.stopListeningForMoneyEvents();
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

    deleteUser() {
        const user = this.auth.currentUser;
        if (!user) {
            return;
        }
        const userRef = this.getUserDatabaseRef(user.uid);
        remove(userRef);
        if (user.displayName) {
            const usernameRef = this.getUsernameDatabaseRef(user.displayName.toLowerCase());
            remove(usernameRef);
        }
        this.deleteUserAvatar(user.uid);
        const id = user.uid;
        user.delete().then(() => {
            this.socketHandler.send(FriendsEvents.UserDeleted, id);
            this.setUser(null);
            this.disconnectSocket();
            this.router.navigateByUrl('/login');
            this.notificationService.displaySuccessMessage(this.translocoService.translate('auth.dialog-feedback.delete'));
        });
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
                return this.translocoService.translate('auth.error.other-error');
            }
        }
    }

    // FIREBASE STORAGE -- Consider refactoring it in its own service
    async uploadUserAvatar(userId: string | undefined, file: any): Promise<string> {
        if (!userId || userId === '') return '';
        const url: string = await this.uploadImage(`avatars/${userId}`, file);
        return url;
    }

    async uploadQuestionPicture(questionId: string, file: any): Promise<string> {
        const url: string = await this.uploadImage(`questionPictures/${questionId}`, file);
        return url;
    }

    async uploadImage(path: string, file: any): Promise<string> {
        const storageRef = firebaseStorageRef(this.storage, path);
        const uploadTask = uploadBytes(storageRef, file);

        // REFERENCE: https://firebase.google.com/docs/storage/web/upload-files?hl=fr
        return uploadTask
            .then(async () => {
                // Handle successful uploads on complete
                const downloadURL = getDownloadURL((await uploadTask).ref);
                return downloadURL;
            })
            .catch(() => {
                // Handle unsuccessful uploads
                this.notificationService.displayErrorMessage('TODO');
                return '';
            });
    }

    isImageToUpload(imageUrl: string) {
        return imageUrl.startsWith('data:image/');
    }

    async deleteUserAvatar(userId: string) {
        this.deleteImage(`avatars/${userId}`);
    }

    async deleteImage(path: string) {
        const storageRef = firebaseStorageRef(this.storage, path);
        deleteObject(storageRef)
            .then(() => {})
            .catch((error: Error) => {
                console.log(error);
            });
    }

    async getImageDownloadUrl(path: string) {
        const storageRef = firebaseStorageRef(this.storage, path);
        const downloadUrl = await getDownloadURL(storageRef);
        return downloadUrl;
    }
}
