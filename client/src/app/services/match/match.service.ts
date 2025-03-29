import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Game } from '@app/interfaces/game';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { CommunicationService } from '@app/services/communication/communication.service';
import { MatchRoomService } from '@app/services/match-room/match-room.service';
import { PartyConfig } from '@common/interfaces/party-config';
import { Subject } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class MatchService extends CommunicationService<Game> {
    questionAdvanced: Subject<void>;
    currentGame: Game;

    questionId: string;
    private questionAdvanceSubject = new Subject<void>();

    constructor(
        http: HttpClient,
        private readonly matchRoomService: MatchRoomService,
        private readonly authenticationService: AuthenticationService,
    ) {
        super(http, 'match');
    }

    get questionAdvanced$() {
        return this.questionAdvanceSubject.asObservable();
    }

    getAllGames() {
        return this.getAll('games');
    }

    advanceQuestion() {
        this.questionAdvanceSubject.next();
    }

    getBackupGame(id: string) {
        return this.getById(id, 'backups');
    }

    saveBackupGame(id: string) {
        return this.add(this.currentGame, `backups/${id}`);
    }

    deleteBackupGame(id: string) {
        return this.delete(`backups/${id}`);
    }

    createMatch(
        partyConfig: PartyConfig = { isFriendsOnly: false, isEntryFeeRequired: false, isCheaterMode: false , canPlayCheaterMode: false},
        isClassicMode: boolean = false,
    ) {
        const hostId = this.authenticationService.userId;
        const hostUsername = this.authenticationService.userDisplayName;
        this.matchRoomService.connect();
        console.log(isClassicMode);
        if (isClassicMode) {
            this.matchRoomService.createRoom(this.currentGame.id, hostId, hostUsername, isClassicMode, partyConfig);
        } else {
            this.matchRoomService.createRoom(this.currentGame.id, hostId, hostUsername, (isClassicMode = false), partyConfig);
        }
        //  this.matchRoomService.createRoom(this.currentGame.id, hostId, hostUsername, isClassicMode, partyConfig);
    }
}
