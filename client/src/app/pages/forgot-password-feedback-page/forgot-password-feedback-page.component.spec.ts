import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ForgotPasswordFeedbackPageComponent } from '@app/pages/forgot-password-feedback-page/forgot-password-feedback-page.component';

describe('ForgotPasswordFeedbackPageComponent', () => {
    let component: ForgotPasswordFeedbackPageComponent;
    let fixture: ComponentFixture<ForgotPasswordFeedbackPageComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [ForgotPasswordFeedbackPageComponent],
        }).compileComponents();

        fixture = TestBed.createComponent(ForgotPasswordFeedbackPageComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
