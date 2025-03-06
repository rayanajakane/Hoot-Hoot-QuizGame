package com.example.polyquiz.constants

enum class AnswerEvents(val value: String) {
    SELECT_CHOICE("selectChoice"),
    DESELECT_CHOICE("deselectChoice"),
    SUBMIT_ANSWER("submitAnswer"),
    UPDATE_LONG_ANSWER("updateLongAnswer"),
    TIMES_UP("timesUp"),
    GRADES("grades"),
    GRADE_ANSWERS("gradeAnswers"),
    BONUS("bonus"),
    FEEDBACK("feedback"),
    END_GAME("endGame"),
}
