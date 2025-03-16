import com.example.polyquiz.constants.MatchContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object MatchContextService {
    private val _context = MutableStateFlow(MatchContext.Null)
    val context = _context.asStateFlow() // Public read-only flow

    fun resetContext() {
        _context.value = MatchContext.Null
    }

    fun setContext(context: MatchContext) {
        _context.value = context
    }

    fun getContext(): MatchContext {
        return context.value
    }
}
