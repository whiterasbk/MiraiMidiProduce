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
import io.ktor.util.pipeline.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.mider.produce.core.Configuration
import org.mider.produce.core.generate
import org.mider.produce.core.utils.toPinyin
import org.mider.produce.service.data.ResponseBody
import org.mider.produce.service.data.ServiceParameter
import org.mider.produce.service.utlis.getConfiguration
import org.mider.produce.service.utlis.hash
import whiter.music.mider.code.ProduceCoreResult
import whiter.music.mider.xml.LyricInception
import java.io.File
import java.io.InputStream
import java.net.URLDecoder
import java.util.Base64

fun Application.configureRouting() {

    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    LyricInception.replace = { it.toPinyin() }

    val logger = log
    val (config, workspace) = getConfiguration(this)

//    workspace.listFiles()?.forEach { file ->
//        if (file.isFile) {
//            file.delete()
//        }
//    }

    routing {
        get("/") {
            call.respondRedirect("static/index.html")
        }

        post("/api") {
            handleApiRequest(config, workspace, logger, isDirectApi = false)
        }

        post("/direct-api") {
            handleApiRequest(config, workspace, logger, isDirectApi = true)
        }

        get("/m") {
            handleMidercodeRequest(config, workspace, logger, defaultRaw = false)
        }

        get("/md") {
            handleMidercodeRequest(config, workspace, logger, defaultRaw = true)
        }

        get("/generated/{hash}") {
            handleFileRequest(workspace, logger)
        }

        static("/static") {
            resources("static")
        }
    }
}

// 提取通用的错误响应函数
private suspend fun PipelineContext<Unit, ApplicationCall>.respondError(
    statusCode: HttpStatusCode,
    code: Int,
    message: String,
    logger: Logger,
    error: Throwable? = null
) {
    call.response.status(statusCode)
    call.respond(ResponseBody(code, "failure", message))
    error?.let { logger.error(it) } ?: logger.error(message)
}

// 提取参数接收逻辑
private suspend fun PipelineContext<Unit, ApplicationCall>.receiveParameter(
    logger: Logger
): ServiceParameter? {
    return try {
        call.receive<ServiceParameter>()
    } catch (e: BadRequestException) {
        respondError(
            HttpStatusCode.BadRequest,
            400,
            "midercode is required: ${e.message}",
            logger,
            e
        )
        null
    }
}

// 统一处理 API 请求
private suspend fun PipelineContext<Unit, ApplicationCall>.handleApiRequest(
    config: Configuration,
    workspace: File,
    logger: Logger,
    isDirectApi: Boolean
) {
    val parameter = receiveParameter(logger) ?: return
    _handleApiRequest(parameter, config, isDirectApi, logger, workspace)
}

private suspend fun PipelineContext<Unit, ApplicationCall>._handleApiRequest(
    parameter: ServiceParameter,
    config: Configuration,
    isDirectApi: Boolean,
    logger: Logger,
    workspace: File
) {
    parameter.copy(config)

    try {
        val (result, streamList) = config.generate(parameter.midercode)

        if (isDirectApi) {
            handleDirectApiResponse(result, streamList, logger)
        } else {
            handleStandardApiResponse(result, streamList, parameter, workspace, logger)
        }
    } catch (e: Throwable) {
        respondError(
            HttpStatusCode.BadGateway,
            500,
            "server error: ${e.message}",
            logger,
            e
        )
    }
}

// 处理标准 API 响应
private suspend fun PipelineContext<Unit, ApplicationCall>.handleStandardApiResponse(
    result: ProduceCoreResult,
    streamList: List<Pair<InputStream, String>>,
    parameter: ServiceParameter,
    workspace: File,
    logger: Logger
) {
    val links = streamList.mapIndexed { index, (stream, name) ->
        val ext = determineFileExtension(name, result)
        val fileName = parameter.midercode.hash()
        val file = File(workspace, "$fileName-${index + 1}.$ext")

        if (!file.exists() || !parameter.cache) {
            file.writeBytes(withContext(Dispatchers.IO) {
                stream.readAllBytes()
            })
            logger.info("Generated file at: ${file.absolutePath}")
        }

        mapOf(name to "/generated/${file.name}")
    }

    call.respond(
        ResponseBody(
            stateCode = 200,
            state = "success",
            message = "Successfully generated midercode",
            type = determineResponseType(result),
            link = links
        )
    )
}

// 处理直接 API 响应
private suspend fun PipelineContext<Unit, ApplicationCall>.handleDirectApiResponse(
    result: ProduceCoreResult,
    streamList: List<Pair<InputStream, String>>,
    logger: Logger
) {
    val (stream, name) = streamList.first()
    val contentType = determineContentType(name, result)

    call.respondBytes(contentType = contentType) {
        withContext(Dispatchers.IO) {
            stream.readAllBytes()
        }
    }
}

// 处理文件请求
private suspend fun PipelineContext<Unit, ApplicationCall>.handleFileRequest(
    workspace: File,
    logger: Logger
) {
    val hash = call.parameters["hash"]

    if (hash.isNullOrBlank()) {
        respondError(
            HttpStatusCode.BadRequest,
            400,
            "filename is required",
            logger
        )
        return
    }

    try {
        call.respondFile(workspace, hash)
    } catch (e: Throwable) {
        respondError(
            HttpStatusCode.BadGateway,
            500,
            "server error: ${e.message}",
            logger,
            e
        )
    }
}

// 确定文件扩展名
private fun determineFileExtension(name: String, result: ProduceCoreResult): String {
    return when {
        '.' in name -> name.substringAfterLast('.')
        else -> when {
            result.isUploadMidi -> "mid"
            result.isRenderingNotation -> "tmp"
            else -> "mp3"
        }
    }
}

// 确定响应类型
private fun determineResponseType(result: ProduceCoreResult): String {
    return when {
        result.isSing -> "sing"
        result.isUploadMidi -> "midi"
        result.isRenderingNotation -> "notation"
        else -> "mp3"
    }
}

// 确定内容类型
private fun determineContentType(name: String, result: ProduceCoreResult): ContentType {
    val extension = if ('.' in name) name.substringAfterLast('.') else null

    return when (extension) {
        "png" -> ContentType.Image.PNG
        "mp3" -> ContentType.Audio.MPEG
        "mid", "silk" -> ContentType.Audio.Any
        null -> when {
            result.isUploadMidi -> ContentType.Audio.Any
            result.isRenderingNotation -> ContentType.Any
            else -> ContentType.Audio.MPEG
        }
        else -> ContentType.Any
    }
}

// 处理 /m 和 /md 接口的请求
private suspend fun PipelineContext<Unit, ApplicationCall>.handleMidercodeRequest(
    config: Configuration,
    workspace: File,
    logger: Logger,
    defaultRaw: Boolean
) {
    // 获取查询参数
    val cParam = call.parameters["c"]
    val bParam = call.parameters["b"]
    val rawParam = call.parameters["raw"]

    // 解析 midercode
    val midercode = when {
        !cParam.isNullOrBlank() -> {
            // c 参数存在，使用 URL 解码
            try {
                URLDecoder.decode(cParam, "UTF-8")
            } catch (e: Exception) {
                respondError(
                    HttpStatusCode.BadRequest,
                    400,
                    "Invalid URL encoding in parameter 'c': ${e.message}",
                    logger,
                    e
                )
                return
            }
        }

        !bParam.isNullOrBlank() -> {
            // b 参数存在，使用 Base64 解码
            try {
                String(Base64.getDecoder().decode(bParam), Charsets.UTF_8)
            } catch (e: Exception) {
                respondError(
                    HttpStatusCode.BadRequest,
                    400,
                    "Invalid Base64 encoding in parameter 'b': ${e.message}",
                    logger,
                    e
                )
                return
            }
        }

        else -> {
            respondError(
                HttpStatusCode.BadRequest,
                400,
                "Either parameter 'c' or 'b' is required",
                logger
            )
            return
        }
    }

    // 解析 raw 参数
    val isDirectApi = when {
        rawParam != null -> rawParam.toBoolean()
        else -> defaultRaw
    }

    // 创建 ServiceParameter
    val parameter = ServiceParameter(midercode = midercode)

    _handleApiRequest(parameter, config, isDirectApi, logger, workspace)
}