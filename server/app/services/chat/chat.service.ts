import { Message } from '@common/interfaces/message';
import { Injectable } from '@nestjs/common';

@Injectable()
export class ChatService {
    messages: Message[] = [];

    addMessage(message: Message): Message {
        this.messages.push(message);
        return message;
    }

    getMessages(): Message[] {
        // TODO
        return [];
    }
}
