package com.example.note.util

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

sealed class FileType(val path: String) {

    object Log : FileType("log")

    object Cache : FileType("cache")

    object Download : FileType("download")

}

suspend fun Context.copyToSandBox(
    fileType: FileType,
    uri: Uri,
    fileName: String? = null
): String = withContext(Dispatchers.IO){
    val dir = getExternalFilesDir(fileType.path)
    if (dir?.exists() == false) {
        dir.mkdirs()
    }
    val name = fileName ?: uri.getFileInfo(this@copyToSandBox).first
    val file = File(dir, name)
    if (file.exists()) {
        file.delete()
    }
    contentResolver.openInputStream(uri)?.use { inputStream ->
        file.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
        }
    }
    return@withContext file.path
}