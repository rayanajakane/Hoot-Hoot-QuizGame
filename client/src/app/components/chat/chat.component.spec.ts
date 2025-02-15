import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { ChatService } from '@app/services/chat/chat.service';
import { getTranslocoModule } from '@app/transloco-testing.module';
import { ChatComponent } from './chat.component';
import SpyObj = jasmine.SpyObj;

const mockDate = new Date();
const mockMessage = {
    text: 'mock',
    author: 'todo',
    date: mockDate,
};

describe('ChatComponent', () => {
    it('should create', () => {
        expect(true).toBeTruthy();
    });
    let component: ChatComponent;
    let fixture: ComponentFixture<ChatComponent>;
    let chatServiceSpy: SpyObj<ChatService>;
    let authServiceSpy: SpyObj<AuthenticationService>;

    beforeEach(() => {
        const socketHandlerSpy = jasmine.createSpyObj('SocketHandlerService', ['send']);
        const chatSpy = jasmine.createSpyObj('ChatService', ['sendPrototypeMessage', 'handleReceivedMessages']);
        const authSpy = jasmine.createSpyObj('AuthenticationService', ['connectToSocket', 'userDisplayName']);
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
                MatSnackBarModule,
                MatDialogModule,
            ],
            providers: [
                { provide: ChatService, useValue: chatSpy },
                { provide: AuthenticationService, useValue: authSpy },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(ChatComponent);
        component = fixture.componentInstance;
        chatServiceSpy = TestBed.inject(ChatService) as jasmine.SpyObj<ChatService>;
        authServiceSpy = TestBed.inject(AuthenticationService) as jasmine.SpyObj<AuthenticationService>;
        authServiceSpy.connectToSocket.and.returnValue();
        (authServiceSpy as unknown).userDisplayName = 'todo';
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
        component.sendMessage(mockMessage.text);
        expect(chatServiceSpy.sendPrototypeMessage).toHaveBeenCalledWith(mockMessage);
    });

    it('should not send an empty message', () => {
        const messageText = '';
        component.sendMessage(messageText);
        expect(chatServiceSpy.sendPrototypeMessage).not.toHaveBeenCalled();
    });
});
