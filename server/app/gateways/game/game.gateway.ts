import { GameService } from '@app/services/game/game.service';
import { GameEvents } from '@common/events/game.events';
import { UserIdName } from '@common/interfaces/user-id-name';
import { SubscribeMessage, WebSocketGateway, WebSocketServer } from '@nestjs/websockets';
import { Server, Socket } from 'socket.io';
@WebSocketGateway()
export class GameGateway {
    @WebSocketServer() private server: Server;
    constructor(private gameService: GameService) {}

    @SubscribeMessage(GameEvents.UpdateAuthorName)
    updateAuthorName(client: Socket, data: UserIdName) {
        this.gameService.updateAuthorNames(data.id, data.name);
    }
}
