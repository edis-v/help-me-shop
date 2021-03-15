package io.moxd.shopforme

import android.graphics.Color
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import androidx.core.text.set
import androidx.core.text.toSpannable
import io.moxd.shopforme.ui.splashscreen.SplashScreen
import kotlinx.serialization.json.Json

fun requireUserManager() = SplashScreen.userManager!!
fun requireAuthManager() = SplashScreen.authManager!!

val JsonDeserializer = Json {
    ignoreUnknownKeys = true // Nicht alle Keys müssen im Dto/Model vorhanden sein
    ;coerceInputValues = true
}




fun TextView.toClickable(action: () -> Unit) {
    val spannable = text.toSpannable()
    val color = textColors.defaultColor

    highlightColor = Color.TRANSPARENT

    spannable[0 until spannable.length + 1] = object: ClickableSpan() {
        override fun onClick(widget: View) {
            action()
        }

        override fun updateDrawState(ds: TextPaint) {
            ds.color = color
            ds.isUnderlineText = true
        }
    }
    movementMethod = LinkMovementMethod()
    text = spannable
}

val <T> T.exhaustive: T
    get() = this