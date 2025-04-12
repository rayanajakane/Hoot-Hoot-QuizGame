import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { QuestionGeneratorComponent } from '@app/components/question-generator/question-generator.component';
import { ManagementState } from '@app/constants/states';
import { DialogManagement, QuestionService } from '@app/services/question/question.service';
import { getTranslocoModule } from '@app/transloco-testing.module';

describe('QuestionGeneratorComponent', () => {
    let component: QuestionGeneratorComponent;
    let fixture: ComponentFixture<QuestionGeneratorComponent>;
    const dialogData: DialogManagement = { modificationState: ManagementState.GameCreate };

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [getTranslocoModule()],
            declarations: [QuestionGeneratorComponent],
            providers: [
                { provide: MAT_DIALOG_DATA, useValue: dialogData },
                { provide: MatDialogRef, useValue: jasmine.createSpyObj('MatDialogRef', ['close']) },
                { provide: QuestionService, useValue: jasmine.createSpyObj('QuestionService', ['validateChoicesLength']) },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(QuestionGeneratorComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
