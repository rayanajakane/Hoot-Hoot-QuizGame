import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { JoinMatchService } from '@app/services/join-match/join-match.service';
import { NotificationService } from '@app/services/notification/notification.service';
import { getTranslocoModule } from '@app/transloco-testing.module';
import { mockProvider } from '@ngneat/spectator';
import { of, throwError } from 'rxjs';
import { JoinMatchPageComponent } from './join-match-page.component';
import SpyObj = jasmine.SpyObj;
const mockHttpResponse: HttpResponse<string> = new HttpResponse({ status: 200, statusText: 'OK', body: JSON.stringify(true) });
const mockMatches = [
    {
        code: '1234',
        isLocked: false,
        isPlaying: false,
        gameTitle: 'Jeu cute',
        nPlayers: 1,
    },
    {
        code: '1234',
        isLocked: false,
        isPlaying: false,
        gameTitle: 'Jeu cute',
        nPlayers: 1,
    },
    {
        code: '1234',
        isLocked: false,
        isPlaying: false,
        gameTitle: 'Jeu cute',
        nPlayers: 1,
    },
    {
        code: '1234',
        isLocked: false,
        isPlaying: false,
        gameTitle: 'Jeu cute',
        nPlayers: 1,
    },
    {
        code: '1234',
        isLocked: false,
        isPlaying: false,
        gameTitle: 'Jeu cute',
        nPlayers: 1,
    },
    {
        code: '1234',
        isLocked: false,
        isPlaying: false,
        gameTitle: 'Jeu cute',
        nPlayers: 1,
    },
    {
        code: '1234',
        isLocked: false,
        isPlaying: false,
        gameTitle: 'Jeu cute',
        nPlayers: 1,
    },
    {
        code: '1234',
        isLocked: true,
        isPlaying: false,
        gameTitle: 'Jeu cute',
        nPlayers: 1,
    },
    {
        code: '1234',
        isLocked: true,
        isPlaying: true,
        gameTitle: 'Jeu cute',
        nPlayers: 1,
    },
];

describe('JoinMatchPageComponent', () => {
    let component: JoinMatchPageComponent;
    let joinMatchSpy: SpyObj<JoinMatchService>;
    let notificationSpy: SpyObj<NotificationService>;
    let fixture: ComponentFixture<JoinMatchPageComponent>;

    beforeEach(async () => {
        joinMatchSpy = jasmine.createSpyObj('JoinMatchService', [
            'getAllMatches',
            'stopReturningAllMatches',
            'validateMatchRoomCode',
            'validateUsername',
        ]);
        joinMatchSpy.matchRoomCode = '';
        notificationSpy = jasmine.createSpyObj('NotificationService', ['displayErrorMessage']);
        await TestBed.configureTestingModule({
            imports: [getTranslocoModule()],
            declarations: [JoinMatchPageComponent],
            providers: [
                { provide: JoinMatchService, useValue: joinMatchSpy },
                { provide: NotificationService, useValue: notificationSpy },
                mockProvider(AuthenticationService),
            ],
        }).compileComponents();

        joinMatchSpy.matchesInfo = mockMatches;

        fixture = TestBed.createComponent(JoinMatchPageComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should get matches info', () => {
        expect(joinMatchSpy.getAllMatches).toHaveBeenCalled();
    });

    it('submitCode() should call validateMatchRoomCode if code is valid', () => {
        joinMatchSpy.validateMatchRoomCode.and.returnValue(of(mockHttpResponse));
        component.submitCode('mock');
        expect(joinMatchSpy.validateMatchRoomCode).toHaveBeenCalled();
    });

    it('submitCode() should call validateMatchRoomCode if code is invalid', () => {
        const httpError = new HttpErrorResponse({
            status: 409,
            error: { code: '409', message: 'mock' },
        });
        joinMatchSpy.validateMatchRoomCode.and.returnValue(throwError(() => httpError));
        spyOn(JSON, 'parse').and.returnValue(httpError.error);
        component.submitCode('mock');
        expect(joinMatchSpy.validateMatchRoomCode).toHaveBeenCalled();
        expect(notificationSpy.displayErrorMessage).toHaveBeenCalled();
    });
});
