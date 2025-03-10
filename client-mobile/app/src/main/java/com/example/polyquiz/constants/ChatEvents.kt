package com.example.polyquiz.constants

enum class ChatEvents (val value: String) {
    ROOM_MESSAGE("roomMessage"),
    SEND_MESSAGES_HISTORY("sendMessagesHistory"),
    FETCH_OLD_MESSAGES("fetchOldMessages"),
    NEW_MESSAGE("newMessage"),
    CHANGE_CHAT_STATE("changeChatState"),
    TOGGLE_CHAT_STATE("toggleChatState"),
    RETURN_CURRENT_CHAT_STATE("returnCurrentChatState"),
    CHAT_REACTIVATED("chatReactivated"),
    GENERAL_MESSAGE("generalMessage"),
    SENT_GENERAL_MESSAGE("sentGeneralMessage"),
    GENERAL_EMOJI("generalEmoji"),
    ROOM_EMOJI("roomEmoji"),
    SENT_GENERAL_EMOJI("sentGeneralEmoji"),
    SENT_ROOM_EMOJI("sentRoomEmoji"),
}
