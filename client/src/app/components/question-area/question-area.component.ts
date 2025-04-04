import { Component, HostListener, OnInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { AlertComponent } from '@app/components/alert/alert.component';
import { MatchContext } from '@app/constants/states';
import { AnswerService } from '@app/services/answer/answer.service';
import { AudioPlayerService } from '@app/services/audio-player/audio-player.service';
import { MatchContextService } from '@app/services/match-context/match-context.service';
import { MatchRoomService } from '@app/services/match-room/match-room.service';
import { NotificationService } from '@app/services/notification/notification.service';
import { TimeService } from '@app/services/time/time.service';
import { AnswerCorrectness } from '@common/constants/answer-correctness';
import { QuestionType } from '@common/constants/question-types';
//import { MatchEvents } from '@common/events/match.events';
import { PartyConfig } from '@common/interfaces/party-config';
import { TranslocoService } from '@jsverse/transloco';
@Component({
    selector: 'app-question-area',
    templateUrl: './question-area.component.html',
    styleUrls: ['./question-area.component.scss'],
})
export class QuestionAreaComponent implements OnInit {
    @ViewChild('panicAlert') panicAlert: AlertComponent;

    gameDuration: number;
    context: MatchContext;
    isFirstQuestion: boolean = true;
    showVotingDialog: boolean;
    partyConfig: PartyConfig;

    // Allow more constructor parameters to decouple services
    // eslint-disable-next-line max-params
    constructor(
        public matchRoomService: MatchRoomService,
        public timeService: TimeService,
        public answerService: AnswerService,
        public audioService: AudioPlayerService,
        public notificationService: NotificationService,
        public router: Router,
        private translocoService: TranslocoService,
        private readonly matchContextService: MatchContextService,
    ) {}

    get time() {
        return this.timeService.time;
    }

    get currentQuestion() {
        return this.matchRoomService.currentQuestion;
    }

    get answerOptions(): typeof AnswerCorrectness {
        return AnswerCorrectness;
    }

    get matchContext(): typeof MatchContext {
        return MatchContext;
    }

    get questionType(): typeof QuestionType {
        return QuestionType;
    }

    @HostListener('document:keydown', ['$event'])
    handleKeyboardEvent(event: KeyboardEvent) {
        if (document?.activeElement?.id === 'chat-input') return;

        if (event.key === 'Enter' && this.answerService.isSelectionEnabled) {
            this.submitAnswers();
            return;
        }
    }

    ngOnInit(): void {
        this.resetStateForNewQuestion();
        this.listenToGameEvents();
        this.matchRoomService.isQuitting = false;
        this.answerService.playerScore = 0;
        this.context = this.matchContextService.getContext();
        if (this.isFirstQuestion) {
            this.isFirstQuestion = false;
        }

        if (this.matchRoomService.isCheaterMode) {
            if (this.matchRoomService.getUsername() === this.matchRoomService.cheaterPlayer?.username) {
                this.matchContextService.setContext(MatchContext.CheaterView);
                this.notificationService.notifyCheaterPlayer(
                    this.matchRoomService.cheaterPlayer.username,
                    this.translocoService.translate('cheater-mode.notifyCheater'),
                );
            }
            if (this.matchContextService.getContext() === MatchContext.PlayerView) {
                console.log(this.matchRoomService.getUsername());
                this.notificationService.notifyRegularPlayer(
                    this.matchRoomService.getUsername(),
                    this.translocoService.translate('cheater-mode.notifyRegularPlayer'),
                );
            }
        }

        this.matchContextService.getContext();
    }

    // ngOnChanges(): void {
    //     if (this.answerService.isEndGame && !this.matchRoomService.isCooldown) {
    //         this.matchRoomService.goToVoting();
    //     }
    // }

    submitAnswers(): void {
        this.answerService.submitAnswer({ userId: this.matchRoomService.getUserId(), roomCode: this.matchRoomService.getRoomCode() });
    }

    goToNextQuestion() {
        this.matchRoomService.goToNextQuestion();
        this.answerService.isNextQuestionButtonEnabled = false;
    }

    

    routeToResultsPage() {
        this.matchRoomService.routeToResultsPage();
    }

    quitGame() {
        this.matchRoomService.isQuitting = true;
        this.matchRoomService.disconnectFromRoom();
    }

    triggerPanicTimer() {
        const roomCode = this.matchRoomService.getRoomCode();
        this.timeService.triggerPanicTimer(roomCode);
    }

    pauseTimer() {
        this.timeService.pauseTimer(this.matchRoomService.getRoomCode());
    }

    private listenToGameEvents() {
        this.timeService.listenToTimerEvents();
        this.answerService.listenToAnswerEvents();
        this.audioService.onPanicTimer();
    }

    private resetStateForNewQuestion(): void {
        this.answerService.resetStateForNewQuestion();
    }

    voteOnCheater() {
        this.matchRoomService.voteOnCheater();
    }
}
