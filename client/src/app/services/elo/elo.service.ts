import { Injectable } from '@angular/core';
import { NotificationService } from '@app/services/notification/notification.service';
import { SocketHandlerService } from '@app/services/socket-handler/socket-handler.service';
import { EloEvents } from '@common/events/elo.events';

@Injectable({
    providedIn: 'root',
})
export class EloService {
    currentRating: number;

    constructor(
        private readonly socketHandler: SocketHandlerService,
        private notificationService: NotificationService,
    ) {}

    listenForEloEvents() {
        this.onReturnElo();
        this.onRatingChange();
        this.handleError();
    }
    stopListeningForEloEvents() {
        this.socketHandler.socket.removeListener(EloEvents.ReturnElo);
        this.socketHandler.socket.removeListener(EloEvents.EloUpdated);
        this.socketHandler.socket.removeListener(EloEvents.Error);
    }

    getElo(userId: string) {
        this.socketHandler.send(EloEvents.GetElo, userId);
    }

    updateEloForMatch(roomCode: string) {
        this.socketHandler.send(EloEvents.UpdateEloForMatch, roomCode);
    }

    onReturnElo() {
        this.socketHandler.on(EloEvents.ReturnElo, (data: number) => {
            this.currentRating = data;
            return data;
        });
    }

    onRatingChange() {
        this.socketHandler.on(EloEvents.EloUpdated, (data: number) => {
            this.currentRating = data;
        });
    }

    handleError() {
        this.socketHandler.on(EloEvents.Error, (error: string) => {
            this.notificationService.displayErrorMessage(error);
        });
    }
}
