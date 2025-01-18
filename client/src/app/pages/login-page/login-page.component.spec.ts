import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { LoginPageComponent } from './login-page.component';
import SpyObj = jasmine.SpyObj;

describe('LoginPageComponent', () => {
    let component: LoginPageComponent;
    let fixture: ComponentFixture<LoginPageComponent>;
    let authenticationServiceSpy: SpyObj<AuthenticationService>;

    beforeEach(() => {
        const authenticationSpy = jasmine.createSpyObj('AuthenticationService', ['login']);
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

    it('should login', () => {
        component.username = 'mockUsername';
        component.password = 'mockPassword';
        const loginSpy = authenticationServiceSpy.login.and.returnValue();
        component.login();
        expect(loginSpy).toHaveBeenCalledWith(component.username, component.password);
    });
});
