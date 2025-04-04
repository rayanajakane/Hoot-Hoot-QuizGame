import { Injectable } from '@angular/core';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { get, ref, set } from 'firebase/database';
import { BehaviorSubject } from 'rxjs';

export enum Wallpaper {
    None = 'none',
    Mario = 'https://mario.nintendo.com/static/fd723b2893d4d2b39ef71bfdb4e3329c/579b4/mario-background.png',
    Luigi = 'https://purepng.com/public/uploads/large/purepng.com-luigimariofictional-charactervideo-gamefranchisenintendodesigner-1701528631379ubgt6.png',
    Sonic = 'https://www.pixelstalk.net/wp-content/uploads/2016/04/Sonic-wallpaper-HD-pictures-images-download.jpg',
    Shadow = 'https://www.pixelstalk.net/wp-content/uploads/2016/08/Shadow-The-Hedgehog-Background-HD-Free.jpg',
}

@Injectable({
    providedIn: 'root',
})
export class WallpaperService {
    private _currentWallpaper = new BehaviorSubject<string>(Wallpaper.None);

    constructor(private authService: AuthenticationService) {
        this.loadCurrentWallpaper();
    }

    get currentWallpaper$() {
        return this._currentWallpaper.asObservable();
    }

    get currentWallpaper() {
        return this._currentWallpaper.value;
    }

    async loadCurrentWallpaper() {
        const userId = this.authService.userId;
        if (!userId) return;

        const wallpaperRef = ref(this.authService.database, `users/${userId}/currentWallpaper`);
        const snapshot = await get(wallpaperRef);

        if (snapshot.exists()) {
            this._currentWallpaper.next(snapshot.val());
        } else {
            this._currentWallpaper.next(Wallpaper.None);
        }
    }

    async setWallpaper(wallpaper: string) {
        const userId = this.authService.userId;
        if (!userId) return;

        const wallpaperRef = ref(this.authService.database, `users/${userId}/currentWallpaper`);
        await set(wallpaperRef, wallpaper);
        this._currentWallpaper.next(wallpaper);
    }

    async getPurchasedWallpapers(): Promise<string[]> {
        const userId = this.authService.userId;
        if (!userId) return [];

        const wallpapersRef = ref(this.authService.database, `users/${userId}/purchasedWallpapers`);
        const snapshot = await get(wallpapersRef);
        return snapshot.exists() ? Object.keys(snapshot.val()) : [];
    }

    async purchaseWallpaper(wallpaperId: string): Promise<void> {
        const userId = this.authService.userId;
        if (!userId) return;

        const wallpaperRef = ref(this.authService.database, `users/${userId}/purchasedWallpapers/${wallpaperId}`);
        await set(wallpaperRef, true);
    }

    onDestroy() {
        this._currentWallpaper.complete();
    }
}
