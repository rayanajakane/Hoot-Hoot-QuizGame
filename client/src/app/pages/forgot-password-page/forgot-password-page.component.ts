import { Component } from '@angular/core';
import { AuthenticationService } from '@app/services/authentication/authentication.service';

@Component({
    selector: 'app-forgot-password-page',
    templateUrl: './forgot-password-page.component.html',
    styleUrl: './forgot-password-page.component.scss',
})
export class ForgotPasswordPageComponent {
    email: string = '';

    constructor(public authenticationService: AuthenticationService) {}

    sendResetPasswordEmail() {
        if (!this.email) return;
        this.authenticationService.sendResetPasswordEmail(this.email);
    }
}
