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
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import io.moxd.shopforme.data.RestPath
import io.moxd.shopforme.service.MyFirebaseMessagingService
import io.moxd.shopforme.ui.splashscreen.SplashScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
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


fun sendRegistrationToServer(token: String?) {
    // TODO: Implement this method to send token to your app server.
    //api send token to db
    GlobalScope.launch(context = Dispatchers.IO) {
        requireAuthManager().SessionID().take(1).collect {

            val url = RestPath.firebasetoken(it)

            Log.d("URL", url)

            Fuel.put(
                url, listOf("firebase_token" to token)
            ).responseString { request, response, result ->

                when (result) {
                    is Result.Success -> {
                        Log.d("result", result.get())

                    }
                    is Result.Failure -> {
                        Log.d(
                            "Failed",
                            result.getException().message.toString()
                        )
                    }

                }


            }.join()


        }

    }
    Log.d("Firebase", "sendRegistrationTokenToServer($token)")
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