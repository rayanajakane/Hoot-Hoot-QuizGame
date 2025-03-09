import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed class StringValue {

    data class DynamicString(val value: String) : StringValue()

    data object Empty : StringValue()

    class StringResource(
        @StringRes val resId: Int,
        vararg val args: Any
    ) : StringValue()

    @Composable
    fun asString(): Any {
        return when (this) {
            is Empty -> ""
            is DynamicString -> value
            is StringResource -> stringResource(resId, *args)
            // Since this is a sealed class, we never have to worry about hitting the else block.
            // However, this is needed to avoid compiler errors
            else -> ""
        }
    }

    fun asString(context: Context?): String {
        return when (this) {
            is Empty -> ""
            is DynamicString -> value
            is StringResource -> context?.getString(resId, *args).orEmpty()
            // Since this is a sealed class, we never have to worry about hitting the else block.
            // However, this is needed to avoid compiler errors
            else -> ""
        }
    }
}
