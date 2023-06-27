@file:Suppress("unused")
package com.example.note.util

import java.text.SimpleDateFormat
import java.util.Locale


val dateString: String get() {
    return System.currentTimeMillis().dateString
}

val Long.dateString: String get() {
    return SimpleDateFormat(
        "yyyy/M/dd HH:mm:ss",
        Locale.CHINA
    ).format(this)
}