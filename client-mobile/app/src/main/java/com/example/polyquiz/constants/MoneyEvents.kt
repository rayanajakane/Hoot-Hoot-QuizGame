package com.example.polyquiz.constants

enum class MoneyEvents (val value: String){
    GET_BALANCE("getBalance"),
    RETURN_BALANCE("returnBalance"),
    DONATE_MONEY("donateMoney"),
    DONATION_GIVEN("donationGiven"),
    DONATION_RECEIVED("donationReceived"),
    ERROR("error")
}
