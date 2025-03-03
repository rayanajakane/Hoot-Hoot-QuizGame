import { User } from '@angular/fire/auth';
import { Language } from '@app/interfaces/language';
import { AuthError } from '@app/services/authentication/auth-error';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { getTranslocoTestingModules } from '@app/transloco-testing.module';
import { TranslocoService } from '@jsverse/transloco';
import { createServiceFactory, SpectatorService, SpyObject } from '@ngneat/spectator';
import { TranslationService } from './translation.service';

describe('TranslationService', () => {
    let spectator: SpectatorService<TranslationService>;
    let translocoServiceSpy: SpyObject<TranslocoService>;
    let authServiceSpy: SpyObject<AuthenticationService>;

    const createService = createServiceFactory({
        service: TranslationService,
        imports: [...getTranslocoTestingModules()],
        mocks: [AuthenticationService],
    });

    const mockUser = {
        uid: 'testUser',
        email: 'test@test.test',
        displayName: 'testUser',
    } as User;

    beforeEach(() => {
        spectator = createService();
        translocoServiceSpy = spectator.inject(TranslocoService);
        authServiceSpy = spectator.inject(AuthenticationService);
    });

    it('should be created', () => {
        expect(spectator.service).toBeTruthy();
    });

    describe('getAllLanguages', () => {
        it('should return a list of available languages', () => {
            const expectedResult = [Language.EN, Language.FR];
            const result = spectator.service.getAllLanguages();
            expect(result).toEqual(expectedResult);
        });
    });

    describe('getCurrentLanguage', () => {
        describe('when active language is english', () => {
            beforeEach(() => {
                translocoServiceSpy.setActiveLang(Language.EN);
            });
            it('should return english', () => {
                const result = spectator.service.getCurrentLanguage();
                expect(result).toEqual(Language.EN);
            });
        });
        describe('when active language is french', () => {
            beforeEach(() => {
                translocoServiceSpy.setActiveLang(Language.FR);
            });
            it('should return french', () => {
                const result = spectator.service.getCurrentLanguage();
                expect(result).toEqual(Language.FR);
            });
        });
    });
    describe('initLanguageFR', () => {
        it('should set current language, document language, and activeLang to french', () => {
            const setDocumentLanguageSpy = spyOn<any>(spectator.service, 'setDocumentLanguage');
            const setLangSpy = spyOn(translocoServiceSpy, 'setActiveLang');
            spectator.service.initLanguageFR();
            expect(spectator.service.currentLangugage).toEqual(Language.FR);
            expect(setLangSpy).toHaveBeenCalledWith(Language.FR);
            expect(setDocumentLanguageSpy).toHaveBeenCalledWith(Language.FR);
        });
    });

    describe('setLanguage', () => {
        it('should call changeLanguage', () => {
            const changeLanguageSpy = spyOn<any>(spectator.service, 'changeLanguage');
            spectator.service.setLanguage('fr');
            expect(changeLanguageSpy).toHaveBeenCalledWith(Language.FR);
        });
    });

    describe('saveLanguageToDB', () => {
        describe('when user is undefined', () => {
            it('should throw authError', () => {
                expect(() => spectator.service.saveLanguageToDB(Language.FR)).toThrow(new AuthError('UserUndefined', 'user is undefined'));
            });
        });
        describe('when user is defined', () => {
            beforeEach(() => {
                authServiceSpy.currentUser = mockUser;
            });

            it('should not throw authError', () => {
                expect(() => spectator.service.saveLanguageToDB(Language.FR)).not.toThrow(new AuthError('UserUndefined', 'user is undefined'));
            });
        });
    });
});
