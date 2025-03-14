import { HttpErrorResponse } from '@angular/common/http';
import { Component } from '@angular/core';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { JoinMatchService } from '@app/services/join-match/join-match.service';
import { NotificationService } from '@app/services/notification/notification.service';
import { MatchPageInfo } from '@common/interfaces/match-page-info';
@Component({
    selector: 'app-join-match-page',
    standalone: false,
    templateUrl: './join-match-page.component.html',
    styleUrl: './join-match-page.component.scss',
})
export class JoinMatchPageComponent {
    constructor(
        private readonly joinMatchService: JoinMatchService,
        private readonly notificationService: NotificationService,
        private readonly authenticationService: AuthenticationService,
    ) {}

    get unlockedMatches(): MatchPageInfo[] {
        return this.joinMatchService.matchesInfo.filter((match: MatchPageInfo) => !match.isLocked && !match.isPlaying);
    }

    get lockedMatches(): MatchPageInfo[] {
        return this.joinMatchService.matchesInfo.filter((match: MatchPageInfo) => match.isLocked && !match.isPlaying);
    }

    get playingMatches(): MatchPageInfo[] {
        return this.joinMatchService.matchesInfo.filter((match: MatchPageInfo) => match.isPlaying);
    }

    ngOnInit() {
        this.joinMatchService.getAllMatches();
    }
    ngOnDestroy() {
        this.joinMatchService.stopReturningAllMatches();
    }

    submitCode(roomCode: string): void {
        this.joinMatchService.matchRoomCode = '';
        this.joinMatchService.validateMatchRoomCode(roomCode).subscribe({
            next: () => {
                this.joinMatchService.matchRoomCode = roomCode;
                this.joinMatchService.validateUsername(this.authenticationService.userDisplayName, this.authenticationService.userId);
            },
            error: (error: HttpErrorResponse) => {
                this.notificationService.displayErrorMessage(`${JSON.parse(error.error)['message']}`);
                this.joinMatchService.matchRoomCode = '';
            },
        });
    }
}
