import { ChatService } from '@app/services/chat/chat.service';
import { ChatEvents } from '@common/events/chat.events';
import { Message } from '@common/interfaces/message';
import { ConnectedSocket, MessageBody, SubscribeMessage, WebSocketGateway, WebSocketServer } from '@nestjs/websockets';
import { Server, Socket } from 'socket.io';

@WebSocketGateway({ cors: true })
export class ChatGateway {
    @WebSocketServer() private server: Server;

    constructor(private readonly chatService: ChatService) {}

    @SubscribeMessage(ChatEvents.PrototypeMessage)
    handlePrototypeMessage(@ConnectedSocket() socket: Socket, @MessageBody() message: Message) {
        if (this.chatService.isValidMessage(message)) {
            const updatedMessage = this.chatService.addMessage(message);
            this.sendPrototypeMessageToClients(updatedMessage);
        }
    }

    sendPrototypeMessageToClients(message: Message) {
        this.server.emit(ChatEvents.SentPrototypeMessage, message);
    }
}
