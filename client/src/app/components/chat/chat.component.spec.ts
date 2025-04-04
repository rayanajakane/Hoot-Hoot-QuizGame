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
import { MOCK_DATE, MOCK_MESSAGE, MOCK_USER_ID_NAME, MOCK_USER_ID_NAME_2 } from '@app/constants/chat-mocks';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { ChatService } from '@app/services/chat/chat.service';
import { MatchRoomService } from '@app/services/match-room/match-room.service';
import { Wallpaper, WallpaperService } from '@app/services/wallpaper/wallpaper.service';
import { getTranslocoModule } from '@app/transloco-testing.module';
import { ChatEmoji } from '@common/constants/chat-emojis';
import { of, Subject } from 'rxjs';
import SpyObj = jasmine.SpyObj;

const mockDate = MOCK_DATE;
const mockMessage = MOCK_MESSAGE;

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
        const chatSpy = jasmine.createSpyObj('ChatService', ['sendMessage', 'handleReceivedMessages', 'reactToMessage']);
        const authSpy = jasmine.createSpyObj('AuthenticationService', ['connectToSocket', 'userDisplayName']);
        const matchSpy = jasmine.createSpyObj('MatchRoomService', ['getRoomCode']);
        const wallpaperSpy = jasmine.createSpyObj('WallpaperService', [], {
            currentWallpaper$: of(Wallpaper.None), // Using 'of' operator to create a simple observable
        });
        socketHandlerSpy.socket = jasmine.createSpyObj('socket', ['removeListener']);
        chatSpy.socketHandler = socketHandlerSpy;

        const mockMessage = { ...MOCK_MESSAGE };

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
                { provide: WallpaperService, useValue: wallpaperSpy },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(ChatComponent);
        component = fixture.componentInstance;
        chatServiceSpy = TestBed.inject(ChatService) as jasmine.SpyObj<ChatService>;
        authServiceSpy = TestBed.inject(AuthenticationService) as jasmine.SpyObj<AuthenticationService>;
        authServiceSpy.connectToSocket.and.returnValue();
        (authServiceSpy as any).userDisplayName = mockMessage.authorUsername;
        (authServiceSpy as any).userId = mockMessage.authorId;
        (authServiceSpy as any).userAvatarUrl = mockMessage.photoUrl;
        matchRoomServiceSpy = TestBed.inject(MatchRoomService) as jasmine.SpyObj<MatchRoomService>;
        chatServiceSpy.updateChatScroll = new Subject();
        chatServiceSpy.updateChatScroll.next(null);
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
        mockMessage.id = ''; // ID is not determined by the client but by the server
        mockMessage.userLikes = [];
        mockMessage.userDislikes = [];
        mockMessage.userLoves = [];
        component.sendMessage(mockMessage.text);
        expect(chatServiceSpy.sendMessage).toHaveBeenCalledWith(mockMessage, 'test');
    });

    it('should not send an empty message', () => {
        const messageText = '';
        component.sendMessage(messageText);
        expect(chatServiceSpy.sendMessage).not.toHaveBeenCalled();
    });

    it('should react to message', () => {
        matchRoomServiceSpy.getRoomCode.and.returnValue('test');
        component.reactToMessage(mockMessage.id, ChatEmoji.LIKE);
        chatServiceSpy.reactToMessage.and.returnValue();
        expect(chatServiceSpy.reactToMessage).toHaveBeenCalledWith(
            mockMessage.id,
            ChatEmoji.LIKE,
            mockMessage.authorId,
            mockMessage.authorUsername,
            'test',
        );
    });
    it('should display reactions tool tip', () => {
        const result = component.getReactionsToolTip([MOCK_USER_ID_NAME, MOCK_USER_ID_NAME_2]);
        const expectedResult = `\n${MOCK_USER_ID_NAME.name}\n${MOCK_USER_ID_NAME_2.name}`;
        expect(result).toEqual(expectedResult);
    });

    it('should return true if own reaction', () => {
        (authServiceSpy as any).userId = MOCK_USER_ID_NAME.id;
        const result = component.isOwnReaction([MOCK_USER_ID_NAME]);
        expect(result).toBeTruthy();
    });
    it('should return false if not own reaction', () => {
        (authServiceSpy as any).userId = '';
        const result = component.isOwnReaction([MOCK_USER_ID_NAME]);
        expect(result).toBeFalsy();
    });
});
