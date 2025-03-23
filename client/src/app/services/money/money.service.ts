import { Injectable } from '@angular/core';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { NotificationService } from '@app/services/notification/notification.service';
import { SocketHandlerService } from '@app/services/socket-handler/socket-handler.service';
import { MoneyEvents } from '@common/events/money.events';

@Injectable({
    providedIn: 'root',
})
export class MoneyService {
    currentBalance: number;

    constructor(
        private readonly authService: AuthenticationService,
        private readonly socketHandler: SocketHandlerService,
        private notificationService: NotificationService,
    ) {}

    listenForMoneyEvents() {
        this.onReturnBalance();
        this.onDonationGiven();
        this.onDonationReceived();
        this.handleError();
    }

    getCurrentBalance() {
        this.socketHandler.send(MoneyEvents.GetBalance, this.authService.userId);
    }

    onReturnBalance() {
        this.socketHandler.on(MoneyEvents.ReturnBalance, (data: number) => {
            this.currentBalance = data;
            return data;
        });
    }

    donateMoney(friendId: string, amount: number) {
        console.log('Donating money', friendId, amount);
        this.socketHandler.send(MoneyEvents.DonateMoney, {
            user: this.authService.userId,
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
