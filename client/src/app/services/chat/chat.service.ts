import { Injectable } from '@angular/core';
import { SocketHandlerService } from '@app/services/socket-handler/socket-handler.service';
import { ChatEvents } from '@common/events/chat.events';
import { Message } from '@common/interfaces/message';

@Injectable({
    providedIn: 'root',
})
export class ChatService {
    messages: Message[] = [];
    constructor(public socketHandler: SocketHandlerService) {}

    sendPrototypeMessage(message: Message): void {
        this.socketHandler.send(ChatEvents.PrototypeMessage, message);
    }

    handleReceivedMessages() {
        this.socketHandler.on(ChatEvents.SentPrototypeMessage, (message: Message) => {
            this.messages.push(message);
        });
    }

    clearMessages() {
        this.messages = [];
    }
}
