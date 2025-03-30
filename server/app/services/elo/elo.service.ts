import { FirebaseRepositoryService } from '@app/modules/firebase/firebase-repository/firebase-repository.service';
import { MatchRoomService } from '@app/services/match-room/match-room.service';
import { Injectable } from '@nestjs/common';
import { Player as EloPlayer } from 'elo-mmr';
import { EloMmr, Standing } from 'elo-mmr/dist/lib/elo_mmr';
import { Rating } from 'elo-mmr/dist/lib/rating';

@Injectable()
export class EloService {
    private readonly eloMmr: EloMmr;

    constructor(
        private readonly firebaseService: FirebaseRepositoryService,
        private readonly matchRoomService: MatchRoomService,
    ) {
        this.eloMmr = new EloMmr();
    }

    async getPlayerElo(playerId: string): Promise<Rating> {
        const snapshot = await this.firebaseService.database.ref(`users/${playerId}/elo`).once('value');
        if (!snapshot.exists()) {
            return new Rating(1500, 350);
        }
        const data = snapshot.val();
        return new Rating(data.mu, data.sigma);
    }

    async updatePlayerElo(playerId: string, newRating: Rating): Promise<void> {
        await this.firebaseService.database.ref(`users/${playerId}/elo`).set({
            mu: newRating.mu,
            sigma: newRating.sig,
        });
    }

    async updateEloForMatch(roomCode: string): Promise<void> {
        const room = this.matchRoomService.getRoom(roomCode);

        const allPlayers = room.players.map((player) => ({
            ...player,
            score: player.state === 'exit' ? 0 : player.score,
        }));

        allPlayers.sort((a, b) => b.score - a.score);

        const eloPlayers: EloPlayer[] = await Promise.all(
            allPlayers.map(async (player) => {
                const rating = await this.getPlayerElo(player.id);
                return new EloPlayer(rating);
            }),
        );

        const standings: Standing[] = [];
        let rank = 0;

        for (let i = 0; i < allPlayers.length; i++) {
            if (i > 0 && allPlayers[i].score === allPlayers[i - 1].score) {
                rank = standings[standings.length - 1][1];
            } else {
                rank = i;
            }
            standings.push([eloPlayers[i], rank, rank]);
        }

        const contestTime = new Date();
        this.eloMmr.roundUpdate(standings, contestTime);

        for (let i = 0; i < allPlayers.length; i++) {
            const player = allPlayers[i];
            const eloPlayer = eloPlayers[i];
            await this.updatePlayerElo(player.id, eloPlayer.approximatePosterior);
        }
    }
}
