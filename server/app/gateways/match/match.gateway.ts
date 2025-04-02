import { CHAT_REACTIVATED } from '@app/constants/chat-state-messages';
import { ExpiredTimerEvents } from '@app/constants/expired-timer-events';
import { BAN_PLAYER, NO_MORE_HOST, NO_MORE_PLAYERS } from '@app/constants/match-errors';
import { Game } from '@app/model/database/game';
import { MatchRoom } from '@app/model/schema/match-room.schema';
import { Player, VotingData } from '@app/model/schema/player.schema';
import { AnswerService } from '@app/services/answer/answer.service';
import { EloService } from '@app/services/elo/elo.service';
// import { HistogramService } from '@app/services/histogram/histogram.service';
// import { HistoryService } from '@app/services/history/history.service';
import { FriendsService } from '@app/services/friends/friends.service';
import { HistoryService } from '@app/services/history/history.service';
import { MatchBackupService } from '@app/services/match-backup/match-backup.service';
import { MatchRoomService } from '@app/services/match-room/match-room.service';
import { MoneyService } from '@app/services/money/money.service';
import { PartyService } from '@app/services/party/party.service';
import { PlayerRoomService } from '@app/services/player-room/player-room.service';
import { TimeService } from '@app/services/time/time.service';
import { PlayerState } from '@common/constants/player-states';
import { ChatEvents } from '@common/events/chat.events';
import { MatchEvents } from '@common/events/match.events';
import { MoneyEvents } from '@common/events/money.events';
import { PartyConfig } from '@common/interfaces/party-config';
import { UserInfo } from '@common/interfaces/user-info';
import { Injectable } from '@nestjs/common';
import { EventEmitter2, OnEvent } from '@nestjs/event-emitter';
import { ConnectedSocket, MessageBody, OnGatewayDisconnect, SubscribeMessage, WebSocketGateway, WebSocketServer } from '@nestjs/websockets';
import { Server, Socket } from 'socket.io';
import { v4 as uuidv4 } from 'uuid';

@WebSocketGateway({ cors: true })
@Injectable()
export class MatchGateway implements OnGatewayDisconnect {
    @WebSocketServer() private server: Server;

    // permit more params to decouple services
    // eslint-disable-next-line max-params
    constructor(
        private readonly matchRoomService: MatchRoomService,
        private readonly playerRoomService: PlayerRoomService,
        private readonly matchBackupService: MatchBackupService,
        private readonly friendService: FriendsService,
        private readonly moneyService: MoneyService,
        private readonly answerService: AnswerService,
        private readonly timeService: TimeService,
        private historyService: HistoryService,
        private readonly partyService: PartyService,
        private readonly playerService: PlayerRoomService,
        private readonly eventEmitter: EventEmitter2,
        private readonly eloService: EloService,
    ) {}

    @SubscribeMessage(MatchEvents.JoinRoom)
    async joinRoom(@ConnectedSocket() socket: Socket, @MessageBody() data: UserInfo) {
        const matchRoom = this.matchRoomService.getRoom(data.roomCode);
        const codeErrors = this.matchRoomService.getRoomCodeErrors(data.roomCode);
        const usernameErrors = this.playerRoomService.getUsernameErrors(data.roomCode, data.userId);
        let errorMessage = [];
        errorMessage = errorMessage.concat(codeErrors);
        errorMessage = errorMessage.concat(usernameErrors);
        console.log('Joining room', matchRoom.partyConfig);
        if (matchRoom.partyConfig.isFriendsOnly || matchRoom.partyConfig.isEntryFeeRequired) {
            const partyErrors = await this.partyService.canJoinParty(data.userId, data.roomCode);
            errorMessage.concat(partyErrors);
        }

        if (errorMessage) {
            this.sendError(socket.id, errorMessage);
        } else {
            socket.join(data.roomCode);
            if (matchRoom.partyConfig.isEntryFeeRequired) {
                await this.partyService.joinParty(data.userId, data.roomCode);
                const currPlayerBalance = await this.moneyService.getCurrentBalance(data.userId);
                this.server.to(socket.id).emit(MoneyEvents.ReturnBalance, currPlayerBalance);
            }
            const newPlayer = await this.playerRoomService.addPlayer(socket, data.roomCode, data.userId, data.username);
            this.returnAllMatches();
            return { code: data.roomCode, username: newPlayer.username, userId: newPlayer.id };
        }
    }

    @SubscribeMessage(MatchEvents.CreateRoom)
    async createRoom(
        @ConnectedSocket() socket: Socket,
        @MessageBody() data: { gameId: string; hostId: string; isClassicMode: boolean; partyConfig: PartyConfig },
    ) {
        console.log('Creating room', data.hostId);
        // DEACTIVATED CALLS because no longer necessary + caused bugs where clients were stuck (pseudo-crash)
        // COMMENTED to avoid confusion during eventual rebases
        /*
        if (data.partyConfig) {
            if (data.partyConfig.isFriendsOnly) {
                const friendshipErrors = await this.friendService.getFriendshipErrors(data.hostId, true);
                if (friendshipErrors) {
                    this.sendError(socket.id, friendshipErrors);
                    return;
                }
            }
            if (data.partyConfig.isEntryFeeRequired) {
                const moneyErrors = await this.moneyService.getMoneyError(data.hostId, data.partyConfig.entryFeeAmount);
                if (moneyErrors) {
                    this.sendError(socket.id, moneyErrors);
                    return;
                }
            }
        }
        */

        let selectedGame: Game = {} as Game;
        selectedGame = this.matchBackupService.getBackupGame(data.gameId);
        const newMatchRoom: MatchRoom = await this.matchRoomService.addRoom(selectedGame, socket, data.hostId, data.partyConfig, data.isClassicMode);

        socket.join(newMatchRoom.code);
        this.returnAllMatches();
        return { code: newMatchRoom.code };
    }

    @SubscribeMessage(MatchEvents.GetAllMatches)
    getAllMatches(@ConnectedSocket() socket: Socket) {
        const allMatches = this.matchRoomService.getAllMatchesInfo();
        this.server.to(socket.id).emit(MatchEvents.ReturnAllMatches, allMatches);
    }
    roomCode: string;

    @SubscribeMessage(MatchEvents.VoteOnCheater)
    voteOnCheater(@ConnectedSocket() socket: Socket, @MessageBody() matchRoomCode: string) {
        this.server.to(matchRoomCode).emit(MatchEvents.ShowVotingDialog);
        this.roomCode = matchRoomCode;
    }
    totalVotes: VotingData[] = [{ username: '', numberOfVotes: 0, usersWhoVoted: [] }];
    @SubscribeMessage(MatchEvents.SendVotesResults)
    sendResults(@ConnectedSocket() socket: Socket, @MessageBody() newVotesCount: VotingData) {
        this.matchRoomService.totalVotes.push(newVotesCount);

        const username = newVotesCount.username;
        const newVoteCount = newVotesCount.numberOfVotes;
        let votesCount = this.matchRoomService.votesCount;

        if (votesCount[username]) {
            votesCount[username] += newVoteCount;
        } else {
            votesCount[username] = newVoteCount;
        }
        this.server.to(this.roomCode).emit(MatchEvents.SendBackVotesResults, votesCount);
        this.server.to(this.roomCode).emit(MatchEvents.SendVotingUsers, newVotesCount.usersWhoVoted);
    }

    @SubscribeMessage(MatchEvents.SendUpdatedScores)
    sendUpdatedScores(@ConnectedSocket() socket: Socket, @MessageBody() roomCode) {
        this.playerRoomService.recalculateScores(roomCode);
        this.handleSendPlayersData(roomCode);
    }

    returnAllMatches() {
        const allMatches = this.matchRoomService.getAllMatchesInfo();
        this.server.emit(MatchEvents.ReturnAllMatches, allMatches);
    }

    @SubscribeMessage(MatchEvents.RouteToResultsPage)
    async routeToResultsPage(@ConnectedSocket() socket: Socket, @MessageBody() matchRoomCode: string) {
        const roomIndex = this.matchRoomService.getRoomIndex(matchRoomCode);
        try {
            this.matchRoomService.matchRooms[roomIndex].isPlaying = false;

            this.matchRoomService.matchRooms[roomIndex].end = new Date();

            this.playerRoomService.setStateForAll(matchRoomCode, PlayerState.default);
            this.server.to(matchRoomCode).emit(MatchEvents.RouteToResultsPage);

            await this.moneyService.rewardPlayers(matchRoomCode);
            for (const player of this.matchRoomService.matchRooms[roomIndex].players) {
                const currPlayerBalance = await this.moneyService.getCurrentBalance(player.id);
                this.server.in(player.socket.id).emit(MoneyEvents.ReturnBalance, currPlayerBalance);
            }

            this.matchBackupService.updateNMatchesPlayed(this.matchRoomService.matchRooms[roomIndex].game.originalId);

            this.matchRoomService.matchRooms[roomIndex].players.forEach((player: Player) => {
                if (!player.isChatActive) {
                    player.isChatActive = true;
                    this.server.in(player.socket.id).emit(ChatEvents.ChatReactivated, CHAT_REACTIVATED);
                }
            });
        } catch (error) {
            // Try-catch to be on the safe side and avoid server crash (happened a few times when sudden disconnect due to client refresh)
            console.log(error);
        }

        try {
            await this.eloService.updateEloForMatch(matchRoomCode);
            console.log(`Elo ratings updated for match: ${matchRoomCode}`);
        } catch (error) {
            console.error(`Failed to update Elo ratings for match: ${matchRoomCode}`, error);
        }
    }

    @SubscribeMessage(MatchEvents.ToggleLock)
    toggleLock(@ConnectedSocket() socket: Socket, @MessageBody() matchRoomCode: string) {
        this.matchRoomService.toggleLock(matchRoomCode);
        this.returnAllMatches();
    }

    @SubscribeMessage(MatchEvents.BanUsername)
    banUsername(@ConnectedSocket() socket: Socket, @MessageBody() data: UserInfo) {
        this.playerRoomService.addBannedPlayers(data.roomCode, data.userId);
        const playerToBan = this.playerRoomService.getPlayerById(data.roomCode, data.userId);
        if (playerToBan) {
            this.playerRoomService.deletePlayer(data.roomCode, data.userId);
            this.sendError(playerToBan.socket.id, [BAN_PLAYER]);
            this.server.in(playerToBan.socket.id).emit(MatchEvents.KickPlayer);
            playerToBan.socket.leave(data.roomCode);
            // this.server.in(playerToBan.socket.id).disconnectSockets();
        }
        this.sendPlayersData(socket, data.roomCode);
        this.returnAllMatches();
    }

    @SubscribeMessage(MatchEvents.SendPlayersData)
    sendPlayersData(@ConnectedSocket() socket: Socket, @MessageBody() matchRoomCode: string) {
        if (socket.rooms.has(matchRoomCode)) {
            this.handleSendPlayersData(matchRoomCode);
        }
    }

    @SubscribeMessage(MatchEvents.StartMatch)
    startMatch(@ConnectedSocket() socket: Socket, @MessageBody() roomCode: string) {
        this.matchRoomService.markGameAsPlaying(roomCode);
        this.matchRoomService.startMatch(socket, this.server, roomCode);
        this.playerRoomService.setStateForAll(roomCode, PlayerState.noInteraction);
        this.returnAllMatches();
    }

    @SubscribeMessage(MatchEvents.StartMatchCheaterMode)
    startMatchCheaterMode(@ConnectedSocket() socket: Socket, @MessageBody() roomCode: string) {
        this.matchRoomService.markGameAsPlaying(roomCode);
        this.matchRoomService.getRoom(roomCode).players;
        this.matchRoomService.startCheaterModeMatch(socket, this.server, roomCode);
        this.playerRoomService.setStateForAll(roomCode, PlayerState.noInteraction);
        const randomPlayer = this.matchRoomService.getRandomPlayer(roomCode);
        if (randomPlayer) {
            this.matchRoomService.sendCheaterPlayer(this.server, roomCode, randomPlayer.username);
        }

        this.returnAllMatches();
    }

    @SubscribeMessage(MatchEvents.GoToNextQuestion)
    goToNextQuestion(@ConnectedSocket() socket: Socket, @MessageBody() roomCode: string) {
        this.playerRoomService.setStateForAll(roomCode, PlayerState.noInteraction);
        this.matchRoomService.startNextQuestionCooldown(this.server, roomCode);
    }

    @OnEvent(ExpiredTimerEvents.CountdownTimerExpired)
    onCountdownTimerExpired(matchRoomCode: string) {
        this.matchRoomService.sendFirstQuestion(this.server, matchRoomCode);
    }

    @OnEvent(ExpiredTimerEvents.CooldownTimerExpired)
    onCooldownTimerExpired(matchRoomCode: string) {
        this.matchRoomService.sendNextQuestion(this.server, matchRoomCode);
    }

    @OnEvent(MatchEvents.RouteToResultsPage)
    onRouteToResultsPage(matchRoomCode: string) {
        this.routeToResultsPage({} as Socket, matchRoomCode);
    }

    @SubscribeMessage(MatchEvents.Disconnect)
    async handleDisconnectFromRoom(@ConnectedSocket() socket: Socket) {
        const isHostDisconnected = this.handleHostDisconnect(socket);
        if (!isHostDisconnected) await this.handlePlayersDisconnect(socket);
    }

    @SubscribeMessage('Disconnect')
    async handleDisconnect(@ConnectedSocket() socket: Socket) {
        await this.handleDisconnectFromRoom(socket);
    }

    handleHostDisconnect(@ConnectedSocket() socket: Socket): boolean {
        const hostRoomCode = this.matchRoomService.getRoomCodeByHostSocket(socket.id);
        if (!hostRoomCode) return false;
        const hostRoom = this.matchRoomService.getRoom(hostRoomCode);
        socket.leave(hostRoomCode);

        // !hostRoom.currentQuestionIndex is true when in wait page (=== 0)
        if (hostRoom.isPlaying || !hostRoom.currentQuestionIndex) {
            this.sendError(hostRoomCode, [NO_MORE_HOST]);
            const endDate = new Date();
            if (hostRoom.players) {
                hostRoom.players.forEach((player) => {
                    this.historyService.addMatchHistoryItem(player.id, {
                        id: uuidv4(),
                        start: hostRoom.startTime,
                        end: endDate,
                        nGoodAnswers: player.nGoodAnswers,
                        nTotalQuestions: hostRoom.gameLength,
                        hasWon: false,
                        hasGivenUp: false,
                    });
                });
            }
            this.deleteRoom(hostRoomCode);
            return true;
        }
        if (this.isRoomEmpty(hostRoom)) {
            this.deleteRoom(hostRoomCode);
            return true;
        }
        return false;
    }

    async handlePlayersDisconnect(@ConnectedSocket() socket: Socket) {
        const player = this.playerRoomService.getPlayerBySocket(socket.id);
        const roomCode = this.playerRoomService.deletePlayerBySocket(socket.id);
        if (!roomCode || !player) {
            return;
        }
        const room = this.matchRoomService.getRoom(roomCode);
        const isOnePlayerLeft = this.isOnePlayerLeft(room);
        const lessthanThreePlayers = this.isRoomLessThanThreePlayers(room);

        if (room.partyConfig.isEntryFeeRequired) {
            if (!room.isPlaying && !room.currentQuestionIndex) {
                await this.partyService.leaveParty(player.id, roomCode);
                const currPlayerBalance = await this.moneyService.getCurrentBalance(player.id);
                this.server.in(socket.id).emit(MoneyEvents.ReturnBalance, currPlayerBalance);
            } else if (isOnePlayerLeft) {
                this.timeService.expireTimer(roomCode, this.server, ExpiredTimerEvents.QuestionTimerExpired);
                await this.routeToResultsPage({} as Socket, roomCode);
            }
        }
        socket.leave(roomCode);
        const isRoomEmpty = this.isRoomEmpty(room);
        if (room.isPlaying && isRoomEmpty) {
            this.sendError(roomCode, [NO_MORE_PLAYERS]);
            this.deleteRoom(roomCode);
            return;
        }

        if (this.matchRoomService.isCheaterMode && room.isPlaying && lessthanThreePlayers) {
            this.sendError(roomCode, LESS_THAN_3_PLAYERS);
            this.deleteRoom(roomCode);
            return;
        }

        if (this.matchRoomService.isCheaterMode && room.isPlaying && lessthanThreePlayers) {
            this.sendError(roomCode, LESS_THAN_3_PLAYERS);
            this.deleteRoom(roomCode);
            return;
        }

        if (isRoomEmpty && (!room.hostSocket.connected || !room.hostSocket.rooms.has(roomCode))) {
            this.deleteRoom(roomCode);
            return;
        }
        this.handleSendPlayersData(roomCode);
        this.returnAllMatches();
    }

    deleteRoom(matchRoomCode: string) {
        this.triggerLeaveMatchForAll(matchRoomCode);
        this.matchRoomService.deleteRoom(matchRoomCode);
        console.log(`Deleting room ${matchRoomCode}`); // For debugging purposes
        this.returnAllMatches();
    }

    triggerLeaveMatchForAll(matchRoomCode: string) {
        this.server.to(matchRoomCode).emit(MatchEvents.HostQuitMatch);
        this.server.in(matchRoomCode).socketsLeave(matchRoomCode);
    }

    handleSendPlayersData(matchRoomCode: string) {
        this.server.to(matchRoomCode).emit(MatchEvents.FetchPlayersData, this.playerRoomService.getPlayersStringified(matchRoomCode));
    }

    sendError(socketId: string, error: string[]) {
        this.server.to(socketId).emit(MatchEvents.Error, error);
    }

    private isRoomEmpty(room: MatchRoom) {
        return room.players.every((player) => !player.isPlaying || !player.socket.rooms.has(room.code));
    }

    private isRoomLessThanThreePlayers(room: MatchRoom) {
        return room.players.filter((player) => player.isPlaying || player.socket.rooms.has(room.code)).length < 4;
    }

    private isOnePlayerLeft(room: MatchRoom) {
        return room.players.filter((player) => player.isPlaying || player.socket.rooms.has(room.code)).length === 1;
    }
}
