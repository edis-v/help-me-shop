package io.moxd.shopforme

import android.graphics.Color
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.text.set
import androidx.core.text.toSpannable
import io.moxd.shopforme.ui.splashscreen.SplashScreen
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.abs

fun requireUserManager() = SplashScreen.userManager!!
fun requireAuthManager() = SplashScreen.authManager!!

val JsonDeserializer = Json {
    ignoreUnknownKeys = true // Nicht alle Keys müssen im Dto/Model vorhanden sein
    ;coerceInputValues = true
}




fun FormatDate(date: String) : String{

    var format = SimpleDateFormat("yyyy-M-dd")


    val currentTime: String = SimpleDateFormat("yyyy-M-dd", Locale.getDefault()).format(Date())
    Log.d("DAta2",currentTime)
    val t1 = format.parse(date)
    val t2 = format.parse(currentTime)
    // Toast.makeText(this, "D  ${t1.toString()}  D2  ${t2.toString()}  " , Toast.LENGTH_LONG).show()
    Log.d("t2",t2.time.toString())
    Log.d("t1",t1.time.toString())
    val difference = abs(t2.time - t1.time)
    Log.d("DAta2",difference.toString())
    val differenceDates = difference / (24 * 60 * 60 * 1000)
    val dayDifference =  (differenceDates)
    if(dayDifference == 1.toLong())
        return "Erstellt: Gestern"
    return if(dayDifference > 0) "Erstellt: vor $dayDifference Tagen" else "Erstellt: Heute"
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