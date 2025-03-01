import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ForgotPasswordPageComponent } from '@app/pages/forgot-password-page/forgot-password-page.component';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import SpyObj = jasmine.SpyObj;

describe('ForgotPasswordPageComponent', () => {
    let component: ForgotPasswordPageComponent;
    let fixture: ComponentFixture<ForgotPasswordPageComponent>;
    let authenticationServiceSpy: SpyObj<AuthenticationService>;

    beforeEach(async () => {
        const authenticationSpy = jasmine.createSpyObj('AuthenticationService', ['sendResetPasswordEmail']);
        await TestBed.configureTestingModule({
            declarations: [ForgotPasswordPageComponent],
            providers: [{ provide: AuthenticationService, useValue: authenticationSpy }],
        }).compileComponents();

        fixture = TestBed.createComponent(ForgotPasswordPageComponent);
        component = fixture.componentInstance;
        authenticationServiceSpy = TestBed.inject(AuthenticationService) as jasmine.SpyObj<AuthenticationService>;

        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should send reset password email', () => {
        component.email = 'test@test.test';
        component.sendResetPasswordEmail();
        expect(authenticationServiceSpy.sendResetPasswordEmail).toHaveBeenCalledWith(component.email);
    });

    it('should not send reset password email if email is empty', () => {
        component.email = '';
        component.sendResetPasswordEmail();
        expect(authenticationServiceSpy.sendResetPasswordEmail).not.toHaveBeenCalled();
    });
});
