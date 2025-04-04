import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { ChatChannel } from '@app/constants/chat-channels';
import { MatchContext } from '@app/constants/states';
import { Player } from '@app/interfaces/player';
import { Question } from '@app/interfaces/question';
import { ChatService } from '@app/services/chat/chat.service';
import { MatchContextService } from '@app/services/match-context/match-context.service';
import { NotificationService } from '@app/services/notification/notification.service';
import { SocketHandlerService } from '@app/services/socket-handler/socket-handler.service';
import { ChatEvents } from '@common/events/chat.events';
import { MatchEvents } from '@common/events/match.events';
import { PartyConfig } from '@common/interfaces/party-config';
import { UserInfo } from '@common/interfaces/user-info';
import { translate } from '@jsverse/transloco';
@Injectable({
    providedIn: 'root',
})
export class MatchRoomService {
    players: Player[];
    isMatchStarted: boolean;
    isResults: boolean;
    isWaitOver: boolean;
    isBanned: boolean;
    isPlaying: boolean;
    gameTitle: string;
    gameDuration: number;
    currentQuestion: Question;
    isHostPlaying: boolean;
    isCooldown: boolean;
    isQuitting: boolean;
    partyConfig: PartyConfig;

    currentAnswers: string[] = [];

    private hostId: string;
    private matchRoomCode: string;
    private username: string;
    private userId: string;
    private hasEnteredRoom: boolean;

    // Allow more constructor parameters
    // eslint-disable-next-line max-params
    constructor(
        public socketService: SocketHandlerService,
        private readonly router: Router,
        private readonly notificationService: NotificationService,
        private readonly matchContextService: MatchContextService,
        private chatService: ChatService,
    ) {
        this.hasEnteredRoom = false;
    }

    get socketId() {
        return this.socketService.socket.id ? this.socketService.socket.id : '';
    }

    getRoomCode() {
        return this.matchRoomCode;
    }

    getUsername() {
        return this.username;
    }

    getUserId() {
        return this.userId;
    }

    getHostId() {
        return this.hostId;
    }

    connect() {
        if (!this.hasEnteredRoom) {
            this.hasEnteredRoom = true;
            this.chatService.channel = ChatChannel.ROOM;
            this.resetMatchValues();
            this.onRedirectAfterDisconnection();
            this.onFetchPlayersData();
            this.onMatchStarted();
            this.onBeginQuiz();
            this.onNextQuestion();
            this.onStartCooldown();
            this.onHostQuit();
            this.onPlayerKick();
            this.handleError();
            this.onPlayerChatStateToggle();
            this.onRouteToResultsPage();
            this.onCurrentAnswers();
        }
    }

    disconnectFromRoom() {
        this.router.navigateByUrl('/home');
        this.chatService.channel = ChatChannel.GENERAL;
        this.chatService.clearMatchRoomMessages();
        this.hasEnteredRoom = false;
        this.socketService.socket.removeListener(MatchEvents.FetchPlayersData);
        this.socketService.socket.removeListener(MatchEvents.MatchStarting);
        this.socketService.socket.removeListener(MatchEvents.BeginQuiz);
        this.socketService.socket.removeListener(MatchEvents.GoToNextQuestion);
        this.socketService.socket.removeListener(MatchEvents.StartCooldown);
        this.socketService.socket.removeListener(MatchEvents.HostQuitMatch);
        this.socketService.socket.removeListener(MatchEvents.KickPlayer);
        this.socketService.socket.removeListener(MatchEvents.Error);
        this.socketService.socket.removeListener(MatchEvents.RouteToResultsPage);
        this.socketService.socket.removeListener(MatchEvents.CurrentAnswers);
        this.socketService.send(MatchEvents.Disconnect);
        this.matchContextService.resetContext();
        // this.socketService.socket.removeListener(MatchEvents.Disconnect);
    }

    createRoom(
        gameId: string,
        hostId: string,
        hostUsername: string,
        isClassicMode: boolean = true,
        partyConfig: PartyConfig = { isFriendsOnly: false, isEntryFeeRequired: false },
    ) {
        this.socketService.send(MatchEvents.CreateRoom, { gameId, hostId, isClassicMode, partyConfig }, (res: { code: string }) => {
            if (res) {
                this.matchRoomCode = res.code;
                this.username = hostUsername;
                this.hostId = hostId;
                this.userId = hostId;
                this.partyConfig = partyConfig;

                this.sendPlayersData(this.matchRoomCode);
                this.router.navigateByUrl('/match-room');
            }
        });
    }

    getPlayerByUsername(username: string): Player | null {
        const playerToFindByUsername = this.players.find((player) => player.username === username);
        return playerToFindByUsername ? playerToFindByUsername : null;
    }

    onPlayerChatStateToggle() {
        this.socketService.on(ChatEvents.ReturnCurrentChatState, (currentChatState: boolean) => {
            const player = this.getPlayerByUsername(this.username);
            if (player) {
                player.isChatActive = currentChatState;
            }
        });
    }

    joinRoom(roomCode: string, username: string, userId: string) {
        const sentInfo: UserInfo = { roomCode, username, userId };

        this.socketService.send(MatchEvents.JoinRoom, sentInfo, (res: { code: string; userId: string; username: string }) => {
            if (res) {
                this.matchRoomCode = res.code;
                this.username = res.username;
                this.userId = res.userId;
                this.router.navigateByUrl('/match-room');

                this.sendPlayersData(roomCode);
            }
        });
    }

    sendPlayersData(roomCode: string) {
        this.socketService.send(MatchEvents.SendPlayersData, roomCode);
    }

    banUser(userId: string) {
        if (this.userId === this.hostId) {
            const sentInfo: UserInfo = { roomCode: this.matchRoomCode, userId };
            this.socketService.send(MatchEvents.BanUsername, sentInfo);
        }
    }

    handleError() {
        this.socketService.on(MatchEvents.Error, (errorMessage: string) => {
            this.notificationService.displayErrorMessage(errorMessage);
        });
    }

    startMatch() {
        this.isMatchStarted = true;
        this.socketService.send(MatchEvents.StartMatch, this.matchRoomCode);
    }

    onMatchStarted() {
        this.socketService.on(MatchEvents.MatchStarting, (data: { start: boolean; gameTitle: string }) => {
            if (data.start) {
                this.isMatchStarted = data.start;
            }
            if (data.gameTitle) {
                this.gameTitle = data.gameTitle;
            }
        });
    }

    onBeginQuiz() {
        this.socketService.on(MatchEvents.BeginQuiz, (data: { firstQuestion: Question; gameDuration: number; isClassicMode: boolean }) => {
            this.isWaitOver = true;
            this.currentQuestion = data.firstQuestion;
            this.gameDuration = data.gameDuration;
            const { firstQuestion, gameDuration } = data;
            this.router.navigate(['/play-match'], { state: { question: firstQuestion, duration: gameDuration } });
        });
    }

    goToNextQuestion() {
        this.socketService.send(MatchEvents.GoToNextQuestion, this.matchRoomCode);
    }

    onStartCooldown() {
        this.socketService.on(MatchEvents.StartCooldown, () => {
            this.isCooldown = true;
            const context = this.matchContextService.getContext();
            if (this.isCooldown && context !== MatchContext.TestPage && context !== MatchContext.RandomMode) {
                this.currentQuestion.text = translate('feedback-messages.prepare');
            }
        });
    }

    onNextQuestion() {
        this.socketService.on(MatchEvents.GoToNextQuestion, (question: Question) => {
            this.isCooldown = false;
            this.currentQuestion = question;
        });
    }

    onFetchPlayersData() {
        this.socketService.on(MatchEvents.FetchPlayersData, (res: string) => {
            this.players = JSON.parse(res);
        });
    }

    onHostQuit() {
        this.socketService.on(MatchEvents.HostQuitMatch, () => {
            this.isHostPlaying = false;
            this.disconnectFromRoom();
        });
    }

    onRedirectAfterDisconnection() {
        this.socketService.on(MatchEvents.Disconnect, () => {
            this.router.navigateByUrl('/home');
            this.resetMatchValues();
        });
    }

    resetMatchValues() {
        this.matchRoomCode = '';
        this.username = '';
        this.players = [];
        this.isResults = false;
        this.isWaitOver = false;
        this.isPlaying = false;
        this.isCooldown = false;
        this.chatService.clearMatchRoomMessages();
    }

    routeToResultsPage() {
        this.socketService.send(MatchEvents.RouteToResultsPage, this.matchRoomCode);
    }

    onRouteToResultsPage() {
        this.socketService.on(MatchEvents.RouteToResultsPage, () => {
            this.isResults = true;
            this.router.navigateByUrl('/results');
        });
    }

    onPlayerKick() {
        this.socketService.on(MatchEvents.KickPlayer, () => {
            this.isBanned = true;
            this.disconnectFromRoom();
        });
    }

    toggleLock() {
        this.socketService.send(MatchEvents.ToggleLock, this.matchRoomCode);
    }

    onCurrentAnswers() {
        this.socketService.on(MatchEvents.CurrentAnswers, (answer: string[]) => {
            if (this.userId !== this.hostId) return;
            this.currentAnswers = answer;
        });
    }
}
