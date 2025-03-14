import { HttpClient, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { CommunicationService } from '@app/services/communication/communication.service';
import { SocketHandlerService } from '@app/services/socket-handler/socket-handler.service';
import { FriendsEvents } from '@common/events/friends.events';
import { FriendsInfo } from '@common/interfaces/friends-info';
import { UserIdName } from '@common/interfaces/user-id-name';
import { Observable } from 'rxjs';
import { catchError, take } from 'rxjs/operators';

@Injectable({
    providedIn: 'root',
})
export class FriendsService extends CommunicationService<UserIdName> {
    allUsers: UserIdName[] = [];
    friends: UserIdName[] = [];
    pendingRequests: UserIdName[] = [];
    sentRequests: UserIdName[] = [];
    searchResults: UserIdName[] = [];

    constructor(
        http: HttpClient,
        private readonly authService: AuthenticationService,
        private readonly socketHandler: SocketHandlerService,
    ) {
        super(http, 'friends');
        this.loadUsers();
        this.listenToAllFriendEvents((update, event) => {
            this.loadUsers();
        });
    }

    sendFriendRequest(toUserId: string): void {
        this.handleRequest(`send/${this.authService.userId}/${toUserId}`)
            .pipe(take(1))
            .subscribe({
                next: () => this.loadUsers(),
                error: (error) => console.error('Failed to send friend request', error),
            });
    }

    acceptFriendRequest(friendId: string): void {
        this.handleRequest(`accept/${this.authService.userId}/${friendId}`)
            .pipe(take(1))
            .subscribe({
                next: () => this.loadUsers(),
                error: (error) => console.error('Failed to accept friend request', error),
            });
    }

    rejectFriendRequest(friendId: string): void {
        this.handleRequest(`reject/${this.authService.userId}/${friendId}`)
            .pipe(take(1))
            .subscribe({
                next: () => this.loadUsers(),
                error: (error) => console.error('Failed to reject friend request', error),
            });
    }

    cancelRequest(friendId: string): void {
        this.delete(`cancel/${this.authService.userId}/${friendId}`)
            .pipe(take(1))
            .subscribe({
                next: () => this.loadUsers(),
                error: (error) => console.error('Failed to cancel friend request', error),
            });
    }

    removeFriend(friendId: string): void {
        this.delete(`remove/${this.authService.userId}/${friendId}`)
            .pipe(take(1))
            .subscribe({
                next: () => this.loadUsers(),
                error: (error) => console.error('Failed to remove friend', error),
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

    private getAllUsers(): void {
        this.getAll(`all/${this.authService.userId}`)
            .pipe(take(1))
            .subscribe({
                next: (users) => {
                    this.allUsers = users;
                    // this.searchResults = this.allUsers.filter((user) => !this.isFriend(user));
                    this.searchResults = this.allUsers;
                },
                error: (error) => console.error('Failed to fetch all users', error),
            });
    }

    private getFriendsList(): void {
        this.getAll(`list/${this.authService.userId}`)
            .pipe(take(1))
            .subscribe({
                next: (friends) => (this.friends = friends),
                error: (error) => console.error('Failed to fetch friends list', error),
            });
    }

    private getPendingRequests(): void {
        this.getAll(`requests/pending/${this.authService.userId}`)
            .pipe(take(1))
            .subscribe({
                next: (requests) => (this.pendingRequests = requests),
                error: (error) => console.error('Failed to fetch pending requests', error),
            });
    }

    private getSentRequests(): void {
        this.getAll(`requests/sent/${this.authService.userId}`)
            .pipe(take(1))
            .subscribe({
                next: (requests) => (this.sentRequests = requests),
                error: (error) => console.error('Failed to fetch sent requests', error),
            });
    }

    private handleRequest(endpoint: string = ''): Observable<HttpResponse<string>> {
        return this.http
            .post(`${this.serverUrl}/${this.baseUrl}/${endpoint}`, null, this.httpOptions)
            .pipe(catchError(this.handleError<HttpResponse<string>>()));
    }

    private loadUsers(): void {
        this.getAllUsers();
        this.getFriendsList();
        this.getPendingRequests();
        this.getSentRequests();
    }

    private listenToAllFriendEvents(callback: (update: FriendsInfo, event: string) => void): void {
        const events = [
            FriendsEvents.RequestSent,
            FriendsEvents.RequestAccepted,
            FriendsEvents.RequestRejected,
            FriendsEvents.RequestCanceled,
            FriendsEvents.FriendRemoved,
        ];

        events.forEach((event) => {
            this.socketHandler.on<FriendsInfo>(event, (update) => callback(update, event));
        });
    }
}
