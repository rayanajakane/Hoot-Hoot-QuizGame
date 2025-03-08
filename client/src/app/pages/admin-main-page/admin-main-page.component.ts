import { Component, OnInit } from '@angular/core';
import { Game } from '@app/interfaces/game';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { GameService } from '@app/services/game/game.service';

@Component({
    selector: 'app-admin-main-page',
    templateUrl: './admin-main-page.component.html',
    styleUrl: './admin-main-page.component.scss',
})
export class AdminMainPageComponent implements OnInit {
    order: string = 'ascending';
    subject: string = 'date';

    constructor(
        readonly gameService: GameService,
        public authenticationService: AuthenticationService,
    ) {}

    ngOnInit(): void {
        this.gameService.getGames();
    }

    async onDeleteGameFromList(gameToDeleteId: string) {
        this.gameService.deleteGame(gameToDeleteId);
    }

    addGame(newGame: Game): void {
        this.gameService.uploadGame(newGame);
    }
}
