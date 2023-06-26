package com.example.note

sealed class LCE<out T> {
    object Loading: LCE<Nothing>()
    data class Success<T>(val message: String, val data: T) : LCE<T>()
    data class Error(val message: String) : LCE<Nothing>()
}

inline fun <T> LCE<T>.onSuccess(success: (message: String, data: T) -> Unit): LCE<T> {
    if (this is LCE.Success) {
        success(this.message, this.data)
    }
    return this
}

inline fun <T> LCE<T>.onError(error: (message: String) -> Unit): LCE<T> {
    if (this is LCE.Error) {
        error(this.message)
    }
    return this
}