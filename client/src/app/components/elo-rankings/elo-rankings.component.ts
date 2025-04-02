import { Component, OnDestroy, OnInit } from '@angular/core';
import { PresetAvatar } from '@app/constants/avatar-constants';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { EloService } from '@app/services/elo/elo.service';

@Component({
    selector: 'app-elo-rankings',
    templateUrl: './elo-rankings.component.html',
    styleUrl: './elo-rankings.component.scss',
})
export class EloRankingsComponent implements OnInit, OnDestroy {
    displayedColumns: string[] = ['rank', 'username', 'rating'];
    defaultAvatar = PresetAvatar.Default;

    constructor(
        public eloService: EloService,
        private readonly authService: AuthenticationService,
    ) {}

    ngOnInit(): void {
        this.eloService.listenForEloEvents();
        this.eloService.getElo(this.authService.userId);
        this.eloService.getRankings();
    }

    ngOnDestroy(): void {
        this.eloService.stopListeningForEloEvents();
    }
}
