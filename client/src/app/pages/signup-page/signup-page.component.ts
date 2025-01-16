import { Component } from '@angular/core';
import { AuthenticationService } from '@app/services/authentication/authentication.service';

@Component({
    selector: 'app-signup-page',
    templateUrl: './signup-page.component.html',
    styleUrls: ['./signup-page.component.scss'],
})
export class SignupPageComponent {
    hide = true;
    username: string = '';
    password: string = '';

    constructor(private readonly authenticationService: AuthenticationService) {}

    ngOnInit() {}

    autofocus() {
        // REFERENCE: https://stackoverflow.com/questions/59893531/accessibilty-focus-is-lost-when-route-changed-in-angular
        let blurElement: HTMLElement = document.getElementById('username-input') as HTMLElement;
        blurElement.blur();

        setTimeout(function () {
            let focusElement: HTMLElement = document.getElementById('username-input') as HTMLElement;
            focusElement.focus();
        }, 0);
    }

    signup() {
        this.authenticationService.signup(this.username, this.password);
    }
}
