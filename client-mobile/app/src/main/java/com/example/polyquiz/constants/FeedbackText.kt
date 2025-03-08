package com.example.polyquiz.constants

enum class MatchStatus(val value: String) {
    PREPARE("Préparez vous pour la prochaine question! ⏳")
}

enum class BankStatus(val message: String) {
    UNAVAILABLE("👀 Aucune autre question valide de la banque n'est disponible! 👀"),
    AVAILABLE("🖐 Glissez et déposez une question de la banque dans le jeu! 🖐"),
    SUCCESS("Question ajoutée à la banque avec succès! 😺"),
    FAILURE("La question n'a pas pu être ajoutée. 😿"),
    DUPLICATE("Cette question fait déjà partie de la banque! 😾"),
    MODIFIED("Question modifiée avec succès! 😺"),
    DELETED("Question supprimée avec succès! 😺"),
    UNMODIFIED("La question n'a pas pu être modifiée. 😿"),
    UNRETRIEVED("Échec d'obtention des questions 😿"),
    STILL("Échec de suppression de la question 😿")
}

enum class GameStatus(val message: String) {
    VERIFIED("Question vérifiée avec succès! 😺"),
    ARCHIVED("Question vérifiée et ajoutée à la banque avec succès! 😺"),
    DUPLICATE("Cette question fait déjà partie de la liste des questions de ce jeu! 😾"),
    FAILURE("Échec d'obtention du jeu 😿")
}

enum class QuestionStatus(val message: String) {
    VERIFIED("Question vérifiée avec succès! 😺"),
    UNVERIFIED("Question non vérifiée 😿"),
    DUPLICATE("Cette question fait déjà partie du jeu! 😾")
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
    BONUS("\uD83C\uDF89 Vous avez obtenu un bonus de %d points! \uD83C\uDF89");

    fun withPoints(points: Int): String {
        return String.format(value, points)
    }
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

enum class WarningMessage(val message: String) {
    PENDING("Vous avez des modifications non sauvegardées. Êtes-vous certain de vouloir quitter?"),
    QUIT("Vous êtes sur le point de quitter la partie. Êtes-vous certain de vouloir quitter?")
}

enum class SnackBarError(val message: String) {
    DELETED("Le jeu sélectionné n'existe plus 😿"),
    INVISIBLE("Le jeu sélectionné n'est plus visible 😿")
}

enum class SnackBarAction(val label: String) {
    REFRESH("Actualiser")
}

enum class RandomModeStatus(val message: String) {
    FAILURE("Il n'y a pas assez de questions pour un jeu aléatoire 😿")
}
