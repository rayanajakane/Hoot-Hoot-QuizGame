import { FirebaseAuthService } from '@app/modules/firebase/firebase-auth/firebase-auth.service';
import { MoneyService } from '@app/services/money/money.service';
import { MoneyEvents } from '@common/events/money.events';
import { PurchaseInfo } from '@common/interfaces/shop-item';
import { TransferInfo } from '@common/interfaces/transfer-info';
import { SubscribeMessage, WebSocketGateway, WebSocketServer } from '@nestjs/websockets';
import { Server, Socket } from 'socket.io';

@WebSocketGateway({ cors: true })
export class MoneyGateway {
    @WebSocketServer() private server: Server;
    private userSockets: Map<string, string> = new Map();
    constructor(
        private moneyService: MoneyService,
        private readonly firebaseAuthService: FirebaseAuthService,
    ) {}

    @SubscribeMessage(MoneyEvents.GetBalance)
    async getBalance(client: Socket, userId: string) {
        this.userSockets.set(userId, client.id);
        client.emit(MoneyEvents.ReturnBalance, await this.moneyService.getCurrentBalance(userId));
    }

    @SubscribeMessage(MoneyEvents.DonateMoney)
    async donateMoney(client: Socket, data: TransferInfo) {
        const roundedAmount = Math.round(data.amount * 100) / 100;
        data.amount = Number.isInteger(roundedAmount) ? Math.trunc(roundedAmount) : roundedAmount;
        console.log('Donating money:', data);
        const moneyErrors = await this.moneyService.getMoneyError(data.user, data.amount, true);
        if (moneyErrors) {
            this.sendError(client.id, moneyErrors);
            return;
        }

        const success = await this.moneyService.donateMoney(data.user, data.friend, data.amount);
        if (!success) return;
        const friendUsername = (await this.firebaseAuthService.getUserById(data.friend)).displayName;
        const userUsername = (await this.firebaseAuthService.getUserById(data.user)).displayName;
        client.emit(MoneyEvents.DonationGiven, {
            to: friendUsername,
            amount: data.amount,
            newBalance: await this.moneyService.getCurrentBalance(data.user),
        });

        const friendSocketId = this.userSockets.get(data.friend);
        if (friendSocketId) {
            this.server.to(friendSocketId).emit(MoneyEvents.DonationReceived, {
                from: userUsername,
                amount: data.amount,
                newBalance: await this.moneyService.getCurrentBalance(data.friend),
            });
        }
    }

    @SubscribeMessage(MoneyEvents.BuyAvatar)
    async buyAvatar(client: Socket, data: PurchaseInfo) {
        if (data.item.owned) {
            this.sendError(client.id, 'Avatar already owned');
            return;
        }
        const moneyErrors = await this.moneyService.getMoneyError(data.user, data.item.price, false);
        if (moneyErrors) {
            this.sendError(client.id, moneyErrors);
            return;
        }

        await this.moneyService.updateBalance(data.user, -data.item.price);
        client.emit(MoneyEvents.AvatarBought, data.item);
        client.emit(MoneyEvents.ReturnBalance, await this.moneyService.getCurrentBalance(data.user));
    }

    @SubscribeMessage(MoneyEvents.BuyTheme)
    async buyTheme(client: Socket, data: PurchaseInfo) {
        console.log('Buying theme:', data);
        if (data.item.owned) {
            this.sendError(client.id, 'Theme already owned');
            return;
        }
        const moneyErrors = await this.moneyService.getMoneyError(data.user, data.item.price, false);
        if (moneyErrors) {
            this.sendError(client.id, moneyErrors);
            return;
        }
        await this.moneyService.updateBalance(data.user, -data.item.price);
        client.emit(MoneyEvents.ThemeBought, data.item);
        client.emit(MoneyEvents.ReturnBalance, await this.moneyService.getCurrentBalance(data.user));
    }

    @SubscribeMessage(MoneyEvents.BuyWallpaper)
    async buyWallpaper(client: Socket, data: PurchaseInfo) {
        console.log('Buying wallpaper:', data);
        if (data.item.owned) {
            this.sendError(client.id, 'Wallpaper already owned');
            return;
        }
        const moneyErrors = await this.moneyService.getMoneyError(data.user, data.item.price, false);
        if (moneyErrors) {
            this.sendError(client.id, moneyErrors);
            return;
        }
        await this.moneyService.updateBalance(data.user, -data.item.price);
        client.emit(MoneyEvents.WallpaperBought, data.item);
        client.emit(MoneyEvents.ReturnBalance, await this.moneyService.getCurrentBalance(data.user));
    }

    handleDisconnect(client: Socket) {
        const userId = Array.from(this.userSockets.entries()).find(([, socketId]) => socketId === client.id)?.[0];
        if (userId) this.userSockets.delete(userId);
    }

    sendError(socketId: string, error: string) {
        console.log('Sending error:', error);
        this.server.to(socketId).emit(MoneyEvents.Error, error);
    }
}
