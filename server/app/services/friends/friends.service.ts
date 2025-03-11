import { FirebaseAuthService } from '@app/modules/firebase/firebase-auth/firebase-auth.service';
import { FirebaseRepositoryService } from '@app/modules/firebase/firebase-repository/firebase-repository.service';
import { UserIdName } from '@common/interfaces/user-id-name';
import { Injectable } from '@nestjs/common';
import { Database } from 'firebase-admin/lib/database/database';

@Injectable()
export class FriendsService {
    private database: Database;

    constructor(
        private readonly firebaseService: FirebaseRepositoryService,
        private readonly firebaseAuthService: FirebaseAuthService,
    ) {
        this.database = this.firebaseService.database;
    }

    async getAllUsers(): Promise<UserIdName[]> {
        console.log('Fetching all users...');
        const listUsersResult = await this.firebaseAuthService.getUsers();
        return listUsersResult.users.map((user) => ({
            id: user.uid,
            name: user.displayName,
        }));
    }

    async getFriendsList(userId: string): Promise<UserIdName[]> {
        const snapshot = await this.database.ref(`users/${userId}/friends`).once('value');
        if (!snapshot.exists()) return [];
        const friendIds = Object.keys(snapshot.val());
        const friends: UserIdName[] = await Promise.all(
            friendIds.map(async (id) => {
                const userRecord = await this.firebaseAuthService.getUserById(id);
                return { id, name: userRecord.displayName };
            }),
        );
        return friends;
    }

    async getPendingRequests(userId: string): Promise<UserIdName[]> {
        const snapshot = await this.database.ref(`users/${userId}/friend_requests_received`).once('value');
        if (!snapshot.exists()) return [];
        const requestIds = Object.keys(snapshot.val());
        const requests: UserIdName[] = await Promise.all(
            requestIds.map(async (id) => {
                const userRecord = await this.firebaseAuthService.getUserById(id);
                return { id, name: userRecord.displayName };
            }),
        );
        return requests;
    }

    async cancelRequest(userId: string, friendId: string): Promise<void> {
        await this.database.ref(`users/${userId}/friend_requests_sent/${friendId}`).remove();
        await this.database.ref(`users/${friendId}/friend_requests_received/${userId}`).remove();
    }

    async getSentRequests(userId: string): Promise<UserIdName[]> {
        const snapshot = await this.database.ref(`users/${userId}/friend_requests_sent`).once('value');
        if (!snapshot.exists()) return [];
        const requestIds = Object.keys(snapshot.val());
        const requests: UserIdName[] = await Promise.all(
            requestIds.map(async (id) => {
                const userRecord = await this.firebaseAuthService.getUserById(id);
                return { id, name: userRecord.displayName };
            }),
        );
        return requests;
    }

    async sendFriendRequest(fromUserId: string, toUserId: string): Promise<void> {
        console.log(`Sending friend request from ${fromUserId} to ${toUserId}`);
        await this.database.ref(`users/${fromUserId}/friend_requests_sent/${toUserId}`).set(true);
        await this.database.ref(`users/${toUserId}/friend_requests_received/${fromUserId}`).set(true);
        // TODO: add a notification for the recipient here.
    }

    async acceptFriendRequest(userId: string, friendId: string): Promise<void> {
        await this.database.ref(`users/${userId}/friends/${friendId}`).set(true);
        await this.database.ref(`users/${friendId}/friends/${userId}`).set(true);
        await this.database.ref(`users/${userId}/friend_requests_received/${friendId}`).remove();
        await this.database.ref(`users/${friendId}/friend_requests_sent/${userId}`).remove();
    }

    async rejectFriendRequest(userId: string, friendId: string): Promise<void> {
        await this.database.ref(`users/${userId}/friend_requests_received/${friendId}`).remove();
        await this.database.ref(`users/${friendId}/friend_requests_sent/${userId}`).remove();
    }

    async removeFriend(userId: string, friendId: string): Promise<void> {
        await this.database.ref(`users/${userId}/friends/${friendId}`).remove();
        await this.database.ref(`users/${friendId}/friends/${userId}`).remove();
    }
}
