import { MoneyService } from '@app/services/money/money.service';
import { MoneyEvents } from '@common/events/money.events';
import { TransferInfo } from '@common/interfaces/transfer-info';
import { SubscribeMessage, WebSocketGateway, WebSocketServer } from '@nestjs/websockets';
import { Server, Socket } from 'socket.io';

@WebSocketGateway()
export class MoneyGateway {
    @WebSocketServer() private server: Server;
    private userSockets: Map<string, string> = new Map(); // userId -> socketId
    constructor(private moneyService: MoneyService) {}

    @SubscribeMessage(MoneyEvents.GetBalance)
    async getBalance(client: Socket, userId: string) {
        this.userSockets.set(userId, client.id);
        client.emit('balance', await this.moneyService.getCurrentBalance(userId));
    }

    // *** Temp solution to get free money for testing, remove when done ***
    @SubscribeMessage(MoneyEvents.AddMoney)
    async addMoney(client: Socket, data: { userId: string; amount: number }) {
        const newBalance = await this.moneyService.updateBalance(data.userId, data.amount);
        client.emit('balance', newBalance);
    }

    @SubscribeMessage(MoneyEvents.DonateMoney)
    async donateMoney(client: Socket, data: TransferInfo) {
        const moneyErrors = await this.moneyService.getMoneyError(data.user, data.amount, true);
        if (moneyErrors) {
            this.sendError(client.id, moneyErrors);
            return;
        }

        const success = await this.moneyService.donateMoney(data.user, data.friend, data.amount);
        client.emit('transferResult', success);

        if (!success) return;

        client.emit(MoneyEvents.DonationGiven, {
            to: data.friend,
            amount: data.amount,
            newBalance: await this.moneyService.getCurrentBalance(data.user),
        });

        // Notify the friend
        const friendSocketId = this.userSockets.get(data.friend);
        if (friendSocketId) {
            this.server.to(friendSocketId).emit(MoneyEvents.DonationReceived, {
                from: data.user,
                amount: data.amount,
                newBalance: await this.moneyService.getCurrentBalance(data.friend),
            });
        }
    }

    handleDisconnect(client: Socket) {
        const userId = Array.from(this.userSockets.entries()).find(([, socketId]) => socketId === client.id)?.[0];
        if (userId) this.userSockets.delete(userId);
    }

    sendError(socketId: string, error: string) {
        this.server.to(socketId).emit(MoneyEvents.Error, error);
    }
}
