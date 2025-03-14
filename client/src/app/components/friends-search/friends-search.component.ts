import { Component, OnInit } from '@angular/core';
import { FormControl } from '@angular/forms';
import { FriendsService } from '@app/services/friends/friends.service';

@Component({
    selector: 'app-friends-search',
    templateUrl: './friends-search.component.html',
    styleUrls: ['./friends-search.component.scss'],
})
export class FriendsSearchComponent implements OnInit {
    searchControl = new FormControl('');

    constructor(public friendsService: FriendsService) {}

    ngOnInit(): void {
        this.searchControl.valueChanges.subscribe((query: string | null) => this.friendsService.searchUsers(query || ''));
    }
}
