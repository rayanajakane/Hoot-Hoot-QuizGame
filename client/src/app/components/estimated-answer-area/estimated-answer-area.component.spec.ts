import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { EstimatedAnswerAreaComponent } from '@app/components/estimated-answer-area/estimated-answer-area.component';
import { getMockQuestion } from '@app/constants/question-mocks';
import { Question } from '@app/interfaces/question';
import { AnswerService } from '@app/services/answer/answer.service';
import { MatchContextService } from '@app/services/match-context/match-context.service';
import { MatchRoomService } from '@app/services/match-room/match-room.service';
import spyObj = jasmine.SpyObj;

describe('EstimatedAnswerAreaComponent', () => {
    let component: EstimatedAnswerAreaComponent;
    let fixture: ComponentFixture<EstimatedAnswerAreaComponent>;
    let answerSpy: spyObj<AnswerService>;
    let matchRoomSpy: spyObj<MatchRoomService>;
    let questionContextSpy: spyObj<MatchContextService>;
    let mockQuestion: Question;

    beforeEach(async () => {
        mockQuestion = getMockQuestion();
        matchRoomSpy = jasmine.createSpyObj('MatchRoomService', [
            'goToNextQuestion',
            'getUsername',
            'getRoomCode',
            'disconnect',
            'sendPlayersData',
            'onRouteToResultsPage',
            'routeToResultsPage',
            'onGameOver',
        ]);

        matchRoomSpy.currentQuestion = mockQuestion;
        answerSpy = jasmine.createSpyObj('AnswerService', [
            'selectChoice',
            'deselectChoice',
            'submitAnswer',
            'onFeedback',
            'onBonusPoints',
            'onEndGame',
            'onTimesUp',
            'onGradeAnswers',
            'resetStateForNewQuestion',
            'listenToAnswerEvents',
        ]);
        answerSpy.currentLongAnswer = '0';
        answerSpy.showingFeedback$ = jasmine.createSpyObj('showFeedback$', ['subscribe']);
        answerSpy.showFeedback = false;
        questionContextSpy = jasmine.createSpyObj('MatchContextService', ['getContext']);

        await TestBed.configureTestingModule({
            imports: [MatFormFieldModule, MatInputModule, ReactiveFormsModule, NoopAnimationsModule],
            declarations: [EstimatedAnswerAreaComponent],
            providers: [
                { provide: AnswerService, useValue: answerSpy },
                { provide: MatchRoomService, useValue: matchRoomSpy },
                { provide: MatchContextService, useValue: questionContextSpy },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(EstimatedAnswerAreaComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
