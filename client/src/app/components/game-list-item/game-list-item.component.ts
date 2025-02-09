import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Game } from '@app/interfaces/game';

@Component({
    selector: 'app-game-list-item',
    templateUrl: './game-list-item.component.html',
    styleUrls: ['./game-list-item.component.scss'],
})
export class GameListItemComponent {
    @Input() game: Game;
    @Input() isAdminMode: boolean;
    @Output() deleteGameFromList: EventEmitter<string> = new EventEmitter<string>();

    constructor() {}

    deleteGame() {
        if (!this.isAdminMode) return;
        this.deleteGameFromList.emit(this.game.id);
    }
}
