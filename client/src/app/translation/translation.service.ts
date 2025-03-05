import { DOCUMENT } from '@angular/common';
import { Inject, Injectable } from '@angular/core';
import { Language } from '@app/interfaces/language';
import { AuthError } from '@app/services/authentication/auth-error';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { TranslocoService } from '@jsverse/transloco';
import { DataSnapshot, get, update } from 'firebase/database';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class TranslationService {
    languageChanges$: Observable<string>;
    currentLangugage: Language;

    constructor(
        @Inject(DOCUMENT) private document: Document,
        private translocoService: TranslocoService,
        private authenticationService: AuthenticationService,
    ) {
        this.languageChanges$ = this.translocoService.langChanges$;
    }

    saveLanguageToDB(language: Language) {
        const user = this.authenticationService.currentUser;
        if (user) {
            const userRef = this.authenticationService.getUserDatabaseRef(user.uid + '/configs');
            update(userRef, { lang: language });
        } else {
            throw new AuthError('UserUndefined', 'user is undefined');
        }
    }

    async getLanguageFromDB(): Promise<Language> {
        const user = this.authenticationService.currentUser;
        if (user) {
            const userRef = this.authenticationService.getUserDatabaseRef(user.uid + '/configs/lang');
            return get(userRef)
                .then((dataSnapshot: DataSnapshot) => {
                    if (dataSnapshot.exists()) {
                        return this.toLanguage(dataSnapshot.val());
                    }
                    return Language.FR;
                })
                .catch((error: unknown) => {
                    // eslint-disable-next-line no-console
                    console.error(error);
                    return Language.FR;
                });
        } else {
            return Language.FR;
        }
    }

    getAllLanguages(): Language[] {
        return (this.translocoService.getAvailableLangs() as string[]).map((language) => this.toLanguage(language));
    }

    getCurrentLanguage(): Language {
        return this.toLanguage(this.translocoService.getActiveLang());
    }

    initLanguageFR() {
        this.currentLangugage = Language.FR;
        this.setDocumentLanguage(Language.FR);
        this.translocoService.setActiveLang(Language.FR);
    }

    setLanguage(language: string) {
        this.changeLanguage(this.toLanguage(language));
    }

    private changeLanguage(language: Language) {
        this.currentLangugage = language;
        this.setDocumentLanguage(language);
        this.translocoService.setActiveLang(language);
        this.saveLanguageToDB(language);
    }

    private setDocumentLanguage(language: Language) {
        this.document.documentElement.lang = language;
    }

    private toLanguage(language: string): Language {
        switch (language) {
            case 'fr':
                return Language.FR;
            case 'en':
            default:
                return Language.EN;
        }
    }
}
