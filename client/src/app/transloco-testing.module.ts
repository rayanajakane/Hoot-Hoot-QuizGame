import en from '@assets/i18n/en.json';
import fr from '@assets/i18n/fr.json';
import { TranslocoTestingModule, TranslocoTestingOptions } from '@jsverse/transloco';

export const getTranslocoModule = (options: TranslocoTestingOptions = {}) => {
    return TranslocoTestingModule.forRoot({
        langs: { en, fr },
        translocoConfig: {
            availableLangs: ['en', 'fr'],
            defaultLang: 'fr',
        },
        preloadLangs: true,
        ...options,
    });
};
