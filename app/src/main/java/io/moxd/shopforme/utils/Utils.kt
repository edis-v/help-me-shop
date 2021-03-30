package io.moxd.shopforme.utils

import io.moxd.shopforme.MainActivity
import kotlinx.serialization.json.Json

val JsonDeserializer = Json {
    ignoreUnknownKeys = true // Nicht alle Keys müssen im Dto/Model vorhanden sein
    ;coerceInputValues = true
}


fun requireAuthManager() = MainActivity.authManager!!
fun requireUserManager() = MainActivity.userManager!!

val Int.minutes get() = this * 60 * 1000

enum class ActionType { DoneHF, DoneHFS, PayHFS }

val <T> T.exhaustive: T
    get() = this