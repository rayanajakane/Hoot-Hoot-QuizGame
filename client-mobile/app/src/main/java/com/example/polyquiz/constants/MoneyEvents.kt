package com.example.polyquiz.constants

enum class MoneyEvents (val value: String){
    GET_BALANCE("getBalance"),
    RETURN_BALANCE("returnBalance"),
    DONATE_MONEY("donateMoney"),
    DONATION_GIVEN("donationGiven"),
    DONATION_RECEIVED("donationReceived"),
    BUY_AVATAR("buyAvatar"),
    AVATAR_BOUGHT("avatarBought"),
    BUY_THEME("buyTheme"),
    THEME_BOUGHT("themeBought"),
    BUY_WALLPAPER("buyWallpaper"),
    WALLPAPER_BOUGHT("wallpaperBought"),
    ERROR("error")
}
