import { FirebaseAuthService } from '@app/modules/firebase/firebase-auth/firebase-auth.service';
import { FirebaseRepositoryService } from '@app/modules/firebase/firebase-repository/firebase-repository.service';
import { HistoryAuthItem, HistoryMatchItem, UserHistory } from '@common/interfaces/history-items';
import { Injectable } from '@nestjs/common';
import { Database } from 'firebase-admin/lib/database/database';

@Injectable()
export class HistoryService {
    private database: Database;

    constructor(
        private readonly firebaseService: FirebaseRepositoryService,
        private readonly firebaseAuthService: FirebaseAuthService,
    ) {
        this.database = this.firebaseService.database;
    }

    async getHistory(userId: string) {
        const authHistory = await this.getAuthHistory(userId);
        const matchHistory = await this.getMatchHistory(userId);
        const history: UserHistory = {
            auth: authHistory,
            match: matchHistory,
        };
        return history;
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
        console.log(historyAuthItems);
        return historyAuthItems;
    }

    async addAuthHistoryItem(userId: string, historyAuthItem: HistoryAuthItem) {
        if (!userId) return;
        console.log(userId);
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
        console.log(historyMatchItems);
        return historyMatchItems;
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
