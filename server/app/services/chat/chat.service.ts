import { Message } from '@common/interfaces/message';
import { Injectable } from '@nestjs/common';

@Injectable()
export class ChatService {
    messages: Message[] = [];

    addMessage(message: Message): Message {
        message.date = new Date();
        this.messages.push(message);
        return message;
    }

    getMessages(): Message[] {
        // TODO
        return [];
    }
}
