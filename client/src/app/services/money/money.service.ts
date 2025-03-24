import { Injectable } from '@angular/core';
import { NotificationService } from '@app/services/notification/notification.service';
import { SocketHandlerService } from '@app/services/socket-handler/socket-handler.service';
import { MoneyEvents } from '@common/events/money.events';

@Injectable({
    providedIn: 'root',
})
export class MoneyService {
    currentBalance: number;

    constructor(
        private readonly socketHandler: SocketHandlerService,
        private notificationService: NotificationService,
    ) {}

    listenForMoneyEvents() {
        this.onReturnBalance();
        this.onDonationGiven();
        this.onDonationReceived();
        this.handleError();
    }

    stopListeningForMoneyEvents() {
        this.socketHandler.socket.removeListener(MoneyEvents.ReturnBalance);
        this.socketHandler.socket.removeListener(MoneyEvents.DonationGiven);
        this.socketHandler.socket.removeListener(MoneyEvents.DonationReceived);
        this.socketHandler.socket.removeListener(MoneyEvents.Error);
    }

    getCurrentBalance(userId: string) {
        this.socketHandler.send(MoneyEvents.GetBalance, userId);
    }

    onReturnBalance() {
        this.socketHandler.on(MoneyEvents.ReturnBalance, (data: number) => {
            console.log('Current balance:', data);
            this.currentBalance = data;
            return data;
        });
    }

    donateMoney(userId: string, friendId: string, amount: number) {
        this.socketHandler.send(MoneyEvents.DonateMoney, {
            user: userId,
            friend: friendId,
            amount,
        });
    }

    onDonationGiven() {
        this.socketHandler.on(MoneyEvents.DonationGiven, (data: { to: string; amount: number; newBalance: number }) => {
            this.notificationService.displaySuccessMessage(`You have donated ${data.amount} to ${data.to}`);
            this.currentBalance = data.newBalance;
        });
    }

    onDonationReceived() {
        this.socketHandler.on(MoneyEvents.DonationReceived, (data: { from: string; amount: number; newBalance: number }) => {
            this.notificationService.displaySuccessMessage(`${data.from} has donated ${data.amount} to you`);
            this.currentBalance = data.newBalance;
        });
    }

    handleError() {
        this.socketHandler.on(MoneyEvents.Error, (error: string) => {
            this.notificationService.displayErrorMessage(error);
        });
    }
}
