import { FriendsService } from '@app/services/friends/friends.service';
import { HistoryService } from '@app/services/history/history.service';
import { FriendsEvents } from '@common/events/friends.events';
import { FriendsInfo } from '@common/interfaces/friends-info';
import { HistoryAuthItem } from '@common/interfaces/history-items';
import { OnGatewayDisconnect, SubscribeMessage, WebSocketGateway, WebSocketServer } from '@nestjs/websockets';
import { Server, Socket } from 'socket.io';
import { v4 as uuidv4 } from 'uuid';

@WebSocketGateway({ cors: true })
export class FriendsGateway implements OnGatewayDisconnect {
    @WebSocketServer() private server: Server;
    private userSockets: Map<string, string> = new Map(); // userId -> socketId
    constructor(
        private friendsService: FriendsService,
        private historyService: HistoryService,
    ) {}

    @SubscribeMessage(FriendsEvents.UpdateData)
    async updateData(client: Socket) {
        const allUsers = await this.friendsService.getAllUsers('');
        allUsers.forEach(async (user) => {
            this.server.to(this.userSockets.get(user.id)).emit(FriendsEvents.ReturnAllUsers, await this.friendsService.getAllUsers(user.id));
            this.server.to(this.userSockets.get(user.id)).emit(FriendsEvents.ReturnAllFriends, await this.friendsService.getFriendsList(user.id));
            this.server
                .to(this.userSockets.get(user.id))
                .emit(FriendsEvents.ReturnAllPendingRequests, await this.friendsService.getPendingRequests(user.id));
            this.server
                .to(this.userSockets.get(user.id))
                .emit(FriendsEvents.ReturnAllSentRequests, await this.friendsService.getSentRequests(user.id));
        });
    }

    @SubscribeMessage(FriendsEvents.ReturnAllData)
    async returnAllData(client: Socket, userId: string) {
        client.emit(FriendsEvents.ReturnAllUsers, await this.friendsService.getAllUsers(userId));
        client.emit(FriendsEvents.ReturnAllFriends, await this.friendsService.getFriendsList(userId));
        client.emit(FriendsEvents.ReturnAllPendingRequests, await this.friendsService.getPendingRequests(userId));
        client.emit(FriendsEvents.ReturnAllSentRequests, await this.friendsService.getSentRequests(userId));
    }

    @SubscribeMessage(FriendsEvents.RequestSent)
    async sendFriendRequest(client: Socket, data: FriendsInfo) {
        await this.friendsService.sendFriendRequest(data.user, data.friend);

        client.emit(FriendsEvents.ReturnAllSentRequests, await this.friendsService.getSentRequests(data.user));
        client.emit(FriendsEvents.ReturnAllUsers, await this.friendsService.getAllUsers(data.user));

        const friendSocketId = this.userSockets.get(data.friend);
        if (friendSocketId) {
            this.server.to(friendSocketId).emit(FriendsEvents.ReturnAllPendingRequests, await this.friendsService.getPendingRequests(data.friend));
            this.server.to(friendSocketId).emit(FriendsEvents.ReturnAllUsers, await this.friendsService.getAllUsers(data.friend));
        }
    }

    @SubscribeMessage(FriendsEvents.RequestAccepted)
    async acceptFriendRequest(client: Socket, data: FriendsInfo) {
        await this.friendsService.acceptFriendRequest(data.user, data.friend);

        client.emit(FriendsEvents.ReturnAllFriends, await this.friendsService.getFriendsList(data.user));
        client.emit(FriendsEvents.ReturnAllPendingRequests, await this.friendsService.getPendingRequests(data.user));
        client.emit(FriendsEvents.ReturnAllUsers, await this.friendsService.getAllUsers(data.user));

        const friendSocketId = this.userSockets.get(data.friend);
        if (friendSocketId) {
            this.server.to(friendSocketId).emit(FriendsEvents.ReturnAllFriends, await this.friendsService.getFriendsList(data.friend));
            this.server.to(friendSocketId).emit(FriendsEvents.ReturnAllSentRequests, await this.friendsService.getSentRequests(data.friend));
            this.server.to(friendSocketId).emit(FriendsEvents.ReturnAllUsers, await this.friendsService.getAllUsers(data.friend));
        }
    }

    @SubscribeMessage(FriendsEvents.RequestRejected)
    async rejectFriendRequest(client: Socket, data: FriendsInfo) {
        await this.friendsService.rejectFriendRequest(data.user, data.friend);

        client.emit(FriendsEvents.ReturnAllPendingRequests, await this.friendsService.getPendingRequests(data.user));
        client.emit(FriendsEvents.ReturnAllUsers, await this.friendsService.getAllUsers(data.user));

        const friendSocketId = this.userSockets.get(data.friend);
        if (friendSocketId) {
            this.server.to(friendSocketId).emit(FriendsEvents.ReturnAllSentRequests, await this.friendsService.getSentRequests(data.friend));
            this.server.to(friendSocketId).emit(FriendsEvents.ReturnAllUsers, await this.friendsService.getAllUsers(data.friend));
        }
    }

    @SubscribeMessage(FriendsEvents.RequestCanceled)
    async cancelRequest(client: Socket, data: FriendsInfo) {
        await this.friendsService.cancelRequest(data.user, data.friend);

        client.emit(FriendsEvents.ReturnAllSentRequests, await this.friendsService.getSentRequests(data.user));
        client.emit(FriendsEvents.ReturnAllUsers, await this.friendsService.getAllUsers(data.user));

        const friendSocketId = this.userSockets.get(data.friend);
        if (friendSocketId) {
            this.server.to(friendSocketId).emit(FriendsEvents.ReturnAllPendingRequests, await this.friendsService.getPendingRequests(data.friend));
            this.server.to(friendSocketId).emit(FriendsEvents.ReturnAllUsers, await this.friendsService.getAllUsers(data.friend));
        }
    }

    @SubscribeMessage(FriendsEvents.FriendRemoved)
    async removeFriend(client: Socket, data: FriendsInfo) {
        await this.friendsService.removeFriend(data.user, data.friend);

        client.emit(FriendsEvents.ReturnAllFriends, await this.friendsService.getFriendsList(data.user));
        client.emit(FriendsEvents.ReturnAllUsers, await this.friendsService.getAllUsers(data.user));

        const friendSocketId = this.userSockets.get(data.friend);
        if (friendSocketId) {
            this.server.to(friendSocketId).emit(FriendsEvents.ReturnAllFriends, await this.friendsService.getFriendsList(data.friend));
            this.server.to(friendSocketId).emit(FriendsEvents.ReturnAllUsers, await this.friendsService.getAllUsers(data.friend));
        }
    }

    broadcastEvent(event: FriendsEvents, data: FriendsInfo): void {
        this.server.emit(event, data);
    }

    @SubscribeMessage(FriendsEvents.Connect)
    handleConnect(client: Socket, userId: string): void {
        this.userSockets.set(userId, client.id);
        const historyAuthItem: HistoryAuthItem = {
            id: uuidv4(),
            isLogin: true,
            date: new Date(),
        };
        this.historyService.addAuthHistoryItem(userId, historyAuthItem);
    }

    handleDisconnect(client: Socket): void {
        const userId = Array.from(this.userSockets.entries()).find(([_, socketId]) => socketId === client.id)?.[0];
        const historyAuthItem: HistoryAuthItem = {
            id: uuidv4(),
            isLogin: false,
            date: new Date(),
        };
        this.historyService.addAuthHistoryItem(userId, historyAuthItem);
        if (userId) {
            this.userSockets.delete(userId);
        }
    }
}
