import { Component, OnInit } from '@angular/core';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { ThemeService } from '@app/services/theme/theme.service';
import { TranslationService } from '@app/translation/translation.service';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss'],
})
export class AppComponent implements OnInit {
    constructor(
        private translationService: TranslationService,
        private themeService: ThemeService,
        public authenticationService: AuthenticationService,
    ) {}
    ngOnInit(): void {
        this.authenticationService.authenticatedUser.subscribe(async (user) => {
            if (user) {
                const currentLangugage = await this.translationService.getLanguageFromDB();
                const currentTheme = await this.themeService.getThemeFromDB();
                this.translationService.setLanguage(currentLangugage);
                this.themeService.setTheme(currentTheme);
            } else {
                // Fallback language in case user is not authenticated
                this.translationService.initLanguageFR();
                this.themeService.initLightTheme();
            }
        });
    }
}
