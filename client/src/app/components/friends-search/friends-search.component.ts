import { Component, OnInit } from '@angular/core';
import { FormControl } from '@angular/forms';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { FriendsService } from '@app/services/friends/friends.service';
import { UserIdName } from '@common/interfaces/user-id-name';

@Component({
    selector: 'app-friends-search',
    templateUrl: './friends-search.component.html',
    styleUrls: ['./friends-search.component.scss'],
})
export class FriendsSearchComponent implements OnInit {
    allUsers: UserIdName[] = [];
    friends: UserIdName[] = [];
    pendingRequests: UserIdName[] = [];
    sentRequests: UserIdName[] = [];
    searchResults: UserIdName[] = [];
    currentUserID: string;
    searchControl = new FormControl('');

    constructor(
        private friendsService: FriendsService,
        private authService: AuthenticationService,
    ) {
        this.currentUserID = this.authService.userId;
    }

    ngOnInit(): void {
        this.searchControl.valueChanges.subscribe((query: string | null) => this.searchUsers(query || ''));
        this.loadData();
        this.friendsService.listenToAllFriendEvents((update, event) => {
            this.loadData();
        });
    }

    loadData(): void {
        this.friendsService.getFriendsList(this.currentUserID).subscribe((friends) => (this.friends = friends));
        this.friendsService.getPendingRequests(this.currentUserID).subscribe((requests) => (this.pendingRequests = requests));
        this.friendsService.getSentRequests(this.currentUserID).subscribe((sent) => (this.sentRequests = sent));
        this.friendsService.getAllUsers(this.currentUserID).subscribe((users) => {
            this.allUsers = users;
            this.searchResults = this.allUsers.filter((user) => !this.isFriend(user));
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

    sendFriendRequest(userId: string): void {
        this.friendsService.sendFriendRequest(this.currentUserID, userId).subscribe(() => {
            this.loadData();
        });
    }

    cancelRequest(userId: string): void {
        this.friendsService.cancelRequest(this.currentUserID, userId).subscribe(() => {
            this.loadData();
        });
    }

    acceptFriendRequest(userId: string): void {
        this.friendsService.acceptFriendRequest(this.currentUserID, userId).subscribe(() => {
            this.loadData();
        });
    }

    rejectFriendRequest(userId: string): void {
        this.friendsService.rejectFriendRequest(this.currentUserID, userId).subscribe(() => {
            this.loadData();
        });
    }

    removeFriend(userId: string): void {
        this.friendsService.removeFriend(this.currentUserID, userId).subscribe(() => {
            this.loadData();
        });
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
