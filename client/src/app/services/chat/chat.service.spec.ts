import { TestBed } from '@angular/core/testing';
import { MatDialogModule } from '@angular/material/dialog';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { ChatChannel } from '@app/constants/chat-channels';
import { MatchContext } from '@app/constants/states';
import { ChatService } from '@app/services/chat/chat.service';
import { MatchContextService } from '@app/services/match-context/match-context.service';
import { SocketHandlerService } from '@app/services/socket-handler/socket-handler.service';
import SpyObj = jasmine.SpyObj;

describe('ChatService', () => {
    let service: ChatService;
    let socketHandlerSpy: SpyObj<SocketHandlerService>;
    let matchContextSpy: SpyObj<MatchContextService>;

    beforeEach(() => {
        const socketSpy = jasmine.createSpyObj('SocketHandlerService', ['on', 'send', 'isSocketAlive']);

        TestBed.configureTestingModule({
            imports: [MatSnackBarModule, MatDialogModule],
            providers: [ChatService, { provide: SocketHandlerService, useValue: socketSpy }],
        });

        service = TestBed.inject(ChatService);
        matchContextSpy = TestBed.inject(MatchContextService) as jasmine.SpyObj<MatchContextService>;
        socketHandlerSpy = TestBed.inject(SocketHandlerService) as jasmine.SpyObj<SocketHandlerService>;
    });

    const mockMessage = {
        text: 'mock',
        author: '',
        date: new Date(),
    };

    it('should be created', () => {
        expect(service).toBeTruthy();
    });

    it('should send general message', () => {
        service.sendGeneralMessage(mockMessage);
        expect(socketHandlerSpy.send).toHaveBeenCalled();
    });
    it('should send room message', () => {
        service.sendRoomMessage('1234', mockMessage);
        expect(socketHandlerSpy.send).toHaveBeenCalled();
    });
    it('should handle sentGeneralMessage event', () => {
        const sentData = { message: mockMessage };
        service.handleReceivedMessages();
        socketHandlerSpy.on.calls.mostRecent().args[1](sentData);
        expect(service.generalMessages.length).toEqual(1);
    });
    it('should handle new message event', () => {
        const sentData = { message: mockMessage, roomCode: '1234' };
        service.handleRoomMessages();
        socketHandlerSpy.on.calls.mostRecent().args[1](sentData);
        expect(service.matchRoomMessages.length).toEqual(1);
    });
    it('should clear general messages', () => {
        service.generalMessages = [mockMessage];
        service.clearMessages();
        expect(service.generalMessages.length).toEqual(0);
    });
    it('should clear room messages', () => {
        service.matchRoomMessages = [mockMessage];
        service.clearMatchRoomMessages();
        expect(service.matchRoomMessages.length).toEqual(0);
    });
    it('should send message to general channel', () => {
        service.channel = ChatChannel.GENERAL;
        const spy = spyOn(service, 'sendGeneralMessage');
        service.sendMessage(mockMessage, '');
        expect(spy).toHaveBeenCalledWith(mockMessage);
    });
    it('should send message to room channel if match context is not null', () => {
        service.channel = ChatChannel.ROOM;
        matchContextSpy.setContext(MatchContext.HostView);
        const spy = spyOn(service, 'sendRoomMessage');
        service.sendMessage(mockMessage, '1234');
        expect(spy).toHaveBeenCalledWith('1234', mockMessage);
    });
    it('should not send message to room channel if match context is null', () => {
        service.channel = ChatChannel.ROOM;
        matchContextSpy.setContext(MatchContext.Null);
        const spy = spyOn(service, 'sendRoomMessage');
        service.sendMessage(mockMessage, '1234');
        expect(spy).not.toHaveBeenCalled();
    });
});
