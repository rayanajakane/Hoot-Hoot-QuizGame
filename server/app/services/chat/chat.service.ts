import { Message } from '@common/interfaces/message';
import { Injectable } from '@nestjs/common';

@Injectable()
export class ChatService {
    private messages: Message[] = [];

    getMessages(): Message[] {
        return this.messages;
    }

    isValidMessage(message: Message): boolean {
        return message.text.replace(/\s/g, '').trim() !== '';
    }

    addMessage(message: Message): Message {
        message.date = new Date();
        this.messages.push(message); // TODO: Check if we save messages (else, remove this line)
        return message;
    }
}
