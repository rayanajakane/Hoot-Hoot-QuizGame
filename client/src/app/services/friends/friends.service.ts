import { Injectable } from '@angular/core';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { SocketHandlerService } from '@app/services/socket-handler/socket-handler.service';
import { FriendsEvents } from '@common/events/friends.events';
import { UserIdName } from '@common/interfaces/user-id-name';

@Injectable({
    providedIn: 'root',
})
export class FriendsService {
    allUsers: UserIdName[] = [];
    friends: UserIdName[] = [];
    pendingRequests: UserIdName[] = [];
    sentRequests: UserIdName[] = [];
    searchResults: UserIdName[] = [];
    currentQuery: string = '';

    allDataLoaded: boolean = false;

    constructor(
        private readonly authService: AuthenticationService,
        private readonly socketHandler: SocketHandlerService,
    ) {}

    returnAllData() {
        this.allDataLoaded = false;
        this.onReturnUsers();
        this.socketHandler.send(FriendsEvents.ReturnAllData, this.authService.userId);
    }

    onReturnUsers() {
        let loadedDataTypes = 0;
        const totalDataTypes = 4;

        const checkAllDataLoaded = () => {
            loadedDataTypes++;
            if (loadedDataTypes >= totalDataTypes) {
                this.allDataLoaded = true;
            }
        };

        this.socketHandler.on(FriendsEvents.ReturnAllUsers, (data: UserIdName[]) => {
            this.allUsers = data;
            // this.searchResults = this.allUsers.filter((user) => !this.isFriend(user));
            this.searchUsers(this.currentQuery);
            checkAllDataLoaded();
        });
        this.socketHandler.on(FriendsEvents.ReturnAllFriends, (data: UserIdName[]) => {
            this.friends = data;
            checkAllDataLoaded();
        });
        this.socketHandler.on(FriendsEvents.ReturnAllPendingRequests, (data: UserIdName[]) => {
            this.pendingRequests = data;
            checkAllDataLoaded();
        });
        this.socketHandler.on(FriendsEvents.ReturnAllSentRequests, (data: UserIdName[]) => {
            this.sentRequests = data;
            checkAllDataLoaded();
        });
    }

    stopReturningUsers() {
        this.socketHandler.socket.removeListener(FriendsEvents.ReturnAllUsers);
        this.socketHandler.socket.removeListener(FriendsEvents.ReturnAllFriends);
        this.socketHandler.socket.removeListener(FriendsEvents.ReturnAllPendingRequests);
        this.socketHandler.socket.removeListener(FriendsEvents.ReturnAllSentRequests);
        this.currentQuery = '';
    }

    sendFriendRequest(toUserId: string) {
        this.socketHandler.send(FriendsEvents.RequestSent, { user: this.authService.userId, friend: toUserId });
    }

    acceptFriendRequest(friendId: string) {
        this.socketHandler.send(FriendsEvents.RequestAccepted, { user: this.authService.userId, friend: friendId });
    }

    rejectFriendRequest(friendId: string) {
        this.socketHandler.send(FriendsEvents.RequestRejected, { user: this.authService.userId, friend: friendId });
    }

    cancelRequest(friendId: string) {
        this.socketHandler.send(FriendsEvents.RequestCanceled, { user: this.authService.userId, friend: friendId });
    }

    removeFriend(friendId: string) {
        this.socketHandler.send(FriendsEvents.FriendRemoved, { user: this.authService.userId, friend: friendId });
    }

    searchUsers(query: string) {
        this.currentQuery = query;
        if (!query.trim()) {
            this.searchResults = this.allUsers.filter((user) => !this.isFriend(user));
        } else {
            const q = query.toLowerCase();
            this.searchResults = this.allUsers.filter((user) => (user.name || '').toLowerCase().includes(q) && !this.isFriend(user));
        }
    }

    isFriend(user: UserIdName): boolean {
        return this.friends.some((f) => f.id === user.id);
    }

    isRequestPending(user: UserIdName): boolean {
        return this.pendingRequests.some((r) => r.id === user.id);
    }

    isRequestSent(user: UserIdName): boolean {
        return this.sentRequests.some((r) => r.id === user.id);
    }

    isEligible(user: UserIdName): boolean {
        return !this.isFriend(user) && !this.isRequestPending(user) && !this.isRequestSent(user);
    }
}
