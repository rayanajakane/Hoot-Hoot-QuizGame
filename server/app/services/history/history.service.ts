import { FirebaseRepositoryService } from '@app/modules/firebase/firebase-repository/firebase-repository.service';
import { HistoryAuthItem, HistoryMatchItem, IntensityGridItem, UserHistoryInfo } from '@common/interfaces/history-items';
import { Injectable } from '@nestjs/common';
import { Database } from 'firebase-admin/lib/database/database';

@Injectable()
export class HistoryService {
    private database: Database;

    constructor(private readonly firebaseService: FirebaseRepositoryService) {
        this.database = this.firebaseService.database;
    }

    async getHistory(userId: string) {
        const authHistory = await this.getAuthHistory(userId);
        const matchHistory = await this.getMatchHistory(userId);
        let stats;
        let intensityGrid;
        if (!matchHistory || matchHistory.length === 0) {
            stats = { nMatchesPlayed: 0, nMatchesWon: 0, averageGoodAnswersPercentage: 0, averageTime: 0 };
            const year = new Date().getFullYear();
            const isLeapYear = (year % 4 == 0 && year % 100 != 0) || year % 400 == 0;
            const nDays = isLeapYear ? 366 : 365;
            intensityGrid = Array(nDays).fill(0);
        } else {
            stats = this.getMatchStats(matchHistory);
            intensityGrid = this.getIntensityGrid(matchHistory);
        }
        const history: UserHistoryInfo = {
            auth: authHistory,
            match: matchHistory,
            stats,
            intensityGrid,
        };
        return history;
    }

    getIntensityGrid(historyMatchItems: HistoryMatchItem[]) {
        const year = historyMatchItems[historyMatchItems.length - 1].end.getFullYear();
        const yearStart = new Date(year, 0, 0);
        const isLeapYear = (year % 4 == 0 && year % 100 != 0) || year % 400 == 0;
        const nDays = isLeapYear ? 366 : 365;
        const intensityGrid: IntensityGridItem[] = [];
        let nDaysWithMatches = 0;

        for (let i = 1; i <= nDays; i++) {
            intensityGrid.push({
                date: new Date(yearStart.getTime() + i * (1000 * 60 * 60 * 24)),
                intensity: 0,
                nMatches: 0,
            });
        }
        const matchCount: number[] = Array(nDays).fill(0);
        historyMatchItems.forEach((historyMatchItem) => {
            if (historyMatchItem.end.getFullYear() === year) {
                // Reference: https://stackoverflow.com/questions/8619879/javascript-calculate-the-day-of-the-year-1-366
                const date = historyMatchItem.end;
                const index =
                    (Date.UTC(date.getFullYear(), date.getMonth(), date.getDate()) - Date.UTC(date.getFullYear(), 0, 0)) / 24 / 60 / 60 / 1000 - 1;

                matchCount[index]++;
                nDaysWithMatches++;
            }
        });
        const averageMatchCount = matchCount.reduce((a, b) => a + b) / nDaysWithMatches;
        console.log(averageMatchCount);
        const maxMatchCount = Math.max(...matchCount);
        matchCount.forEach((count, index) => {
            intensityGrid[index].nMatches = count;
            if (count === 0) {
                intensityGrid[index].intensity = 0;
            } else if (count <= averageMatchCount) {
                intensityGrid[index].intensity = 1;
            } else if (count > averageMatchCount && count != maxMatchCount) {
                intensityGrid[index].intensity = 2;
            } else if (count === maxMatchCount) {
                intensityGrid[index].intensity = 3;
            }
        });
        return intensityGrid;
    }

    getMatchStats(historyMatchItems: HistoryMatchItem[]) {
        const nMatchesPlayed = historyMatchItems.length;
        const nMatchesWon = historyMatchItems.filter((historyMatchItem) => historyMatchItem.hasWon).length;
        let totalPercentage = 0;
        let totalTime = 0;
        historyMatchItems.forEach((historyMatchItem) => {
            totalPercentage += historyMatchItem.nGoodAnswers / historyMatchItem.nTotalQuestions;
            totalTime += historyMatchItem.end.getTime() - historyMatchItem.start.getTime();
        });
        let averageGoodAnswersPercentage: number;
        if (nMatchesPlayed) {
            averageGoodAnswersPercentage = Math.round((totalPercentage / nMatchesPlayed) * 100);
        } else {
            averageGoodAnswersPercentage = 0;
        }
        const averageTime = Math.round(totalTime / nMatchesPlayed / 1000);
        const stats = {
            nMatchesPlayed,
            nMatchesWon,
            averageGoodAnswersPercentage,
            averageTime,
        };
        return stats;
    }

    async getAuthHistory(userId: string) {
        const snapshot = await this.database.ref(`users/${userId}/auth_history`).once('value');
        if (!snapshot.exists()) return [];
        // TODO: Consider optimizing to avoid calling ref too many times (maybe just parse the current snapshot.val())
        const itemIds = Object.keys(snapshot.val());
        const historyAuthItems: HistoryAuthItem[] = await Promise.all(
            itemIds.map(async (id) => {
                const itemSnapshot = await this.database.ref(`users/${userId}/auth_history/${id}`).once('value');
                const itemData = itemSnapshot.exists() ? itemSnapshot.val() : {};
                return {
                    id,
                    isLogin: itemData.isLogin,
                    date: new Date(itemData.date),
                };
            }),
        );
        historyAuthItems.sort((a: HistoryAuthItem, b: HistoryAuthItem) => {
            return a.date.getTime() - b.date.getTime();
        });
        return historyAuthItems.reverse();
    }

    async addAuthHistoryItem(userId: string, historyAuthItem: HistoryAuthItem) {
        if (!userId) return;
        const snapshot = this.database.ref(`users/${userId}/auth_history/${historyAuthItem.id}`);
        snapshot.set({
            id: historyAuthItem.id,
            isLogin: historyAuthItem.isLogin,
            date: historyAuthItem.date.toUTCString(),
        });
    }

    async getMatchHistory(userId: string) {
        if (!userId) return;
        const snapshot = await this.database.ref(`users/${userId}/match_history`).once('value');
        if (!snapshot.exists()) return [];
        // TODO: Consider optimizing to avoid calling ref too many times (maybe just parse the current snapshot.val())
        const itemIds = Object.keys(snapshot.val());
        const historyMatchItems: HistoryMatchItem[] = await Promise.all(
            itemIds.map(async (id) => {
                const itemSnapshot = await this.database.ref(`users/${userId}/match_history/${id}`).once('value');
                const itemData = itemSnapshot.exists() ? itemSnapshot.val() : {};
                return {
                    id,
                    start: new Date(itemData.start),
                    end: new Date(itemData.end),
                    hasWon: itemData.hasWon,
                    hasGivenUp: itemData.hasGivenUp,
                    nGoodAnswers: itemData.nGoodAnswers,
                    nTotalQuestions: itemData.nTotalQuestions,
                };
            }),
        );
        historyMatchItems.sort((a: HistoryMatchItem, b: HistoryMatchItem) => {
            return a.start.getTime() - b.start.getTime();
        });
        return historyMatchItems.reverse();
    }

    async addMatchHistoryItem(userId: string, historyMatchItem: HistoryMatchItem) {
        const snapshot = this.database.ref(`users/${userId}/match_history/${historyMatchItem.id}`);
        snapshot.set({
            id: historyMatchItem.id,
            start: historyMatchItem.start.toUTCString(),
            end: historyMatchItem.end.toUTCString(),
            hasWon: historyMatchItem.hasWon,
            hasGivenUp: historyMatchItem.hasGivenUp,
            nGoodAnswers: historyMatchItem.nGoodAnswers,
            nTotalQuestions: historyMatchItem.nTotalQuestions,
        });
    }
}
