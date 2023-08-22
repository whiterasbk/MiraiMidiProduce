package org.mider.produce.service.plugins

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.logging.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.mider.produce.core.generate
import org.mider.produce.core.utils.toPinyin
import org.mider.produce.service.data.ResponseBody
import org.mider.produce.service.data.ServiceParameter
import org.mider.produce.service.utlis.getConfiguration
import org.mider.produce.service.utlis.hash
import whiter.music.mider.xml.LyricInception
import java.io.File

fun Application.configureRouting() {

    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    LyricInception.replace = { it.toPinyin() }

    val logger = log
    val (config, workspace) = getConfiguration(this)

    workspace.listFiles { dir, _ ->
        dir.delete()
    }

    // Starting point for a Ktor app:
    routing {
        get("/") {
            call.respondRedirect("static/index.html")
        }

        post("/api") {

            val parameter = try {
                 call.receive<ServiceParameter>()
            } catch (e: BadRequestException) {
                call.response.status(HttpStatusCode.BadRequest)
                call.respond(
                    ResponseBody(
                        404,
                        "failure",
                        "midercode is required: $e"
                    )
                )
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
                         logger.info("generate file at: ${file.absolutePath}")
                    }

                    links += mapOf(name to "/generated/${file.name}")
                }

                call.respond(
                    ResponseBody(
                        200,
                        "success",
                        "success generated midercode",
                        when {
                            result.isSing -> "sing"
                            result.isUploadMidi -> "midi"
                            result.isRenderingNotation -> "notation"
                            else -> "mp3"
                        },
                        links
                    )
                )

            } catch (e: Throwable) {
                call.response.status(HttpStatusCode.BadGateway)
                call.respond(
                    ResponseBody(
                        500,
                        "failure",
                        "server error: $e"
                    )
                )
                logger.error(e)
            }
        }

        post("/direct-api") {
            val parameter = try {
                call.receive<ServiceParameter>()
            } catch (e: BadRequestException) {
                call.response.status(HttpStatusCode.BadRequest)
                call.respond(
                    ResponseBody(
                        404,
                        "failure",
                        "midercode is required: $e"
                    )
                )
                logger.error(e)
                return@post
            }

            parameter.copy(config)

            try {

                val (result, streamList) = config.generate(parameter.midercode)
                val (stream, name) = streamList[0]
                val contentType: ContentType = when {
                    name.contains(".") -> name.split(".").last().let {
                        when (it) {
                            "png" -> ContentType.Image.PNG
                            "mp3" -> ContentType.Audio.MPEG
                            "mid" -> ContentType.Audio.Any
                            "silk" -> ContentType.Audio.Any
                            else -> ContentType.Any
                        }
                    }

                    else -> when {
                        result.isUploadMidi -> ContentType.Audio.Any
                        result.isRenderingNotation -> ContentType.Any
                        else -> ContentType.Audio.MPEG
                    }
                }

                call.respondBytes(contentType = contentType) {
                    withContext(Dispatchers.IO) {
                        stream.readAllBytes()
                    }
                }
            } catch (e: Throwable) {
                call.response.status(HttpStatusCode.BadGateway)
                call.respond(
                    ResponseBody(
                        500,
                        "failure",
                        "server error: $e"
                    )
                )
                logger.error(e)
            }
        }

        get("/generated/{hash}") {
            val hash = call.parameters["hash"] ?: run {
                call.response.status(HttpStatusCode.BadRequest)
                call.respond(
                    ResponseBody(
                        404,
                        "failure",
                        "filename is required"
                    )
                )
                logger.error("no hash provided.")
                return@get
            }

            try {
                call.respondFile(workspace, hash)
            } catch (e: Throwable) {
                call.response.status(HttpStatusCode.BadGateway)
                call.respond(
                    ResponseBody(
                        500,
                        "failure",
                        "server error: $e"
                    )
                )
                logger.error(e)
            }
        }

        static("/static") {
            resources("static")
        }
    }
}
