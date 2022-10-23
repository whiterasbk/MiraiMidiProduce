package org.mider.produce.service.plugins

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.logging.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.mider.produce.core.Configuration
import org.mider.produce.core.generate
import org.mider.produce.service.data.ServiceParameter
import org.mider.produce.service.logger.AppLogger
import org.mider.produce.service.utlis.getConfiguration
import org.mider.produce.service.utlis.hash
import org.mider.produce.service.utlis.setupViaEnvironment
import java.io.File

fun Application.configureRouting() {

    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    val logger = log
    val (config, workspace) = getConfiguration(this)

    log.info(">>>>>" + config.debug)

    // Starting point for a Ktor app:
    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        post("/api") {

            val parameter = try {
                 call.receive<ServiceParameter>()
            } catch (e: BadRequestException) {
                call.respond(mapOf(
                    "status" to "failure",
                    "message" to "midercode is required: $e"
                ))
                logger.error(e)
                return@post
            }

            parameter.copy(config)

            try {
                val (result, streamList) = config.generate(parameter.midercode)

                val links = mutableListOf<Map<String, String>>()

                for ((index, streamItem) in streamList.withIndex()) {
                    val (stream, name) = streamItem
                    val ext = when {
                        '.' in name -> name.split(".").last()
                        else -> when {
                            result.isUploadMidi -> "mid"
                            result.isRenderingNotation -> "tmp"
                            else -> "mp3"
                        }
                    }
                    val fileName = parameter.midercode.hash()
                    val file = File(workspace, "$fileName-${index + 1}.$ext")
                    if (!file.exists() or !parameter.cache) {
                        file.writeBytes(withContext(Dispatchers.IO) {
                            stream.readAllBytes()
                        })
                        logger.info("generate file: $file at: ${file.absolutePath}")
                    }

                    links += mapOf(name to "/generated/${file.name}")
                }

                call.respond(mapOf(
                    "status" to "success",
                    "type" to when {
                        result.isSing -> "sing"
                        result.isUploadMidi -> "midi"
                        result.isRenderingNotation -> "notation"
                        else -> "mp3"
                    },
                    "links" to links
                ))

            } catch (e: Throwable) {
                call.respond(mapOf(
                    "status" to "failure",
                    "message" to e.message
                ))
                logger.error(e)
            }
        }

        get("/generated/{hash}") {
            val hash = call.parameters["hash"] ?: run {
                call.respond(mapOf(
                    "status" to "failure",
                    "message" to "file name is require."
                ))
                logger.error("no hash provided.")
                return@get
            }

            try {
                call.respondFile(workspace, hash)
            } catch (e: Throwable) {
                call.respond(mapOf(
                    "status" to "failure",
                    "message" to e.message
                ))
                logger.error(e)
            }
        }
    }
}
