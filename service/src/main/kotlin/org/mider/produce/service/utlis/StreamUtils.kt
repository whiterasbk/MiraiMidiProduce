package org.mider.produce.service.utlis

import java.io.File
import java.io.InputStream
import java.security.MessageDigest

fun String.hash(): String = MessageDigest
    .getInstance("SHA-256")
    .digest(toByteArray())
    .fold("") { str, it -> str + "%02x".format(it) }