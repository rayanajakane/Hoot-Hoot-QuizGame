import { Component } from '@angular/core';
import { PremiumAvatar } from '@app/constants/image-constants';
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
export class ShopPageComponent {
    readonly AVATAR_PRICE = 1000;
    shopItems: ShopItem[] = [];

    constructor(
        public moneyService: MoneyService,
        // private authService: AuthenticationService,
        private notificationService: NotificationService,
    ) {
        this.initializeShopItems();
    }

    purchaseAvatar(item: ShopItem) {
        if (this.moneyService.currentBalance >= item.price) {
            // this.moneyService.decreaseBalance(item.price);
            item.owned = true;
            this.notificationService.displaySuccessMessage('Avatar purchased successfully!');
        } else {
            this.notificationService.displayErrorMessage('Insufficient funds!');
        }
    }

    private initializeShopItems() {
        this.shopItems = Object.entries(PremiumAvatar).map(([key, value]) => ({
            id: key,
            imageUrl: value,
            price: this.AVATAR_PRICE,
            owned: false, // TODO: Add logic to check if user owns the avatar
        }));
    }
}
