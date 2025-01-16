import { Message } from '@common/interfaces/message';
import { Injectable } from '@nestjs/common';

@Injectable()
export class ChatService {
    messages: Message[] = [];
    constructor() {}

    addMessage(message: Message): Message {
        this.messages.push(message);
        return message;
    }

    getMessages(roomCode: string): Message[] {
        // TODO
        return [];
    }
}
