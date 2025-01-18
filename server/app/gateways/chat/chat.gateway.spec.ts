/* eslint-disable max-lines */
/* eslint-disable @typescript-eslint/no-explicit-any */
import { ChatService } from '@app/services/chat/chat.service';
import { ChatEvents } from '@common/events/chat.events';
import { Test, TestingModule } from '@nestjs/testing';
import { SinonStubbedInstance, createStubInstance } from 'sinon';
import { Server, Socket } from 'socket.io';
import { ChatGateway } from './chat.gateway';

const mockMessage = {
    text: '',
    author: '',
    date: new Date(),
};

describe('MatchGateway', () => {
    let gateway: ChatGateway;
    let chatSpy: SinonStubbedInstance<ChatService>;
    let socket: SinonStubbedInstance<Socket>;
    let server: SinonStubbedInstance<Server>;

    beforeEach(async () => {
        chatSpy = createStubInstance(ChatService);
        socket = createStubInstance<Socket>(Socket);
        server = createStubInstance<Server>(Server);

        const module: TestingModule = await Test.createTestingModule({
            providers: [ChatGateway, { provide: ChatService, useValue: chatSpy }],
        }).compile();

        gateway = module.get<ChatGateway>(ChatGateway);
        // We want to assign a value to the private field
        // eslint-disable-next-line dot-notation
        gateway['server'] = server;
    });

    beforeAll(() => {
        jest.useFakeTimers();
        jest.setSystemTime(new Date());
    });

    afterAll(() => {
        jest.useRealTimers();
    });

    it('should be defined', () => {
        expect(gateway).toBeDefined();
    });

    it('handlePrototypeMessage() should add the received message to the list of messages, and emit a newMessage event', () => {
        const sendSpy = jest.spyOn(gateway, 'sendPrototypeMessageToClients').mockReturnThis();
        const addMessageSpy = jest.spyOn(chatSpy, 'addMessage').mockReturnThis();
        const validateMessageSpy = jest.spyOn(chatSpy, 'isValidMessage').mockReturnThis();
        gateway.handlePrototypeMessage(socket, mockMessage);
        expect(sendSpy).toHaveBeenCalled();
        expect(addMessageSpy).toHaveBeenCalled();
        expect(validateMessageSpy).toHaveBeenCalled();
    });

    it('sendMessageToClients() should emit a NewMessage event and send the messages to the players in the right room', () => {
        const emitSpy = jest.spyOn(server, 'emit').mockReturnThis();
        gateway.sendPrototypeMessageToClients(mockMessage);
        expect(emitSpy).toHaveBeenCalledWith(ChatEvents.SentPrototypeMessage, mockMessage);
    });
});
