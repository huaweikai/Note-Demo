package com.example.note.util

import android.content.res.Resources
import android.util.TypedValue
import android.widget.EditText

fun EditText.setTextWithEnd(
    text: CharSequence
) {
    setText(text)
    setSelection(text.length)
}

val Int.dp: Int
    get() {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            Resources.getSystem().displayMetrics
        ).toInt()
    }
