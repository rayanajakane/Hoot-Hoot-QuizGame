package com.example.polyquiz.constants

enum class MatchStatus(val value: String) {
    PREPARE("Préparez vous pour la prochaine question! ⏳")
}


enum class AnswerFeedback(val value: String) {
    WRONG("\uD83D\uDE14 Mauvaise Réponse \uD83D\uDE14"),
    GOOD("\uD83C\uDD97 Réponse correcte! Vous avez obtenu %d points \uD83C\uDD97"),
    OK("\uD83C\uDD97 Réponse partielle! Vous avez obtenu %d points \uD83C\uDD97");

    fun withPoints(points: Int): String {
        return String.format(value, points)
    }
}

enum class BonusFeedback(val value: String) {
    BONUS("\uD83C\uDF89 Vous avez obtenu un bonus de 20%! \uD83C\uDF89");
}

enum class GradingFeedback(val value: String) {
    GRADE_PLAYERS("Veuillez noter les réponses des joueurs!"),
    PLAYERS_TO_GRADE("Il reste des joueurs à évaluer"),
    WAITING_FOR_GRADING("En attente de correction..."),
}

enum class StartMatchFeedback(val value: String) {
    WAITING_TO_START("La partie va bientôt commencer..."),
    LOCK_MATCH("Verrouiller la partie"),
}

enum class EstimatedQuestionFeedback(val value: String) {
    CHOOSE_VALUE("Choisissez une valeur estimée. La marge est de +/- %s"),
    ANSWER_OUT_OF_BOUNDS("Votre réponse est en dehors de l'intervalle"),
    CORRECT_ANSWER("Réponse correcte : %s. La marge était de +/-");

    fun withPoints(points: Int): String {
        return String.format(value, points)
    }
}
