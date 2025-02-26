/* eslint-disable max-classes-per-file */
import en from '@assets/i18n/en.json';
import fr from '@assets/i18n/fr.json';
import testFr from '@assets/i18n/test/fr.json';
import { TranslocoTestingModule, TranslocoTestingOptions } from '@jsverse/transloco';

export const getTranslocoModule = (options: TranslocoTestingOptions = {}) => {
    return TranslocoTestingModule.forRoot({
        langs: { en, fr },
        translocoConfig: {
            availableLangs: ['en', 'fr'],
            defaultLang: 'fr',
            fallbackLang: 'en',
        },
        preloadLangs: true,
        ...options,
    });
};

// eslint-disable-next-line prefer-arrow/prefer-arrow-functions
export function getTranslocoTestingModules(options: TranslocoTestingOptions = {}) {
    return [
        TranslocoTestingModule.forRoot({
            langs: {
                en,
                fr,
                // eslint-disable-next-line @typescript-eslint/naming-convention
                'test/fr': testFr,
            },
            translocoConfig: {
                availableLangs: ['en', 'fr'],
                defaultLang: 'fr',
            },
            preloadLangs: true,
            ...options,
        }),
    ];
}
