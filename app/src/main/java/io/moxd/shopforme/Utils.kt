package io.moxd.shopforme

import android.app.Activity
import android.graphics.Color
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.text.set
import androidx.core.text.toSpannable
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.result.Result
import io.moxd.shopforme.data.RestPath
import io.moxd.shopforme.data.model.ErrorField
import io.moxd.shopforme.service.MyFirebaseMessagingService
import io.moxd.shopforme.ui.splashscreen.SplashScreen
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.regex.Pattern
import kotlin.math.abs

val PASSWORD_PATTERN =
    Pattern.compile("^" +
            "(?=.*[0-9])" +         //at least 1 digit
            "(?=.*[a-z])" +         //at least 1 lower case letter
            "(?=.*[A-Z])" +         //at least 1 upper case letter
            "(?=.*[a-zA-Z])" +      //any letter
            "(?=\\S+$)" +           //no white spaces
            ".{4,}" +               //at least 4 characters
            "$");

fun requireUserManager() = SplashScreen.userManager!!
fun requireAuthManager() = SplashScreen.authManager!!
var ActitityMain : Activity? = null
val JsonDeserializer = Json {
    ignoreUnknownKeys = true // Nicht alle Keys müssen im Dto/Model vorhanden sein
    ;coerceInputValues = true
}



fun getError(response: Response) : String = runBlocking{
    withContext(Dispatchers.IO) {
        val data = response.body().asString("application/json")
        Log.d("getErrorData", data)

        /*   if(data.contains("Invalid Sessionid"))
        GlobalScope.launch {  requireAuthManager().auth2() }

*/
        return@withContext if (data.contains("non_field_errors"))
            JsonDeserializer.decodeFromString<ErrorField>(data).Error()
        else
            data.replace("[\"", "").replace("\"]", "")
    }
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
        return "Gestern"
    return if(dayDifference > 0) "Vor $dayDifference Tagen" else "Heute"
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