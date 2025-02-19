import { Component, OnInit } from '@angular/core';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { TranslocoService } from '@jsverse/transloco';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss'],
})
export class AppComponent implements OnInit {
    constructor(
        private translocoService: TranslocoService,
        public authenticationService: AuthenticationService,
    ) {}
    ngOnInit(): void {
        this.translocoService.load('fr').subscribe();
    }
}
