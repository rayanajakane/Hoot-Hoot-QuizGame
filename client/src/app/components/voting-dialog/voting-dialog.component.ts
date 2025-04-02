import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Player } from '@app/interfaces/player';
import { VotingData } from '@app/interfaces/voting-data';
//import { AnswerService } from '@app/services/answer/answer.service';
import { MatchRoomService } from '@app/services/match-room/match-room.service';
import { MatchEvents } from '@common/events/match.events';


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
    voteCounts: VotingData = {username: "", numberOfVotes: 0, usersWhoVoted: []};
    totalVotes: number;
    

    constructor(
        public matchRoomService: MatchRoomService,
        private dialog: MatDialog, // private readonly dialog: MatDialog,
      //  private answerService: AnswerService,
    ) {}

    onVote() {
        if (this.selectedPlayer) {
            if (!this.voteCounts.username) {
                this.voteCounts.numberOfVotes = 0;
            }
            this.voteCounts.username = this.selectedPlayer;
            this.voteCounts.usersWhoVoted.push(this.matchRoomService.getUsername());
            this.voteCounts.numberOfVotes++;
            this.totalVotes++;
           const votes ={username: this.voteCounts.username, numberOfVotes: this.voteCounts.numberOfVotes++, usersWhoVoted: this.voteCounts.usersWhoVoted}
           this.matchRoomService.sendBackVotesResult(votes);
        }
        this.closeDialog();

        //const player = this.matchRoomService.getPlayerByUsername(this.voteCounts.username);
        this.matchRoomService.socketService.socket.emit(MatchEvents.SendUpdatedScores, this.matchRoomService.getRoomCode());
    }

    closeDialog() {
        this.dialog.closeAll();
    }
}
