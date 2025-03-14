import { FriendsEvents } from '@common/events/friends.events';
import { FriendsInfo } from '@common/interfaces/friends-info';
import { WebSocketGateway, WebSocketServer } from '@nestjs/websockets';
import { Server } from 'socket.io';

@WebSocketGateway({ cors: true })
export class FriendsGateway {
    @WebSocketServer() private server: Server;

    broadcastEvent(event: FriendsEvents, data: FriendsInfo): void {
        this.server.emit(event, data);
    }
}
