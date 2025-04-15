import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { JoinMatchService } from '@app/services/join-match/join-match.service';
import { NotificationService } from '@app/services/notification/notification.service';
import { MatchPageInfo } from '@common/interfaces/match-page-info';
import { translate } from '@jsverse/transloco';
import { Subject, Subscription } from 'rxjs';
import { debounceTime } from 'rxjs/operators';

@Component({
    selector: 'app-join-match-page',
    standalone: false,
    templateUrl: './join-match-page.component.html',
    styleUrl: './join-match-page.component.scss',
})
export class JoinMatchPageComponent implements OnInit, OnDestroy {
    private joinCodeClickSubject = new Subject<string>();
    private joinCodeClickSubscription: Subscription;

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

        this.joinCodeClickSubscription = this.joinCodeClickSubject
            .pipe(debounceTime(500))
            .subscribe((roomCode: string) => this.processJoinCode(roomCode));
    }

    ngOnDestroy() {
        this.joinMatchService.stopReturningAllMatches();
        if (this.joinCodeClickSubscription) {
            this.joinCodeClickSubscription.unsubscribe();
        }
    }

    submitCode(roomCode: string): void {
        this.joinCodeClickSubject.next(roomCode);
    }

    private processJoinCode(roomCode: string): void {
        this.joinMatchService.matchRoomCode = '';
        this.joinMatchService.validateMatchRoomCode(roomCode).subscribe({
            next: () => {
                this.joinMatchService.matchRoomCode = roomCode;
                this.joinMatchService.validateUsername(this.authenticationService.userDisplayName, this.authenticationService.userId);
            },
            error: (error: HttpErrorResponse) => {
                console.error(error);
                const message = JSON.parse(error.error)['message'];
                const displayMessage = translate(message);
                this.notificationService.displayErrorMessage(`${displayMessage}`);
            },
        });
    }
}
