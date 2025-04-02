import { DOCUMENT } from '@angular/common';
import { Inject, Injectable, Renderer2, RendererFactory2 } from '@angular/core';
import { AuthError } from '@app/services/authentication/auth-error';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { DataSnapshot, get, ref, set, update } from '@firebase/database';

export enum Theme {
    DARK = 'dark-theme',
    LIGHT = 'light-theme',
    BENTEN = 'ben-ten-theme',
    MARIO = 'mario-theme',
    SONIC = 'sonic-theme',
    PIKACHU = 'pikachu-theme',
}

@Injectable({
    providedIn: 'root',
})
export class ThemeService {
    currentTheme: Theme = Theme.DARK;
    private renderer: Renderer2;
    private purchasedThemes: string[] = [];

    constructor(
        private authService: AuthenticationService,
        rendererFactory: RendererFactory2,
        @Inject(DOCUMENT) private document: Document,
    ) {
        this.renderer = rendererFactory.createRenderer(null, null);
    }

    saveThemeToDB(visualTheme: Theme) {
        const user = this.authService.currentUser;
        if (user) {
            const userRef = this.authService.getUserDatabaseRef(user.uid + '/configs');
            update(userRef, { theme: this.themeToString(visualTheme) });
        } else {
            throw new AuthError('UserUndefined', 'user is undefined');
        }
    }

    async getThemeFromDB(): Promise<Theme> {
        const user = this.authService.currentUser;
        if (user) {
            const userRef = this.authService.getUserDatabaseRef(user.uid + '/configs/theme');
            return get(userRef)
                .then((dataShapshot: DataSnapshot) => {
                    if (dataShapshot.exists()) {
                        return this.toTheme(dataShapshot.val());
                    }
                    return Theme.LIGHT;
                })
                .catch((error: unknown) => {
                    // eslint-disable-next-line no-console
                    console.error(error);
                    return Theme.LIGHT;
                });
        } else {
            return Theme.LIGHT;
        }
    }

    initLightTheme() {
        this.renderer.addClass(this.document.body, Theme.LIGHT);
        this.currentTheme = Theme.LIGHT;
    }

    getAvailableThemes(): Theme[] {
        return [Theme.DARK, Theme.LIGHT];
    }

    setTheme(theme: Theme) {
        this.renderer.removeClass(this.document.body, this.currentTheme);
        this.renderer.addClass(this.document.body, theme);
        this.currentTheme = theme;
        this.saveThemeToDB(theme);
    }

    async getPurchasedThemes(): Promise<string[]> {
        const userId = this.authService.userId;
        if (!userId) return [];

        const themesRef = ref(this.authService.database, `users/${userId}/purchasedThemes`);
        const snapshot = await get(themesRef);
        this.purchasedThemes = snapshot.exists() ? Object.keys(snapshot.val()) : [];
        return this.purchasedThemes;
    }

    async purchaseTheme(themeId: string): Promise<void> {
        const userId = this.authService.userId;
        if (!userId) return;

        const themeRef = ref(this.authService.database, `users/${userId}/purchasedThemes/${themeId}`);
        await set(themeRef, true);
        this.purchasedThemes.push(themeId);
    }

    isThemePurchased(theme: Theme): boolean {
        if (theme === Theme.DARK || theme === Theme.LIGHT) return true;

        return this.purchasedThemes.includes(theme.toString());
    }

    // Prevents unexpected behavior. Used instead of 'as Theme'
    private toTheme(theme: string): Theme {
        switch (theme) {
            case 'DARK':
                return Theme.DARK;
            case 'LIGHT':
                return Theme.LIGHT;
            case 'BENTEN':
                return Theme.BENTEN;
            case 'MARIO':
                return Theme.MARIO;
            case 'SONIC':
                return Theme.SONIC;
            case 'PIKACHU':
                return Theme.PIKACHU;
            default:
                return Theme.LIGHT;
        }
    }

    // On android studio, themes are DARK, LIGHT
    private themeToString(theme: string) {
        switch (theme) {
            case Theme.DARK:
                return 'DARK';
            case Theme.LIGHT:
                return 'LIGHT';
            case Theme.BENTEN:
                return 'BENTEN';
            case Theme.MARIO:
                return 'MARIO';
            case Theme.SONIC:
                return 'SONIC';
            case Theme.PIKACHU:
                return 'PIKACHU';
            default:
                return 'LIGHT';
        }
    }
}
