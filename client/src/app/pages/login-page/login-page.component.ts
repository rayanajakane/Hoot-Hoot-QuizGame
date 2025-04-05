import { Component, OnInit } from '@angular/core';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { Theme, ThemeService } from '@app/services/theme/theme.service';

@Component({
    selector: 'app-login-page',
    templateUrl: './login-page.component.html',
    styleUrls: ['./login-page.component.scss'],
})
export class LoginPageComponent implements OnInit {
    hide = true;
    email: string = '';
    password: string = '';

    constructor(
        private readonly authenticationService: AuthenticationService,
        private themeService: ThemeService,
    ) {}

    ngOnInit() {
        this.themeService.setTheme(Theme.LIGHT);
        this.autofocus();
    }

    autofocus() {
        // REFERENCE: https://stackoverflow.com/questions/59893531/accessibilty-focus-is-lost-when-route-changed-in-angular
        const blurElement: HTMLElement = document.getElementById('email-input') as HTMLElement;
        blurElement?.blur();

        setTimeout(() => {
            const focusElement: HTMLElement = document.getElementById('email-input') as HTMLElement;
            focusElement?.focus();
        }, 0);
    }

    // TODO: Maybe disable the login (and sign up) buttons when Firebase is loading?
    signIn() {
        this.authenticationService.signIn(this.email, this.password);
    }
}
