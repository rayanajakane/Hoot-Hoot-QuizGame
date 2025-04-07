import { DonationRecord } from '@app/constants/donation-record';
import { DONATION_LIMIT_EXCEEDED, INVALID_AMOUNT, LOW_BALANCE, NOT_FRIENDS, ZERO_AMOUNT } from '@app/constants/money-errors';
import { FirebaseRepositoryService } from '@app/modules/firebase/firebase-repository/firebase-repository.service';
import { MatchRoomService } from '@app/services/match-room/match-room.service';
import { MAX_REWARD, MIN_REWARD } from '@common/constants/match-constants';
import { Injectable } from '@nestjs/common';
import { Database } from 'firebase-admin/lib/database/database';
import { FriendsService } from '../friends/friends.service';
@Injectable()
export class MoneyService {
    private database: Database;
    private readonly DAILY_DONATION_LIMIT = 500;

    constructor(
        private readonly firebaseService: FirebaseRepositoryService,
        private matchRoomService: MatchRoomService,
        private friendService: FriendsService,
    ) {
        this.database = this.firebaseService.database;
    }

    async getCurrentBalance(uid: string): Promise<number> {
        const snapshot = await this.database.ref(`users/${uid}/balance`).once('value');
        return snapshot.exists() ? snapshot.val() : 0;
    }

    async updateBalance(uid: string, amount: number): Promise<number> {
        const balance = await this.getCurrentBalance(uid);
        const newBalance = balance + amount;
        await this.database.ref(`users/${uid}/balance`).set(newBalance);
        return newBalance;
    }

    private async getAndClearDonationsToday(uid: string): Promise<number> {
        const donationHistoryRef = this.database.ref(`users/${uid}/donation_history`);
        const snapshot = await donationHistoryRef.once('value');
        let donationsToday = 0;
        const todayStart = new Date();
        todayStart.setHours(0, 0, 0, 0);
        const todayTimestamp = todayStart.getTime();

        if (snapshot.exists()) {
            const donationHistory = snapshot.val();
            const records = Object.values(donationHistory) as DonationRecord[];
            for (const record of records) {
                if (record.timestamp >= todayTimestamp) {
                    donationsToday += record.amount;
                }
            }
            if (donationsToday === 0) {
                await donationHistoryRef.remove();
            }
        }
        return donationsToday;
    }

    async donateMoney(fromUid: string, toUid: string, amount: number): Promise<boolean> {
        const fromBalance = await this.getCurrentBalance(fromUid);
        if (fromBalance < amount) return false;

        const donationsToday = await this.getAndClearDonationsToday(fromUid);
        if (donationsToday + amount > this.DAILY_DONATION_LIMIT) return false;

        await this.updateBalance(fromUid, -amount);
        await this.updateBalance(toUid, amount);

        await this.database.ref(`users/${fromUid}/donation_history`).push().set({
            amount,
            timestamp: Date.now(),
        });

        return true;
    }

    async getMoneyError(uid: string, amount: number, isDonation: boolean = false): Promise<string[]> {
        const errors: string[] = [];
        const balance = await this.getCurrentBalance(uid);

        if (balance < amount) {
            errors.push(LOW_BALANCE);
        }
        if (amount < 0) {
            errors.push(INVALID_AMOUNT);
        }

        if (isDonation) {
            if (amount == 0) {
                errors.push(ZERO_AMOUNT);
            }

            const donationsToday = await this.getAndClearDonationsToday(uid);
            if (donationsToday + amount > this.DAILY_DONATION_LIMIT) {
                errors.push(DONATION_LIMIT_EXCEEDED);
            }

            const friends = await this.friendService.getFriendsList(uid);
            const isFriend = friends.some((friend) => friend.id === uid);
            if (!isFriend) {
                errors.push(NOT_FRIENDS);
            }
        }

        return errors;
    }

    async rewardPlayers(roomCode: string): Promise<void> {
        const room = this.matchRoomService.getRoom(roomCode);
        const activePlayers = room.players.filter((player) => player.state !== 'exit');
        const partyConfig = room.partyConfig;
        const winners = this.matchRoomService.declareWinner(roomCode);
        const isEntryFeeRequired = partyConfig.isEntryFeeRequired;

        let minReward = MIN_REWARD;
        let maxReward = MAX_REWARD;

        if (isEntryFeeRequired) {
            const totalReward = activePlayers.length * partyConfig.entryFeeAmount;
            maxReward = Math.round(totalReward * (2 / 3));
            const nonWinnersCount = activePlayers.length - winners.length;
            if (nonWinnersCount > 0) {
                minReward = Math.round((totalReward * (1 / 3)) / nonWinnersCount);
            } else {
                minReward = maxReward;
            }
        }

        for (const player of activePlayers) {
            const reward = winners.includes(player) ? maxReward : minReward;
            await this.updateBalance(player.id, reward);
        }
    }
}
