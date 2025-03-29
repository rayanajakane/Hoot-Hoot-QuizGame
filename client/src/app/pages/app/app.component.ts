import { Component, OnInit } from '@angular/core';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { MatchContextService } from '@app/services/match-context/match-context.service';
import { MoneyService } from '@app/services/money/money.service';
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
        public matchContextService: MatchContextService,
        public moneyService: MoneyService,
    ) {}
    ngOnInit(): void {
        this.authenticationService.authenticatedUser.subscribe(async (user) => {
            if (user) {
                const currentLangugage = await this.translationService.getLanguageFromDB();
                this.translationService.setLanguage(currentLangugage);
            } else {
                this.translationService.initLanguageFR();
            }
        });
    }
}
