import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LoginPageComponent } from '@app/pages/login-page/login-page.component';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import SpyObj = jasmine.SpyObj;

describe('LoginPageComponent', () => {
    let component: LoginPageComponent;
    let fixture: ComponentFixture<LoginPageComponent>;
    let authenticationServiceSpy: SpyObj<AuthenticationService>;

    beforeEach(() => {
        const authenticationSpy = jasmine.createSpyObj('AuthenticationService', ['signIn']);
        TestBed.configureTestingModule({
            declarations: [LoginPageComponent],
            providers: [{ provide: AuthenticationService, useValue: authenticationSpy }],
        });
        fixture = TestBed.createComponent(LoginPageComponent);
        component = fixture.componentInstance;
        authenticationServiceSpy = TestBed.inject(AuthenticationService) as jasmine.SpyObj<AuthenticationService>;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should signIn', () => {
        component.email = 'mockUsername';
        component.password = 'mockPassword';
        const signInSpy = authenticationServiceSpy.signIn.and.returnValue();
        component.signIn();
        expect(signInSpy).toHaveBeenCalledWith(component.email, component.password);
    });
});
