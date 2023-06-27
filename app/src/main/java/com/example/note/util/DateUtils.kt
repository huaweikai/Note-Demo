package com.example.note.util

import java.text.SimpleDateFormat
import java.util.Locale


val dateString: String get() {
    return SimpleDateFormat(
        "yyyy/M/dd HH:mm:ss",
        Locale.getDefault()
    ).format(System.currentTimeMillis())
}