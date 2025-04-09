/* eslint-disable @typescript-eslint/no-explicit-any */
import { HttpClient } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Routes } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { Game } from '@app/interfaces/game';
import { WaitPageComponent } from '@app/pages/wait-page/wait-page.component';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { MatchContextService } from '@app/services/match-context/match-context.service';
import { MatchRoomService } from '@app/services/match-room/match-room.service';
import { MatchService } from '@app/services/match/match.service';
import { NotificationService } from '@app/services/notification/notification.service';
import { TimeService } from '@app/services/time/time.service';
import SpyObj = jasmine.SpyObj;
import { getTranslocoModule } from '@app/transloco-testing.module';

@Component({
    selector: 'app-chat',
    template: '',
})
class MockChatComponent {}

describe('WaitPageComponent', () => {
    let component: WaitPageComponent;
    let fixture: ComponentFixture<WaitPageComponent>;
    let matchRoomSpy: SpyObj<MatchRoomService>;
    let matchSpy: SpyObj<MatchService>;
    let timeSpy: SpyObj<TimeService>;
    let questionContextSpy: SpyObj<MatchContextService>;
    let notificationServiceSpy: SpyObj<NotificationService>;
    let authenticationServiceSpy: SpyObj<AuthenticationService>;

    const routes: Routes = [{ path: 'home', component: WaitPageComponent }];

    beforeEach(() => {
        authenticationServiceSpy = jasmine.createSpyObj('AuthenticationService', ['getImageDownloadUrl']);
        matchRoomSpy = jasmine.createSpyObj('MatchRoomService', [
            'getUsername',
            'getHostId',
            'banUser',
            'toggleLock',
            'connect',
            'startMatch',
            'getGameTitleObservable',
            'getStartMatchObservable',
            'matchStarted',
            'beginQuiz',
            'goToNextQuestion',
            'gameOver',
            'disconnectFromRoom',
            'getRoomCode',
        ]);
        matchSpy = jasmine.createSpyObj('MatchService', ['']);
        questionContextSpy = jasmine.createSpyObj('QuestionContextService', ['setContext', 'getContext']);
        timeSpy = jasmine.createSpyObj('TimeService', ['handleTimer', 'handleStopTimer', 'computeTimerProgress', 'listenToTimerEvents']);
        notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['displayErrorMessage', 'openWarningDialog']);

        TestBed.configureTestingModule({
            declarations: [WaitPageComponent, MockChatComponent],
            imports: [RouterTestingModule.withRoutes(routes), HttpClientTestingModule, MatProgressSpinnerModule,getTranslocoModule()],
            providers: [
                HttpClient,
                { provide: MatchRoomService, useValue: matchRoomSpy },
                { provide: MatchService, useValue: matchSpy },
                { provide: MatchContextService, useValue: questionContextSpy },
                { provide: TimeService, useValue: timeSpy },
                { provide: NotificationService, useValue: notificationServiceSpy },
                { provide: AuthenticationService, useValue: authenticationServiceSpy },
            ],
        });

        fixture = TestBed.createComponent(WaitPageComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should initalize correctly for the host', async () => {
        const mockGame: Game = {
            id: '1',
            title: 'test',
            authorId: '',
            authorName: '',
            description: 'test',
            lastModification: '2021-10-10T10:10:10.000Z',
            duration: 100,
            isVisible: true,
            questions: [],
        };
        spyOnProperty(component, 'isHost').and.returnValue(true);
        spyOnProperty(component, 'currentGame').and.returnValue(mockGame);

        await component.ngOnInit();

        expect(matchRoomSpy.gameTitle).toEqual(mockGame.title);
    });

    it('should get current game', () => {
        expect(component.currentGame).toEqual(matchSpy.currentGame);
    });

    it('time() should return the time of the timeService', () => {
        const time = 100;
        component.timeService['time'] = time;
        expect(component.time).toEqual(time);
    });

    it('toggleLock() should call toggleLock of matchRoomService', () => {
        component.toggleLock();
        expect(matchRoomSpy.toggleLock).toHaveBeenCalled();
    });

    it('banUsername() should call banUsername of matchRoomService', () => {
        component.banPlayerId('test');
        expect(matchRoomSpy.banUser).toHaveBeenCalledWith('test');
    });

    it('banUsername() should not call banUsername if user is the host', () => {
        component.banPlayerId(matchRoomSpy.getHostId());
        expect(matchRoomSpy.banUser).not.toHaveBeenCalled();
    });

    it('startMatch() should call startMatch from matchRoomService', () => {
        component.startMatch();
        expect(matchRoomSpy.startMatch).toHaveBeenCalled();
    });

    it('quitGame() should call disconnectFromRoom from matchRoomService', () => {
        component.quitGame();
        expect(matchRoomSpy.disconnectFromRoom).toHaveBeenCalled();
    });

    it('resetWaitPage() should reset all wait page attributes', () => {
        component.isLocked = true;
        matchRoomSpy.isMatchStarted = true;
        matchRoomSpy.isHostPlaying = false;
        matchRoomSpy.isBanned = true;
        matchRoomSpy.isQuitting = true;

        component['resetWaitPage']();

        expect(component.isLocked).toBe(false);
        expect(matchRoomSpy.isMatchStarted).toBe(false);
        expect(matchRoomSpy.isHostPlaying).toBe(true);
        expect(matchRoomSpy.isBanned).toBe(false);
        expect(matchRoomSpy.isQuitting).toBe(false);
    });
});
