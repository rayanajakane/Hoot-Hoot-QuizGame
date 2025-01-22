import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { ChatService } from '@app/services/chat/chat.service';
import { getTranslocoModule } from '@app/transloco-testing.module';
import { ChatPageComponent } from './chat-page.component';

import SpyObj = jasmine.SpyObj;

describe('ChatPageComponent', () => {
    let component: ChatPageComponent;
    let fixture: ComponentFixture<ChatPageComponent>;
    let chatServiceSpy: SpyObj<ChatService>;
    let authenticationServiceSpy: SpyObj<AuthenticationService>;

    beforeEach(() => {
        const chatSpy = jasmine.createSpyObj('ChatService', ['clearMessages']);
        const authenticationSpy = jasmine.createSpyObj('AuthenticationService', ['signOut']);
        TestBed.configureTestingModule({
            imports: [getTranslocoModule()],
            declarations: [ChatPageComponent],
            providers: [
                { provide: ChatService, useValue: chatSpy },
                { provide: AuthenticationService, useValue: authenticationSpy },
            ],
        });
        fixture = TestBed.createComponent(ChatPageComponent);
        component = fixture.componentInstance;
        chatServiceSpy = TestBed.inject(ChatService) as jasmine.SpyObj<ChatService>;
        authenticationServiceSpy = TestBed.inject(AuthenticationService) as jasmine.SpyObj<AuthenticationService>;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should signOut and clear messages', () => {
        const clearSpy = chatServiceSpy.clearMessages.and.returnValue();
        const signOutSpy = authenticationServiceSpy.signOut.and.returnValue();
        component.signOut();
        expect(clearSpy).toHaveBeenCalled();
        expect(signOutSpy).toHaveBeenCalled();
    });
});
