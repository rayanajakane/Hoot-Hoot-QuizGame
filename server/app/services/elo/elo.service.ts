import { FirebaseAuthService } from '@app/modules/firebase/firebase-auth/firebase-auth.service';
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
        private readonly firebaseAuthService: FirebaseAuthService,
    ) {
        this.eloMmr = new EloMmr();
    }

    async getAllUsersWithElo(): Promise<{ username: string; rating: number }[]> {
        try {
            const listUsersResult = await this.firebaseAuthService.getUsers();
            const allUsers = listUsersResult.users.map((user) => ({
                userId: user.uid,
                username: user.displayName || 'Unknown User',
            }));
            const snapshot = await this.firebaseService.database.ref('users').once('value');
            if (!snapshot.exists()) return [];
            const users = snapshot.val();
            const rankings = allUsers.map((user) => {
                const userData = users[user.userId];
                const elo = userData?.elo?.mu || 0;
                return {
                    username: user.username,
                    rating: elo,
                };
            });

            return rankings;
        } catch (error) {
            console.error('Failed to fetch Elo rankings:', error);
            throw new Error('Unable to fetch Elo rankings.');
        }
    }

    async getPlayerElo(playerId: string): Promise<Rating> {
        try {
            const snapshot = await this.firebaseService.database.ref(`users/${playerId}/elo`).once('value');
            if (!snapshot.exists()) {
                const defaultRating = new Rating(1500, 350);
                await this.updatePlayerElo(playerId, defaultRating);
                return defaultRating;
            }
            const data = snapshot.val();
            console.log('Elo data:', data);
            return new Rating(data.mu, data.sigma);
        } catch (error) {
            console.error(`Failed to get Elo for player ${playerId}:`, error);
            throw new Error('Unable to retrieve player Elo rating.');
        }
    }

    async updatePlayerElo(playerId: string, newRating: Rating): Promise<void> {
        try {
            await this.firebaseService.database.ref(`users/${playerId}/elo`).set({
                mu: newRating.mu,
                sigma: newRating.sig,
            });
        } catch (error) {
            console.error(`Failed to update Elo for player ${playerId}:`, error);
            throw new Error('Unable to update player Elo rating.');
        }
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
