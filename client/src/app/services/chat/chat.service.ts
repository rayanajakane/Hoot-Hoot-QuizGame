import { Injectable } from '@angular/core';
import { ChatChannel } from '@app/constants/chat-channels';
import { MatchContext } from '@app/constants/states';
import { MatchContextService } from '@app/services/match-context/match-context.service';
import { SocketHandlerService } from '@app/services/socket-handler/socket-handler.service';
import { ChatEmoji } from '@common/constants/chat-emojis';
import { ChatEvents } from '@common/events/chat.events';
import { Message } from '@common/interfaces/message';
import { MessageEmojiInfo, MessageInfo } from '@common/interfaces/message-info';
import { UserIdName } from '@common/interfaces/user-id-name';
import { Subject } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class ChatService {
    updateChatScroll = new Subject();
    generalMessages: Message[] = [];
    matchRoomMessages: Message[] = [];
    channel: string = ChatChannel.GENERAL;

    constructor(
        public socketHandler: SocketHandlerService,
        readonly matchContextService: MatchContextService,
    ) {}

    sendMessage(message: Message, roomCode: string): void {
        if (this.channel === ChatChannel.GENERAL) {
            this.sendGeneralMessage(message);
        } else if (this.channel === ChatChannel.ROOM && this.matchContextService.getContext() !== MatchContext.Null) {
            this.sendRoomMessage(roomCode, message);
        }
    }

    sendGeneralMessage(message: Message): void {
        this.socketHandler.send(ChatEvents.GeneralMessage, message);
    }

    handleReceivedMessages() {
        this.socketHandler.on(ChatEvents.SentGeneralMessage, (message: Message) => {
            this.generalMessages.push(message);
            if (this.channel === ChatChannel.GENERAL) {
                this.updateChatScroll.next(null);
            }
        });
    }

    sendRoomMessage(roomCode: string, message: Message): void {
        const messageInfo: MessageInfo = { roomCode, message };
        this.socketHandler.send(ChatEvents.RoomMessage, messageInfo);
    }

    handleRoomMessages() {
        this.socketHandler.on(ChatEvents.NewMessage, (messageInfo: MessageInfo) => {
            console.log('New message', messageInfo);
            this.matchRoomMessages.push(messageInfo.message);
            if (this.channel === ChatChannel.ROOM) {
                this.updateChatScroll.next(null);
            }
        });
    }

    reactToMessage(messageId: string, chatEmoji: ChatEmoji, userId: string, username: string, roomCode: string) {
        const userIdName: UserIdName = { id: userId, name: username };
        var messageEmojiInfo: MessageEmojiInfo;
        if (this.channel === ChatChannel.GENERAL) {
            messageEmojiInfo = { messageId, chatEmoji, userIdName };
            console.log('Message emo info', messageEmojiInfo);
            this.socketHandler.send(ChatEvents.GeneralEmoji, messageEmojiInfo);
        } else if (this.channel === ChatChannel.ROOM && this.matchContextService.getContext() !== MatchContext.Null) {
            messageEmojiInfo = { messageId, chatEmoji, userIdName, roomCode };
            console.log('Message emo info', messageEmojiInfo);
            this.socketHandler.send(ChatEvents.RoomEmoji, messageEmojiInfo);
        }
    }

    handleGeneralEmoji() {
        this.socketHandler.on(ChatEvents.SentGeneralEmoji, (updatedMessage: Message) => {
            const messageIndex = this.generalMessages.findIndex((message: Message) => updatedMessage.id === message.id);
            if (messageIndex > -1) {
                this.generalMessages[messageIndex] = updatedMessage;
            }
        });
    }

    handleRoomEmoji() {
        this.socketHandler.on(ChatEvents.SentRoomEmoji, (updatedMessage: Message) => {
            const messageIndex = this.matchRoomMessages.findIndex((message: Message) => updatedMessage.id === message.id);
            if (messageIndex > -1) {
                this.matchRoomMessages[messageIndex] = updatedMessage;
            }
        });
    }

    clearMessages() {
        this.generalMessages = [];
    }

    clearMatchRoomMessages() {
        this.matchRoomMessages = [];
    }
}
