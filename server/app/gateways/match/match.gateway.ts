import { CHAT_REACTIVATED } from '@app/constants/chat-state-messages';
import { ExpiredTimerEvents } from '@app/constants/expired-timer-events';
import { BAN_PLAYER, NO_MORE_HOST, NO_MORE_PLAYERS } from '@app/constants/match-errors';
import { Game } from '@app/model/database/game';
import { MatchRoom } from '@app/model/schema/match-room.schema';
import { Player } from '@app/model/schema/player.schema';
// import { HistogramService } from '@app/services/histogram/histogram.service';
// import { HistoryService } from '@app/services/history/history.service';
import { FriendsService } from '@app/services/friends/friends.service';
import { MatchBackupService } from '@app/services/match-backup/match-backup.service';
import { MatchRoomService } from '@app/services/match-room/match-room.service';
import { PlayerRoomService } from '@app/services/player-room/player-room.service';
import { PlayerState } from '@common/constants/player-states';
import { ChatEvents } from '@common/events/chat.events';
import { MatchEvents } from '@common/events/match.events';
import { UserInfo } from '@common/interfaces/user-info';
import { Injectable } from '@nestjs/common';
import { EventEmitter2, OnEvent } from '@nestjs/event-emitter';
import { ConnectedSocket, MessageBody, OnGatewayDisconnect, SubscribeMessage, WebSocketGateway, WebSocketServer } from '@nestjs/websockets';
import { Server, Socket } from 'socket.io';

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
        // private readonly histogramService: HistogramService,
        // private readonly historyService: HistoryService,
        private readonly eventEmitter: EventEmitter2,
    ) {}

    @SubscribeMessage(MatchEvents.JoinRoom)
    async joinRoom(@ConnectedSocket() socket: Socket, @MessageBody() data: UserInfo) {
        const matchRoom = this.matchRoomService.getRoom(data.roomCode);
        const codeErrors = this.matchRoomService.getRoomCodeErrors(data.roomCode);
        const usernameErrors = this.playerRoomService.getUsernameErrors(data.roomCode, data.userId);
        let errorMessage = codeErrors + usernameErrors;
        if (matchRoom.isFriendsOnly) {
            const friendshipErrors = await this.friendService.getFriendshipErrors(matchRoom.hostId, false, data.userId);
            if (friendshipErrors) {
                errorMessage += friendshipErrors;
            }
        }

        if (errorMessage) {
            this.sendError(socket.id, errorMessage);
        } else {
            socket.join(data.roomCode);
            const newPlayer = this.playerRoomService.addPlayer(socket, data.roomCode, data.userId, data.username);
            this.returnAllMatches();
            return { code: data.roomCode, username: newPlayer.username, userId: newPlayer.id };
        }
    }

    @SubscribeMessage(MatchEvents.CreateRoom)
    async createRoom(
        @ConnectedSocket() socket: Socket,
        @MessageBody() data: { gameId: string; hostId: string; isClassicMode: boolean; isFriendsOnly: boolean },
    ) {
        console.log('Creating room', data.hostId);
        if (data.isFriendsOnly) {
            const friendshipErrors = await this.friendService.getFriendshipErrors(data.hostId, true);
            if (friendshipErrors) {
                this.sendError(socket.id, friendshipErrors);
                return;
            }
        }

        let selectedGame: Game = {} as Game;
        selectedGame = this.matchBackupService.getBackupGame(data.gameId);
        const newMatchRoom: MatchRoom = await this.matchRoomService.addRoom(
            selectedGame,
            socket,
            data.hostId,
            data.isClassicMode,
            data.isFriendsOnly,
        );

        socket.join(newMatchRoom.code);
        this.returnAllMatches();
        return { code: newMatchRoom.code };
    }

    @SubscribeMessage(MatchEvents.GetAllMatches)
    getAllMatches(@ConnectedSocket() socket: Socket) {
        const allMatches = this.matchRoomService.getAllMatchesInfo();
        this.server.to(socket.id).emit(MatchEvents.ReturnAllMatches, allMatches);
    }

    returnAllMatches() {
        const allMatches = this.matchRoomService.getAllMatchesInfo();
        this.server.emit(MatchEvents.ReturnAllMatches, allMatches);
    }

    @SubscribeMessage(MatchEvents.RouteToResultsPage)
    routeToResultsPage(@ConnectedSocket() socket: Socket, @MessageBody() matchRoomCode: string) {
        const roomIndex = this.matchRoomService.getRoomIndex(matchRoomCode);
        this.matchRoomService.matchRooms[roomIndex].isPlaying = false;

        this.playerRoomService.setStateForAll(matchRoomCode, PlayerState.default);
        this.server.to(matchRoomCode).emit(MatchEvents.RouteToResultsPage);
        // this.emitHistogramHistory(matchRoomCode);

        this.matchRoomService.declareWinner(matchRoomCode);
        // this.historyService.createHistoryItem(this.matchRoomService.getRoom(matchRoomCode));

        this.matchBackupService.updateNMatchesPlayed(this.matchRoomService.matchRooms[roomIndex].game.originalId);

        this.matchRoomService.matchRooms[roomIndex].players.forEach((player: Player) => {
            if (!player.isChatActive) {
                player.isChatActive = true;
                this.server.in(player.socket.id).emit(ChatEvents.ChatReactivated, CHAT_REACTIVATED);
            }
        });
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
            this.sendError(playerToBan.socket.id, BAN_PLAYER);
            this.server.in(playerToBan.socket.id).emit(MatchEvents.KickPlayer);
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

    @SubscribeMessage(MatchEvents.GoToNextQuestion)
    goToNextQuestion(@ConnectedSocket() socket: Socket, @MessageBody() roomCode: string) {
        this.playerRoomService.setStateForAll(roomCode, PlayerState.noInteraction);
        this.matchRoomService.startNextQuestionCooldown(this.server, roomCode);
    }

    @OnEvent(ExpiredTimerEvents.CountdownTimerExpired)
    onCountdownTimerExpired(matchRoomCode: string) {
        this.matchRoomService.sendFirstQuestion(this.server, matchRoomCode);
        // this.histogramService.sendEmptyHistogram(matchRoomCode);
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
    handleDisconnectFromRoom(@ConnectedSocket() socket: Socket) {
        const isHostDisconnected = this.handleHostDisconnect(socket);
        if (!isHostDisconnected) this.handlePlayersDisconnect(socket);
    }

    handleDisconnect(@ConnectedSocket() socket: Socket) {
        this.handleDisconnectFromRoom(socket);
    }

    handleHostDisconnect(@ConnectedSocket() socket: Socket): boolean {
        const hostRoomCode = this.matchRoomService.getRoomCodeByHostSocket(socket.id);
        if (!hostRoomCode) return false;
        const hostRoom = this.matchRoomService.getRoom(hostRoomCode);
        socket.leave(hostRoomCode);
        if (hostRoom.isPlaying || !hostRoom.currentQuestionIndex) {
            this.sendError(hostRoomCode, NO_MORE_HOST);
            this.deleteRoom(hostRoomCode);
            return true;
        }
        if (this.isRoomEmpty(hostRoom)) {
            this.deleteRoom(hostRoomCode);
            return true;
        }
        return false;
    }

    handlePlayersDisconnect(@ConnectedSocket() socket: Socket) {
        const player = this.playerRoomService.getPlayerBySocket(socket.id);
        const roomCode = this.playerRoomService.deletePlayerBySocket(socket.id);
        socket.leave(roomCode);
        if (!roomCode || !player) {
            return;
        }
        const room = this.matchRoomService.getRoom(roomCode);
        const isRoomEmpty = this.isRoomEmpty(room);
        if (room.isPlaying && isRoomEmpty) {
            this.sendError(roomCode, NO_MORE_PLAYERS);
            this.deleteRoom(roomCode);
            return;
        }
        if (isRoomEmpty && (!room.hostSocket.connected || !room.hostSocket.rooms.has(roomCode))) {
            this.deleteRoom(roomCode);
            return;
        }
        this.handleSendPlayersData(roomCode);
        // this.sendMessageOnDisconnect(roomCode, player.username);
        this.returnAllMatches();
    }

    deleteRoom(matchRoomCode: string) {
        this.server.to(matchRoomCode).emit(MatchEvents.HostQuitMatch);
        // this.server.in(matchRoomCode).disconnectSockets(); // TODO: Check if we need to manually remove from room instead.
        this.matchRoomService.deleteRoom(matchRoomCode);
        this.returnAllMatches();
    }

    handleSendPlayersData(matchRoomCode: string) {
        this.server.to(matchRoomCode).emit(MatchEvents.FetchPlayersData, this.playerRoomService.getPlayersStringified(matchRoomCode));
    }

    sendError(socketId: string, error: string) {
        this.server.to(socketId).emit(MatchEvents.Error, error);
    }

    // sendMessageOnDisconnect(roomCode: string, username: string) {
    //     this.server
    //         .to(roomCode)
    //         .emit(ChatEvents.NewMessage, { roomCode, message: { author: '', text: `${username} a quitté la partie.`, date: new Date() } });
    // }

    private isRoomEmpty(room: MatchRoom) {
        return room.players.every((player) => !player.isPlaying);
    }
}
