import { EloService } from '@app/services/elo/elo.service';
import { EloEvents } from '@common/events/elo.events';
import { SubscribeMessage, WebSocketGateway, WebSocketServer } from '@nestjs/websockets';
import { Server, Socket } from 'socket.io';

@WebSocketGateway({ cors: true })
export class EloGateway {
    @WebSocketServer() private server: Server;
    private userSockets: Map<string, string> = new Map();

    constructor(private readonly eloService: EloService) {}

    @SubscribeMessage(EloEvents.GetElo)
    async getElo(client: Socket, userId: string) {
        try {
            this.userSockets.set(userId, client.id);
            const rating = await this.eloService.getPlayerElo(userId);
            client.emit(EloEvents.ReturnElo, {
                mu: rating.mu,
                sigma: rating.sig,
            });
        } catch (error) {
            this.sendError(client.id, 'Failed to retrieve Elo rating.');
        }
    }

    @SubscribeMessage(EloEvents.UpdateEloForMatch)
    async updateEloForMatch(client: Socket, roomCode: string) {
        try {
            await this.eloService.updateEloForMatch(roomCode);
            this.server.emit(EloEvents.EloUpdated, { roomCode });
        } catch (error) {
            this.sendError(client.id, 'Failed to update Elo ratings for the match.');
        }
    }

    handleDisconnect(client: Socket) {
        const userId = Array.from(this.userSockets.entries()).find(([, socketId]) => socketId === client.id)?.[0];
        if (userId) this.userSockets.delete(userId);
    }

    private sendError(socketId: string, error: string) {
        this.server.to(socketId).emit(EloEvents.Error, error);
    }
}
