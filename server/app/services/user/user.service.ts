import { FirebaseRepository } from '@app/modules/firebase/firebase-repository/firebase-repository.service';
import { Injectable } from '@nestjs/common';
import { ListUsersResult } from 'firebase-admin/lib/auth/base-auth';
import { UserRecord } from 'firebase-admin/lib/auth/user-record';

@Injectable()
export class UserService {
    constructor(private firebaseRepository: FirebaseRepository) {}

    async getUsers(): Promise<ListUsersResult> {
        return this.firebaseRepository.auth.listUsers();
    }

    async getUserById(uid: string): Promise<UserRecord> {
        return this.firebaseRepository.auth.getUser(uid);
    }

    async getUserPhotoUrl(uid: string) {
        this.getUserById(uid).then((userRecord: UserRecord) => {
            return userRecord.photoURL ? userRecord.photoURL : '';
        });
    }

    async getUsername(uid: string) {
        this.getUserById(uid).then((userRecord: UserRecord) => {
            return userRecord.displayName ? userRecord.displayName : '';
        });
    }
}
