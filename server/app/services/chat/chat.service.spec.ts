import { Test, TestingModule } from '@nestjs/testing';
import { ChatService } from './chat.service';

const validMessage = {
    text: 'valid ',
    author: '',
    date: new Date(),
};
const secondValidMessage = {
    text: 'valid 2',
    author: '',
    date: new Date(),
};
const emptyMessage = {
    text: '',
    author: '',
    date: new Date(),
};

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
        const whiteSpaceMessage = {
            text: ' ',
            author: '',
            date: new Date(),
        };
        expect(service.isValidMessage(whiteSpaceMessage)).toBeFalsy();
    });
    it('should check if message is valid', () => {
        expect(service.isValidMessage(validMessage)).toBeTruthy();
    });

    it('should add and return added message', () => {
        service['messages'] = [];
        expect(service.addMessage(validMessage)).toEqual(validMessage);
        expect(service.getMessages().length).toEqual(1);
    });
});
