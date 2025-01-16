import { Component } from '@angular/core';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { ChatService } from '@app/services/chat/chat.service';

@Component({
    selector: 'app-chat-page',
    templateUrl: './chat-page.component.html',
    styleUrls: ['./chat-page.component.scss'],
})
export class ChatPageComponent {
    constructor(
        private readonly authenticationService: AuthenticationService,
        private readonly chatService: ChatService,
    ) {}

    logout() {
        this.authenticationService.logout();
        this.chatService.clearMessages();
    }
}
