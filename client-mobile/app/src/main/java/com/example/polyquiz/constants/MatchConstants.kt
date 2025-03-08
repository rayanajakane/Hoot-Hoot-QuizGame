package com.example.polyquiz.constants

const val FACTOR = 9000
const val MAXIMUM_CODE_LENGTH = 4
const val COUNTDOWN_TIME = 5
const val COOLDOWN_TIME = 3
const val BONUS_FACTOR = 0.2
const val MULTIPLICATION_FACTOR = 100
const val MINIMUM_QUESTIONS = 5
const val HOST_USERNAME = "Organisateur"
const val FREE_ANSWER_MAX_LENGTH = 200
const val LONG_ANSWER_TIME = 60
const val HISTOGRAM_UPDATE_TIME_SECONDS = 5
const val HISTOGRAM_UPDATE_TIME_MS = 5000
const val PANIC_ALERT_DELAY = 1000

enum class MatchButtonActions(val value: String){
    NEXT_QUESTION("QUESTION SUIVANTE"),
    SUBMIT_ANSWER("SOUMETTRE"),
    LEAVE_MATCH("QUITTER"),
    SUBMIT_GRADING("SOUMETTRE CORRECTION"),
    END_GAME("TERMINER LA PARTIE"),
    BAN_PLAYER("BANNIR JOUEUR"),
    START_MATCH("COMMENCER LA PARTIE"),
}

enum class MatchDisplayText(val value: String){
    ANSWER("Réponse"),
}
