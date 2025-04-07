export interface UserHistoryInfo {
    auth: HistoryAuthItem[];
    match: HistoryMatchItem[];
    stats: MatchStats;
    intensityGrid: IntensityGridItem[];
}

export interface IntensityGridItem {
    date: Date;
    intensity: number;
    nMatches: number;
}

export interface HistoryAuthItem {
    id: string;
    isLogin: boolean;
    date: Date;
}

export interface HistoryMatchItem {
    id: string;
    start: Date;
    end: Date;
    hasWon: boolean;
    hasGivenUp: boolean;
    nGoodAnswers: number;
    nTotalQuestions: number;
}

export interface MatchStats {
    nMatchesPlayed: number;
    nMatchesWon: number;
    averageGoodAnswersPercentage: number;
    averageTime: number;
}
