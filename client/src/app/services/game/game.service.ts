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
                this.notificationService.displayErrorMessage(`Échec d'obtention des jeux 😿\n  ${displayMessage}`);
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
                this.notificationService.displayErrorMessage(`Échec de supression du jeu 😿\n ${displayMessage}`);
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
                this.notificationService.displaySuccessMessage('Jeu ajouté avec succès! 😺');
            },
            error: (error: HttpErrorResponse) => {
                if (error.message === 'Un jeu du même titre existe déjà.' || error.status === HttpStatusCode.Conflict) {
                    this.openDialog(newGame);
                } else {
                    const message = error.message || '';
                    const displayMessage = message
                        .split('\n')
                        .map((line) => translate(line.trim()))
                        .join(' ');
                    this.notificationService.displayErrorMessage(`Le jeu n'a pas pu être ajouté. 😿 \n ${displayMessage}`);
                }
            },
        });
    }

    openDialog(newGame: Game): void {
        const dialogRef = this.dialog.open(DialogTextInputComponent, {
            data: { input: '', title: 'Veillez renommer le jeu.', placeholder: 'Nouveau titre' },
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
