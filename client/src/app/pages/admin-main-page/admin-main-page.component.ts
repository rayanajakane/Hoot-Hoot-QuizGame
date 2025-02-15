import { Component, OnInit } from '@angular/core';
import { Game } from '@app/interfaces/game';
import { GameService } from '@app/services/game/game.service';

@Component({
    selector: 'app-admin-main-page',
    templateUrl: './admin-main-page.component.html',
    styleUrl: './admin-main-page.component.scss',
})
export class AdminMainPageComponent implements OnInit {
    order: string = 'ascending';
    subject: string = 'date';

    constructor(readonly gameService: GameService) {}

    ngOnInit(): void {
        this.gameService.getGames();
    }

    onDeleteGameFromList(gameToDeleteId: string): void {
        this.gameService.deleteGame(gameToDeleteId);
    }

    addGame(newGame: Game): void {
        this.gameService.uploadGame(newGame);
    }
}
