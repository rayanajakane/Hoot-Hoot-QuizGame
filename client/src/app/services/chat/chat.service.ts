import { Injectable } from '@angular/core';
import { ChatChannel } from '@app/constants/chat-channels';
import { MatchContext } from '@app/constants/states';
import { MatchContextService } from '@app/services/match-context/match-context.service';
import { SocketHandlerService } from '@app/services/socket-handler/socket-handler.service';
import { ChatEvents } from '@common/events/chat.events';
import { Message } from '@common/interfaces/message';
import { MessageInfo } from '@common/interfaces/message-info';

@Injectable({
    providedIn: 'root',
})
export class ChatService {
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
        });
    }

    sendRoomMessage(roomCode: string, message: Message): void {
        const messageInfo: MessageInfo = { roomCode, message };
        this.socketHandler.send(ChatEvents.RoomMessage, messageInfo);
    }

    handleRoomMessages() {
        this.socketHandler.on(ChatEvents.NewMessage, (messageInfo: MessageInfo) => {
            this.matchRoomMessages.push(messageInfo.message);
        });
    }

    clearMessages() {
        this.generalMessages = [];
    }

    clearMatchRoomMessages() {
        this.matchRoomMessages = [];
    }
}
