package bot.music.whiter

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import okio.utf8Size
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
    var VIBPOWER: Int = Config.sinsyVibpower,
    var F0SHIFT: Int = Config.sinsyF0shift,
    var SYNALPHA: Float = Config.sinsySynAlpha
)

suspend fun sinsy(xmlPath: String, config: SinsyConfig, sinsyLink: String = Config.sinsyLink): InputStream {
    val client = HttpClient(OkHttp)
    val r = client.post<String>() {
        url("$sinsyLink/index.php")
        header("User-Agent", "Mozilla/5.0")
        body = MultiPartFormDataContent(formData {
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

        onUpload { bytesSentTotal, contentLength ->
            ifDebug("Sent $bytesSentTotal bytes from $contentLength")
        }
    }

    var rFileName: String? = null
    for (i in r.split("temp/")) {
        val i1 = i.split(".")
        if (i1[1].startsWith("wav")) {
            rFileName = wavFileNameRegex.find(i1[0])?.value
            break
        }
    }

    if (rFileName == null) throw Exception("combine failed, no results found")
    return client.get("$sinsyLink/temp/$rFileName.wav")
}