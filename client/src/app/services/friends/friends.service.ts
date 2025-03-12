import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { CommunicationService } from '@app/services/communication/communication.service';
import { SocketHandlerService } from '@app/services/socket-handler/socket-handler.service';
import { FriendsEvents } from '@common/events/friends.events';
import { FriendsInfo } from '@common/interfaces/friends-info';
import { UserIdName } from '@common/interfaces/user-id-name';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
@Injectable({
    providedIn: 'root',
})
export class FriendsService extends CommunicationService<any> {
    constructor(
        http: HttpClient,
        private socketHandler: SocketHandlerService,
    ) {
        super(http, 'friends');
    }

    getAllUsers(userId: string): Observable<UserIdName[]> {
        return this.getAll(`all/${userId}`);
    }

    getFriendsList(userId: string): Observable<UserIdName[]> {
        return this.getAll(`list/${userId}`);
    }

    getPendingRequests(userId: string): Observable<UserIdName[]> {
        return this.getAll(`requests/pending/${userId}`);
    }

    getSentRequests(userId: string): Observable<UserIdName[]> {
        return this.getAll(`requests/sent/${userId}`);
    }

    cancelRequest(userId: string, friendId: string): Observable<void> {
        return this.delete(`cancel/${userId}/${friendId}`).pipe(map(() => undefined));
    }

    sendFriendRequest(fromUserId: string, toUserId: string): Observable<void> {
        return this.add({}, `send/${fromUserId}/${toUserId}`).pipe(map(() => undefined));
    }

    acceptFriendRequest(userId: string, friendId: string): Observable<void> {
        return this.add({}, `accept/${userId}/${friendId}`).pipe(map(() => undefined));
    }

    rejectFriendRequest(userId: string, friendId: string): Observable<void> {
        return this.add({}, `reject/${userId}/${friendId}`).pipe(map(() => undefined));
    }

    removeFriend(userId: string, friendId: string): Observable<void> {
        return this.delete(`remove/${userId}/${friendId}`).pipe(map(() => undefined));
    }

    listenToRequestSent(callback: (update: FriendsInfo) => void): void {
        this.socketHandler.on<FriendsInfo>(FriendsEvents.RequestSent, callback);
    }

    listenToRequestAccepted(callback: (update: FriendsInfo) => void): void {
        this.socketHandler.on<FriendsInfo>(FriendsEvents.RequestAccepted, callback);
    }

    listenToRequestRejected(callback: (update: FriendsInfo) => void): void {
        this.socketHandler.on<FriendsInfo>(FriendsEvents.RequestRejected, callback);
    }

    listenToRequestCanceled(callback: (update: FriendsInfo) => void): void {
        this.socketHandler.on<FriendsInfo>(FriendsEvents.RequestCanceled, callback);
    }

    listenToFriendRemoved(callback: (update: FriendsInfo) => void): void {
        this.socketHandler.on<FriendsInfo>(FriendsEvents.FriendRemoved, callback);
    }

    listenToAllFriendEvents(callback: (update: FriendsInfo, event: string) => void): void {
        this.listenToRequestSent((update) => callback(update, FriendsEvents.RequestSent));
        this.listenToRequestAccepted((update) => callback(update, FriendsEvents.RequestAccepted));
        this.listenToRequestRejected((update) => callback(update, FriendsEvents.RequestRejected));
        this.listenToRequestCanceled((update) => callback(update, FriendsEvents.RequestCanceled));
        this.listenToFriendRemoved((update) => callback(update, FriendsEvents.FriendRemoved));
    }
}
