import { Component } from '@angular/core';
import { DisplayAuthenticationText } from 'src/assets/i18n/display/display-texts-fr';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { ChatService } from '@app/services/chat/chat.service';

@Component({
    selector: 'app-chat-page',
    templateUrl: './chat-page.component.html',
    styleUrls: ['./chat-page.component.scss'],
})
export class ChatPageComponent {
    displayText = DisplayAuthenticationText;

    constructor(
        private readonly authenticationService: AuthenticationService,
        private readonly chatService: ChatService,
    ) {}

    logout() {
        this.authenticationService.signOut();
        this.chatService.clearMessages();
    }
}
