import { DOCUMENT } from '@angular/common';
import { Inject, Injectable } from '@angular/core';
import { Language } from '@app/interfaces/language';
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
        }
    }

    getLanguageFromDB(): Language {
        const user = this.authenticationService.currentUser;
        if (user) {
            const userRef = this.authenticationService.getUserDatabaseRef(user.uid + '/configs/lang');
            get(userRef)
                .then((dataSnapshot: DataSnapshot) => {
                    if (dataSnapshot.exists()) {
                        return this.toLanguage(dataSnapshot.val());
                    } else {
                        return Language.FR;
                    }
                })
                // eslint-disable-next-line no-console
                .catch((error: unknown) => console.error(error));
        }
        return Language.FR;
    }

    getAllLanguages(): Language[] {
        return (this.translocoService.getAvailableLangs() as string[]).map((l) => this.toLanguage(l));
    }

    getCurrentLanguage(): Language {
        return this.toLanguage(this.translocoService.getActiveLang());
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
