package org.mider.produce.core.utils

import org.mider.produce.core.Configuration

fun Long.autoTimeUnit(): String {
    return if (this < 1000) {
        "${this}ms"
    } else if (this in 1000..59999) {
        "${ String.format("%.2f", this.toFloat() / 1000) }s"
    } else {
        "${ this / 60_000 }m${ (this % 60_000) / 1000 }s"
    }
}

suspend fun <R> Configuration.time(block: suspend () -> R): R {
    return if (debug) {
        val startCountingTime = System.currentTimeMillis()
        val r = block()
        val useTime = System.currentTimeMillis() - startCountingTime
        logger("生成用时: ${useTime.autoTimeUnit()}")
        r
    } else block()
}
