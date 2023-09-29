package org.mider.produce.cl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.mider.produce.core.Configuration
import org.mider.produce.core.SinsyConfig
import org.mider.produce.core.selectSinger
import org.mider.produce.core.utils.*
import whiter.music.mider.code.MiderCodeParserConfiguration
import whiter.music.mider.code.NotationType
import whiter.music.mider.code.ProduceCoreResult
import whiter.music.mider.code.produceCore
import whiter.music.mider.dsl.Dsl2MusicXml
import whiter.music.mider.dsl.fromDslInstance
import whiter.music.mider.xml.LyricInception
import java.io.*
import javax.sound.midi.*
import javax.sound.sampled.*


fun getConfiguration(): Pair<Configuration, File> {

    val workspaceName = ".mider_tmp"

    LyricInception.replace = {
        it.toPinyin()
    }

    val generatedWorkspace = System.getenv()["MiderTemp"]?.let {
        File(it).apply {
            if (!exists()) error("tmp folder 'MiderTemp': ($it) is not exist")
        }
    } ?: File(workspaceName).apply {
        if (exists()) {
            if (listFiles() != null && listFiles().isNotEmpty()) error("${this.absolutePath} is not empty! consider delete it or point workspace to a empty folder by setting MiderTemp env")
        } else mkdir()
    }

    val cfg = Configuration(generatedWorkspace)

    cfg.error = {
        error("[mider-error] $it")
    }

    cfg.info = {
        println("[mider-info] $it")
    }

    return cfg to generatedWorkspace
}

suspend fun Configuration.generate(
    code: String,
    miderCfg: MiderCodeParserConfiguration? = null,
    sinsyProxy: String? = null,
    sinsyCallback: ((Long, Long) -> Unit)? = null
): Pair<ProduceCoreResult, List<InputStream>> {
    info("sounds begin")

    val produceCoreResult = miderCfg?.let { produceCore(code, it) } ?: produceCore(code)
    val midiStream: InputStream = fromDslInstance(produceCoreResult.miderDSL).inStream()

    val stream: List<InputStream> = when {
        produceCoreResult.isUploadMidi -> listOf(midiStream)

        produceCoreResult.isRenderingNotation -> {
            val midi = audioUtilsGetTempFile("mid")
            midi.writeBytes(withContext(Dispatchers.IO) {
                midiStream.readAllBytes()
            })

            when (produceCoreResult.notationType) {
                NotationType.PNGS -> convert2PNGS(midi).map { it.inputStream() }
                NotationType.PDF -> listOf(convert2PDF(midi).inputStream())
                NotationType.MSCZ -> listOf(convert2MSCZ(midi).inputStream())
                else -> {
                    val errorMsg = "plz provide the output format"
                    error(errorMsg)
                    kotlin.error(errorMsg)
                }
            }
        }

        produceCoreResult.isSing -> {
            val xmlFile = audioUtilsGetTempFile("xml")
            Dsl2MusicXml(produceCoreResult.miderDSL).save(xmlFile)

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

            listOf(sinsyDownload(xmlFile.absolutePath, sinsyCfg, sinsyCallback, sinsyProxy))
        }

        else -> listOf(midiStream) // listOf(generateAudioStreamByFormatMode(midiStream))
    }

    return produceCoreResult to stream
}

fun playWav(wavData: ByteArray) {
    val inputStream: InputStream = ByteArrayInputStream(wavData)
    val audioInputStream = AudioSystem.getAudioInputStream(inputStream)
    val audioFormat = audioInputStream.format
    val dataLineInfo = DataLine.Info(SourceDataLine::class.java, audioFormat)
    val sourceDataLine = AudioSystem.getLine(dataLineInfo) as SourceDataLine
    sourceDataLine.open(audioFormat)

    sourceDataLine.start()
    var bytesRead: Int
    val buffer = ByteArray(1024)
    while (audioInputStream.read(buffer).also { bytesRead = it } != -1) {
        sourceDataLine.write(buffer, 0, bytesRead)
    }

    sourceDataLine.drain()
    sourceDataLine.close()
    audioInputStream.close()
}

fun errorPrint(msg: String) {
    print("\u001b[1;31m$msg\u001B[0m")
}
fun errorPrintln(msg: String) = errorPrint("$msg\n")

fun infoPrint(msg: String) {
    print("\u001b[1;33m$msg\u001B[0m")
}

fun infoPrintln(msg: String) = infoPrint("$msg\n")