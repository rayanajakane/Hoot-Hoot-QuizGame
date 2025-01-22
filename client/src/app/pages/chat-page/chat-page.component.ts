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

    signOut() {
        // TODO: Move this line in a more appropriate place:
        // this.chatService.socketHandler.socket.removeListener(ChatEvents.NewMessage);
        this.authenticationService.disconnectSocket();
        this.authenticationService.signOut();
        this.chatService.clearMessages();
    }
}
