import { Component, OnInit } from '@angular/core';
import { PremiumAvatar } from '@app/constants/image-constants';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { AvatarService } from '@app/services/avatar/avatar.service';
import { MoneyService } from '@app/services/money/money.service';
import { NotificationService } from '@app/services/notification/notification.service';
import { SocketHandlerService } from '@app/services/socket-handler/socket-handler.service';
import { MoneyEvents } from '@common/events/money.events';
import { ShopItem } from '@common/interfaces/shop-item';
import { TranslocoService } from '@jsverse/transloco';

@Component({
    selector: 'app-shop-page',
    templateUrl: './shop-page.component.html',
    styleUrls: ['./shop-page.component.scss'],
})
export class ShopPageComponent implements OnInit {
    readonly AVATAR_PRICE = 10;
    avatarItems: ShopItem[] = [];

    constructor(
        public moneyService: MoneyService,
        private avatarService: AvatarService,
        private notificationService: NotificationService,
        private readonly authService: AuthenticationService,
        private readonly socketHandler: SocketHandlerService,
        private readonly translocoService: TranslocoService,
    ) {}

    async ngOnInit() {
        await this.initializeShopItems();
        this.onAvatarBought();
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

    private async initializeShopItems() {
        const purchasedAvatars = await this.avatarService.getPurchasedAvatars();

        this.avatarItems = Object.entries(PremiumAvatar).map(([key, value]) => ({
            id: key,
            imageUrl: value,
            price: this.AVATAR_PRICE,
            owned: purchasedAvatars.includes(key),
        }));
    }
}
