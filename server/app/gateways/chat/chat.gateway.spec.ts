/* eslint-disable max-lines */
/* eslint-disable @typescript-eslint/no-explicit-any */
import { MOCK_MESSAGE_EMOJI_INFO } from '@app/constants/chat-mocks';
import { MOCK_MESSAGE, MOCK_MESSAGE_INFO } from '@app/constants/match-mocks';
import { ChatService } from '@app/services/chat/chat.service';
import { ChatEvents } from '@common/events/chat.events';
import { Message } from '@common/interfaces/message';
import { Test, TestingModule } from '@nestjs/testing';
import { SinonStubbedInstance, createStubInstance, stub } from 'sinon';
import { BroadcastOperator, Server, Socket } from 'socket.io';
import { DefaultEventsMap } from 'socket.io/dist/typed-events';
import { ChatGateway } from './chat.gateway';

const mockMessage = MOCK_MESSAGE;

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

    it('sendRoomMessage() should emit a NewMessage event and send the messages to the players in the right room', () => {
        const toSpy = jest.spyOn(server, 'to').mockReturnValue({
            emit: (event: string, messageInfo) => {
                expect(event).toEqual('newMessage');
                expect(messageInfo).toEqual(MOCK_MESSAGE_INFO);
            },
        } as unknown as BroadcastOperator<DefaultEventsMap, unknown>);
        gateway.sendRoomMessage(MOCK_MESSAGE_INFO);
        expect(toSpy).toHaveBeenCalledWith(MOCK_MESSAGE_INFO.roomCode);
    });

    it('sendRoomMessage() should emit a NewMessage event to the player in the right room', () => {
        const spy = jest.spyOn(gateway, 'sendRoomMessage').mockReturnThis();
        stub(socket, 'rooms').value(new Set([MOCK_MESSAGE_INFO.roomCode]));
        gateway.sendRoomMessage(MOCK_MESSAGE_INFO);
        expect(spy).toHaveBeenCalledWith(MOCK_MESSAGE_INFO);
    });

    it('handleGeneralMessage() should add the received message to the list of messages, and emit a newMessage event', () => {
        const sendSpy = jest.spyOn(gateway, 'sendGeneralMessage').mockReturnThis();
        const addMessageSpy = jest.spyOn(chatSpy, 'addMessage').mockReturnThis();
        const validateMessageSpy = jest.spyOn(chatSpy, 'isValidMessage').mockReturnThis();
        gateway.handleGeneralMessage(socket, mockMessage);
        expect(sendSpy).toHaveBeenCalled();
        expect(addMessageSpy).toHaveBeenCalled();
        expect(validateMessageSpy).toHaveBeenCalled();
    });

    it('handleRoomMessage() should add the received message to the list of messages, and emit a newMessage event', () => {
        const sendSpy = jest.spyOn(gateway, 'sendRoomMessage').mockReturnThis();
        const addMessageSpy = jest.spyOn(chatSpy, 'addRoomMessage').mockReturnThis();
        const validateMessageSpy = jest.spyOn(chatSpy, 'isValidMessage').mockReturnThis();
        gateway.handleRoomMessage(socket, MOCK_MESSAGE_INFO);
        expect(sendSpy).toHaveBeenCalled();
        expect(addMessageSpy).toHaveBeenCalled();
        expect(validateMessageSpy).toHaveBeenCalled();
    });

    it('sendGeneralMessage() should emit a NewMessage event and send the messages to the players in the right room', () => {
        const emitSpy = jest.spyOn(server, 'emit').mockReturnThis();
        gateway.sendGeneralMessage(MOCK_MESSAGE);
        expect(emitSpy).toHaveBeenCalledWith(ChatEvents.SentGeneralMessage, MOCK_MESSAGE);
    });

    it('handleGeneralEmoji() should react to message in general channel', () => {
        const reactSpy = jest.spyOn(chatSpy, 'reactToGeneralMessage').mockReturnValue(MOCK_MESSAGE);
        const sendSpy = jest.spyOn(gateway, 'sendGeneralEmoji');
        gateway.handleGeneralEmoji(socket, MOCK_MESSAGE_EMOJI_INFO);
        expect(reactSpy).toHaveBeenCalled();
        expect(sendSpy).toHaveBeenCalledWith(MOCK_MESSAGE);
    });
    it('handleRoomEmoji() should react to room message', () => {
        const reactSpy = jest.spyOn(chatSpy, 'reactToRoomMessage').mockReturnValue(MOCK_MESSAGE);
        const sendSpy = jest.spyOn(gateway, 'sendRoomEmoji').mockReturnThis();
        gateway.handleRoomEmoji(socket, MOCK_MESSAGE_EMOJI_INFO);
        expect(reactSpy).toHaveBeenCalled();
        expect(sendSpy).toHaveBeenCalledWith(MOCK_MESSAGE, MOCK_MESSAGE_EMOJI_INFO.roomCode);
    });
    it('sendRoomEmoji() should emit updated message to room', () => {
        const toSpy = jest.spyOn(server, 'to').mockReturnValue({
            emit: (event: string, message: Message) => {
                expect(event).toEqual(ChatEvents.SentRoomEmoji);
                expect(message).toEqual(MOCK_MESSAGE);
            },
        } as unknown as BroadcastOperator<DefaultEventsMap, unknown>);
        gateway.sendRoomEmoji(MOCK_MESSAGE, MOCK_MESSAGE_EMOJI_INFO.roomCode);
        expect(toSpy).toHaveBeenCalledWith(MOCK_MESSAGE_EMOJI_INFO.roomCode);
    });
    it('sendGeneralEmoji() should emit updated message to all users', () => {
        const emitSpy = jest.spyOn(server, 'emit').mockReturnThis();
        gateway.sendGeneralEmoji(MOCK_MESSAGE);
        expect(emitSpy).toHaveBeenCalledWith(ChatEvents.SentGeneralEmoji, MOCK_MESSAGE);
    });
});
