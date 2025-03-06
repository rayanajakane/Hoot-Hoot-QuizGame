import { MatchRoomService } from '@app/services/match-room/match-room.service';
import { ChatEmoji } from '@common/constants/chat-emojis';
import { Message } from '@common/interfaces/message';
import { UserIdName } from '@common/interfaces/user-id-name';
import { Injectable } from '@nestjs/common';
import { v4 as uuidv4 } from 'uuid';

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
        message.id = uuidv4();
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

    getChatEmojiAttribute(chatEmoji: ChatEmoji) {
        switch (chatEmoji) {
            case ChatEmoji.LIKE:
                return 'userLikes';
            case ChatEmoji.LOVE:
                return 'userLoves';
            case ChatEmoji.DISLIKE:
                return 'userDislikes';
            default:
                return '';
        }
    }

    reactToGeneralMessage(messageId: string, userIdName: UserIdName, chatEmoji: ChatEmoji) {
        const messageIndex = this.messages.findIndex((message: Message) => message.id === messageId);
        let attribute = this.getChatEmojiAttribute(chatEmoji);

        if (this.messages[messageIndex][attribute].find((it: UserIdName) => it.id === userIdName.id)) {
            this.messages[messageIndex][attribute] = this.messages[messageIndex][attribute].filter((it: UserIdName) => it.id !== userIdName.id);
        } else {
            this.messages[messageIndex][attribute].push(userIdName);
        }
        return this.messages[messageIndex];
    }

    reactToRoomMessage(messageId: string, userIdName: UserIdName, chatEmoji: ChatEmoji, roomCode: string) {
        const matchRoomIndex = this.matchRoomService.getRoomIndex(roomCode);
        const messageIndex = this.getRoomMessages(roomCode).findIndex((message: Message) => message.id === messageId);
        const attribute = this.getChatEmojiAttribute(chatEmoji);
        if (this.matchRoomService.matchRooms[matchRoomIndex].messages[messageIndex][attribute].find((it: UserIdName) => it.id === userIdName.id)) {
            this.matchRoomService.matchRooms[matchRoomIndex].messages[messageIndex][attribute] = this.matchRoomService.matchRooms[
                matchRoomIndex
            ].messages[messageIndex][attribute].filter((it: UserIdName) => it.id !== userIdName.id);
        } else {
            this.matchRoomService.matchRooms[matchRoomIndex].messages[messageIndex][attribute].push(userIdName);
        }
        return this.matchRoomService.matchRooms[matchRoomIndex].messages[messageIndex];
    }
}
