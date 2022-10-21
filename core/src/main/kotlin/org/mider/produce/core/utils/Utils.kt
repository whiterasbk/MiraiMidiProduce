package org.mider.produce.core.utils

import org.mider.produce.core.Configuration

fun Configuration.ifDebug(block: ()-> Unit) {
    if (debug) block()
}

fun Configuration.ifDebug(info: String) {
    if (debug) logger(info)
}
