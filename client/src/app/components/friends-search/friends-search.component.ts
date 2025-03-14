import { Component, OnInit } from '@angular/core';
import { FormControl } from '@angular/forms';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { FriendsService } from '@app/services/friends/friends.service';

@Component({
    selector: 'app-friends-search',
    templateUrl: './friends-search.component.html',
    styleUrls: ['./friends-search.component.scss'],
})
export class FriendsSearchComponent implements OnInit {
    searchControl = new FormControl('');

    constructor(
        public friendsService: FriendsService,
        private authService: AuthenticationService,
    ) {}

    ngOnInit(): void {
        this.searchControl.valueChanges.subscribe((query: string | null) => this.friendsService.searchUsers(query || ''));
        this.friendsService.currentUserID = this.authService.userId;
        this.friendsService.loadData();
        this.friendsService.listenToAllFriendEvents((update, event) => {
            this.friendsService.loadData();
        });
    }

    sendFriendRequest(userId: string): void {
        this.friendsService.sendFriendRequest(userId);
    }

    cancelRequest(userId: string): void {
        this.friendsService.cancelRequest(userId);
    }

    acceptFriendRequest(userId: string): void {
        this.friendsService.acceptFriendRequest(userId);
    }

    rejectFriendRequest(userId: string): void {
        this.friendsService.rejectFriendRequest(userId);
    }

    removeFriend(userId: string): void {
        this.friendsService.removeFriend(userId);
    }
}
