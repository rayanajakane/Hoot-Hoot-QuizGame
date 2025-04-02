import { HttpResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormControl } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { PartyConfigDialogComponent } from '@app/components/party-config-dialog/party-config-dialog.component';
import { RandomModeStatus } from '@app/constants/feedback-messages';
import { RANDOM_MODE_GAME } from '@app/constants/question-creation';
import { MatchContext } from '@app/constants/states';
import { Game } from '@app/interfaces/game';
import { Question } from '@app/interfaces/question';
import { GameService } from '@app/services/game/game.service';
import { MatchContextService } from '@app/services/match-context/match-context.service';
import { MatchRoomService } from '@app/services/match-room/match-room.service';
import { MatchService } from '@app/services/match/match.service';
import { NotificationService } from '@app/services/notification/notification.service';
import { QuestionService } from '@app/services/question/question.service';
import { MINIMUM_QUESTIONS } from '@common/constants/match-constants';
import { QuestionType } from '@common/constants/question-types';
import { PartyConfig } from '@common/interfaces/party-config';
import { translate } from '@jsverse/transloco';

const N_POPULAR_GAMES = 3;
const MINIMUM_PLAYERS = 3;

@Component({
    selector: 'app-match-creation-page',
    templateUrl: './match-creation-page.component.html',
    styleUrls: ['./match-creation-page.component.scss'],
})
export class MatchCreationPageComponent implements OnInit {
    games: Game[] = [];
    searchResults: Game[] = [];
    currentTitleQuery: string = '';
    currentAuthorQuery: string = '';
    selectedGame: Game;
    gameIsValid: boolean;
    gameIsValidCheaterMode: boolean;
    matchContext = MatchContext;
    isRandomGame: boolean;
    isLoadingGames: boolean;
    isLoadingSelectedGame: boolean;
    mostPopularGames: Game[] = [];
    buttonClicked = false;
    titleSearchControl = new FormControl('');
    authorSearchControl = new FormControl('');

    partyConfig: PartyConfig = {
        isFriendsOnly: false,
        isEntryFeeRequired: false,
        entryFeeAmount: 0,
        isCheaterMode: false,
        canPlayCheaterMode: false,
    };

    // Services are required to decouple logic
    // eslint-disable-next-line max-params
    constructor(
        private readonly gameService: GameService,
        private readonly notificationService: NotificationService,
        private readonly matchService: MatchService,
        private readonly matchContextService: MatchContextService,
        private readonly questionService: QuestionService,
        private readonly matchRoomService: MatchRoomService,
        private readonly dialog: MatDialog,
    ) {
        this.gameIsValid = false;
        this.isRandomGame = false;
        this.gameIsValidCheaterMode = false;
        this.isLoadingGames = false;
        this.isLoadingSelectedGame = false;
    }

    ngOnInit(): void {
        this.reloadAllGames();
        this.titleSearchControl.valueChanges.subscribe((query: string | null) => this.searchGamesByTitle(query || '', false));
        this.authorSearchControl.valueChanges.subscribe((query: string | null) => this.searchGamesByAuthor(query || '', false));
    }

    searchGamesByTitle(query: string, isPrefiltered: boolean): void {
        this.currentTitleQuery = query;
        const gamesToFilter = isPrefiltered ? this.searchResults : this.games;
        if (!query.trim()) {
            this.searchResults = gamesToFilter;
        } else {
            const q = query.toLowerCase();
            this.searchResults = gamesToFilter.filter((game) => game.title.toLowerCase().includes(q));
        }
        if (!isPrefiltered) {
            this.searchGamesByAuthor(this.currentAuthorQuery, true);
        }
    }

    searchGamesByAuthor(query: string, isPrefiltered: boolean): void {
        this.currentAuthorQuery = query;
        const gamesToFilter = isPrefiltered ? this.searchResults : this.games;
        if (!query.trim()) {
            this.searchResults = isPrefiltered ? this.searchResults : this.games;
        } else {
            const q = query.toLowerCase();
            this.searchResults = gamesToFilter.filter((game) => (game.authorName ? game.authorName.toLowerCase().includes(q) : false));
        }
        if (!isPrefiltered) {
            this.searchGamesByTitle(this.currentTitleQuery, true);
        }
    }

    reloadAllGames(): void {
        this.isLoadingGames = true;
        this.matchService.getAllGames().subscribe((data: Game[]) => {
            this.games = data;
            this.isLoadingGames = false;
            this.sortMostPopularGames();
            this.searchGamesByTitle(this.currentTitleQuery, false);
            this.searchGamesByAuthor(this.currentAuthorQuery, true);
        });
    }

    handleLoadRandomGame(data: Question[]) {
        const questionsCount = [...data].length;
        if (this.hasEnoughRandomQuestions(questionsCount)) {
            this.selectedGame = RANDOM_MODE_GAME;
        }
        this.isLoadingSelectedGame = false;
    }

    loadRandomGame(): void {
        this.isLoadingSelectedGame = true;
        this.questionService.getAllQuestions().subscribe({
            next: (data: Question[]) => {
                data = data.filter((question) => question.type === QuestionType.MultipleChoice);
                this.handleLoadRandomGame(data);
            },
        });
    }

    hasEnoughRandomQuestions(questionsCount: number): boolean {
        if (questionsCount < MINIMUM_QUESTIONS) {
            this.notificationService.displayErrorMessage(RandomModeStatus.FAILURE);
            this.isRandomGame = this.gameIsValid = false;
            return false;
        }
        this.isRandomGame = this.gameIsValid = true;
        return true;
    }

    hasCorrectType(questions: Question[]): boolean {
        for (let element of questions) {
            if (element.type === 'QRL') {
                this.gameIsValidCheaterMode = false;
                this.partyConfig.canPlayCheaterMode = false;
                // this.partyConfig.isCheaterMode = false;
                console.log('element', element.type);
                return false;
            }
        }
        this.gameIsValidCheaterMode = true;
        this.partyConfig.canPlayCheaterMode = true;
        return true;
    }

    hasEnoughPlayers(playersCount: number) {
        if (playersCount < MINIMUM_PLAYERS) {
            this.gameIsValidCheaterMode = false;
            this.partyConfig.canPlayCheaterMode = false;
            return false;
        }
        this.partyConfig.canPlayCheaterMode = true;
        return true;
    }

    loadSelectedGame(selectedGame: Game): void {
        this.isRandomGame = false;
        this.gameService.getGameById(selectedGame.id).subscribe({
            next: (data: Game) => {
                this.selectedGame = data;
                this.validateGame(this.selectedGame);
                this.isLoadingSelectedGame = false;
            },
            error: () => {
                const snackBarRef = this.notificationService.displayErrorMessageAction(
                    translate('feedback-messages.deleted'),
                    translate('feedback-messages.refresh'),
                );
                snackBarRef.onAction().subscribe(() => this.reloadAllGames());
            },
        });
    }

    reloadSelectedGame(): void {
        this.isRandomGame = false;
        this.gameService.getGameById(this.selectedGame.id).subscribe({
            next: (data: Game) => {
                this.selectedGame = data;
                this.revalidateGame();
            },
            error: () => {
                const snackBarRef = this.notificationService.displayErrorMessageAction(
                    translate('feedback-messages.deleted'),
                    translate('feedback-messages.refresh'),
                );
                snackBarRef.onAction().subscribe(() => this.reloadAllGames());
            },
        });
    }

    validateGame(selectedGame: Game): void {
        if (selectedGame.isVisible) {
            this.gameIsValid = true;
            this.hasCorrectType(selectedGame.questions);
        } else {
            const snackBarRef = this.notificationService.displayErrorMessageAction(
                translate('feedback-messages.invisible'),
                translate('feedback-messages.refresh'),
            );
            snackBarRef.onAction().subscribe(() => this.reloadAllGames());
        }
    }

    revalidateGame(): void {
        if (this.selectedGame.isVisible) {
            this.gameIsValid = true;
            // this.partyConfig.isCheaterMode = true;
            this.partyConfig.canPlayCheaterMode = true;
            this.hasCorrectType(this.selectedGame.questions);
            this.matchService.currentGame = this.selectedGame;
            this.matchService.saveBackupGame(this.selectedGame.id).subscribe((response: HttpResponse<string>) => {
                if (response.body) {
                    const backupGame = JSON.parse(response.body);
                    this.matchService.currentGame = backupGame;
                    if (this.partyConfig.canPlayCheaterMode && this.hasCorrectType(backupGame.questions) && this.partyConfig.isCheaterMode) {
                        this.matchService.createMatch(
                            (this.partyConfig = {
                                isFriendsOnly: this.partyConfig.isFriendsOnly,
                                isEntryFeeRequired: this.partyConfig.isEntryFeeRequired,
                                entryFeeAmount: this.partyConfig.entryFeeAmount,
                                isCheaterMode: this.partyConfig.isCheaterMode,
                                canPlayCheaterMode: this.partyConfig.canPlayCheaterMode,
                            }),
                        );
                    } else {
                        this.matchRoomService.isCheaterMode = false;
                        this.matchService.createMatch(this.partyConfig, true);
                    }
                    //  this.matchService.createMatch(this.partyConfig);
                }
            });
        } else {
            const snackBarRef = this.notificationService.displayErrorMessageAction(
                translate('feedback-messages.invisible'),
                translate('feedback-messages.refresh'),
            );
            snackBarRef.onAction().subscribe(() => this.reloadAllGames());
        }
    }

    createMatch(context: MatchContext): void {
        this.buttonClicked = true;
        this.matchContextService.setContext(context);
        this.reloadSelectedGame();
    }

    createMatchCheaterMode(context: MatchContext) {
        this.buttonClicked = true; //we can set it here
        this.matchRoomService.isCheaterMode = true;
        console.log('cheater', this.matchRoomService.isCheaterMode);
        this.matchContextService.setContext(context);
        this.reloadSelectedGame();
    }

    openPartyConfigDialog(): void {
        if (!this.gameIsValid) {
            return;
        }

        const dialogRef = this.dialog.open(PartyConfigDialogComponent, {
            width: '400px',
            data: { ...this.partyConfig },
        });

        dialogRef.afterClosed().subscribe((result) => {
            if (result) {
                this.partyConfig = result;
                console.log('match', this.partyConfig.isCheaterMode);
                if (this.partyConfig.isCheaterMode) {
                    this.matchRoomService.isCheaterMode = true;
                    this.createMatchCheaterMode(this.matchContext.HostView);
                } else this.createMatch(this.matchContext.HostView);
            }
        });
    }

    createStandardMatch(): void {
        this.matchRoomService.isCheaterMode = false;
        this.partyConfig = {
            isFriendsOnly: false,
            isEntryFeeRequired: false,
            entryFeeAmount: 0,
            isCheaterMode: false,
            canPlayCheaterMode: false,
        };
        this.createMatch(this.matchContext.HostView);
    }

    createFriendsOnlyMatch(): void {
        this.partyConfig = {
            isFriendsOnly: true,
            isEntryFeeRequired: false,
            entryFeeAmount: 0,
            isCheaterMode: false,
            canPlayCheaterMode: false,
        };
        this.createMatch(this.matchContext.HostView);
    }

    private sortMostPopularGames() {
        if (this.games.length <= N_POPULAR_GAMES) this.mostPopularGames = this.games;
        const sortedGames = this.games.sort((gameX: Game, gameY: Game) => {
            if (!gameX.nMatchesPlayed) gameX.nMatchesPlayed = 0;
            if (!gameY.nMatchesPlayed) gameY.nMatchesPlayed = 0;
            return gameY.nMatchesPlayed - gameX.nMatchesPlayed;
        });
        this.mostPopularGames = sortedGames.slice(0, N_POPULAR_GAMES);
        return this.mostPopularGames;
    }
}
