import { Component, OnInit } from '@angular/core';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { TranslationService } from '@app/translation/translation.service';
import { firstValueFrom, take } from 'rxjs';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss'],
})
export class AppComponent implements OnInit {
    constructor(
        private translationService: TranslationService,
        public authenticationService: AuthenticationService,
    ) {}
    ngOnInit(): void {
        firstValueFrom(this.authenticationService.authenticatedUser$.pipe(take(1))).then((user) => {
            if (user) {
                // load and set configs from DB
                const currentLang = this.translationService.getLanguageFromDB();
                this.translationService.setLanguage(currentLang);
            } else {
                // load default translation stuff?
            }
        });
    }
}
