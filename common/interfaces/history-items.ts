export interface UserHistory {
    auth: HistoryAuthItem[];
    match: HistoryMatchItem[];
    stats: MatchStats;
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
