package org.mider.produce.service

import com.typesafe.config.ConfigFactory
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.mider.produce.service.plugins.configureRouting
import org.slf4j.LoggerFactory
import java.io.File

const val DEFAULT_PORT = 8080
const val DEFAULT_HOST = "0.0.0.0"

fun main(args: Array<String>) {

    val configPath = args.firstOrNull()
        ?: System.getenv("APP_CONFIG")
        ?: System.getProperty("config.file")
        ?: "application.conf"

    embeddedServer(Netty, environment = applicationEngineEnvironment {
        log = LoggerFactory.getLogger("produce.service.ktor.application")
        config = HoconApplicationConfig(try {
            ConfigFactory.parseFile(File(configPath))
                .withFallback(ConfigFactory.load())
                .resolve()
        } catch (e: Exception) {
            log.warn("Warning: Could not load config from $configPath, msg: ${e.message}, using default")
            ConfigFactory.load()
        })

        module {
            configureRouting()
        }

        val providePort = config.propertyOrNull("service.deployment.port")?.getString()?.toIntOrNull()
        val provideHost = config.propertyOrNull("service.deployment.host")?.getString()

        connector {
            port = providePort ?: DEFAULT_PORT
            host = provideHost ?: DEFAULT_HOST
        }
    }).start(true)
}
