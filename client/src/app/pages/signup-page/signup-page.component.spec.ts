import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SignupPageComponent } from '@app/pages/signup-page/signup-page.component';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import SpyObj = jasmine.SpyObj;

describe('SignupPageComponent', () => {
    let component: SignupPageComponent;
    let fixture: ComponentFixture<SignupPageComponent>;
    let authenticationServiceSpy: SpyObj<AuthenticationService>;

    beforeEach(() => {
        const authenticationSpy = jasmine.createSpyObj('AuthenticationService', ['signUp']);
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
    it('should signUp', () => {
        component.username = 'mockUsername';
        component.password = 'mockPassword';
        const signUpSpy = authenticationServiceSpy.signUp.and.returnValue();
        component.signUp();
        expect(signUpSpy).toHaveBeenCalledWith(component.username, component.password);
    });
});
