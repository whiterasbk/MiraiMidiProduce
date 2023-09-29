package org.mider.produce.cl

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.mider.produce.core.SinsyConfig
import java.io.ByteArrayInputStream
import java.io.File


suspend fun sinsyDownload(xmlPath: String, config: SinsyConfig, uploadCallback: ((Long, Long) -> Unit)? = null, proxyHost: String? = null): ByteArrayInputStream {
    val client = HttpClient(OkHttp) {

        engine {
            proxy = proxyHost?.let { ProxyBuilder.http(it) }
        }

        install(HttpTimeout) {
            requestTimeoutMillis = config.sinsyClientRequestTimeoutMillis
            connectTimeoutMillis = config.sinsyClientConnectTimeoutMillis
            socketTimeoutMillis = config.sinsyClientSocketTimeoutMillis
        }
    }

    val r = client.post {
        url("${config.sinsyLink}/index.php")
        header("User-Agent", "Mozilla/5.0")

        setBody(MultiPartFormDataContent(formData {
            append("SPKR_LANG", config.SPKR_LANG)
            append("SPKR", config.SPKR)
            append("VIBPOWER", config.VIBPOWER)
            append("F0SHIFT", config.F0SHIFT)
            append("SYNALPHA", config.SYNALPHA)
            val file = File(xmlPath)
            append("SYNSRC", file.readBytes(), Headers.build {
                append(HttpHeaders.UserAgent, "Mozilla/5.0")
                append(HttpHeaders.ContentType, "text/xml")
                append(HttpHeaders.Connection, "Keep-Alive")
                append(HttpHeaders.ContentDisposition, "filename=\"${file.name}\"")
            })
        }))

        if (uploadCallback != null) {
            onUpload(uploadCallback)
        }
    }

    var rFileName: String? = null
    for (i in r.bodyAsText().split("temp/")) {
        val i1 = i.split(".")
        if (i1[1].startsWith("wav")) {
            rFileName = Regex("[\\w'\"\\-+#@:,.\\[\\]()]+").find(i1[0])?.value
            break
        }
    }

    if (rFileName == null) error("combine failed, no results found")
    return client.get("${config.sinsyLink}/temp/$rFileName.wav").readBytes().inputStream()
}