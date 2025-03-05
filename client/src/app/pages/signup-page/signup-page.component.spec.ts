import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatCard } from '@angular/material/card';
import { MatError, MatLabel } from '@angular/material/form-field';
import { MatIcon } from '@angular/material/icon';
import { SignupPageComponent } from '@app/pages/signup-page/signup-page.component';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { getTranslocoModule } from '@app/transloco-testing.module';
// import SpyObj = jasmine.SpyObj;

describe('SignupPageComponent', () => {
    let component: SignupPageComponent;
    let fixture: ComponentFixture<SignupPageComponent>;
    // let authenticationServiceSpy: SpyObj<AuthenticationService>;

    beforeEach(() => {
        const authenticationSpy = jasmine.createSpyObj('AuthenticationService', ['signUp']);
        TestBed.configureTestingModule({
            imports: [getTranslocoModule(), MatError, MatIcon, ReactiveFormsModule, MatLabel, MatCard],
            declarations: [SignupPageComponent],
            providers: [{ provide: AuthenticationService, useValue: authenticationSpy }],
        });
        fixture = TestBed.createComponent(SignupPageComponent);
        component = fixture.componentInstance;
        // authenticationServiceSpy = TestBed.inject(AuthenticationService) as jasmine.SpyObj<AuthenticationService>;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
