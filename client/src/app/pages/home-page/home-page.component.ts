import { HttpErrorResponse } from '@angular/common/http';
import { Component } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { DialogTextInputComponent } from '@app/components/dialog-text-input/dialog-text-input.component';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { JoinMatchService } from '@app/services/join-match/join-match.service';
import { NotificationService } from '@app/services/notification/notification.service';
import { TranslocoService } from '@jsverse/transloco';

@Component({
    selector: 'app-home-page',
    templateUrl: './home-page.component.html',
    styleUrls: ['./home-page.component.scss'],
})
export class HomePageComponent {
    password: string;
    input: string;
    // Services are required to decouple logic
    // eslint-disable-next-line max-params
    constructor(
        private readonly dialog: MatDialog,
        private readonly joinMatchService: JoinMatchService,
        private readonly notificationService: NotificationService,
        private readonly authenticationService: AuthenticationService,
        private readonly translocoService: TranslocoService,
    ) {}

    openJoinDialog(): void {
        const dialogRef = this.dialog.open(DialogTextInputComponent, {
            data: {
                input: this.input,
                title: this.translocoService.translate('main-page.join-match'),
                placeholder: this.translocoService.translate('main-page.access-code'),
            },
        });

        dialogRef.afterClosed().subscribe((result: string) => {
            if (result) {
                this.submitCode(result);
            }
        });
    }

    submitCode(roomCode: string): void {
        this.input = '';
        this.joinMatchService.matchRoomCode = '';
        this.joinMatchService.validateMatchRoomCode(roomCode).subscribe({
            next: () => {
                this.joinMatchService.matchRoomCode = roomCode;
                this.joinMatchService.validateUsername(this.authenticationService.userDisplayName);
            },
            error: (error: HttpErrorResponse) => {
                // this.notificationService.displayErrorMessage(`${JSON.parse(error.error)['message']}`);
                this.notificationService.displayErrorMessage(JSON.stringify(JSON.parse(error.error), null, 2));
                this.joinMatchService.matchRoomCode = '';
            },
        });
    }
}
