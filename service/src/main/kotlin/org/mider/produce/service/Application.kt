package org.mider.produce.service

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.mider.produce.service.plugins.configureRouting

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureRouting()
    }.start(wait = true)
}
