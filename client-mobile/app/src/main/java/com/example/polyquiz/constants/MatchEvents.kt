package com.example.polyquiz.constants

enum class MatchEvents(val value: String) {
    JOIN_ROOM("joinRoom"),
    CREATE_ROOM("createRoom"),
    ADD_PLAYER("addPlayer"),
    TOGGLE_LOCK("toggleLock"),
    BAN_USERNAME("banUsername"),
    KICK_PLAYER("kickPlayer"),
    SEND_PLAYERS_DATA("sendPlayersData"),
    FETCH_PLAYERS_DATA("fetchPlayersData"),
    START_MATCH("startMatch"),
    MATCH_STARTING("matchStarting"),
    BEGIN_QUIZ("beginQuiz"),
    GO_TO_NEXT_QUESTION("goToNextQuestion"),
    START_COOLDOWN("startCooldown"),
    CURRENT_ANSWERS("currentAnswers"),
    WINNER("winner"),
    ROUTE_TO_RESULTS_PAGE("routeToResultsPage"),
    HOST_QUIT_MATCH("hostQuitMatch"),
    DISCONNECT("disconnectFromRoom"),
    ERROR("error")
}
