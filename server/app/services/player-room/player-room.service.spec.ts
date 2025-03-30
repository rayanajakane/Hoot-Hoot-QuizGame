import { BANNED_PLAYER, HOST_CONFLICT } from '@app/constants/match-login-errors';
import { MOCK_MATCH_ROOM, MOCK_PLAYER, MOCK_PLAYER_ROOM, MOCK_ROOM_CODE, MOCK_USERID, MOCK_USERNAME } from '@app/constants/match-mocks';
import { MultipleChoiceAnswer } from '@app/model/answer-types/multiple-choice-answer/multiple-choice-answer';
import { Player } from '@app/model/schema/player.schema';
import { FirebaseAuthService } from '@app/modules/firebase/firebase-auth/firebase-auth.service';
import { MatchRoomService } from '@app/services/match-room/match-room.service';
import { AnswerCorrectness } from '@common/constants/answer-correctness';
import { HOST_USERNAME } from '@common/constants/match-constants';
import { PlayerState } from '@common/constants/player-states';
import { Test, TestingModule } from '@nestjs/testing';
import { SinonStubbedInstance, createStubInstance } from 'sinon';
import { Socket } from 'socket.io';
import { HistoryService } from '../history/history.service';
import { PlayerRoomService } from './player-room.service';

describe('PlayerRoomService', () => {
    let emitMock;
    let mockSocket;
    let service: PlayerRoomService;
    let matchRoomSpy: SinonStubbedInstance<MatchRoomService>;
    let socket: SinonStubbedInstance<Socket>;
    let historyService: SinonStubbedInstance<HistoryService>;
    let authService: SinonStubbedInstance<FirebaseAuthService>;

    beforeEach(async () => {
        emitMock = jest.fn();
        mockSocket = {
            id: '',
            to: jest.fn().mockReturnValueOnce({ emit: emitMock }),
            send: jest.fn().mockReturnValueOnce({ emit: emitMock }),
            emit: emitMock,
        };

        matchRoomSpy = createStubInstance(MatchRoomService);
        socket = createStubInstance<Socket>(Socket);
        historyService = createStubInstance(HistoryService);
        authService = createStubInstance(FirebaseAuthService);
        const module: TestingModule = await Test.createTestingModule({
            providers: [
                PlayerRoomService,
                { provide: MatchRoomService, useValue: matchRoomSpy },
                { provide: HistoryService, useValue: historyService },
                { provide: FirebaseAuthService, useValue: authService },
            ],
        }).compile();

        service = module.get<PlayerRoomService>(PlayerRoomService);
        jest.spyOn(matchRoomSpy, 'getRoom').mockReturnValue(MOCK_PLAYER_ROOM);
    });

    it('should be defined', () => {
        expect(service).toBeDefined();
    });

    it('getPlayers() should return an array of players in the MatchRoom with the corresponding code', () => {
        const result = service.getPlayers('');
        expect(result).toEqual([MOCK_PLAYER]);
    });

    it('getPlayersStringified() should return the list of stringified players without socket attribute', () => {
        jest.spyOn(service, 'getPlayers').mockReturnValue([MOCK_PLAYER]);
        // disable max lines since cant be split for string comparision
        // eslint-disable-next-line max-len
        const expectedResult =
            '[{"username":"","id":"","photoUrl":"","answer":{"isSubmitted":false,"selectedChoices":{}},"score":0,"answerCorrectness":0,"bonusCount":0,"isPlaying":true,"isChatActive":true,"nGoodAnswers":0,"state":"default"}]';
        const result = service.getPlayersStringified('');
        expect(result).toEqual(expectedResult);
    });

    it('addPlayer() should not add player if the username is invalid', async () => {
        const validateSpy = jest.spyOn(service, 'getUsernameErrors').mockReturnValue(HOST_CONFLICT);
        jest.spyOn(authService, 'getUserPhotoUrl').mockResolvedValue('');
        const result = await service.addPlayer(socket, '', '', '');
        expect(result).toBeFalsy();
        expect(validateSpy).toHaveBeenCalled();
    });

    it('addPlayer() should add the player if the username is valid', async () => {
        const validateSpy = jest.spyOn(service, 'getUsernameErrors').mockReturnValue('');
        jest.spyOn(authService, 'getUserPhotoUrl').mockResolvedValue('');
        const pushSpy = jest.spyOn(Array.prototype, 'push');
        const mockUsername = 'mock';
        const mockId = 'mockId';
        const expectedResult: Player = {
            username: mockUsername,
            id: mockId,
            photoUrl: '',
            answer: { selectedChoices: new Map<string, boolean>(), isSubmitted: false } as MultipleChoiceAnswer,
            score: 0,
            answerCorrectness: AnswerCorrectness.WRONG,
            nGoodAnswers: 0,
            bonusCount: 0,
            isPlaying: true,
            isChatActive: true,
            socket,
            state: PlayerState.default,
        };
        const result = await service.addPlayer(socket, '', mockId, mockUsername);
        expect(result).toEqual(expectedResult);
        expect(validateSpy).toHaveBeenCalled();
        expect(pushSpy).toHaveBeenCalled();
    });

    it('deletePlayerBySocket() should delete the player if the foundMatchRoom is not playing yet', () => {
        const mockRoom = MOCK_PLAYER_ROOM;
        mockRoom.isPlaying = false;
        const mockPlayer = MOCK_PLAYER;
        mockPlayer.socket = socket;
        mockRoom.players = [mockPlayer];
        matchRoomSpy.matchRooms = [mockRoom];

        const deleteSpy = jest.spyOn(service, 'deletePlayer').mockReturnThis();
        const inactiveSpy = jest.spyOn(service, 'makePlayerInactive').mockReturnThis();

        const result = service.deletePlayerBySocket(socket.id);
        expect(result).toEqual(MOCK_PLAYER_ROOM.code);
        expect(deleteSpy).toHaveBeenCalled();
        expect(inactiveSpy).not.toHaveBeenCalled();
    });

    it('getPlayerBySocket() should delete the player if the foundMatchRoom is not playing yet', () => {
        const mockRoom = MOCK_PLAYER_ROOM;
        const mockPlayer = MOCK_PLAYER;
        mockPlayer.socket = socket;
        mockRoom.players = [mockPlayer];
        matchRoomSpy.matchRooms = [mockRoom];
        const result = service.getPlayerBySocket(socket.id);
        expect(result).toEqual(MOCK_PLAYER);
    });

    it('deletePlayerBySocket() should make the player inactive if the foundMatchRoom is playing', () => {
        const mockRoom = MOCK_PLAYER_ROOM;
        mockRoom.isPlaying = true;
        const mockPlayer = MOCK_PLAYER;
        mockPlayer.socket = socket;
        mockRoom.players = [mockPlayer];
        matchRoomSpy.matchRooms = [mockRoom];

        const deleteSpy = jest.spyOn(service, 'deletePlayer').mockReturnThis();
        const inactiveSpy = jest.spyOn(service, 'makePlayerInactive').mockReturnThis();

        const result = service.deletePlayerBySocket(socket.id);
        expect(result).toEqual(MOCK_PLAYER_ROOM.code);
        expect(deleteSpy).not.toHaveBeenCalled();
        expect(inactiveSpy).toHaveBeenCalled();
    });

    it('deletePlayerBySocket() should return undefined if player and room are not found', () => {
        matchRoomSpy.matchRooms = [MOCK_MATCH_ROOM];

        const deleteSpy = jest.spyOn(service, 'deletePlayer').mockReturnThis();
        const inactiveSpy = jest.spyOn(service, 'makePlayerInactive').mockReturnThis();

        const result = service.deletePlayerBySocket('');
        expect(result).toEqual(undefined);
        expect(deleteSpy).not.toHaveBeenCalled();
        expect(inactiveSpy).not.toHaveBeenCalled();
    });

    it('getPlayerById() should return the player with the corresponding username (non case sensitive)', () => {
        const searchedPlayer = MOCK_PLAYER;
        searchedPlayer.username = MOCK_USERNAME;
        searchedPlayer.id = MOCK_USERID;
        const otherPlayer: Player = {
            username: '',
            id: '',
            score: 0,
            bonusCount: 0,
            isPlaying: false,
            socket: undefined,
        } as Player;
        jest.spyOn(service, 'getPlayers').mockReturnValue([searchedPlayer, otherPlayer]);
        expect(service.getPlayerById('', searchedPlayer.id)).toEqual(searchedPlayer);
        expect(service.getPlayerById('', searchedPlayer.id)).toEqual(searchedPlayer);
    });

    it('makePlayerInactive() should set the player isPlaying property to false', () => {
        const cases = [true, false];
        cases.forEach((playingState: boolean) => {
            const mockRoom = MOCK_PLAYER_ROOM;
            const mockPlayer = MOCK_PLAYER;
            const mockUsername = MOCK_USERNAME;
            const mockId = MOCK_USERID;
            mockPlayer.id = mockId;
            mockPlayer.username = mockUsername;
            mockPlayer.isPlaying = playingState;
            mockRoom.players = [mockPlayer];
            matchRoomSpy.matchRooms = [mockRoom];

            jest.spyOn(matchRoomSpy, 'getRoomIndex').mockReturnValue(0);
            jest.spyOn(matchRoomSpy, 'getRoom').mockClear();
            jest.spyOn(matchRoomSpy, 'getRoom').mockReturnValue(mockRoom);
            service.makePlayerInactive('', mockId);
            expect(matchRoomSpy.matchRooms[0].players[0].isPlaying).toBeFalsy();
            expect(matchRoomSpy.matchRooms[0].players[0].state).toEqual(PlayerState.exit);
        });
    });

    it('deletePlayer() should remove player from the MatchRoom', () => {
        const mockRoom = MOCK_PLAYER_ROOM;
        const mockPlayer = MOCK_PLAYER;
        mockPlayer.username = MOCK_USERNAME;
        mockPlayer.id = MOCK_USERID;
        mockRoom.players = [mockPlayer];
        matchRoomSpy.matchRooms = [mockRoom];

        jest.spyOn(matchRoomSpy, 'getRoomIndex').mockReturnValue(0);
        service.deletePlayer('', MOCK_USERID);
        expect(matchRoomSpy.matchRooms[0].players.length).toEqual(0);
    });

    it('getBannedPlayers() should return banned usernames', () => {
        const mockRoom = MOCK_PLAYER_ROOM;
        mockRoom.bannedIds = [MOCK_USERID];
        matchRoomSpy.matchRooms = [mockRoom];
        expect(service.getBannedPlayers('')).toEqual([MOCK_USERID]);
    });

    it('addBannedPlayers() should add username to bannedUsernames list from matchRoomService', () => {
        const pushSpy = jest.spyOn(Array.prototype, 'push');
        service.addBannedPlayers('', MOCK_USERID);
        expect(pushSpy).toHaveBeenCalledWith(MOCK_USERID);
    });

    it('isBannedPlayer() should return true if username is banned', () => {
        jest.spyOn(service, 'getBannedPlayers').mockReturnValue([MOCK_USERID]);
        expect(service.isBannedPlayer('', MOCK_USERID)).toEqual(true);
    });

    it('isBannedPlayer() should return false if username is not banned', () => {
        jest.spyOn(service, 'getBannedPlayers').mockReturnValue([]);
        expect(service.isBannedPlayer('', MOCK_USERID)).toEqual(false);
    });

    it('getUsernameErrors() should show applicable errors', () => {
        const testCases = [{ userId: MOCK_USERID, isBanned: true, expectedResult: BANNED_PLAYER }];
        for (const { userId, isBanned, expectedResult } of testCases) {
            const banSpy = jest.spyOn(service, 'isBannedPlayer').mockReturnValue(isBanned);
            // const usedSpy = jest.spyOn(service, 'getPlayerByUsername').mockReturnValue(isUsed ? MOCK_PLAYER : undefined);
            const result = service.getUsernameErrors('', userId);
            expect(banSpy).toHaveBeenCalled();
            // expect(usedSpy).toHaveBeenCalled();
            expect(result).toEqual(expectedResult);
        }
    });

    it('setStateForAll() should change state for all players in match room', () => {
        jest.spyOn(matchRoomSpy, 'getRoomIndex').mockReturnValue(0);
        const sendSpy = jest.spyOn(service, 'sendPlayersToHost').mockReturnThis();
        const mockRoom = MOCK_MATCH_ROOM;
        mockRoom.players = [
            { state: PlayerState.default } as Player,
            { state: PlayerState.default } as Player,
            { state: PlayerState.exit } as Player,
        ];
        matchRoomSpy.matchRooms = [mockRoom];
        service.setStateForAll(mockRoom.code, 'mock');
        expect(
            matchRoomSpy.matchRooms[0].players.every((player: Player) =>
                player.state === PlayerState.exit ? player.state === PlayerState.exit : player.state === 'mock',
            ),
        );
        expect(sendSpy).toHaveBeenCalledWith(MOCK_PLAYER_ROOM.code);
    });

    it('setState() should change the state for the player in the match room', () => {
        const sendSpy = jest.spyOn(service, 'sendPlayersToHost').mockReturnThis();
        const mockPlayerRoom = { ...MOCK_PLAYER_ROOM };
        const mockPlayer = { ...MOCK_PLAYER };
        mockPlayerRoom.players = [mockPlayer];
        matchRoomSpy.matchRooms = [mockPlayerRoom];
        mockPlayerRoom.players[0].socket = mockSocket;
        service.setState(mockPlayerRoom.players[0].socket.id, 'mock');
        expect(sendSpy).toHaveBeenCalledWith(mockPlayerRoom.code);
        expect(matchRoomSpy.matchRooms[0].players[0].state).toEqual('mock');
    });

    it('sendPlayersToHost should emit FetchPlayersData to host socket', () => {
        const getSpy = jest.spyOn(matchRoomSpy, 'getRoomIndex').mockReturnValue(0);
        const stringifySpy = jest.spyOn(service, 'getPlayersStringified').mockReturnValue('mock');
        const mockRoom = { ...MOCK_PLAYER_ROOM };
        mockRoom.hostSocket = mockSocket;
        matchRoomSpy.matchRooms = [mockRoom];
        service.sendPlayersToHost(mockRoom.code);
        expect(getSpy).toHaveBeenCalledWith(mockRoom.code);
        expect(stringifySpy).toHaveBeenCalledWith(mockRoom.code);
    });
    it('isHostPlayer() should return true if the host player exists in the MatchRoom', () => {
        const mockRoom = MOCK_PLAYER_ROOM;
        const mockPlayer = MOCK_PLAYER;
        mockRoom.hostId = MOCK_USERID;
        mockPlayer.username = HOST_USERNAME;
        mockPlayer.id = MOCK_USERID;
        mockRoom.players = [mockPlayer];
        matchRoomSpy.matchRooms = [mockRoom];

        const result = service.isHostPlayer(MOCK_ROOM_CODE);
        expect(result).toEqual(true);
    });

    it('isHostPlayer() should return false if the host player does not exist in the MatchRoom', () => {
        const mockRoom = MOCK_PLAYER_ROOM;
        const mockPlayer = MOCK_PLAYER;
        mockPlayer.username = 'otherPlayer';
        mockPlayer.id = 'otherPlayer';
        mockRoom.players = [mockPlayer];
        matchRoomSpy.matchRooms = [mockRoom];

        const result = service.isHostPlayer(MOCK_ROOM_CODE);
        expect(result).toEqual(false);
    });
});
