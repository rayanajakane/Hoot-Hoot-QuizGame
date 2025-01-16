import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { ChatEvents } from '@common/events/chat.events';
import { SocketHandlerService } from '../socket-handler/socket-handler.service';

@Injectable({
    providedIn: 'root',
})
export class AuthenticationService {
    constructor(
        private readonly router: Router,
        private readonly socketHandler: SocketHandlerService,
    ) {}

    get userDisplayName(): string {
        // TODO
        return 'TODO';
    }

    signup(username: string, password: string) {
        // TODO: Firebase Authentication + NotificationService
        this.router.navigateByUrl('/chat');
    }

    login(username: string, password: string) {
        // TODO: Firebase Authentication + NotificationService

        // If user can login:
        this.router.navigateByUrl('/chat');
    }

    connectToSocket() {
        this.socketHandler.connect();
    }

    disconnectSocket() {
        this.socketHandler.disconnect();
    }

    logout() {
        // TODO: Firebase Authentication
        this.socketHandler.socket.removeListener(ChatEvents.NewMessage);
        this.router.navigateByUrl('/login');
    }
}
