import { DOCUMENT } from '@angular/common';
import { Inject, Injectable, Renderer2, RendererFactory2 } from '@angular/core';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { DataSnapshot, get, update } from '@firebase/database';
import { AuthError } from '@app/services/authentication/auth-error';

export enum Theme {
    DARK = 'dark-theme',
    LIGHT = 'light-theme',
}

@Injectable({
    providedIn: 'root',
})
export class ThemeService {
    currentTheme: Theme = Theme.DARK;
    private renderer: Renderer2;

    constructor(
        private authService: AuthenticationService,
        rendererFactory: RendererFactory2,
        @Inject(DOCUMENT) private document: Document,
    ) {
        // TODO : Get and set current theme
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

    // Prevents unexpected behavior. Used instead of 'as Theme'
    private toTheme(theme: string): Theme {
        switch (theme) {
            case 'DARK':
                return Theme.DARK;
            case 'LIGHT':
                return Theme.LIGHT;
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
            default:
                return 'LIGHT';
        }
    }
}
