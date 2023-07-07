package com.example.note.util

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonWriter
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter

val gson = Gson()

fun List<Any>.writeToJson(
    fileName: String,
    path: String
) {
    if (isEmpty()) return
    val file = File(path, fileName)
    if (file.exists()) file.delete()
    FileOutputStream(file).use { fos ->
        BufferedOutputStream(fos, 64 * 1024).use {
            gson.writeToOutputStream(it, this)
        }
    }
}

fun Gson.writeToOutputStream(
    outputStream: OutputStream,
    any: Any
) {
    val writer = JsonWriter(OutputStreamWriter(outputStream, "UTF-8"))
    writer.setIndent("  ")
    if (any is Collection<*>) {
        writer.beginArray()
        any.forEach {
            it?.let {
                toJson(it, it::class.java, writer)
            }
        }
        writer.endArray()
    } else {
        toJson(any, any::class.java, writer)
    }
    writer.close()
}

inline fun <reified T> fileToListT(path: String, fileName: String): List<T>? {
    runCatching {
        val file = File(path, fileName)
        if (!file.exists()) return null
        FileInputStream(file).use {
            return gson.fromJsonArray<T>(it).getOrThrow()
        }
    }.onFailure {
        it.printStackTrace()
    }
    return null
}

inline fun <reified T> Gson.fromJsonArray(inputStream: FileInputStream): Result<List<T>> {
    return kotlin.runCatching {
        val reader = InputStreamReader(inputStream)
        fromJson(
            reader,
            TypeToken.getParameterized(List::class.java, T::class.java).type
        ) as List<T>
    }
}