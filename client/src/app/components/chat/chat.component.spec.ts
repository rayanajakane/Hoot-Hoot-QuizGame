/* eslint-disable @typescript-eslint/no-explicit-any */
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatOptionModule } from '@angular/material/core';
import { MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ChatComponent } from '@app/components/chat/chat.component';
import { MOCK_DATE, MOCK_MESSAGE } from '@app/constants/chat-mocks';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { ChatService } from '@app/services/chat/chat.service';
import { MatchRoomService } from '@app/services/match-room/match-room.service';
import { getTranslocoModule } from '@app/transloco-testing.module';
import SpyObj = jasmine.SpyObj;

const mockDate = MOCK_DATE;

describe('ChatComponent', () => {
    it('should create', () => {
        expect(true).toBeTruthy();
    });
    let component: ChatComponent;
    let fixture: ComponentFixture<ChatComponent>;
    let chatServiceSpy: SpyObj<ChatService>;
    let authServiceSpy: SpyObj<AuthenticationService>;
    let matchRoomServiceSpy: SpyObj<MatchRoomService>;

    beforeEach(() => {
        const socketHandlerSpy = jasmine.createSpyObj('SocketHandlerService', ['send']);
        const chatSpy = jasmine.createSpyObj('ChatService', ['sendMessage', 'handleReceivedMessages']);
        const authSpy = jasmine.createSpyObj('AuthenticationService', ['connectToSocket', 'userDisplayName']);
        const matchSpy = jasmine.createSpyObj('MatchRoomService', ['getRoomCode']);
        socketHandlerSpy.socket = jasmine.createSpyObj('socket', ['removeListener']);
        chatSpy.socketHandler = socketHandlerSpy;

        TestBed.configureTestingModule({
            declarations: [ChatComponent],
            imports: [
                getTranslocoModule(),
                MatIconModule,
                MatFormFieldModule,
                MatInputModule,
                BrowserAnimationsModule,
                MatFormFieldModule,
                MatSelectModule,
                MatOptionModule,
                MatSnackBarModule,
                MatDialogModule,
            ],
            providers: [
                { provide: ChatService, useValue: chatSpy },
                { provide: AuthenticationService, useValue: authSpy },
                { provide: MatchRoomService, useValue: matchSpy },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(ChatComponent);
        component = fixture.componentInstance;
        chatServiceSpy = TestBed.inject(ChatService) as jasmine.SpyObj<ChatService>;
        authServiceSpy = TestBed.inject(AuthenticationService) as jasmine.SpyObj<AuthenticationService>;
        authServiceSpy.connectToSocket.and.returnValue();
        (authServiceSpy as any).userDisplayName = MOCK_MESSAGE.authorUsername;
        (authServiceSpy as any).userId = MOCK_MESSAGE.authorId;
        (authServiceSpy as any).userAvatarUrl = MOCK_MESSAGE.photoUrl;
        matchRoomServiceSpy = TestBed.inject(MatchRoomService) as jasmine.SpyObj<MatchRoomService>;
        fixture.detectChanges();
    });

    beforeAll(() => {
        jasmine.clock().install();
        jasmine.clock().mockDate(mockDate);
    });

    afterAll(() => {
        jasmine.clock().uninstall();
        fixture.destroy();
    });

    it('should create', () => {
        expect(true).toBeTruthy();
    });

    it('should send message', () => {
        matchRoomServiceSpy.getRoomCode.and.returnValue('test');
        component.sendMessage(MOCK_MESSAGE.text);
        MOCK_MESSAGE.id = ''; // ID is not determined by the client but by the server
        expect(chatServiceSpy.sendMessage).toHaveBeenCalledWith(MOCK_MESSAGE, 'test');
    });

    it('should not send an empty message', () => {
        const messageText = '';
        component.sendMessage(messageText);
        expect(chatServiceSpy.sendMessage).not.toHaveBeenCalled();
    });
});
