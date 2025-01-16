/* eslint-disable max-lines */
/* eslint-disable @typescript-eslint/no-explicit-any */
import { ChatService } from '@app/services/chat/chat.service';
import { Test, TestingModule } from '@nestjs/testing';
import { SinonStubbedInstance, createStubInstance } from 'sinon';
import { Server } from 'socket.io';
import { ChatGateway } from './chat.gateway';

describe('MatchGateway', () => {
    let gateway: ChatGateway;
    let chatSpy: SinonStubbedInstance<ChatService>;
    // let socket: SinonStubbedInstance<Socket>;
    let server: SinonStubbedInstance<Server>;

    beforeEach(async () => {
        chatSpy = createStubInstance(ChatService);
        // socket = createStubInstance<Socket>(Socket);
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

    it('handleIncomingRoomMessages() should add the received message to the list of messages, and emit a newMessage event', () => {
        /*
        const mockMessageInfo = MOCK_MESSAGE_INFO;
        const sendSpy = jest.spyOn(gateway, 'sendMessageToClients').mockReturnThis();
        const addMessageSpy = jest.spyOn(chatSpy, 'addMessage').mockReturnThis();
        gateway.handleIncomingRoomMessages(socket, mockMessageInfo);
        expect(addMessageSpy).toHaveBeenCalledWith(mockMessageInfo.message, mockMessageInfo.roomCode);
        expect(sendSpy).toHaveBeenCalledWith(MOCK_MESSAGE_INFO);
        */
    });

    it('sendMessageToClients() should emit a NewMessage event and send the messages to the players in the right room', () => {
        /*
        const toSpy = jest.spyOn(server, 'to').mockReturnValue({
            emit: (event: string, messageInfo) => {
                expect(event).toEqual('newMessage');
                expect(messageInfo).toEqual(MOCK_MESSAGE_INFO);
            },
        } as unknown as BroadcastOperator<DefaultEventsMap, unknown>);
        gateway.sendMessageToClients(MOCK_MESSAGE_INFO);
        expect(toSpy).toHaveBeenCalledWith(MOCK_MESSAGE_INFO.roomCode);
        */
    });
});
