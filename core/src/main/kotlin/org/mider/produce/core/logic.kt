package org.mider.produce.core

import org.mider.produce.core.utils.*
import whiter.music.mider.code.MiderCodeParserConfiguration
import whiter.music.mider.code.NotationType
import whiter.music.mider.code.ProduceCoreResult
import whiter.music.mider.code.produceCore
import whiter.music.mider.dsl.Dsl2MusicXml
import whiter.music.mider.dsl.fromDslInstance
import java.io.BufferedInputStream
import java.io.InputStream

suspend fun Configuration.generate(code: String, miderCfg: MiderCodeParserConfiguration? = null): Pair<ProduceCoreResult, List<Pair<InputStream, String>>> = time {
    info("sounds begin")

    val produceCoreResult = miderCfg?.let { produceCore(code, it) } ?: produceCore(code)

    /*
     produceCoreResult的内容:
     - 若干控制类变量的新值
     - 得到 miderDSL instance
     */
    val midiStream: InputStream = fromDslInstance(produceCoreResult.miderDSL).inStream()

    val result = mutableListOf<Pair<InputStream, String>>()

    if (produceCoreResult.isUploadMidi) {
        // 上传 midi
        result += midiStream to "stream"
    } else if (produceCoreResult.isRenderingNotation) {
        // 渲染 乐谱
        val midi = audioUtilsGetTempFile("mid")
        midi.writeBytes(midiStream.readAllBytes())

        when (produceCoreResult.notationType) {
            NotationType.PNGS -> result += convert2PNGS(midi).map { it.inputStream() to it.name }
            NotationType.PDF ->  result += convert2PDF(midi).let { it.inputStream() to it.name }
            NotationType.MSCZ -> result += convert2MSCZ(midi).let { it.inputStream() to it.name }
            else -> throw Exception("plz provide the output format")
        }
    } else if (produceCoreResult.isSing) {
        val xmlFile = audioUtilsGetTempFile("xml")
        val dsl2MusicXml = Dsl2MusicXml(produceCoreResult.miderDSL)
        dsl2MusicXml.save(xmlFile)

        val singer = selectSinger(produceCoreResult.singSong!!.first to produceCoreResult.singSong!!.second)
        val sinsyCfg = SinsyConfig(
            singer.second,
            singer.first,
            sinsyVibpower,
            sinsyF0shift,
            sinsySynAlpha,
            sinsyLink,
            sinsyClientRequestTimeoutMillis,
            sinsyClientConnectTimeoutMillis,
            sinsyClientSocketTimeoutMillis
        )
        val after = sinsy(xmlFile.absolutePath, sinsyCfg)
        result += generateAudioStreamByFormatModeFromWav(BufferedInputStream(after)) to "stream"
    } else {
        result += generateAudioStreamByFormatMode(midiStream) to "stream"
    }

    produceCoreResult to result
}
