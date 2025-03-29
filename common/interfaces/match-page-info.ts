import { PartyConfig } from './party-config';

export interface MatchPageInfo {
    code: string;
    isLocked: boolean;
    isPlaying: boolean;
    gameTitle: string;
    nPlayers: number;
    partyConfig: PartyConfig;
}
