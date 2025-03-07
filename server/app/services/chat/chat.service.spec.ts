import { MOCK_USER_ID_NAME } from '@app/constants/chat-mocks';
import { MOCK_MATCH_ROOM, MOCK_MESSAGE, MOCK_ROOM_CODE } from '@app/constants/match-mocks';
import { MatchRoom } from '@app/model/schema/match-room.schema';
import { MatchRoomService } from '@app/services/match-room/match-room.service';
import { ChatEmoji } from '@common/constants/chat-emojis';
import { Test, TestingModule } from '@nestjs/testing';
import { ChatService } from './chat.service';

const validMessage = MOCK_MESSAGE;
const secondValidMessage = MOCK_MESSAGE;
const emptyMessage = MOCK_MESSAGE;
emptyMessage.text = '';

describe('ChatService', () => {
    let service: ChatService;
    let matchRoomService: MatchRoomService;
    let mockMatchRooms: MatchRoom[];
    beforeEach(async () => {
        mockMatchRooms = [{ ...MOCK_MATCH_ROOM, messages: [] }];
        const module: TestingModule = await Test.createTestingModule({
            providers: [
                ChatService,
                {
                    provide: MatchRoomService,
                    useValue: {
                        getRoomIndex: jest.fn(),
                        matchRooms: mockMatchRooms,
                    },
                },
            ],
        }).compile();

        service = module.get<ChatService>(ChatService);
        matchRoomService = module.get<MatchRoomService>(MatchRoomService);
    });

    const matchRoomCode = MOCK_MATCH_ROOM.code;
    const mockMessage = MOCK_MESSAGE;
    const mockRoomCode = MOCK_ROOM_CODE;
    const INDEX_NOT_FOUND = -1;

    it('should be defined', () => {
        expect(service).toBeDefined();
    });

    it('should return messages', () => {
        service['messages'] = [validMessage, secondValidMessage];
        expect(service.getMessages().length).toEqual(2);
        expect(service.getMessages()[0]).toEqual(validMessage);
        expect(service.getMessages()[1]).toEqual(secondValidMessage);
    });

    it('should check if message is empty', () => {
        expect(service.isValidMessage(emptyMessage)).toBeFalsy();
    });
    it('should check if message is whitespace only', () => {
        const whiteSpaceMessage = MOCK_MESSAGE;
        MOCK_MESSAGE.text = ' ';
        expect(service.isValidMessage(whiteSpaceMessage)).toBeFalsy();
    });
    it('should check if message is valid', () => {
        validMessage.text = 'validMessage';
        expect(service.isValidMessage(validMessage)).toBeTruthy();
    });
    it('should add and get messages', () => {
        const matchRoomIndex = 0;
        jest.spyOn(matchRoomService, 'getRoomIndex').mockReturnValue(matchRoomIndex);
        const returnedMessage = service.addRoomMessage(mockMessage, mockRoomCode);
        expect(returnedMessage.text).toEqual(mockMessage.text);
        expect(returnedMessage.authorId).toEqual(mockMessage.authorId);
    });
    it('should add and return added message', () => {
        service['messages'] = [];
        expect(service.addMessage(validMessage)).toEqual(validMessage);
        expect(service.getMessages().length).toEqual(1);
    });
    it('should add and get room messages', () => {
        const matchRoomIndex = 0;
        jest.spyOn(matchRoomService, 'getRoomIndex').mockReturnValue(matchRoomIndex);
        service.addRoomMessage(mockMessage, MOCK_MATCH_ROOM.code);
        const messages = service.getRoomMessages(mockRoomCode);
        expect(messages).toEqual([mockMessage]);
        const returnedMessage = service.addRoomMessage(mockMessage, mockRoomCode);
        expect(returnedMessage).toEqual(mockMessage);
    });
    it('should not add a message to a match room that does not exist', () => {
        jest.spyOn(matchRoomService, 'getRoomIndex').mockReturnValue(INDEX_NOT_FOUND);
        service.addRoomMessage(mockMessage, matchRoomCode);
        const messages = service.getRoomMessages(matchRoomCode);
        expect(messages).toEqual([]);
    });
    it('should return userLikes chat emoji attribute', () => {
        const result = service.getChatEmojiAttribute(ChatEmoji.LIKE);
        expect(result).toEqual('userLikes');
    });
    it('should return userLoves chat emoji attribute', () => {
        const result = service.getChatEmojiAttribute(ChatEmoji.LOVE);
        expect(result).toEqual('userLoves');
    });
    it('should return userDislikes chat emoji attribute', () => {
        const result = service.getChatEmojiAttribute(ChatEmoji.DISLIKE);
        expect(result).toEqual('userDislikes');
    });
    it('reactToGeneralMessage() should add reaction to general channel', () => {
        service['messages'] = [MOCK_MESSAGE];
        const expectedResult = MOCK_MESSAGE;
        expectedResult.userLikes = [MOCK_USER_ID_NAME];
        const result = service.reactToGeneralMessage(MOCK_MESSAGE.id, MOCK_USER_ID_NAME, ChatEmoji.LIKE);
        expect(result).toEqual(expectedResult);
    });
    it('reactToGeneralMessage() should remove reaction to general channel if user has already reacted to the message', () => {
        service['messages'] = [MOCK_MESSAGE];
        const expectedResult = MOCK_MESSAGE;
        MOCK_MESSAGE.userLikes = [MOCK_USER_ID_NAME];
        const result = service.reactToGeneralMessage(MOCK_MESSAGE.id, MOCK_USER_ID_NAME, ChatEmoji.LIKE);
        expect(result).toEqual(expectedResult);
    });
    it('should add reaction to room channel message', () => {
        const matchRoomIndex = 0;
        jest.spyOn(matchRoomService, 'getRoomIndex').mockReturnValue(matchRoomIndex);
        jest.spyOn(service, 'getRoomMessages');
        matchRoomService.matchRooms[0].messages = [MOCK_MESSAGE];
        const expectedResult = MOCK_MESSAGE;
        expectedResult.userLikes = [MOCK_USER_ID_NAME];
        const result = service.reactToRoomMessage(MOCK_MESSAGE.id, MOCK_USER_ID_NAME, ChatEmoji.LIKE, 'abc');
        expect(result).toEqual(expectedResult);
    });
    it('should remove reaction to room channel message if user has already reacted', () => {
        const matchRoomIndex = 0;
        jest.spyOn(matchRoomService, 'getRoomIndex').mockReturnValue(matchRoomIndex);
        jest.spyOn(service, 'getRoomMessages');
        matchRoomService.matchRooms[0].messages = [MOCK_MESSAGE];
        const expectedResult = MOCK_MESSAGE;
        MOCK_MESSAGE.userLikes = [MOCK_USER_ID_NAME];
        const result = service.reactToRoomMessage(MOCK_MESSAGE.id, MOCK_USER_ID_NAME, ChatEmoji.LIKE, 'abc');
        expect(result).toEqual(expectedResult);
    });
});
