import { Component, OnInit } from '@angular/core';
import { AuthenticationService } from '@app/services/authentication/authentication.service';

@Component({
    selector: 'app-signup-page',
    templateUrl: './signup-page.component.html',
    styleUrls: ['./signup-page.component.scss'],
})
export class SignupPageComponent implements OnInit {
    hide = true;
    email: string = '';
    username: string = '';
    password: string = '';

    constructor(private readonly authenticationService: AuthenticationService) {}

    ngOnInit() {
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

    signUp() {
        this.authenticationService.signUp(this.email, this.username, this.password);
    }

    uploadAvatar() {
        // TODO
    }
}
