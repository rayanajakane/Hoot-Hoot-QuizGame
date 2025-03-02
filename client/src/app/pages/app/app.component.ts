import { Component, OnDestroy, OnInit } from '@angular/core';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { TranslocoService } from '@jsverse/transloco';
import { firstValueFrom, Subscription, take } from 'rxjs';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss'],
})
export class AppComponent implements OnInit, OnDestroy {
    constructor(
        private translocoService: TranslocoService,
        // private translationService: TranslationService,
        public authenticationService: AuthenticationService,
    ) {}
    ngOnInit(): void {
        firstValueFrom(this.authenticationService.authenticatedUser$.pipe(take(1))).then((user) => {
            if (user) {
                // load configs from DB then set language?
            } else {
                // load default translation stuff?
            }
        });
    }

}
