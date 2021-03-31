package io.moxd.shopforme.utils

import android.os.Build
import android.util.Log
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

fun currentDateAsString(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm:ss")
        return current.format(formatter)
    } else {
        val current = Date()
        val formatter = SimpleDateFormat("dd.MM.yyyy. HH:mm:ss")
        return formatter.format(current)
    }
}

fun stringifiedDateOlderThan30Min(date: String): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm:ss")
        val parsedDate = LocalDateTime.parse(date, formatter)

        val duration = Duration.between(parsedDate, current)

        if(duration.toMinutes() >= 30) {
            return true
        }
    } else {
        val current = Date()
        val formatter = SimpleDateFormat("dd.MM.yyyy. HH:mm:ss")
        val parsedDate = formatter.parse(date)
        val diffInMillies = Math.abs(current.getTime() - parsedDate.getTime())
        val diff = TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MINUTES)

        if(diff >= 30) {
            return true
        }
    }

    return false
}

fun FormatDate(date: String): String {
    var format = SimpleDateFormat("yyyy-M-dd")

    val currentTime: String = SimpleDateFormat("yyyy-M-dd", Locale.getDefault()).format(Date())
    Log.d("DAta2", currentTime)
    val t1 = format.parse(date)
    val t2 = format.parse(currentTime)
    // Toast.makeText(this, "D  ${t1.toString()}  D2  ${t2.toString()}  " , Toast.LENGTH_LONG).show()
    Log.d("t2", t2.time.toString())
    Log.d("t1", t1.time.toString())
    val difference = abs(t2.time - t1.time)
    Log.d("DAta2", difference.toString())
    val differenceDates = difference / (24 * 60 * 60 * 1000)
    val dayDifference = (differenceDates)
    if (dayDifference == 1.toLong())
        return "Gestern"
    return if (dayDifference > 0) "Vor $dayDifference Tagen" else "Heute"
}

fun ParseDate(): String {
    val sdf2 = SimpleDateFormat("yyyy-MM-dd HH:mm.ss", Locale.getDefault())
    val stwentyfourhour = sdf2.format(Date())
    Log.d("Parse Date", stwentyfourhour.replace(" ", "T").replace(".", ":") + "Z")
    return stwentyfourhour.replace(" ", "T").replace(".", ":") + "Z"
}
