import { Component, OnInit } from '@angular/core';
import { AVATAR_PRICE, PremiumAvatar } from '@app/constants/avatar-constants';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { AvatarService } from '@app/services/avatar/avatar.service';
import { MoneyService } from '@app/services/money/money.service';
import { NotificationService } from '@app/services/notification/notification.service';
import { SocketHandlerService } from '@app/services/socket-handler/socket-handler.service';
import { Theme, ThemeService } from '@app/services/theme/theme.service';
import { Wallpaper, WallpaperService } from '@app/services/wallpaper/wallpaper.service';
import { MoneyEvents } from '@common/events/money.events';
import { ShopItem } from '@common/interfaces/shop-item';
import { TranslocoService } from '@jsverse/transloco';

@Component({
    selector: 'app-shop-page',
    templateUrl: './shop-page.component.html',
    styleUrl: './shop-page.component.scss',
})
export class ShopPageComponent implements OnInit {
    avatarItems: ShopItem[] = [];
    themeItems: ShopItem[] = [];
    wallpaperItems: ShopItem[] = [];
    premiumThemes = [Theme.LUIGI, Theme.MARIO, Theme.SONIC, Theme.PIKACHU];

    private readonly THEME_PRICE = 50;
    private readonly WALLPAPER_PRICE = 30;

    constructor(
        public moneyService: MoneyService,
        private avatarService: AvatarService,
        private notificationService: NotificationService,
        private readonly authService: AuthenticationService,
        private readonly socketHandler: SocketHandlerService,
        private readonly translocoService: TranslocoService,
        private readonly themeService: ThemeService,
        private wallpaperService: WallpaperService,
    ) {}

    async ngOnInit() {
        await this.initializeShopItems();
        this.onAvatarBought();
        this.onThemeBought();
        this.onWallpaperBought();
    }

    buyAvatar(item: ShopItem) {
        this.socketHandler.send(MoneyEvents.BuyAvatar, {
            user: this.authService.userId,
            item,
        });
    }

    onAvatarBought() {
        this.socketHandler.on(MoneyEvents.AvatarBought, (item: ShopItem) => {
            console.log('Avatar bought:', item);
            this.avatarService.purchaseAvatar(item.id);
            const foundAvatar = this.avatarItems.find((avatar) => avatar.id === item.id);
            if (foundAvatar) {
                foundAvatar.owned = true;
            }
            this.notificationService.displaySuccessMessage(this.translocoService.translate('shop.buy-successfully'));
        });
    }

    // getThemeLabel(theme: Theme): string {
    //     return this.themeService.themeLabels[theme];
    // }

    buyTheme(item: ShopItem) {
        this.socketHandler.send(MoneyEvents.BuyTheme, {
            user: this.authService.userId,
            item,
        });
    }

    onThemeBought() {
        this.socketHandler.on(MoneyEvents.ThemeBought, (item: ShopItem) => {
            console.log('Theme bought:', item);
            this.themeService.purchaseTheme(item.id);
            const foundTheme = this.themeItems.find((theme) => theme.id === item.id);
            if (foundTheme) {
                foundTheme.owned = true;
            }
            this.notificationService.displaySuccessMessage(this.translocoService.translate('shop.buy-successfully'));
        });
    }

    buyWallpaper(item: ShopItem) {
        this.socketHandler.send(MoneyEvents.BuyWallpaper, {
            user: this.authService.userId,
            item,
        });
    }

    onWallpaperBought() {
        this.socketHandler.on(MoneyEvents.WallpaperBought, (item: ShopItem) => {
            console.log('Wallpaper bought:', item);
            this.wallpaperService.purchaseWallpaper(item.id);
            const foundWallpaper = this.wallpaperItems.find((wallpaper) => wallpaper.id === item.id);
            if (foundWallpaper) {
                foundWallpaper.owned = true;
            }
            this.notificationService.displaySuccessMessage(this.translocoService.translate('shop.buy-successfully'));
        });
    }

    private async initializeShopItems() {
        const purchasedAvatars = await this.avatarService.getPurchasedAvatars();

        this.avatarItems = Object.entries(PremiumAvatar).map(([key, value]) => ({
            id: key,
            imageUrl: value,
            price: AVATAR_PRICE,
            owned: purchasedAvatars.includes(key),
        }));

        const purchasedThemes = await this.themeService.getPurchasedThemes();

        this.themeItems = this.premiumThemes.map((theme) => ({
            id: theme,
            imageUrl: this.themeService.getThemeImage(theme),
            price: this.THEME_PRICE,
            owned: purchasedThemes.includes(theme.toString()),
        }));

        const purchasedWallpapers = await this.wallpaperService.getPurchasedWallpapers();

        this.wallpaperItems = Object.entries(Wallpaper)
            .filter(([key]) => key !== 'None')
            .map(([key, value]) => ({
                id: key,
                imageUrl: value,
                price: this.WALLPAPER_PRICE,
                owned: purchasedWallpapers.includes(key),
            }));
    }
}
