package io.moxd.shopforme.utils

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

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
