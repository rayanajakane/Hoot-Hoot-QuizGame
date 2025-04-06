package com.example.polyquiz.constants

enum class TimerEvents(val value: String) {
    START_TIMER("startTimer"),
    STOP_TIMER("stopTimer"),
    PAUSE_TIMER("pauseTimer"),
    RESUME_TIMER("resumeTimer"),
    TIMER("timer"),
    PANIC_TIMER("panicTimer"),
    DISABLE_PANIC_TIMER("disablePanicTimer"),
}
