import { Message } from '@common/interfaces/message';
import { Injectable } from '@nestjs/common';

@Injectable()
export class ChatService {
    messages: Message[] = [];
    constructor() {}

    addMessage(message: Message) {
        // TODO
    }

    getMessages(roomCode: string): Message[] {
        // TODO
        return [];
    }
}
