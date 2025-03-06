/* eslint-disable max-classes-per-file */
import { Injectable, NgModule } from '@angular/core';
import { provideTransloco, Translation, TranslocoLoader, TranslocoModule } from '@jsverse/transloco';
// eslint-disable-next-line no-restricted-imports
import { HttpClient } from '@angular/common/http';
// eslint-disable-next-line no-restricted-imports
import { environment } from '../environments/environment';

@Injectable({ providedIn: 'root' })
export class TranslocoHttpLoader implements TranslocoLoader {
    constructor(private http: HttpClient) {}

    getTranslation(lang: string) {
        return this.http.get<Translation>(`/assets/i18n/${lang}.json`);
    }
}

@NgModule({
    exports: [TranslocoModule],
    providers: [
        provideTransloco({
            config: {
                availableLangs: ['en', 'fr'],
                defaultLang: 'fr',
                reRenderOnLangChange: true,
                prodMode: environment.production,
            },
            loader: TranslocoHttpLoader,
        }),
    ],
})
export class TranslocoRootModule {}
