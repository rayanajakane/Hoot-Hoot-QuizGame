import { Message } from '@common/interfaces/message';
import { Injectable } from '@nestjs/common';

@Injectable()
export class ChatService {
    messages: Message[] = [];
    constructor() {}

    addMessage(message: Message) {
        this.messages.push(message);
    }

    getMessages(roomCode: string): Message[] {
        // TODO
        return [];
    }
}
