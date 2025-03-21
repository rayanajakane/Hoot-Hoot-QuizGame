import { DONATION_LIMIT_EXCEEDED, LOW_BALANCE } from '@app/constants/money-errors';
import { FirebaseAuthService } from '@app/modules/firebase/firebase-auth/firebase-auth.service';
import { FirebaseRepositoryService } from '@app/modules/firebase/firebase-repository/firebase-repository.service';
import { Injectable } from '@nestjs/common';
import { Database } from 'firebase-admin/lib/database/database';
@Injectable()
export class MoneyService {
    private database: Database;

    constructor(
        private readonly firebaseService: FirebaseRepositoryService,
        private readonly firebaseAuthService: FirebaseAuthService,
    ) {
        this.database = this.firebaseService.database;
    }

    async getCurrentBalance(uid: string): Promise<number> {
        const snapshot = await this.database.ref(`users/${uid}/balance`).once('value');
        if (!snapshot.exists()) return 0;
        return snapshot.val();
    }

    async updateBalance(uid: string, amount: number): Promise<number> {
        const balance = await this.getCurrentBalance(uid);
        const newBalance = balance + amount;
        if (newBalance < 0) return balance; // error
        await this.database.ref(`users/${uid}/balance`).set(newBalance);
        return newBalance;
    }

    async donateMoney(fromUid: string, toUid: string, amount: number): Promise<boolean> {
        const fromBalance = await this.getCurrentBalance(fromUid);
        if (fromBalance < amount) return false; // error
        await this.updateBalance(fromUid, -amount);
        await this.updateBalance(toUid, amount);

        return true;
    }

    async getMoneyError(uid: string, amount: number, isDonation: boolean = false): Promise<string> {
        let errors = '';
        const balance = await this.getCurrentBalance(uid);
        const donationLimit = Number.MAX_SAFE_INTEGER;
        const amountDonated = 0; // get from  and add to it the amount that the user wants to donate
        const errorConditions: Map<string, boolean> = new Map([
            [LOW_BALANCE, balance < amount],
            [DONATION_LIMIT_EXCEEDED, isDonation && amountDonated > donationLimit], // adjust the conditions
        ]);

        errorConditions.forEach((hasError: boolean, message: string) => {
            if (hasError) errors += message;
        });
        return errors;
    }
}
