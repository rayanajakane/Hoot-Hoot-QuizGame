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

    reactToGeneralMessage(messageId: string, userIdName: UserIdName, chatEmoji: ChatEmoji) {
        const messageIndex = this.messages.findIndex((message: Message) => message.id === messageId);
        switch (chatEmoji) {
            case ChatEmoji.LIKE:
                if (this.messages[messageIndex].userLikes.find((it) => it.id === userIdName.id)) {
                    this.messages[messageIndex].userLikes = this.messages[messageIndex].userLikes.filter((it) => it.id !== userIdName.id);
                } else {
                    this.messages[messageIndex].userLikes.push(userIdName);
                }
                break;
            case ChatEmoji.LOVE:
                if (this.messages[messageIndex].userLoves.find((it) => it.id === userIdName.id)) {
                    this.messages[messageIndex].userLoves = this.messages[messageIndex].userLoves.filter((it) => it.id !== userIdName.id);
                } else {
                    this.messages[messageIndex].userLoves.push(userIdName);
                }
                break;
            case ChatEmoji.DISLIKE:
                if (this.messages[messageIndex].userDislikes.find((it) => it.id === userIdName.id)) {
                    this.messages[messageIndex].userDislikes = this.messages[messageIndex].userDislikes.filter((it) => it.id !== userIdName.id);
                } else {
                    this.messages[messageIndex].userDislikes.push(userIdName);
                }
                break;
        }
        return this.messages[messageIndex];
    }

    reactToRoomMessage(messageId: string, userIdName: UserIdName, chatEmoji: ChatEmoji, roomCode: string) {
        const matchRoomIndex = this.matchRoomService.getRoomIndex(roomCode);
        const messageIndex = this.getRoomMessages(roomCode).findIndex((message: Message) => message.id === messageId);
        // TODO: Use dict instead
        switch (chatEmoji) {
            case ChatEmoji.LIKE:
                if (this.matchRoomService.matchRooms[matchRoomIndex].messages[messageIndex].userLikes.find((it) => it.id === userIdName.id)) {
                    this.matchRoomService.matchRooms[matchRoomIndex].messages[messageIndex].userLikes = this.matchRoomService.matchRooms[
                        matchRoomIndex
                    ].messages[messageIndex].userLikes.filter((it) => it.id !== userIdName.id);
                } else {
                    this.matchRoomService.matchRooms[matchRoomIndex].messages[messageIndex].userLikes.push(userIdName);
                }
                break;
            case ChatEmoji.LOVE:
                if (this.matchRoomService.matchRooms[matchRoomIndex].messages[messageIndex].userLoves.find((it) => it.id === userIdName.id)) {
                    this.matchRoomService.matchRooms[matchRoomIndex].messages[messageIndex].userLoves = this.matchRoomService.matchRooms[
                        matchRoomIndex
                    ].messages[messageIndex].userLoves.filter((it) => it.id !== userIdName.id);
                } else {
                    this.matchRoomService.matchRooms[matchRoomIndex].messages[messageIndex].userLoves.push(userIdName);
                }
                break;
            case ChatEmoji.DISLIKE:
                if (this.matchRoomService.matchRooms[matchRoomIndex].messages[messageIndex].userDislikes.find((it) => it.id === userIdName.id)) {
                    this.matchRoomService.matchRooms[matchRoomIndex].messages[messageIndex].userDislikes = this.matchRoomService.matchRooms[
                        matchRoomIndex
                    ].messages[messageIndex].userDislikes.filter((it) => it.id !== userIdName.id);
                } else {
                    this.matchRoomService.matchRooms[matchRoomIndex].messages[messageIndex].userDislikes.push(userIdName);
                }
                break;
        }
        return this.matchRoomService.matchRooms[matchRoomIndex].messages[messageIndex];
    }
}
