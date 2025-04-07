import { HttpClient, HttpErrorResponse, HttpResponse, HttpStatusCode } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { DialogTextInputComponent } from '@app/components/dialog-text-input/dialog-text-input.component';
import { ManagementState } from '@app/constants/states';
import { Game } from '@app/interfaces/game';
import { CommunicationService } from '@app/services/communication/communication.service';
import { NotificationService } from '@app/services/notification/notification.service';
import { translate } from '@jsverse/transloco';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class GameService extends CommunicationService<Game> {
    games: Game[];
    isLoadingGames: boolean;

    constructor(
        http: HttpClient,
        private readonly notificationService: NotificationService,
        private readonly dialog: MatDialog,
    ) {
        super(http, 'admin/games');
        this.games = [];
        this.isLoadingGames = false;
    }

    getGames(): void {
        this.isLoadingGames = true;
        this.getAll().subscribe({
            next: (data: Game[]) => {
                this.games = [...data];
                this.isLoadingGames = false;
            },
            error: (error: HttpErrorResponse) => {
                const message = error.message || '';
                const displayMessage = message
                    .split('\n')
                    .map((line) => translate(line.trim()))
                    .join(' ');
                this.notificationService.displayErrorMessage(`${translate('request-errors.error-could-not-get')}\n  ${displayMessage}`);
            },
        });
    }

    getGameById(id: string): Observable<Game> {
        return this.getById('', id);
    }

    deleteGame(id: string): void {
        this.delete(id).subscribe({
            next: () => (this.games = this.games.filter((game: Game) => game.id !== id)),
            error: (error: HttpErrorResponse) => {
                const message = error.message || '';
                const displayMessage = message
                    .split('\n')
                    .map((line) => translate(line.trim()))
                    .join(' ');
                this.notificationService.displayErrorMessage(`${translate('request-errors.error-could-not-delete')}\n ${displayMessage}`);
            },
        });
    }

    addGame(newGame: Game): Observable<HttpResponse<string>> {
        return this.add(newGame, '');
    }

    uploadGame(newGame: Game): void {
        this.addGame(newGame).subscribe({
            next: (response: HttpResponse<string>) => {
                if (response.body) {
                    newGame = JSON.parse(response.body);
                }
                newGame.isVisible = false;
                this.games.push(newGame);
                //TODO: transalte
                this.notificationService.displaySuccessMessage(translate('game-modification.creation-success'));
            },

            error: (error: HttpErrorResponse) => {
                if (error.message === translate('request-errors.error-game-same-title') || error.status === HttpStatusCode.Conflict) {
                    this.openDialog(newGame);
                } else {
                    const message = error.message || '';
                    const displayMessage = message
                        .split('\n')
                        .map((line) => translate(line.trim()))
                        .join(' ');
                    //TODO: transalte
                    this.notificationService.displayErrorMessage(`${translate('request-errors.error-could-not-add')} \n ${displayMessage}`);
                }
            },
        });
    }

    openDialog(newGame: Game): void {
        //TODO: transalte
        const dialogRef = this.dialog.open(DialogTextInputComponent, {
            data: { input: '', title: translate('game-modification.rename-game'), placeholder: translate('game-modification.title') },
        });

        dialogRef.afterClosed().subscribe((result: string) => {
            newGame.title = result;
            this.uploadGame(newGame);
        });
    }

    replaceGame(modifiedGame: Game) {
        return this.put(modifiedGame, modifiedGame.id);
    }

    submitGame(game: Game, state: ManagementState) {
        return state === ManagementState.GameModify ? this.replaceGame(game) : this.addGame(game);
    }
}
