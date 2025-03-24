import { Injectable } from '@angular/core';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { Database, get, getDatabase, ref, set } from 'firebase/database';

@Injectable({
    providedIn: 'root',
})
export class AvatarService {
    private database: Database;

    constructor(private authService: AuthenticationService) {
        this.database = getDatabase();
    }

    async getPurchasedAvatars(): Promise<string[]> {
        const userId = this.authService.userId;
        if (!userId) return [];

        const avatarsRef = ref(this.database, `users/${userId}/purchasedAvatars`);
        const snapshot = await get(avatarsRef);
        return snapshot.exists() ? Object.keys(snapshot.val()) : [];
    }

    async purchaseAvatar(avatarId: string): Promise<void> {
        const userId = this.authService.userId;
        if (!userId) return;

        const avatarRef = ref(this.database, `users/${userId}/purchasedAvatars/${avatarId}`);
        await set(avatarRef, true);
    }
}
