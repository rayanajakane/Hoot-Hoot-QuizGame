import { Component, OnInit } from '@angular/core';
import { AuthenticationService } from '@app/services/authentication/authentication.service';

@Component({
    selector: 'app-login-page',
    templateUrl: './login-page.component.html',
    styleUrls: ['./login-page.component.scss'],
})
export class LoginPageComponent implements OnInit {
    hide = true;
    username: string = '';
    password: string = '';

    constructor(private readonly authenticationService: AuthenticationService) {}

    ngOnInit() {
        this.autofocus();
    }

    autofocus() {
        // REFERENCE: https://stackoverflow.com/questions/59893531/accessibilty-focus-is-lost-when-route-changed-in-angular
        const blurElement: HTMLElement = document.getElementById('username-input') as HTMLElement;
        blurElement?.blur();

        setTimeout(() => {
            const focusElement: HTMLElement = document.getElementById('username-input') as HTMLElement;
            focusElement?.focus();
        }, 0);
    }

    // TODO: Maybe disable the login (and sign up) buttons when Firebase is loading?
    signIn() {
        this.authenticationService.signIn(this.username, this.password);
    }
}
