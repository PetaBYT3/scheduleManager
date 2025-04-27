package com.schedule.rt.sync.function

import android.content.res.Resources
import android.util.TypedValue

fun String.capitalizeEachWord(): String {
    return this.split(" ")
        .joinToString(" ") { word -> word.lowercase().replaceFirstChar { it.uppercase() } }
}

fun String.capitalizeAfterDot(): String {
    return this.split(Regex("(?<=\\.)"))
        .joinToString("") { part ->
            part.replaceFirstChar { if (it.isLetter()) it.uppercase() else it.toString() }
        }
}

fun dpToPx(dp: Int): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp.toFloat(),
        Resources.getSystem().displayMetrics
    ).toInt()
}
