package com.taka.encfilereader.util

import kotlin.math.floor
import kotlin.math.log
import kotlin.math.pow

fun Int.formatBytes(): String {
    if (this <= 0) return "0 B"

    val baseSize = 1024.0
    val units = arrayOf("B", "KB", "MB", "GB", "TB")

    val unitIndex = (floor(log(this.toDouble(), baseSize))).toInt()

    val value = (this / baseSize.pow(unitIndex.toDouble()))

    return "${"%.2g".format(value)}${units[unitIndex]}"
}