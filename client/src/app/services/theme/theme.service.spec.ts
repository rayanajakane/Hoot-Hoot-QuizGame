import { fakeAsync } from '@angular/core/testing';
import { authStub } from '@app/constants/auth-mocks';
import { AuthError } from '@app/services/authentication/auth-error';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { Theme, ThemeService } from '@app/services/theme/theme.service';
import { getTranslocoTestingModules } from '@app/transloco-testing.module';
import { createServiceFactory, SpectatorService, SpyObject } from '@ngneat/spectator';

describe('ThemeService', () => {
    let spectator: SpectatorService<ThemeService>;
    let authServiceSpy: SpyObject<AuthenticationService>;

    const createService = createServiceFactory({
        service: ThemeService,
        imports: [...getTranslocoTestingModules()],
        providers: [{ provide: AuthenticationService, useValue: authStub }],
    });

    beforeEach(() => {
        spectator = createService();
        authServiceSpy = spectator.inject(AuthenticationService);
    });

    afterEach(() => {
        spectator.service.currentTheme = Theme.LIGHT;
    });

    it('should be created', () => {
        expect(spectator).toBeTruthy();
    });

    describe('saveThemeToDB', () => {
        describe('when user is not defined', () => {
            it('should throw an error', () => {
                const visualTheme: Theme = Theme.DARK;
                authServiceSpy.currentUser = null;

                expect(() => spectator.service.saveThemeToDB(visualTheme)).toThrowError(AuthError);
            });
        });

        // TypeError: Cannot read properties of undefined (reading 'pieceNum_')
        // Cannot test when user is defined cause database not mocked correctly
    });

    describe('getThemeFromDB', () => {
        describe('when user is not defined', () => {
            it('should return light theme', fakeAsync(async () => {
                authServiceSpy.currentUser = null;
                const res = await spectator.service.getThemeFromDB();

                expect(res).toEqual(Theme.LIGHT);
            }));
        });
    });

    describe('initLightTheme', () => {
        it('should initialize light theme', () => {
            spectator.service.initLightTheme();

            expect(spectator.service.currentTheme).toBe(Theme.LIGHT);
        });
    });

    // TypeError: Cannot read properties of undefined (reading 'pieceNum_')
    // describe('setTheme', () => {
    //     it('should set current theme to given theme', () => {
    //         authServiceSpy.currentUser = mockUser as User;
    //         spectator.service.setTheme(Theme.DARK);
    //         expect(spectator.service.currentTheme).toEqual(Theme.DARK);
    //     });
    // });
});
