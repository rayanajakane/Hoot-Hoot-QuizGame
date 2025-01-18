import { TestBed } from '@angular/core/testing';
import { MatDialogModule } from '@angular/material/dialog';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { SocketHandlerService } from '../socket-handler/socket-handler.service';
import { ChatService } from './chat.service';
import SpyObj = jasmine.SpyObj;

describe('ChatService', () => {
    let service: ChatService;
    let socketHandlerSpy: SpyObj<SocketHandlerService>;

    beforeEach(() => {
        const socketSpy = jasmine.createSpyObj('SocketHandlerService', ['on', 'send', 'isSocketAlive']);

        TestBed.configureTestingModule({
            imports: [MatSnackBarModule, MatDialogModule],
            providers: [ChatService, { provide: SocketHandlerService, useValue: socketSpy }],
        });

        service = TestBed.inject(ChatService);
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

    it('should send message', () => {
        service.sendPrototypeMessage(mockMessage);
        expect(socketHandlerSpy.send).toHaveBeenCalled();
    });

    it('should handle sentPrototypeMessage event', () => {
        const sentData = { message: mockMessage };
        service.handleReceivedMessages();
        socketHandlerSpy.on.calls.mostRecent().args[1](sentData);
        expect(service.messages.length).toEqual(1);
    });

    it('should clear messages', () => {
        service.messages = [mockMessage];
        service.clearMessages();
        expect(service.messages.length).toEqual(0);
    });
});
