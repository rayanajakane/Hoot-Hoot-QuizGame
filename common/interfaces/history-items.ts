export interface UserHistory {
    auth: HistoryAuthItem[];
    match: HistoryMatchItem[];
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
