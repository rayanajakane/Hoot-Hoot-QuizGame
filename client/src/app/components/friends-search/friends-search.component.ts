import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormControl } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { DialogTextInputComponent } from '@app/components/dialog-text-input/dialog-text-input.component';
import { TextDialogData } from '@app/interfaces/dialog-data/text-dialog-data';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { FriendsService } from '@app/services/friends/friends.service';
import { MoneyService } from '@app/services/money/money.service';
import { Subscription } from 'rxjs';

@Component({
    selector: 'app-friends-search',
    templateUrl: './friends-search.component.html',
    styleUrls: ['./friends-search.component.scss'],
})
export class FriendsSearchComponent implements OnInit, OnDestroy {
    searchControl = new FormControl('');
    private searchSubscription: Subscription;

    constructor(
        public friendsService: FriendsService,
        private readonly authService: AuthenticationService,
        private moneyService: MoneyService,
        private dialog: MatDialog,
    ) {}

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

    onDonate(friendId: string): void {
        const dialogRef = this.dialog.open(DialogTextInputComponent, {
            data: {
                title: 'Enter Donation Amount',
                placeholder: 'Amount',
                input: '',
            } as TextDialogData,
        });

        dialogRef.afterClosed().subscribe((donationAmount: string) => {
            if (donationAmount) {
                this.moneyService.donateMoney(this.authService.userId, friendId, +donationAmount);
            }
        });
    }
}
