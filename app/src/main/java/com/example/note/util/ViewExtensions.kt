package com.example.note.util

import android.widget.EditText

fun EditText.setTextWithEnd(
    text: CharSequence
) {
    setText(text)
    setSelection(text.length)
}