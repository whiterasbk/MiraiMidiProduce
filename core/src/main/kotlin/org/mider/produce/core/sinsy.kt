package org.mider.produce.core

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import okio.Timeout
import java.io.File
import java.io.InputStream

private val singers = mapOf(
    "english" to mapOf(
        "female" to listOf(9, 10),
        "male" to listOf(11)
    ),
    "japanese" to mapOf(
        "female" to listOf(0, 1, 2, 4, 5, 6, 7),
        "male" to listOf(3,8)
    ),
    "mandarin" to mapOf(
        "female" to listOf(12),
        "male" to listOf()
    )
)

fun selectSinger(info: Pair<String, String>?): Pair<Int, String> {
    fun pick(area: String, pattern: String): Int =
            singers[area]?.let { lang ->
                lang[if (pattern.startsWith("f")) "female" else "male"]?.let { gender ->
                    try {
                        gender[(pattern
                            .replace("f", "")
                            .replace("m", "")
                            .toIntOrNull() ?: throw Exception("convert to int failed")) - 1]
                    } catch (e: IndexOutOfBoundsException) {
                        throw Exception("no such singer")
                    }
                } ?: throw Exception("unsupported gender")
            } ?: throw Exception("unsupported area")

    return if (info == null) 12 to "mandarin" else when (info.first) {
        "cn", "zh", "zh-cn" -> pick("mandarin", info.second) to "mandarin"
        "jp" -> pick("japanese", info.second) to "japanese"
        "us" -> pick("english", info.second) to "english"
        else -> (info.second.toIntOrNull() ?: 12) to "mandarin"
    }
}

private val wavFileNameRegex = Regex("[\\w'\"\\-+#@:,.\\[\\]()]+")

data class SinsyConfig(
    var SPKR_LANG: String,
    var SPKR: Int,
    var VIBPOWER: Int,
    var F0SHIFT: Int,
    var SYNALPHA: Float,
    val sinsyLink: String
)

suspend fun sinsy(xmlPath: String, config: SinsyConfig, uploadCallback: ((Long, Long) -> Unit)? = null): InputStream {
    val client = HttpClient(OkHttp) {
        install(HttpTimeout)
    }

    val r = client.post {
        url("${config.sinsyLink}/index.php")
        header("User-Agent", "Mozilla/5.0")

        val body = MultiPartFormDataContent(formData {
            append("SPKR_LANG", config.SPKR_LANG)
            append("SPKR", config.SPKR)
            append("VIBPOWER", config.VIBPOWER)
            append("F0SHIFT", config.F0SHIFT)
            append("SYNALPHA", config.SYNALPHA)
            val file = File(xmlPath)
            append("SYNSRC", file.readBytes(), Headers.build {
                append(HttpHeaders.UserAgent, "Mozilla/5.0")
                append(HttpHeaders.ContentType, "text/xml")
                append(HttpHeaders.ContentDisposition, "filename=\"${file.name}\"")
            })
        })

        setBody(body)

        if (uploadCallback != null) {
//            "Sent $bytesSentTotal bytes from $contentLength"
            onUpload(uploadCallback)
        }
    }

    var rFileName: String? = null
    for (i in r.bodyAsText().split("temp/")) {
        val i1 = i.split(".")
        if (i1[1].startsWith("wav")) {
            rFileName = wavFileNameRegex.find(i1[0])?.value
            break
        }
    }

    if (rFileName == null) throw Exception("combine failed, no results found")
    return client.get("${config.sinsyLink}/temp/$rFileName.wav").readBytes().inputStream()
}