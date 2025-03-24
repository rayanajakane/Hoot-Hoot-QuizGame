import { FirebaseAuthService } from '@app/modules/firebase/firebase-auth/firebase-auth.service';
import { FirebaseRepositoryService } from '@app/modules/firebase/firebase-repository/firebase-repository.service';
import { HistoryAuthItem, HistoryMatchItem } from '@common/interfaces/history-items';
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

    async getAuthHistory(userId: string) {
        const snapshot = await this.database.ref(`users/${userId}/auth_history`).once('value');
        if (!snapshot.exists()) return [];
        console.log(snapshot.val()); // TODO: Convert date (LocaleDateString --> Date obj)
        // return snapshot.val();
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
        console.log(snapshot.val()); // TODO: Convert date
        // return snapshot.val();
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
