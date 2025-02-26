import { ComponentFixture, TestBed } from '@angular/core/testing';
import { UserEditPageComponent } from '@app/pages/user-edit-page/user-edit-page.component';
import { AuthenticationService } from '@app/services/authentication/authentication.service';

describe('UserEditPageComponent', () => {
    let component: UserEditPageComponent;
    let fixture: ComponentFixture<UserEditPageComponent>;

    beforeEach(async () => {
        const authenticationSpy = jasmine.createSpyObj('AuthenticationService', ['deleteUser']);
        await TestBed.configureTestingModule({
            declarations: [UserEditPageComponent],
            providers: [{ provide: AuthenticationService, useValue: authenticationSpy }],
        }).compileComponents();

        fixture = TestBed.createComponent(UserEditPageComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
