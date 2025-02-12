import { Component } from '@angular/core';
import { AuthenticationService } from '@app/services/authentication/authentication.service';

@Component({
    selector: 'app-chat-page',
    templateUrl: './chat-page.component.html',
    styleUrls: ['./chat-page.component.scss'],
})
export class ChatPageComponent {
    constructor(private readonly authenticationService: AuthenticationService) {}

    signOut() {
        this.authenticationService.signOut();
    }
}
