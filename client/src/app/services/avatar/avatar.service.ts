import { Injectable } from '@angular/core';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { get, ref, set } from 'firebase/database';

@Injectable({
    providedIn: 'root',
})
export class AvatarService {
    constructor(private authService: AuthenticationService) {}

    async getPurchasedAvatars(): Promise<string[]> {
        const userId = this.authService.userId;
        if (!userId) return [];

        const avatarsRef = ref(this.authService.database, `users/${userId}/purchasedAvatars`);
        const snapshot = await get(avatarsRef);
        return snapshot.exists() ? Object.keys(snapshot.val()) : [];
    }

    async purchaseAvatar(avatarId: string): Promise<void> {
        const userId = this.authService.userId;
        if (!userId) return;

        const avatarRef = ref(this.authService.database, `users/${userId}/purchasedAvatars/${avatarId}`);
        await set(avatarRef, true);
    }
}
