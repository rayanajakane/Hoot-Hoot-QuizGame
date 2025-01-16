import { Test, TestingModule } from '@nestjs/testing';
import { ChatService } from './chat.service';

describe('ChatService', () => {
    let service: ChatService;
    beforeEach(async () => {
        const module: TestingModule = await Test.createTestingModule({
            providers: [ChatService],
        }).compile();

        service = module.get<ChatService>(ChatService);
    });

    it('should be defined', () => {
        expect(service).toBeDefined();
    });

    it('should add and get messages', () => {
        /*
        const matchRoomIndex = 0;
        jest.spyOn(matchRoomService, 'getRoomIndex').mockReturnValue(matchRoomIndex);
        service.addMessage(mockMessage, MOCK_MATCH_ROOM.code);
        const messages = service.getMessages(mockRoomCode);
        expect(messages).toEqual([mockMessage]);
        const returnedMessage = service.addMessage(mockMessage, mockRoomCode);
        expect(returnedMessage).toEqual(mockMessage);
        */
    });

    it('should return the added message', () => {
        /*
        const matchRoomIndex = 0;
        jest.spyOn(matchRoomService, 'getRoomIndex').mockReturnValue(matchRoomIndex);
        const returnedMessage = service.addMessage(mockMessage, matchRoomCode);
        expect(returnedMessage).toEqual(mockMessage);
        */
    });
});
