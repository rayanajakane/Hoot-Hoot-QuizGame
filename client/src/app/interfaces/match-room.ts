import { Game } from './game';
import { Message } from './message';
import { Player } from './player';

export interface MatchRoom {
    code: string;
    isLocked: boolean;
    isPlaying: boolean;
    game: Game;
    bannedIds string[];
    players: Player[];
    messages: Message[];
}
