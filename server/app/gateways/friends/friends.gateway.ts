import { FriendsEvents } from '@common/events/friends.events';
import { FriendsInfo } from '@common/interfaces/friends-info';
import { ConnectedSocket, MessageBody, SubscribeMessage, WebSocketGateway, WebSocketServer } from '@nestjs/websockets';
import { Server, Socket } from 'socket.io';

@WebSocketGateway({ cors: true })
export class FriendsGateway {
    @WebSocketServer() private server: Server;

    @SubscribeMessage(FriendsEvents.RequestSent)
    handleRequestSent(@ConnectedSocket() socket: Socket, @MessageBody() data: FriendsInfo) {
        this.server.emit(FriendsEvents.RequestSent, data);
    }

    @SubscribeMessage(FriendsEvents.RequestAccepted)
    handleRequestAccepted(@ConnectedSocket() socket: Socket, @MessageBody() data: FriendsInfo) {
        this.server.emit(FriendsEvents.RequestAccepted, data);
    }

    @SubscribeMessage(FriendsEvents.RequestRejected)
    handleRequestRejected(@ConnectedSocket() socket: Socket, @MessageBody() data: FriendsInfo) {
        this.server.emit(FriendsEvents.RequestRejected, data);
    }

    @SubscribeMessage(FriendsEvents.RequestCanceled)
    handleRequestCanceled(@ConnectedSocket() socket: Socket, @MessageBody() data: FriendsInfo) {
        this.server.emit(FriendsEvents.RequestCanceled, data);
    }

    @SubscribeMessage(FriendsEvents.FriendRemoved)
    handleFriendRemoved(@ConnectedSocket() socket: Socket, @MessageBody() data: FriendsInfo) {
        this.server.emit(FriendsEvents.FriendRemoved, data);
    }

    public broadcastEvent(event: string, data: any): void {
        this.server.emit(event, data);
    }
}
