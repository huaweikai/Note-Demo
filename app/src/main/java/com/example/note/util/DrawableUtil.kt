package com.example.note.util

import android.content.Context
import android.graphics.drawable.GradientDrawable
import com.example.note.R


fun Context.getCircleDrawable(
    color: Int
): GradientDrawable {
    val drawable = GradientDrawable()
    drawable.shape = GradientDrawable.OVAL
    drawable.setSize(21.dp, 21.dp)
    drawable.setColor(color)
    drawable.setStroke(1.dp, getColor(R.color.ColorDefaultNote))
    return drawable
}

