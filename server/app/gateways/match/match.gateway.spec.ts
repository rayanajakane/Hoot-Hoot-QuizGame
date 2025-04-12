/* eslint-disable max-lines */
/* eslint-disable @typescript-eslint/no-explicit-any */
import { MOCK_DATE } from '@app/constants/chat-mocks';
import { ExpiredTimerEvents } from '@app/constants/expired-timer-events';
import { GAME_VALID_QUESTION } from '@app/constants/game-mocks';
import { BAN_PLAYER } from '@app/constants/match-errors';
import { HOST_CONFLICT, INVALID_CODE } from '@app/constants/match-login-errors';
import {
    MOCK_MATCH_ROOM,
    MOCK_PLAYER,
    MOCK_PLAYER_ROOM,
    MOCK_RANDOM_MATCH_ROOM,
    MOCK_ROOM_CODE,
    MOCK_TEST_MATCH_ROOM,
    MOCK_USER_INFO,
    MOCK_USERNAME,
} from '@app/constants/match-mocks';
import { MatchGateway } from '@app/gateways/match/match.gateway';
import { Player } from '@app/model/schema/player.schema';
import { AnswerService } from '@app/services/answer/answer.service';
import { EloService } from '@app/services/elo/elo.service';
import { FriendsService } from '@app/services/friends/friends.service';
import { HistoryService } from '@app/services/history/history.service';
// import { HistogramService } from '@app/services/histogram/histogram.service';
// import { HistoryService } from '@app/services/history/history.service';
import { MatchBackupService } from '@app/services/match-backup/match-backup.service';
import { MatchRoomService } from '@app/services/match-room/match-room.service';
import { MoneyService } from '@app/services/money/money.service';
import { PartyService } from '@app/services/party/party.service';
import { PlayerRoomService } from '@app/services/player-room/player-room.service';
import { TimeService } from '@app/services/time/time.service';
import { PlayerState } from '@common/constants/player-states';
import { ChatEvents } from '@common/events/chat.events';
import { MatchEvents } from '@common/events/match.events';
import { EventEmitter2 } from '@nestjs/event-emitter';
import { Test, TestingModule } from '@nestjs/testing';
import { createStubInstance, SinonStubbedInstance, stub } from 'sinon';
import { BroadcastOperator, Server, Socket } from 'socket.io';

describe('MatchGateway', () => {
    let gateway: MatchGateway;
    // let histogramSpy: SinonStubbedInstance<HistogramService>;
    let matchRoomSpy: SinonStubbedInstance<MatchRoomService>;
    let matchBackupSpy: SinonStubbedInstance<MatchBackupService>;
    let timeSpy: SinonStubbedInstance<TimeService>;
    let playerRoomSpy: SinonStubbedInstance<PlayerRoomService>;
    // let historySpy: SinonStubbedInstance<HistoryService>;
    let friendsSpy: SinonStubbedInstance<FriendsService>;
    let moneySpy: SinonStubbedInstance<MoneyService>;
    let partySpy: SinonStubbedInstance<PartyService>;
    let socket: SinonStubbedInstance<Socket>;
    let server: SinonStubbedInstance<Server>;
    let eventEmitter: EventEmitter2;
    let historySpy: SinonStubbedInstance<HistoryService>;
    let eloSpy: SinonStubbedInstance<EloService>;
    let answerSpy: SinonStubbedInstance<AnswerService>;

    beforeEach(async () => {
        // histogramSpy = createStubInstance(HistogramService);
        historySpy = createStubInstance(HistoryService);
        matchRoomSpy = createStubInstance(MatchRoomService);
        matchBackupSpy = createStubInstance(MatchBackupService);
        answerSpy = createStubInstance(AnswerService);
        timeSpy = createStubInstance(TimeService);
        // historySpy = createStubInstance(HistoryService);
        playerRoomSpy = createStubInstance(PlayerRoomService);
        friendsSpy = createStubInstance(FriendsService);
        moneySpy = createStubInstance(MoneyService);
        partySpy = createStubInstance(PartyService);
        socket = createStubInstance<Socket>(Socket);
        server = createStubInstance<Server>(Server);
        eloSpy = createStubInstance(EloService);

        stub(socket, 'rooms').value(new Set([]));

        const module: TestingModule = await Test.createTestingModule({
            providers: [
                MatchGateway,
                // { provide: HistogramService, useValue: histogramSpy },
                { provide: MatchRoomService, useValue: matchRoomSpy },
                { provide: MatchBackupService, useValue: matchBackupSpy },
                { provide: TimeService, useValue: timeSpy },
                { provide: PlayerRoomService, useValue: playerRoomSpy },
                { provide: FriendsService, useValue: friendsSpy },
                { provide: MoneyService, useValue: moneySpy },
                { provide: PartyService, useValue: partySpy },
                { provide: HistoryService, useValue: historySpy },
                { provide: EloService, useValue: eloSpy },
                { provide: AnswerService, useValue: answerSpy },
                // { provide: HistoryService, useValue: historySpy },
                EventEmitter2,
            ],
        }).compile();

        eventEmitter = module.get<EventEmitter2>(EventEmitter2);
        gateway = module.get<MatchGateway>(MatchGateway);
        // We want to assign a value to the private field
        // eslint-disable-next-line dot-notation
        gateway['server'] = server;
    });

    beforeAll(() => {
        jest.useFakeTimers();
        jest.setSystemTime(MOCK_DATE);
    });

    afterAll(() => {
        jest.useRealTimers();
    });

    it('should be defined', () => {
        expect(gateway).toBeDefined();
    });

    it('joinRoom() should let the player join if the room code and the username are valid', async () => {
        matchRoomSpy.getRoomCodeErrors.returns([]);
        matchRoomSpy.getRoom.returns(MOCK_MATCH_ROOM);
        playerRoomSpy.getUsernameErrors.returns([]);
        playerRoomSpy.addPlayer.resolves(MOCK_PLAYER);
        const result = await gateway.joinRoom(socket, MOCK_USER_INFO);
        expect(socket.join.calledOnce).toBeTruthy();
        expect(playerRoomSpy.addPlayer.calledOnce).toBeTruthy();
        expect(result).toEqual({ code: MOCK_USER_INFO.roomCode, username: MOCK_PLAYER.username, userId: MOCK_PLAYER.id });
    });

    it('joinRoom() should not let the player join if the room code or the username are invalid', () => {
        matchRoomSpy.getRoomCodeErrors.returns([INVALID_CODE]);
        matchRoomSpy.getRoom.returns(MOCK_MATCH_ROOM);
        playerRoomSpy.getUsernameErrors.returns([HOST_CONFLICT]);
        const sendErrorSpy = jest.spyOn(gateway, 'sendError').mockReturnThis();
        server.in.returns({
            socketsLeave: (code) => {
                return null;
            },
        } as BroadcastOperator<unknown, unknown>);

        gateway.joinRoom(socket, MOCK_USER_INFO);
        expect(socket.join.calledOnce).toBeFalsy();
        expect(sendErrorSpy).toHaveBeenCalled();
    });

    it('createRoom() should let the host create a match room and let the host join the new room', async () => {
        matchRoomSpy.addRoom.resolves(MOCK_MATCH_ROOM);
        const result = await gateway.createRoom(socket, {
            gameId: MOCK_MATCH_ROOM.game.id,
            hostId: MOCK_PLAYER.id,
            isClassicMode: true,
            partyConfig: { isFriendsOnly: false, isEntryFeeRequired: false, entryFeeAmount: 0, isCheaterMode: false, canPlayCheaterMode: false },
        });
        expect(socket.join.calledOnce).toBeTruthy();
        expect(result).toEqual({ code: MOCK_MATCH_ROOM.code });
    });

    it('createRoom() should let host create a testing match room and let host join as the only player in the new room', async () => {
        matchRoomSpy.addRoom.resolves(MOCK_MATCH_ROOM);
        const result = await gateway.createRoom(socket, {
            gameId: MOCK_TEST_MATCH_ROOM.game.id,
            hostId: MOCK_PLAYER.id,
            isClassicMode: true,
            partyConfig: { isFriendsOnly: false, isEntryFeeRequired: false, entryFeeAmount: 0, isCheaterMode: false, canPlayCheaterMode: false },
        });
        expect(socket.join.calledOnce).toBeTruthy();
        expect(result).toEqual({ code: MOCK_TEST_MATCH_ROOM.code });
    });

    it('createRoom() should let host create a random match room and let host join as a regular player', async () => {
        matchRoomSpy.addRoom.resolves(MOCK_MATCH_ROOM);
        matchBackupSpy.getBackupGame.returns(GAME_VALID_QUESTION);
        const result = await gateway.createRoom(socket, {
            gameId: MOCK_RANDOM_MATCH_ROOM.game.id,
            hostId: MOCK_PLAYER.id,
            isClassicMode: true,
            partyConfig: { isFriendsOnly: false, isEntryFeeRequired: false, entryFeeAmount: 0, isCheaterMode: false, canPlayCheaterMode: false },
        });
        expect(socket.join.calledOnce).toBeTruthy();
        expect(result).toEqual({ code: MOCK_RANDOM_MATCH_ROOM.code });
    });

    it('isRoomEmpty() should return true if room is empty', () => {
        const room = { ...MOCK_PLAYER_ROOM };
        room.players[0].isPlaying = false;
        room.players[0].socket = socket;
        const result = gateway['isRoomEmpty'](room);
        expect(result).toBe(true);
    });

    it('isRoomEmpty() should return true if room is empty', () => {
        const room = { ...MOCK_PLAYER_ROOM };
        room.players = [];
        const result = gateway['isRoomEmpty'](room);
        expect(result).toBe(true);
    });

    it('isRoomEmpty() should return false if room is not empty', () => {
        const room = { ...MOCK_PLAYER_ROOM };
        room.players[0].isPlaying = true;
        stub(socket, 'rooms').value(new Set([MOCK_PLAYER_ROOM.code]));
        room.players[0].socket = socket;
        const result = gateway['isRoomEmpty'](room);
        expect(result).toBe(false);
    });

    it('toggleLock() should call toggleLock', () => {
        const toggleSpy = jest.spyOn(matchRoomSpy, 'toggleLock').mockReturnThis();
        gateway.toggleLock(socket, '');
        expect(toggleSpy).toHaveBeenCalled();
    });

    it('banUsername() should add username to banned usernames list, and delete player if applicable, then update list', () => {
        const addBannedPlayersSpy = jest.spyOn(playerRoomSpy, 'addBannedPlayers').mockReturnThis();
        const mockPlayer = MOCK_PLAYER;
        mockPlayer.socket = socket;
        const errorSpy = jest.spyOn(gateway, 'sendError').mockReturnThis();
        const playerSpy = jest.spyOn(playerRoomSpy, 'getPlayerById').mockReturnValue(mockPlayer);
        const deleteSpy = jest.spyOn(playerRoomSpy, 'deletePlayer').mockReturnThis();
        const returnSpy = jest.spyOn(gateway, 'returnAllMatches').mockReturnThis();
        server.in.returns({
            disconnectSockets: () => {
                return null;
            },
            emit: (event: string) => {
                expect(event).toBe('kickPlayer');
            },
        } as BroadcastOperator<unknown, unknown>);
        const sendSpy = jest.spyOn(gateway, 'sendPlayersData').mockReturnThis();
        gateway.banUsername(socket, MOCK_USER_INFO);
        expect(addBannedPlayersSpy).toHaveBeenCalledWith(MOCK_USER_INFO.roomCode, MOCK_USER_INFO.userId);
        expect(playerSpy).toHaveBeenCalledWith(MOCK_USER_INFO.roomCode, MOCK_USER_INFO.userId);
        expect(deleteSpy).toHaveBeenCalledWith(MOCK_USER_INFO.roomCode, MOCK_USER_INFO.userId);
        expect(sendSpy).toHaveBeenCalledWith(socket, MOCK_USER_INFO.roomCode);
        expect(errorSpy).toHaveBeenCalledWith(mockPlayer.socket.id, [BAN_PLAYER]);
        expect(returnSpy).toHaveBeenCalled();
    });

    it('banUsername() should add username to banned usernames list then update list (if player is not found)', () => {
        const addBannedPlayersSpy = jest.spyOn(playerRoomSpy, 'addBannedPlayers').mockReturnThis();
        const playerSpy = jest.spyOn(playerRoomSpy, 'getPlayerById').mockReturnValue(undefined);
        const deleteSpy = jest.spyOn(playerRoomSpy, 'deletePlayer').mockReturnThis();
        const sendSpy = jest.spyOn(gateway, 'sendPlayersData').mockReturnThis();
        gateway.banUsername(socket, MOCK_USER_INFO);
        expect(addBannedPlayersSpy).toHaveBeenCalledWith(MOCK_USER_INFO.roomCode, MOCK_USER_INFO.userId);
        expect(playerSpy).toHaveBeenCalledWith(MOCK_USER_INFO.roomCode, MOCK_USER_INFO.userId);
        expect(deleteSpy).not.toHaveBeenCalled();
        expect(sendSpy).toHaveBeenCalledWith(socket, MOCK_USER_INFO.roomCode);
    });

    it('sendPlayersData() should check if the socket is in the right room and handle the data', () => {
        const spy = jest.spyOn(gateway, 'handleSendPlayersData').mockReturnThis();
        stub(socket, 'rooms').value(new Set([MOCK_ROOM_CODE]));
        gateway.sendPlayersData(socket, MOCK_ROOM_CODE);
        expect(spy).toHaveBeenCalledWith(MOCK_ROOM_CODE);
    });

    it('sendPlayersData() should not handle the data if the socket is not in the right room', () => {
        const spy = jest.spyOn(gateway, 'handleSendPlayersData').mockReturnThis();
        stub(socket, 'rooms').value(new Set([]));
        gateway.sendPlayersData(socket, MOCK_ROOM_CODE);
        expect(spy).not.toHaveBeenCalled();
    });

    it('deleteRoom() should disconnect all sockets and delete the match room', () => {
        const deleteSpy = jest.spyOn(matchRoomSpy, 'deleteRoom').mockReturnThis();
        const returnSpy = jest.spyOn(gateway, 'returnAllMatches').mockReturnThis();
        const triggerSpy = jest.spyOn(gateway, 'triggerLeaveMatchForAll').mockReturnThis();

        gateway.deleteRoom('');
        expect(deleteSpy).toHaveBeenCalled();
        expect(returnSpy).toHaveBeenCalled();
        expect(triggerSpy).toHaveBeenCalled();
    });

    it('handleDisconnect() should disconnect host and all other players and delete the match room if the host disconnects', () => {
        const mockRoom = MOCK_MATCH_ROOM;
        mockRoom.players = [];
        matchRoomSpy.getRoomCodeByHostSocket.returns(MOCK_ROOM_CODE);
        mockRoom.hostSocket = socket;
        matchRoomSpy.getRoom.resolves(mockRoom);
        jest.spyOn(gateway as any, 'isRoomEmpty').mockReturnThis();
        const sendErrorSpy = jest.spyOn(gateway, 'sendError').mockReturnThis();
        const deleteSpy = jest.spyOn(gateway, 'deleteRoom').mockReturnThis();
        gateway.handleDisconnect(socket);
        expect(deleteSpy).toHaveBeenCalled();
        expect(sendErrorSpy).toHaveBeenCalled();
    });

    it('handleDisconnect() should disconnect host and delete room if no more players in results page', () => {
        const mockRoom = { ...MOCK_MATCH_ROOM };
        mockRoom.hostSocket = socket;
        const mockPlayer = { ...MOCK_PLAYER };
        mockPlayer.isPlaying = false;
        mockPlayer.socket = socket;
        mockRoom.players = [mockPlayer];
        mockRoom.currentQuestionIndex = 1;
        mockRoom.gameLength = 1;
        matchRoomSpy.getRoomCodeByHostSocket.returns(MOCK_ROOM_CODE);
        matchRoomSpy.getRoom.returns(mockRoom);
        const deleteSpy = jest.spyOn(gateway, 'deleteRoom').mockReturnThis();
        gateway.handleDisconnect(socket);
        expect(deleteSpy).toHaveBeenCalled();
    });

    it('handleDisconnect() should disconnect host and not delete room if players in results page', () => {
        const mockRoom = { ...MOCK_MATCH_ROOM };
        mockRoom.hostSocket = socket;
        const mockPlayer = { ...MOCK_PLAYER };
        mockPlayer.isPlaying = true;
        mockRoom.players = [mockPlayer];
        stub(socket, 'rooms').value(new Set([MOCK_MATCH_ROOM.code]));
        mockRoom.players[0].socket = socket;
        mockRoom.currentQuestionIndex = 1;
        mockRoom.gameLength = 1;
        matchRoomSpy.getRoomCodeByHostSocket.returns(MOCK_ROOM_CODE);
        matchRoomSpy.getRoom.returns(mockRoom);
        const deleteSpy = jest.spyOn(gateway, 'deleteRoom').mockReturnThis();
        gateway.handleDisconnect(socket);
        expect(deleteSpy).not.toHaveBeenCalled();
    });

    it('handleDisconnect() should disconnect the player and update list if a player disconnects', () => {
        matchRoomSpy.getRoomCodeByHostSocket.returns('');
        playerRoomSpy.deletePlayerBySocket.returns(MOCK_ROOM_CODE);
        playerRoomSpy.getPlayerBySocket.returns(MOCK_PLAYER);
        const returnSpy = jest.spyOn(gateway, 'returnAllMatches').mockReturnThis();
        const room = { ...MOCK_MATCH_ROOM };
        const mockPlayer: Player = { ...MOCK_PLAYER };
        room.hostSocket = socket;
        room.isPlaying = true;
        room.hostSocket.connected = true;
        stub(socket, 'rooms').value(new Set([MOCK_MATCH_ROOM.code]));
        room.players.push(mockPlayer);
        matchRoomSpy.getRoom.returns(room);
        const handleSpy = jest.spyOn(gateway, 'handleSendPlayersData').mockReturnThis();
        const triggerSpy = jest.spyOn(gateway, 'triggerLeaveMatchForAll').mockReturnThis();
        jest.spyOn(gateway as any, 'isOnePlayerLeft').mockReturnValue(false);
        jest.spyOn(gateway as any, 'isRoomEmpty').mockReturnValue(false);
        gateway.handleDisconnect(socket);
        expect(handleSpy).toHaveBeenCalled();
        expect(returnSpy).toHaveBeenCalled();
        expect(triggerSpy).not.toHaveBeenCalled();
    });

    it('handleDisconnect() should disconnect the player and delete the room if player is last one in the room', () => {
        matchRoomSpy.getRoomCodeByHostSocket.returns('');
        playerRoomSpy.deletePlayerBySocket.returns(MOCK_ROOM_CODE);
        playerRoomSpy.getPlayerBySocket.returns(MOCK_PLAYER);
        const room = { ...MOCK_MATCH_ROOM };
        room.hostSocket = socket;
        socket.connected = false;
        const mockPlayer: Player = { ...MOCK_PLAYER };
        mockPlayer.isPlaying = false;
        room.players = [mockPlayer];
        room.players[0].socket = socket;
        matchRoomSpy.getRoom.returns(room);
        const deleteSpy = jest.spyOn(gateway, 'deleteRoom').mockReturnThis();
        jest.spyOn(gateway, 'triggerLeaveMatchForAll').mockReturnThis();
        gateway.handleDisconnect(socket);
        expect(deleteSpy).toHaveBeenCalled();
    });

    it('handleDisconnect() should disconnect the player disconnect host as well if there are no more players', () => {
        matchRoomSpy.getRoomCodeByHostSocket.returns(undefined);
        const mockRoomToDelete = MOCK_MATCH_ROOM;
        mockRoomToDelete.hostSocket = socket;
        mockRoomToDelete.players = [];
        mockRoomToDelete.isPlaying = true;
        stub(socket, 'rooms').value(new Set([MOCK_MATCH_ROOM.code]));
        playerRoomSpy.deletePlayerBySocket.returns(MOCK_ROOM_CODE);
        playerRoomSpy.getPlayerBySocket.returns(MOCK_PLAYER);
        matchRoomSpy.getRoom.returns(mockRoomToDelete);
        const errorSpy = jest.spyOn(gateway, 'sendError').mockReturnThis();
        const handleSpy = jest.spyOn(gateway, 'handleSendPlayersData').mockReturnThis();
        const deleteSpy = jest.spyOn(gateway, 'deleteRoom').mockReturnThis();
        jest.spyOn(gateway, 'triggerLeaveMatchForAll').mockReturnThis();
        gateway.handleDisconnect(socket);
        expect(errorSpy).toHaveBeenCalled();
        expect(handleSpy).not.toHaveBeenCalled();
        expect(deleteSpy).toHaveBeenCalled();
    });

    it('handleDisconnect() should do nothing if there is no corresponding roomCode for the player', () => {
        matchRoomSpy.getRoomCodeByHostSocket.returns(undefined);
        const mockRoomToDelete = MOCK_MATCH_ROOM;
        mockRoomToDelete.players = [];
        mockRoomToDelete.hostSocket = socket;
        mockRoomToDelete.isPlaying = true;
        stub(socket, 'rooms').value(new Set([MOCK_MATCH_ROOM.code]));
        playerRoomSpy.deletePlayerBySocket.returns(undefined);
        playerRoomSpy.getPlayerBySocket.returns(undefined);
        const errorSpy = jest.spyOn(gateway, 'sendError').mockReturnThis();
        jest.spyOn(matchRoomSpy, 'getRoom').mockReturnThis();
        const handleSpy = jest.spyOn(gateway, 'handleSendPlayersData').mockReturnThis();
        const deleteSpy = jest.spyOn(gateway, 'deleteRoom').mockReturnThis();
        jest.spyOn(gateway, 'triggerLeaveMatchForAll').mockReturnThis();
        gateway.handleDisconnect(socket);
        expect(handleSpy).not.toHaveBeenCalled();
        expect(errorSpy).not.toHaveBeenCalled();
        expect(deleteSpy).not.toHaveBeenCalled();
    });

    it('handleSendPlayersData() should emit a fetch event to the match room with a list of stringified players', () => {
        const getSpy = jest.spyOn(playerRoomSpy, 'getPlayersStringified').mockReturnValue('mock');
        server.to.returns({
            emit: (event: string, playersStringified: string) => {
                expect(event).toEqual(MatchEvents.FetchPlayersData);
                expect(playersStringified).toEqual('mock');
            },
        } as BroadcastOperator<unknown, unknown>);
        gateway.handleSendPlayersData(MOCK_ROOM_CODE);
        expect(getSpy).toHaveBeenCalled();
    });

    it('sendMessageOnDisconnect() should emit a NewMessage event to the match room chat when a player leaves the game', () => {
        const playerLeftMessageMock = {
            roomCode: MOCK_ROOM_CODE,
            message: { author: '', text: `${MOCK_USERNAME} a quitté la partie.`, date: MOCK_DATE },
        };
        server.to.returns({
            emit: (event: string, res) => {
                expect(event).toEqual(ChatEvents.NewMessage);
                expect(res).toEqual(playerLeftMessageMock);
            },
        } as BroadcastOperator<unknown, unknown>);
        // gateway.sendMessageOnDisconnect(MOCK_ROOM_CODE, MOCK_USERNAME);
    });

    it('sendError() should send the error to the socketId', () => {
        server.to.returns({
            emit: (event: string, error: string) => {
                expect(event).toEqual(MatchEvents.Error);
                expect(error).toEqual([INVALID_CODE]);
            },
        } as BroadcastOperator<unknown, unknown>);
        gateway.sendError('', [INVALID_CODE]);
    });

    it('sendError() should send the error to the socketId', () => {
        const emitSpy = jest.spyOn(server, 'emit').mockReturnThis();
        const mockMatches = [
            {
                code: '1234',
                isLocked: true,
                isPlaying: true,
                gameTitle: '',
                nPlayers: 1,
                partyConfig: { isFriendsOnly: false, isEntryFeeRequired: false, entryFeeAmount: 0, isCheaterMode: false, canPlayCheaterMode: false },
            },
            {
                code: '1234',
                isLocked: false,
                isPlaying: true,
                gameTitle: '',
                nPlayers: 1,
                partyConfig: { isFriendsOnly: false, isEntryFeeRequired: false, entryFeeAmount: 0, isCheaterMode: false, canPlayCheaterMode: false },
            },
        ];
        const allMatchesSpy = jest.spyOn(matchRoomSpy, 'getAllMatchesInfo').mockReturnValue(mockMatches);
        gateway.returnAllMatches();
        expect(emitSpy).toHaveBeenCalledWith(MatchEvents.ReturnAllMatches, mockMatches);
        expect(allMatchesSpy).toHaveBeenCalled();
    });

    it('startMatch() should delegate starting match to match room service', () => {
        const markGameSpy = jest.spyOn(matchRoomSpy, 'markGameAsPlaying');
        const startSpy = jest.spyOn(matchRoomSpy, 'startMatch').mockReturnThis();
        const stateSpy = jest.spyOn(playerRoomSpy, 'setStateForAll').mockReturnThis();
        const returnSpy = jest.spyOn(gateway, 'returnAllMatches').mockReturnThis();
        gateway.startMatch(socket, MOCK_ROOM_CODE);
        expect(startSpy).toHaveBeenCalledWith(socket, server, MOCK_ROOM_CODE);
        expect(markGameSpy).toHaveBeenCalled();
        expect(returnSpy).toHaveBeenCalled();
        expect(stateSpy).toHaveBeenCalledWith(MOCK_ROOM_CODE, PlayerState.noInteraction);
    });

    it('nextQuestion() should delegate starting next question to match room service', () => {
        const nextSpy = jest.spyOn(matchRoomSpy, 'startNextQuestionCooldown').mockReturnThis();
        stub(socket, 'rooms').value(new Set([MOCK_ROOM_CODE]));
        const stateSpy = jest.spyOn(playerRoomSpy, 'setStateForAll').mockReturnThis();
        gateway.goToNextQuestion(socket, MOCK_ROOM_CODE);
        expect(nextSpy).toHaveBeenCalledWith(server, MOCK_ROOM_CODE);
        expect(stateSpy).toHaveBeenCalledWith(MOCK_ROOM_CODE, PlayerState.noInteraction);
    });

    it('onCountdownTimerExpired() should call helper functions when CountdownTimerExpired event is emitted', () => {
        const sendFirstQuestionSpy = jest.spyOn(matchRoomSpy, 'sendFirstQuestion');

        eventEmitter.addListener(ExpiredTimerEvents.CountdownTimerExpired, gateway.onCountdownTimerExpired);
        expect(eventEmitter.hasListeners(ExpiredTimerEvents.CountdownTimerExpired)).toBe(true);

        server.in.returns({
            emit: (event: string) => {
                expect(event).toEqual('beginQuiz');
            },
        } as BroadcastOperator<unknown, unknown>);

        gateway.onCountdownTimerExpired(MOCK_ROOM_CODE);
        expect(sendFirstQuestionSpy).toHaveBeenCalledWith(server, MOCK_ROOM_CODE);

        eventEmitter.removeListener(ExpiredTimerEvents.CountdownTimerExpired, gateway.onCountdownTimerExpired);
    });

    it('onCooldownTimerExpired() should call helper functions when CooldownTimerExpired event is emitted', () => {
        const sendNextQuestionSpy = jest.spyOn(matchRoomSpy, 'sendNextQuestion').mockReturnThis();
        eventEmitter.addListener(ExpiredTimerEvents.CooldownTimerExpired, gateway.onCountdownTimerExpired);
        expect(eventEmitter.hasListeners(ExpiredTimerEvents.CooldownTimerExpired)).toBe(true);

        gateway.onCooldownTimerExpired(MOCK_ROOM_CODE);
        expect(sendNextQuestionSpy).toHaveBeenCalledWith(server, MOCK_ROOM_CODE);

        eventEmitter.removeListener(ExpiredTimerEvents.CooldownTimerExpired, gateway.onCountdownTimerExpired);
    });

    it('onRouteToResultsPage() should call routeToResultsPage when RouteToResultsPage event is emitted', () => {
        const routeToResultsPageSpy = jest.spyOn(gateway, 'routeToResultsPage').mockReturnThis();

        eventEmitter.addListener(MatchEvents.RouteToResultsPage, gateway.onRouteToResultsPage);
        expect(eventEmitter.hasListeners(MatchEvents.RouteToResultsPage)).toBe(true);

        gateway.onRouteToResultsPage(MOCK_ROOM_CODE);
        expect(routeToResultsPageSpy).toHaveBeenCalledWith({}, MOCK_ROOM_CODE);

        eventEmitter.removeListener(MatchEvents.RouteToResultsPage, gateway.onCountdownTimerExpired);
    });

    it('should return all matches when get all matches event', () => {
        const spy = jest.spyOn(matchRoomSpy, 'getAllMatchesInfo');
        server.to.returns({
            emit: (event: string) => {
                expect(event).toEqual(MatchEvents.ReturnAllMatches);
            },
        } as BroadcastOperator<unknown, unknown>);
        gateway.getAllMatches(socket);
        expect(spy).toHaveBeenCalled();
    });

    it('should return all matches (update matches info) when toggle lock', () => {
        jest.spyOn(matchRoomSpy, 'toggleLock').mockReturnThis();
        const spy = jest.spyOn(gateway, 'returnAllMatches');
        gateway.toggleLock(socket, '');
        expect(spy).toHaveBeenCalled();
    });
});
