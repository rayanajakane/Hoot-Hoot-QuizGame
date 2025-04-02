import { Inject, Injectable } from '@nestjs/common';
import { app } from 'firebase-admin';
import { Auth } from 'firebase-admin/lib/auth/auth';
import { ListUsersResult } from 'firebase-admin/lib/auth/base-auth';
import { UserRecord } from 'firebase-admin/lib/auth/user-record';

@Injectable()
export class FirebaseAuthService {
    auth: Auth;

    constructor(@Inject('FIREBASE_APP') private firebaseApp: app.App) {
        this.auth = firebaseApp.auth();
    }

    async getUsers(): Promise<ListUsersResult> {
        return this.auth.listUsers();
    }

    async getUserById(uid: string): Promise<UserRecord> {
        return this.auth.getUser(uid);
    }

    async getUserPhotoUrl(uid: string) {
        return this.getUserById(uid).then((userRecord: UserRecord) => {
            return userRecord.photoURL ? userRecord.photoURL : '';
        });
    }

    async getUsername(uid: string) {
        this.getUserById(uid).then((userRecord: UserRecord) => {
            return userRecord.displayName ? userRecord.displayName : '';
        });
    }
}
