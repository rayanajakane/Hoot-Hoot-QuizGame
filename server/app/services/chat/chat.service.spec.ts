import { MOCK_MATCH_ROOM, MOCK_MESSAGE, MOCK_ROOM_CODE } from '@app/constants/match-mocks';
import { MatchRoom } from '@app/model/schema/match-room.schema';
import { MatchRoomService } from '@app/services/match-room/match-room.service';
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
});
