import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';

import { Auth } from '@angular/fire/auth';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { NotificationService } from '@app/services/notification/notification.service';
import { getTranslocoModule } from '@app/transloco-testing.module';
import { authenticationGuard } from './authentication.guard';
import SpyObj = jasmine.SpyObj;

describe('authenticationGuard', () => {
    let authenticationSpy: SpyObj<AuthenticationService>;
    let routerSpy: SpyObj<Router>;
    let notificationSpy: SpyObj<NotificationService>;
    let mockAuth: SpyObj<Auth>;

    beforeEach(() => {
        authenticationSpy = jasmine.createSpyObj('AuthenticationService', ['userDisplayName', 'isUserAuthenticated']);
        routerSpy = jasmine.createSpyObj('Router', ['navigateByUrl']);
        notificationSpy = jasmine.createSpyObj('NotificationService', ['displayErrorMessage']);
        mockAuth = jasmine.createSpyObj<Auth>('Auth', ['name']);
        TestBed.configureTestingModule({
            imports: [getTranslocoModule()],
            providers: [
                { provide: AuthenticationService, useValue: authenticationSpy },
                { provide: Router, useValue: routerSpy },
                { provide: NotificationService, useValue: notificationSpy },
                { provide: Auth, useValue: mockAuth },
            ],
        });
    });

    it('should redirect to login page if user is not authenticated', () => {
        (authenticationSpy as any).userDisplayName = '';
        TestBed.runInInjectionContext(authenticationGuard);
        expect(routerSpy.navigateByUrl).toHaveBeenCalledWith('/login');
    });

    it('should not redirect to login page if user is authenticated', () => {
        (authenticationSpy as any).userDisplayName = 'LoremIpsum';
        authenticationSpy.isUserAuthenticated.and.returnValue(true);
        TestBed.runInInjectionContext(authenticationGuard);
        expect(routerSpy.navigateByUrl).not.toHaveBeenCalled();
    });
});
