@file:JvmName("Utilities")

package com.varunbarad.cluetrackingdonutclone

import kotlin.math.PI

fun Float.toRadians(): Float {
    return ((this / 180.toFloat()) * PI.toFloat())
}

fun Float.toDegrees(): Float {
    return ((this / PI.toFloat()) * 180.toFloat())
}
