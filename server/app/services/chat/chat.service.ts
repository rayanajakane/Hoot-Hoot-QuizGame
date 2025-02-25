import { MatchRoomService } from '@app/services/match-room/match-room.service';
import { Message } from '@common/interfaces/message';
import { Injectable } from '@nestjs/common';

const INDEX_NOT_FOUND = -1;

@Injectable()
export class ChatService {
    private messages: Message[] = [];

    constructor(private readonly matchRoomService: MatchRoomService) {}

    getMessages(): Message[] {
        return this.messages;
    }

    isValidMessage(message: Message): boolean {
        return message.text.replace(/\s/g, '').trim() !== '';
    }

    addMessage(message: Message): Message {
        message.date = new Date();
        this.messages.push(message);
        return message;
    }

    getRoomMessages(roomCode: string): Message[] {
        const matchRoomIndex = this.matchRoomService.getRoomIndex(roomCode);
        if (matchRoomIndex === INDEX_NOT_FOUND) {
            return [];
        }
        return this.matchRoomService.matchRooms[matchRoomIndex].messages;
    }

    addRoomMessage(message: Message, roomCode: string) {
        const matchRoomIndex = this.matchRoomService.getRoomIndex(roomCode);
        if (matchRoomIndex === INDEX_NOT_FOUND) {
            return;
        }
        message.date = new Date();
        this.matchRoomService.matchRooms[matchRoomIndex].messages.push(message);
        return message;
    }
}
