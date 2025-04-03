import { ExpiredTimerEvents } from '@app/constants/expired-timer-events';
import { INVALID_CODE, LOCKED_ROOM } from '@app/constants/match-login-errors';
import { QuestionType } from '@app/constants/question-types';
import { Choice } from '@app/model/database/choice';
import { Game } from '@app/model/database/game';
import { Question } from '@app/model/database/question';
import { MatchRoom } from '@app/model/schema/match-room.schema';
import { Player, VotingData } from '@app/model/schema/player.schema';
import { ChoiceTracker } from '@app/model/tally-trackers/choice-tracker/choice-tracker';
import { HistoryService } from '@app/services/history/history.service';
import { QrCodeService } from '@app/services/qr-code/qr-code.service';
import { QuestionStrategyContext } from '@app/services/question-strategy-context/question-strategy-context.service';
import { TimeService } from '@app/services/time/time.service';
import { COOLDOWN_TIME, COUNTDOWN_TIME, FACTOR, MAXIMUM_CODE_LENGTH } from '@common/constants/match-constants';
import { PlayerState } from '@common/constants/player-states';
import { MatchEvents } from '@common/events/match.events';
import { TimerEvents } from '@common/events/timer.events';
import { GameInfo } from '@common/interfaces/game-info';
import { MatchPageInfo } from '@common/interfaces/match-page-info';
import { PartyConfig } from '@common/interfaces/party-config';
import { Injectable } from '@nestjs/common';
import { EventEmitter2 } from '@nestjs/event-emitter';
import { Server, Socket } from 'socket.io';
import { v4 as uuidv4 } from 'uuid';


@Injectable()
export class MatchRoomService {
    matchRooms: MatchRoom[];
    backgroundHostSocket: Socket;
    cheaterPlayer: Player;
    votesCount: { [username: string]: number } = { ['']: 0 };
    isCheaterMode: boolean = false;
    // totalVotes: VotingData[];
   // totalVotes: { [username: string]: number }[] = [];
    totalVotes: VotingData[] = [{ username: '', numberOfVotes: 0, usersWhoVoted: [] }];

    constructor(
        private readonly eventEmitter: EventEmitter2,
        private readonly timeService: TimeService,
        private readonly questionStrategyService: QuestionStrategyContext,
        private qrCodeService: QrCodeService,
        private historyService: HistoryService,
    ) {
        this.matchRooms = [];
    }

    generateRoomCode(): string {
        let generatedCode: string;
        while (!generatedCode || this.getRoom(generatedCode)) {
            generatedCode = Math.floor(Math.random() * FACTOR).toString();
        }
        while (generatedCode.length < MAXIMUM_CODE_LENGTH) {
            generatedCode = '0' + generatedCode;
        }
        return generatedCode;
    }

    getRoom(code: string): MatchRoom | undefined {
        return this.matchRooms.find((room: MatchRoom) => {
            return room.code === code;
        });
    }

    getRoomIndex(code: string): number {
        return this.matchRooms.findIndex((room: MatchRoom) => {
            return room.code === code;
        });
    }

    // allow more parameters to make method more reusable
    // eslint-disable-next-line max-params
    async addRoom(selectedGame: Game, socket: Socket, hostId: string, partyConfig: PartyConfig, isClassicMode: boolean = true): Promise<MatchRoom> {
        const isLocked = false;
        const isPlaying = false;

        const roomCode = this.generateRoomCode();
        const qrCodeUrl = await this.qrCodeService.generateQrCode(roomCode);
        this.votesCount ={};
        this.totalVotes = [];


        const newRoom: MatchRoom = {
            code: roomCode,
            hostSocket: socket,
            isLocked,
            isPlaying,
            game: selectedGame,
            gameLength: selectedGame.questions.length,
            questionDuration: 0,
            currentQuestion: selectedGame.questions[0],
            currentQuestionIndex: 0,
            currentQuestionAnswer: [],
            choiceTracker: new ChoiceTracker(),
            matchHistograms: [],
            bannedIds: [],
            players: [],
            activePlayers: 0,
            submittedPlayers: 0,
            messages: [],
            isClassicMode,
            startTime: new Date(),
            qrCodeUrl,
            hostId,
            partyConfig,
        };
        this.matchRooms.push(newRoom);
        this.setQuestionStrategy(newRoom);
        return newRoom;
    }

    getRoomCodeByHostSocket(socketId: string): string {
        let matchRoomCode: string;
        this.matchRooms.forEach((matchRoom: MatchRoom) => {
            matchRoomCode = matchRoom.hostSocket.id === socketId ? matchRoom.code : undefined;
        });
        return matchRoomCode;
    }

    toggleLock(matchRoomCode: string): void {
        this.getRoom(matchRoomCode).isLocked = !this.getRoom(matchRoomCode).isLocked;
    }

    async deleteRoom(matchRoomCode: string): Promise<void> {
        this.timeService.terminateTimer(matchRoomCode);
        this.questionStrategyService.deleteRoom(matchRoomCode);
        this.matchRooms = this.matchRooms.filter((room: MatchRoom) => {
            return room.code !== matchRoomCode;
        });
        this.qrCodeService.deleteQrCode(matchRoomCode);
    }

    getRoomCodeErrors(matchRoomCode: string): string {
        let errors = '';
        const room = this.getRoom(matchRoomCode);
        if (!room) {
            errors += INVALID_CODE;
        } else if (room.isLocked) {
            errors += LOCKED_ROOM;
        }
        return errors;
    }

    startMatch(socket: Socket, server: Server, matchRoomCode: string) {
        if (!this.canStartMatch(matchRoomCode)) return;
        this.isCheaterMode = false;
        const gameTitle = this.getGameTitle(matchRoomCode);
        const gameInfo: GameInfo = { start: true, gameTitle };
        socket.to(matchRoomCode).emit(MatchEvents.MatchStarting, gameInfo);

        const roomIndex = this.getRoomIndex(matchRoomCode);
        this.matchRooms[roomIndex].startTime = new Date();
        this.timeService.startTimer(server, matchRoomCode, COUNTDOWN_TIME, ExpiredTimerEvents.CountdownTimerExpired);
    }

    startCheaterModeMatch(socket: Socket, server: Server, matchRoomCode: string) {
        // if (!this.canStartMatchCheaterMode(matchRoomCode)) return;
        this.isCheaterMode = true;
        const gameTitle = this.getGameTitle(matchRoomCode);
        const gameInfo: GameInfo = { start: true, gameTitle };
        socket.to(matchRoomCode).emit(MatchEvents.CheaterModeMatchStarting, gameInfo);

        const roomIndex = this.getRoomIndex(matchRoomCode);
        this.matchRooms[roomIndex].startTime = new Date();
        console.log('dow e go here');

        this.timeService.startTimer(server, matchRoomCode, COUNTDOWN_TIME, ExpiredTimerEvents.CountdownTimerExpired);
    }

    getRandomPlayer(roomCode): Player {
        const players = this.getRoom(roomCode).players;
        if (players && players.length > 0) {
            const randomIndex = Math.floor(Math.random() * players.length);
            //onsole.log("all players", players)
            console.log('rando', players[randomIndex].username);
            this.cheaterPlayer = players[randomIndex];
            return players[randomIndex];
        } else {
            return null;
        }
    }

    cheaterGetsBonus(username) {
        // TO DO: USE THE TOTAL DEFINED IN MATCH GATEWAY
        let total = 0;
        for (const voteData of this.totalVotes) {
            for (const username in voteData) {
                total += voteData[username];
            }
        }

        if (this.votesCount[username]) {
            if (this.votesCount[username] / total <= 0.5) {
                return true;
            } else return false;
        }
        return true;
    }

    pauseMatchTimer(server: Server, matchRoomCode: string) {
        this.timeService.pauseTimer(server, matchRoomCode);
    }

    triggerPanicMode(server: Server, matchRoomCode: string) {
        this.timeService.startPanicTimer(server, matchRoomCode);
        server.to(matchRoomCode).emit(TimerEvents.PanicTimer);
    }

    markGameAsPlaying(matchRoomCode: string): void {
        const matchRoom: MatchRoom = this.getRoom(matchRoomCode);
        matchRoom.isPlaying = true;
    }

    isGamePlaying(matchRoomCode: string): boolean {
        return this.getRoom(matchRoomCode).isPlaying;
    }

    sendFirstQuestion(server: Server, matchRoomCode: string): void {
        const matchRoom: MatchRoom = this.getRoom(matchRoomCode);
        const firstQuestion = matchRoom.game.questions[0];
        const gameDuration: number = matchRoom.game.duration;
        this.setQuestionStrategy(matchRoom);
        this.defineCurrentQuestionAnswer(matchRoomCode, firstQuestion);
        this.removeAnswerField(firstQuestion);
        server.to(matchRoom.hostSocket.id).emit(MatchEvents.CurrentAnswers, matchRoom.currentQuestionAnswer);
        server.to(this.cheaterPlayer?.socket.id).emit(MatchEvents.CurrentAnswers, matchRoom.currentQuestionAnswer);
        const isClassicMode: boolean = matchRoom.isClassicMode;
        server.in(matchRoomCode).emit(MatchEvents.BeginQuiz, { firstQuestion, gameDuration, isClassicMode });
        this.timeService.startTimer(server, matchRoomCode, matchRoom.questionDuration, ExpiredTimerEvents.QuestionTimerExpired);
    }

    startNextQuestionCooldown(server: Server, matchRoomCode: string): void {
        server.in(matchRoomCode).emit(MatchEvents.StartCooldown, matchRoomCode);
        this.timeService.startTimer(server, matchRoomCode, COOLDOWN_TIME, ExpiredTimerEvents.CooldownTimerExpired);
    }

    sendNextQuestion(server: Server, matchRoomCode: string): void {
        const matchRoom: MatchRoom = this.getRoom(matchRoomCode);

        const nextQuestion = this.getCurrentQuestion(matchRoomCode);
        matchRoom.currentQuestion = nextQuestion;

        this.defineCurrentQuestionAnswer(matchRoomCode, matchRoom.currentQuestion);
        this.setQuestionStrategy(matchRoom);

        this.removeAnswerField(nextQuestion);
        server.in(matchRoomCode).emit(MatchEvents.GoToNextQuestion, nextQuestion);
        server.to(matchRoom.hostSocket.id).emit(MatchEvents.CurrentAnswers, matchRoom.currentQuestionAnswer);
        this.timeService.startTimer(server, matchRoomCode, matchRoom.questionDuration, ExpiredTimerEvents.QuestionTimerExpired);
    }

    sendCheaterPlayer(server: Server, matchRoomCode: string, player: string) {
        const matchRoom: MatchRoom = this.getRoom(matchRoomCode);
        // TO DO: FIX SYNTAX
        server.in(matchRoomCode).emit(MatchEvents.SendCheater, { player });
    }

    defineCurrentQuestionAnswer(matchRoomCode: string, question: Question) {
        const matchRoom: MatchRoom = this.getRoom(matchRoomCode);
        switch (question.type) {
            case QuestionType.MultipleChoice:
                matchRoom.currentQuestionAnswer = this.filterCorrectChoices(question);
                break;
            case QuestionType.EstimatedAnswer:
                matchRoom.currentQuestionAnswer = [String(question.estimatedParameters.correctAnswer)];
                break;
            default:
                matchRoom.currentQuestionAnswer = [];
                break;
        }
    }

    resetPlayerSubmissionCount(matchRoomCode: string) {
        this.getRoom(matchRoomCode).submittedPlayers = 0;
    }

    resetCheaterPlayer() {
        this.cheaterPlayer = null;
    }

    incrementCurrentQuestionIndex(matchRoomCode: string) {
        this.getRoom(matchRoomCode).currentQuestionIndex++;
    }

    getGameTitle(matchRoomCode: string): string {
        return this.getRoom(matchRoomCode).game.title;
    }

    canStartMatch(matchRoomCode: string): boolean {
        const room = this.getRoom(matchRoomCode);
        if (!room) {
            return false;
        }
        return room.isLocked && room.players.length > 0;
    }

    canStartMatchCheaterMode(matchRoomCode: string): boolean {
        const room = this.getRoom(matchRoomCode);
        if (!room) {
            return false;
        }
        return room.isLocked && room.players.length > 2 && this.isQuestionTypeNotQRL(matchRoomCode);
    }

    isQuestionTypeNotQRL(matchRoomCode) {
        const room = this.getRoom(matchRoomCode);
        if (!room) {
            return false;
        }
        for (let question of room.game.questions) {
            if (question.type === QuestionType.LongAnswer) {
                return false;
            }
        }
        return true;
    }

    getCurrentQuestion(matchRoomCode: string) {
        const matchRoom: MatchRoom = this.getRoom(matchRoomCode);
        return matchRoom.game.questions[matchRoom.currentQuestionIndex];
    }
    //totalVotes: VotingData[] = [{ username: '', numberOfVotes: 0, usersWhoVoted: [] }];

    declareWinner(matchRoomCode: string): Player[] {
        const matchRoom = this.getRoom(matchRoomCode);
        const players: Player[] = matchRoom.players;
        const playingPlayers = players.filter((player) => player.isPlaying && player.state !== PlayerState.exit);
        const maxScore = Math.max(...playingPlayers.map((player) => player.score));
        const playersWithMaxScore = playingPlayers.filter((player) => player.score === maxScore);
        const playersWithoutMaxScore = playingPlayers.filter((player) => player.score !== maxScore);

        playersWithMaxScore.forEach((player) => {
            player.socket.emit(MatchEvents.Winner);
            this.historyService.addMatchHistoryItem(player.id, {
                id: uuidv4(),
                start: matchRoom.startTime,
                end: matchRoom.end,
                nGoodAnswers: player.nGoodAnswers,
                nTotalQuestions: matchRoom.gameLength,
                hasWon: true,
                hasGivenUp: false,
            });
        });

        playersWithoutMaxScore.forEach((player) => {
            this.historyService.addMatchHistoryItem(player.id, {
                id: uuidv4(),
                start: matchRoom.startTime,
                end: matchRoom.end,
                nGoodAnswers: player.nGoodAnswers,
                nTotalQuestions: matchRoom.gameLength,
                hasWon: false,
                hasGivenUp: false,
            });
        });
        return playersWithMaxScore;
    }

    getAllMatchesInfo() {
        const matchPagesInfo: MatchPageInfo[] = [];
        this.matchRooms.forEach((matchRoom: MatchRoom) => {
            matchPagesInfo.push({
                code: matchRoom.code,
                isLocked: matchRoom.isLocked,
                isPlaying: matchRoom.isPlaying,
                gameTitle: matchRoom.game.title,
                nPlayers: matchRoom.players.length,
                partyConfig: matchRoom.partyConfig,
            });
        });
        return matchPagesInfo;
    }

    private filterCorrectChoices(question: Question) {
        const correctChoices = [];
        question.choices.forEach((choice) => {
            if (choice.isCorrect) {
                correctChoices.push(choice.text);
            }
        });
        return correctChoices;
    }

    private removeAnswerField(question: Question) {
        question.choices.forEach((choice: Choice) => delete choice.isCorrect);
        question.estimatedParameters.correctAnswer = undefined;
    }

    private setQuestionStrategy(matchRoom: MatchRoom) {
        this.questionStrategyService.setQuestionStrategy(matchRoom);
        this.timeService.currentPanicThresholdTime = this.questionStrategyService.getQuestionPanicThreshold(matchRoom.code);
    }
}
