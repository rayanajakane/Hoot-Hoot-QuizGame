import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { SignupPageComponent } from './signup-page.component';
import SpyObj = jasmine.SpyObj;

describe('SignupPageComponent', () => {
    let component: SignupPageComponent;
    let fixture: ComponentFixture<SignupPageComponent>;
    let authenticationServiceSpy: SpyObj<AuthenticationService>;

    beforeEach(() => {
        const authenticationSpy = jasmine.createSpyObj('AuthenticationService', ['signup']);
        TestBed.configureTestingModule({
            declarations: [SignupPageComponent],
            providers: [{ provide: AuthenticationService, useValue: authenticationSpy }],
        });
        fixture = TestBed.createComponent(SignupPageComponent);
        component = fixture.componentInstance;
        authenticationServiceSpy = TestBed.inject(AuthenticationService) as jasmine.SpyObj<AuthenticationService>;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
    it('should signup', () => {
        component.username = 'mockUsername';
        component.password = 'mockPassword';
        const signupSpy = authenticationServiceSpy.signup.and.returnValue();
        component.signup();
        expect(signupSpy).toHaveBeenCalledWith(component.username, component.password);
    });
});
