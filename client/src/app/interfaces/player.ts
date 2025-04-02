export interface Player {
    username: string;
    id: string;
    photoUrl: string;
    score: number;
    bonusCount: number;
    isPlaying: boolean;
    isChatActive: boolean;
    state: string;
}
