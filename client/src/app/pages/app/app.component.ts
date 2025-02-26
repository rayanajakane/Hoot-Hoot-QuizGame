import { Component, OnDestroy, OnInit } from '@angular/core';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { TranslocoService } from '@jsverse/transloco';
import { Subscription } from 'rxjs';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss'],
})
export class AppComponent implements OnInit, OnDestroy {
    languageSubscription: Subscription;
    constructor(
        private translocoService: TranslocoService,
        // private translationService: TranslationService,
        public authenticationService: AuthenticationService,
    ) {}
    ngOnInit(): void {
        this.languageSubscription = this.translocoService.load('fr').subscribe();
    }

    ngOnDestroy(): void {
        if (this.languageSubscription) {
            this.languageSubscription.unsubscribe();
        }
    }
}
