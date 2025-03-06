package com.example.polyquiz.match.domain
import com.example.polyquiz.constants.MatchContext

object MatchContextService {
    private var context: MatchContext = MatchContext.Null

    fun resetContext() {
        context = MatchContext.Null
    }

    fun setContext(context: MatchContext) {
        this.context = context
    }

    fun getContext(): MatchContext = context
}
