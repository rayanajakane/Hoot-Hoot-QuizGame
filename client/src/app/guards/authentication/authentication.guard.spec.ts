import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';

import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { NotificationService } from '@app/services/notification/notification.service';
import { authenticationGuard } from './authentication.guard';
import SpyObj = jasmine.SpyObj;

describe('authenticationGuard', () => {
    let authenticationSpy: SpyObj<AuthenticationService>;
    let routerSpy: SpyObj<Router>;
    let notificationSpy: SpyObj<NotificationService>;

    beforeEach(() => {
        authenticationSpy = jasmine.createSpyObj('AuthenticationService', ['userDisplayName']);
        routerSpy = jasmine.createSpyObj('Router', ['navigateByUrl']);
        notificationSpy = jasmine.createSpyObj('NotificationService', ['displayErrorMessage']);
        TestBed.configureTestingModule({
            providers: [
                { provide: authenticationSpy, useValue: authenticationSpy },
                { provide: Router, useValue: routerSpy },
                { provide: NotificationService, useValue: notificationSpy },
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
        TestBed.runInInjectionContext(authenticationGuard);
        expect(routerSpy.navigateByUrl).not.toHaveBeenCalled();
    });
});
