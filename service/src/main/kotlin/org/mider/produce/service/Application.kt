package org.mider.produce.service

import com.typesafe.config.ConfigFactory
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.mider.produce.service.plugins.configureRouting
import org.slf4j.LoggerFactory

const val defaultPort = 8080
const val defaultHost = "127.0.0.1"

fun main() {

    embeddedServer(Netty, environment = applicationEngineEnvironment {
        log = LoggerFactory.getLogger("produce.service.ktor.application")
        config = HoconApplicationConfig(ConfigFactory.load())

        module {
            configureRouting()
        }

        val providePort = config.propertyOrNull("service.deployment.port")?.getString()?.toIntOrNull()
        val provideHost = config.propertyOrNull("service.deployment.host")?.getString()

        connector {
            port = providePort ?: defaultPort
            host = provideHost ?: defaultHost
        }
    }).start(true)
}
