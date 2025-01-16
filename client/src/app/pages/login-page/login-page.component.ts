import { Component } from '@angular/core';
import { AuthenticationService } from '@app/services/authentication/authentication.service';

@Component({
    selector: 'app-login-page',
    templateUrl: './login-page.component.html',
    styleUrls: ['./login-page.component.scss'],
})
export class LoginPageComponent {
    hide = true;
    username: string = '';
    password: string = '';

    constructor(private readonly authenticationService: AuthenticationService) {}

    ngOnInit() {
        this.autofocus();
    }

    autofocus() {
        // REFERENCE: https://stackoverflow.com/questions/59893531/accessibilty-focus-is-lost-when-route-changed-in-angular
        let blurElement: HTMLElement = document.getElementById('username-input') as HTMLElement;
        blurElement.blur();

        setTimeout(function () {
            let focusElement: HTMLElement = document.getElementById('username-input') as HTMLElement;
            focusElement.focus();
        }, 0);
    }

    // TODO: Maybe disable the login (and sign up) buttons when Firebase is loading?
    login() {
        this.authenticationService.login(this.username, this.password);
    }
}
