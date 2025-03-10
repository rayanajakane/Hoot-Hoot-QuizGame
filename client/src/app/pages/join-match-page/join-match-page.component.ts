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
    input: string;

    constructor(
        private readonly joinMatchService: JoinMatchService,
        private readonly notificationService: NotificationService,
        private readonly authenticationService: AuthenticationService,
    ) {}

    get matches(): MatchPageInfo[] {
        const mocks = [
            {
                code: '1234',
                isLocked: false,
                isPlaying: false,
                gameTitle: 'Jeu cute',
                nPlayers: 1,
            },
            {
                code: '1234',
                isLocked: false,
                isPlaying: false,
                gameTitle: 'Jeu cute',
                nPlayers: 1,
            },
            {
                code: '1234',
                isLocked: false,
                isPlaying: false,
                gameTitle: 'Jeu cute',
                nPlayers: 1,
            },
            {
                code: '1234',
                isLocked: false,
                isPlaying: false,
                gameTitle: 'Jeu cute',
                nPlayers: 1,
            },
            {
                code: '1234',
                isLocked: false,
                isPlaying: false,
                gameTitle: 'Jeu cute',
                nPlayers: 1,
            },
            {
                code: '1234',
                isLocked: false,
                isPlaying: false,
                gameTitle: 'Jeu cute',
                nPlayers: 1,
            },
            {
                code: '1234',
                isLocked: false,
                isPlaying: false,
                gameTitle: 'Jeu cute',
                nPlayers: 1,
            },
            {
                code: '1234',
                isLocked: true,
                isPlaying: false,
                gameTitle: 'Jeu cute',
                nPlayers: 1,
            },
            {
                code: '1234',
                isLocked: true,
                isPlaying: true,
                gameTitle: 'Jeu cute',
                nPlayers: 1,
            },
        ];
        console.log(mocks);
        return this.joinMatchService.matchesInfo;
        return mocks;
    }

    ngOnInit() {
        this.joinMatchService.getAllMatches();
    }
    ngOnDestroy() {
        this.joinMatchService.stopReturningAllMatches();
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
                this.notificationService.displayErrorMessage(`${JSON.parse(error.error)['message']}`);
                this.joinMatchService.matchRoomCode = '';
            },
        });
    }
}
