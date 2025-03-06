import { DOCUMENT } from '@angular/common';
import { Inject, Injectable, Renderer2, RendererFactory2 } from '@angular/core';
import { AuthError } from '@app/services/authentication/auth-error';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { DataSnapshot, get, update } from '@firebase/database';

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
        private authenticationService: AuthenticationService,
        rendererFactory: RendererFactory2,
        @Inject(DOCUMENT) private document: Document,
    ) {
        //TODO : get current theme
        this.renderer = rendererFactory.createRenderer(null, null);
    }

    saveThemeToDB(visualTheme: Theme) {
        const user = this.authenticationService.currentUser;
        if (user) {
            const userRef = this.authenticationService.getUserDatabaseRef(user.uid + '/configs');
            update(userRef, { theme: visualTheme });
        } else {
            throw new AuthError('UserUndefined', 'user is undefined');
        }
    }

    async getThemeFromDB(): Promise<Theme> {
        const user = this.authenticationService.currentUser;
        if (user) {
            const userRef = this.authenticationService.getUserDatabaseRef(user.uid + '/configs/theme');
            return get(userRef)
                .then((dataSnapshot: DataSnapshot) => {
                    if (dataSnapshot.exists()) {
                        return dataSnapshot.val() as Theme;
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
}
