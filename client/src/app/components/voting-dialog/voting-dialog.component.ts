import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MatchContext } from '@app/constants/states';
import { Player } from '@app/interfaces/player';
import { VotingData } from '@app/interfaces/voting-data';
import { AnswerService } from '@app/services/answer/answer.service';
import { MatchContextService } from '@app/services/match-context/match-context.service';
import { MatchRoomService } from '@app/services/match-room/match-room.service';
import { MatchEvents } from '@common/events/match.events';
import { PresetAvatar } from '@app/constants/avatar-constants';

@Component({
    selector: 'app-voting-dialog',
    templateUrl: './voting-dialog.component.html',
    styleUrl: './voting-dialog.component.scss',
})
export class VotingDialogComponent {
    @Input() players: Player[] = [];
    @Output() vote: EventEmitter<string> = new EventEmitter<string>();
    selectedPlayer: string | null = null;
    showVotingDialog: boolean;
    voteCounts: VotingData = { username: '', numberOfVotes: 0, usersWhoVoted: [] };
    totalVotes: number;
    currentVotes: number;
    isVotingDisabled: boolean = true;
    usersWhoVoted: string[] = [];
    defaultAvatar = PresetAvatar.Default;

    constructor(
        public matchRoomService: MatchRoomService,
        public answerSerivce: AnswerService,
        public matchContextService: MatchContextService, 
    ) {}
    get playersPlaying() {
        return this.matchRoomService.players.filter((player) => player.isPlaying);
    }

    get totalVotesOfActivePlayers() {
       // return Object.values(this?.matchRoomService.votesResults).reduce((total, vote) => total + vote, 0);
       const activeUsernames = this.playersPlaying.map(player => player.username);
        const votes = this.matchRoomService.votesResults;
       return Object.entries(votes)
       .filter(([username]) => activeUsernames.includes(username))
       .reduce((total, [, vote]) => total + vote, 0);
    }

    ngOnInit() {
        this.setTheCheaterView();
    }
    get matchContext(): typeof MatchContext {
        return MatchContext;
    }

    handleDisconnect() {
        this.matchRoomService.disconnectFromRoom();
    }

    setTheCheaterView() {
        if (this.matchContextService.getContext() === MatchContext.HostView) {
            for (let player of this.matchRoomService.players) {
                this.matchRoomService.votesResults[player.username] ? this.matchRoomService.votesResults[player.username] : 0;
                console.log(this.matchRoomService.votesResults);
                this.currentVotes = this.matchRoomService.votesResults[player.username];
            }
        }
    }

    routeToResultsPage() {
        if (this.matchRoomService.isCheaterMode) {
            this.matchRoomService.socketService.socket.emit(MatchEvents.SendUpdatedScores, this.matchRoomService.getRoomCode());
        }
        this.matchRoomService.routeToResultsPage();
    }

    onVote() {
        if (this.selectedPlayer) {
            if (!this.voteCounts.username) {
                this.voteCounts.numberOfVotes = 0;
            }
            this.voteCounts.username = this.selectedPlayer;
            this.voteCounts.usersWhoVoted.push(this.matchRoomService.getUsername());
            this.voteCounts.numberOfVotes++;

            const votes = {
                username: this.voteCounts.username,
                numberOfVotes: this.voteCounts.numberOfVotes,
                usersWhoVoted: this.voteCounts.usersWhoVoted,
            };

            this.matchRoomService.sendBackVotesResult(votes);
            this.voteCounts.numberOfVotes = this.matchRoomService.votesData?.numberOfVotes;
        }
        this.isVotingDisabled = false;
    }
}
