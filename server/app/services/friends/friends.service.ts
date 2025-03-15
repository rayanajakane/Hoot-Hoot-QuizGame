import { NO_FRIENDS, NOT_FRIENDS_WITH_HOST } from '@app/constants/match-login-errors';
import { FriendsGateway } from '@app/gateways/friends/friends.gateway';
import { FirebaseAuthService } from '@app/modules/firebase/firebase-auth/firebase-auth.service';
import { FirebaseRepositoryService } from '@app/modules/firebase/firebase-repository/firebase-repository.service';
import { FriendsEvents } from '@common/events/friends.events';
import { FriendsInfo } from '@common/interfaces/friends-info';
import { UserIdName } from '@common/interfaces/user-id-name';
import { Injectable } from '@nestjs/common';
import { Database } from 'firebase-admin/lib/database/database';
@Injectable()
export class FriendsService {
    private database: Database;

    constructor(
        private readonly firebaseService: FirebaseRepositoryService,
        private readonly firebaseAuthService: FirebaseAuthService,
        private readonly friendsGateway: FriendsGateway,
    ) {
        this.database = this.firebaseService.database;
    }

    async getAllUsers(): Promise<UserIdName[]> {
        const listUsersResult = await this.firebaseAuthService.getUsers();
        const snapshot = await this.database.ref('users').once('value');
        const usersStatus = snapshot.exists() ? snapshot.val() : {};
        return listUsersResult.users.map((user) => ({
            id: user.uid,
            name: user.displayName || 'Unknown User',
            photoUrl: user.photoURL || '',
            isOnline: usersStatus[user.uid] ? usersStatus[user.uid].isOnline || false : false,
        }));
    }

    async getFriendsList(userId: string): Promise<UserIdName[]> {
        const snapshot = await this.database.ref(`users/${userId}/friends`).once('value');
        if (!snapshot.exists()) return [];
        const friendIds = Object.keys(snapshot.val());
        const friends: UserIdName[] = await Promise.all(
            friendIds.map(async (id) => {
                const userRecord = await this.firebaseAuthService.getUserById(id);
                const userSnapshot = await this.database.ref(`users/${id}`).once('value');
                const userData = userSnapshot.exists() ? userSnapshot.val() : {};
                return {
                    id,
                    name: userRecord.displayName || 'Unknown User',
                    photoUrl: userRecord.photoURL || '',
                    isOnline: userData.isOnline || false,
                };
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
                const userSnapshot = await this.database.ref(`users/${id}`).once('value');
                const userData = userSnapshot.exists() ? userSnapshot.val() : {};
                return {
                    id,
                    name: userRecord.displayName || 'Unknown User',
                    photoUrl: userRecord.photoURL || '',
                    isOnline: userData.isOnline || false,
                };
            }),
        );
        return requests;
    }

    async getSentRequests(userId: string): Promise<UserIdName[]> {
        const snapshot = await this.database.ref(`users/${userId}/friend_requests_sent`).once('value');
        if (!snapshot.exists()) return [];
        const requestIds = Object.keys(snapshot.val());
        const requests: UserIdName[] = await Promise.all(
            requestIds.map(async (id) => {
                const userRecord = await this.firebaseAuthService.getUserById(id);
                const userSnapshot = await this.database.ref(`users/${id}`).once('value');
                const userData = userSnapshot.exists() ? userSnapshot.val() : {};
                return {
                    id,
                    name: userRecord.displayName || 'Unknown User',
                    photoUrl: userRecord.photoURL || '',
                    isOnline: userData.isOnline || false,
                };
            }),
        );
        return requests;
    }

    async sendFriendRequest(fromUserId: string, toUserId: string): Promise<void> {
        await this.database.ref(`users/${fromUserId}/friend_requests_sent/${toUserId}`).set(true);
        await this.database.ref(`users/${toUserId}/friend_requests_received/${fromUserId}`).set(true);
        // TODO: add a notification for the recipient here.
        const friendsInfo: FriendsInfo = { user: fromUserId, friend: toUserId };
        this.friendsGateway.broadcastEvent(FriendsEvents.RequestSent, friendsInfo);
    }

    async acceptFriendRequest(userId: string, friendId: string): Promise<void> {
        await this.database.ref(`users/${userId}/friends/${friendId}`).set(true);
        await this.database.ref(`users/${friendId}/friends/${userId}`).set(true);
        await this.database.ref(`users/${userId}/friend_requests_received/${friendId}`).remove();
        await this.database.ref(`users/${friendId}/friend_requests_sent/${userId}`).remove();
        const friendsInfo: FriendsInfo = { user: userId, friend: friendId };
        this.friendsGateway.broadcastEvent(FriendsEvents.RequestAccepted, friendsInfo);
    }

    async rejectFriendRequest(userId: string, friendId: string): Promise<void> {
        await this.database.ref(`users/${userId}/friend_requests_received/${friendId}`).remove();
        await this.database.ref(`users/${friendId}/friend_requests_sent/${userId}`).remove();
        const friendsInfo: FriendsInfo = { user: userId, friend: friendId };
        this.friendsGateway.broadcastEvent(FriendsEvents.RequestRejected, friendsInfo);
    }

    async cancelRequest(userId: string, friendId: string): Promise<void> {
        await this.database.ref(`users/${userId}/friend_requests_sent/${friendId}`).remove();
        await this.database.ref(`users/${friendId}/friend_requests_received/${userId}`).remove();
        const friendsInfo: FriendsInfo = { user: userId, friend: friendId };
        this.friendsGateway.broadcastEvent(FriendsEvents.RequestCanceled, friendsInfo);
    }

    async removeFriend(userId: string, friendId: string): Promise<void> {
        await this.database.ref(`users/${userId}/friends/${friendId}`).remove();
        await this.database.ref(`users/${friendId}/friends/${userId}`).remove();
        const friendsInfo: FriendsInfo = { user: userId, friend: friendId };
        this.friendsGateway.broadcastEvent(FriendsEvents.FriendRemoved, friendsInfo);
    }

    async getFriendshipErrors(userId: string, isRoomCreation: boolean, friendId: string = ''): Promise<string> {
        let errors = '';
        const friendsList = await this.getFriendsList(userId);
        const isFriend = friendsList.some((friend) => friend.id === friendId);

        const errorConditions: Map<string, boolean> = new Map([
            [NOT_FRIENDS_WITH_HOST, !isFriend && !isRoomCreation],
            [NO_FRIENDS, friendsList.length === 0 && isRoomCreation],
        ]);

        errorConditions.forEach((hasError: boolean, message: string) => {
            if (hasError) errors += message;
        });

        return errors;
    }
}
