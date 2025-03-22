import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { MatchContext } from '@app/constants/states';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { MatchContextService } from '@app/services/match-context/match-context.service';
import { MatchRoomService } from '@app/services/match-room/match-room.service';
import { MatchService } from '@app/services/match/match.service';
import { TimeService } from '@app/services/time/time.service';

@Component({
    selector: 'app-wait-page',
    templateUrl: './wait-page.component.html',
    styleUrls: ['./wait-page.component.scss'],
})
export class WaitPageComponent implements OnInit {
    isLocked: boolean;
    isHostPlaying: boolean;
    qrCodeUrl: string;

    // permit more class parameters to decouple services
    // eslint-disable-next-line max-params
    constructor(
        public matchRoomService: MatchRoomService,
        public timeService: TimeService,
        public router: Router,
        public matchService: MatchService,
        private readonly matchContextService: MatchContextService,
        public authenticationService: AuthenticationService,
    ) {}

    get time() {
        return this.timeService.time;
    }

    get isHost() {
        return this.matchContextService.getContext() === MatchContext.HostView;
    }

    get currentGame() {
        return this.matchService.currentGame;
    }

    async ngOnInit(): Promise<void> {
        this.resetWaitPage();
        this.timeService.listenToTimerEvents();
        this.qrCodeUrl = await this.authenticationService.getImageDownloadUrl(`qrCodes/${this.matchRoomService.getRoomCode()}.png`);

        if (this.isHost) {
            this.matchRoomService.gameTitle = this.currentGame.title;
        } else {
            if (!this.matchContextService.getContext()) {
                this.matchContextService.setContext(MatchContext.PlayerView);
            }
        }
    }

    toggleLock() {
        this.matchRoomService.toggleLock();
        this.isLocked = this.isLocked ? false : true;
    }

    banPlayerId(userId: string) {
        if (userId === this.matchRoomService.getHostId()) return; // TODO: Migrate the logic to server, use UserID instead (need to track Host User ID in match room)
        this.matchRoomService.banUser(userId);
    }

    startMatch() {
        this.matchRoomService.startMatch();
    }

    quitGame() {
        this.matchRoomService.disconnectFromRoom();
    }

    private resetWaitPage() {
        this.isLocked = false;
        this.matchRoomService.isMatchStarted = false;
        this.matchRoomService.isHostPlaying = true;
        this.matchRoomService.isBanned = false;
        this.matchRoomService.isQuitting = false;
    }
}
