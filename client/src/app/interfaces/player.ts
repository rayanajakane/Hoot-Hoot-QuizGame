export interface Player {
    username: string;
    id: string;
    score: number;
    bonusCount: number;
    isPlaying: boolean;
    isChatActive: boolean;
    state: string;
}
