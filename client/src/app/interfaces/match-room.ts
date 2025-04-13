import { Game } from '@app/interfaces/game';
import { Player } from '@app/interfaces/player';
import { Message } from '@common/interfaces/message';

export interface MatchRoom {
    code: string;
    isLocked: boolean;
    isPlaying: boolean;
    game: Game;
    bannedIds: string[];
    players: Player[];
    messages: Message[];
}
