import { ChatService } from '@app/services/chat/chat.service';
import { ChatEvents } from '@common/events/chat.events';
import { Message } from '@common/interfaces/message';
import { MessageEmojiInfo, MessageInfo } from '@common/interfaces/message-info';
import { ConnectedSocket, MessageBody, SubscribeMessage, WebSocketGateway, WebSocketServer } from '@nestjs/websockets';
import { Server, Socket } from 'socket.io';

@WebSocketGateway({ cors: true })
export class ChatGateway {
    @WebSocketServer() private server: Server;

    constructor(private readonly chatService: ChatService) {}

    @SubscribeMessage(ChatEvents.GeneralMessage)
    handleGeneralMessage(@ConnectedSocket() socket: Socket, @MessageBody() message: Message) {
        if (this.chatService.isValidMessage(message)) {
            const updatedMessage = this.chatService.addMessage(message);
            this.sendGeneralMessage(updatedMessage);
        }
    }

    @SubscribeMessage(ChatEvents.RoomMessage)
    handleRoomMessage(@ConnectedSocket() socket: Socket, @MessageBody() data: MessageInfo) {
        if (this.chatService.isValidMessage(data.message)) {
            this.chatService.addRoomMessage(data.message, data.roomCode);
            this.sendRoomMessage(data);
        }
    }

    @SubscribeMessage(ChatEvents.GeneralEmoji)
    handleGeneralEmoji(@ConnectedSocket() socket: Socket, @MessageBody() data: MessageEmojiInfo) {
        const updatedMessage = this.chatService.reactToGeneralMessage(data.messageId, data.userIdName, data.chatEmoji);
        this.sendGeneralEmoji(updatedMessage);
    }

    @SubscribeMessage(ChatEvents.RoomEmoji)
    handleRoomEmoji(@ConnectedSocket() socket: Socket, @MessageBody() data: MessageEmojiInfo) {
        const updatedMessage = this.chatService.reactToRoomMessage(data.messageId, data.userIdName, data.chatEmoji, data.roomCode);
        this.sendRoomEmoji(updatedMessage, data.roomCode);
    }

    sendRoomMessage(data: MessageInfo) {
        this.server.to(data.roomCode).emit(ChatEvents.NewMessage, data);
    }

    sendGeneralMessage(message: Message) {
        this.server.emit(ChatEvents.SentGeneralMessage, message);
    }

    sendRoomEmoji(updatedMessage: Message, roomCode: string) {
        this.server.to(roomCode).emit(ChatEvents.SentRoomEmoji, updatedMessage);
    }

    sendGeneralEmoji(updatedMessage: Message) {
        this.server.emit(ChatEvents.SentGeneralEmoji, updatedMessage);
    }
}
