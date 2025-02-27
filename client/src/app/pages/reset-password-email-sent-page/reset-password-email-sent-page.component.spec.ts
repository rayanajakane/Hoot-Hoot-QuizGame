import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ResetPasswordEmailSentPageComponent } from './reset-password-email-sent-page.component';

describe('ResetPasswordEmailSentPageComponent', () => {
    let component: ResetPasswordEmailSentPageComponent;
    let fixture: ComponentFixture<ResetPasswordEmailSentPageComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [ResetPasswordEmailSentPageComponent],
        }).compileComponents();

        fixture = TestBed.createComponent(ResetPasswordEmailSentPageComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
