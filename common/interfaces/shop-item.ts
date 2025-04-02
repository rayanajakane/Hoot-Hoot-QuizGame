export interface ShopItem {
    id: string;
    imageUrl: string;
    price: number;
    owned: boolean;
}

export interface PurchaseInfo extends ShopItem {
    user: string;
    item: ShopItem;
}
