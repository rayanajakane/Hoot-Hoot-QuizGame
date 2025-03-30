import { Injectable } from '@angular/core';
import { Player } from '@app/interfaces/player';
import { NotificationService } from '@app/services/notification/notification.service';
import { SocketHandlerService } from '@app/services/socket-handler/socket-handler.service';
import { EloEvents } from '@common/events/elo.events';

@Injectable({
    providedIn: 'root',
})
export class EloService {
    currentRating: number;
    rankings: { username: string; rating: number }[];

    constructor(
        private readonly socketHandler: SocketHandlerService,
        private notificationService: NotificationService,
    ) {}

    listenForEloEvents() {
        // this.onRatingChange();
        this.onReturnRankings();
    }
    stopListeningForEloEvents() {
        // this.socketHandler.socket.removeListener(EloEvents.EloUpdated);
        // this.socketHandler.socket.removeListener(EloEvents.Error);
        this.socketHandler.socket.removeListener(EloEvents.ReturnRankings);
    }

    onReturnRankings() {
        this.socketHandler.on(EloEvents.ReturnRankings, (data: { username: string; rating: number }[]) => {
            this.rankings = data
                .sort((a, b) => b.rating - a.rating)
                .map((ranking) => ({
                    ...ranking,
                    rating: Math.round(ranking.rating),
                }));
        });
    }

    getRankings() {
        this.socketHandler.send(EloEvents.GetRankings);
    }

    getElo(userId: string) {
        this.socketHandler.send(EloEvents.GetElo, userId);
    }

    updateEloForMatch(roomCode: string) {
        this.socketHandler.send(EloEvents.UpdateEloForMatch, roomCode);
    }

    onReturnElo() {
        this.socketHandler.on(EloEvents.ReturnElo, (data: any) => {
            this.currentRating = Math.round(data.mu);
        });
    }

}
