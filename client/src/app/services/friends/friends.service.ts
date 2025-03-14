import { HttpClient, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { CommunicationService } from '@app/services/communication/communication.service';
import { SocketHandlerService } from '@app/services/socket-handler/socket-handler.service';
import { FriendsEvents } from '@common/events/friends.events';
import { FriendsInfo } from '@common/interfaces/friends-info';
import { UserIdName } from '@common/interfaces/user-id-name';
import { Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Injectable({
    providedIn: 'root',
})
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export class FriendsService extends CommunicationService<any> {
    allUsers: UserIdName[] = [];
    friends: UserIdName[] = [];
    pendingRequests: UserIdName[] = [];
    sentRequests: UserIdName[] = [];
    searchResults: UserIdName[] = [];
    currentUserID: string;

    constructor(
        http: HttpClient,
        private socketHandler: SocketHandlerService,
    ) {
        super(http, 'friends');
    }

    getAllUsers(): Observable<UserIdName[]> {
        return this.getAll(`all/${this.currentUserID}`);
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

    sendFriendRequest(toUserId: string): void {
        this.handleRequest(`send/${this.currentUserID}/${toUserId}`).subscribe({
            next: () => this.loadData(),
            error: (error) => console.error('Failed to send friend request', error),
        });
    }

    acceptFriendRequest(friendId: string): void {
        this.handleRequest(`accept/${this.currentUserID}/${friendId}`).subscribe({
            next: () => this.loadData(),
            error: (error) => console.error('Failed to accept friend request', error),
        });
    }

    rejectFriendRequest(friendId: string): void {
        this.handleRequest(`reject/${this.currentUserID}/${friendId}`).subscribe({
            next: () => this.loadData(),
            error: (error) => console.error('Failed to reject friend request', error),
        });
    }

    cancelRequest(friendId: string): void {
        this.delete(`cancel/${this.currentUserID}/${friendId}`).subscribe({
            next: () => this.loadData(),
            error: (error) => console.error('Failed to cancel friend request', error),
        });
    }

    removeFriend(friendId: string): void {
        this.delete(`remove/${this.currentUserID}/${friendId}`).subscribe({
            next: () => this.loadData(),
            error: (error) => console.error('Failed to remove friend', error),
        });
    }

    handleRequest(endpoint: string = ''): Observable<HttpResponse<string>> {
        return this.http
            .post(`${this.serverUrl}/${this.baseUrl}/${endpoint}`, null, this.httpOptions)
            .pipe(catchError(this.handleError<HttpResponse<string>>()));
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

    loadData(): void {
        this.getFriendsList(this.currentUserID).subscribe((friends) => (this.friends = friends));
        this.getPendingRequests(this.currentUserID).subscribe((requests) => (this.pendingRequests = requests));
        this.getSentRequests(this.currentUserID).subscribe((sent) => (this.sentRequests = sent));
        this.getAllUsers().subscribe((users) => {
            this.allUsers = users;
            // this.searchResults = this.allUsers.filter((user) => !this.isFriend(user));
            this.searchResults = this.allUsers;
        });
    }

    searchUsers(query: string): void {
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
