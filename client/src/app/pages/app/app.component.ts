import { Component, OnInit } from '@angular/core';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { TranslationService } from '@app/translation/translation.service';

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
        this.authenticationService.authenticatedUser.subscribe(async (user) => {
            if (user) {
                const currentLangugage = await this.translationService.getLanguageFromDB();
                this.translationService.setLanguage(currentLangugage);
            } else {
                // Fallback language in case user is not authenticated
                this.translationService.initLanguageFR();
            }
        });
    }
}
