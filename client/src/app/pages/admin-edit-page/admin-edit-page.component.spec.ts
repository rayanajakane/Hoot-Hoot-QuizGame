/* eslint-disable max-classes-per-file */
/* eslint-disable @typescript-eslint/no-explicit-any */
/* eslint-disable max-lines */
/* eslint-disable @typescript-eslint/no-magic-numbers */
import { HttpClientModule, HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';

import { QuestionListItemComponent } from '@app/components/question-list-item/question-list-item.component';
import { ManagementState } from '@app/constants/states';
import { Question } from '@app/interfaces/question';
import { SortByLastModificationPipe } from '@app/pipes/sort-by-last-modification.pipe';
import { NotificationService } from '@app/services/notification/notification.service';

import { DragDropModule } from '@angular/cdk/drag-drop';
import { ScrollingModule } from '@angular/cdk/scrolling';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatDialogModule } from '@angular/material/dialog';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatSliderModule } from '@angular/material/slider';
import { MatSnackBar } from '@angular/material/snack-bar';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { GameStatus } from '@app/constants/feedback-messages';
import { getMockGame } from '@app/constants/game-mocks';
import { getMockQuestion } from '@app/constants/question-mocks';
import { AdminEditPageComponent } from '@app/pages/admin-edit-page/admin-edit-page.component';
import { GameModificationService } from '@app/services/game-modification/game-modification.service';
import { getTranslocoTestingModules } from '@app/transloco-testing.module';
import { of, throwError } from 'rxjs';

describe('AdminEditPageComponent', () => {
    let component: AdminEditPageComponent;
    let fixture: ComponentFixture<AdminEditPageComponent>;

    let notificationServiceSpy: jasmine.SpyObj<NotificationService>;
    let activatedRouteSpy: jasmine.SpyObj<ActivatedRoute>;
    let gameModificationSpy: jasmine.SpyObj<GameModificationService>;

    @Component({
        selector: 'app-question-creation-form',
        template: '',
    })
    class MockCreateQuestionComponent {
        @Input() modificationState: ManagementState;
        @Input() question: Question;
        @Output() createQuestionEvent: EventEmitter<Question> = new EventEmitter<Question>();
    }

    @Component({
        selector: 'app-short-question',
        template: '',
    })
    class MockShortQuestionComponent {
        @Input() question: Question;
    }

    beforeEach(() => {
        activatedRouteSpy = jasmine.createSpyObj('ActivatedRoute', ['params', 'data', 'navigate']);
        activatedRouteSpy.params = of({ id: '1' });

        notificationServiceSpy = jasmine.createSpyObj('NotificationService', [
            'openWarningDialog',
            'displayErrorMessage',
            'displaySuccessMessage',
            'confirmBankUpload',
        ]);

        gameModificationSpy = jasmine.createSpyObj('GameModificationService', ['setGame', 'setNewGame']);

        TestBed.configureTestingModule({
            imports: [
                HttpClientModule,
                MatDialogModule,
                RouterTestingModule,
                MatIconModule,
                MatCardModule,
                MatExpansionModule,
                MatFormFieldModule,
                MatInputModule,
                BrowserAnimationsModule,
                DragDropModule,
                ReactiveFormsModule,
                MatSidenavModule,
                ScrollingModule,
                MatSliderModule,
                ...getTranslocoTestingModules(),
            ],
            declarations: [
                AdminEditPageComponent,
                SortByLastModificationPipe,
                MockCreateQuestionComponent,
                QuestionListItemComponent,
                MockShortQuestionComponent,
            ],
            providers: [
                { provide: MatSnackBar, useValue: {} },
                { provide: NotificationService, useValue: notificationServiceSpy },
                { provide: ActivatedRoute, useValue: activatedRouteSpy },
                { provide: GameModificationService, useValue: gameModificationSpy },
            ],
        }).compileComponents();

        gameModificationSpy.gameForm = new FormGroup({
            title: new FormControl(''),
            description: new FormControl(''),
            duration: new FormControl('10'),
        });
        gameModificationSpy.gameForm.setValue({ title: 'Test', description: 'Test', duration: '10' });
        gameModificationSpy.game = JSON.parse(JSON.stringify(getMockGame()));
        gameModificationSpy.originalBankQuestions = [JSON.parse(JSON.stringify(getMockQuestion()))];
        gameModificationSpy.bankQuestions = [JSON.parse(JSON.stringify(getMockQuestion()))];

        fixture = TestBed.createComponent(AdminEditPageComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should set state to Modification and call setGame when game is to be modified', () => {
        activatedRouteSpy.params = of({ id: 'id' });
        component['getGameIdFromUrl']();
        expect(component.state).toEqual(ManagementState.GameModify);
        expect(gameModificationSpy.setGame).toHaveBeenCalledWith('id');
    });

    it('should set state to Creation and call setGame when game is to be created', () => {
        activatedRouteSpy.params = of({});
        component['getGameIdFromUrl']();
        expect(component.state).toEqual(ManagementState.GameCreate);
        expect(gameModificationSpy.setNewGame).toHaveBeenCalled();
    });

    it('should display error message on error', fakeAsync(() => {
        const error = new HttpErrorResponse({ error: 'Test Error', status: 404 });
        activatedRouteSpy.params = throwError(() => error);

        component['getGameIdFromUrl']();
        tick(); // Advances time in fakeAsync
        fixture.detectChanges();

        expect(notificationServiceSpy.displayErrorMessage).toHaveBeenCalledWith(`${GameStatus.FAILURE}\n${error.message}`);
    }));
});
