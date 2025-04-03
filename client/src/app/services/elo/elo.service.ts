import { Injectable } from '@angular/core';
import { Player } from '@app/interfaces/player';
import { SocketHandlerService } from '@app/services/socket-handler/socket-handler.service';
import { EloEvents } from '@common/events/elo.events';

@Injectable({
    providedIn: 'root',
})
export class EloService {
    currentRating: number;
    rankings: Player[];

    constructor(private readonly socketHandler: SocketHandlerService) {}

    listenForEloEvents() {
        this.onReturnRankings();
    }
    stopListeningForEloEvents() {
        this.socketHandler.socket.removeListener(EloEvents.ReturnRankings);
    }

    onReturnRankings() {
        this.socketHandler.on(EloEvents.ReturnRankings, (data: { username: string; rating: number; photoUrl: string }[]) => {
            this.rankings = data
                .map((item: any) => ({
                    username: item.name || item.username || 'Unknown',
                    score: Math.round(item.elo || item.rating || 0),
                    photoUrl: item.avatar || item.photoUrl || '',
                    id: '',
                    bonusCount: 0,
                    isPlaying: false,
                    isChatActive: false,
                    state: '',
                }))
                .sort((a, b) => b.score - a.score);
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
