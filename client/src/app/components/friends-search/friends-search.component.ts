import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormControl } from '@angular/forms';
import { FriendsService } from '@app/services/friends/friends.service';
import { Subscription } from 'rxjs';

@Component({
    selector: 'app-friends-search',
    templateUrl: './friends-search.component.html',
    styleUrls: ['./friends-search.component.scss'],
})
export class FriendsSearchComponent implements OnInit, OnDestroy {
    searchControl = new FormControl('');
    private searchSubscription: Subscription;

    constructor(public friendsService: FriendsService) {}

    ngOnInit(): void {
        this.friendsService.returnAllData();
        this.searchSubscription = this.searchControl.valueChanges.subscribe((query: string | null) => this.friendsService.searchUsers(query || ''));
    }

    ngOnDestroy(): void {
        this.friendsService.stopReturningUsers();
        if (this.searchSubscription) {
            this.searchSubscription.unsubscribe();
        }
    }
}
