import { Injectable } from '@angular/core';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { get, ref, set } from 'firebase/database';
import { BehaviorSubject } from 'rxjs';

export enum Wallpaper {
    None = 'none',
    White = 'https://images.unsplash.com/photo-1741467355504-d2df0dd3ae5d?q=80&w=1974&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
    Clouds = 'https://images.unsplash.com/photo-1738682081595-7bac257f60cd?q=80&w=1974&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
    Water = 'https://images.unsplash.com/photo-1738869748479-fdcb2b782715?q=80&w=1974&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
    Mountains = 'https://images.unsplash.com/photo-1738597452982-5759da74f68d?q=80&w=1932&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
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
