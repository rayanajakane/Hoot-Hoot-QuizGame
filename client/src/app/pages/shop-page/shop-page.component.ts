import { Component, OnInit } from '@angular/core';
import { PremiumAvatar } from '@app/constants/image-constants';
import { AvatarService } from '@app/services/avatar/avatar.service';
import { MoneyService } from '@app/services/money/money.service';
import { NotificationService } from '@app/services/notification/notification.service';

interface ShopItem {
    id: string;
    imageUrl: string;
    price: number;
    owned: boolean;
}

@Component({
    selector: 'app-shop-page',
    templateUrl: './shop-page.component.html',
    styleUrls: ['./shop-page.component.scss'],
})
export class ShopPageComponent implements OnInit {
    readonly AVATAR_PRICE = 1000;
    avatarItems: ShopItem[] = [];

    constructor(
        public moneyService: MoneyService,
        private avatarService: AvatarService,
        private notificationService: NotificationService,
    ) {}

    async ngOnInit() {
        await this.initializeShopItems();
    }

    async purchaseAvatar(item: ShopItem) {
        if (this.moneyService.currentBalance >= item.price) {
            try {
                await this.avatarService.purchaseAvatar(item.id);
                // await this.moneyService.decreaseBalance(item.price);
                item.owned = true;
                this.notificationService.displaySuccessMessage('Avatar purchased successfully!');
            } catch (error) {
                this.notificationService.displayErrorMessage('Failed to purchase avatar');
            }
        } else {
            this.notificationService.displayErrorMessage('Insufficient funds!');
        }
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
