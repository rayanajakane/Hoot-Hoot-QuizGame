import { ChatService } from '@app/services/chat/chat.service';
import { ChatEvents } from '@common/events/chat.events';
import { Message } from '@common/interfaces/message';
import { MessageInfo } from '@common/interfaces/message-info';
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

    sendRoomMessage(data: MessageInfo) {
        this.server.to(data.roomCode).emit(ChatEvents.NewMessage, data);
    }

    sendGeneralMessage(message: Message) {
        this.server.emit(ChatEvents.SentGeneralMessage, message);
    }
}
